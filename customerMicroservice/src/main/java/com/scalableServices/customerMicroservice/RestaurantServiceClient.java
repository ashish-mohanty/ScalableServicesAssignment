package com.scalableServices.customerMicroservice;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class RestaurantServiceClient {

    private final RestTemplate restTemplate;

    @Autowired
    public RestaurantServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private final String RESTAURANT_SERVICE_URL = "http://localhost:9090/restaurants";
    private final String ORDER_SERVICE_URL = "http://localhost:8080/orders";

    public List<Restaurant> getAllRestaurants(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Restaurant[]> response = restTemplate.exchange(
                RESTAURANT_SERVICE_URL,
                HttpMethod.GET,
                entity,
                Restaurant[].class
        );
        return Arrays.asList(response.getBody());
    }

    public ResponseEntity<?> placeOrder(OrderRequest orderRequest, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderRequest> entity = new HttpEntity<>(orderRequest, headers);

        try {
            ResponseEntity<?> response = restTemplate.exchange(
                    ORDER_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    Object.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing order");
        }
    }

    public ResponseEntity<String> getOrderStatusByOrderId(Long customerId, Long orderId, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    ORDER_SERVICE_URL +"/" + customerId+ "/orders/" + orderId + "/status",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving order status");
        }
    }
}
