import 'package:flutter/foundation.dart';
import '../data/database.dart';
import '../models/transaction.dart';

class TransactionProvider extends ChangeNotifier {
  TransactionProvider(this.database);
  final AppDatabase database;
  List<MoneyTransaction> _items = [];
  List<MoneyTransaction> get items => List.unmodifiable(_items);

  Future<void> load() async { _items = await database.getAll(); notifyListeners(); }
  Future<void> add(MoneyTransaction item) async { final id = await database.insert(item); _items = [item.copyWith(id: id), ..._items]; _sort(); notifyListeners(); }
  Future<void> update(MoneyTransaction item) async { await database.update(item); final i = _items.indexWhere((e) => e.id == item.id); if (i >= 0) _items[i] = item; _sort(); notifyListeners(); }
  Future<void> remove(int id) async { await database.delete(id); _items.removeWhere((e) => e.id == id); notifyListeners(); }
  void _sort() => _items.sort((a, b) => b.date.compareTo(a.date));
}
