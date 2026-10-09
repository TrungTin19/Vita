# THIẾT KẾ CHI TIẾT GIAO DIỆN: REGISTER ACTIVITY
## Màn hình Đăng ký Tài khoản và Khởi tạo Thông số Sức khỏe Ban đầu

> **File:** `RegisterActivity.java`  
> **Layout:** `res/layout/activity_register.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Tiếp nhận thông tin đăng ký tài khoản mới và thiết lập các thông số thể chất ban đầu của người dùng (phục vụ tính BMI và mục tiêu nước):
  - Thông tin tài khoản: Tên đăng nhập (`username`), Mật khẩu (`password`), Xác nhận mật khẩu (`confirm_password`).
  - Thông tin cá nhân: Họ và tên (`fullname`), Năm sinh (`birth_year`), Giới tính (`gender`).
  - Thông số thể chất cơ sở: Chiều cao (`height_cm`), Cân nặng ban đầu (`base_weight_kg`).
- Kiểm tra tính hợp lệ dữ liệu (Validation):
  - Tên đăng nhập không được rỗng, không chứa khoảng trắng, tối thiểu 3 ký tự.
  - Mật khẩu tối thiểu 6 ký tự; Xác nhận mật khẩu phải trùng khớp với Mật khẩu.
  - Năm sinh hợp lệ (ví dụ: từ 1920 đến năm hiện tại).
  - Chiều cao (từ 50cm đến 250cm), Cân nặng (từ 20kg đến 300kg).
- Khi người dùng bấm **"Hoàn tất Đăng ký"**:
  - Kiểm tra xem `username` đã tồn tại trong bảng `users` chưa. Nếu đã có: Báo lỗi *"Tên đăng nhập đã tồn tại, vui lòng chọn tên khác"*.
  - Nếu hợp lệ: Sinh ngẫu nhiên `salt`, băm mật khẩu `SHA-256`, lưu tài khoản vào SQLite qua `UserDao.register(...)`.
  - Tự động ghi nhận phiên vào `SessionManager`, chuyển hướng vào thẳng `MainActivity`, kết thúc `RegisterActivity`.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  ← (Nút Quay lại)     Tạo tài khoản mới  │
├──────────────────────────────────────────┤
│                                          │
│  Bắt đầu hành trình sống khỏe cùng Vita! │
│  (Body Regular 14sp, #616161)            │
│                                          │
│  ── THÔNG TIN ĐĂNG NHẬP ───────────────  │
│  ┌────────────────────────────────────┐  │
│  │ 👤 Tên đăng nhập *                 │  │
│  │ [Nhập tên tài khoản...]            │  │
│  └────────────────────────────────────┘  │
│  ┌────────────────────────────────────┐  │
│  │ 🔒 Mật khẩu (tối thiểu 6 ký tự) *  │  │
│  │ [••••••••••••]              👁 (Ẩn)│  │
│  └────────────────────────────────────┘  │
│  ┌────────────────────────────────────┐  │
│  │ 🔒 Xác nhận lại mật khẩu *         │  │
│  │ [••••••••••••]              👁 (Ẩn)│  │
│  └────────────────────────────────────┘  │
│                                          │
│  ── THÔNG TIN THỂ CHẤT BAN ĐẦU ─────────  │
│  ┌────────────────────────────────────┐  │
│  │ Họ và tên                          │  │
│  │ [Ví dụ: Nguyễn Văn A]              │  │
│  └────────────────────────────────────┘  │
│  ┌──────────────────┐ ┌───────────────┐  │
│  │ Năm sinh         │ │ Giới tính     │  │
│  │ [1975]           │ │ [Nam / Nữ ▼]  │  │
│  └──────────────────┘ └───────────────┘  │
│  ┌──────────────────┐ ┌───────────────┐  │
│  │ Chiều cao (cm) * │ │ Cân nặng (kg)*│  │
│  │ [165]            │ │ [62.5]        │  │
│  └──────────────────┘ └───────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │          HOÀN TẤT ĐĂNG KÝ          │  │
│  │    (Filled Button #00897B, 48dp)   │  │
│  └────────────────────────────────────┘  │
│                                          │
│  Đã có tài khoản? Đăng nhập ngay         │
│  (Text Link 14sp, #00897B)               │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Giá trị mặc định |
| :--- | :--- | :--- | :--- |
| **Thanh công cụ Toolbar** | `toolbar_register` | `MaterialToolbar`, cao `56dp`, nền `#FFFFFF` | Nút Back Navigation mũi tên trái |
| **Nội dung cuộn** | `scroll_register` | `ScrollView`, `fillViewport="true"`, padding `20dp` | Nền: `@color/vita_bg` |
| **Khung Tên đăng nhập** | `til_reg_username` | `TextInputLayout` Outlined, bo góc `8dp` | Bắt buộc, không dấu, không space |
| **Khung Mật khẩu** | `til_reg_password` | `TextInputLayout` Outlined, Toggle ẩn/hiện | `inputType="textPassword"` |
| **Khung Xác nhận MK** | `til_reg_confirm` | `TextInputLayout` Outlined, Toggle ẩn/hiện | So khớp trực tiếp khi gõ |
| **Khung Họ và tên** | `til_reg_fullname` | `TextInputLayout` Outlined, bo góc `8dp` | `inputType="textPersonName"` |
| **Khung Năm sinh** | `til_reg_birth_year`| `TextInputLayout` Outlined | `inputType="number"`, maxLength 4 |
| **Chọn Giới tính** | `sp_reg_gender` | `Spinner`, nền bo góc `8dp`, viền `1dp` | Các mục: "Nam", "Nữ", "Khác" |
| **Khung Chiều cao** | `til_reg_height` | `TextInputLayout` Outlined, suffix "cm" | `inputType="numberDecimal"` |
| **Khung Cân nặng** | `til_reg_weight` | `TextInputLayout` Outlined, suffix "kg" | `inputType="numberDecimal"` |
| **Nút Đăng ký** | `btn_reg_submit` | `MaterialButton`, `minHeight="48dp"`, bo góc `8dp`| Nền: `@color/vita_teal`, Chữ trắng `16sp`|
| **Nút chuyển Đăng nhập** | `tv_reg_goto_login` | `TextView`, `minHeight="48dp"`, căn giữa, `14sp` | Màu: `@color/vita_teal` |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Kiểm tra tức thì (Inline Validation):**
   - Khi người dùng rời khỏi ô Xác nhận mật khẩu (`OnFocusChangeListener`), nếu không khớp, hiển thị ngay `setError("Mật khẩu xác nhận không trùng khớp")`.
   - Khi người dùng bắt đầu chỉnh sửa lại, tự động xóa thông báo lỗi (`setError(null)`).
2. **Hỗ trợ người dùng nhập liệu nhanh:**
   - Trường Chiều cao và Cân nặng tự động bật bàn phím số thập phân (`numberDecimal`).
   - Suffix Text hiển thị đơn vị rõ ràng: *"cm"* và *"kg"* giúp người dùng không bị nhầm lẫn đơn vị đo.
3. **Tiếp cận người cao tuổi:**
   - Kích thước vùng bấm chuyển giới tính dạng Spinner cao tối thiểu `48dp`, chữ to rõ ràng `16sp`.
