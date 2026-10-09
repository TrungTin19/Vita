# HỆ THỐNG DESIGN TOKENS 3 TẦNG VITA (THREE-LAYER TOKEN ARCHITECTURE)
## Quy chuẩn Tokens, Trạng thái Thành phần (States & Variants) và Mã nguồn Android Native

> **Tiêu chuẩn áp dụng:** W3C Design Tokens Community Group & Material Design 3  
> **Nền tảng:** Android Native (Java + XML Resource System)  
> **Tệp cấu hình máy đọc:** [`tokens.json`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/tokens.json)  
> **Tệp CSS Variables sinh tự động:** [`tokens.css`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/tokens.css)  
> **Nguồn chân lý mẹ:** [`MASTER.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/MASTER.md)

---

## 1. MÔ HÌNH KIẾN TRÚC 3 TẦNG (TOKEN ARCHITECTURE)

Hệ thống token của Vita được phân tầng chặt chẽ theo cấu trúc 3 lớp nhằm loại bỏ hoàn toàn các giá trị hardcode (mã hex tùy tiện, kích thước dp/sp rải rác) trong các layout XML:

```text
┌────────────────────────────────────────────────────────────────────────┐
│  TẦNG 3: COMPONENT TOKENS (Đặc thù từng Widget)                         │
│  @color/btn_primary_bg, @dimen/card_radius, @dimen/btn_height_min      │
├────────────────────────────────────────────────────────────────────────┤
│  TẦNG 2: SEMANTIC TOKENS (Gán ngữ nghĩa & mục đích sử dụng)            │
│  @color/vita_teal, @color/vita_bg, @color/vita_status_good             │
├────────────────────────────────────────────────────────────────────────┤
│  TẦNG 1: PRIMITIVE TOKENS (Giá trị thô nền tảng: Hex, Base dp, Base sp)│
│  #00897B, #00ACC1, #F8F9FA, 4dp, 8dp, 16dp, 24dp, 32sp                 │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. MA TRẬN TRẠNG THÁI & BIẾN THỂ THÀNH PHẦN (STATES & VARIANTS MATRIX)

Theo chuẩn `ui-ux-pro-max` và `design-system`, mọi widget tương tác bắt buộc phải xác định hành vi và màu sắc trên cả 4 trạng thái: **Default (Mặc định), Hover/Focus (Tiêu điểm), Pressed/Active (Đang nhấn/chạm), Disabled (Vô hiệu hóa)**.

### 2.1. Nút bấm Chính (Filled Primary Button - `btn_primary`)

| Thuộc tính (Property) | Default | Focus | Pressed (Chạm) | Disabled |
| :--- | :--- | :--- | :--- | :--- |
| **Nền (Background)** | `@color/vita_teal` (`#00897B`) | `@color/vita_teal_dark` | Ripple `#005B4F` | `#E0E0E0` (Xám nhạt) |
| **Màu chữ (Text)** | `#FFFFFF` | `#FFFFFF` | `#FFFFFF` | `#9E9E9E` (Xám mờ) |
| **Độ nổi bóng (Elevation)**| `2dp` | `4dp` | `6dp` | `0dp` (Phẳng) |
| **Chiều cao tối thiểu**| `48dp` (Touch target chuẩn) | `48dp` | `48dp` | `48dp` |
| **Bán kính bo góc** | `8dp` (`@dimen/radius_button`)| `8dp` | `8dp` | `8dp` |

### 2.2. Nút bấm Phụ (Outlined Secondary Button - `btn_secondary`)

| Thuộc tính | Default | Focus | Pressed | Disabled |
| :--- | :--- | :--- | :--- | :--- |
| **Nền (Background)** | Trong suốt (`@android:color/transparent`) | `@color/vita_teal_light` (10%) | Ripple xanh Teal | Trong suốt |
| **Viền (Stroke)** | `1.5dp` solid `@color/vita_teal` | `2dp` solid `@color/vita_teal` | `1.5dp` solid `@color/vita_teal` | `1dp` solid `#E0E0E0` |
| **Màu chữ (Text)** | `@color/vita_teal` (`#00897B`) | `@color/vita_teal_dark` | `@color/vita_teal_dark` | `#9E9E9E` |
| **Chiều cao / Bo góc** | `48dp` / `8dp` | `48dp` / `8dp` | `48dp` / `8dp` | `48dp` / `8dp` |

### 2.3. Nút Nguy hiểm (Destructive Button - Đăng xuất / Xóa)

| Thuộc tính | Default | Focus | Pressed | Disabled |
| :--- | :--- | :--- | :--- | :--- |
| **Nền** | Trong suốt | Nền đỏ nhạt (10%) | Ripple đỏ nhạt | Trong suốt |
| **Viền** | `1.5dp` solid `@color/vita_status_danger` | `2dp` solid `#D32F2F` | `1.5dp` solid `#D32F2F` | `1dp` solid `#E0E0E0` |
| **Màu chữ** | `@color/vita_status_danger` (`#D32F2F`)| `#B71C1C` | `#B71C1C` | `#9E9E9E` |

### 2.4. Thẻ Dữ liệu (Material CardView - `cv_card`)

| Thuộc tính | Resting (Tĩnh) | Hover / Focus | Long Press (Context Menu) |
| :--- | :--- | :--- | :--- |
| **Màu nền (Card Surface)** | `#FFFFFF` (`@color/vita_surface`)| `#FFFFFF` | Nền sáng có hiệu ứng gợn sóng |
| **Độ nổi bóng (Elevation)**| `2dp` | `3dp` | `4dp` (`@dimen/elevation_card_pressed`) |
| **Bo góc (Corner Radius)** | `12dp` (`@dimen/radius_card`) | `12dp` | `12dp` |
| **Đường viền phụ (Stroke)** | `1dp` `@color/vita_divider` | `1dp` `@color/vita_divider` | `1.5dp` `@color/vita_teal_light` |
| **Lề trong (Padding)** | `16dp` (`@dimen/spacing_md`) | `16dp` | `16dp` |

### 2.5. Khung Nhập liệu (TextInputLayout & TextInputEditText)

| Thuộc tính | Default (Chưa chạm) | Focus (Đang nhập) | Error (Có lỗi) | Disabled |
| :--- | :--- | :--- | :--- | :--- |
| **Độ dày viền (Box Stroke)**| `1dp` | `2dp` | `2dp` | `1dp` |
| **Màu viền (Stroke Color)** | `@color/vita_divider` (`#E0E0E0`)| `@color/vita_teal` (`#00897B`)| `@color/vita_status_danger` (`#D32F2F`)| `#E0E0E0` |
| **Màu Nhãn (Hint / Label)** | `@color/vita_text_secondary` | `@color/vita_teal` | `@color/vita_status_danger` | `#9E9E9E` |
| **Chữ nhập (Input Text)** | `@color/vita_text_primary` | `@color/vita_text_primary` | `@color/vita_text_primary` | `#9E9E9E` |
| **Thông điệp trợ giúp / Lỗi**| `12sp` Regular (`#616161`) | `12sp` Regular (`#00897B`) | `12sp` Regular (`#D32F2F`) kèm Icon ⚠️ | Ẩn |

### 2.6. Công tắc Gạt (MaterialSwitch - Lịch nhắc thuốc)

| Thuộc tính | Checked (Đang Bật) | Unchecked (Đang Tắt) | Disabled |
| :--- | :--- | :--- | :--- |
| **Nút trượt (Thumb Color)** | `@color/vita_teal` (`#00897B`) | `#FFFFFF` | `#BDBDBD` |
| **Thanh trượt (Track Color)** | `@color/vita_teal_light` (`#4EBAAA`)| `#E0E0E0` | `#EEEEEE` |

### 2.7. Nút Hành động Nổi (FloatingActionButton - FAB)

| Thuộc tính | Resting | Pressed (Chạm ngón tay) | Vị trí neo |
| :--- | :--- | :--- | :--- |
| **Kích thước** | `56dp × 56dp` | `56dp × 56dp` | Góc dưới phải (Bottom End) |
| **Màu nền** | `@color/vita_aqua` (`#00ACC1`)| `@color/vita_aqua_dark` (`#007C91`)| Cách lề phải `16dp`, lề đáy `16dp` |
| **Biểu tượng (Icon)** | Dấu `+` màu trắng (`24dp`) | Dấu `+` màu trắng (`24dp`) | Nổi phía trên BottomNav |
| **Độ nổi bóng** | `6dp` | `12dp` | - |

---

## 3. BẢNG MÃ NGUỒN TÀI NGUYÊN ANDROID RES/VALUES CHUẨN

Bộ mã nguồn sau được đóng gói sẵn để sao chép trực tiếp vào dự án Android Studio:

### 3.1. Tệp `res/values/colors.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ============================================================ -->
    <!-- TẦNG 1 & 2: PRIMITIVE & SEMANTIC BRAND COLORS               -->
    <!-- ============================================================ -->
    <!-- Primary Teal -->
    <color name="vita_teal">#00897B</color>
    <color name="vita_teal_dark">#005B4F</color>
    <color name="vita_teal_light">#4EBAAA</color>
    <color name="vita_teal_50">#E0F2F1</color>

    <!-- Secondary Aqua -->
    <color name="vita_aqua">#00ACC1</color>
    <color name="vita_aqua_dark">#007C91</color>
    <color name="vita_aqua_light">#E0F7FA</color>

    <!-- Neutrals & Surfaces -->
    <color name="vita_bg">#F8F9FA</color>
    <color name="vita_surface">#FFFFFF</color>
    <color name="vita_divider">#E0E0E0</color>
    <color name="vita_text_primary">#1C1B1F</color>
    <color name="vita_text_secondary">#616161</color>
    <color name="vita_text_disabled">#9E9E9E</color>

    <!-- Semantic Status & Features -->
    <color name="vita_status_good">#2E7D32</color>
    <color name="vita_status_warning">#F57C00</color>
    <color name="vita_status_danger">#D32F2F</color>
    <color name="vita_water">#1976D2</color>
    <color name="vita_sleep">#512DA8</color>

    <!-- ============================================================ -->
    <!-- TẦNG 3: COMPONENT LEVEL ALIASES                              -->
    <!-- ============================================================ -->
    <color name="btn_primary_bg">@color/vita_teal</color>
    <color name="btn_primary_fg">#FFFFFF</color>
    <color name="btn_fab_bg">@color/vita_aqua</color>
    <color name="card_bg">@color/vita_surface</color>
    <color name="card_stroke">@color/vita_divider</color>
    <color name="bottom_nav_active">@color/vita_teal</color>
    <color name="bottom_nav_inactive">@color/vita_text_secondary</color>
</resources>
```

### 3.2. Tệp `res/values/dimens.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ============================================================ -->
    <!-- TẦNG 1: PRIMITIVE SPACING (8dp GRID RHYTHM)                  -->
    <!-- ============================================================ -->
    <dimen name="spacing_xs">4dp</dimen>
    <dimen name="spacing_sm">8dp</dimen>
    <dimen name="spacing_md">16dp</dimen>
    <dimen name="spacing_lg">24dp</dimen>
    <dimen name="spacing_xl">32dp</dimen>
    <dimen name="spacing_2xl">48dp</dimen>
    <dimen name="spacing_scroll_pad">80dp</dimen>

    <!-- ============================================================ -->
    <!-- TẦNG 1 & 2: TYPOGRAPHY (SCALE-INDEPENDENT PIXELS)           -->
    <!-- ============================================================ -->
    <dimen name="text_display">32sp</dimen>
    <dimen name="text_h1">22sp</dimen>
    <dimen name="text_h2">18sp</dimen>
    <dimen name="text_body_lg">16sp</dimen>
    <dimen name="text_body">14sp</dimen>
    <dimen name="text_caption">12sp</dimen>

    <!-- ============================================================ -->
    <!-- TẦNG 3: COMPONENT SPECIFIC DIMENSIONS                        -->
    <!-- ============================================================ -->
    <!-- Radius Tokens -->
    <dimen name="radius_button">8dp</dimen>
    <dimen name="radius_input">8dp</dimen>
    <dimen name="radius_card">12dp</dimen>
    <dimen name="radius_pill">16dp</dimen>

    <!-- Elevation Tokens -->
    <dimen name="elevation_card_resting">2dp</dimen>
    <dimen name="elevation_card_pressed">4dp</dimen>
    <dimen name="elevation_toolbar">2dp</dimen>
    <dimen name="elevation_fab">6dp</dimen>
    <dimen name="elevation_bottom_nav">8dp</dimen>

    <!-- Touch Target Minimums -->
    <dimen name="min_touch_target">48dp</dimen>
    <dimen name="fab_size">56dp</dimen>
    <dimen name="bottom_nav_height">56dp</dimen>
    <dimen name="toolbar_height">56dp</dimen>
</resources>
```

### 3.3. Tệp `res/values/styles.xml` / `themes.xml` (Áp dụng Tokens vào Style Thành phần)
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Base Theme kế thừa Material 3 -->
    <style name="Theme.Vita" parent="Theme.Material3.Light.NoActionBar">
        <!-- Brand Colors -->
        <item name="colorPrimary">@color/vita_teal</item>
        <item name="colorPrimaryVariant">@color/vita_teal_dark</item>
        <item name="colorSecondary">@color/vita_aqua</item>
        <item name="android:statusBarColor">@color/vita_teal_dark</item>
        <item name="android:windowBackground">@color/vita_bg</item>
    </style>

    <!-- Style: Nút Chính (Primary Filled Button) -->
    <style name="Widget.Vita.Button.Primary" parent="Widget.Material3.Button">
        <item name="android:minHeight">@dimen/min_touch_target</item>
        <item name="android:textAllCaps">false</item>
        <item name="android:textSize">@dimen/text_body_lg</item>
        <item name="backgroundTint">@color/btn_primary_bg</item>
        <item name="android:textColor">@color/btn_primary_fg</item>
        <item name="cornerRadius">@dimen/radius_button</item>
    </style>

    <!-- Style: Nút Phụ (Outlined Button) -->
    <style name="Widget.Vita.Button.Outlined" parent="Widget.Material3.Button.OutlinedButton">
        <item name="android:minHeight">@dimen/min_touch_target</item>
        <item name="android:textAllCaps">false</item>
        <item name="android:textSize">@dimen/text_body_lg</item>
        <item name="android:textColor">@color/vita_teal</item>
        <item name="strokeColor">@color/vita_teal</item>
        <item name="strokeWidth">1.5dp</item>
        <item name="cornerRadius">@dimen/radius_button</item>
    </style>

    <!-- Style: Thẻ CardView hiển thị dữ liệu -->
    <style name="Widget.Vita.Card" parent="Widget.Material3.CardView.Elevated">
        <item name="cardCornerRadius">@dimen/radius_card</item>
        <item name="cardElevation">@dimen/elevation_card_resting</item>
        <item name="cardBackgroundColor">@color/card_bg</item>
        <item name="strokeColor">@color/card_stroke</item>
        <item name="strokeWidth">1dp</item>
        <item name="contentPadding">@dimen/spacing_md</item>
    </style>
</resources>
```

---

## 4. BẢNG TRA CỨU NHANH TOKEN -> CODE JAVA & XML

| Khi cần áp dụng | Trong Layout XML | Trong Code Java |
| :--- | :--- | :--- |
| **Màu xanh chủ đạo** | `@color/vita_teal` | `ContextCompat.getColor(context, R.color.vita_teal)` |
| **Màu cảnh báo xấu** | `@color/vita_status_danger` | `ContextCompat.getColor(context, R.color.vita_status_danger)` |
| **Kích thước chữ lớn** | `android:textSize="@dimen/text_display"` | `tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32)` |
| **Khoảng cách lề chuẩn** | `android:padding="@dimen/spacing_md"` | `getResources().getDimensionPixelSize(R.dimen.spacing_md)` |
| **Vùng đệm đáy chống che** | `android:paddingBottom="@dimen/spacing_scroll_pad"` | Đặt cố định `80dp` cho RecyclerView |

---

## 5. TIÊU CHUẨN ĐỒ HỌA BIỂU TƯỢNG (GOOGLE FONTS MATERIAL SYMBOLS)

Toàn bộ ứng dụng Vita thống nhất sử dụng thư viện **Material Symbols từ Google Fonts** (`fonts.google.com/icons`) dưới dạng **Android Vector Drawable XML (`<vector>`)**:

| Tên Icon Vector | Tên Material Symbol gốc | Kích thước | Mục đích sử dụng |
| :--- | :--- | :--- | :--- |
| `ic_arrow_back.xml` | `arrow_back` | 24dp × 24dp | Điều hướng quay lại trên Toolbar |
| `ic_person.xml` | `person` | 24dp × 24dp | Nhập tên đăng nhập & Tab hồ sơ |
| `ic_lock.xml` | `lock` | 24dp × 24dp | Nhập mật khẩu |
| `ic_profile_user.xml`| `account_circle` / `person` | 48dp × 48dp | Avatar đại diện tài khoản người dùng |
| `ic_nav_home.xml` | `home` | 24dp × 24dp | Tab Trang chủ (Bottom Navigation) |
| `ic_nav_vitals.xml` | `monitor_heart` | 24dp × 24dp | Tab Chỉ số sức khỏe & BMI |
| `ic_nav_medicines.xml`| `medication` | 24dp × 24dp | Tab Lịch nhắc uống thuốc |
| `ic_water_drop.xml` | `water_drop` | 24dp × 24dp | Nhật ký lượng nước uống |
| `ic_bedtime.xml` | `bedtime` | 24dp × 24dp | Nhật ký theo dõi giấc ngủ |
| `ic_notifications.xml`| `notifications` | 24dp × 24dp | Thông báo nhắc nhở & chuông báo thức |
| `ic_add.xml` | `add` | 24dp × 24dp | Nút FAB / Thêm cữ thuốc / Thêm số đo |
| `ic_check.xml` | `check` | 24dp × 24dp | Xác nhận đã uống thuốc / Hoàn tất |
| `ic_calendar.xml` | `calendar_today` | 24dp × 24dp | Lịch trình & Chọn ngày tháng |

**Quy tắc bất di bất dịch:**
- Viewport chuẩn: `viewportWidth="24"`, `viewportHeight="24"`.
- Kích thước mặc định: `width="24dp"`, `height="24dp"` (trừ avatar lớn 48dp).
- Không hardcode màu tĩnh bên trong vector path nếu icon cần đổi trạng thái tint; dùng `android:tint="?attr/colorControlNormal"` hoặc tô màu ngữ nghĩa qua XML layout.
- Tuyệt đối không dùng icon của Android SDK cũ (`@android:drawable/...`).
