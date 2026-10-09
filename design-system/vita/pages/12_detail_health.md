# THIẾT KẾ CHI TIẾT GIAO DIỆN: DETAIL HEALTH ACTIVITY
## Màn hình Chi tiết Bản ghi Sinh hiệu & Phân tích Đánh giá Sức khỏe

> **File:** `DetailHealthActivity.java`  
> **Layout:** `res/layout/activity_detail_health.xml`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & HÀNH VI MÀN HÌNH (OBJECTIVE & BEHAVIOR)
- Hiển thị đầy đủ, chi tiết và trực quan toàn bộ các thông số của một lần đo trong quá khứ khi người dùng bấm vào một dòng trong danh sách `HealthFragment`.
- **Phân loại và Đánh giá chi tiết (Health Interpretation):**
  - Đánh giá huyết áp theo chuẩn Hội Tim mạch học Việt Nam: Bình thường tối ưu (< 120/80), Bình thường (120-129/80-84), Tiền tăng huyết áp (130-139/85-89), Tăng huyết áp độ 1 (140-159/90-99), Tăng huyết áp độ 2 ($\ge$ 160/100).
  - Tự động hiển thị huy hiệu trạng thái Semantic Color tương ứng.
- **Tác vụ điều hướng tiếp theo:**
  - Nút **"Chỉnh sửa bản ghi này"**: Chuyển sang `AddHealthActivity` kèm `record_id` để cập nhật lại số liệu.
  - Nút **"Xóa bản ghi"** (hoặc icon Thùng rác trên Toolbar): Mở Dialog xác nhận xóa bản ghi.

---

## 2. BỐ CỤC THỊ GIÁC & WIREFRAME MÔ TẢ (VISUAL STRUCTURE)

```text
┌──────────────────────────────────────────┐
│  ← (Quay lại)     Chi tiết lần đo      🗑 │  <- Toolbar trắng, icon Xóa
├──────────────────────────────────────────┤
│                                          │
│  📅 Ngày đo: 09/10/2026                  │  <- Heading 2 (18sp Bold)
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Huyết áp (12dp)
│  │ ❤️ HUYẾT ÁP & NHỊP TIM              │  │
│  │                                    │  │
│  │      125 / 82       [ Bình thường ]│  │  <- 32sp Bold, Badge Xanh lá #2E7D32
│  │        mmHg                        │  │
│  │                                    │  │
│  │  • Nhịp tim: 76 bpm (Bình thường)  │  │
│  │  • Đánh giá: Huyết áp ổn định      │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Thể trạng & Đường huyết
│  │ ⚖️ CÂN NẶNG & ĐƯỜNG HUYẾT          │  │
│  │                                    │  │
│  │  • Cân nặng: 61.5 kg               │  │  <- 16sp Medium
│  │  • Đường huyết: 95.0 mg/dL (Lúc đói)│  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │  <- Thẻ Ghi chú
│  │ 📝 GHI CHÚ ĐÍNH KÈM                │  │
│  │ "Đo sau bữa sáng 30 phút, cơ thể   │  │  <- Body Regular (14sp)
│  │  cảm thấy khỏe khoắn, không mệt."  │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │        CHỈNH SỬA BẢN GHI NÀY       │  │  <- Outlined Button #00897B, 48dp
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ THIẾT KẾ WIDGET (UI COMPONENT SPECS)

| Thành phần (Widget) | ID XML | Thuộc tính Style / Kích thước | Ghi chú & Giá trị |
| :--- | :--- | :--- | :--- |
| **Thanh Toolbar** | `toolbar_detail_health`| `MaterialToolbar`, cao `56dp`, nền `#FFFFFF` | Nút Back Arrow & Menu Delete |
| **Nội dung cuộn** | `scroll_detail_health` | `ScrollView`, `fillViewport="true"`, padding `20dp` | Nền: `@color/vita_bg` |
| **Tiêu đề ngày đo** | `tv_detail_date` | `TextView`, `18sp`, Bold (700), marginBottom `16dp` | Màu: `@color/vita_text_primary` |
| **Thẻ Huyết áp** | `cv_detail_bp` | `MaterialCardView`, cornerRadius `12dp` | Nền: `#FFFFFF`, elevation `2dp` |
| **Số đo Huyết áp lớn** | `tv_detail_bp_value` | `TextView`, `32sp`, Bold (700) | Màu: `@color/vita_teal` |
| **Huy hiệu đánh giá** | `tv_detail_bp_status` | `TextView`, `14sp`, Medium, bo góc `16dp` | Màu nền mờ & chữ Semantic |
| **Thẻ Cân nặng & Đường**| `cv_detail_body` | `MaterialCardView`, cornerRadius `12dp` | Nền: `#FFFFFF` |
| **Thẻ Ghi chú** | `cv_detail_note` | `MaterialCardView`, cornerRadius `12dp` | Nền: `#FFFFFF` |
| **Nút Sửa bản ghi** | `btn_detail_edit` | `MaterialButton` Outlined, cao `48dp` | Viền & Chữ: `@color/vita_teal` |

---

## 4. CHI TIẾT TƯƠNG TÁC & UX GUIDELINES (UI-UX-PRO-MAX)
1. **Trình bày dữ liệu khuyết thiếu (Missing Data Representation):**
   - Với các chỉ số người dùng bỏ trống (NULL trong CSDL), hiển thị dòng chữ xám: *"-- (Không đo trong lần này)"* để giữ bố cục trang cân đối, không làm người dùng lo lắng.
2. **Ngôn từ tư vấn an tâm:**
   - Dưới phần đánh giá có ghi chú nhỏ: *"Lời khuyên tham khảo từ Vita. Nếu bạn cảm thấy chóng mặt, đau đầu, hãy tham vấn ý kiến bác sĩ gia đình."*.
