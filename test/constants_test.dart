import 'package:flutter_test/flutter_test.dart';
import 'package:expense_tracker/models/transaction.dart';
import 'package:expense_tracker/utils/constants.dart';

void main() {
  test('calculates monthly summary', () {
    final items = [
      MoneyTransaction(type: TransactionType.income, amount: 10000000, date: DateTime(2026, 8, 2), category: 'Lương', note: ''),
      MoneyTransaction(type: TransactionType.expense, amount: 2500000, date: DateTime(2026, 8, 4), category: 'Ăn uống', note: ''),
      MoneyTransaction(type: TransactionType.income, amount: 500000, date: DateTime(2026, 7, 4), category: 'Khác', note: ''),
    ];
    final summary = calculateSummary(items, DateTime(2026, 8));
    expect(summary.income, 10000000);
    expect(summary.expense, 2500000);
    expect(summary.balance, 7500000);
  });

  test('formats Vietnamese dong', () {
    expect(formatVnd(1234567), '1.234.567 đ');
    expect(formatVnd(0), '0 đ');
  });
}
