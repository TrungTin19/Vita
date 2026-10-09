# THIẾT KẾ CHI TIẾT GIAO DIỆN: HEALTH FRAGMENT
## Tab 2: Lịch sử Chỉ số Sức khỏe, Bộ lọc Ngày, Danh sách RecyclerView & Context Menu

> **File:** `HealthFragment.java`  
> **Layout:** `res/layout/fragment_health.xml`  
> **Item Layout:** `res/layout/item_health_record.xml`  
> **Context Menu:** `res/menu/context_menu_health.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Là trung tâm lưu trữ và tra cứu lịch sử sinh hiệu sức khỏe đo định kỳ: Cân nặng (`weight_kg`), Huyết áp (`systolic`/`diastolic`), Nhịp tim (`heart_rate`), Đường huyết (`blood_sugar`).
- **Danh sách theo thời gian giảm dần (Chronological Order):** Bản ghi mới nhất hiển thị trên cùng.
- **Thanh lọc theo ngày (Date Filter):** Cho phép chọn ngày bắt đầu hoặc xem toàn bộ.
- **Tương tác cốt lõi trên RecyclerView (Trọng tâm Đồ án & Chương 2 Báo cáo):**
  - **Nhấn nhanh (Single Click):** Mở `DetailHealthActivity` để xem toàn bộ thông tin chi tiết và ghi chú.
  - **Nhấn giữ (Long Click / Context Menu):** Hiển thị thực đơn ngữ cảnh gồm 2 tùy chọn:
    1. **"Sửa bản ghi này"**: Mở `AddHealthActivity` kèm dữ liệu cũ qua Intent (`is_edit = true`).
    2. **"Xóa bản ghi"**: Mở Dialog xác nhận *"Bạn có chắc chắn muốn xóa bản ghi ngày [ngày] không?"*. Nếu đồng ý -> Gọi `HealthDao.delete()`, cập nhật RecyclerView và hiển thị Toast phản hồi.
- **Nút hành động nổi (FAB):** Đặt ở góc dưới phải, nhấn vào mở `AddHealthActivity` để thêm lượt đo mới.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  NHẬT KÝ CHỈ SỐ SỨC KHỎE                 │  <- Heading 2 (18sp Bold)
│                                          │
│  [📅 Tất cả ngày ▼]   [🔍 Lọc theo ngày] │  <- Thanh công cụ lọc ngày
│                                          │
│  ┌────────────────────────────────────┐  │  <- RecyclerView Item 1 (CardView 12dp)
│  │ 📅 Hôm nay, 09/10/2026             │  │  <- Tiêu đề ngày (14sp Semi-Bold)
│  │                                    │  │
│  │  Huyết áp: 122/82 mmHg   [Bình thường]│  <- Semantic Badge Xanh lá #2E7D32
│  │  Cân nặng: 61.5 kg       Tim: 75 bpm│  │
│  │  Đường huyết: 95 mg/dL            │  │
│  └────────────────────────────────────┘  │  <- (Nhấn giữ mở Context Menu Sửa/Xóa)
│                                          │
│  ┌────────────────────────────────────┐  │  <- RecyclerView Item 2
│  │ 📅 Hôm qua, 08/10/2026             │  │
│  │                                    │  │
│  │  Huyết áp: 138/88 mmHg   [Cần chú ý]  │  <- Semantic Badge Vàng cam #F57C00
│  │  Cân nặng: 61.8 kg       Tim: 82 bpm│  │
│  │  Đường huyết: -- (Chưa đo)         │  │
│  └────────────────────────────────────┘  │
│                                          │
│                              ┌────────┐  │
│                              │   ➕   │  │  <- FAB (#00ACC1 Sky Aqua, 56dp)
│                              └────────┘  │
│  [Đệm đáy 80dp chống che bởi BottomNav]  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Tương tác |
| :--- | :--- | :--- | :--- |
| **Root Layout** | `root_health` | `CoordinatorLayout`, `match_parent` | Nền: `@color/vita_bg` |
| **Thanh lọc ngày** | `layout_health_filter`| `LinearLayout` ngang, padding `12dp` | Nền trắng, bo góc `8dp` |
| **Nút chọn ngày lọc** | `btn_health_filter_date`| `MaterialButton` Outlined, icon `ic_calendar` | Mở `DatePickerDialog` |
| **Nút xóa bộ lọc** | `btn_health_clear_filter`| `MaterialButton` TextButton, nhãn "Xem tất cả" | Tải lại toàn bộ bản ghi |
| **Danh sách Recycler**| `rv_health_records` | `RecyclerView`, `clipToPadding="false"`, paddingBottom `88dp` | Gắn `HealthRecordAdapter` |
| **Trạng thái rỗng** | `layout_health_empty` | `LinearLayout`, icon `ic_empty_notes`, 48dp | Hiển thị khi chưa có bản ghi |
| **Nút FAB thêm mới** | `fab_add_health` | `FloatingActionButton`, `56dp × 56dp` | Nền: `@color/vita_aqua`, icon dấu `+` |

### Thông số Thẻ item danh sách (`item_health_record.xml`):
- `cv_health_item`: `MaterialCardView`, cornerRadius `12dp`, elevation `2dp`, cardUseCompatPadding `true`.
- `tv_item_date`: `TextView`, `14sp`, Semi-Bold, chữ `@color/vita_text_primary`.
- `tv_item_bp`: `TextView`, `16sp`, Bold, hiển thị ví dụ: `120/80 mmHg`.
- `tv_item_bp_status`: `TextView`, Badge nền mờ viền bo tròn `12dp`, chữ màu trạng thái chuẩn y tế.
- `tv_item_weight`: `TextView`, `14sp`, hiển thị `62.0 kg`.
- `tv_item_heart_rate`: `TextView`, `14sp`, hiển thị `78 bpm`.
- `tv_item_sugar`: `TextView`, `14sp`, nếu null hiển thị `Đường huyết: --`.

---

## 4. CHI TIẾT TƯƠNG TÁC CONTEXT MENU & DIALOG (CHƯƠNG 2 BÁO CÁO)
1. **Đăng ký Context Menu:**
   - Trong `ViewHolder`, gắn `itemView.setOnCreateContextMenuListener(...)`.
   - Các mục Menu:
     - Item 1: `Sửa bản ghi này` (ID: `menu_health_edit`, Icon: `ic_edit`).
     - Item 2: `Xóa bản ghi này` (ID: `menu_health_delete`, Icon: `ic_delete`).
2. **Hộp thoại xác nhận Xóa (Confirmation Dialog):**
   - Tiêu đề: *"Xác nhận xóa"*
   - Nội dung: *"Bạn có chắc chắn muốn xóa bản ghi đo ngày [ngày] không? Thao tác này không thể hoàn tác."*
   - Nút Hành động:
     - Nút "Hủy bỏ" (`NegativeButton`): Giữ nguyên dữ liệu.
     - Nút "Xóa" (`PositiveButton`): Chữ màu đỏ `@color/vita_status_danger` (`#D32F2F`), thực hiện xóa trong CSDL và cập nhật Adapter bằng `notifyItemRemoved()`.
