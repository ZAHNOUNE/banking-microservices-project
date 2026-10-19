package ma.ebank.ebank.tools;

import ma.ebank.ebank.entities.Account;
import ma.ebank.ebank.entities.Transaction;
import ma.ebank.ebank.entities.TransactionType;
import ma.ebank.ebank.repositories.AccountRepository;
import ma.ebank.ebank.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountToolsTest {
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    private AccountTools tools;

    @BeforeEach
    void setUp() {
        tools = new AccountTools(accountRepository, transactionRepository);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-100.0, -1.0, 0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void depositRejectsInvalidAmounts(double amount) {
        assertThrows(IllegalArgumentException.class, () -> tools.deposit(1L, amount));
        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-100.0, -1.0, 0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void withdrawRejectsInvalidAmounts(double amount) {
        assertThrows(IllegalArgumentException.class, () -> tools.withdraw(1L, amount));
        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void depositUpdatesBalanceForPositiveAmount() {
        Account account = Account.builder().id(1L).balance(100.0).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transaction = tools.deposit(1L, 25.0);
        assertEquals(125.0, account.getBalance());
        assertEquals(25.0, transaction.getAmount());
        assertEquals(TransactionType.DEPOT, transaction.getType());
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawUpdatesBalanceForPositiveAmount() {
        Account account = Account.builder().id(1L).balance(100.0).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transaction = tools.withdraw(1L, 25.0);
        assertEquals(75.0, account.getBalance());
        assertEquals(25.0, transaction.getAmount());
        assertEquals(TransactionType.RETRAIT, transaction.getType());
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawRejectsInsufficientBalance() {
        Account account = Account.builder().id(1L).balance(10.0).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThrows(RuntimeException.class, () -> tools.withdraw(1L, 25.0));
        assertEquals(10.0, account.getBalance());
        verifyNoInteractions(transactionRepository);
    }
}
