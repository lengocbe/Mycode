import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

class ExpenseTrackerApp extends StatelessWidget {
  const ExpenseTrackerApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Thu Chi',
      theme: ThemeData(useMaterial3: true, colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF2E7D32)), scaffoldBackgroundColor: const Color(0xFFF7F8F7)),
      home: const HomeScreen(),
    );
  }
}
