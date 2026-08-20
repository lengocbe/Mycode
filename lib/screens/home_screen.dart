import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../models/transaction.dart';
import '../providers/transaction_provider.dart';
import '../services/excel_export_service.dart';
import '../utils/constants.dart';
import 'statistics_screen.dart';
import 'transaction_form_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _index = 0;
  @override
  Widget build(BuildContext context) {
    final pages = [const _DashboardTab(), const _TransactionTab(), const StatisticsScreen()];
    return Scaffold(
      appBar: AppBar(
        title: Text(_index == 0 ? 'Thu Chi' : _index == 1 ? 'Giao dịch' : 'Thống kê'),
        actions: [IconButton(tooltip: 'Xuất Excel', icon: const Icon(Icons.file_download_outlined), onPressed: () async {
          final items = context.read<TransactionProvider>().items;
          if (items.isEmpty) { ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Chưa có dữ liệu để xuất.'))); return; }
          try { await ExcelExportService().export(items); } catch (e) { if (context.mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Xuất Excel thất bại: $e'))); }
        })],
      ),
      body: pages[_index],
      floatingActionButton: _index == 2 ? null : FloatingActionButton.extended(onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const TransactionFormScreen())), icon: const Icon(Icons.add), label: const Text('Thêm giao dịch')),
      bottomNavigationBar: NavigationBar(selectedIndex: _index, onDestinationSelected: (v) => setState(() => _index = v), destinations: const [
        NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'Tổng quan'),
        NavigationDestination(icon: Icon(Icons.receipt_long_outlined), selectedIcon: Icon(Icons.receipt_long), label: 'Giao dịch'),
        NavigationDestination(icon: Icon(Icons.bar_chart_outlined), selectedIcon: Icon(Icons.bar_chart), label: 'Thống kê'),
      ]),
    );
  }
}

class _DashboardTab extends StatelessWidget {
  const _DashboardTab();
  @override
  Widget build(BuildContext context) {
    final items = context.watch<TransactionProvider>().items;
    final summary = calculateSummary(items, DateTime.now());
    return ListView(padding: const EdgeInsets.fromLTRB(16, 16, 16, 100), children: [
      Text(DateFormat('MM/yyyy').format(DateTime.now()), style: Theme.of(context).textTheme.titleMedium),
      const SizedBox(height: 10),
      Card(child: Padding(padding: const EdgeInsets.all(20), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Text('Số dư tháng này'), const SizedBox(height: 6),
        Text(formatVnd(summary.balance), style: Theme.of(context).textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.bold)), const SizedBox(height: 16),
        Row(children: [Expanded(child: _MoneyTile(label: 'Tổng thu', value: summary.income, icon: Icons.arrow_downward)), const SizedBox(width: 10), Expanded(child: _MoneyTile(label: 'Tổng chi', value: summary.expense, icon: Icons.arrow_upward))])
      ]))),
      const SizedBox(height: 20), Text('Giao dịch gần đây', style: Theme.of(context).textTheme.titleLarge), const SizedBox(height: 8),
      if (items.isEmpty) const Card(child: Padding(padding: EdgeInsets.all(24), child: Center(child: Text('Chưa có giao dịch. Hãy thêm khoản đầu tiên.')))) else ...items.take(8).map((e) => _TransactionTile(item: e)),
    ]);
  }
}

class _MoneyTile extends StatelessWidget {
  const _MoneyTile({required this.label, required this.value, required this.icon});
  final String label; final double value; final IconData icon;
  @override Widget build(BuildContext context) => Container(padding: const EdgeInsets.all(12), decoration: BoxDecoration(color: Theme.of(context).colorScheme.surfaceContainerHighest, borderRadius: BorderRadius.circular(14)), child: Row(children: [CircleAvatar(child: Icon(icon, size: 18)), const SizedBox(width: 8), Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(label), Text(formatVnd(value), style: const TextStyle(fontWeight: FontWeight.bold))]))]));
}

class _TransactionTab extends StatelessWidget {
  const _TransactionTab();
  @override Widget build(BuildContext context) { final items = context.watch<TransactionProvider>().items; return items.isEmpty ? const Center(child: Text('Chưa có giao dịch')) : ListView.builder(padding: const EdgeInsets.fromLTRB(12, 12, 12, 100), itemCount: items.length, itemBuilder: (_, i) => _TransactionTile(item: items[i])); }
}

class _TransactionTile extends StatelessWidget {
  const _TransactionTile({required this.item});
  final MoneyTransaction item;
  @override
  Widget build(BuildContext context) {
    final income = item.type == TransactionType.income;
    return Card(child: ListTile(leading: CircleAvatar(child: Icon(income ? Icons.arrow_downward : Icons.arrow_upward)), title: Text(item.category, style: const TextStyle(fontWeight: FontWeight.w600)), subtitle: Text('${DateFormat('dd/MM/yyyy').format(item.date)}${item.note.isEmpty ? '' : ' • ${item.note}'}'), trailing: Text('${income ? '+' : '-'}${formatVnd(item.amount)}', style: TextStyle(fontWeight: FontWeight.bold, color: income ? Colors.green.shade700 : Colors.red.shade700)), onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => TransactionFormScreen(item: item))));
  }
}
