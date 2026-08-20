enum TransactionType { income, expense }

class MoneyTransaction {
  const MoneyTransaction({this.id, required this.type, required this.amount, required this.date, required this.category, required this.note});
  final int? id;
  final TransactionType type;
  final double amount;
  final DateTime date;
  final String category;
  final String note;

  MoneyTransaction copyWith({int? id, TransactionType? type, double? amount, DateTime? date, String? category, String? note}) => MoneyTransaction(id: id ?? this.id, type: type ?? this.type, amount: amount ?? this.amount, date: date ?? this.date, category: category ?? this.category, note: note ?? this.note);

  Map<String, Object?> toMap() => {'id': id, 'type': type.name, 'amount': amount, 'date': date.toIso8601String(), 'category': category, 'note': note};

  factory MoneyTransaction.fromMap(Map<String, Object?> map) => MoneyTransaction(id: map['id'] as int?, type: TransactionType.values.byName(map['type'] as String), amount: (map['amount'] as num).toDouble(), date: DateTime.parse(map['date'] as String), category: map['category'] as String, note: map['note'] as String? ?? '');
}
