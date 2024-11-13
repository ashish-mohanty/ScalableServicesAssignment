package com.scalableServices.customerMicroservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuItem {


    private Long menuItemId;
    private Restaurant restaurant;
    private String itemName;
    private String description;
    private Double price;
}
