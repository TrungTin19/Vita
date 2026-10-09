# THIẾT KẾ CHI TIẾT GIAO DIỆN: ADD SLEEP ACTIVITY
## Màn hình Ghi nhận Giấc ngủ (TimePicker & Xử lý Logic Qua Nửa Đêm)

> **File:** `AddSleepActivity.java`  
> **Layout:** `res/layout/activity_add_sleep.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Tiếp nhận dữ liệu giấc ngủ đêm qua của người dùng để phục vụ phân tích phục hồi thể chất:
  - Ngày thức dậy (`date`, mặc định ngày hôm nay).
  - Giờ bắt đầu đi ngủ (`sleep_time`, ví dụ: `23:00`).
  - Giờ thức dậy buổi sáng (`wake_time`, ví dụ: `06:30`).
- **Thuật toán xử lý qua nửa đêm (Overnight Logic - Điểm nhấn kỹ thuật):**
  - Người dùng thường đi ngủ vào tối hôm trước (22:00, 23:30) và thức dậy vào sáng hôm sau (06:00, 07:00).
  - Hệ thống tự động kiểm tra: Nếu `wake_time < sleep_time`, thời lượng tính theo công thức:
    $$\text{duration\_h} = \frac{(24 \times 60 - \text{sleep\_minutes}) + \text{wake\_minutes}}{60}$$
  - Tự động hiển thị con số tổng thời lượng ngủ ngay trên giao diện theo thời gian thực (ví dụ: *"Tổng thời lượng: 7.5 giờ"*).
- Lưu bản ghi vào bảng `sleep_logs` trong SQLite và trở về màn hình Thống kê `StatsFragment`.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  ← (Hủy)          Ghi nhận giấc ngủ      │  <- Toolbar trắng
├──────────────────────────────────────────┤
│                                          │
│  [Biểu tượng Mặt trăng tím 🌙 64dp]      │  <- #512DA8 Deep Indigo
│                                          │
│  Đêm qua bạn ngủ có ngon không?          │  <- Heading 2 (18sp Bold)
│  Theo dõi giấc ngủ để duy trì thể lực.   │  <- Body Regular (14sp)
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ 📅 Ngày thức dậy                   │  │
│  │ [ 09/10/2026                 📅 ]  │  <- DatePickerDialog
│  └────────────────────────────────────┘  │
│                                          │
│  ┌──────────────────┐ ┌───────────────┐  │
│  │ 🛌 Giờ bắt đầu ngủ│ │ ⏰ Giờ thức dậy│  │
│  │ [ 23:00       🌙]│ │ [ 06:30     ☀️]│  │  <- Chạm vào mở TimePickerDialog
│  └──────────────────┘ └───────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Kết quả Thời lượng tự tính
│  │ TỔNG THỜI LƯỢNG GIẤC NGỦ           │  │
│  │                                    │  │
│  │        7.5 giờ     [ Đủ giấc ✨ ]  │  │  <- 28sp Bold, Badge Xanh lá #2E7D32
│  │                                    │  │
│  │ "Thời lượng ngủ rất tốt cho tim    │  │  <- Đánh giá y khoa tham khảo
│  │  mạch và phục hồi não bộ."         │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │          LƯU GIẤC NGỦ              │  │  <- Filled Button #00897B, 48dp
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Màu sắc |
| :--- | :--- | :--- | :--- |
| **Thanh Toolbar** | `toolbar_add_sleep` | `MaterialToolbar`, cao `56dp`, nền `#FFFFFF` | Nút Back Arrow |
| **Biểu tượng Mặt trăng**| `iv_sleep_icon` | `ImageView`, `64dp × 64dp`, tint `@color/vita_sleep`| Màu: `#512DA8` Deep Indigo |
| **Ô chọn Ngày** | `et_sleep_date` | `TextInputEditText`, `clickable="true"` | Mở `DatePickerDialog` |
| **Ô Giờ bắt đầu ngủ** | `et_sleep_time` | `TextInputEditText`, `clickable="true"` | Mở `TimePickerDialog` |
| **Ô Giờ thức dậy** | `et_wake_time` | `TextInputEditText`, `clickable="true"` | Mở `TimePickerDialog` |
| **Thẻ Tổng thời lượng**| `cv_sleep_duration` | `MaterialCardView`, cornerRadius `12dp` | Nền: `#FFFFFF`, elevation `2dp` |
| **Số giờ hiển thị** | `tv_sleep_duration_val`| `TextView`, `28sp`, Bold (700) | Màu: `@color/vita_sleep` |
| **Huy hiệu đánh giá** | `tv_sleep_badge` | `TextView`, `14sp`, Medium, bo góc `16dp` | Nền mờ, chữ theo chuẩn y học |
| **Nút Lưu** | `btn_sleep_save` | `MaterialButton`, cao `48dp`, bo góc `8dp` | Nền: `@color/vita_teal`, Chữ trắng |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Tính toán và Phản hồi tức thì (Live Calculation):**
   - Ngay khi người dùng chọn xong Giờ thức dậy hoặc Giờ đi ngủ, ứng dụng tự động kích hoạt hàm tính toán thời lượng và cập nhật số giờ hiển thị trên thẻ `cv_sleep_duration` mà không cần bấm nút phụ nào.
2. **Ngưỡng Đánh giá Giấc ngủ (Sleep Health Standards):**
   - Dưới 6 giờ: Badge *"Thiếu ngủ"* (Màu vàng cam `#F57C00`) kèm lời khuyên: *"Bạn nên sắp xếp nghỉ trưa hoặc ngủ sớm hơn tối nay nhé."*.
   - Từ 7.0 đến 9.0 giờ: Badge *"Đủ giấc"* (Màu xanh lá `#2E7D32`) kèm lời khuyên: *"Thời lượng giấc ngủ lý tưởng để cơ thể tái tạo năng lượng."*.
   - Trên 10 giờ: Badge *"Ngủ nhiều"* (Màu xanh tím).
3. **Phòng chống nhập sai giờ:**
   - Nếu giờ ngủ và giờ thức trùng khít nhau (0 giờ), hiển thị cảnh báo: *"Giờ ngủ và giờ thức không thể trùng nhau. Vui lòng kiểm tra lại."*.
