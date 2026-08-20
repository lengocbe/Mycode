import 'package:sqflite/sqflite.dart';
import '../models/transaction.dart';

class AppDatabase {
  Database? _db;

  Future<void> init() async {
    _db = await openDatabase('${await getDatabasesPath()}/expense_tracker.db', version: 1, onCreate: (db, version) async {
      await db.execute('''CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, amount REAL NOT NULL, date TEXT NOT NULL, category TEXT NOT NULL, note TEXT NOT NULL)''');
      await db.execute('CREATE INDEX idx_transactions_date ON transactions(date)');
    });
  }

  Database get db => _db ?? (throw StateError('Database has not been initialized.'));
  Future<List<MoneyTransaction>> getAll() async => (await db.query('transactions', orderBy: 'date DESC, id DESC')).map(MoneyTransaction.fromMap).toList();
  Future<int> insert(MoneyTransaction item) => db.insert('transactions', item.toMap());
  Future<int> update(MoneyTransaction item) => db.update('transactions', item.toMap()..remove('id'), where: 'id = ?', whereArgs: [item.id]);
  Future<int> delete(int id) => db.delete('transactions', where: 'id = ?', whereArgs: [id]);
}
