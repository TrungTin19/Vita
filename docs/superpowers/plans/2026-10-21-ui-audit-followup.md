# ViTa — bước tiếp theo sau audit

Phạm vi người dùng yêu cầu tiếp tục: các đề xuất giao diện còn lại của báo cáo UI/UX, sau năm lỗi ưu tiên và sửa form che chữ. Giữ Java/XML, MVC, dữ liệu và nghiệp vụ hiện có.

1. Chi tiết số đo: đưa badge huyết áp vào LinearLayout của card chỉ số. Giữ ID, visibility, lời khuyên và callback; kiểm tra các caller dùng getParent trước khi di chuyển.
2. Xu hướng: đơn vị nằm ngay dưới số thay vì cột xa; tăng chữ thông tin/axis, nhãn chart theo fontScale và tăng chiều cao chart tương ứng. Bật legend huyết áp, giữ đường liền/đứt và toàn bộ truy vấn/marker hiện có.
3. Hồ sơ: thêm heading quản lý hồ sơ, giảm logout về text action; giữ delete tách biệt và luồng xác thực/xác nhận cũ.
4. Bằng chứng: xem ảnh BMI hợp lệ trước sửa; chụp mới profile tên dài, BMI, chi tiết và chart ở font lớn trong gói verification. Không dùng ảnh bị ghép hình làm bằng chứng.
5. Kiểm tra: unit, các bài StatsData/HealthComparison/ProfileBmi/Deletion liên quan; build APK chính/lint, đối chiếu ảnh; ghi rõ TalkBack/IME hoặc cấu hình chưa kiểm tra.

Không áp TDD cho XML đơn thuần. Không thêm business logic hoặc dependency. Không tự thiết kế lại Home hoặc đổi tiêu chí sức khỏe từ nhận định thẩm mỹ.
