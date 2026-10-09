# BẢN QUY CHUẨN THIẾT KẾ UI/UX CHỦ ĐẠO (MASTER DESIGN SYSTEM)
## Ứng dụng Di động Vita – Quản lý Sức khỏe Cá nhân và Gia đình tại Nhà (Android Native)

> **CƠ CHẾ KẾ THỪA (HIERARCHICAL RETRIEVAL):**
> - Tệp này là **Nguồn Chân lý Duy nhất (Single Source of Truth - Master)** cho toàn bộ hệ thống giao diện, tương tác và trải nghiệm người dùng của ứng dụng Vita.
> - Khi thiết kế hoặc hiện thực một màn hình cụ thể, hãy kiểm tra tệp ghi đè tương ứng trong thư mục `design-system/vita/pages/[screen-name].md`.
> - Nếu tệp màn hình có quy tắc đặc thù, quy tắc đó sẽ **ghi đè** quy tắc chung trong tệp Master này. Nếu không, bắt buộc tuân thủ 100% các tiêu chuẩn dưới đây.

---

## 1. ĐỊNH VỊ THƯƠNG HIỆU & NGUYÊN TẮC THIẾT KẾ (BRAND & CORE PRINCIPLES)

### 1.1. Thông tin Định danh Thương hiệu
- **Tên thương hiệu:** **Vita** (Xuất phát từ tiếng Latinh mang ý nghĩa *Sự sống, sức sống, nguồn năng lượng tươi trẻ*).
- **Khẩu hiệu (Slogan):** *"Sức sống mỗi ngày, an tâm tại nhà"* (English: *"Healthy Living, Right at Home"*).
- **Nền tảng mục tiêu:** Android Native (Java + XML Layouts + Material Design 3).
- **Đối tượng người dùng chính:** Người dùng gia đình, người trung niên và cao tuổi, người có bệnh lý mạn tính cần theo dõi chỉ số tại nhà.
- **Tuyên bố y tế bắt buộc (Medical Disclaimer):** Mọi chỉ số, cảnh báo và lời khuyên trên app chỉ mang tính chất tham khảo, hỗ trợ theo dõi tại nhà, không thay thế chẩn đoán chuyên môn của bác sĩ.

### 1.2. Bốn Nguyên tắc Trải nghiệm Người dùng Cốt lõi (UX Pillars)
1. **Dễ đọc – Dễ thấy – Dễ bấm (Senior & Accessibility First):**
   - Mọi nút bấm và vùng chạm tương tác phải có kích thước tối thiểu **$\ge$ 48dp × 48dp** (chuẩn Android Accessibility & Material Design).
   - Khoảng cách giữa các phần tử bấm cạnh nhau tối thiểu **8dp** để chống bấm nhầm.
   - Số đo sinh hiệu quan trọng (Huyết áp, Cân nặng, BMI, Đường huyết) hiển thị cực lớn (**Display 32sp Bold**), tương phản vượt chuẩn WCAG AAA.
2. **Ân cần – Không gây hoang mang (Calm & Ethical UX):**
   - Thông điệp cảnh báo sức khỏe luôn điềm tĩnh, chuẩn mực, hướng dẫn hành động tích cực (nghỉ ngơi, đo lại, tham vấn bác sĩ), tuyệt đối không dùng từ ngữ giật gân, đe dọa.
3. **Minh bạch – Trực quan đa kênh (Multimodal Clarity):**
   - Trạng thái sức khỏe không bao giờ chỉ dùng màu sắc đơn thuần; luôn kết hợp **Màu sắc + Nhãn chữ + Biểu tượng Vector**.
   - Hỗ trợ người dùng mù màu hoặc suy giảm thị lực nhận biết ngay trạng thái (Bình thường / Cần chú ý / Nguy cơ).
4. **Ngoại tuyến trơn tru – Không giật lag (Offline-First Polish):**
   - Lưu trữ dữ liệu tức thì vào SQLite cục bộ. Không có vòng quay loading vô tận.
   - Phản hồi thị giác xúc giác tức thì: Hiệu ứng gợn sóng (Ripple Effect) trong 80–120ms khi chạm.

---

## 2. BẢNG MÀU CHUẨN NHẬN DIỆN VITA (COLOR PALETTE TOKENS)

Toàn bộ bảng màu được đồng bộ tuyệt đối với `docs/brand-guidelines.md` và mã hóa thành tài nguyên Android `res/values/colors.xml`:

### 2.1. Màu Thương hiệu Chính & Phụ (Primary & Secondary)

| Vai trò thiết kế | Tên màu | Mã Hex | XML Token Name | Ứng dụng giao diện thực tế |
| :--- | :--- | :--- | :--- | :--- |
| **Primary** | Vita Teal | `#00897B` | `@color/vita_teal` | Toolbar nền, Nút CTA chính, Tab active BottomNav, Nút Radio đã chọn |
| **Primary Variant** | Vita Teal Dark | `#005B4F` | `@color/vita_teal_dark` | Thanh trạng thái hệ thống (StatusBar) |
| **Primary Container**| Vita Teal Light | `#4EBAAA` | `@color/vita_teal_light`| Nền chip đã chọn, đường viền nhấn nổi bật |
| **Secondary** | Sky Aqua | `#00ACC1` | `@color/vita_aqua` | Nút hành động nổi FAB (`+`), thanh tiến độ nước ProgressBar |
| **Secondary Variant**| Sky Aqua Dark | `#007C91` | `@color/vita_aqua_dark`| Trạng thái nhấn (Pressed/Ripple) của nút phụ và FAB |
| **Secondary Light** | Sky Aqua Light | `#E0F7FA` | `@color/vita_aqua_light`| Nền badge lượng nước, nền thẻ thời tiết nhẹ nhàng |

### 2.2. Màu Trung tính & Bề mặt (Neutrals & Surfaces)

| Vai trò | Mã Hex | XML Token Name | Ứng dụng | Độ tương phản chữ (Contrast Ratio) |
| :--- | :--- | :--- | :--- | :--- |
| **Background** | `#F8F9FA` | `@color/vita_bg` | Nền toàn màn hình ứng dụng (dịu mắt, chống mỏi mắt) | - |
| **Surface** | `#FFFFFF` | `@color/vita_surface` | Nền các thẻ CardView, Dialog thông báo, BottomSheet | - |
| **Text Primary** | `#1C1B1F` | `@color/vita_text_primary` | Tiêu đề, số đo chỉ số sức khỏe, nội dung chính | **13.4:1** trên Surface (Vượt chuẩn WCAG AAA) |
| **Text Secondary**| `#616161` | `@color/vita_text_secondary`| Ghi chú, ngày đo, đơn vị tính (kg, mmHg, bpm, ml) | **4.6:1** trên Surface (Đạt chuẩn WCAG AA) |
| **Border / Divider**| `#E0E0E0` | `@color/vita_divider` | Đường kẻ phân cách RecyclerView, viền thẻ Card, viền EditText | 3.1:1 |

### 2.3. Bảng màu Trạng thái & Phân hệ Sức khỏe (Semantic Status Colors)

| Phân hệ / Trạng thái | Mã Hex | XML Token Name | Nhãn hiển thị đi kèm | Ngữ cảnh sử dụng |
| :--- | :--- | :--- | :--- | :--- |
| **Tốt / Ổn định** | `#2E7D32` | `@color/vita_status_good` | **Bình thường / Tốt** | BMI chuẩn, Huyết áp chuẩn (< 120/80), Đã uống thuốc (`TAKEN`) |
| **Cần chú ý** | `#F57C00` | `@color/vita_status_warning` | **Cần chú ý** | Tiền tăng huyết áp (120-139 / 80-89), Thừa cân, AQI nhạy cảm (101-150) |
| **Cảnh báo / Nguy cơ**| `#D32F2F` | `@color/vita_status_danger`| **Nguy cơ cao** | Tăng huyết áp độ 1-2 ($\ge$ 140/90), Béo phì, Bỏ qua thuốc (`SKIPPED`) |
| **Nước uống** | `#1976D2` | `@color/vita_water` | **Nước uống** | Icon giọt nước, thanh nạp nước, nút nhanh "+250 ml" |
| **Giấc ngủ** | `#512DA8` | `@color/vita_sleep` | **Giấc ngủ** | Icon vầng trăng, biểu đồ cột thời lượng ngủ |

---

## 3. QUY CHUẨN KIỂU CHỮ (TYPOGRAPHY TOKENS)

- **Phông chữ hệ thống:** `Roboto` (Android System Font tiêu chuẩn, tương thích 100% mọi phiên bản Android từ 7.0 đến 14+).
- **Đơn vị kích thước:** Bắt buộc dùng `sp` (scale-independent pixels) để người dùng điều chỉnh kích thước chữ trên cài đặt hệ điều hành thì app tự động phóng to tương ứng.

| Tên Style Token | Kích thước (sp) | Trọng số (Font Weight) | Line Height | Màu sắc mặc định | Áp dụng trên giao diện |
| :--- | :---: | :---: | :---: | :--- | :--- |
| **Display Large** | `32sp` | Bold (700) | `40sp` | `@color/vita_teal` / Semantic | Số đo lớn trên thẻ tổng quan (65.2 kg, 120/80, 22.4 BMI) |
| **Heading 1 (H1)** | `22sp` | Bold (700) | `28sp` | `@color/vita_text_primary` | Tiêu đề thanh Toolbar, Tên người dùng |
| **Heading 2 (H2)** | `18sp` | Semi-Bold (600) | `24sp` | `@color/vita_text_primary` | Tiêu đề nhóm thẻ: "Chỉ số hôm nay", "Lịch uống thuốc" |
| **Body Large** | `16sp` | Medium (500) | `22sp` | `@color/vita_text_primary` | Tên thuốc, tên người dùng, chữ trên nút bấm CTA chính |
| **Body Regular** | `14sp` | Regular (400) | `20sp` | `@color/vita_text_secondary` | Liều dùng, thời gian đo, lời khuyên thời tiết |
| **Caption / Label**| `12sp` | Regular (400) | `16sp` | `@color/vita_text_secondary` | Nhãn ngày trục X biểu đồ, nhãn BottomNavigation |

---

## 4. HỆ THỐNG KHOẢNG CÁCH, BỐ CỤC & THẺ GIAO DIỆN (SPACING & ELEVATION)

### 4.1. Thang đo Khoảng cách 8dp (8dp Grid Rhythm)
Tuân thủ nghiêm ngặt nhịp điệu 8dp trong tệp `res/values/dimens.xml`:
- `spacing_xs` = `4dp` (Khoảng cách vi mô giữa icon và text cùng dòng)
- `spacing_sm` = `8dp` (Khoảng cách giữa các chip, giữa label và input field)
- `spacing_md` = `16dp` (Lề chuẩn màn hình padding màn hình, lề trong CardView)
- `spacing_lg` = `24dp` (Khoảng cách phân cách giữa các khối mục lớn)
- `spacing_xl` = `32dp` (Khoảng cách đầu trang, lề chân trang)

### 4.2. Thẻ hiển thị dữ liệu (Material CardView)
- **Bo góc (Corner Radius):** `12dp` (`app:cardCornerRadius="12dp"`)
- **Độ nổi bóng (Elevation):**
  - Tĩnh thông thường: `2dp` (`app:cardElevation="2dp"`)
  - Khi nhấn giữ (Context Menu active / Pressed): `4dp`
- **Màu nền thẻ:** `#FFFFFF` (`@color/vita_surface`)
- **Đường viền phụ (Tùy chọn cho thẻ phân cấp):** `1dp` solid `#E0E0E0`, `app:strokeColor="@color/vita_divider"`
- **Lề trong (Card Content Padding):** `16dp`

### 4.3. Nút bấm tương tác (Button Components)
1. **Nút chính (Filled Button - Primary CTA):**
   - Nền: `@color/vita_teal` (`#00897B`)
   - Chữ: `#FFFFFF`, kích thước `16sp`, Medium, `android:textAllCaps="false"`
   - Chiều cao tối thiểu: `48dp` (`android:minHeight="48dp"`)
   - Bo góc: `8dp` (`app:cornerRadius="8dp"`)
   - Hiệu ứng chạm: Ripple màu trắng mờ (`?attr/selectableItemBackgroundBorderless`)
2. **Nút phụ (Outlined Button):**
   - Nền: Trong suốt
   - Viền: `1.5dp` solid `@color/vita_teal`
   - Chữ: `@color/vita_teal`, `16sp`, Medium, `android:textAllCaps="false"`
   - Chiều cao tối thiểu: `48dp`, bo góc `8dp`
3. **Nút hành động nổi (Floating Action Button - FAB):**
   - Kích thước chuẩn: `56dp × 56dp`
   - Nền: `@color/vita_aqua` (`#00ACC1`)
   - Biểu tượng: Dấu `+` (Vector Drawable, màu trắng `#FFFFFF`, kích thước `24dp × 24dp`)
   - Tọa độ neo: Góc dưới bên phải màn hình, cách lề phải `16dp`, cách cạnh trên của BottomNavigationView `16dp`
   - Độ nổi bóng: `6dp` (tĩnh), `12dp` (khi nhấn)

### 4.4. Trường nhập liệu (TextInputLayout & TextInputEditText)
- **Phong cách:** Outline Box (`com.google.android.material.textfield.TextInputLayout` với `app:boxBackgroundMode="outline"`).
- **Bo góc khung nhập:** `8dp` (`app:boxCornerRadiusTopStart="8dp"`, v.v.).
- **Màu viền khi chưa focus:** `@color/vita_divider` (`#E0E0E0`, dày 1dp).
- **Màu viền khi focus:** `@color/vita_teal` (`#00897B`, dày 2dp).
- **Màu viền khi có lỗi (Error state):** `@color/vita_status_danger` (`#D32F2F`, dày 2dp kèm icon cảnh báo và thông điệp lỗi rõ ràng dưới trường nhập).

---

## 5. BIỂU TƯỢNG VECTOR & TÀI NGUYÊN HÌNH ẢNH (ICONOGRAPHY RULES)

- **Quy tắc tuyệt đối:** **KHÔNG DÙNG EMOJI** (như 💉, 💧, 😴, 🩺) làm biểu tượng chức năng hay điều hướng giao diện. Emoji hiển thị không nhất quán giữa các hãng điện thoại (Samsung, Xiaomi, Oppo, Pixel), không đổi màu theo Theme được.
- **Quy chuẩn tài nguyên:** Toàn bộ biểu tượng phải dùng định dạng **Android Vector Drawable (`.xml`)** đặt trong `res/drawable/`.
- **Họ icon khuyến nghị:** Google Material Symbols / Phosphor Icons dạng bo cong nhẹ nhàng (Rounded), độ dày nét `2dp` nhất quán.

### Danh mục Biểu tượng Vector Chuẩn hóa:
1. `ic_nav_home.xml`: Icon ngôi nhà (Tab Trang chủ)
2. `ic_nav_health.xml`: Icon nhịp tim trên trang sổ đo (Tab Chỉ số)
3. `ic_nav_reminder.xml`: Icon viên thuốc & đồng hồ (Tab Nhắc nhở)
4. `ic_nav_stats.xml`: Icon biểu đồ cột & đường xu hướng (Tab Thống kê)
5. `ic_water_drop.xml`: Icon giọt nước (Nạp nước, mục tiêu bù nước)
6. `ic_sleep_moon.xml`: Icon mặt trăng lưỡi liềm (Giấc ngủ)
7. `ic_heart_pulse.xml`: Icon đo nhịp tim & huyết áp
8. `ic_scale_weight.xml`: Icon cái cân sức khỏe
9. `ic_weather_sun.xml` / `ic_weather_cloud.xml`: Icon thời tiết Open-Meteo
10. `ic_edit.xml`, `ic_delete.xml`: Icon cho Context Menu và Action Button

---

## 6. KHUNG TƯƠNG TÁC, VI CHUYỂN ĐỘNG & TIẾP CẬN (INTERACTION & ACCESSIBILITY)

### 6.1. Phản hồi Xúc giác và Thị giác (Touch & Haptic Feedback)
- Mọi nút bấm, thẻ danh sách khi chạm phải kích hoạt ngay Ripple Drawable trong vòng **100ms**.
- Thao tác nhấn giữ (Long Click) để mở **Context Menu (Sửa / Xóa)** trên danh sách RecyclerView phải có rung phản hồi xúc giác nhẹ (`android:hapticFeedbackEnabled="true"`).

### 6.2. Vùng an toàn & Thanh điều hướng (Safe Areas & System Chrome)
- Thanh trạng thái phía trên (Status Bar) phủ màu `@color/vita_teal_dark` (`#005B4F`) với icon màu sáng.
- Thanh điều hướng đáy (BottomNavigationView) cao `56dp`, nền trắng `#FFFFFF`, có đường viền chia nhẹ `1dp` `#E0E0E0` ở đỉnh.
- Nội dung danh sách cuộn (`RecyclerView`, `NestedScrollView`) bắt buộc phải thêm `android:clipToPadding="false"` và `android:paddingBottom="80dp"` để các phần tử cuối danh sách không bị che khuất bởi BottomNavigationView hoặc FAB.

### 6.3. Tiêu chuẩn Tiếp cận Không rào cản (WCAG & Accessibility Checklist)
- Mọi `ImageView` hoặc `ImageButton` mang tính điều hướng bắt buộc phải có thuộc tính `android:contentDescription="..."` bằng tiếng Việt rõ nghĩa (Ví dụ: `android:contentDescription="Mở trang cá nhân"`, `android:contentDescription="Thêm chỉ số mới"`).
- Các icon chỉ mang tính trang trí bên cạnh chữ phải gắn `android:importantForAccessibility="no"`.
- Hỗ trợ đầy đủ chế độ "Chữ lớn" (Font Scaling): Giao diện không bị cắt cụt chữ nhờ thiết lập `android:ellipsize="end"` và bố cục `wrap_content` linh hoạt.

---

## 7. KHUNG GIỌNG ĐIỆU & THÔNG ĐIỆP (VOICE & TONE FRAMEWORK)

Ứng dụng Vita giao tiếp với người dùng theo tiêu chí: **Ân cần – Minh bạch – Khích lệ – Chuẩn mực**.

### 7.1. Bảng chuyển đổi ngôn từ (UX Copywriting Matrix)

| Ngữ cảnh | Câu cần tránh (Negative / Panic) | Câu chuẩn Vita (Caring & Constructive) |
| :--- | :--- | :--- |
| **Huyết áp cao** | ❌ *"Huyết áp tăng vọt! Nguy cơ tai biến đột quỵ!"* | ✅ *"Chỉ số huyết áp hôm nay ở mức cần chú ý (142/92 mmHg). Bạn hãy nghỉ ngơi tĩnh dưỡng và kiểm tra lại sau 30 phút nhé."* |
| **Nhắc uống thuốc**| ❌ *"Chuông báo tử! Phải uống thuốc ngay."* | ✅ *"Đã đến cữ uống thuốc: [Amlodipine] - Liều: [1 viên sau ăn]. Bạn đã uống chưa?"* |
| **Nắng nóng gay gắt**| ❌ *"Nắng nóng nguy hiểm, cấm ra đường!"* | ✅ *"Nhiệt độ hôm nay lên đến 35°C. Vita gợi ý bạn nên bổ sung thêm 300 ml nước để thanh lọc cơ thể nhé."* |
| **Hoàn thành nước**| ❌ *"Ghi dữ liệu vào database thành công."* | ✅ *"Tuyệt vời! Bạn đã đạt 100% mục tiêu uống nước của ngày hôm nay."* |

### 7.2. Danh mục Thuật ngữ Cấm (Prohibited UX Terms)
- Cấm dùng: *"Bác sĩ AI"*, *"Chẩn đoán bệnh"*, *"Kê đơn thuốc"*, *"Chữa dứt điểm"*.
- Thay bằng: *"Chỉ số tham khảo"*, *"Lời khuyên từ Vita"*, *"Lịch uống thuốc cá nhân"*, *"Theo dõi sức khỏe"*.

---

## 8. DANH MỤC KIỂM TRA TRƯỚC KHI BÀN GIAO (PRE-DELIVERY CHECKLIST)

- [ ] **Màu sắc & Nhận diện:** Đạt 100% mã màu Vita Teal (`#00897B`), Sky Aqua (`#00ACC1`), Background (`#F8F9FA`).
- [ ] **Accessibility (Độ tương phản):** Chữ đen trên nền trắng đạt $\ge$ 13:1; Nút chính chữ trắng trên nền xanh Teal đạt $\ge$ 4.5:1.
- [ ] **Vùng chạm ngón tay (Touch Target):** Mọi nút bấm, item danh sách $\ge$ 48dp × 48dp.
- [ ] **Không Emoji:** 100% biểu tượng là Vector Drawable XML.
- [ ] **Bố cục cuộn:** Nội dung danh sách không bị che bởi BottomNavigationView hay FAB nhờ `clipToPadding="false"` và padding đáy 80dp.
- [ ] **Phạm vi kỹ thuật:** Đảm bảo đúng 14 màn hình và 10 chức năng cốt lõi của đề tài Vita, không thêm tính năng thừa.
