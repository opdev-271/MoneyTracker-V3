package com.moneytracker.app.data.repository;

import androidx.lifecycle.LiveData;

import com.moneytracker.app.data.AppDatabase;
import com.moneytracker.app.data.dao.AccountDao;
import com.moneytracker.app.data.entity.Account;
import com.moneytracker.app.data.relation.AccountWithBalance;
import com.moneytracker.app.util.AppExecutors;

import java.util.List;

public class AccountRepository {

    private final AccountDao accountDao;
    private final AppExecutors executors;

    public AccountRepository(AppDatabase database, AppExecutors executors) {
        this.accountDao = database.accountDao();
        this.executors = executors;
    }

    public LiveData<List<Account>> getAllAccounts() {
        return accountDao.getAllAccounts();
    }

    public LiveData<Account> getAccountById(long accountId) {
        return accountDao.getAccountById(accountId);
    }

    public LiveData<Double> getAccountBalance(long accountId) {
        return accountDao.getAccountBalance(accountId);
    }

    public LiveData<List<AccountWithBalance>> getAccountsWithBalance() {
        return accountDao.getAccountsWithBalance();
    }

    public void insert(Account account, OnAccountSaved callback) {
        executors.diskIO().execute(() -> {
            long id = accountDao.insert(account);
            if (callback != null) {
                executors.mainThread().execute(() -> callback.onSaved(id));
            }
        });
    }

    public void update(Account account) {
        executors.diskIO().execute(() -> accountDao.update(account));
    }

    public void delete(Account account) {
        executors.diskIO().execute(() -> accountDao.delete(account));
    }

    public interface OnAccountSaved {
        void onSaved(long accountId);
    }
}
