# ViTa — sửa sau khi xác nhận audit

Phạm vi được người dùng xác nhận: báo cáo `2026-10-21-ui-ux-quality-audit.md`. Lượt này xử lý năm vấn đề đã xác minh đứng đầu; giữ bố cục chính, Java/XML, MVC, database, phân quyền, quy tắc đánh giá và lịch nhắc hiện có.

## Đã sửa

1. Chữ phụ đổi từ `#627979` sang `#526B6B`. Tính tương phản từ token: trắng5,72:1; nền app5,34:1; nền nước5,10:1. Không dùng kết quả này để khẳng định mọi chữ trong app đều đạt.
2. Hai tab Nhắc nhở dùng chiều cao co giãn, tối thiểu48dp, thay cao cố định40dp.
3. Nhật ký sức khỏe bổ sung `mmHg` cho huyết áp đầy đủ hoặc số đo một phần; không đổi phân loại chỉ số.
4. Nhắc nhở làm tròn tỷ lệ nước giống Home. Thống nhất nhãn `+250ml`/`Lượng khác`, giữ tổng ml, mục tiêu và cỡ cốc. Không giới hạn phần trăm về100 khi dữ liệu vượt mục tiêu.
5. Login/register/profile/health/medicine/password dialog dùng style ô nhập chung với nền trắng, outline bo12dp, viền thường1dp/focus2dp. Form nước tạo động dùng cùng màu/trạng thái. Bỏ nền EditText cũ có thể che background Material; giữ inputType, validation, helper/error, password toggle và picker.

Tài liệu MASTER cũ được đánh dấu lịch sử và ghi token/phạm vi hiện hành, tránh dùng các mục cũ làm lý do phục hồi chức năng đã xóa. Không đổi các module dữ liệu để phù hợp tài liệu cũ.

## Kiểm chứng

- RED: `UiAuditDeviceTest` chạy trên bản trước sửa, thất bại ở phần trăm tab Nhắc nhở. Log `.audit-cache/ui-audit-red-device.txt`.
- GREEN: bài kiểm tra nước đi qua0/2000,1250/2000,2250/2000; cùng HomeDashboard và hai bài FigmaDialog, `OK (4 tests)`. Log `.audit-cache/ui-audit-green-device.txt`.
- Build verification/unit: `.audit-cache/ui-audit-green-build.txt`, thành công; style nền được build tiếp ở `.audit-cache/ui-audit-visual-build.txt`.
- Chụp luồng: `FigmaScreenshotsDeviceTest`, `OK (1 test)`, `.audit-cache/ui-audit-visual-device.txt`; ảnh trong `.audit-cache/ui-audit-after/`. Bộ ảnh gồm39 file figma và2 file upgrade có sẵn; không coi2file upgrade là ảnh mới của lượt này.
- Đã xem trực tiếp form thuốc/hồ sơ và bảng ảnh nhật ký sức khỏe, Nhắc nhở, thêm nước, login error, delete profile, water settings error. Thấy `118 /78mmHg`, tỷ lệ nước63%, viền/focus/error và trường mật khẩu rõ. Bảng ảnh `.audit-cache/ui-audit-after-review.png`.
- Ảnh chụp trước tinh chỉnh màu nhãn/helper/suffix cuối cùng trong style. Tinh chỉnh này dùng cùng token chữ phụ; không coi ảnh đó là bằng chứng xác minh pixel của màu nhãn cuối.
- XML parse và kiểm tra minHeight tab thành công; không thêm dependency. Dữ liệu mẫu chỉ thuộc gói verification, không commit vào dữ liệu sản phẩm.

## Chưa kết luận

- Không chấm lại điểm66/100: cần một lượt audit mới và tương tác trợ năng đầy đủ.
- Chưa xác minh TalkBack, Switch Access, keyboard thực, font130–200%, landscape và màn360dp trong lượt này.
- Các đề xuất thiết kế tiếp theo (thu gọn card/helper, đổi thứ bậc Home, nhóm badge huyết áp, rút gọn hàng dữ liệu xu hướng, bố trí ngày ở màn hẹp) chưa triển khai trong lượt năm lỗi ưu tiên. Không gọi chúng là bug đã được sửa.
- Chưa chạy lại toàn bộ device suite; kiểm thử tập trung vào phần vừa đổi, không dùng các lượt system ANR trước làm kết quả pass.

Build APK chính/unit/lint cuối: `.audit-cache/ui-audit-final-build.txt`, `BUILD SUCCESSFUL in42s`;70 unit tests,0failures/errors; lint0errors/198warnings. APK: `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`. Định dạng huyết áp đã đưa vào string resource; xóa drawable nền ô nhập cũ sau khi kiểm tra toàn bộ tham chiếu.
