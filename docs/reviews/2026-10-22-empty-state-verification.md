# Kiểm chứng trạng thái trống — 22/10/2026

Phạm vi: Chỉ số chưa có bản ghi, Xu hướng cân nặng chưa có dữ liệu, Nhật ký nước chưa ghi lần uống. Không sửa mã ứng dụng hoặc nghiệp vụ trong lượt này.

## Bằng chứng mới

- Build riêng verification thành công: `.audit-cache/empty-states-build.txt`, BUILD SUCCESSFUL in 37s.
- `FigmaScreenshotsDeviceTest.emptyStatesKeepTheirEntryActionsAndDoNotCreateData`: OK (1 test), 29.737 giây. Log: `.audit-cache/empty-states-device.txt`.
- Emulator: 1080×2400, density420, fontScale1.0, không thay cấu hình thiết bị.
- Đã xem ba ảnh mới: `.audit-cache/verified-health-empty.png`, `verified-stats-empty.png`, `verified-water-empty.png`. Nhãn trạng thái và hành động hiện rõ, không thấy chữ bị che hoặc cắt trong các ảnh này.
- Nút Ghi chỉ số mở form; Back quay lại. Nút ghi lần cân đầu tiên mở form có ô cân nặng; Back quay lại. Nhật ký nước hiển thị thông báo chưa ghi nhận. DAO xác nhận cả bản ghi sức khỏe và nhật ký nước của fixture vẫn trống.
- Fixture dùng tài khoản tạm trong package kết thúc `.verification`, được xóa trong finally. Không ghi fixture vào ứng dụng chính.

## Rà soát thay đổi

Theo ponytail-review: chỉ bổ sung một kiểm thử dùng các DAO, cơ chế ảnh và điều hướng đã có; không thêm dependency, không đổi code sản phẩm. Không phát hiện vấn đề cần chặn trong phạm vi kiểm thử mới. Không build lại APK chính hoặc chạy lại unit/lint vì không thay mã sản phẩm; các số liệu trước đó không được tính là kết quả mới.

## Giới hạn

Chưa xác minh lượt mới với font200%, TalkBack, lọc ra danh sách trống trên dữ liệu đã có hoặc mọi trạng thái lỗi/loading. Bài kiểm thử xác nhận ô cân nặng hiện trên form, chưa khẳng định mọi trường khác đã được ẩn đúng theo nhóm. Không chấm lại điểm audit và không tuyên bố toàn app khớp Figma100%.
