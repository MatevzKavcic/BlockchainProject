package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionPool {
    private HashMap<String, Transaction> transactionPool;

    public TransactionPool() {
        this.transactionPool = new HashMap<>();
    }

    public void addTransaction(Transaction transaction) {
        transactionPool.put(transaction.getTransactionId(), transaction);
    }

    public List<Transaction> getTransactionsForBlock(int maxCount) {
        List<Transaction> selectedTransactions = transactionPool.values()
                .stream()
                .limit(maxCount)
                .collect(Collectors.toList());

        // Remove selected transactions from the pool
        for (Transaction transaction : selectedTransactions) {
            transactionPool.remove(transaction.getTransactionId());
        }

        return selectedTransactions;
    }

    public int size() {
        return transactionPool.size();
    }
}
