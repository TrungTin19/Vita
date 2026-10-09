# THIẾT KẾ CHI TIẾT GIAO DIỆN: SPLASH ACTIVITY
## Màn hình Khởi động và Điều hướng Phiên người dùng

> **File:** `SplashActivity.java`  
> **Layout:** `res/layout/activity_splash.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Là điểm tiếp xúc đầu tiên khi người dùng mở ứng dụng Vita.
- **Hành vi ngầm (Logic):**
  1. Hiển thị Logo biểu tượng Vita và Slogan thương hiệu với hoạt ảnh nhẹ nhàng (Fade-in mượt mà trong 600ms).
  2. Truy vấn `SessionManager.isLoggedIn()` từ `SharedPreferences`:
     - Nếu **ĐÃ ĐĂNG NHẬP (`user_id > 0`)**: Chuyển thẳng tới `MainActivity`, gọi `finish()`.
     - Nếu **CHƯA ĐĂNG NHẬP**: Chuyển tới `LoginActivity` sau độ trễ trải nghiệm `1200ms`, gọi `finish()`.
- Thời gian hiển thị tối đa: `1200ms - 1500ms`, không bắt người dùng chờ đợi lâu.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│             StatusBar (Teal Dark)        │
│                                          │
│                                          │
│                                          │
│                 [LOGO]                   │
│          Biểu tượng Chữ V cách điệu      │
│          kết hợp Lá mầm & Nhịp tim       │
│                  (120dp)                 │
│                                          │
│                   Vita                   │
│         (Display Large 32sp Bold)        │
│                                          │
│       "Sức sống mỗi ngày, an tâm tại nhà" │
│          (Body Regular 14sp, #00897B)    │
│                                          │
│                                          │
│                                          │
│                                          │
│             [ProgressBar]                │
│       Vòng tròn xoay nhỏ gọn (32dp)      │
│                                          │
│       Phiên bản 1.0 - ĐH Thủ Dầu Một      │
│           (Caption 12sp, #616161)        │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Màu sắc quy chuẩn |
| :--- | :--- | :--- | :--- |
| **Root Layout** | `root_splash` | `ConstraintLayout`, `match_parent` | Nền: `@color/vita_bg` (`#F8F9FA`) |
| **App Logo** | `iv_splash_logo` | `ImageView`, `120dp × 120dp`, căn giữa màn hình | Drawable: `@drawable/vita_app_icon` |
| **Tên ứng dụng** | `tv_splash_title` | `TextView`, `32sp`, Bold (700), marginTop `16dp` | Màu: `@color/vita_teal` (`#00897B`) |
| **Khẩu hiệu (Slogan)**| `tv_splash_slogan` | `TextView`, `14sp`, Medium (500), marginTop `8dp` | Màu: `@color/vita_text_secondary` (`#616161`) |
| **Chỉ báo tải** | `pb_splash_loading`| `ProgressBar` (Indeterminate), `32dp × 32dp`, marginBottom `48dp` | Tint: `@color/vita_aqua` (`#00ACC1`) |
| **Thông tin bản quyền**| `tv_splash_version`| `TextView`, `12sp`, Regular (400), marginBottom `16dp` | Màu: `@color/vita_text_secondary` |

---

## 4. QUY CHUẨN TIẾP CẬN & ĐA THIẾT BỊ (ACCESSIBILITY & RESPONSIVE)
- `iv_splash_logo`: Gắn `android:contentDescription="Biểu tượng ứng dụng quản lý sức khỏe Vita"`.
- Hoạt ảnh `alpha` từ `0f` lên `1f` trong `600ms`. Nếu người dùng bật `Reduce Motion` trên thiết bị, hiển thị tĩnh ngay lập tức.
- Không chứa nút bấm điều hướng thủ công (tự động điều phối qua Intent).
