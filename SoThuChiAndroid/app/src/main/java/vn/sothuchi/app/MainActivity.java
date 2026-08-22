package vn.sothuchi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int EXPORT_XLSX_REQUEST = 41;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final NumberFormat moneyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private DatabaseHelper database;
    private String selectedMonth;
    private TextView monthText;
    private TextView incomeText;
    private TextView expenseText;
    private TextView balanceText;
    private TextView emptyText;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new DatabaseHelper(this);
        selectedMonth = monthFormat.format(calendar.getTime());
        getWindow().setStatusBarColor(Color.rgb(20, 92, 64));
        getWindow().setNavigationBarColor(Color.WHITE);
        buildScreen();
        refreshData();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247, 249, 248));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(16), dp(18), dp(16));
        header.setBackgroundColor(Color.rgb(20, 110, 76));
        TextView title = label("SỔ THU CHI", 25, Color.WHITE, true);
        header.addView(title);
        TextView subtitle = label("Theo dõi thu nhập và chi tiêu mỗi ngày", 14, Color.rgb(218, 246, 232), false);
        subtitle.setPadding(0, dp(4), 0, 0);
        header.addView(subtitle);
        root.addView(header);

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setPadding(dp(14), dp(12), dp(14), dp(8));
        monthText = label("", 17, Color.rgb(20, 70, 50), true);
        controls.addView(monthText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button chooseMonth = new Button(this);
        chooseMonth.setText("Chọn tháng");
        chooseMonth.setOnClickListener(v -> showMonthPicker());
        controls.addView(chooseMonth);
        root.addView(controls);

        LinearLayout cards = new LinearLayout(this);
        cards.setPadding(dp(12), 0, dp(12), dp(8));
        cards.setWeightSum(3);
        incomeText = summaryCard(cards, "TỔNG THU", Color.rgb(1, 118, 76));
        expenseText = summaryCard(cards, "TỔNG CHI", Color.rgb(202, 61, 61));
        balanceText = summaryCard(cards, "CÒN LẠI", Color.rgb(42, 92, 178));
        root.addView(cards);

        LinearLayout actions = new LinearLayout(this);
        actions.setPadding(dp(12), 0, dp(12), dp(9));
        actions.setGravity(Gravity.CENTER);
        Button add = new Button(this);
        add.setText("+ Thêm giao dịch");
        add.setOnClickListener(v -> showAddDialog());
        actions.addView(add, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button export = new Button(this);
        export.setText("Xuất Excel");
        export.setOnClickListener(v -> requestExport());
        LinearLayout.LayoutParams exportParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        exportParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(export, exportParams);
        root.addView(actions);

        TextView historyTitle = label("GIAO DỊCH TRONG THÁNG", 14, Color.rgb(82, 92, 87), true);
        historyTitle.setPadding(dp(18), dp(6), dp(18), dp(5));
        root.addView(historyTitle);

        ListView listView = new ListView(this);
        listView.setDividerHeight(dp(1));
        adapter = new TransactionAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            TransactionItem item = adapter.getItem(position);
            if (item != null) confirmDelete(item);
            return true;
        });
        root.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        emptyText = label("Chưa có giao dịch nào trong tháng này.\nBấm “Thêm giao dịch” để bắt đầu.", 16,
                Color.rgb(100, 108, 104), false);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(dp(20), dp(20), dp(20), dp(20));
        root.addView(emptyText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }

    private TextView summaryCard(LinearLayout parent, String title, int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(9), dp(9), dp(7), dp(9));
        card.setBackgroundColor(Color.WHITE);
        TextView heading = label(title, 10, Color.rgb(102, 108, 105), true);
        card.addView(heading);
        TextView amount = label("0 đ", 15, color, true);
        amount.setPadding(0, dp(5), 0, 0);
        card.addView(amount);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(dp(2), 0, dp(2), 0);
        parent.addView(card, params);
        return amount;
    }

    private void showMonthPicker() {
        Calendar current = Calendar.getInstance();
        try { current.setTime(monthFormat.parse(selectedMonth)); } catch (Exception ignored) { }
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, 1);
            selectedMonth = monthFormat.format(chosen.getTime());
            refreshData();
        }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), 1);
        picker.show();
    }

    private void showAddDialog() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(8), dp(22), dp(4));
        scroll.addView(form);

        RadioGroup typeGroup = new RadioGroup(this);
        typeGroup.setOrientation(LinearLayout.HORIZONTAL);
        RadioButton income = new RadioButton(this);
        income.setText("Khoản thu");
        income.setId(View.generateViewId());
        income.setChecked(true);
        RadioButton expense = new RadioButton(this);
        expense.setText("Khoản chi");
        expense.setId(View.generateViewId());
        typeGroup.addView(income);
        typeGroup.addView(expense);
        form.addView(typeGroup);

        EditText amount = input("Số tiền (VND), ví dụ: 500000");
        amount.setInputType(InputType.TYPE_CLASS_NUMBER);
        form.addView(amount);
        EditText category = input("Danh mục, ví dụ: Lương / Ăn uống");
        form.addView(category);
        EditText note = input("Ghi chú (không bắt buộc)");
        form.addView(note);

        String[] chosenDate = {selectedMonth + "-01"};
        Button dateButton = new Button(this);
        dateButton.setAllCaps(false);
        dateButton.setText("Ngày: " + chosenDate[0]);
        dateButton.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            try { c.setTime(dateFormat.parse(chosenDate[0])); } catch (Exception ignored) { }
            new DatePickerDialog(this, (view, year, month, day) -> {
                c.set(year, month, day);
                chosenDate[0] = dateFormat.format(c.getTime());
                dateButton.setText("Ngày: " + chosenDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        form.addView(dateButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm giao dịch")
                .setView(scroll)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button -> {
            String amountValue = amount.getText().toString().trim().replace(".", "").replace(",", "");
            String categoryValue = category.getText().toString().trim();
            if (amountValue.isEmpty() || categoryValue.isEmpty()) {
                Toast.makeText(this, "Hãy nhập số tiền và danh mục.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                long money = Long.parseLong(amountValue);
                if (money <= 0) throw new NumberFormatException();
                String type = typeGroup.getCheckedRadioButtonId() == income.getId() ? "Thu" : "Chi";
                database.addTransaction(type, money, categoryValue, note.getText().toString().trim(), chosenDate[0]);
                dialog.dismiss();
                if (chosenDate[0].startsWith(selectedMonth)) refreshData();
                Toast.makeText(this, "Đã lưu khoản " + type.toLowerCase() + ".", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException error) {
                Toast.makeText(this, "Số tiền phải là số dương, không có chữ.", Toast.LENGTH_SHORT).show();
            }
        }));
        dialog.show();
    }

    private EditText input(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(16);
        field.setSingleLine(true);
        field.setPadding(0, dp(8), 0, dp(8));
        return field;
    }

    private void confirmDelete(TransactionItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch?")
                .setMessage(item.type + " " + formatMoney(item.amount) + " - " + item.category)
                .setNegativeButton("Không", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    database.deleteTransaction(item.id);
                    refreshData();
                }).show();
    }

    private void requestExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, "So_thu_chi_" + dateFormat.format(calendar.getTime()) + ".xlsx");
        startActivityForResult(intent, EXPORT_XLSX_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != EXPORT_XLSX_REQUEST || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            ExcelExporter.export(getContentResolver(), uri, database.getMonthlySummaries(), database.getAllTransactions());
            Toast.makeText(this, "Đã xuất file Excel thành công.", Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Toast.makeText(this, "Không thể xuất Excel: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshData() {
        monthText.setText("Tháng " + selectedMonth);
        long income = database.getTotal(selectedMonth, "Thu");
        long expense = database.getTotal(selectedMonth, "Chi");
        incomeText.setText(formatMoney(income));
        expenseText.setText(formatMoney(expense));
        balanceText.setText(formatMoney(income - expense));
        List<TransactionItem> items = database.getTransactionsForMonth(selectedMonth);
        adapter.setItems(items);
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String formatMoney(long amount) {
        return moneyFormat.format(amount) + " đ";
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class TransactionAdapter extends ArrayAdapter<TransactionItem> {
        TransactionAdapter() { super(MainActivity.this, android.R.layout.simple_list_item_1); }

        void setItems(List<TransactionItem> items) {
            clear();
            addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TransactionItem item = getItem(position);
            LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(18), dp(11), dp(18), dp(11));
            row.setBackgroundColor(Color.WHITE);
            LinearLayout left = new LinearLayout(MainActivity.this);
            left.setOrientation(LinearLayout.VERTICAL);
            TextView first = label(item.date + "  •  " + item.category, 16, Color.rgb(35, 43, 39), true);
            left.addView(first);
            if (!item.note.isEmpty()) {
                TextView second = label(item.note, 13, Color.rgb(110, 117, 113), false);
                second.setPadding(0, dp(3), 0, 0);
                left.addView(second);
            }
            row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            TextView amount = label((item.type.equals("Thu") ? "+ " : "- ") + formatMoney(item.amount), 16,
                    item.type.equals("Thu") ? Color.rgb(0, 130, 82) : Color.rgb(205, 58, 58), true);
            amount.setGravity(Gravity.END);
            row.addView(amount);
            return row;
        }
    }
}

