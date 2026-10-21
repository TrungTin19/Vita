# ViTa — Thiết kế UX/UI theo phạm vi hiện tại

Ngày: 20/10/2026.

File đích: https://www.figma.com/design/NlGw68WKTNzdur3pMqfhix?node-id=7-119

Nguồn chức năng: danh sách đầy đủ người dùng cung cấp trong tin nhắn ngày 20/10/2026. Phiên bản này thay thế phạm vi cũ có thời tiết và giấc ngủ; không dùng các chức năng đã bỏ.

## Cấu trúc

- Hôm nay: hành động nhanh, cữ thuốc sắp tới, nước, số đo gần nhất, BMI.
- Chỉ số: nhật ký, lọc ngày, nhập theo nhóm, sửa/xóa và đối chiếu.
- Nhắc nhở: Cữ hôm nay tách khỏi Danh sách thuốc; lịch sử và xử lý trạng thái riêng.
- Xu hướng: một chỉ số mỗi lần, 7/30 ngày, dữ liệu chi tiết; năm nhóm chỉ số.
- Hồ sơ mở từ avatar; BMI và cài nước có màn riêng.
- Đăng ký chia hai bước: tài khoản và thông tin nền.

## Các node chính

| Mã | Ngữ cảnh | Node |
|---|---|---|
| A01 | Khởi động | 7:2 |
| A02 | Đăng nhập | 7:24 |
| A03 | Đăng ký — tài khoản | 7:53 |
| A04 | Đăng ký — thông tin nền | 7:83 |
| H01 | Hôm nay | 7:119 |
| M01 | Nhật ký chỉ số | 7:198 |
| M02 | Ghi chỉ số | 7:252 |
| M03 | Chi tiết và đối chiếu | 7:313 |
| R01 | Cữ hôm nay | 7:361 |
| R02 | Danh sách thuốc | 7:442 |
| R03 | Thuốc và lịch sử | 7:495 |
| R04 | Thêm thuốc | 7:530 |
| W01 | Nhật ký nước | 7:583 |
| W02 | Thêm lượng khác | 7:649 |
| W03 | Sửa lần uống | 7:674 |
| W04 | Cài nước | 7:701 |
| T01 | Xu hướng cân nặng | 7:741 |
| T02 | Dữ liệu cân nặng | 7:812 |
| P01 | Hồ sơ | 7:835 |
| P02 | BMI | 7:880 |
| P03 | Sửa hồ sơ | 7:915 |

Biến thể biểu đồ: huyết áp 8:1341, nhịp tim 8:1449, đường huyết 8:1555, nước 8:1676. Hướng dẫn và giới hạn bàn giao nằm trong file Figma tại node 13:290; tham chiếu nội dung thông báo Android tại 13:304.

## Kết quả kiểm tra thiết kế

- 21 ngữ cảnh chính; tổng cộng 57 frame điện thoại gồm các chế độ sửa, biến thể và trạng thái; 15 dialog.
- Kiểm tra cấu trúc cuối: 1.220 text layer, 208 component instance, 301 kết nối prototype; không tìm thấy node đích bị mất hoặc thiếu vùng cuộn trên frame điện thoại.
- Đã quan sát screenshot trực tiếp của Hôm nay, Chi tiết lần đo, Cữ hôm nay và Xu hướng đường huyết. Không kiểm tra hình ảnh riêng từng frame.
- Nền sáng, Roboto, khung 412 × 892, màu xanh chính #0B786E, nước #286DAD; semantic color variables và component nút/input có thể chỉnh sửa.

## Giới hạn

Prototype minh họa dữ liệu cố định và các trạng thái mẫu; không lưu form, không kết nối SQLite hay chạy lịch nhắc Android. Các picker chưa mô phỏng mọi giá trị và lựa chọn bối cảnh chưa áp dụng bộ lọc động. Dialog bỏ thay đổi quay về Hôm nay trong bản mẫu; bản Android phải trở lại đúng màn gọi. Gói Starter không cho chuỗi nhiều hành động trên một tương tác.

Chưa triển khai UI mới vào app, chưa kiểm thử trên máy giả lập/điện thoại. Khi tiếp tục, dùng đúng file đích và đọc cấu trúc hiện tại trước khi sửa, tránh tạo lại file hoặc nhân đôi các frame đã có.
