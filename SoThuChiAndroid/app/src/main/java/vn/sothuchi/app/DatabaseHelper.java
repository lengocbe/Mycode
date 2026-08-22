package vn.sothuchi.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "so_thu_chi.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "transactions";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "type TEXT NOT NULL,"
                + "amount INTEGER NOT NULL,"
                + "category TEXT NOT NULL,"
                + "note TEXT,"
                + "tx_date TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void addTransaction(String type, long amount, String category, String note, String date) {
        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("amount", amount);
        values.put("category", category);
        values.put("note", note);
        values.put("tx_date", date);
        getWritableDatabase().insert(TABLE, null, values);
    }

    public void deleteTransaction(long id) {
        getWritableDatabase().delete(TABLE, "id=?", new String[]{String.valueOf(id)});
    }

    public List<TransactionItem> getTransactionsForMonth(String month) {
        List<TransactionItem> items = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE, null, "substr(tx_date,1,7)=?", new String[]{month},
                null, null, "tx_date DESC, id DESC");
        try {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public List<TransactionItem> getAllTransactions() {
        List<TransactionItem> items = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE, null, null, null, null, null, "tx_date DESC, id DESC");
        try {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public long getTotal(String month, String type) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(amount),0) FROM " + TABLE + " WHERE substr(tx_date,1,7)=? AND type=?",
                new String[]{month, type});
        try {
            return cursor.moveToFirst() ? cursor.getLong(0) : 0L;
        } finally {
            cursor.close();
        }
    }

    public List<MonthSummary> getMonthlySummaries() {
        List<MonthSummary> summaries = new ArrayList<>();
        String sql = "SELECT substr(tx_date,1,7) AS month, "
                + "COALESCE(SUM(CASE WHEN type='Thu' THEN amount ELSE 0 END),0) AS income, "
                + "COALESCE(SUM(CASE WHEN type='Chi' THEN amount ELSE 0 END),0) AS expense "
                + "FROM " + TABLE + " GROUP BY substr(tx_date,1,7) ORDER BY month DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        try {
            while (cursor.moveToNext()) {
                summaries.add(new MonthSummary(cursor.getString(0), cursor.getLong(1), cursor.getLong(2)));
            }
        } finally {
            cursor.close();
        }
        return summaries;
    }

    private TransactionItem fromCursor(Cursor cursor) {
        return new TransactionItem(cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("type")),
                cursor.getLong(cursor.getColumnIndexOrThrow("amount")),
                cursor.getString(cursor.getColumnIndexOrThrow("category")),
                cursor.getString(cursor.getColumnIndexOrThrow("note")),
                cursor.getString(cursor.getColumnIndexOrThrow("tx_date")));
    }

    public static final class MonthSummary {
        public final String month;
        public final long income;
        public final long expense;

        MonthSummary(String month, long income, long expense) {
            this.month = month;
            this.income = income;
            this.expense = expense;
        }
    }
}

