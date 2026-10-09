# THIẾT KẾ CHI TIẾT GIAO DIỆN: STATS FRAGMENT
## Tab 4: Thống kê Trực quan Xu hướng Sức khỏe (MPAndroidChart) & Lịch sử Giấc ngủ

> **File:** `StatsFragment.java`  
> **Layout:** `res/layout/fragment_stats.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Trực quan hóa dữ liệu sinh hiệu từ SQLite giúp người dùng và bác sĩ gia đình quan sát được diễn biến sức khỏe theo thời gian.
- **Thanh chuyển đổi phạm vi thời gian (Time Range Toggle):**
  - Hai nút chọn nhanh: **"7 ngày gần nhất"** (Mặc định) và **"30 ngày gần nhất"**.
- **Biểu đồ Đường song song (MPAndroidChart LineChart):**
  - **Biểu đồ Huyết áp:** Vẽ 2 đường song song rõ rệt trên cùng một đồ thị:
    - Đường Huyết áp Tâm thu (Systolic): Màu đỏ san hô `@color/vita_status_danger` (`#D32F2F`), nét đậm, có chấm tròn tại các điểm đo.
    - Đường Huyết áp Tâm trương (Diastolic): Màu xanh dương `@color/vita_water` (`#1976D2`).
    - Có đường tham chiếu chuẩn (LimitLine) ở mức 120/80 mmHg để người dùng so sánh.
  - **Biểu đồ Cân nặng:** Đường màu xanh Teal `@color/vita_teal` (`#00897B`) với vùng tô mờ bên dưới (Cubic Bezier curve).
- **Biểu đồ Cột (MPAndroidChart BarChart):**
  - Thống kê lượng nước uống mỗi ngày (Màu xanh nước biển).
  - Thống kê thời lượng ngủ mỗi đêm (Màu tím chàm `@color/vita_sleep` `#512DA8`).
- **Phân hệ Giấc ngủ:**
  - Danh sách `RecyclerView` hiển thị chi tiết các đêm ngủ gần nhất (Giờ ngủ, giờ thức, tổng thời lượng).
  - Nút FAB (hoặc nút hành động) mở `AddSleepActivity` để ghi nhận giấc ngủ đêm qua.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  XU HƯỚNG & PHÂN TÍCH THỐNG KÊ           │  <- Heading 2 (18sp Bold)
│                                          │
│  ┌────────────────────────────────────┐  │  <- Segmented Toggle Button
│  │   [ 7 ngày qua ]   |   30 ngày qua │  │  <- Bo góc 8dp, Nền active #00897B
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Card Huyết áp LineChart (12dp)
│  │ 📈 BIẾN THIÊN HUYẾT ÁP (mmHg)      │  │
│  │  🔴 Tâm thu     🔵 Tâm trương      │  │  <- Chú giải (Legend)
│  │                                    │  │
│  │  140 ┌───●────────●──────●──       │  │  <- LineChart vẽ mượt mà (Cubic)
│  │  120 ├───┼──●──●──┼──────┼──       │  │  <- LimitLine 120/80
│  │   80 └───●──●──●──●──────●──       │  │
│  │      03/10       ...   09/10       │  │  <- Trục X định dạng dd/MM
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Card Nước & Giấc ngủ BarChart
│  │ 📊 LƯỢNG NƯỚC UỐNG MỖI NGÀY (ml)   │  │
│  │  [||]  [||]  [||]  [||]  [||]  [||]│  │  <- Cột BarChart màu #1976D2
│  │  Mục tiêu: 2,000 ml/ngày           │  │  <- LimitLine mục tiêu
│  └────────────────────────────────────┘  │
│                                          │
│  ── NHẬT KÝ GIẤC NGỦ GẦN ĐÂY ─────────  │
│  ┌────────────────────────────────────┐  │  <- Card Giấc ngủ (item_sleep.xml)
│  │ 🌙 Đêm 08/10 -> Sáng 09/10         │  │
│  │ Ngủ: 23:00 - Thức: 06:30 (7.5 giờ) │  │  <- Tự tính qua nửa đêm
│  │ Trạng thái: [ Giấc ngủ đủ giấc ]   │  │  <- Badge Tím/Xanh lá
│  └────────────────────────────────────┘  │
│                                          │
│                              ┌────────┐  │
│                              │   ➕   │  │  <- FAB ghi giấc ngủ (#00ACC1, 56dp)
│                              └────────┘  │
│  [Đệm đáy 80dp chống che bởi BottomNav]  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Cấu hình MPAndroidChart |
| :--- | :--- | :--- | :--- |
| **Nội dung cuộn** | `scroll_stats` | `NestedScrollView`, paddingBottom `88dp` | Nền: `@color/vita_bg` |
| **Nút chuyển 7/30 ngày**| `toggle_time_range`| `MaterialButtonToggleGroup`, cao `40dp` | Phân cấp tab chọn khoảng ngày |
| **Thẻ Đồ thị Huyết áp** | `cv_chart_bp` | `MaterialCardView`, cornerRadius `12dp` | Nền: `#FFFFFF`, elevation `2dp` |
| **Biểu đồ Huyết áp** | `chart_bp` | `com.github.mikephil.charting.charts.LineChart` | Cao `220dp`, tắt PinchZoom, bật Marker |
| **Thẻ Đồ thị Nước** | `cv_chart_water` | `MaterialCardView`, cornerRadius `12dp` | Nền: `#FFFFFF`, elevation `2dp` |
| **Biểu đồ Cột Nước** | `chart_water` | `com.github.mikephil.charting.charts.BarChart` | Cao `200dp`, bo góc đầu cột |
| **Danh sách Giấc ngủ** | `rv_sleep_records` | `RecyclerView`, `nestedScrollingEnabled="false"` | Gắn `SleepRecordAdapter` |
| **Nút FAB Thêm ngủ** | `fab_add_sleep` | `FloatingActionButton`, `56dp × 56dp` | Nền: `@color/vita_aqua`, icon `ic_sleep` |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX BIỂU ĐỒ (UI-UX-PRO-MAX)
1. **Thiết kế Tiếp cận Màu sắc (Colorblind-Safe Chart):**
   - Đồ thị Huyết áp không chỉ dựa vào màu đỏ và xanh: Nét vẽ tâm thu dùng nét liền (`solid`), nét vẽ tâm trương dùng nét đứt nhẹ hoặc chấm tròn to hơn để người mù màu vẫn phân biệt tuyệt đối.
   - Khi chạm vào một điểm nút trên biểu đồ, hiển thị `MarkerView` nổi với nền đen chữ trắng hiển thị rõ ngày và con số đo: *"09/10: 122/82 mmHg"*.
2. **Xử lý Trạng thái Ít Dữ liệu (Empty Data State):**
   - Nếu khoảng ngày được chọn có ít hơn 2 bản ghi, biểu đồ hiển thị thông điệp hướng dẫn ân cần: *"Chưa đủ dữ liệu vẽ đồ thị. Hãy ghi nhận ít nhất 2 ngày để theo dõi xu hướng nhé."* thay vì để đồ thị trống trơn gây hiểu lầm.
3. **Hoạt ảnh biểu đồ mượt mà:**
   - Kích hoạt phương thức `chart.animateY(800, Easing.EaseInOutQuad)` khi màn hình hiển thị để biểu đồ dâng lên sống động.
