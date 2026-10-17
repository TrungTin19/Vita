# Hồ sơ và cấu hình cá nhân

Phạm vi theo mục 2 của báo cáo đánh giá: dữ liệu năm sinh tùy chọn trung thực, cấu hình nước độc lập, xóa hồ sơ có xác nhận. Dùng Android Java/XML và Material hiện có; giữ schema4 và dữ liệu cũ; không thêm thư viện.

1. Năm sinh: đăng ký bỏ trống lưu0; sửa hồ sơ cho phép xóa năm đã nhập; hiển thị Chưa cập nhật. Giữ nguyên các năm đã lưu, kể cả2000. Kiểm thử đăng ký trống/hợp lệ/vượt giới hạn và sửa xóa năm.
2. Giao diện/cấu hình: tách nhóm thông tin cá nhân và cài đặt nước thành hai chế độ của EditProfileActivity; Nhắc nhở mở trực tiếp chế độ nước. DAO cập nhật riêng các cột của từng nhóm, giữ các cột còn lại. Nhãn rõ, vùng chạm48dp, lỗi inline và focus trường lỗi.
3. Quản lý hồ sơ: nút xóa có thông báo rõ phạm vi, xác nhận bằng mật khẩu; chỉ xóa user hiện tại bằng khóa ngoại cascade; giữ user khác; hủy lịch thuốc/nước, thông báo và phiên sau khi xóa thành công. Đăng xuất vẫn chỉ kết thúc phiên.

TDD cho logic và hành vi ghi dữ liệu, không ép test XML. Review/dọn sau từng module. Android tests chỉ chạy với applicationId .verification và fixture riêng; không xóa tài khoản thật. Build/unit/lint/APK thường ở cuối. Báo cáo đánh giá ban đầu là tài liệu tham khảo, được giữ nguyên.

## Tiến độ

- Đã đọc báo cáo mục2, các Activity/DAO/model/callers và design system. Root cause năm sinh nằm ở giá trị mặc định2000 khi đăng ký và giữ lại năm cũ khi trường sửa để trống.
- Module1: RED thực tế 2 test expected0/actual2000; sửa đúng hai giá trị mặc định, không đổi schema hoặc dữ liệu cũ. GREEN cả5 ProfileBirthYearDeviceTest và77 unit test. Review: đăng ký/sửa/hồ sơ/DAO đều dùng0 là chưa biết; giữ nguyên các năm hợp lệ. Helper text đổi thành Có thể để trống.
- Module2: test DAO mới RED thiếu API; test stale personal form RED expected1500/actual2000 xác nhận ghi đè cột nước. Test UI nước độc lập cũng RED expected1500/actual2000 vì form cũ yêu cầu thể trạng. Sau tách cột và hai chế độ giao diện, cả3 test GREEN (có xoay/tạo lại Activity). Đã cập nhật caller Nhắc nhở và fixture cũ; review không thay schema hay dữ liệu đã lưu.
- Module3: DAO RED thiếu API rồi GREEN cả3 test SQLite, gồm lỗi cascade phải giữ nguyên cả6 bảng. UI RED thiếu nút xóa rồi GREEN cả4 test hủy/xác nhận/sai mật khẩu/đổi phiên. Test xác nhận kiểm tra cả hai giờ thuốc, nhắc nước, thông báo và dữ liệu người khác. Dùng lại cơ chế mật khẩu, khóa ngoại và hủy lịch hiện có; chia sẻ cleanup với đăng xuất. Chỉ kết thúc phiên sau khi DELETE thành công.
- Kiểm tra: 10 test module2/3 GREEN; toàn bộ32 Android test GREEN trên gói .verification, có phát thông báo báo thức thật trên máy ảo.77 unit GREEN; assembleDebug, assembleDebugAndroidTest và lintDebug thành công (0 lỗi,73 cảnh báo toàn dự án). Đã xem ảnh màn hình sửa hồ sơ thực tế.
- Review độc lập phát hiện bàn phím cứng vẫn sửa được ô giờ dù không bật IME. Test RED thực tế07:00 thành07:0 sau phím Delete; khóa key listener ở hai ô, giữ focus, test GREEN trên máy ảo. Reviewer kiểm tra lại: không còn phát hiện cần sửa. Chạy lại77 unit/build/lint sau sửa cuối: thành công. Test này nâng bộ Android test lên33.
- QA rộng320dp, chữ130%: cả2 test màn nước GREEN; ảnh thực tế phát hiện nhãn chiều cao/cân nặng bị cắt. Đã xếp dọc hai trường, căn padding hộp mật khẩu, rút gọn lời xác nhận và nhãn mật khẩu; phân biệt đăng xuất với nút xóa. Review lại XML/string không có phát hiện cần sửa. Cả4 test xóa hồ sơ chạy lại GREEN trên màn nhỏ; chụp và xem đủ3 màn. Harness chụp ảnh tạm đã xóa khỏi source; ảnh giữ trong .audit-cache bị gitignore. Không chạy test phá dữ liệu trên gói app thường.
- Bàn giao: APK debug thường vn.edu.tdmu.vita đã build lại sau chỉnh nhãn cuối; lint thành công,0 lỗi/73 cảnh báo. Máy ảo đã trả về kích thước/mật độ gốc và font1.0; gỡ thành công cả2 gói verification. Chưa thử điện thoại thật; chưa commit/push thay đổi của đợt này.
