# ViTa — triển khai Figma lên Android

Thiết kế người dùng đã chọn: NlGw68WKTNzdur3pMqfhix, node map `docs/VITA_FIGMA_UX_2026-10-20.md`. Ngày 20/10 người dùng cho phép đọc trực tiếp qua trình duyệt thay get_design_context vì quota Starter. Không sửa file Figma. Đây là thay giao diện các luồng hiện có, không đổi schema/DAO/quy tắc.

## Phạm vi và phân công

1. Root: palette/theme/typography/spacing, Main điều hướng và Home realdata. Đọc Variables 15 primitive qua UI, nền #F4F8F7, ink#173333 muted#627979 brand#0B786E deep#104B47 mint#DFF4EB line#D9E6E3 water#286DAD/paleBlue#EAF3FD amber#8D5716/#FFF2DC red#AD4042/#FCECEE soft#EEF4F2; Roboto. Frame tham chiếu412×892 không áp kích thước cố định lên màn thật.
2. Auth/profile agent: A01–A04, P01–P03, hai bước đăng ký, BMI riêng; giữ localaccounts và form ownership. Layout EditProfile được phối hợp với agent nước.
3. Health/stats agent: M01–M03, T01/T02 và biến thể; dữ liệu thực/null/context/range/previous giữ nguyên.
4. Reminder/water agent: R01–R04 W01–W04 dialog; tách segment cữ/danh sách, giữ alarm/status/undo và hai kiểu lịch nước.
5. Root tích hợp: thêm Activity manifest nếu cần, chạy fullunit/device và lint; riêng significant logic phải RED rồi GREEN. XML/styles kiểm bằng build và ảnh thực, không ép TDD.
6. Review/QA từng module, đối chiếu screenshots Figma-browser và emulator, sửa mismatch; test nhỏ/landscape/chữ lớn/bàn phím khi chạy được. APK cuối gói vn.edu.tdmu.vita, test chỉ .verification.

## Quy tắc tích hợp

- Giữ test IDs cũ khi cùng chức năng, chỉ cập nhật test cho điều hướng thay đổi chủ ý; không bỏ test để xanh.
- Dữ liệu mẫu, giờ cố định và redirect sai trong prototype không phải nghiệp vụ. Back trở về đúng nơi gọi; picker native cho phép mọi giá trị hợp lệ.
- Không khôi phục weather/sleep/backup. Không dùng ảnh cả màn làm giao diện. Giữ dữ liệu cài đặt cũ và thay đổi chưa commit của người dùng.
- Global toolbar chỉ Home; các tab còn lại có header trong Fragment. BottomNav: Hôm nay/Chỉ số/Nhắc nhở/Xu hướng. Lịch nhắc/session/vòng đời cập nhật theo code hiện tại.
- Không thêm framework/web stack. Material/Java/XML và MPAndroidChart hiện có.

## Tiến độ

- Đã đọc code nguồn, node map, screenshot H01 và Variables UI; triển khai theo từng nhóm file độc lập theo dispatching-parallel-agents.
- RegistrationStepsDeviceTest được viết trước đổi logic; đang build để chạy RED trên verification.
- Chưa hoàn tất/chưa tuyên bố APK mới cho đến full verification và visual QA.

### Tích hợp và kiểm chứng ngày 20/10

- Đã thêm BmiActivity vào manifest, Home mở thẳng BMI, ngày Home cập nhật theo clock refresh. Đăng ký hai bước và Back/rotation đã GREEN sau RED.
- Metric selector dùng ChipGroup trực tiếp; cập nhật kiểm thử validation theo cùng selector, không đổi quy tắc lưu số đo.
- Đã xuất PNG biểu tượng lá trực tiếp bằng Copy as PNG từ vector 7:17; asset `drawable-nodpi/figma_leaf.png` dùng ở splash/login. Không vẽ lại hay dùng ảnh cả màn.
- Build verification + 70 unit tests thành công. APK applicationId gốc và lint cũng build thành công; lint có 117 cảnh báo, không có lỗi.
- 11 kiểm thử smoke (bao gồm capture 11 màn qua SQLite fixture thật, thuốc, nước) qua. Ảnh nằm trong `.audit-cache/figma-screenshots/`.
- Đã đối chiếu R01 qua browser; sửa tiêu đề ngắn, tách giờ/tên thuốc. Ảnh thực còn giúp sửa chữ mô tả BMI quá lớn và xuống dòng mục tiêu nước. Các chỉnh sửa này đã build verification lại.
- Full device run đầu: 63/64 qua, một assertion chuyển tab danh sách thuốc lỗi khi chạy chung. Đã đổi MedicineStatusUiTest sang tài khoản riêng và cleanup thay xóa toàn DB, đang chạy lại full suite. Không bỏ assertion.
- Emulator từng bị System UI ANR che focus; đã xác nhận bằng ảnh và đóng hộp thoại trước chạy lại. Đây không phải crash của ViTa.
- Chưa xác nhận xong mọi dialog/keyboard/empty/error và kích thước nhỏ/chữ lớn; chưa coi vòng visual QA là hoàn tất. Các icon điều hướng cũ và bố cục nhật ký nước cần tiếp tục so sát mẫu.
- Kết quả cuối lượt tích hợp: 64/64 device tests qua trong 122.88 giây sau teardown thông báo/lịch của bài alarm; 70/70 unit tests qua. APK gốc và lint sau sửa padding/logo thành công. Báo cáo `docs/reviews/2026-10-20-figma-integration-review.md` ghi rõ những điểm hình thức còn cần rà tiếp.
