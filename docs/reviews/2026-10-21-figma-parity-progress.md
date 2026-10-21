# Đối chiếu giao diện ViTa với Figma — 21/10/2026

## Phạm vi

Tiếp tục yêu cầu khớp giao diện file `NlGw68WKTNzdur3pMqfhix`. Người dùng đã cho phép đọc thiết kế qua trình duyệt khi Figma MCP hết quota. Không sửa thiết kế, không dùng ảnh cả màn làm UI, không thay dữ liệu thật bằng dữ liệu mẫu.

## Đã chỉnh trong vòng đối chiếu

- Header, khoảng cách, font Roboto, màu và các thẻ Home; icon điều hướng, avatar, thuốc và nước lấy trực tiếp từ Figma.
- Form đăng nhập/đăng ký/hồ sơ, BMI và cấu hình nước; giữ xử lý dữ liệu, mật khẩu, quyền sở hữu và lịch nhắc.
- Nhật ký/chỉnh sửa nước dùng trang đầy đủ, cập nhật lại trang gọi sau khi lưu; duy trì bản nháp khi tái tạo Activity.
- Danh sách chỉ số, form đo, chi tiết, biểu đồ và danh sách dữ liệu mới nhất trước; không đổi cách đánh giá chỉ số.
- Thuốc/cữ hôm nay/lịch sử và form lịch; giữ lịch báo thức và trạng thái từng cữ.
- Dialog hồ sơ dùng AppCompatDialog với content riêng để tránh trùng ID nút của AlertDialog native. Dialog xóa hồ sơ theo DProfileDelete: tiêu đề, mô tả, nhãn/placeholder mật khẩu, hai nút xếp dọc, màu đỏ nhạt.
- Dialog xóa nước theo DWaterDelete: tên thao tác, thông tin lượng/giờ/ngày từ bản ghi thật, hai nút xếp dọc. Tái sử dụng layout dialog hiện có; giữ DAO và điều kiện sở hữu.

## Kiểm chứng và giới hạn

- Build verification và 70 unit tests thành công, không lỗi/failure: `.audit-cache/exact-resume-build.txt`.
- Build APK gốc `vn.edu.tdmu.vita` và lint sau sửa dialog thành công: `.audit-cache/exact-current-apk.txt`; lint còn 245 cảnh báo, 0 lỗi. Đã sửa 5 lỗi thuộc tính giãn dòng trong style bằng thuộc tính tương thích Material và 4 chỗ hướng LinearLayout dùng số trực tiếp. Không tắt lint hoặc nâng minSdk để né lỗi.
- Đã thêm kiểm thử thiết bị cho bản nháp nước/rotation, hoàn tác trong nhật ký, lịch hai giờ qua đêm, liên kết hàng dữ liệu mới nhất với đúng bản ghi và hủy/xác nhận xóa nước.
- Mã cuối sau các sửa lint/dialog: biên dịch bộ kiểm thử thiết bị và chạy lại 70 unit tests thành công, 0 lỗi/failure: `.audit-cache/exact-final-verification.txt`. Các bài thiết bị mới chưa có kết quả chạy đạt; việc biên dịch không thay thế kiểm thử trên thiết bị.
- Lượt capture mới không phải kết quả đạt: System UI bị ANR và chiếm focus; Espresso báo `RootViewWithoutFocusException`. Bằng chứng `.audit-cache/exact-resume-capture.txt` và `.audit-cache/exact-state.png`. Đã dừng máy giả lập để giải phóng bộ nhớ.
- Không dùng kết quả 64/64 của vòng trước để kết luận vòng UI hiện tại đã đạt. Bộ kiểm thử thiết bị hiện tại cần chạy lại đầy đủ.
- Tham chiếu H01 xuất trực tiếp: `.audit-cache/figma-H01-reference.png`. Figma 412×892 có thanh trạng thái giả 32dp; khi so ảnh phải loại vùng hệ thống Android tương ứng. Không thêm thanh hệ thống giả vào app.
- Chưa xác nhận toàn bộ dialog xóa thuốc/chỉ số, empty/error, bàn phím, chữ lớn, màn nhỏ và mọi biến thể biểu đồ. Chưa có ảnh hợp lệ của toàn bộ vòng chỉnh mới để kết luận khớp 100%.

Kết luận: tiếp tục visual QA; chưa đủ bằng chứng nghiệm thu yêu cầu khớp toàn bộ Figma.
