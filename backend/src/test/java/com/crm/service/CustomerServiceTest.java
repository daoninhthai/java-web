package com.crm.service;

import com.crm.dto.CustomerDTO;
import com.crm.entity.Customer;
import com.crm.exception.DuplicateResourceException;
import com.crm.exception.ResourceNotFoundException;
import com.crm.mapper.CustomerMapper;
import com.crm.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private CustomerDTO sampleDTO;
    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleDTO = new CustomerDTO();
        sampleDTO.setId(1L);
        sampleDTO.setName("Acme Corp Contact");
        sampleDTO.setEmail("contact@acme.com");
        sampleDTO.setCompany("Acme Corporation");
        sampleDTO.setDealValue(new BigDecimal("50000.00"));
        sampleDTO.setStatus("ACTIVE");
        sampleDTO.setLastContactDate(LocalDate.of(2023, 11, 15));

        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setName("Acme Corp Contact");
        sampleCustomer.setEmail("contact@acme.com");
        sampleCustomer.setCompany("Acme Corporation");
        sampleCustomer.setDealValue(new BigDecimal("50000.00"));
        sampleCustomer.setStatus("ACTIVE");
        sampleCustomer.setLastContactDate(LocalDate.of(2023, 11, 15));
    }

    @Nested
    @DisplayName("Create Customer")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create a new customer successfully")
        void shouldCreateCustomerSuccessfully() {
            when(customerRepository.existsByEmail(sampleDTO.getEmail())).thenReturn(false);
            when(customerMapper.toEntity(sampleDTO)).thenReturn(sampleCustomer);
            when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
            when(customerMapper.toDTO(sampleCustomer)).thenReturn(sampleDTO);

            CustomerDTO result = customerService.createCustomer(sampleDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Acme Corp Contact");
            assertThat(result.getEmail()).isEqualTo("contact@acme.com");
            assertThat(result.getDealValue()).isEqualByComparingTo(new BigDecimal("50000.00"));
            verify(customerRepository).save(customerCaptor.capture());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowWhenEmailExists() {
            when(customerRepository.existsByEmail(sampleDTO.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> customerService.createCustomer(sampleDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("contact@acme.com");

            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Read Customer")
    class ReadCustomerTests {

        @Test
        @DisplayName("Should return customer by ID")
        void shouldReturnCustomerById() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(customerMapper.toDTO(sampleCustomer)).thenReturn(sampleDTO);

            CustomerDTO result = customerService.getCustomerById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCompany()).isEqualTo("Acme Corporation");
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getCustomerById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer not found with id: 999");
        }

        @Test
        @DisplayName("Should return paginated customers")
        void shouldReturnPaginatedCustomers() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Customer> customerPage = new PageImpl<>(
                    List.of(sampleCustomer), pageable, 1
            );

            when(customerRepository.findAll(pageable)).thenReturn(customerPage);
            when(customerMapper.toDTO(sampleCustomer)).thenReturn(sampleDTO);

            Page<CustomerDTO> result = customerService.getAllCustomers(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Acme Corp Contact");
        }

        @Test
        @DisplayName("Should search customers by name or company")
        void shouldSearchCustomersByNameOrCompany() {
            when(customerRepository.findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCase(
                    eq("Acme"), eq("Acme")
            )).thenReturn(List.of(sampleCustomer));
            when(customerMapper.toDTO(sampleCustomer)).thenReturn(sampleDTO);

            List<CustomerDTO> results = customerService.searchCustomers("Acme");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getCompany()).isEqualTo("Acme Corporation");
        }
    }

    @Nested
    @DisplayName("Update Customer")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Should update customer successfully")
        void shouldUpdateCustomerSuccessfully() {
            CustomerDTO updateDTO = new CustomerDTO();
            updateDTO.setName("Acme Corp Contact");
            updateDTO.setEmail("new-contact@acme.com");
            updateDTO.setCompany("Acme Corporation");
            updateDTO.setDealValue(new BigDecimal("75000.00"));
            updateDTO.setStatus("NEGOTIATION");
            updateDTO.setLastContactDate(LocalDate.now());

            Customer updatedCustomer = new Customer();
            updatedCustomer.setId(1L);
            updatedCustomer.setName("Acme Corp Contact");
            updatedCustomer.setEmail("new-contact@acme.com");
            updatedCustomer.setDealValue(new BigDecimal("75000.00"));
            updatedCustomer.setStatus("NEGOTIATION");

            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);
            when(customerMapper.toDTO(updatedCustomer)).thenReturn(updateDTO);

            CustomerDTO result = customerService.updateCustomer(1L, updateDTO);

            assertThat(result.getDealValue()).isEqualByComparingTo(new BigDecimal("75000.00"));
            assertThat(result.getStatus()).isEqualTo("NEGOTIATION");
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should update last contact date")
        void shouldUpdateLastContactDate() {
            LocalDate today = LocalDate.now();

            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
            when(customerMapper.toDTO(any(Customer.class))).thenReturn(sampleDTO);

            customerService.updateLastContactDate(1L);

            verify(customerRepository).save(customerCaptor.capture());
            assertThat(customerCaptor.getValue().getLastContactDate()).isEqualTo(today);
        }
    }

    @Nested
    @DisplayName("Delete Customer")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Should delete customer successfully")
        void shouldDeleteCustomerSuccessfully() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            doNothing().when(customerRepository).delete(sampleCustomer);

            customerService.deleteCustomer(1L);

            verify(customerRepository).delete(sampleCustomer);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent customer")
        void shouldThrowWhenDeletingNonExistent() {
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.deleteCustomer(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(customerRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Customer Statistics")
    class CustomerStatisticsTests {

        @Test
        @DisplayName("Should return customers by status")
        void shouldReturnCustomersByStatus() {
            when(customerRepository.findByStatus("ACTIVE")).thenReturn(List.of(sampleCustomer));
            when(customerMapper.toDTO(sampleCustomer)).thenReturn(sampleDTO);

            List<CustomerDTO> results = customerService.getCustomersByStatus("ACTIVE");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Should calculate total deal value")
        void shouldCalculateTotalDealValue() {
            Customer customer2 = new Customer();
            customer2.setId(2L);
            customer2.setDealValue(new BigDecimal("30000.00"));

            when(customerRepository.findByStatus("ACTIVE"))
                    .thenReturn(List.of(sampleCustomer, customer2));

            BigDecimal totalValue = customerService.getTotalDealValueByStatus("ACTIVE");

            assertThat(totalValue).isEqualByComparingTo(new BigDecimal("80000.00"));
        }
    }
}
