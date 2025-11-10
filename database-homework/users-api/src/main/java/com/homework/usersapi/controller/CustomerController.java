package com.homework.usersapi.controller;

import com.homework.usersapi.customer.Customer;
import com.homework.usersapi.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository repository;

    @GetMapping
    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Customer updateCustomer(@PathVariable Long id, @RequestBody Customer updatedCustomer) {
        return repository.findById(id)
                .map(customer -> {
                    customer.setFullName(updatedCustomer.getFullName());
                    customer.setEmail(updatedCustomer.getEmail());
                    return repository.save(customer);
                })
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        repository.deleteById(id);
    }
}