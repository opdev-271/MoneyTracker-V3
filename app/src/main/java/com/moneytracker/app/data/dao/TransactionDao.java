package com.moneytracker.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.moneytracker.app.data.entity.Transaction;
import com.moneytracker.app.data.relation.TransactionWithDetails;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    String DETAILS_SELECT =
            "SELECT t.id, t.account_id, t.category_id, t.type, t.amount, t.counterparty, t.note, t.date, " +
                    "a.name AS account_name, a.currency_code AS account_currency, " +
                    "c.name AS category_name, c.icon AS category_icon, c.color AS category_color " +
                    "FROM transactions t " +
                    "LEFT JOIN accounts a ON a.id = t.account_id " +
                    "LEFT JOIN categories c ON c.id = t.category_id ";

    @Query(DETAILS_SELECT + "ORDER BY t.date DESC, t.id DESC")
    LiveData<List<TransactionWithDetails>> getAllTransactions();

    @Query(DETAILS_SELECT + "ORDER BY t.date DESC, t.id DESC LIMIT :limit")
    LiveData<List<TransactionWithDetails>> getRecentTransactions(int limit);

    @Query(DETAILS_SELECT + "WHERE t.account_id = :accountId ORDER BY t.date DESC, t.id DESC")
    LiveData<List<TransactionWithDetails>> getTransactionsForAccount(long accountId);

    @Query(DETAILS_SELECT + "WHERE t.date BETWEEN :startMillis AND :endMillis ORDER BY t.date DESC, t.id DESC")
    LiveData<List<TransactionWithDetails>> getTransactionsBetween(long startMillis, long endMillis);

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    LiveData<Transaction> getTransactionById(long transactionId);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startMillis AND :endMillis")
    LiveData<Double> getTotalIncomeBetween(long startMillis, long endMillis);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startMillis AND :endMillis")
    LiveData<Double> getTotalExpenseBetween(long startMillis, long endMillis);

    @Query("SELECT IFNULL(c.name, '') AS categoryName, IFNULL(c.color, 0) AS color, SUM(t.amount) AS total " +
            "FROM transactions t LEFT JOIN categories c ON c.id = t.category_id " +
            "WHERE t.type = 'EXPENSE' AND t.date BETWEEN :startMillis AND :endMillis " +
            "GROUP BY t.category_id ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getExpenseTotalsByCategory(long startMillis, long endMillis);

    class CategoryTotal {
        public String categoryName;
        public int color;
        public double total;
    }
}
