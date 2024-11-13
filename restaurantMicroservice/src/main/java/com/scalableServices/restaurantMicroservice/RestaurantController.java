package com.scalableServices.restaurantMicroservice;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Autowired
    public RestaurantController(RestaurantRepository restaurantRepository,
                                MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Get menu items for a specific restaurant.
     *
     * @param restaurantId The ID of the restaurant.
     * @return List of menu items.
     */
    @GetMapping("/{restaurantId}/menuItems")
    public ResponseEntity<List<MenuItem>> getMenuItemsByRestaurant(@PathVariable Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            return ResponseEntity.notFound().build();
        }
        List<MenuItem> menuItems = menuItemRepository.findByRestaurantRestaurantId(restaurantId);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<?> getRestaurantById(@PathVariable Long restaurantId) {
        Optional<Restaurant> restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant.isPresent()) {
            return ResponseEntity.ok(restaurant.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Restaurant not found");
        }
    }

    @GetMapping("/{restaurantId}/menuItems/{menuItemId}")
    public ResponseEntity<?> getMenuItemById(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {

        Optional<MenuItem> menuItem = menuItemRepository.findById(menuItemId);
        if (menuItem.isPresent() && menuItem.get().getRestaurant().getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.ok(menuItem.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu item not found");
        }
    }


}