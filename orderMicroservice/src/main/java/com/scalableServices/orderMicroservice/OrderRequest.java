package com.scalableServices.orderMicroservice;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    private Long customerId;
    private Long restaurantId;
    private List<OrderItemRequest> orderItems;

}