# Rà soát tích hợp giao diện Figma

Phạm vi: các thay đổi UI trong phiên ngày 20/10, ứng dụng cá nhân lưu SQLite trên thiết bị. Không đổi schema, phân loại BMI, quyền sở hữu bản ghi hoặc quy tắc lịch nhắc.

## Đã sửa sau rà soát

- BmiActivity thiếu manifest: đã khai báo, giữ màn riêng dùng chung logic đọc hồ sơ/BMI.
- Home vẫn mở hồ sơ thay vì BMI: đã trỏ đúng màn riêng; ngày trên header cũng được làm mới theo clock refresh.
- Selector chỉ số dùng Spinner ẩn có thể trì hoãn callback: chuyển sang ChipGroup trực tiếp, kiểm thử validation vẫn chọn nhóm khác rồi kiểm tra trường lỗi được hiện.
- Logo splash/login cũ không đúng mẫu: lấy PNG trực tiếp từ vector 7:17 của Figma, không dựng lại bằng phỏng đoán.
- Ảnh thực cho thấy chữ mô tả BMI quá lớn, mục tiêu nước không xuống dòng, padding thẻ hồ sơ bị cộng hai lần: đã sửa.
- Bài thử báo thức để lại thông báo/lịch cho các bài UI sau: thêm teardown hủy lịch, thông báo và tài khoản fixture. Chuỗi alarm → tab thuốc → capture đã qua 5 bài sau sửa.
- Xóa các script sinh layout tạm `.auth_*.py`; giữ mã nguồn và tài liệu cần bàn giao.

## Chưa chốt về hình thức

- Chưa đối chiếu đủ mọi dialog, bàn phím, empty/error, landscape và chữ lớn. Không tuyên bố giao diện đã khớp toàn bộ Figma.
- Icon điều hướng vẫn dùng bộ vector cũ; W01 và lịch sử thuốc còn cấu trúc dialog native, cần tiếp tục đối chiếu chi tiết.
- R01 giữ hai hành động xác nhận nhanh trên thẻ bên cạnh hộp thoại trạng thái; thẻ cao hơn mẫu. Chức năng cũ vẫn hoạt động.

## Bằng chứng

- 70 unit tests qua. Build APK gốc `vn.edu.tdmu.vita` và lint qua; 117 cảnh báo lint, không có lỗi.
- Hai lượt full device trước teardown: 63/64 qua; một bài click tab bị ảnh hưởng khi chạy chung. Không che hoặc bỏ assertion.
- Chuỗi alarm/tab/capture sau teardown: `OK (5 tests)` trong `.audit-cache/figma-alarm-order-device.txt`.
- Chạy lại toàn bộ sau teardown: `OK (64 tests)`, 122.88 giây, `.audit-cache/figma-clean-full-device.txt`. Không còn lỗi tab thuốc trong lượt đầy đủ.
- APK cập nhật sau sửa padding hồ sơ và tỷ lệ logo đã build cùng lint thành công: `.audit-cache/figma-apk-build.txt`.
- Ảnh đã chụp từ dữ liệu fixture qua DAO thật trong package `.verification`, nằm ở `.audit-cache/figma-screenshots-reviewed/`. Đã thêm chờ đúng view/idle trước chụp để tránh ảnh lệch tab do transaction vẽ sau.

Kết luận: các luồng chính đã tích hợp, tiếp tục vòng visual QA trước khi chốt hoàn tất toàn bộ yêu cầu.
