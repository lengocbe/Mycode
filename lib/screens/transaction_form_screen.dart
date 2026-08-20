import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../models/transaction.dart';
import '../providers/transaction_provider.dart';
import '../utils/constants.dart';

class TransactionFormScreen extends StatefulWidget {
  const TransactionFormScreen({super.key, this.item});
  final MoneyTransaction? item;
  @override State<TransactionFormScreen> createState() => _TransactionFormScreenState();
}

class _TransactionFormScreenState extends State<TransactionFormScreen> {
  late TransactionType _type; late DateTime _date; late String _category;
  late final TextEditingController _amount; late final TextEditingController _note;
  final _formKey = GlobalKey<FormState>();
  @override void initState() { super.initState(); final i = widget.item; _type = i?.type ?? TransactionType.expense; _date = i?.date ?? DateTime.now(); _category = i?.category ?? expenseCategories.first; _amount = TextEditingController(text: i == null ? '' : i.amount.toStringAsFixed(0)); _note = TextEditingController(text: i?.note ?? ''); }
  List<String> get categories => _type == TransactionType.income ? incomeCategories : expenseCategories;
  @override void dispose() { _amount.dispose(); _note.dispose(); super.dispose(); }
  Future<void> _save() async { if (!_formKey.currentState!.validate()) return; final amount = double.tryParse(_amount.text.replaceAll('.', '').replaceAll(',', '')); if (amount == null || amount <= 0) return; final item = MoneyTransaction(id: widget.item?.id, type: _type, amount: amount, date: _date, category: _category, note: _note.text.trim()); final p = context.read<TransactionProvider>(); if (widget.item == null) await p.add(item); else await p.update(item); if (mounted) Navigator.pop(context); }
  Future<void> _delete() async { final id = widget.item?.id; if (id == null) return; final ok = await showDialog<bool>(context: context, builder: (c) => AlertDialog(title: const Text('Xóa giao dịch?'), content: const Text('Giao dịch này sẽ bị xóa vĩnh viễn. Bạn có chắc chắn không?'), actions: [TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('Hủy')), FilledButton(onPressed: () => Navigator.pop(c, true), child: const Text('Xóa'))])); if (ok == true) { await context.read<TransactionProvider>().remove(id); if (mounted) Navigator.pop(context); } }
  @override
  Widget build(BuildContext context) => Scaffold(appBar: AppBar(title: Text(widget.item == null ? 'Thêm giao dịch' : 'Sửa giao dịch'), actions: [if (widget.item != null) IconButton(onPressed: _delete, icon: const Icon(Icons.delete_outline))]), body: Form(key: _formKey, child: ListView(padding: const EdgeInsets.all(16), children: [
    SegmentedButton<TransactionType>(segments: const [ButtonSegment(value: TransactionType.income, icon: Icon(Icons.arrow_downward), label: Text('Khoản thu')), ButtonSegment(value: TransactionType.expense, icon: Icon(Icons.arrow_upward), label: Text('Khoản chi'))], selected: {_type}, onSelectionChanged: (v) => setState(() { _type = v.first; if (!categories.contains(_category)) _category = categories.first; })),
    const SizedBox(height: 20), TextFormField(controller: _amount, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Số tiền', suffixText: 'đ', prefixIcon: Icon(Icons.payments_outlined)), validator: (v) { final n = double.tryParse((v ?? '').replaceAll('.', '').replaceAll(',', '')); return n == null || n <= 0 ? 'Nhập số tiền hợp lệ' : null; }),
    const SizedBox(height: 16), DropdownButtonFormField<String>(value: _category, decoration: const InputDecoration(labelText: 'Danh mục', prefixIcon: Icon(Icons.category_outlined)), items: categories.map((v) => DropdownMenuItem(value: v, child: Text(v))).toList(), onChanged: (v) => setState(() => _category = v!)),
    const SizedBox(height: 16), ListTile(contentPadding: EdgeInsets.zero, leading: const Icon(Icons.calendar_today_outlined), title: const Text('Ngày giao dịch'), subtitle: Text(DateFormat('dd/MM/yyyy').format(_date)), onTap: () async { final picked = await showDatePicker(context: context, initialDate: _date, firstDate: DateTime(2000), lastDate: DateTime(2100)); if (picked != null) setState(() => _date = picked); }),
    const SizedBox(height: 8), TextFormField(controller: _note, maxLines: 3, decoration: const InputDecoration(labelText: 'Nội dung / Ghi chú', alignLabelWithHint: true, prefixIcon: Icon(Icons.notes_outlined))),
    const SizedBox(height: 28), FilledButton.icon(onPressed: _save, icon: const Icon(Icons.save_outlined), label: const Text('Lưu giao dịch')),
  ]));
}
