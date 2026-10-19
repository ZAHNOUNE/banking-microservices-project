package ma.ebank.ebank.services;

import lombok.RequiredArgsConstructor;
import ma.ebank.ebank.entities.Account;
import ma.ebank.ebank.entities.Transaction;
import ma.ebank.ebank.entities.TransactionType;
import ma.ebank.ebank.repositories.AccountRepository;
import ma.ebank.ebank.repositories.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class BankingTransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction deposit(Long accountId, double amount) {
        return execute(accountId, amount, TransactionType.DEPOT);
    }

    @Transactional
    public Transaction withdraw(Long accountId, double amount) {
        return execute(accountId, amount, TransactionType.RETRAIT);
    }

    private Transaction execute(Long accountId, double amount, TransactionType type) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le montant doit être strictement positif et fini");
        }
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte introuvable"));
        if (!Double.isFinite(account.getBalance()) || account.getBalance() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solde du compte invalide");
        }
        if (type == TransactionType.RETRAIT && account.getBalance() < amount) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solde insuffisant");
        }
        double balance = type == TransactionType.DEPOT
                ? account.getBalance() + amount : account.getBalance() - amount;
        if (!Double.isFinite(balance)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant hors limites");
        }
        account.setBalance(balance);
        accountRepository.save(account);
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .date(new Date())
                .type(type)
                .account(account)
                .build();
        return transactionRepository.save(transaction);
    }
}
