import 'dart:io';
import 'package:cross_file/cross_file.dart';
import 'package:excel/excel.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import '../models/transaction.dart';

class ExcelExportService {
  Future<void> export(List<MoneyTransaction> items) async {
    final excel = Excel.createExcel();
    final sheet = excel['Thu Chi'];
    sheet.appendRow([TextCellValue('Ngày'), TextCellValue('Loại'), TextCellValue('Danh mục'), TextCellValue('Nội dung'), TextCellValue('Số tiền')]);
    for (final item in items.reversed) {
      sheet.appendRow([TextCellValue('${item.date.day.toString().padLeft(2, '0')}/${item.date.month.toString().padLeft(2, '0')}/${item.date.year}'), TextCellValue(item.type == TransactionType.income ? 'Thu' : 'Chi'), TextCellValue(item.category), TextCellValue(item.note), DoubleCellValue(item.amount)]);
    }
    final bytes = excel.save();
    if (bytes == null) throw StateError('Không thể tạo file Excel.');
    final directory = await getTemporaryDirectory();
    final file = File('${directory.path}/thu-chi-${DateTime.now().millisecondsSinceEpoch}.xlsx');
    await file.writeAsBytes(bytes, flush: true);
    await SharePlus.instance.share(ShareParams(title: 'Xuất dữ liệu Thu Chi', files: [XFile(file.path)]));
  }
}
