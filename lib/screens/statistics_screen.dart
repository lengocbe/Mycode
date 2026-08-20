import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../models/transaction.dart';
import '../providers/transaction_provider.dart';
import '../utils/constants.dart';

class StatisticsScreen extends StatefulWidget { const StatisticsScreen({super.key}); @override State<StatisticsScreen> createState() => _StatisticsScreenState(); }
class _StatisticsScreenState extends State<StatisticsScreen> {
  DateTime _month = DateTime(DateTime.now().year, DateTime.now().month); bool _allHistory = false;
  @override Widget build(BuildContext context) {
    final items = context.watch<TransactionProvider>().items;
    final selected = _allHistory ? items : items.where((i) => i.date.year == _month.year && i.date.month == _month.month).toList();
    final summary = _allHistory ? _allSummary(items) : calculateSummary(items, _month);
    final monthly = _lastSixMonths(items);
    return ListView(padding: const EdgeInsets.fromLTRB(16, 16, 16, 100), children: [
      SegmentedButton<bool>(segments: const [ButtonSegment(value: false, label: Text('Theo tháng')), ButtonSegment(value: true, label: Text('Toàn bộ'))], selected: {_allHistory}, onSelectionChanged: (v) => setState(() => _allHistory = v.first)),
      const SizedBox(height: 14),
      if (!_allHistory) Card(child: ListTile(leading: const Icon(Icons.calendar_month), title: const Text('Tháng thống kê'), subtitle: Text(DateFormat('MM/yyyy').format(_month)), trailing: const Icon(Icons.chevron_right), onTap: () async { final picked = await showDatePicker(context: context, initialDate: _month, firstDate: DateTime(2000), lastDate: DateTime(2100), initialDatePickerMode: DatePickerMode.year); if (picked != null) setState(() => _month = DateTime(picked.year, picked.month)); })),
      const SizedBox(height: 10),
      Row(children: [Expanded(child: _StatCard(title: 'Tổng thu', value: summary.income, icon: Icons.arrow_downward)), const SizedBox(width: 10), Expanded(child: _StatCard(title: 'Tổng chi', value: summary.expense, icon: Icons.arrow_upward))]),
      const SizedBox(height: 10), Card(child: ListTile(title: const Text('Còn lại'), trailing: Text(formatVnd(summary.balance), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 17)))),
      const SizedBox(height: 20), Text('Thu và chi 6 tháng gần nhất', style: Theme.of(context).textTheme.titleLarge), const SizedBox(height: 12),
      Card(child: Padding(padding: const EdgeInsets.fromLTRB(8, 20, 20, 12), child: SizedBox(height: 260, child: BarChart(_barData(monthly))))),
      const SizedBox(height: 20), Text(_allHistory ? 'Giao dịch toàn bộ lịch sử' : 'Giao dịch trong tháng', style: Theme.of(context).textTheme.titleLarge), const SizedBox(height: 8),
      if (selected.isEmpty) const Card(child: Padding(padding: EdgeInsets.all(24), child: Center(child: Text('Chưa có dữ liệu.')))) else ...selected.map((i) => ListTile(title: Text(i.category), subtitle: Text(DateFormat('dd/MM/yyyy').format(i.date)), trailing: Text(formatVnd(i.amount)))),
    ]);
  }
  MonthSummary _allSummary(List<MoneyTransaction> items) { var income = 0.0, expense = 0.0; for (final i in items) { if (i.type == TransactionType.income) income += i.amount; else expense += i.amount; } return MonthSummary(income: income, expense: expense); }
  List<MonthSummary> _lastSixMonths(List<MoneyTransaction> items) => List.generate(6, (i) => calculateSummary(items, DateTime(_month.year, _month.month - (5 - i))));
  BarChartData _barData(List<MonthSummary> data) { final maxValue = data.fold<double>(0, (m, i) => [m, i.income, i.expense].reduce((a, b) => a > b ? a : b)); return BarChartData(maxY: maxValue == 0 ? 100 : maxValue * 1.2, barGroups: List.generate(data.length, (i) => BarChartGroupData(x: i, barRods: [BarChartRodData(toY: data[i].income, width: 10), BarChartRodData(toY: data[i].expense, width: 10)])), titlesData: FlTitlesData(rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)), topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)), bottomTitles: AxisTitles(sideTitles: SideTitles(showTitles: true, getTitlesWidget: (v, m) { final d = DateTime(_month.year, _month.month - (5 - v.toInt())); return Padding(padding: const EdgeInsets.only(top: 8), child: Text('${d.month}/${d.year % 100}')); })), borderData: FlBorderData(show: false)); }
}
class _StatCard extends StatelessWidget { const _StatCard({required this.title, required this.value, required this.icon}); final String title; final double value; final IconData icon; @override Widget build(BuildContext context) => Card(child: Padding(padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Icon(icon), const SizedBox(height: 8), Text(title), const SizedBox(height: 4), Text(formatVnd(value), style: const TextStyle(fontWeight: FontWeight.bold))]))); }
