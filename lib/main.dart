import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'app.dart';
import 'data/database.dart';
import 'providers/transaction_provider.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final database = AppDatabase();
  await database.init();
  runApp(ChangeNotifierProvider(create: (_) => TransactionProvider(database)..load(), child: const ExpenseTrackerApp()));
}
