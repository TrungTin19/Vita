# THIẾT KẾ CHI TIẾT GIAO DIỆN: ADD MEDICINE ACTIVITY
## Màn hình Thêm mới & Chỉnh sửa Đơn thuốc và Lịch nhắc (TimePicker & 7-Day Bitmask)

> **File:** `AddMedicineActivity.java`  
> **Layout:** `res/layout/activity_add_medicine.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Tiếp nhận thông tin thuốc và thiết lập lịch hẹn báo thức chính xác:
  - Tên thuốc (`name`, ví dụ: "Panadol", "Amlodipine 5mg").
  - Liều lượng & Hướng dẫn (`dosage`, ví dụ: "1 viên sau khi ăn sáng").
  - Giờ nhắc (`time`, định dạng `HH:mm`): Chọn qua `TimePickerDialog`.
  - Các ngày lặp lại trong tuần: Cung cấp 7 nút bấm/CheckBox (Thứ 2, Thứ 3, ..., Chủ Nhật), lưu dưới dạng mặt nạ bit `days_mask` (0–127).
- **Hỗ trợ 2 chế độ:**
  1. *Thêm mới:* Mở từ FAB của `ReminderFragment`.
  2. *Chỉnh sửa:* Mở từ Context Menu của danh sách thuốc, đổ dữ liệu cũ vào form.
- Khi bấm **"Lưu lịch uống thuốc"**:
  - Lưu bản ghi vào bảng `medicines` trong SQLite.
  - Sử dụng `id` của bản ghi làm `requestCode` để kích hoạt `AlarmManager.setExactAndAllowWhileIdle(...)` tới `MedicineAlarmReceiver`.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  ← (Hủy)           Lịch uống thuốc mới   │  <- Toolbar trắng
├──────────────────────────────────────────┤
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ 💊 Tên thuốc *                     │  │
│  │ [Nhập tên thuốc hoặc biệt dược...] │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ 📝 Liều lượng & Hướng dẫn dùng     │  │
│  │ [Ví dụ: 1 viên sau ăn sáng...]     │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Khung Chọn Giờ Báo thức
│  │ ⏰ Giờ uống thuốc *                │  │
│  │ [  08 : 00                 ⏰  ]   │  │  <- Chạm vào mở TimePickerDialog
│  └────────────────────────────────────┘  │
│                                          │
│  ── LẶP LẠI CÁC NGÀY TRONG TUẦN ──────── │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐│  <- 7 Nút Tròn (Filter Chips / Buttons)
│  │ T2│ │ T3│ │ T4│ │ T5│ │ T6│ │ T7│ │ CN││  <- Nền #00897B khi chọn
│  └───┘ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘│
│  [✓] Chọn tất cả các ngày (Hàng ngày)    │  <- CheckBox chọn nhanh
│                                          │
│  ┌────────────────────────────────────┐  │
│  │         LƯU LỊCH UỐNG THUỐC        │  │  <- Filled Button #00897B, 48dp
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Kiểu nhập liệu |
| :--- | :--- | :--- | :--- |
| **Thanh Toolbar** | `toolbar_add_medicine`| `MaterialToolbar`, cao `56dp`, nền `#FFFFFF` | Nút Back Arrow |
| **Khung Tên thuốc** | `til_med_name` | `TextInputLayout` Outlined, bo góc `8dp` | Bắt buộc, `inputType="textCapWords"`|
| **Khung Liều dùng** | `til_med_dosage` | `TextInputLayout` Outlined, bo góc `8dp` | `inputType="text"` |
| **Ô chọn Giờ nhắc** | `et_med_time` | `TextInputEditText`, `clickable="true"`, `focusable="false"` | Mở `TimePickerDialog` định dạng 24h |
| **Nhóm chọn thứ** | `layout_med_days` | `LinearLayout` ngang, phân bổ đều `weight` | 7 `MaterialButton` tròn (40dp × 40dp)|
| **Nút T2 -> CN** | `btn_day_mon`...`sun`| Toggleable Button, bo tròn tròn | Đổi màu `@color/vita_teal` khi active |
| **Chọn tất cả các ngày**| `cb_med_all_days` | `MaterialCheckBox`, `15sp` | Checked: Bật sáng cả 7 ngày (127) |
| **Nút Lưu** | `btn_med_save` | `MaterialButton`, cao `48dp`, bo góc `8dp` | Nền: `@color/vita_teal`, Chữ trắng |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Trải nghiệm chọn giờ trực quan (TimePicker UX):**
   - Hộp thoại `TimePickerDialog` sử dụng phong cách đồng hồ tròn Material Design 3 (`android.R.style.Theme_DeviceDefault_Light_Dialog`).
   - Mặc định đề xuất cữ giờ kế tiếp gần nhất (ví dụ: đang 07:15 sáng thì đề xuất 08:00).
2. **Xử lý Mặt nạ bit (Days Mask Bitwise Logic):**
   - Người dùng bấm chọn các thứ tương ứng với các bit từ 0 (Thứ 2) đến 6 (Chủ Nhật).
   - Nếu không có thứ nào được chọn, tự động nhắc nhở: *"Vui lòng chọn ít nhất một ngày trong tuần để báo thức hoạt động"*.
3. **Phản hồi âm thanh & rung:**
   - Sau khi lưu, hiển thị thông báo Snackbar: *"Đã đặt báo thức nhắc [Tên thuốc] lúc [HH:mm]"*.
