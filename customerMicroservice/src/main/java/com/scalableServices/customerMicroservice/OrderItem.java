package com.scalableServices.customerMicroservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OrderItem {


    private Long orderItemId;
    private Order order;
    private Long menuItemId;
    private Integer quantity;
    private Double price;


}