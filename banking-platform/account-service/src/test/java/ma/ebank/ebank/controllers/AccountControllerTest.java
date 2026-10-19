package ma.ebank.ebank.controllers;

import ma.ebank.ebank.entities.Account;
import ma.ebank.ebank.entities.Transaction;
import ma.ebank.ebank.entities.TransactionType;
import ma.ebank.ebank.feign.CustomerRestClient;
import ma.ebank.ebank.repositories.AccountRepository;
import ma.ebank.ebank.repositories.TransactionRepository;
import ma.ebank.ebank.services.BankingTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRestClient customerRestClient;

    @Mock
    private BankingTransactionService bankingTransactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AccountController controller = new AccountController(
                accountRepository,
                transactionRepository,
                customerRestClient,
                bankingTransactionService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void missingAccountIs404() throws Exception {
        when(accountRepository.findById(42L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/accounts/42")).andExpect(status().isNotFound());
    }

    @Test
    void missingAccountDetailsIs404() throws Exception {
        when(accountRepository.findById(42L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/accounts/42/details")).andExpect(status().isNotFound());
        verifyNoInteractions(customerRestClient);
    }

    @Test
    void depositAcceptsPositiveFiniteAmount() throws Exception {


        when(bankingTransactionService.deposit(1L, 25.5)).thenReturn(Transaction.builder().amount(25.5).type(TransactionType.DEPOT).build());
        mockMvc.perform(post("/accounts/1/depot").param("amount", "25.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25.5))
                .andExpect(jsonPath("$.type").value("DEPOT"));

        verify(bankingTransactionService).deposit(1L, 25.5);
    }

    @Test
    void withdrawAcceptsPositiveFiniteAmount() throws Exception {


        when(bankingTransactionService.withdraw(1L, 25.5)).thenReturn(Transaction.builder().amount(25.5).type(TransactionType.RETRAIT).build());
        mockMvc.perform(post("/accounts/1/retrait").param("amount", "25.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25.5))
                .andExpect(jsonPath("$.type").value("RETRAIT"));

        verify(bankingTransactionService).withdraw(1L, 25.5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "NaN", "Infinity", "-Infinity"})
    void depositRejectsInvalidAmount(String amount) throws Exception {
        org.mockito.Mockito.when(bankingTransactionService.deposit(
                org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.anyDouble()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Montant invalide"));
        mockMvc.perform(post("/accounts/1/depot").param("amount", amount))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "NaN", "Infinity", "-Infinity"})
    void withdrawRejectsInvalidAmount(String amount) throws Exception {
        org.mockito.Mockito.when(bankingTransactionService.withdraw(
                org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.anyDouble()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Montant invalide"));
        mockMvc.perform(post("/accounts/1/retrait").param("amount", amount))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(accountRepository, transactionRepository);
    }
}
