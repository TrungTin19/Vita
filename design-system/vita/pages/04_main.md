# THIẾT KẾ CHI TIẾT GIAO DIỆN: MAIN ACTIVITY
## Màn hình Khung chứa Điều hướng Chính (BottomNavigationView & Toolbar)

> **File:** `MainActivity.java`  
> **Layout:** `res/layout/activity_main.xml`  
> **Menu Layout:** `res/menu/bottom_nav_menu.xml` & `res/menu/main_toolbar_menu.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Là màn hình trung tâm của ứng dụng Vita sau khi đăng nhập thành công.
- Lưu giữ khung chứa 4 phân hệ chính thông qua `FragmentContainerView` (hoặc `FrameLayout`) kết hợp với `BottomNavigationView`:
  1. **Tab 1 - Trang chủ (`HomeFragment`):** Tổng quan BMI, thời tiết & AQI, lời khuyên bù nước.
  2. **Tab 2 - Chỉ số (`HealthFragment`):** Lịch sử các lần đo sinh hiệu, bộ lọc ngày, FAB thêm mới.
  3. **Tab 3 - Nhắc nhở (`ReminderFragment`):** Danh sách thuốc cần uống và thanh tiến độ nạp nước.
  4. **Tab 4 - Thống kê (`StatsFragment`):** Biểu đồ xu hướng MPAndroidChart và nhật ký giấc ngủ.
- Thanh công cụ phía trên (Toolbar):
  - Hiển thị logo/tên màn hình tương ứng với từng Tab đang chọn.
  - Góc trên bên phải chứa icon Hồ sơ cá nhân (`ic_account_circle`): Bấm vào mở `ProfileActivity`.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  Vita          [Tên Tab hiện tại]     👤 │  <- Toolbar (Cao 56dp, #00897B hoặc #FFFFFF)
├──────────────────────────────────────────┤
│                                          │
│                                          │
│        [FRAGMENT CONTAINER VIEW]         │
│                                          │
│         Hiển thị nội dung của            │
│         Fragment đang được chọn          │
│                                          │
│                                          │
│                                          │
│                                          │
├──────────────────────────────────────────┤
│   🏠          ❤️           ⏰          📊   │  <- BottomNavigationView (Cao 56dp)
│ Trang chủ   Chỉ số    Nhắc nhở   Thống kê│
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Màu sắc & Icon |
| :--- | :--- | :--- | :--- |
| **Root Layout** | `root_main` | `CoordinatorLayout` hoặc `ConstraintLayout` | Nền: `@color/vita_bg` (`#F8F9FA`) |
| **Thanh Toolbar** | `toolbar_main` | `MaterialToolbar`, cao `56dp`, elevation `2dp` | Nền: `#FFFFFF`, Chữ: `@color/vita_teal` |
| **Menu Profile** | `action_profile` | Item trong `main_toolbar_menu.xml` | Icon: `ic_account_circle.xml` (24dp) |
| **Khung chứa Fragment**| `fragment_container`| `FragmentContainerView`, `match_parent` | Chiếm toàn bộ không gian giữa Toolbar & Nav |
| **Thanh BottomNav** | `bottom_nav_main` | `BottomNavigationView`, cao `56dp`, elevation `8dp`| Nền: `#FFFFFF`, viền đỉnh `1dp` `#E0E0E0` |

### Bảng cấu hình 4 Tab trên BottomNavigationView:

| Thứ tự Tab | ID Item Menu | Nhãn hiển thị (Title) | Icon Vector XML | Fragment đích |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `nav_home` | **Trang chủ** | `@drawable/ic_nav_home` | `HomeFragment` |
| **2** | `nav_health` | **Chỉ số** | `@drawable/ic_nav_health` | `HealthFragment` |
| **3** | `nav_reminder`| **Nhắc nhở** | `@drawable/ic_nav_reminder` | `ReminderFragment` |
| **4** | `nav_stats` | **Thống kê** | `@drawable/ic_nav_stats` | `StatsFragment` |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Trạng thái Tab Active / Inactive:**
   - Khi được chọn (Active): Icon và chữ đổi sang màu `@color/vita_teal` (`#00897B`), kích thước chữ phóng to nhẹ (`14sp`, Medium).
   - Khi không chọn (Inactive): Icon và chữ giữ màu `@color/vita_text_secondary` (`#616161`), kích thước chữ `12sp`, Regular.
   - Luôn hiển thị nhãn chữ (`app:labelVisibilityMode="labeled"`): Không ẩn chữ để người lớn tuổi nhận biết rõ ràng chức năng.
2. **Quy tắc điều hướng mượt mà:**
   - Thay thế Fragment bằng `FragmentTransaction.replace()` kết hợp phương thức `commit()`.
   - Tránh việc khởi tạo lại Fragment nếu người dùng nhấn lại chính Tab đang đứng (`setOnItemReselectedListener`).
3. **Vùng an toàn (Safe Areas):**
   - Đảm bảo BottomNavigationView nằm cố định phía dưới, không bị bàn phím ảo đẩy lên làm vỡ bố cục khi nhập liệu bằng cách cài đặt `android:windowSoftInputMode="adjustPan"` trong `AndroidManifest.xml`.
