# THIẾT KẾ CHI TIẾT GIAO DIỆN: LOGIN ACTIVITY
## Màn hình Đăng nhập Tài khoản Người dùng

> **File:** `LoginActivity.java`  
> **Layout:** `res/layout/activity_login.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Tiếp nhận thông tin định danh: Tên đăng nhập (`username`) và Mật khẩu (`password`).
- Kiểm tra hợp lệ dữ liệu nhập (Validation):
  - Không được bỏ trống `username` hoặc `password`.
  - Hiển thị thông báo lỗi tức thì trên `TextInputLayout.setError(...)`.
- Khi người dùng bấm **"Đăng nhập"**:
  - Truy vấn `UserDao.login(username, password)`.
  - Nếu thành công: Lưu `user_id` vào `SessionManager`, mở `MainActivity`, kết thúc `LoginActivity`.
  - Nếu thất bại: Hiển thị thông báo thân thiện: *"Tên đăng nhập hoặc mật khẩu chưa chính xác. Vui lòng kiểm tra lại."*
- Có nút chuyển nhanh sang `RegisterActivity` cho người dùng mới.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│             StatusBar (Teal Dark)        │
│                                          │
│  [Logo Vita nhỏ 64dp]                    │
│                                          │
│  Đăng nhập vào Vita                      │
│  (Heading 1 22sp Bold, #1C1B1F)          │
│  Cùng chăm sóc sức khỏe mỗi ngày nhé!    │
│  (Body Regular 14sp, #616161)            │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ 👤 Tên đăng nhập                   │  │
│  │ [Nhập tên tài khoản...]            │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ 🔒 Mật khẩu                        │  │
│  │ [••••••••••••]              👁 (Ẩn)│  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │            ĐĂNG NHẬP               │  │
│  │     (Filled Button #00897B, 48dp)  │  │
│  └────────────────────────────────────┘  │
│                                          │
│  Chưa có tài khoản? Đăng ký ngay         │
│  (Text Link 14sp, Medium, #00897B)       │
│                                          │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Màu sắc & Tương tác |
| :--- | :--- | :--- | :--- |
| **Root ScrollView** | `scroll_login` | `ScrollView`, `fillViewport="true"`, padding `24dp` | Nền: `@color/vita_bg` |
| **Biểu tượng nhỏ** | `iv_login_logo` | `ImageView`, `64dp × 64dp`, marginBottom `16dp` | Drawable: `@drawable/vita_app_icon` |
| **Tiêu đề trang** | `tv_login_title` | `TextView`, `22sp`, Bold (700) | Màu: `@color/vita_text_primary` |
| **Phụ đề trang** | `tv_login_subtitle`| `TextView`, `14sp`, Regular (400), marginBottom `24dp`| Màu: `@color/vita_text_secondary` |
| **Khung Tên đăng nhập**| `til_login_username`| `TextInputLayout` Outlined, bo góc `8dp` | Focus stroke: `@color/vita_teal` (2dp) |
| **Ô nhập Username** | `et_login_username` | `TextInputEditText`, `16sp`, `inputType="text"` | Chữ: `@color/vita_text_primary` |
| **Khung Mật khẩu** | `til_login_password`| `TextInputLayout` Outlined, `passwordToggleEnabled="true"` | Icon hiện/ẩn mật khẩu tích hợp |
| **Ô nhập Mật khẩu** | `et_login_password` | `TextInputEditText`, `16sp`, `inputType="textPassword"` | Hỗ trợ Password Autofill & Paste |
| **Nút Đăng nhập** | `btn_login_submit` | `MaterialButton`, `minHeight="48dp"`, bo góc `8dp` | Nền: `@color/vita_teal`, Chữ trắng `16sp` |
| **Nút chuyển Đăng ký**| `tv_login_goto_register`| `TextView`, `minHeight="48dp"`, căn giữa, `14sp` | Màu: `@color/vita_teal`, In đậm |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Quản lý Bàn phím & Vùng chạm:**
   - Trường `et_login_username` có cờ `imeOptions="actionNext"`. Bấm Next tự động chuyển con trỏ sang ô mật khẩu.
   - Trường `et_login_password` có cờ `imeOptions="actionDone"`. Bấm Done trên bàn phím tự động kích hoạt sự kiện bấm nút Đăng nhập.
2. **Khả năng tiếp cận & Trợ năng (A11y):**
   - Mọi `TextInputLayout` có `hint` tiếng Việt rõ nghĩa: *"Tên đăng nhập"* và *"Mật khẩu"*.
   - Cho phép dán (paste) mật khẩu từ trình quản lý mật khẩu của Android.
   - Tỷ lệ tương phản nút Đăng nhập: 4.6:1 (Đạt chuẩn WCAG AA).
