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

    // READ (All)
    @GetMapping
    public List<SalesOrder> getAllOrders() {
        return repository.findAll();
    }

    // CREATE (This is the "link"!)
    @PostMapping
    public SalesOrder createOrder(@RequestBody SalesOrder order) {
        // The 'order' object will just have customerIdLink and productIdLink
        // The @PrePersist in the entity will set the date
        return repository.save(order);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        repository.deleteById(id);
    }

    @GetMapping("/{id}")
    public SalesOrder getOrderById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    // UPDATE
    @PutMapping("/{id}")
    public SalesOrder updateOrder(@PathVariable Long id, @RequestBody SalesOrder updatedOrder) {
        return repository.findById(id)
                .map(order -> {
                    // We're only allowing the links to be updated. The date is a "created" date.
                    order.setCustomerIdLink(updatedOrder.getCustomerIdLink());
                    order.setProductIdLink(updatedOrder.getProductIdLink());
                    return repository.save(order);
                })
                .orElse(null);
    }
}