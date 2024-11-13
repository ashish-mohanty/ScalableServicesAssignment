package com.scalableServices.customerMicroservice;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Restaurant {

    private Long restaurantId;
    private String name;
    private String address;
    private String contactNumber;
    private String cuisineType;

    private List<MenuItem> menuItems;
}