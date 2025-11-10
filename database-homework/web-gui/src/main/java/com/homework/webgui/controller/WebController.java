package com.homework.webgui.controller;

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
        this.usersApiUrl = usersApiUrl + "/api/customers";
        this.productsApiUrl = productsApiUrl + "/api/products";
        this.salesApiUrl = salesApiUrl + "/api/orders";
    }

    @GetMapping("/")
    public ModelAndView getHomePage() {
        Map<String, Object> model = new HashMap<>();

        try {
            List<Customer> customers = restTemplate.exchange(
                    usersApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<Customer>>() {}
            ).getBody();
            Map<Long, Customer> customerMap = customers.stream()
                    .collect(Collectors.toMap(Customer::getId, Function.identity()));

            List<Product> products = restTemplate.exchange(
                    productsApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<Product>>() {}
            ).getBody();
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));

            List<SalesOrder> orders = restTemplate.exchange(
                    salesApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<SalesOrder>>() {}
            ).getBody();

            List<OrderDetails> orderDetailsList = new ArrayList<>();
            for (SalesOrder order : orders) {
                OrderDetails details = new OrderDetails();
                details.setId(order.getId());
                details.setOrderDate(order.getOrderDate());

                Customer customer = customerMap.get(order.getCustomerIdLink());
                details.setCustomerId(order.getCustomerIdLink());
                details.setCustomerName(customer != null ? customer.getFullName() : "N/A (ID not found)");

                Product product = productMap.get(order.getProductIdLink());
                details.setProductId(order.getProductIdLink());
                details.setProductName(product != null ? product.getProductName() : "N/A (ID not found)");

                orderDetailsList.add(details);
            }

            model.put("customers", customers);
            model.put("products", products);
            model.put("orders", orderDetailsList);

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
            Customer newCustomer = new Customer();
            newCustomer.setFullName(fullName);
            newCustomer.setEmail(email);

            restTemplate.postForObject(usersApiUrl, newCustomer, Customer.class);

        } catch (RestClientException e) {
            System.err.println("Error creating customer: " + e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/create-product")
    public String createProduct(@RequestParam String productName, @RequestParam double price) {
        try {
            Product newProduct = new Product();
            newProduct.setProductName(productName);
            newProduct.setPrice(price);

            restTemplate.postForObject(productsApiUrl, newProduct, Product.class);

        } catch (RestClientException e) {
            System.err.println("Error creating product: " + e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/create-order")
    public String createOrder(@RequestParam Long customerIdLink, @RequestParam Long productIdLink) {
        try {
            try {
                restTemplate.getForObject(usersApiUrl + "/" + customerIdLink, Customer.class);
            } catch (RestClientException e) {
                System.err.println("Invalid Customer ID: " + customerIdLink);
                return "redirect:/?error=Invalid Customer ID";
            }

            SalesOrder newOrder = new SalesOrder();
            newOrder.setCustomerIdLink(customerIdLink);
            newOrder.setProductIdLink(productIdLink);

            restTemplate.postForObject(salesApiUrl, newOrder, SalesOrder.class);

        } catch (RestClientException e) {
            System.err.println("Error creating order: " + e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/delete-customer/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        try {
            restTemplate.delete(usersApiUrl + "/" + id);
        } catch (RestClientException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable Long id) {
        try {
            restTemplate.delete(productsApiUrl + "/" + id);
        } catch (RestClientException e) {
            System.err.println("Error deleting product: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/delete-order/{id}")
    public String deleteOrder(@PathVariable Long id) {
        try {
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
            Customer customer = restTemplate.getForObject(usersApiUrl + "/" + id, Customer.class);
            model.put("customer", customer);

        } catch (RestClientException e) {
            System.err.println("Error fetching customer for edit: " + e.getMessage());
            model.put("customer", new Customer());
        }

        return new ModelAndView("edit-customer", model);
    }

    @PostMapping("/update-customer")
    public String updateCustomer(@ModelAttribute Customer customer) {
        try {
            restTemplate.exchange(
                    usersApiUrl + "/" + customer.getId(),
                    HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(customer),
                    Void.class);

        } catch (RestClientException e) {
            System.err.println("Error updating customer: " + e.getMessage());
        }

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