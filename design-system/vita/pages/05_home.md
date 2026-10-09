# THIẾT KẾ CHI TIẾT GIAO DIỆN: HOME FRAGMENT
## Tab 1: Tổng quan Sức khỏe, Thời tiết & AQI, Lời khuyên Bù nước

> **File:** `HomeFragment.java`  
> **Layout:** `res/layout/fragment_home.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Đóng vai trò là trang tổng quan hàng ngày của người dùng:
  1. **Lời chào thân thiện theo buổi:** *"Chào buổi sáng, [Tên người dùng]!"* hoặc *"Chào buổi chiều..."* / *"Chào buổi tối..."*.
  2. **Thẻ Tóm tắt Thể trạng & BMI:**
     - Hiển thị chỉ số BMI tính toán tự động dựa trên chiều cao trong hồ sơ và cân nặng mới nhất từ bảng `health_records`.
     - Phân loại trạng thái (Gầy, Bình thường, Thừa cân, Béo phì) theo chuẩn đã chọn (Châu Á hoặc WHO).
     - Badge màu trạng thái: Xanh lá (`#2E7D32`), Vàng cam (`#F57C00`), Đỏ (`#D32F2F`).
  3. **Thẻ Khí tượng & Chất lượng Không khí (Open-Meteo REST API):**
     - Spinner chọn thành phố lớn (TP.HCM, Bình Dương, Hà Nội, Đà Nẵng, Cần Thơ) không cần cấp quyền GPS.
     - Hiển thị: Nhiệt độ (°C), Độ ẩm (%), Chỉ số ô nhiễm không khí (US AQI).
     - Hỗ trợ lưu đệm ngoại tuyến (Offline Cache trong SharedPreferences), hiển thị nhãn *"Dữ liệu ngoại tuyến"* nếu mất mạng.
  4. **Thẻ Lời khuyên Thông minh từ Vita (Heuristic Advice Card):**
     - Tự động phát hiện điều kiện thời tiết khắc nghiệt:
       - Nếu Nhiệt độ $\ge 33^\circ C$: Gợi ý tăng thêm 300 ml nước bù khoáng.
       - Nếu AQI > 100: Gợi ý đeo khẩu trang lọc bụi và hạn chế vận động mạnh ngoài trời.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  Chào buổi sáng, Bác Tín! 👋             │  <- Heading 1 (22sp Bold)
│  Chúc bạn một ngày tràn đầy năng lượng.  │  <- Body Regular (14sp)
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ BMI CardView (Radius 12dp)
│  │ CHỈ SỐ THỂ TRẠNG (BMI)             │  │
│  │                                    │  │
│  │     22.4          [ Bình thường ]  │  │  <- 32sp Bold, Badge Xanh lá #2E7D32
│  │    kg/m²                           │  │
│  │                                    │  │
│  │ Chiều cao: 165 cm   Cân nặng: 61 kg│  │  <- Body Regular (14sp)
│  │ Chuẩn đánh giá: Châu Á (IDI & WPRO)│  │  <- Caption (12sp)
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Thời tiết & AQI CardView
│  │ THỜI TIẾT & MÔI TRƯỜNG             │  │
│  │ Thành phố: [ Bình Dương        ▼ ] │  │  <- Spinner chọn tỉnh/thành
│  │                                    │  │
│  │    ☀️ 33°C         AQI: 65         │  │  <- Nhiệt độ (28sp Bold), AQI (Vừa phải)
│  │   Nắng ráo      Không khí tốt      │  │
│  │   Độ ẩm: 68%    (Cập nhật: 08:30)  │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Lời khuyên Heuristic
│  │ 💡 GỢI Ý CHĂM SÓC HÔM NAY          │  │
│  │                                    │  │
│  │ "Hôm nay trời nắng nóng 33°C.      │  │  <- Body Large (16sp, #1C1B1F)
│  │  Vita gợi ý bạn nên bổ sung thêm   │  │
│  │  300 ml nước để thanh lọc cơ thể." │  │
│  └────────────────────────────────────┘  │
│                                          │
│  [Khoảng trống đệm đáy 80dp chống che]   │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Màu sắc quy chuẩn |
| :--- | :--- | :--- | :--- |
| **Nội dung cuộn** | `scroll_home` | `NestedScrollView`, `clipToPadding="false"`, paddingBottom `80dp` | Nền: `@color/vita_bg` |
| **Lời chào cá nhân** | `tv_home_greeting` | `TextView`, `22sp`, Bold (700), marginTop `16dp` | Màu: `@color/vita_text_primary` |
| **Thông điệp ngắn** | `tv_home_sub_greeting` | `TextView`, `14sp`, Regular (400), marginBottom `16dp` | Màu: `@color/vita_text_secondary` |
| **Thẻ BMI Card** | `cv_home_bmi` | `MaterialCardView`, cornerRadius `12dp`, elevation `2dp` | Nền: `#FFFFFF`, viền `@color/vita_divider` |
| **Số đo BMI lớn** | `tv_home_bmi_value`| `TextView`, `32sp`, Bold (700) | Màu: `@color/vita_teal` |
| **Huy hiệu phân loại** | `tv_home_bmi_badge`| `TextView`, `14sp`, Medium, padding `6dp 12dp`, bo góc `16dp` | Nền mờ, chữ theo mã Semantic status |
| **Thẻ Thời tiết** | `cv_home_weather` | `MaterialCardView`, cornerRadius `12dp`, elevation `2dp` | Nền: `#FFFFFF` |
| **Spinner Thành phố** | `sp_home_city` | `Spinner`, cao `44dp`, nền bo cong nhẹ | Chữ: `@color/vita_text_primary` |
| **Nhiệt độ hiện tại** | `tv_home_temp` | `TextView`, `28sp`, Bold (700) | Màu: `@color/vita_text_primary` |
| **Chỉ số AQI** | `tv_home_aqi` | `TextView`, `18sp`, Semi-Bold (600) | Màu sắc phụ thuộc giá trị AQI |
| **Thẻ Lời khuyên** | `cv_home_advice` | `MaterialCardView`, cornerRadius `12dp`, nền `@color/vita_aqua_light` | Nền: `#E0F7FA`, viền `#B2EBF2` |
| **Nội dung lời khuyên**| `tv_home_advice_text`| `TextView`, `15sp`, LineHeight `22sp` | Màu: `@color/vita_text_primary` |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Phản hồi ngoại tuyến (Offline State):**
   - Nếu gọi API Open-Meteo không có mạng, hiển thị dữ liệu thời tiết cũ từ SharedPreferences kèm một nhãn nhỏ màu xám nhạt: *"☁️ Ngoại tuyến"*, không bao giờ ném lỗi Crash hay hiển thị Dialog chặn màn hình.
2. **Động lực học dữ liệu BMI:**
   - Nếu người dùng chưa có bản ghi đo cân nặng nào trong bảng `health_records`, app tự động lấy `base_weight_kg` khai báo khi Đăng ký để hiển thị chỉ số ước tính ban đầu kèm nhãn *"Dựa trên cân nặng ban đầu"*.
3. **Phân cấp màu sắc AQI chuẩn y tế:**
   - AQI 0–50 (Tốt): Xanh lá cây (`#2E7D32`).
   - AQI 51–100 (Vừa phải): Vàng nhạt (`#FBC02D`).
   - AQI 101–150 (Nhạy cảm): Vàng cam (`#F57C00`).
   - AQI > 150 (Xấu/Nguy hại): Đỏ đậm (`#D32F2F`).
