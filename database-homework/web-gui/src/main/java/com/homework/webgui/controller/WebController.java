package com.homework.webgui.controller;

// Import the DTOs
import com.homework.webgui.dto.Customer;
import com.homework.webgui.dto.OrderDetails;
import com.homework.webgui.dto.Product;
import com.homework.webgui.dto.SalesOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final RestTemplate restTemplate;
    private final String usersApiUrl;
    private final String productsApiUrl;
    private final String salesApiUrl;

    @Autowired
    public WebController(RestTemplate restTemplate,
                         @Value("${api.users.url}") String usersApiUrl,
                         @Value("${api.products.url}") String productsApiUrl,
                         @Value("${api.sales.url}") String salesApiUrl) {
        this.restTemplate = restTemplate;
        this.usersApiUrl = usersApiUrl + "/api/customers"; // Add the path
        this.productsApiUrl = productsApiUrl + "/api/products"; // Add the path
        this.salesApiUrl = salesApiUrl + "/api/orders"; // Add the path
    }

    @GetMapping("/")
    public ModelAndView getHomePage() {
        Map<String, Object> model = new HashMap<>();

        try {
            // --- 1. FETCH ALL DATA ---

            // Get Customers and put them in a Map for fast lookup
            List<Customer> customers = restTemplate.exchange(
                    usersApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<Customer>>() {}
            ).getBody();
            Map<Long, Customer> customerMap = customers.stream()
                    .collect(Collectors.toMap(Customer::getId, Function.identity()));

            // Get Products and put them in a Map
            List<Product> products = restTemplate.exchange(
                    productsApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<Product>>() {}
            ).getBody();
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));

            // Get Orders
            List<SalesOrder> orders = restTemplate.exchange(
                    salesApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<SalesOrder>>() {}
            ).getBody();

            // --- 2. PERFORM THE "APPLICATION-LEVEL JOIN" ---
            List<OrderDetails> orderDetailsList = new ArrayList<>();
            for (SalesOrder order : orders) {
                OrderDetails details = new OrderDetails();
                details.setId(order.getId());
                details.setOrderDate(order.getOrderDate());

                // Look up the Customer name
                Customer customer = customerMap.get(order.getCustomerIdLink());
                details.setCustomerId(order.getCustomerIdLink());
                details.setCustomerName(customer != null ? customer.getFullName() : "N/A (ID not found)");

                // Look up the Product name
                Product product = productMap.get(order.getProductIdLink());
                details.setProductId(order.getProductIdLink());
                details.setProductName(product != null ? product.getProductName() : "N/A (ID not found)");

                orderDetailsList.add(details);
            }

            // --- 3. SEND DATA TO THE GUI ---
            model.put("customers", customers); // Still send original lists for their tables
            model.put("products", products);
            model.put("orders", orderDetailsList); // <-- Send the new "merged" list

        } catch (RestClientException e) {
            System.err.println("Error fetching data: " + e.getMessage());
            model.put("customers", Collections.emptyList());
            model.put("products", Collections.emptyList());
            model.put("orders", Collections.emptyList());
        }

        return new ModelAndView("index", model);
    }

    @PostMapping("/create-customer")
    public String createCustomer(@RequestParam String fullName, @RequestParam String email) {
        try {
            // 1. Create a new Customer DTO
            Customer newCustomer = new Customer();
            newCustomer.setFullName(fullName);
            newCustomer.setEmail(email);

            // 2. Send it to the users-api
            restTemplate.postForObject(usersApiUrl, newCustomer, Customer.class);

        } catch (RestClientException e) {
            System.err.println("Error creating customer: " + e.getMessage());
        }

        // 3. Redirect back to the home page
        return "redirect:/";
    }

    /**
     * Handles the "New Product" form submission.
     */
    @PostMapping("/create-product")
    public String createProduct(@RequestParam String productName, @RequestParam double price) {
        try {
            Product newProduct = new Product();
            newProduct.setProductName(productName);
            newProduct.setPrice(price);

            // Send it to the products-api
            restTemplate.postForObject(productsApiUrl, newProduct, Product.class);

        } catch (RestClientException e) {
            System.err.println("Error creating product: " + e.getMessage());
        }

        return "redirect:/";
    }

    /**
     * Handles the "New Order" form submission.
     * This is the "link" implementation.
     */
    @PostMapping("/create-order")
    public String createOrder(@RequestParam Long customerIdLink, @RequestParam Long productIdLink) {
        try {
            // --- This is the application-level link validation ---
            // 1. Check if Customer ID exists
            try {
                restTemplate.getForObject(usersApiUrl + "/" + customerIdLink, Customer.class);
            } catch (RestClientException e) {
                System.err.println("Invalid Customer ID: " + customerIdLink);
                return "redirect:/?error=Invalid Customer ID"; // Show an error
            }

            // 2. Check if Product ID exists (We need to add this API endpoint first!)
            // For now, we'll assume it exists. We can add this check later.

            // 3. If checks pass, create the order
            SalesOrder newOrder = new SalesOrder();
            newOrder.setCustomerIdLink(customerIdLink);
            newOrder.setProductIdLink(productIdLink);

            // Send it to the sales-api
            restTemplate.postForObject(salesApiUrl, newOrder, SalesOrder.class);

        } catch (RestClientException e) {
            System.err.println("Error creating order: " + e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/delete-customer/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        try {
            // Call the DELETE endpoint on the users-api
            restTemplate.delete(usersApiUrl + "/" + id);
        } catch (RestClientException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable Long id) {
        try {
            // Call the DELETE endpoint on the products-api
            restTemplate.delete(productsApiUrl + "/" + id);
        } catch (RestClientException e) {
            System.err.println("Error deleting product: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/delete-order/{id}")
    public String deleteOrder(@PathVariable Long id) {
        try {
            // Call the DELETE endpoint on the sales-api
            restTemplate.delete(salesApiUrl + "/" + id);
        } catch (RestClientException e) {
            System.err.println("Error deleting order: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/edit-customer/{id}")
    public ModelAndView showEditCustomerPage(@PathVariable Long id) {
        Map<String, Object> model = new HashMap<>();
        try {
            // Call the users-api to get the existing customer data
            Customer customer = restTemplate.getForObject(usersApiUrl + "/" + id, Customer.class);
            model.put("customer", customer); // Add the customer to the model

        } catch (RestClientException e) {
            System.err.println("Error fetching customer for edit: " + e.getMessage());
            // On error, send an empty object
            model.put("customer", new Customer());
        }

        // Return the new "edit-customer.html" template
        return new ModelAndView("edit-customer", model);
    }

    /**
     * STEP 2: Process the "Edit Customer" form submission.
     * This is called when the user clicks "Update" on the edit page.
     */
    @PostMapping("/update-customer")
    public String updateCustomer(@ModelAttribute Customer customer) {
        try {
            // Call the users-api's PUT endpoint to send the updated customer object
            // We use restTemplate.put() which returns void
            restTemplate.exchange(
                    usersApiUrl + "/" + customer.getId(),
                    HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(customer), // Send customer in request body
                    Void.class); // We don't expect a response body

        } catch (RestClientException e) {
            System.err.println("Error updating customer: " + e.getMessage());
        }

        // Redirect back to the home page
        return "redirect:/";
    }

    @GetMapping("/edit-product/{id}")
    public ModelAndView showEditProductPage(@PathVariable Long id) {
        Map<String, Object> model = new HashMap<>();
        try {
            Product product = restTemplate.getForObject(productsApiUrl + "/" + id, Product.class);
            model.put("product", product);
        } catch (RestClientException e) {
            model.put("product", new Product());
        }
        return new ModelAndView("edit-product", model);
    }

    @PostMapping("/update-product")
    public String updateProduct(@ModelAttribute Product product) {
        try {
            restTemplate.exchange(
                    productsApiUrl + "/" + product.getId(),
                    HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(product),
                    Void.class);
        } catch (RestClientException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/edit-order/{id}")
    public ModelAndView showEditOrderPage(@PathVariable Long id) {
        Map<String, Object> model = new HashMap<>();
        try {
            SalesOrder order = restTemplate.getForObject(salesApiUrl + "/" + id, SalesOrder.class);
            model.put("order", order);
        } catch (RestClientException e) {
            model.put("order", new SalesOrder());
        }
        return new ModelAndView("edit-order", model);
    }

    @PostMapping("/update-order")
    public String updateOrder(@ModelAttribute SalesOrder order) {
        try {
            // Note: We're not re-validating the Customer/Product IDs for simplicity
            // But you could add the same checks from your createOrder method here
            restTemplate.exchange(
                    salesApiUrl + "/" + order.getId(),
                    HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(order),
                    Void.class);
        } catch (RestClientException e) {
            System.err.println("Error updating order: " + e.getMessage());
        }
        return "redirect:/";
    }
}