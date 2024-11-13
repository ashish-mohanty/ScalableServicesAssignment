package com.scalableServices.orderMicroservice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;


    private final String restaurantServiceUrl = "http://localhost:9090";

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/{customerId}/orders/{orderId}/status")
    public ResponseEntity<String> getOrderStatus(@PathVariable Long orderId, @PathVariable Long customerId) {

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            // Check if the order belongs to the authenticated customer
            if (!order.getCustomerId().equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view this order");
            }
            return ResponseEntity.ok(order.getOrderStatus());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {

        String jwtToken = extractJwtFromRequest(request);

        // Set up the Order entity
        Order order = new Order();
        order.setCustomerId(orderRequest.getCustomerId());
        order.setRestaurantId(orderRequest.getRestaurantId());
        order.setOrderStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());

        // Validate restaurantId
        if (!validateRestaurantId(order.getRestaurantId(),jwtToken)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid restaurantId");
        }

        // Process order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            // Validate menuItemId
            Double price = getMenuItemPrice(order.getRestaurantId(), itemRequest.getMenuItemId(),jwtToken);
            if (price == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid menuItemId: " + itemRequest.getMenuItemId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(itemRequest.getMenuItemId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(price);
            orderItem.setOrder(order);

            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);

        // Calculate total amount
        Double totalAmount = calculateTotalAmount(orderItems);
        order.setTotalAmount(totalAmount);

        // Save the order
        Order savedOrder = orderRepository.save(order);

        return ResponseEntity.ok(savedOrder);
    }

    private boolean validateRestaurantId(Long restaurantId,String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = restaurantServiceUrl + "/restaurants/" + restaurantId;

        try {
            ResponseEntity<?> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    private Double getMenuItemPrice(Long restaurantId, Long menuItemId, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = restaurantServiceUrl + "/restaurants/" + restaurantId + "/menuItems/" + menuItemId;

        try {
            ResponseEntity<MenuItemResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    MenuItemResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                MenuItemResponse menuItem = response.getBody();
                return menuItem.getPrice();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Double calculateTotalAmount(List<OrderItem> orderItems) {
        double total = 0.0;
        for (OrderItem item : orderItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    @Getter
    @Setter
    // Inner class to represent the response from the Restaurant Service
    private static class MenuItemResponse {
        private Double price;
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (org.springframework.util.StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


}