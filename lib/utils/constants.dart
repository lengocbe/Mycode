import '../models/transaction.dart';

const incomeCategories = <String>['Lương', 'Thưởng', 'Bán hàng', 'Khác'];
const expenseCategories = <String>['Ăn uống', 'Đi lại', 'Mua sắm', 'Hóa đơn', 'Khác'];

String formatVnd(num value) {
  final text = value.round().toString();
  final buffer = StringBuffer();
  for (var i = 0; i < text.length; i++) {
    if (i > 0 && (text.length - i) % 3 == 0) buffer.write('.');
    buffer.write(text[i]);
  }
  return '${buffer.toString()} đ';
}

class MonthSummary {
  const MonthSummary({required this.income, required this.expense});
  final double income;
  final double expense;
  double get balance => income - expense;
}

MonthSummary calculateSummary(Iterable<MoneyTransaction> items, DateTime month) {
  var income = 0.0;
  var expense = 0.0;
  for (final item in items) {
    if (item.date.year != month.year || item.date.month != month.month) continue;
    if (item.type == TransactionType.income) income += item.amount; else expense += item.amount;
  }
  return MonthSummary(income: income, expense: expense);
}
