package com.homework.usersapi.controller;

import com.homework.usersapi.customer.Customer;
import com.homework.usersapi.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers") // All methods in this class start with this path
public class CustomerController {

    @Autowired
    private CustomerRepository repository;

    // READ (All)
    @GetMapping
    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    // CREATE
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }

    // READ (One)
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Customer updateCustomer(@PathVariable Long id, @RequestBody Customer updatedCustomer) {
        return repository.findById(id)
                .map(customer -> {
                    customer.setFullName(updatedCustomer.getFullName());
                    customer.setEmail(updatedCustomer.getEmail());
                    return repository.save(customer);
                })
                .orElse(null); // Or throw an exception
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        repository.deleteById(id);
    }
}