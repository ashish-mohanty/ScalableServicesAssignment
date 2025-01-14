package com.scalableServices.restaurantMicroservice;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantRestaurantId(Long restaurantId);
}
