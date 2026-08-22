package vn.sothuchi.app;

public final class TransactionItem {
    public final long id;
    public final String type;
    public final long amount;
    public final String category;
    public final String note;
    public final String date;

    public TransactionItem(long id, String type, long amount, String category, String note, String date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
    }
}

