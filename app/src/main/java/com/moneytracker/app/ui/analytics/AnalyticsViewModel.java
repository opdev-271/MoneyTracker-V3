package com.moneytracker.app.ui.analytics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.moneytracker.app.MoneyTrackerApp;
import com.moneytracker.app.data.dao.TransactionDao;
import com.moneytracker.app.data.repository.TransactionRepository;

import java.util.Calendar;
import java.util.List;

public class AnalyticsViewModel extends AndroidViewModel {

    public enum Period {
        WEEK, MONTH, YEAR
    }

    private final TransactionRepository transactionRepository;

    private final MutableLiveData<Period> period = new MutableLiveData<>(Period.MONTH);
    private final LiveData<long[]> dateRange;

    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final LiveData<List<TransactionDao.CategoryTotal>> categoryTotals;

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = MoneyTrackerApp.from(application).getTransactionRepository();

        dateRange = Transformations.map(period, AnalyticsViewModel::computeRange);
        totalIncome = Transformations.switchMap(dateRange, r ->
                transactionRepository.getTotalIncomeBetween(r[0], r[1]));
        totalExpense = Transformations.switchMap(dateRange, r ->
                transactionRepository.getTotalExpenseBetween(r[0], r[1]));
        categoryTotals = Transformations.switchMap(dateRange, r ->
                transactionRepository.getExpenseTotalsByCategory(r[0], r[1]));
    }

    private static long[] computeRange(Period period) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        switch (period) {
            case WEEK:
                start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
                break;
            case YEAR:
                start.set(Calendar.DAY_OF_YEAR, 1);
                break;
            case MONTH:
            default:
                start.set(Calendar.DAY_OF_MONTH, 1);
                break;
        }

        Calendar end = (Calendar) start.clone();
        switch (period) {
            case WEEK:
                end.add(Calendar.DAY_OF_YEAR, 7);
                break;
            case YEAR:
                end.add(Calendar.YEAR, 1);
                break;
            case MONTH:
            default:
                end.add(Calendar.MONTH, 1);
                break;
        }
        return new long[]{start.getTimeInMillis(), end.getTimeInMillis() - 1};
    }

    public void setPeriod(Period newPeriod) {
        if (!newPeriod.equals(period.getValue())) {
            period.setValue(newPeriod);
        }
    }

    public LiveData<Period> getPeriod() {
        return period;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<List<TransactionDao.CategoryTotal>> getCategoryTotals() {
        return categoryTotals;
    }
}
