# BẢN QUY CHUẨN NHẬN DIỆN THƯƠNG HIỆU VITA (BRAND GUIDELINES v1.0)
## Ứng dụng Quản lý Sức khỏe Cá nhân và Gia đình tại Nhà (Android Native)

> **Cập nhật lần cuối:** 09/10/2026  
> **Trạng thái:** Chính thức (Approved)  
> **Nền tảng:** Android (Mobile Native) – Học phần Đồ án môn học PTUDD, ĐH Thủ Dầu Một  

---

## TỔNG QUAN NHANH (QUICK REFERENCE)

| Yếu tố nhận diện | Giá trị quy chuẩn | Ý nghĩa & Vị trí áp dụng |
| :--- | :--- | :--- |
| **Tên thương hiệu** | **Vita** | Tiếng Latinh mang ý nghĩa "Sự sống, sức sống, nguồn năng lượng tươi trẻ" |
| **Khẩu hiệu (Slogan)** | **Sức sống mỗi ngày, an tâm tại nhà** | Cam kết đồng hành chăm sóc sức khỏe gia đình chủ động |
| **Màu chủ đạo (Primary)** | `#00897B` (Vita Teal Green) | Tượng trưng cho sự phục hồi y tế, cân bằng, an toàn sinh học |
| **Màu phụ (Secondary)** | `#00ACC1` (Sky Aqua) | Tượng trưng cho công nghệ hiện đại, độ ẩm, sự thanh lọc |
| **Font chữ hệ thống** | `Roboto` (Android System Font) | Tối ưu hiển thị, nhẹ nhàng, tương thích 100% mọi dòng máy |
| **Giọng điệu (Voice)** | **Ân cần – Minh bạch – Khích lệ – Chuẩn mực** | Dễ hiểu với người cao tuổi, không gây hoang mang, tôn trọng quyền riêng tư |

---

## 1. BẢNG MÀU NHẬN DIỆN THƯƠNG HIỆU (COLOR PALETTE)

### 1.1. Bảng màu Chính & Phụ (Primary & Secondary)

| Tên màu | Mã Hex | RGB | Phân cấp Android | Ứng dụng thực tế |
| :--- | :--- | :--- | :--- | :--- |
| **Vita Teal (Chính)** | `#00897B` | `0, 137, 123` | `colorPrimary` | Toolbar, Header, Nút chính (Button CTA), Tab active |
| **Vita Teal Dark** | `#005B4F` | `0, 91, 79` | `colorPrimaryVariant`| Thanh trạng thái hệ thống (StatusBar) |
| **Vita Teal Light** | `#4EBaaa` | `78, 186, 170`| `colorSecondaryContainer` | Nền chip được chọn, viền highlight thẻ Card |
| **Sky Aqua (Phụ)** | `#00ACC1` | `0, 172, 193` | `colorSecondary` | Nút hành động nổi (FAB), thanh tiến độ nước ProgressBar |
| **Sky Aqua Dark** | `#007C91` | `0, 124, 145` | `colorSecondaryVariant` | Trạng thái nhấn (Pressed state) của nút phụ |

### 1.2. Bảng màu Trung tính & Nền (Neutral & Surface)

| Tên màu | Mã Hex | RGB | Ứng dụng thực tế |
| :--- | :--- | :--- | :--- |
| **Background (Nền)** | `#F8F9FA` | `248, 249, 250`| Nền toàn màn hình ứng dụng (dịu mắt, chống mỏi mắt) |
| **Surface (Bề mặt)** | `#FFFFFF` | `255, 255, 255`| Nền các thẻ CardView, Dialog thông báo, BottomSheet |
| **Text Primary** | `#1C1B1F` | `28, 27, 31` | Tiêu đề, số đo chỉ số sức khỏe, nội dung chính (Tương phản cao) |
| **Text Secondary** | `#616161` | `97, 97, 97` | Ghi chú, ngày đo, đơn vị tính (kg, mmHg, bpm, ml) |
| **Border / Divider** | `#E0E0E0` | `224, 224, 224`| Đường phân cách item trong RecyclerView, viền input form |

### 1.3. Bảng màu Trạng thái & Tính năng Sức khỏe (Semantic & Features)

| Trạng thái | Mã Hex | Tên nhãn hiển thị | Ý nghĩa & Vị trí sử dụng |
| :--- | :--- | :--- | :--- |
| **Success / Good** | `#2E7D32` | **Bình thường / Tốt** | BMI chuẩn, huyết áp ổn định, đã uống thuốc (`TAKEN`) |
| **Warning / Caution**| `#F57C00` | **Cần chú ý / Thừa cân** | Tiền tăng huyết áp, thừa cân, AQI mức nhạy cảm (101–150) |
| **Alert / Danger** | `#D32F2F` | **Cảnh báo / Nguy cơ** | Huyết áp cao độ 1-2, đường huyết cao, bỏ uống thuốc (`SKIPPED`) |
| **Water / Hydration**| `#1976D2` | **Nước uống** | Icon giọt nước, biểu đồ lượng nước, nút nạp "+250 ml" |
| **Sleep / Rest** | `#512DA8` | **Giấc ngủ** | Icon vầng trăng, biểu đồ cột thời lượng giấc ngủ |

### 1.4. Tiêu chuẩn Tiếp cận & Độ tương phản (Accessibility Standards)
* Chữ chính (`#1C1B1F`) trên nền trắng (`#FFFFFF`): Tỷ lệ tương phản **13.4:1** (Vượt chuẩn **WCAG AAA** tối đa).
* Nút chính Vita Teal (`#00897B`) với chữ trắng: Tỷ lệ tương phản **4.6:1** (Đạt chuẩn **WCAG AA** cho nút bấm cảm ứng).
* Mọi biểu tượng trạng thái đều đi kèm **chữ mô tả song song** (không chỉ dựa vào màu sắc) để hỗ trợ người dùng bị suy giảm thị lực hoặc mù màu.

---

## 2. QUY CHUẨN KIỂU CHỮ (TYPOGRAPHY SPECIFICATIONS)

Ứng dụng sử dụng font chữ **Roboto** mặc định của Android Studio. Tối ưu theo chuẩn đơn vị `sp` (scale-independent pixels) để tự động phóng to khi người dùng bật tính năng "Chữ lớn" trên điện thoại.

| Cấp độ | Cỡ chữ (sp) | Trọng số (Weight) | Màu sắc quy định | Vị trí áp dụng |
| :--- | :---: | :---: | :--- | :--- |
| **Display (Số đo lớn)** | **32sp** | Bold (700) | Primary / Semantic | Con số cân nặng, huyết áp, nhịp tim trên thẻ tóm tắt |
| **Heading 1 (H1)** | **22sp** | Bold (700) | Text Primary (`#1C1B1F`) | Tiêu đề màn hình Toolbar, Tên người dùng |
| **Heading 2 (H2)** | **18sp** | Semi-Bold (600)| Text Primary (`#1C1B1F`) | Tiêu đề các mục: "Chỉ số hôm nay", "Lịch uống thuốc" |
| **Body Large** | **16sp** | Medium (500) | Text Primary (`#1C1B1F`) | Tên thuốc, tên chỉ số, chữ trên nút bấm CTA |
| **Body Regular** | **14sp** | Regular (400) | Text Secondary (`#616161`)| Lời khuyên thời tiết, liều lượng thuốc, ghi chú sức khỏe |
| **Caption / Label** | **12sp** | Regular (400) | Text Secondary (`#616161`)| Giờ đo, ngày tháng trên trục X biểu đồ, nhãn BottomNav |

---

## 3. LOGO VÀ BIỂU TƯỢNG ỨNG DỤNG (LOGO & ICONOGRAPHY)

### 3.1. Ý niệm thiết kế Logo (Logo Concept)
Biểu tượng thương hiệu Vita kết hợp hài hòa 3 hình tượng mang ý nghĩa nhân văn:
1. **Chữ V (Vita / Vitality):** Biểu trưng cho sự vươn lên mạnh mẽ và chiến thắng bệnh tật.
2. **Chiếc lá mầm xanh:** Biểu trưng cho tự nhiên, sự tái sinh và lối sống lành mạnh bền vững.
3. **Nhịp đập trái tim & Giọt nước:** Nằm trọn trong lòng chữ V, tượng trưng cho sự tuần hoàn sinh mệnh và chăm sóc sức khỏe mỗi ngày.

### 3.2. Quy chuẩn kích thước App Icon trên Android (Mipmap Resources)

| Mật độ màn hình | Kích thước Pixel | Tên thư mục tài nguyên | Mục đích |
| :--- | :---: | :--- | :--- |
| **mdpi** | 48 × 48 px | `res/mipmap-mdpi/` | Thiết bị chuẩn cơ bản |
| **hdpi** | 72 × 72 px | `res/mipmap-hdpi/` | Thiết bị độ phân giải cao |
| **xhdpi** | 96 × 96 px | `res/mipmap-xhdpi/` | Màn hình Retina / Full HD |
| **xxhdpi** | 144 × 144 px | `res/mipmap-xxhdpi/` | Chuẩn máy hiện đại phổ biến |
| **xxxhdpi** | 192 × 192 px | `res/mipmap-xxxhdpi/` | Màn hình 2K/4K siêu nét |
| **Google Play Icon** | 512 × 512 px | Thư mục tài liệu đồ án | Nộp bài báo cáo và bìa thuyết trình |

### 3.3. Vùng an toàn và Nguyên tắc cấm (Logo Don'ts)
* **Khoảng cách an toàn (Clear space):** Luôn giữ khoảng trống tối thiểu bằng 25% chiều cao logo ở cả 4 phía.
* **Không làm:**
  - Không kéo dãn, bóp méo tỷ lệ ngang dọc của biểu tượng.
  - Không đặt biểu tượng trên nền có hoa văn phức tạp làm giảm độ nhận diện.
  - Không tự ý thay đổi màu sắc ngoài bảng màu quy chuẩn (`#00897B`).
  - Không thêm hiệu ứng đổ bóng 3D hoặc viền phát sáng lòe loẹt.

---

## 4. GIỌNG ĐIỆU VÀ KHUNG THÔNG ĐIỆP (VOICE & MESSAGING FRAMEWORK)

### 4.1. Khung giọng điệu theo ngữ cảnh (Voice Matrix)

| Ngữ cảnh | Giọng điệu | Ví dụ chuẩn của Vita | Ví dụ cần tránh |
| :--- | :--- | :--- | :--- |
| **Chào buổi sáng** | Ân cần, tươi vui | *"Chào buổi sáng! Hãy khởi đầu ngày mới bằng một ly nước ấm nhé."* | *"Hôm nay bạn chưa uống nước, nguy cơ mất nước cao."* |
| **Nhắc uống thuốc** | Rõ ràng, đúng giờ | *"Đã đến cữ uống thuốc: [Panadol] - Liều: [1 viên sau ăn]. Bạn đã uống chưa?"* | *"Báo thức! Uống thuốc ngay lập tức."* |
| **Thời tiết nắng nóng**| Chu đáo, khuyên nhủ | *"Hôm nay trời nắng nóng 34°C. Vita gợi ý bạn nên bổ sung thêm 300ml nước nhé."* | *"Nắng gắt nguy hiểm, hãy ở trong nhà."* |
| **Cảnh báo chỉ số cao**| Bình tĩnh, cẩn trọng | *"Chỉ số huyết áp hôm nay ở mức chú ý (135/88 mmHg). Hãy nghỉ ngơi và theo dõi lại sau 30 phút nhé."* | *"Huyết áp tăng vọt! Nguy cơ đột quỵ."* |
| **Ghi nhận thành công**| Khích lệ, ngắn gọn | *"Tuyệt vời! Bạn đã hoàn thành 100% mục tiêu uống nước hôm nay."* | *"Dữ liệu đã được nạp thành công vào bảng SQLite."* |

### 4.2. Danh sách thuật ngữ Cấm & Thay thế (Prohibited Terms)

| Thuật ngữ cấm | Lý do cấm | Cụm từ chuẩn thay thế của Vita |
| :--- | :--- | :--- |
| *"Chẩn đoán bệnh"* | Vi phạm đạo đức nghề nghiệp, app không phải bác sĩ | *"Chỉ số tham khảo"*, *"Phân loại sức khỏe"* |
| *"Nguy hiểm chết người"*| Gây hoang mang sợ hãi cho người bệnh | *"Cần chú ý theo dõi"*, *"Nên tham vấn bác sĩ"* |
| *"Database", "Query"* | Ngôn ngữ kỹ thuật lộ ra ngoài UI | *"Nhật ký"*, *"Lịch sử ghi nhận"* |
| *"Bác sĩ ảo AI"* | Quảng cáo sai sự thật, thổi phồng tính năng | *"Gợi ý thông minh từ Vita"* |

---

## 5. QUY CHUẨN THÀNH PHẦN GIAO DIỆN ANDROID (UI COMPONENT TOKENS)

### 5.1. Thẻ hiển thị dữ liệu (CardView)
* **Bo góc (Corner Radius):** `12dp`
* **Đổ bóng (Elevation):** `2dp` (trạng thái tĩnh), `4dp` (khi nhấn giữ mở Context Menu)
* **Khoảng cách lề trong (Card Padding):** `16dp`
* **Màu nền thẻ:** `#FFFFFF` trên nền app `#F8F9FA`

### 5.2. Nút bấm tương tác (Buttons & FAB)
* **Nút chính (Filled Button):** Nền `#00897B`, chữ trắng in hoa vừa phải (`textAllCaps="false"`), bo góc `8dp`, chiều cao tối thiểu `48dp` (bảo đảm diện tích chạm ngón tay cho người lớn tuổi).
* **Nút phụ (Outlined Button):** Nền trong suốt, viền `1.5dp` màu `#00897B`, chữ `#00897B`, bo góc `8dp`.
* **Nút hành động nổi (FloatingActionButton):** Nền `#00ACC1`, icon dấu `+` màu trắng, đặt ở góc dưới cùng bên phải, cách đáy và lề phải `16dp`.

### 5.3. Khung nhập liệu (Input Fields)
* Sử dụng `TextInputLayout` với phong cách `BoxBackgroundMode="outline"`.
* Bán kính góc bo: `8dp`.
* Khi active: Viền đổi sang màu `#00897B` với độ dày `2dp`.

---

## 6. MÃ NGUỒN TÀI NGUYÊN GIAO DIỆN (ANDROID RES/VALUES CODES)

Nhóm phát triển chỉ cần sao chép các định nghĩa tài nguyên chuẩn sau vào dự án Android Studio:

### 6.1. Tệp `res/values/colors.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Brand Primary -->
    <color name="vita_teal">#00897B</color>
    <color name="vita_teal_dark">#005B4F</color>
    <color name="vita_teal_light">#4EBAAA</color>

    <!-- Brand Secondary -->
    <color name="vita_aqua">#00ACC1</color>
    <color name="vita_aqua_dark">#007C91</color>
    <color name="vita_aqua_light">#E0F7FA</color>

    <!-- Neutrals & Surfaces -->
    <color name="vita_bg">#F8F9FA</color>
    <color name="vita_surface">#FFFFFF</color>
    <color name="vita_text_primary">#1C1B1F</color>
    <color name="vita_text_secondary">#616161</color>
    <color name="vita_divider">#E0E0E0</color>

    <!-- Semantic Status -->
    <color name="vita_status_good">#2E7D32</color>
    <color name="vita_status_warning">#F57C00</color>
    <color name="vita_status_danger">#D32F2F</color>
    <color name="vita_water">#1976D2</color>
    <color name="vita_sleep">#512DA8</color>
</resources>
```

### 6.2. Tệp `res/values/dimens.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Spacing Scale -->
    <dimen name="spacing_xs">4dp</dimen>
    <dimen name="spacing_sm">8dp</dimen>
    <dimen name="spacing_md">16dp</dimen>
    <dimen name="spacing_lg">24dp</dimen>
    <dimen name="spacing_xl">32dp</dimen>

    <!-- Corner Radius -->
    <dimen name="radius_button">8dp</dimen>
    <dimen name="radius_card">12dp</dimen>
    <dimen name="radius_input">8dp</dimen>

    <!-- Typography Text Sizes -->
    <dimen name="text_display">32sp</dimen>
    <dimen name="text_h1">22sp</dimen>
    <dimen name="text_h2">18sp</dimen>
    <dimen name="text_body_lg">16sp</dimen>
    <dimen name="text_body">14sp</dimen>
    <dimen name="text_caption">12sp</dimen>
</resources>
```
