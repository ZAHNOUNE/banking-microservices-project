package ma.ebank.ebank.controllers;

import ma.ebank.ebank.entities.Account;
import ma.ebank.ebank.entities.Transaction;
import ma.ebank.ebank.entities.TransactionType;
import ma.ebank.ebank.feign.CustomerRestClient;
import ma.ebank.ebank.repositories.AccountRepository;
import ma.ebank.ebank.repositories.TransactionRepository;
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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AccountController controller = new AccountController(
                accountRepository,
                transactionRepository,
                customerRestClient
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void depositAcceptsPositiveFiniteAmount() throws Exception {
        Account account = Account.builder().id(1L).balance(100.0).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/accounts/1/depot").param("amount", "25.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25.5))
                .andExpect(jsonPath("$.type").value("DEPOT"));

        assertEquals(125.5, account.getBalance());
        verify(accountRepository).save(account);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(TransactionType.DEPOT, transactionCaptor.getValue().getType());
    }

    @Test
    void withdrawAcceptsPositiveFiniteAmount() throws Exception {
        Account account = Account.builder().id(1L).balance(100.0).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/accounts/1/retrait").param("amount", "25.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25.5))
                .andExpect(jsonPath("$.type").value("RETRAIT"));

        assertEquals(74.5, account.getBalance());
        verify(accountRepository).save(account);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(TransactionType.RETRAIT, transactionCaptor.getValue().getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "NaN", "Infinity", "-Infinity"})
    void depositRejectsInvalidAmount(String amount) throws Exception {
        mockMvc.perform(post("/accounts/1/depot").param("amount", amount))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "NaN", "Infinity", "-Infinity"})
    void withdrawRejectsInvalidAmount(String amount) throws Exception {
        mockMvc.perform(post("/accounts/1/retrait").param("amount", amount))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountRepository, transactionRepository);
    }
}
