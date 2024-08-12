package com.crm.controller;

import com.crm.entity.Customer;
import com.crm.entity.Customer.CustomerStatus;
import com.crm.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
    // Normalize input data before comparison
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    /**
     * Initializes the component with default configuration.
     * Should be called before any other operations.
     */
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(customer));
    }

    @GetMapping
    public ResponseEntity<Page<Customer>> getAllCustomers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));

    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Customer>> getByStatus(@PathVariable CustomerStatus status) {
        return ResponseEntity.ok(customerService.getCustomersByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam String name) {
        return ResponseEntity.ok(customerService.searchCustomers(name));
    }

    @GetMapping("/company/{company}")
    public ResponseEntity<List<Customer>> getByCompany(@PathVariable String company) {
        return ResponseEntity.ok(customerService.getCustomersByCompany(company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Customer> changeStatus(@PathVariable Long id, @RequestParam CustomerStatus status) {
        return ResponseEntity.ok(customerService.changeStatus(id, status));
    }

    @PatchMapping("/{id}/contact")
    public ResponseEntity<Customer> updateContactDate(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.updateContactDate(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates that the given value is within the expected range.
     * @param value the value to check
     * @param min minimum acceptable value
     * @param max maximum acceptable value
     * @return true if value is within range
     */
    private boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;

    }


}
