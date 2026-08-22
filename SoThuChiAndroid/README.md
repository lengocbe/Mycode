# Sổ Thu Chi Android

Ứng dụng Android độc lập để ghi thu chi cá nhân, lưu dữ liệu cục bộ trên điện thoại.

## Chức năng

- Thêm khoản thu hoặc chi: số tiền, danh mục, ghi chú và ngày giao dịch.
- Xem tổng thu, tổng chi và số tiền còn lại theo từng tháng.
- Xem danh sách giao dịch của tháng; nhấn giữ một giao dịch để xóa.
- Xuất file `.xlsx` thật, có 2 trang:
  - `Tổng hợp theo tháng`: tổng thu, tổng chi và chênh lệch của mọi tháng.
  - `Danh sách giao dịch`: toàn bộ dữ liệu giao dịch.
- Không cần Internet và không yêu cầu quyền truy cập bộ nhớ rộng. Khi xuất Excel, Android sẽ hỏi nơi lưu file.

## Mở bằng Android Studio

1. Mở đúng thư mục project này bằng Android Studio.
2. Chờ Gradle Sync hoàn tất (project dùng Android SDK 35 và Java 17).
3. Cắm điện thoại rồi bấm **Run**, hoặc chọn **Build → Generate App Bundles or APKs → Generate APKs** để tạo APK.

