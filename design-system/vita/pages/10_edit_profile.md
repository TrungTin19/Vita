# THIẾT KẾ CHI TIẾT GIAO DIỆN: EDIT PROFILE ACTIVITY
## Màn hình Chỉnh sửa Thông tin Cá nhân và Đăng xuất Tài khoản

> **File:** `EditProfileActivity.java`  
> **Layout:** `res/layout/activity_edit_profile.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Cho phép người dùng cập nhật các thông tin cá nhân và thông số thể chất:
  - Họ và tên (`fullname`).
  - Năm sinh (`birth_year`) và Giới tính (`gender`).
  - Chiều cao (`height_cm`) và Cân nặng cơ sở (`base_weight_kg`).
  - Mục tiêu nước uống trong ngày (`water_goal_ml`, ví dụ: 2000 ml).
- Lưu lại thay đổi vào bảng `users` trong SQLite thông qua `UserDao.updateProfile(...)`.
- **Nút Đăng xuất an toàn (Logout Action):**
  - Đặt ở cuối màn hình để tránh vô tình bấm nhầm.
  - Khi bấm, hiển thị Dialog xác nhận: *"Bạn có muốn đăng xuất khỏi ứng dụng Vita không?"*.
  - Nếu xác nhận: Xóa thông tin phiên trong `SessionManager`, chuyển về `LoginActivity` với cờ `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` để xóa sạch ngăn xếp Activity (Back Stack).

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  ← (Hủy bỏ)          Chỉnh sửa hồ sơ     │  <- Toolbar trắng, nút Back
├──────────────────────────────────────────┤
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ Họ và tên                          │  │
│  │ [Huỳnh Trung Tín]                  │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌──────────────────┐ ┌───────────────┐  │
│  │ Năm sinh         │ │ Giới tính     │  │
│  │ [1980]           │ │ [Nam       ▼] │  │
│  └──────────────────┘ └───────────────┘  │
│                                          │
│  ┌──────────────────┐ ┌───────────────┐  │
│  │ Chiều cao (cm)   │ │ Cân nặng cơ sở│  │
│  │ [168]            │ │ [64.0]        │  │
│  └──────────────────┘ └───────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ Mục tiêu nước uống mỗi ngày (ml)   │  │
│  │ [2000]                             │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │           LƯU THAY ĐỔI             │  │  <- Filled Button #00897B, 48dp
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │            ĐĂNG XUẤT               │  │  <- Outlined / Text Button Đỏ (#D32F2F)
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Màu sắc |
| :--- | :--- | :--- | :--- |
| **Thanh Toolbar** | `toolbar_edit_profile`| `MaterialToolbar`, cao `56dp`, nền `#FFFFFF` | Nút Back Arrow hủy quay về |
| **Nội dung cuộn** | `scroll_edit_profile` | `ScrollView`, `fillViewport="true"`, padding `20dp` | Nền: `@color/vita_bg` |
| **Khung Họ tên** | `til_edit_fullname` | `TextInputLayout` Outlined, bo góc `8dp` | `inputType="textPersonName"` |
| **Khung Năm sinh** | `til_edit_birth_year` | `TextInputLayout` Outlined | `inputType="number"`, maxLength 4 |
| **Spinner Giới tính** | `sp_edit_gender` | `Spinner`, cao `48dp`, nền bo góc viền xám | "Nam", "Nữ", "Khác" |
| **Khung Chiều cao** | `til_edit_height` | `TextInputLayout` Outlined, suffix "cm" | `inputType="numberDecimal"` |
| **Khung Cân nặng** | `til_edit_weight` | `TextInputLayout` Outlined, suffix "kg" | `inputType="numberDecimal"` |
| **Khung Mục tiêu nước**| `til_edit_water_goal` | `TextInputLayout` Outlined, suffix "ml" | `inputType="number"` |
| **Nút Lưu** | `btn_edit_save` | `MaterialButton`, cao `48dp`, bo góc `8dp` | Nền: `@color/vita_teal`, Chữ trắng |
| **Nút Đăng xuất** | `btn_edit_logout` | `MaterialButton` Outlined, cao `48dp` | Viền & Chữ: `@color/vita_status_danger` (`#D32F2F`)|

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Bảo vệ dữ liệu người dùng:**
   - Nếu người dùng đã sửa đổi nhưng bấm nút Back quay lại mà chưa Lưu, hiển thị hộp thoại cảnh báo: *"Thay đổi chưa được lưu. Bạn có chắc muốn thoát?"*.
2. **Thiết kế Nút Đăng xuất an toàn (Destructive Action Design):**
   - Nút Đăng xuất có màu đỏ cảnh báo (`#D32F2F`) để người dùng chú ý phân biệt rõ với nút Lưu (`#00897B`).
   - Khoảng cách giữa nút Lưu và nút Đăng xuất là `16dp` để tuyệt đối tránh bấm nhầm ngón tay.
   - Hộp thoại xác nhận đăng xuất có nút "Hủy" được tô sáng mặc định, nút "Đồng ý đăng xuất" hiển thị chữ đỏ.
