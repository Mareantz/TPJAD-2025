package com.homework.salesapi.controller;

import com.homework.salesapi.order.SalesOrder;
import com.homework.salesapi.order.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderRepository repository;

    @GetMapping
    public List<SalesOrder> getAllOrders() {
        return repository.findAll();
    }

    @PostMapping
    public SalesOrder createOrder(@RequestBody SalesOrder order) {
        return repository.save(order);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        repository.deleteById(id);
    }

    @GetMapping("/{id}")
    public SalesOrder getOrderById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public SalesOrder updateOrder(@PathVariable Long id, @RequestBody SalesOrder updatedOrder) {
        return repository.findById(id)
                .map(order -> {
                    order.setCustomerIdLink(updatedOrder.getCustomerIdLink());
                    order.setProductIdLink(updatedOrder.getProductIdLink());
                    return repository.save(order);
                })
                .orElse(null);
    }
}