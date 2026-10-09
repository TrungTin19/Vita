# THIẾT KẾ CHI TIẾT GIAO DIỆN: PROFILE ACTIVITY
## Màn hình Xem Hồ sơ Cá nhân và Cấu hình Chuẩn Phân loại BMI

> **File:** `ProfileActivity.java`  
> **Layout:** `res/layout/activity_profile.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Cho phép người dùng xem toàn bộ thông số thể trạng hiện tại của mình: Họ tên, Tên đăng nhập, Năm sinh (và Tuổi tính tự động), Giới tính, Chiều cao, Cân nặng cơ sở, Mục tiêu nước uống mỗi ngày.
- **Tính năng lựa chọn Chuẩn BMI (Chức năng 2 & 5):**
  - Cung cấp nhóm nút chọn `RadioGroup` để người dùng linh hoạt chọn giữa:
    1. **Chuẩn Châu Á (IDI & WPRO - Mặc định cho người Việt Nam):** Ngưỡng Bình thường: `18.5 – 22.9`, Thừa cân: `23.0 – 24.9`, Béo phì: $\ge 25.0$.
    2. **Chuẩn Quốc tế (WHO):** Ngưỡng Bình thường: `18.5 – 24.9`, Tiền béo phì: `25.0 – 29.9`, Béo phì: $\ge 30.0$.
  - Khi thay đổi chuẩn, tự động cập nhật trường `bmi_standard` trong bảng `users` và tính toán lại phân loại hiển thị tức thì.
- **Nút điều hướng chỉnh sửa:** Bấm nút "Chỉnh sửa hồ sơ" mở `EditProfileActivity`.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  ← (Quay lại)         Hồ sơ cá nhân      │  <- Toolbar trắng, tiêu đề 18sp Bold
├──────────────────────────────────────────┤
│                                          │
│              [ Avatar 👤 ]               │  <- Vòng tròn 80dp, nền Teal nhạt
│            Huỳnh Trung Tín               │  <- Heading 1 (22sp Bold, #1C1B1F)
│             @trungtin_vita               │  <- Caption (14sp, #616161)
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Thông số Thể chất (12dp)
│  │ THÔNG SỐ CƠ THỂ                    │  │
│  │ • Năm sinh: 1980 (46 tuổi)         │  │
│  │ • Giới tính: Nam                   │  │
│  │ • Chiều cao: 168 cm                │  │
│  │ • Cân nặng ban đầu: 64.0 kg        │  │
│  │ • Mục tiêu nước uống: 2,000 ml/ngày│  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Chuẩn phân loại BMI
│  │ CHUẨN PHÂN LOẠI BMI                │  │
│  │ (●) Chuẩn Châu Á (IDI & WPRO)      │  │  <- RadioButton được chọn
│  │     (Khuyến nghị cho người Việt)   │  │
│  │ (○) Chuẩn Quốc tế (WHO)            │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │         CHỈNH SỬA HỒ SƠ            │  │  <- Outlined Button #00897B, 48dp
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Giá trị |
| :--- | :--- | :--- | :--- |
| **Thanh Toolbar** | `toolbar_profile` | `MaterialToolbar`, cao `56dp`, nền `#FFFFFF` | Nút Back Arrow điều hướng quay lại |
| **Avatar đại diện** | `iv_profile_avatar` | `ImageView`, `80dp × 80dp`, bo tròn | Nền: `@color/vita_teal_light` |
| **Họ tên hiển thị** | `tv_profile_fullname`| `TextView`, `22sp`, Bold (700) | Màu: `@color/vita_text_primary` |
| **Tên tài khoản** | `tv_profile_username`| `TextView`, `14sp`, Regular (400) | Định dạng `@username` |
| **Thẻ thông số** | `cv_profile_stats` | `MaterialCardView`, cornerRadius `12dp` | Nền: `#FFFFFF`, elevation `2dp` |
| **Nhóm chọn chuẩn** | `rg_bmi_standard` | `RadioGroup`, vertical orientation | Gồm 2 RadioButton |
| **Radio Châu Á** | `rb_bmi_asian` | `RadioButton`, chữ `15sp`, Medium | Checked: `@color/vita_teal` |
| **Radio WHO** | `rb_bmi_who` | `RadioButton`, chữ `15sp`, Medium | Checked: `@color/vita_teal` |
| **Nút sửa hồ sơ** | `btn_profile_edit` | `MaterialButton` Outlined, cao `48dp` | Viền `@color/vita_teal`, Chữ Teal |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
- Khi người dùng chuyển đổi RadioButton chọn chuẩn BMI, hiển thị Snackbar thông báo xác nhận: *"Đã cập nhật chuẩn đánh giá thể trạng sang [Châu Á / Quốc tế]"*.
- Toàn bộ các dòng thông tin (Chiều cao, Cân nặng, Tuổi) có khoảng cách dòng (Line Spacing) tối thiểu `8dp` để người dùng lớn tuổi dễ phân biệt từng mục, không bị rối mắt.
