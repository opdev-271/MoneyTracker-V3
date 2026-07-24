package com.moneytracker.app.data.repository;

import androidx.lifecycle.LiveData;

import com.moneytracker.app.data.AppDatabase;
import com.moneytracker.app.data.dao.TransactionDao;
import com.moneytracker.app.data.entity.Transaction;
import com.moneytracker.app.data.relation.TransactionWithDetails;
import com.moneytracker.app.util.AppExecutors;

import java.util.List;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final AppExecutors executors;

    public TransactionRepository(AppDatabase database, AppExecutors executors) {
        this.transactionDao = database.transactionDao();
        this.executors = executors;
    }

    public LiveData<List<TransactionWithDetails>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    public LiveData<List<TransactionWithDetails>> getRecentTransactions(int limit) {
        return transactionDao.getRecentTransactions(limit);
    }

    public LiveData<List<TransactionWithDetails>> getTransactionsForAccount(long accountId) {
        return transactionDao.getTransactionsForAccount(accountId);
    }

    public LiveData<List<TransactionWithDetails>> getTransactionsBetween(long startMillis, long endMillis) {
        return transactionDao.getTransactionsBetween(startMillis, endMillis);
    }

    public LiveData<Double> getTotalIncomeBetween(long startMillis, long endMillis) {
        return transactionDao.getTotalIncomeBetween(startMillis, endMillis);
    }

    public LiveData<Double> getTotalExpenseBetween(long startMillis, long endMillis) {
        return transactionDao.getTotalExpenseBetween(startMillis, endMillis);
    }

    public LiveData<List<TransactionDao.CategoryTotal>> getExpenseTotalsByCategory(long startMillis, long endMillis) {
        return transactionDao.getExpenseTotalsByCategory(startMillis, endMillis);
    }

    public void insert(Transaction transaction, OnTransactionSaved callback) {
        executors.diskIO().execute(() -> {
            long id = transactionDao.insert(transaction);
            if (callback != null) {
                executors.mainThread().execute(() -> callback.onSaved(id));
            }
        });
    }

    public void update(Transaction transaction) {
        executors.diskIO().execute(() -> transactionDao.update(transaction));
    }

    public void delete(Transaction transaction) {
        executors.diskIO().execute(() -> transactionDao.delete(transaction));
    }

    public interface OnTransactionSaved {
        void onSaved(long transactionId);
    }
}
