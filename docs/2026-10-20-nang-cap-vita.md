# ViTa — nâng cấp chức năng hiện có

Phạm vi: ứng dụng Android Java/XML, tài khoản và dữ liệu cục bộ. Giữ bốn tab và phong cách Material màu teal. Không khôi phục thời tiết, giấc ngủ hoặc sao lưu.

## Thay đổi

- **Tài khoản:** truy vấn/sửa/xóa chỉ số và thuốc giới hạn theo hồ sơ; biểu mẫu giữ chủ sở hữu qua khôi phục màn hình và đóng khi phiên thay đổi. Mô tả rõ tài khoản cục bộ và dữ liệu được giữ khi đăng xuất.
- **Hồ sơ/BMI:** phân biệt cân nặng nền với lần cân trong nhật ký, hiển thị nguồn và thời điểm dùng cho BMI, giải thích phạm vi tuổi và chuẩn. Cảnh báo bỏ thay đổi ở các biểu mẫu; nhập số tương đương không tạo cảnh báo thừa.
- **Chỉ số:** chọn nhóm trường để nhập nhưng giữ các giá trị đã điền; lỗi ở nhóm ẩn được hiện và focus. Bộ lọc ngày có số kết quả và trạng thái không khớp riêng. Chi tiết so sánh lần trước đúng loại chỉ số, hồ sơ, thứ tự ngày/giờ và bối cảnh đường huyết. Giá trị huyết áp cũ thiếu một vế vẫn hiển thị vế có dữ liệu.
- **Thuốc:** phân nhóm từng cữ sắp tới/chưa ghi nhận/đã uống/bỏ qua; có thể sửa hoặc bỏ xác nhận. Lịch sử lưu tên và liều tại lần xác nhận, không đổi khi sửa đơn thuốc. Log cũ được đánh dấu kế thừa. Phân biệt quyền thông báo và quyền báo thức chính xác.
- **Nước:** nhật ký từng lần, sửa lượng/giờ hoặc xóa, thêm lượng khác không đổi cỡ cốc. Hoàn tác chỉ xóa lần ghi chưa bị chỉnh sửa. Giữ lịch mỗi hai giờ hoặc các mốc tự chọn, kể cả khung qua đêm.
- **Trang chủ:** ghi chỉ số, cữ thuốc kế tiếp chưa xử lý, nước hôm nay và thao tác ghi chung với Nhắc nhở, bản ghi gần nhất, BMI kèm nguồn cân nặng.
- **Thống kê:** chọn một trong năm nhóm, khoảng 7/30 ngày và bối cảnh đường huyết. Không nối các bối cảnh với nhau hoặc biến số đo thiếu thành 0. Hiển thị số lần ghi, lần gần nhất, tooltip theo thời điểm và danh sách dữ liệu mở chi tiết/nhật ký. Nước thiếu log được ghi rõ; mục tiêu hiện tại chỉ là tham chiếu.
- **Điều hướng:** giữ tab và lựa chọn khi chuyển màn/khôi phục; cập nhật tab đang xem theo thời gian hệ thống. Lịch nhắc được đặt lại khi đổi giờ/múi giờ.

## Tương thích dữ liệu

Schema 6 chỉ thêm snapshot tên/liều và nguồn kế thừa vào log thuốc. Không xóa dữ liệu hay bảng legacy. Bộ kiểm thử thiết bị dùng gói `.verification`, tách khỏi ứng dụng người dùng.

## Kiểm tra thủ công trên điện thoại

1. Tạo hồ sơ mới; ghi/sửa/xóa chỉ số và kiểm tra nguồn BMI.
2. Ghi nước từ hai tab; thử lượng khác, sửa/xóa và Hoàn tác.
3. Thêm nhiều cữ thuốc; xác nhận, sửa trạng thái và kiểm tra lịch sử sau đổi tên/liều.
4. Chọn chỉ số, khoảng ngày và bữa ăn; chuyển tab, xoay màn hình rồi đối chiếu danh sách với biểu đồ.
5. Bật/tắt quyền thông báo và báo thức; kiểm tra chuông khi màn hình tắt, sau khởi động lại và khi đổi múi giờ.

Kết quả tự động và giới hạn kiểm chứng được ghi tại `superpowers/plans/2026-10-19-vita-focused-upgrade-progress.md`.
