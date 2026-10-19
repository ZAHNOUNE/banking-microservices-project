package ma.ebank.ebank.services;

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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankingTransactionServiceTest {
    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    BankingTransactionService service;

    @BeforeEach void setUp() {
        service = new BankingTransactionService(accountRepository, transactionRepository);
    }

    @Test void depositUpdatesBalanceAndCreatesTransaction() {
        Account account = Account.builder().id(1L).balance(100.0).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        Transaction saved = service.deposit(1L, 25.5);
        assertEquals(125.5, account.getBalance());
        assertEquals(25.5, saved.getAmount());
        assertEquals(TransactionType.DEPOT, saved.getType());
        assertSame(account, saved.getAccount());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test void withdrawUpdatesBalanceAndCreatesTransaction() {
        Account account = Account.builder().id(1L).balance(100.0).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        Transaction saved = service.withdraw(1L, 25.5);
        assertEquals(74.5, account.getBalance());
        assertEquals(TransactionType.RETRAIT, saved.getType());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test void withdrawRejectsInsufficientFundsWithoutWriting() {
        Account account = Account.builder().id(1L).balance(10.0).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.withdraw(1L, 50.0));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(10.0, account.getBalance());
        verify(accountRepository, never()).save(any());
        verifyNoInteractions(transactionRepository);
    }

    @Test void missingAccountReturns404() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.deposit(99L, 10.0));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(transactionRepository);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, 0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidDepositAmountReturns400(double amount) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.deposit(1L, amount));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, 0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidWithdrawalAmountReturns400(double amount) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.withdraw(1L, amount));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test void depositRejectsOverflowWithoutWriting() {
        Account account = Account.builder().id(1L).balance(Double.MAX_VALUE).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.deposit(1L, Double.MAX_VALUE));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(accountRepository, never()).save(any());
        verifyNoInteractions(transactionRepository);
    }
}
