# THIẾT KẾ CHI TIẾT GIAO DIỆN: ADD HEALTH ACTIVITY
## Màn hình Nhập mới & Chỉnh sửa Chỉ số Sinh hiệu Sức khỏe (Bỏ trống lưu NULL)

> **File:** `AddHealthActivity.java`  
> **Layout:** `res/layout/activity_add_health.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Tiếp nhận các số đo sinh hiệu trong ngày của người dùng:
  - Ngày đo (`date`, định dạng `yyyy-MM-dd`, mặc định là ngày hôm nay).
  - Cân nặng (`weight_kg`).
  - Huyết áp tâm thu (`systolic`) & Huyết áp tâm trương (`diastolic`).
  - Nhịp tim (`heart_rate`).
  - Đường huyết (`blood_sugar`).
  - Ghi chú thêm (`note`, ví dụ: "Đo sau bữa sáng 30 phút", "Hơi mệt").
- **Nguyên tắc kỹ thuật quan trọng (Đặc tả đề tài):**
  - **Cho phép bỏ trống (Lưu NULL vào SQLite):** Không bắt buộc người dùng phải đo đủ mọi chỉ số trong một lần. Nếu chỉ đo huyết áp thì để trống cân nặng và đường huyết, hệ thống vẫn lưu bình thường và hiển thị `--` khi tra cứu.
  - Phải có ít nhất một chỉ số được nhập hoặc có ghi chú.
- **Hỗ trợ 2 chế độ:**
  1. *Thêm mới (Add Mode):* Mở từ nút FAB trên `HealthFragment`.
  2. *Chỉnh sửa (Edit Mode):* Mở từ Context Menu hoặc `DetailHealthActivity`, tự động đổ dữ liệu cũ vào các ô nhập.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  ← (Hủy)          Ghi nhận chỉ số đo     │  <- Toolbar trắng
├──────────────────────────────────────────┤
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ 📅 Ngày đo                         │  │
│  │ [ 09/10/2026                 📅 ]  │  <- Bấm mở DatePickerDialog
│  └────────────────────────────────────┘  │
│                                          │
│  ── HUYẾT ÁP & TIM MẠCH ──────────────── │
│  ┌──────────────────┐ ┌───────────────┐  │
│  │ Tâm thu (mmHg)   │ │ Tâm trương    │  │
│  │ [120]            │ │ [80]          │  │
│  └──────────────────┘ └───────────────┘  │
│  ┌────────────────────────────────────┐  │
│  │ Nhịp tim (bpm - nhịp/phút)         │  │
│  │ [75]                               │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ── THỂ TRẠNG & ĐƯỜNG HUYẾT ──────────── │
│  ┌──────────────────┐ ┌───────────────┐  │
│  │ Cân nặng (kg)    │ │ Đường huyết   │  │
│  │ [62.5]           │ │ [95.0] (mg/dL)│  │
│  └──────────────────┘ └───────────────┘  │
│                                          │
│  ── GHI CHÚ BỔ SUNG ──────────────────── │
│  ┌────────────────────────────────────┐  │
│  │ Ghi chú (cảm giác cơ thể, thuốc...)│  │
│  │ [Nhập ghi chú...]                  │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ℹ️ Bạn có thể bỏ trống các chỉ số chưa đo│  <- Helper text (13sp, #616161)
│                                          │
│  ┌────────────────────────────────────┐  │
│  │           LƯU BẢN GHI              │  │  <- Filled Button #00897B, 48dp
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Kiểu nhập liệu |
| :--- | :--- | :--- | :--- |
| **Thanh Toolbar** | `toolbar_add_health` | `MaterialToolbar`, cao `56dp`, nền `#FFFFFF` | Nút Back Arrow |
| **Nội dung cuộn** | `scroll_add_health` | `ScrollView`, `fillViewport="true"`, padding `20dp` | Nền: `@color/vita_bg` |
| **Ô chọn Ngày đo** | `et_add_health_date` | `TextInputEditText`, `clickable="true"`, `focusable="false"` | Mở `DatePickerDialog` |
| **Khung Tâm thu** | `til_add_systolic` | `TextInputLayout` Outlined, suffix "mmHg" | `inputType="number"` |
| **Khung Tâm trương** | `til_add_diastolic` | `TextInputLayout` Outlined, suffix "mmHg" | `inputType="number"` |
| **Khung Nhịp tim** | `til_add_heart_rate` | `TextInputLayout` Outlined, suffix "bpm" | `inputType="number"` |
| **Khung Cân nặng** | `til_add_weight` | `TextInputLayout` Outlined, suffix "kg" | `inputType="numberDecimal"` |
| **Khung Đường huyết** | `til_add_sugar` | `TextInputLayout` Outlined, suffix "mg/dL"| `inputType="numberDecimal"` |
| **Khung Ghi chú** | `til_add_note` | `TextInputLayout` Outlined, lines `3` | `inputType="textMultiLine"` |
| **Nút Lưu bản ghi** | `btn_add_health_save`| `MaterialButton`, cao `48dp`, bo góc `8dp` | Nền: `@color/vita_teal`, Chữ trắng |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Kiểm tra tính hợp lý của chỉ số (Sanity Validation):**
   - Nếu nhập tâm thu nhỏ hơn tâm trương (ví dụ: tâm thu 70, tâm trương 110), hiển thị cảnh báo lỗi tức thì: *"Huyết áp tâm thu thường lớn hơn tâm trương. Bạn hãy kiểm tra lại nhé."*.
   - Nhịp tim hợp lý từ 30 đến 250 bpm; Cân nặng từ 20 đến 300 kg.
2. **Trải nghiệm chọn Ngày thuận tiện:**
   - Trường chọn ngày được thiết kế như một nút bấm lớn: Người dùng chạm vào bất kỳ đâu trên ô ngày đều bung ngay giao diện lịch `DatePickerDialog`, không yêu cầu người dùng phải gõ chuỗi ngày thủ công.
3. **Phản hồi sau khi lưu:**
   - Sau khi lưu thành công vào bảng `health_records`, kết thúc Activity (`finish()`) và hiển thị Toast: *"Đã lưu bản ghi sức khỏe thành công!"*.
