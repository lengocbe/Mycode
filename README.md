# App Thu Chi

Ứng dụng Flutter cho Android để quản lý thu chi cá nhân.

## Chức năng

- Thêm khoản **thu** và **chi**.
- Danh mục cố định: Lương, Thưởng, Bán hàng, Khác; Ăn uống, Đi lại, Mua sắm, Hóa đơn, Khác.
- Chọn ngày giao dịch và nhập nội dung/ghi chú.
- Sửa và xóa giao dịch, có xác nhận trước khi xóa.
- Lưu dữ liệu cục bộ bằng SQLite, không cần Internet.
- Thống kê theo tháng hoặc toàn bộ lịch sử.
- Biểu đồ so sánh thu/chi trong 6 tháng gần nhất.
- Xuất toàn bộ giao dịch ra Excel với các cột: **Ngày | Loại | Danh mục | Nội dung | Số tiền**.
- Chia sẻ file Excel bằng share sheet của Android.

## Công nghệ

- Flutter / Dart
- Provider
- SQLite (`sqflite`)
- FL Chart
- Excel
- Share Plus

## Chạy trên Android

```bash
flutter pub get
flutter run
```

Nếu thư mục `android/` chưa được tạo bởi Flutter tooling:

```bash
flutter create --platforms=android --org com.lengocbe .
flutter pub get
flutter run
```

## Kiểm tra

```bash
flutter analyze
flutter test
flutter build apk --release
```
