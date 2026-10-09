# THIẾT KẾ CHI TIẾT TƯƠNG TÁC: HỆ THỐNG THÔNG BÁO NHẮC THUỐC
## Giao diện Notification Hệ thống với Nút Xác nhận Tương tác Trực tiếp ("Đã uống" / "Bỏ qua")

> **Thành phần:** `MedicineAlarmReceiver.java`, `NotificationActionReceiver.java`, `NotificationHelper.java`  
> **Channel ID:** `vita_medicine_channel`  
> **Kế thừa:** `design-system/vita/MASTER.md`

---

## 1. MỤC TIÊU & TẦM QUAN TRỌNG KỸ THUẬT (TECHNICAL HIGHLIGHT #1)
- Là 1 trong 3 điểm nhấn kỹ thuật quan trọng nhất trong đề tài Vita được báo cáo trước Hội đồng chấm thi:
  - Cho phép người dùng xác nhận tình trạng tuân thủ uống thuốc **ngay trên thanh thông báo của điện thoại** mà không cần mở ứng dụng.
  - Phản hồi tức thì: Tự động ghi bản ghi vào bảng `medicine_logs` với trạng thái `TAKEN` (Đã uống) hoặc `SKIPPED` (Bỏ qua), lưu thời điểm `logged_at` chính xác.
  - Sau khi người dùng bấm nút, tự động đóng thông báo (`cancel`) và đặt lại Alarm cho cữ uống của ngày kế tiếp.

---

## 2. BỐ CỤC THỊ GIÁC NOTIFICATION TRÊN ANDROID (SYSTEM UI LAYOUT)

```text
┌────────────────────────────────────────────────────────────┐
│  🟢 VITA • Vừa xong                                         │
│                                                            │
│  ⏰ ĐÃ ĐẾN GIỜ UỐNG THUỐC: Panadol Extra                   │  <- Notification Title (Bold)
│  Liều dùng: 1 viên sau khi ăn sáng. Hãy uống thuốc đúng giờ!│  <- Notification Text
│                                                            │
│  ┌──────────────────────────┐  ┌────────────────────────┐  │
│  │   ✓ ĐÃ UỐNG              │  │   ✕ BỎ QUA             │  │  <- 2 Action Buttons
│  │   (Màu Xanh #2E7D32)     │  │   (Màu Đỏ #D32F2F)     │  │
│  └──────────────────────────┘  └────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

---

## 3. THÔNG SỐ CẤU HÌNH THÔNG BÁO (NOTIFICATION SPECIFICATIONS)

| Thuộc tính | Giá trị cấu hình | Ý nghĩa & Mục đích kỹ thuật |
| :--- | :--- | :--- |
| **NotificationChannel ID** | `vita_medicine_channel` | Kênh thông báo riêng biệt trên Android 8.0+ (API 26+) |
| **Channel Name** | *"Nhắc nhở uống thuốc Vita"* | Hiển thị trong Cài đặt thông báo của thiết bị |
| **Channel Importance** | `NotificationManager.IMPORTANCE_HIGH` | Đảm bảo thông báo nổi trên đầu màn hình (Heads-up Notification) |
| **Small Icon** | `@drawable/ic_nav_reminder` | Biểu tượng viên thuốc hiển thị trên thanh trạng thái |
| **Notification Color** | `@color/vita_teal` (`#00897B`) | Màu sắc nhận diện thương hiệu cho tiêu đề và nút |
| **Sound & Vibration** | Âm báo mặc định + Rung nhịp đôi (`{0, 250, 100, 250}`) | Đánh thức sự chú ý của người dùng, đặc biệt là người lớn tuổi |
| **AutoCancel** | `false` | Không tự mất khi chạm bên ngoài; chỉ mất khi người dùng bấm nút hoặc quẹt bỏ |
| **Priority** | `NotificationCompat.PRIORITY_HIGH` | Tương thích ngược cho các máy Android 7.0 (API 24) |

---

## 4. CHI TIẾT 2 HÀNH ĐỘNG TƯƠNG TÁC (ACTION BUTTONS UX)

### 4.1. Nút 1: "ĐÃ UỐNG" (Action: `ACTION_MEDICINE_TAKEN`)
- **Nhãn hiển thị:** `✓ Đã uống`
- **Icon đi kèm:** `ic_check_circle.xml`
- **Xử lý ngầm qua `NotificationActionReceiver`:**
  1. Ghi 1 bản ghi vào SQLite: `medicine_logs(medicine_id, date, status='TAKEN', logged_at=now)`.
  2. Hủy thông báo khỏi thanh trạng thái: `NotificationManagerCompat.cancel(notificationId)`.
  3. Tính toán mốc thời gian tiếp theo và đặt Alarm cho ngày kế tiếp qua `AlarmManager`.
  4. Bắn Toast ngắn: *"Vita ghi nhận bạn đã uống [Tên thuốc]. Chúc bạn mau khỏe!"*.

### 4.2. Nút 2: "BỎ QUA" (Action: `ACTION_MEDICINE_SKIPPED`)
- **Nhãn hiển thị:** `✕ Bỏ qua`
- **Icon đi kèm:** `ic_cancel.xml`
- **Xử lý ngầm qua `NotificationActionReceiver`:**
  1. Ghi 1 bản ghi vào SQLite: `medicine_logs(medicine_id, date, status='SKIPPED', logged_at=now)`.
  2. Hủy thông báo: `NotificationManagerCompat.cancel(notificationId)`.
  3. Lập lịch cho cữ uống ngày tiếp theo.
  4. Thông điệp hiển thị nhẹ nhàng, không phán xét: *"Đã ghi nhận bỏ qua cữ thuốc này. Hãy chú ý giữ gìn sức khỏe nhé."*.

---

## 5. PHỤC HỒI BÁO THỨC SAU KHI KHỞI ĐỘNG LẠI MÁY (`BOOT_COMPLETED`)
- Đăng ký `BootReceiver` với cờ `android.permission.RECEIVE_BOOT_COMPLETED`.
- Khi điện thoại tắt nguồn bật lại, receiver tự động quét toàn bộ thuốc trong `MedicineDao` có `is_active = 1` để tái lập lịch báo thức, đảm bảo không bao giờ bị mất giờ uống thuốc của người dùng.
