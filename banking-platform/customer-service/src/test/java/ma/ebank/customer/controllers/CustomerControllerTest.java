package ma.ebank.customer.controllers;

import ma.ebank.customer.entities.Customer;
import ma.ebank.customer.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {
    @Mock CustomerRepository repository;
    MockMvc mvc;

    @BeforeEach void setup() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new CustomerController(repository))
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test void missingCustomerIs404() throws Exception {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        mvc.perform(get("/customers/42")).andExpect(status().isNotFound());
    }

    @Test void missingCustomerUpdateIs404() throws Exception {
        when(repository.existsById(42L)).thenReturn(false);
        mvc.perform(put("/customers/42").contentType("application/json")
                .content("{\"name\":\"Client\",\"email\":\"client@example.com\"}"))
                .andExpect(status().isNotFound());
        verify(repository, never()).save(any());
    }

    @Test void missingCustomerDeleteIs404() throws Exception {
        when(repository.existsById(42L)).thenReturn(false);
        mvc.perform(delete("/customers/42")).andExpect(status().isNotFound());
        verify(repository, never()).deleteById(anyLong());
    }

    @Test void blankNameIs400() throws Exception {
        mvc.perform(post("/customers").contentType("application/json")
                .content("{\\"name\\":\\" \\",\\"email\\":\\"valid@example.com\\"}"))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    @Test void invalidEmailIs400() throws Exception {
        mvc.perform(post("/customers").contentType("application/json")
                .content("{\\"name\\":\\"Client\\",\\"email\\":\\"not-an-email\\"}"))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    @Test void validCustomerCanBeCreated() throws Exception {
        when(repository.save(any(Customer.class))).thenAnswer(a -> a.getArgument(0));
        mvc.perform(post("/customers").contentType("application/json")
                .content("{\"name\":\"Fatima\",\"email\":\"fatima@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Fatima"));
    }
}
