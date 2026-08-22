package vn.sothuchi.app;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Creates a standard XLSX workbook without requiring external libraries. */
public final class ExcelExporter {
    private ExcelExporter() { }

    public static void export(ContentResolver resolver, Uri destination, List<DatabaseHelper.MonthSummary> summaries,
                              List<TransactionItem> transactions) throws IOException {
        OutputStream output = resolver.openOutputStream(destination);
        if (output == null) throw new IOException("Không mở được file để ghi.");
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            write(zip, "[Content_Types].xml", contentTypes());
            write(zip, "_rels/.rels", relationships());
            write(zip, "xl/workbook.xml", workbook());
            write(zip, "xl/_rels/workbook.xml.rels", workbookRelationships());
            write(zip, "xl/styles.xml", styles());
            write(zip, "xl/worksheets/sheet1.xml", summarySheet(summaries));
            write(zip, "xl/worksheets/sheet2.xml", transactionSheet(transactions));
        }
    }

    private static void write(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "</Types>";
    }

    private static String relationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"Tổng hợp theo tháng\" sheetId=\"1\" r:id=\"rId1\"/><sheet name=\"Danh sách giao dịch\" sheetId=\"2\" r:id=\"rId2\"/></sheets></workbook>";
    }

    private static String workbookRelationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>"
                + "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font><font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf/></cellStyleXfs><cellXfs count=\"2\"><xf xfId=\"0\"/><xf xfId=\"0\" applyFont=\"1\" fontId=\"1\"/></cellXfs>"
                + "</styleSheet>";
    }

    private static String summarySheet(List<DatabaseHelper.MonthSummary> summaries) {
        StringBuilder rows = new StringBuilder();
        rows.append(row(1, new String[]{"Tháng", "Tổng thu (VND)", "Tổng chi (VND)", "Chênh lệch (VND)"}, true));
        int line = 2;
        for (DatabaseHelper.MonthSummary summary : summaries) {
            rows.append("<row r=\"").append(line).append("\">")
                    .append(textCell("A" + line, summary.month, false))
                    .append(numberCell("B" + line, summary.income))
                    .append(numberCell("C" + line, summary.expense))
                    .append(numberCell("D" + line, summary.income - summary.expense)).append("</row>");
            line++;
        }
        return sheet("D", rows.toString(), "18", "20", "20", "22");
    }

    private static String transactionSheet(List<TransactionItem> transactions) {
        StringBuilder rows = new StringBuilder();
        rows.append(row(1, new String[]{"Ngày", "Loại", "Danh mục", "Ghi chú", "Số tiền (VND)"}, true));
        int line = 2;
        for (TransactionItem item : transactions) {
            rows.append("<row r=\"").append(line).append("\">")
                    .append(textCell("A" + line, item.date, false))
                    .append(textCell("B" + line, item.type, false))
                    .append(textCell("C" + line, item.category, false))
                    .append(textCell("D" + line, item.note, false))
                    .append(numberCell("E" + line, item.amount)).append("</row>");
            line++;
        }
        return sheet("E", rows.toString(), "14", "12", "20", "36", "20");
    }

    private static String sheet(String lastColumn, String rows, String... widths) {
        StringBuilder columns = new StringBuilder("<cols>");
        for (int i = 0; i < widths.length; i++) {
            columns.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                    .append("\" width=\"").append(widths[i]).append("\" customWidth=\"1\"/>");
        }
        columns.append("</cols>");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + columns + "<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>"
                + "<sheetData>" + rows + "</sheetData><autoFilter ref=\"A1:" + lastColumn + "1\"/></worksheet>";
    }

    private static String row(int line, String[] values, boolean header) {
        StringBuilder result = new StringBuilder("<row r=\"").append(line).append("\">");
        for (int i = 0; i < values.length; i++) {
            result.append(textCell(String.valueOf((char) ('A' + i)) + line, values[i], header));
        }
        return result.append("</row>").toString();
    }

    private static String textCell(String address, String value, boolean header) {
        return "<c r=\"" + address + "\" t=\"inlineStr\"" + (header ? " s=\"1\"" : "")
                + "><is><t>" + escape(value == null ? "" : value) + "</t></is></c>";
    }

    private static String numberCell(String address, long value) {
        return "<c r=\"" + address + "\"><v>" + value + "</v></c>";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}

