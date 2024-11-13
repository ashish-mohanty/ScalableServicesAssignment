package com.scalableServices.customerMicroservice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class Order {


    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private String orderStatus;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private List<OrderItem> orderItems;

}