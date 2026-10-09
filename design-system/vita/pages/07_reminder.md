# THIẾT KẾ CHI TIẾT GIAO DIỆN: REMINDER FRAGMENT
## Tab 3: Nhắc nhở Uống thuốc & Theo dõi Tiến độ Nước uống Hàng ngày

> **File:** `ReminderFragment.java`  
> **Layout:** `res/layout/fragment_reminder.xml`  
> **Item Layout:** `res/layout/item_medicine.xml`  
> **Context Menu:** `res/menu/context_menu_medicine.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Quản lý đồng thời 2 thói quen duy trì sức sống quan trọng:
  1. **Theo dõi nước uống trong ngày:**
     - Hiển thị tiến độ uống nước thực tế so với mục tiêu ngày (`water_goal_ml`, mặc định 2000 ml).
     - Thanh tiến độ `ProgressBar` dạng bo tròn hiện đại, hiển thị phần trăm (%) và lượng nước đã uống (ví dụ: `1250 / 2000 ml`).
     - **Nút nạp nhanh "+250 ml" (Quick Add):** Bấm một chạm ghi ngay 1 dòng vào bảng `water_logs`, tăng tiến độ tức thì, có hoạt ảnh nhẹ và âm báo tích cực.
  2. **Danh sách đơn thuốc & lịch nhắc:**
     - Danh sách các loại thuốc đã lên lịch với giờ uống, liều dùng và các ngày trong tuần.
     - **Nút công tắc gạt (Switch):** Bật/Tắt báo thức trực tiếp mà không cần xóa thuốc. Khi gạt Tắt -> hủy Alarm trong hệ thống; khi Bật -> tính toán và kích hoạt lại Alarm kế tiếp.
     - **Nhấn giữ (Context Menu):** Cung cấp 2 tác vụ: `Sửa thuốc` (mở `AddMedicineActivity`) và `Xóa thuốc` (kèm Dialog xác nhận).
  3. **Nút FAB thêm thuốc:** Mở màn hình `AddMedicineActivity` để thêm thuốc mới vào danh mục.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  NHẮC NHỞ & THÓI QUEN HÀNG NGÀY          │  <- Heading 2 (18sp Bold)
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Nước uống CardView (12dp)
│  │ 💧 TIẾN ĐỘ UỐNG NƯỚC HÔM NAY       │  │
│  │                                    │  │
│  │     1,500 / 2,000 ml      (75%)    │  │  <- 20sp Bold, #1976D2 Ocean Blue
│  │  [======================       ]   │  │  <- ProgressBar bo tròn (Cao 12dp)
│  │                                    │  │
│  │  ┌──────────────────────────────┐  │  │
│  │  │      ➕ NẠP 250 ML NƯỚC      │  │  │  <- Nút nạp nhanh (+250ml) #00897B
│  │  └──────────────────────────────┘  │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ── DANH SÁCH LỊCH UỐNG THUỐC ─────────  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Item Thuốc 1 (item_medicine.xml)
│  │ 💊 Panadol Extra       [ BẬT 🟢 ]  │  │  <- Tên thuốc (16sp Bold), Switch
│  │ ⏰ 08:00 • 1 viên sau ăn            │  │  <- Giờ & Liều dùng (14sp)
│  │ Lặp lại: T2, T3, T4, T5, T6, T7, CN│  │  <- Chip thứ lặp lại trong tuần
│  └────────────────────────────────────┘  │  <- (Nhấn giữ mở Context Menu)
│                                          │
│  ┌────────────────────────────────────┐  │  <- Item Thuốc 2
│  │ 💊 Amlodipine 5mg      [ TẮT ⚪ ]  │  │  <- Đang tạm tắt báo thức
│  │ ⏰ 20:00 • 1 viên trước ngủ         │  │
│  │ Lặp lại: Hàng ngày                 │  │
│  └────────────────────────────────────┘  │
│                                          │
│                              ┌────────┐  │
│                              │   ➕   │  │  <- FAB thêm thuốc (#00ACC1, 56dp)
│                              └────────┘  │
│  [Đệm đáy 80dp chống che bởi BottomNav]  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Màu sắc |
| :--- | :--- | :--- | :--- |
| **Nội dung cuộn** | `scroll_reminder` | `NestedScrollView`, paddingBottom `88dp` | Nền: `@color/vita_bg` |
| **Thẻ nước uống** | `cv_water_card` | `MaterialCardView`, cornerRadius `12dp` | Nền: `#FFFFFF`, elevation `2dp` |
| **Chữ số nước** | `tv_water_progress_text`| `TextView`, `20sp`, Bold (700) | Màu: `@color/vita_water` (`#1976D2`) |
| **Thanh tiến độ** | `pb_water_progress` | `ProgressBar`, Style Horizontal, cao `12dp` | ProgressDrawable: `@drawable/progress_water` |
| **Nút +250ml** | `btn_quick_add_water`| `MaterialButton`, `minHeight="48dp"`, bo góc `8dp`| Nền: `@color/vita_teal`, Chữ trắng |
| **Danh sách Thuốc** | `rv_medicines` | `RecyclerView`, `nestedScrollingEnabled="false"` | Gắn `MedicineAdapter` |
| **Trạng thái rỗng thuốc**| `layout_medicine_empty`| `LinearLayout`, căn giữa | Hiện khi chưa có đơn thuốc nào |
| **Nút FAB thêm thuốc** | `fab_add_medicine` | `FloatingActionButton`, `56dp × 56dp` | Nền: `@color/vita_aqua` |

### Thông số Thẻ item thuốc (`item_medicine.xml`):
- `cv_medicine_item`: `MaterialCardView`, cornerRadius `12dp`, padding `16dp`.
- `tv_med_name`: `TextView`, `16sp`, Bold (700), màu `@color/vita_text_primary`.
- `sw_med_active`: `MaterialSwitch`, `thumbTint` & `trackTint` chuyển sang xanh `@color/vita_teal` khi bật.
- `tv_med_dosage_time`: `TextView`, `14sp`, Regular, định dạng `⏰ HH:mm • [Liều dùng]`.
- `tv_med_days`: `TextView`, `12sp`, Regular, màu `@color/vita_text_secondary`, liệt kê các thứ trong tuần được đánh dấu trong `days_mask`.

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Phản hồi khi bấm "+250 ml" (Delight & Engagement):**
   - Nút nạp nước không yêu cầu mở màn hình mới, cộng dồn trực tiếp.
   - Khi bấm, thanh tiến độ nước có hiệu ứng chuyển động mượt mà (`ObjectAnimator` từ mức cũ lên mức mới trong 400ms).
   - Nếu đạt 100% mục tiêu, hiển thị Toast khích lệ: *"Tuyệt vời! Bạn đã hoàn thành mục tiêu nước uống hôm nay!"*.
2. **Thao tác công tắc gạt Switch an toàn:**
   - Khi gạt Switch, cập nhật ngay cờ `is_active` trong bảng `medicines`.
   - Hiển thị Snackbar thông báo ngắn: *"Đã tắt nhắc nhở cho [Tên thuốc]"* kèm nút *"Hoàn tác"* nếu lỡ tay chạm nhầm.
3. **Thực đơn ngữ cảnh Context Menu:**
   - Mục 1: `Sửa đơn thuốc` (Chuyển sang `AddMedicineActivity` truyền `medicine_id`).
   - Mục 2: `Xóa thuốc` (Mở Dialog xác nhận, nếu đồng ý thì hủy Alarm và xóa khỏi SQLite).
