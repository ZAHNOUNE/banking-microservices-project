package ma.ebank.ebank.tools;

import ma.ebank.ebank.entities.Transaction;
import ma.ebank.ebank.entities.TransactionType;
import ma.ebank.ebank.repositories.AccountRepository;
import ma.ebank.ebank.repositories.TransactionRepository;
import ma.ebank.ebank.services.BankingTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountToolsTest {
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BankingTransactionService bankingTransactionService;
    private AccountTools tools;

    @BeforeEach
    void setUp() {
        tools = new AccountTools(accountRepository, transactionRepository, bankingTransactionService);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-100.0, -1.0, 0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void depositRejectsInvalidAmounts(double amount) {
        when(bankingTransactionService.deposit(1L, amount))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant invalide"));
        assertThrows(ResponseStatusException.class, () -> tools.deposit(1L, amount));
        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-100.0, -1.0, 0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void withdrawRejectsInvalidAmounts(double amount) {
        when(bankingTransactionService.withdraw(1L, amount))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant invalide"));
        assertThrows(ResponseStatusException.class, () -> tools.withdraw(1L, amount));
        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void depositDelegatesToBusinessService() {
        Transaction transaction = Transaction.builder().amount(25.0).type(TransactionType.DEPOT).build();
        when(bankingTransactionService.deposit(1L, 25.0)).thenReturn(transaction);
        assertSame(transaction, tools.deposit(1L, 25.0));
        verify(bankingTransactionService).deposit(1L, 25.0);
    }

    @Test
    void withdrawDelegatesToBusinessService() {
        Transaction transaction = Transaction.builder().amount(25.0).type(TransactionType.RETRAIT).build();
        when(bankingTransactionService.withdraw(1L, 25.0)).thenReturn(transaction);
        assertSame(transaction, tools.withdraw(1L, 25.0));
        verify(bankingTransactionService).withdraw(1L, 25.0);
    }

    @Test
    void withdrawRejectsInsufficientBalance() {
        when(bankingTransactionService.withdraw(1L, 25.0))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Solde insuffisant"));
        assertThrows(ResponseStatusException.class, () -> tools.withdraw(1L, 25.0));
        verifyNoInteractions(accountRepository, transactionRepository);
    }
}
