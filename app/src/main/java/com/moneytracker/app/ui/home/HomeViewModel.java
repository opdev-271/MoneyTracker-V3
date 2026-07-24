package com.moneytracker.app.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.moneytracker.app.MoneyTrackerApp;
import com.moneytracker.app.data.entity.Account;
import com.moneytracker.app.data.relation.TransactionWithDetails;
import com.moneytracker.app.data.repository.AccountRepository;
import com.moneytracker.app.data.repository.TransactionRepository;
import com.moneytracker.app.util.SettingsPrefs;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final LiveData<List<Account>> accounts;
    private final MutableLiveData<Long> selectedAccountId = new MutableLiveData<>();
    private final LiveData<Account> selectedAccount;
    private final LiveData<Double> selectedAccountBalance;
    private final LiveData<List<TransactionWithDetails>> transactions;
    private final MutableLiveData<Boolean> balanceHidden;

    private boolean defaultAccountApplied = false;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        MoneyTrackerApp app = MoneyTrackerApp.from(application);
        accountRepository = app.getAccountRepository();
        transactionRepository = app.getTransactionRepository();

        accounts = accountRepository.getAllAccounts();
        selectedAccount = Transformations.switchMap(selectedAccountId, id ->
                id == null ? new MutableLiveData<>(null) : accountRepository.getAccountById(id));
        selectedAccountBalance = Transformations.switchMap(selectedAccountId, id ->
                id == null ? new MutableLiveData<>(0d) : accountRepository.getAccountBalance(id));
        transactions = Transformations.switchMap(selectedAccountId, id ->
                id == null ? new MutableLiveData<>(null) : transactionRepository.getTransactionsForAccount(id));

        balanceHidden = new MutableLiveData<>(SettingsPrefs.isBalanceHidden(application));

        long savedId = SettingsPrefs.getActiveAccountId(application);
        if (savedId != -1L) {
            selectedAccountId.setValue(savedId);
            defaultAccountApplied = true;
        }
    }

    public LiveData<List<Account>> getAccounts() {
        return accounts;
    }

    public LiveData<Account> getSelectedAccount() {
        return selectedAccount;
    }

    public LiveData<Double> getSelectedAccountBalance() {
        return selectedAccountBalance;
    }

    public LiveData<List<TransactionWithDetails>> getTransactions() {
        return transactions;
    }

    public LiveData<Boolean> getBalanceHidden() {
        return balanceHidden;
    }

    /** Called once the accounts list first loads, so Home has something selected without user action. */
    public void ensureDefaultAccountSelected(List<Account> loadedAccounts) {
        if (defaultAccountApplied || loadedAccounts == null || loadedAccounts.isEmpty()) {
            return;
        }
        defaultAccountApplied = true;
        selectAccount(loadedAccounts.get(0).getId());
    }

    public void selectAccount(long accountId) {
        selectedAccountId.setValue(accountId);
        SettingsPrefs.setActiveAccountId(getApplication(), accountId);
    }

    public void toggleBalanceHidden() {
        boolean hidden = Boolean.TRUE.equals(balanceHidden.getValue());
        boolean newValue = !hidden;
        balanceHidden.setValue(newValue);
        SettingsPrefs.setBalanceHidden(getApplication(), newValue);
    }

    public Long getSelectedAccountIdValue() {
        return selectedAccountId.getValue();
    }
}
