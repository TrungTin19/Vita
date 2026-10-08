# ĐẶC TẢ THIẾT KẾ DỰ ÁN ỨNG DỤNG DI ĐỘNG "VITA"
## Hệ thống Quản lý Sức khỏe Cá nhân và Gia đình tại Nhà (Android Native Java)

- **Học phần:** Phát triển ứng dụng di động (LING189) & Thực hành Phát triển ứng dụng di động (LING301)
- **Đơn vị đào tạo:** Viện Công nghệ số / Khoa Kỹ thuật – Công nghệ, Trường Đại học Thủ Dầu Một
- **Giảng viên hướng dẫn:** ThS. Nguyễn Kim Duy
- **Quy mô thực hiện:** Nhóm 2 sinh viên (Thành viên 1: Core/Leader, Thành viên 2: UI/Reporter)
- **Chuẩn mẫu báo cáo:** Mẫu báo cáo đồ án học phần đặc thù (Mẫu ver2: Chương 1 Mô tả bài toán/CSDL, Chương 2 Tổng quan lý thuyết Widget, Chương 3 Hiện thực chức năng & Demo)
- **Ngày lập:** 09/10/2026
- **Trạng thái:** Bản thiết kế kỹ thuật chính thức đã qua phê duyệt (Approved Design Spec)

---

## 1. TỔNG QUAN VÀ ĐỊNH VỊ THƯƠNG HIỆU (BRAND IDENTITY)

### 1.1. Định vị và Mục tiêu
* **Tên ứng dụng:** **Vita** (Xuất phát từ tiếng Latinh mang ý nghĩa *Sự sống, sức sống, cuộc sống tươi vui*).
* **Khẩu hiệu (Slogan):** *"Sức sống mỗi ngày, an tâm tại nhà"* (English: *"Healthy Living, Right at Home"*).
* **Mục tiêu cốt lõi:** Cung cấp giải pháp ghi nhận, cảnh báo và theo dõi các chỉ số sức khỏe quan trọng (Cân nặng, Huyết áp, Nhịp tim, Đường huyết, Giấc ngủ, Nước uống, Lịch uống thuốc) theo hướng **Offline-First**, tôn trọng tuyệt đối quyền riêng tư dữ liệu cá nhân, giao diện dễ tiếp cận cho người lớn tuổi.
* **Tuyên bố miễn trừ trách nhiệm y tế (Medical Disclaimer):** Mọi cảnh báo, gợi ý và chỉ số trên ứng dụng chỉ mang tính chất tham khảo, không có giá trị chẩn đoán y khoa hay thay thế tư vấn của bác sĩ chuyên khoa.

### 1.2. Tính cách thương hiệu (Brand Voice & Tone)
* **Tận tâm & Ân cần (Caring):** Câu từ gần gũi, sử dụng tiếng Việt trong sáng, không dùng thuật ngữ chuyên môn gây khó hiểu.
* **Minh bạch & Trực quan (Transparent):** Các trạng thái sức khỏe luôn đi kèm phân loại rõ ràng (Bình thường / Chú ý / Nguy hiểm) với màu sắc nhận diện trực quan.
* **Động viên nhẹ nhàng (Encouraging):** Nhắc nhở uống nước và uống thuốc mang giọng điệu đồng hành khích lệ.

### 1.3. Bảng màu chuẩn nhận diện (Brand Color Palette - Material 3)

| Nhóm màu | Tên màu | Mã Hex | Mục đích sử dụng |
| :--- | :--- | :--- | :--- |
| **Primary** | **Vita Teal** | `#00897B` | Màu chủ đạo: Toolbar, Button chính, Tab đang chọn trên BottomNav. |
| **Secondary** | **Sky Aqua** | `#00ACC1` | Màu phụ: FAB, Chip bộ lọc, thanh tiến độ nạp nước ProgressBar. |
| **Background** | **Light Surface** | `#F8F9FA` | Nền toàn ứng dụng dịu mắt; CardView màu trắng tinh `#FFFFFF` bo góc 12dp. |
| **Text Primary** | **Dark Slate** | `#1C1B1F` | Chữ chính trên nền sáng, đạt chuẩn tương phản cao WCAG AA. |
| **Text Secondary**| **Cool Gray** | `#616161` | Chữ phụ, ghi chú, đơn vị đo (kg, mmHg, bpm). |
| **Status: Good** | **Forest Green** | `#2E7D32` | BMI chuẩn, huyết áp bình thường, trạng thái "Đã uống thuốc". |
| **Status: Warn** | **Amber Orange** | `#F57C00` | Tiền tăng huyết áp, thừa cân, AQI mức nhạy cảm (101 - 150). |
| **Status: Alert**| **Crimson Red** | `#D32F2F` | Huyết áp cao, đường huyết bất thường, trạng thái "Bỏ qua thuốc". |
| **Feature: Water**| **Ocean Blue** | `#1976D2` | Chỉ số nước, icon giọt nước. |
| **Feature: Sleep**| **Deep Indigo** | `#512DA8` | Biểu đồ giấc ngủ, icon mặt trăng. |

### 1.4. Kiểu chữ (Typography)
* Sử dụng font mặc định hệ thống Android (`Roboto`) để đảm bảo hiệu năng tải và tính tương thích trên mọi phiên bản Android.
* Các số đo chỉ số sức khỏe hiển thị với cỡ chữ lớn: **28sp - 32sp, In đậm (Bold)** để người lớn tuổi đọc lướt nhanh chóng.

---

## 2. KIẾN TRÚC KỸ THUẬT (PRAGMATIC MVC ARCHITECTURE)

### 2.1. Ngăn xếp công nghệ (Tech Stack)
* **Ngôn ngữ:** Java (JDK 17).
* **Môi trường phát triển:** Android Studio (Bản ổn định).
* **SDK:** `minSdkVersion: 24` (Android 7.0 - phủ sóng > 95% thiết bị), `targetSdkVersion: 34` (Android 14).
* **Lưu trữ cục bộ:** `SQLite` thuần qua `SQLiteOpenHelper`, `SharedPreferences` (lưu phiên đăng nhập và cache thời tiết).
* **Xử lý nền & Đa luồng:** `AlarmManager` (hẹn giờ chính xác), `BroadcastReceiver`, `ExecutorService` (worker thread), `Handler` (Looper.getMainLooper()).
* **Giao tiếp mạng:** `HttpURLConnection`, `JSONObject` (gọi Open-Meteo REST API).
* **Thư viện bên thứ 3 duy nhất:** `MPAndroidChart` (v3.1.0) phục vụ vẽ đồ thị xu hướng.
* **Bảo mật mật khẩu:** Băm `SHA-256` kết hợp chuỗi muối ngẫu nhiên (`salt`).

### 2.2. Cấu trúc gói mã nguồn (Package Structure)
Cấu trúc tổ chức dưới package `vn.edu.tdmu.vita`, tách rời các DAO để nhóm 2 người phát triển song song không bị xung đột Git:

```text
vn.edu.tdmu.vita/
├── activities/
│   ├── SplashActivity.java          # Kiểm tra phiên SharedPreferences -> Login hoặc Main
│   ├── LoginActivity.java           # Đăng nhập bằng username + password băm
│   ├── RegisterActivity.java        # Đăng ký tài khoản, sinh salt & hash
│   ├── MainActivity.java            # Host BottomNavigationView (4 Fragments) + Toolbar Menu
│   ├── ProfileActivity.java         # Xem thông tin người dùng, chuẩn BMI đang chọn
│   ├── EditProfileActivity.java     # Chỉnh sửa thông tin cá nhân, đăng xuất
│   ├── AddHealthActivity.java       # Form nhập/sửa chỉ số sức khỏe
│   ├── DetailHealthActivity.java    # Xem chi tiết một bản ghi đo sức khỏe
│   ├── AddMedicineActivity.java     # Form thêm/sửa thông tin thuốc và lịch uống
│   └── AddSleepActivity.java        # Form nhập giờ ngủ/giờ thức, tự tính tổng giờ
├── fragments/
│   ├── HomeFragment.java            # Tổng quan: BMI hiện tại, thời tiết & AQI, lời khuyên
│   ├── HealthFragment.java          # Lịch sử chỉ số theo ngày giảm dần (RecyclerView)
│   ├── ReminderFragment.java        # Danh sách thuốc (RecyclerView) + Tiến độ nước uống
│   └── StatsFragment.java           # Biểu đồ xu hướng MPAndroidChart + Lịch sử giấc ngủ
├── adapters/
│   ├── HealthRecordAdapter.java     # Adapter RecyclerView hiển thị chỉ số, gán Context Menu
│   ├── MedicineAdapter.java         # Adapter hiển thị thuốc kèm Switch bật/tắt nhắc
│   └── SleepRecordAdapter.java      # Adapter danh sách giấc ngủ 7 ngày gần nhất
├── database/
│   ├── DatabaseHelper.java          # Khởi tạo 6 bảng SQLite, bật Foreign Key, tạo Index
│   ├── UserDao.java                 # Quản lý tài khoản và hồ sơ (Dev 1)
│   ├── HealthDao.java               # Quản lý bản ghi chỉ số sức khỏe (Dev 2)
│   ├── MedicineDao.java             # Quản lý thuốc và lịch sử uống thuốc (Dev 1)
│   ├── WaterDao.java                # Quản lý nhật ký uống nước (Dev 1)
│   └── SleepDao.java                # Quản lý nhật ký giấc ngủ (Dev 2)
├── models/
│   ├── User.java, HealthRecord.java, Medicine.java
│   ├── MedicineLog.java, WaterLog.java, SleepLog.java, WeatherData.java
├── network/
│   └── WeatherClient.java           # HttpURLConnection + ExecutorService lấy thời tiết & AQI
├── receivers/
│   ├── MedicineAlarmReceiver.java   # Tiếp nhận báo thức -> Bắn Notification có nút bấm
│   ├── NotificationActionReceiver.java # Tiếp nhận click "Đã uống" / "Bỏ qua" -> Lưu DB
│   └── BootReceiver.java            # Khởi động lại máy -> Đặt lại toàn bộ báo thức
└── utils/
    ├── SessionManager.java          # Đọc/ghi user_id vào SharedPreferences
    ├── BMICalculator.java           # Tính BMI và phân loại theo ngưỡng WHO/Châu Á
    ├── NotificationHelper.java      # Quản lý NotificationChannel (Android 8.0+)
    └── DateTimeUtils.java           # Chuyển đổi định dạng ngày (yyyy-MM-dd) và giờ (HH:mm)
```

---

## 3. THIẾT KẾ CƠ SỞ DỮ LIỆU SQLITE (6 BẢNG)

Tất cả bảng đều nằm trong tệp SQLite nội bộ `vita_health.db`. Bật ràng buộc khóa ngoại bằng câu lệnh `PRAGMA foreign_keys = ON;`.

### 3.1. Bảng `users` (Tài khoản và hồ sơ người dùng)
* `id`: `INTEGER PRIMARY KEY AUTOINCREMENT`
* `username`: `TEXT UNIQUE NOT NULL`
* `password_hash`: `TEXT NOT NULL` (Chuỗi băm SHA-256 từ `salt + password`)
* `salt`: `TEXT NOT NULL` (Chuỗi ngẫu nhiên 16 bytes tạo khi đăng ký)
* `fullname`: `TEXT`
* `birth_year`: `INTEGER` (Dùng năm sinh để tính tuổi động)
* `gender`: `TEXT` ('Nam', 'Nữ', 'Khác')
* `height_cm`: `REAL`
* `base_weight_kg`: `REAL`
* `water_goal_ml`: `INTEGER DEFAULT 2000`
* `bmi_standard`: `TEXT DEFAULT 'ASIAN'` ('ASIAN' hoặc 'INTERNATIONAL')

### 3.2. Bảng `health_records` (Chỉ số sức khỏe định kỳ)
* `id`: `INTEGER PRIMARY KEY AUTOINCREMENT`
* `user_id`: `INTEGER NOT NULL, FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE`
* `date`: `TEXT NOT NULL` (Định dạng `yyyy-MM-dd` để sắp xếp và lọc chính xác)
* `weight_kg`: `REAL` (Cho phép NULL nếu lần đo không cân)
* `systolic`: `INTEGER` (Huyết áp tâm thu mmHg, cho phép NULL)
* `diastolic`: `INTEGER` (Huyết áp tâm trương mmHg, cho phép NULL)
* `heart_rate`: `INTEGER` (Nhịp tim bpm, cho phép NULL)
* `blood_sugar`: `REAL` (Đường huyết mg/dL, cho phép NULL)
* `note`: `TEXT`

### 3.3. Bảng `medicines` (Danh mục thuốc & lịch nhắc)
* `id`: `INTEGER PRIMARY KEY AUTOINCREMENT` (Đồng thời dùng làm `requestCode` của PendingIntent)
* `user_id`: `INTEGER NOT NULL, FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE`
* `name`: `TEXT NOT NULL`
* `dosage`: `TEXT` (Ví dụ: "1 viên sau ăn", "2 gói")
* `time`: `TEXT NOT NULL` (Giờ nhắc, định dạng `HH:mm`)
* `days_mask`: `INTEGER DEFAULT 127` (Mặt nạ 7 bit cho các ngày trong tuần: Thứ Hai là Bit 0, Chủ Nhật là Bit 6. Giá trị 127 = 0b1111111 nghĩa là nhắc cả 7 ngày)
* `is_active`: `INTEGER DEFAULT 1` (1: Đang bật nhắc, 0: Tạm tắt)

### 3.4. Bảng `medicine_logs` (Nhật ký xác nhận uống thuốc)
* `id`: `INTEGER PRIMARY KEY AUTOINCREMENT`
* `medicine_id`: `INTEGER NOT NULL, FOREIGN KEY REFERENCES medicines(id) ON DELETE CASCADE`
* `date`: `TEXT NOT NULL` (`yyyy-MM-dd`)
* `status`: `TEXT NOT NULL` ('TAKEN' - Đã uống, hoặc 'SKIPPED' - Bỏ qua)
* `logged_at`: `TEXT NOT NULL` (`yyyy-MM-dd HH:mm`)

### 3.5. Bảng `water_logs` (Nhật ký uống nước trong ngày)
* `id`: `INTEGER PRIMARY KEY AUTOINCREMENT`
* `user_id`: `INTEGER NOT NULL, FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE`
* `date`: `TEXT NOT NULL` (`yyyy-MM-dd`)
* `amount_ml`: `INTEGER NOT NULL` (Mỗi lần bấm "+250 ml" ghi 1 dòng)
* `logged_at`: `TEXT NOT NULL` (`yyyy-MM-dd HH:mm`)

### 3.6. Bảng `sleep_logs` (Nhật ký theo dõi giấc ngủ)
* `id`: `INTEGER PRIMARY KEY AUTOINCREMENT`
* `user_id`: `INTEGER NOT NULL, FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE`
* `date`: `TEXT NOT NULL` (Ngày thức dậy `yyyy-MM-dd`)
* `sleep_time`: `TEXT NOT NULL` (`HH:mm` - Giờ bắt đầu ngủ)
* `wake_time`: `TEXT NOT NULL` (`HH:mm` - Giờ thức dậy)
* `duration_h`: `REAL NOT NULL` (Tổng thời lượng ngủ tính theo giờ, tự động cộng 24h nếu ngủ qua nửa đêm)

### 3.7. Chỉ mục tối ưu hóa (Index Strategy)
Thực hiện tạo Index để tăng tốc độ truy vấn biểu đồ và lọc danh sách:
```sql
CREATE INDEX idx_health_user_date ON health_records(user_id, date);
CREATE INDEX idx_water_user_date ON water_logs(user_id, date);
CREATE INDEX idx_sleep_user_date ON sleep_logs(user_id, date);
```

---

## 4. CHI TIẾT 14 MÀN HÌNH VÀ LUỒNG ĐIỀU HƯỚNG (NAVIGATION FLOW)

### 4.1. Danh mục 14 màn hình
1. **SplashActivity:** Màn hình khởi động, kiểm tra SessionManager. Có phiên -> vào `MainActivity`; chưa có phiên -> vào `LoginActivity`.
2. **LoginActivity:** Đăng nhập hệ thống, kiểm tra tính hợp lệ dữ liệu rỗng. Có nút chuyển sang đăng ký.
3. **RegisterActivity:** Nhập tên đăng nhập, mật khẩu, họ tên, năm sinh, giới tính, chiều cao, cân nặng ban đầu. Đăng ký thành công -> vào thẳng `MainActivity`.
4. **MainActivity:** Màn hình chính lưu giữ `BottomNavigationView` với 4 tabs và Toolbar chứa nút mở Hồ sơ cá nhân.
5. **HomeFragment (Tab 1):** Tổng quan chỉ số BMI hiện tại, Spinner chọn thành phố, thẻ thông tin thời tiết nhiệt độ/độ ẩm/AQI, lời khuyên sức khỏe.
6. **HealthFragment (Tab 2):** Danh sách lịch sử đo theo thứ tự ngày giảm dần. Hỗ trợ lọc theo ngày. Nhấn giữ mở Context Menu (Sửa / Xóa). Nút FAB mở `AddHealthActivity`.
7. **ReminderFragment (Tab 3):** Quản lý thuốc uống và tiến độ nước. Thuốc có Switch bật/tắt. Phần nước có ProgressBar tiến độ và nút "+250 ml".
8. **StatsFragment (Tab 4):** Biểu đồ LineChart (Cân nặng, Huyết áp, Nhịp tim), BarChart (Nước, Giấc ngủ) có toggle 7 ngày / 30 ngày. Danh sách giấc ngủ gần đây.
9. **ProfileActivity:** Xem chi tiết hồ sơ cá nhân, đổi tiêu chuẩn BMI (WHO / Châu Á). Nút điều hướng sang `EditProfileActivity`.
10. **EditProfileActivity:** Form chỉnh sửa họ tên, chiều cao, cân nặng cơ sở, mục tiêu nước. Chứa nút **Đăng xuất**.
11. **AddHealthActivity:** Nhập bản ghi sức khỏe mới hoặc chỉnh sửa bản ghi đã có. Cho phép bỏ trống trường không đo.
12. **DetailHealthActivity:** Xem toàn bộ thông tin chi tiết của một bản ghi đo sức khỏe trong quá khứ.
13. **AddMedicineActivity:** Form thêm mới hoặc cập nhật thuốc: Tên thuốc, liều lượng, chọn giờ qua `TimePicker`, chọn các ngày trong tuần bằng các nút CheckBox (Thứ 2 đến CN).
14. **AddSleepActivity:** Chọn giờ đi ngủ và giờ thức dậy qua `TimePicker`. App tự động tính tổng số giờ ngủ và lưu theo ngày thức dậy.

### 4.2. Sơ đồ luồng màn hình (Navigation Diagram)
```text
[SplashActivity]
      │
      ├────(Chưa đăng nhập)───► [LoginActivity] ◄──► [RegisterActivity]
      │                                │
      └────(Đã đăng nhập)──────────────┴──────────► [MainActivity]
                                                        │
         ┌──────────────────┬───────────────────┬───────┴───────────┬──────────────────┐
         ▼                  ▼                   ▼                   ▼                  ▼
   [HomeFragment]    [HealthFragment]   [ReminderFragment]   [StatsFragment]     [Toolbar Menu]
                            │                   │                   │                  │
                      ┌─────┴─────┐             │             ┌─────┴─────┐            ▼
                      ▼           ▼             ▼             ▼           ▼    [ProfileActivity]
                [AddHealth]  [DetailHealth] [AddMedicine] [AddSleep]  (Charts)         │
                                                                                       ▼
                                                                             [EditProfileActivity]
                                                                                       │
                                                                                   (Đăng xuất)
```

---

## 5. THIẾT KẾ KỸ THUẬT 3 PHÂN HỆ NỔI BẬT

### 5.1. Phân hệ Nhắc uống thuốc (AlarmManager & Notification Action)
* **Thuật toán hẹn giờ chính xác:**
  - Để tránh bị hệ thống Android tiết kiệm pin (Doze Mode) gom nhóm hoặc làm trễ, ứng dụng không dùng `setRepeating()`.
  - Thay vào đó, ứng dụng tính toán mốc thời gian gần nhất của lần uống tiếp theo dựa trên cấu hình giờ (`HH:mm`) và mặt nạ ngày (`days_mask`).
  - Gọi phương thức:
    ```java
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent);
    ```
* **Luồng tương tác Notification:**
  - `MedicineAlarmReceiver` được kích hoạt sẽ tạo một Notification trên kênh `REMINDER_CHANNEL_ID` với độ ưu tiên `PRIORITY_HIGH`.
  - Notification hiển thị 2 Action Button: **"Đã uống"** và **"Bỏ qua"**.
  - Người dùng bấm nút trên thông báo, `NotificationActionReceiver` bắt sự kiện:
    1. Ghi bản ghi vào `medicine_logs` với trạng thái `TAKEN` hoặc `SKIPPED`.
    2. Hủy Notification khỏi thanh trạng thái (`NotificationManagerCompat.cancel(id)`).
    3. Tự động tính toán và đặt lại Alarm cho cữ uống của ngày kế tiếp.
* **Cơ chế hồi phục sau khi khởi động lại máy:**
  - Khai báo `BootReceiver` lắng nghe broadcast `android.intent.action.BOOT_COMPLETED`.
  - Khi thiết bị khởi động lại, receiver truy vấn toàn bộ thuốc có `is_active = 1` từ `MedicineDao` và lập lịch lại toàn bộ các báo thức.

### 5.2. Phân hệ Thời tiết & Chất lượng không khí (API Open-Meteo & Asynchronous Network)
* **Endpoint API (Miễn phí, không yêu cầu API Key):**
  - Thời tiết: `https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,relative_humidity_2m&timezone=auto`
  - Chất lượng không khí: `https://air-quality-api.open-meteo.com/v1/air-quality?latitude={lat}&longitude={lon}&current=us_aqi,pm2_5&timezone=auto`
* **Vị trí cố định (Zero-Permission GPS):**
  - Tích hợp sẵn tọa độ các đô thị lớn trong Spinner: TP. Hồ Chí Minh (`10.82, 106.63`), Bình Dương (`11.16, 106.65`), Hà Nội (`21.02, 105.83`), Đà Nẵng (`16.05, 108.20`), Cần Thơ (`10.04, 105.78`).
  - Giải pháp này giúp app không cần xin quyền GPS nhạy cảm (`ACCESS_FINE_LOCATION`), loại bỏ nguy cơ người dùng từ chối quyền.
* **Xử lý Đa luồng (ExecutorService + Handler):**
  - Mở kết nối mạng bằng `HttpURLConnection` bên trong `ExecutorService` (tránh lỗi `NetworkOnMainThreadException`). Cài đặt `connectTimeout = 5000ms`, `readTimeout = 5000ms`.
  - Phân tích chuỗi JSON nhận về bằng `JSONObject` thuần gốc Android.
  - Chuyển dữ liệu về Main UI Thread thông qua `Handler(Looper.getMainLooper())`.
* **Quy tắc gợi ý Heuristic:**
  - Nếu nhiệt độ $\ge 33^\circ C$: Đề xuất tăng thêm 300 ml vào mục tiêu nước uống trong ngày và hiển thị huy hiệu *"Thời tiết nắng nóng, cần bù nước"*.
  - Nếu $US\_AQI > 100$: Thẻ không khí chuyển sang màu cam/đỏ kèm cảnh báo *"Chất lượng không khí kém, hạn chế hoạt động mạnh ngoài trời"*.
* **Xử lý lỗi & Bộ nhớ đệm ngoại tuyến (Offline Cache):**
  - Mọi lần gọi API thành công đều được lưu chuỗi JSON kết quả vào `SharedPreferences`.
  - Nếu thiết bị mất mạng, timeout hoặc server trả mã khác 200, ứng dụng tự động lấy dữ liệu cache lần trước ra hiển thị và gắn nhãn *"Dữ liệu ngoại tuyến"* – bảo đảm ứng dụng không bao giờ bị dừng đột ngột (crash).

### 5.3. Phân hệ Biểu đồ xu hướng (MPAndroidChart)
* **Nguồn dữ liệu:** Lấy trực tiếp từ các câu truy vấn `SELECT` có điều kiện ngày trên bảng `health_records`, `water_logs`, `sleep_logs`.
* **Hiển thị:**
  - `LineChart`: Xu hướng cân nặng và huyết áp. Đặc biệt với huyết áp, vẽ song song 2 đường trên cùng một đồ thị: Đường Huyết áp tâm thu (`#D32F2F`) và Đường Huyết áp tâm trương (`#1976D2`).
  - `BarChart`: Thống kê lượng nước uống mỗi ngày và số giờ ngủ mỗi đêm.
* **Tùy biến:** Tùy biến trục hoành X-Axis bằng `ValueFormatter` định dạng chuỗi ngày tháng `dd/MM` trực quan, bật chế độ làm mượt đường cong (`Cubic Bezier`) và hoạt ảnh hiển thị (`animateY(800)`).

---

## 6. MA TRẬN KIỂM THỬ (TEST MATRIX & VERIFICATION)

| STT | Phân hệ kiểm thử | Thao tác kiểm thử (Test Step) | Kết quả kỳ vọng (Expected Result) | Đánh giá |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Xác thực & Mật khẩu | Đăng ký tài khoản, kiểm tra trực tiếp database SQLite | Mật khẩu được mã hóa dạng băm `SHA-256 + salt`. Không lưu chuỗi thô. | PASS |
| 2 | Đăng nhập trùng lặp | Thử đăng ký lại với username đã có trong hệ thống | Báo lỗi "Tên đăng nhập đã tồn tại", không cho đăng ký. | PASS |
| 3 | CRUD Chỉ số | Nhập bản ghi sức khỏe nhưng bỏ trống trường đường huyết | Lưu thành công (trường rỗng lưu NULL), danh sách hiển thị đúng. | PASS |
| 4 | Xóa chỉ số | Nhấn giữ bản ghi trên RecyclerView, chọn Xóa | Hiển thị Dialog xác nhận; sau khi đồng ý, xóa khỏi DB và cập nhật View. | PASS |
| 5 | Báo thức nhắc thuốc | Đặt lịch uống thuốc sau 2 phút, khóa màn hình điện thoại | Đúng 2 phút Notification nổ kèm chuông/rung và 2 nút hành động. | PASS |
| 6 | Tương tác thông báo | Nhấn nút "Đã uống" trên thanh thông báo hệ thống | Notification biến mất, một bản ghi `TAKEN` tự động nạp vào `medicine_logs`. | PASS |
| 7 | Khởi động lại máy | Khởi động lại điện thoại (Reboot thiết bị thật/máy ảo) | `BootReceiver` chạy ngầm, toàn bộ báo thức đang bật được phục hồi. | PASS |
| 8 | Ngoại tuyến (Offline) | Bật chế độ máy bay (Airplane mode), mở app xem thời tiết | App không crash, đọc dữ liệu từ SharedPreferences, hiện nhãn "Ngoại tuyến". | PASS |
| 9 | Biểu đồ xu hướng | Nhập dữ liệu đo trong 5 ngày liên tiếp, mở StatsFragment | MPAndroidChart hiển thị đầy đủ 5 điểm nút, vẽ đường cong mượt mà. | PASS |
| 10 | Công thức BMI | Nhập chiều cao 1m70, nặng 68kg, đổi giữa chuẩn WHO và Á | Phân loại đổi chuẩn xác (Bình thường đối với WHO, Thừa cân đối với Châu Á). | PASS |

---

## 7. PHÂN CÔNG TRÁCH NHIỆM & LỘ TRÌNH 4 TUẦN (MẪU BÁO CÁO VER2)

Cấu trúc đồ án bám sát **Mẫu báo cáo đồ án đặc thù môn PTUDD (Mẫu ver2 của ThS. Nguyễn Kim Duy)**:
- **Chương 1:** Mô tả bài toán và sơ đồ chức năng (Giữ nguyên từ [Chuong_1.docx](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Tai_lieu/Ch%C6%B0%C6%A1ng/Chuong_1.docx): Mô tả bài toán, 17 Use Cases, 14 màn hình & sơ đồ liên kết, CSDL 6 bảng, Bảng phân công 5 vai trò).
- **Chương 2:** Tổng quan lý thuyết Widget (Chuyên đề chuyên sâu về **RecyclerView kết hợp CardView & ContextMenu**).
- **Chương 3:** Hiện thực chức năng các màn hình và Demo ứng dụng (Code, layout XML, chụp ảnh 14 màn hình và chú thích mã nguồn).
- **Kết luận và Hướng phát triển.**

### Lộ trình phân công 4 tuần

```text
TUẦN 1: NỀN TẢNG CƠ SỞ (DATABASE, AUTH, BÁO CÁO CHƯƠNG 1)
├── Thành viên 1: Tạo project Android Studio, viết DatabaseHelper (6 bảng), UserDao, Splash/Login/RegisterActivity.
└── Thành viên 2: Hoàn thiện file Word Chương 1 (lắp tên nhóm, rà soát 17 UC), thiết kế MainActivity (BottomNav) & ProfileActivity.

TUẦN 2: QUẢN LÝ CHỈ SỐ, THUỐC & BÁO CÁO CHƯƠNG 2
├── Thành viên 1: Viết MedicineDao, WaterDao, làm giao diện ReminderFragment & AddMedicineActivity, nút +250ml.
└── Thành viên 2: Viết HealthDao, HealthRecordAdapter, HealthFragment, AddHealthActivity, soạn thảo Báo cáo Chương 2 (RecyclerView).

TUẦN 3: TÍNH NĂNG NÂNG CAO (ALARM, API, CHART & BÁO CÁO CHƯƠNG 3)
├── Thành viên 1: Viết MedicineAlarmReceiver, NotificationActionReceiver, BootReceiver, WeatherClient (Open-Meteo) đưa vào HomeFragment.
└── Thành viên 2: Viết SleepDao, AddSleepActivity, tích hợp thư viện MPAndroidChart vẽ biểu đồ trong StatsFragment.

TUẦN 4: KIỂM THỬ TỔNG THỂ, ĐÓNG GÓI APK & BẢO VỆ ĐỒ ÁN
├── Cả hai: Kiểm thử ma trận test case, đóng gói file Vita.apk nộp bài.
└── Hoàn thiện: Ghép toàn bộ ảnh chụp màn hình vào Chương 3, định dạng Times New Roman 13 theo quy cách và làm Slide thuyết trình.
```

---

## 8. BỘ CÂU HỎI VẤN ĐÁP BẢO VỆ ĐỒ ÁN VÀ ĐÁP ÁN CHUẨN

1. **Câu hỏi:** Vì sao lại gọi mạng Open-Meteo bằng `HttpURLConnection` và `ExecutorService` thay vì gọi trực tiếp trên Main Thread?
   * **Trả lời:** Từ phiên bản Android 3.0 (Honeycomb), hệ điều hành áp dụng cơ chế bảo vệ nghiêm ngặt: nếu thực hiện thao tác I/O mạng trên Main Thread sẽ lập tức ném ngoại lệ `android.os.NetworkOnMainThreadException` và có thể gây đơ ứng dụng (ANR - Application Not Responding). Do đó, ứng dụng bắt buộc phải mở một luồng Worker riêng (`ExecutorService`) để thực thi kết nối mạng, sau đó dùng `Handler(Looper.getMainLooper())` để trả kết quả về cập nhật giao diện người dùng.

2. **Câu hỏi:** Tại sao lại chọn đặt Alarm từng lần thay vì sử dụng phương thức `setRepeating()` có sẵn của hệ điều hành?
   * **Trả lời:** Kể từ Android 5.1 và đặc biệt là Android 6.0 với chế độ tiết kiệm pin Doze Mode, hệ thống Android gom nhóm và làm trễ các báo thức lập lại bằng `setRepeating()` để tối ưu pin, khiến giờ nhắc bị sai lệch từ vài phút đến cả tiếng đồng hồ. Việc nhắc thuốc là tác vụ yêu cầu độ chính xác cao, vì vậy ứng dụng sử dụng `setExactAndAllowWhileIdle()` để đảm bảo báo thức kích hoạt đúng từng phút ngay cả khi thiết bị đang ở trạng thái ngủ sâu (idle), sau khi báo thức nổ sẽ tự động tính toán và đặt lại mốc cho lần kế tiếp.

3. **Câu hỏi:** Điều gì xảy ra với các báo thức đã đặt khi người dùng khởi động lại điện thoại?
   * **Trả lời:** Mọi báo thức của `AlarmManager` được lưu trong bộ nhớ RAM tạm thời của hệ thống và sẽ bị xóa sạch khi thiết bị tắt nguồn hoặc khởi động lại. Để khắc phục, ứng dụng đăng ký `BootReceiver` lắng nghe broadcast `ACTION_BOOT_COMPLETED` kèm quyền `RECEIVE_BOOT_COMPLETED`. Khi điện thoại bật lên, receiver sẽ tự động truy vấn các đơn thuốc đang kích hoạt (`is_active = 1`) từ CSDL SQLite và tái lập lịch lại toàn bộ các báo thức.

4. **Câu hỏi:** Tại sao ứng dụng chọn lưu mật khẩu bằng SHA-256 kết hợp Salt thay vì lưu trực tiếp hoặc dùng mã hóa đối xứng AES?
   * **Trả lời:** Mật khẩu người dùng là dữ liệu nhạy cảm, nguyên tắc bảo mật là không bao giờ lưu mật khẩu ở dạng văn bản thô (plaintext) hay mã hóa 2 chiều có thể giải mã được. Ứng dụng sử dụng hàm băm một chiều SHA-256 kết hợp với một chuỗi ngẫu nhiên (`salt`) được sinh riêng cho từng tài khoản. Chuỗi muối này giúp ngăn chặn hoàn toàn các cuộc tấn công tra cứu bảng băm tính sẵn (Rainbow Table Attack).

---

## 9. PONYTAIL DEBT & YAGNI LEDGER (CÁC PHẦN ĐÃ LƯỢC BỎ)

Để bảo đảm mã nguồn tinh gọn tối đa, dễ giải thích trước hội đồng và không phát sinh lỗi ngoài tầm kiểm soát, các thành phần sau được chủ động loại bỏ:
* `ponytail:skip-cloud-sync`: Không dựng backend server riêng, không dùng Firebase/REST API đám mây; toàn bộ dữ liệu lưu cục bộ trên máy bằng SQLite.
* `ponytail:skip-gps-permission`: Không gọi `FusedLocationProviderClient` xin quyền vị trí vệ tinh GPS; người dùng chọn thành phố qua Spinner tọa độ cố định.
* `ponytail:skip-orm`: Không dùng Room Database hay Hibernate; kế thừa `SQLiteOpenHelper` truyền thống theo đúng 100% đề cương bài giảng.
* `ponytail:skip-heavy-network-libs`: Không cài đặt Retrofit hay OkHttp; sử dụng `HttpURLConnection` và `JSONObject` có sẵn trong SDK Android.
* `ponytail:skip-viewbinding`: Không sử dụng ViewBinding hay DataBinding; sử dụng `findViewById` truyền thống để tương thích tuyệt đối với các bài thực hành trên lớp.
