# Kế hoạch Triển khai Toàn bộ Ứng dụng Android Vita (Implementation Plan)

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Xây dựng hoàn chỉnh ứng dụng Android Native "Vita" quản lý sức khỏe gia đình tại nhà (Offline-First, SQLite thuần, AlarmManager nhắc thuốc tương tác trực tiếp, MPAndroidChart, thời tiết Open-Meteo) bao gồm 14 màn hình và 10 chức năng cốt lõi.

**Architecture:** Classic Pragmatic MVC (`vn.edu.tdmu.vita`), phân tầng rõ ràng giữa 5 DAO độc lập (`database/`), POJO Models (`models/`), Tiện ích chung (`utils/`), Xử lý báo thức hệ thống (`receivers/`), Kết nối mạng luồng phụ (`network/`), Custom Adapters (`adapters/`), 10 Activities (`activities/`) và 4 Fragments gắn `BottomNavigationView` (`fragments/`).

**Tech Stack:**
- **Ngôn ngữ & SDK:** Java (JDK 17), `minSdkVersion: 24` (Android 7.0), `targetSdkVersion: 34` (Android 14), AndroidX & Material Design 3.
- **Lưu trữ CSDL:** SQLite thuần qua `SQLiteOpenHelper`, `SharedPreferences` cho phiên đăng nhập & bộ nhớ đệm thời tiết.
- **Báo thức & Đa luồng:** `AlarmManager` (`setExactAndAllowWhileIdle`), `BroadcastReceiver`, `ExecutorService` (Worker Thread), `Handler(Looper.getMainLooper())`.
- **Giao tiếp Mạng & Thư viện ngoài duy nhất:** `HttpURLConnection`, `JSONObject` (Open-Meteo REST API); `com.github.PhilJay:MPAndroidChart:v3.1.0`.
- **Bảo mật:** `SHA-256` hashing + muối ngẫu nhiên (`salt` 16 bytes).

**Spec:** [`docs/superpowers/specs/2026-10-09-vita-mobile-design.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/docs/superpowers/specs/2026-10-09-vita-mobile-design.md)  
**Design System:** [`design-system/vita/MASTER.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/MASTER.md) & [`design-system/vita/SYSTEM_TOKENS.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/SYSTEM_TOKENS.md)

---

## Global Constraints & Ponytail Directives

- **Nguyên tắc Ponytail (Lazy Senior Dev):** Giải quyết 100% yêu cầu bài toán với ít mã nguồn mới nhất. Không over-engineering, không tạo lớp trừu tượng (abstraction) thừa thãi, tận dụng tối đa Android SDK gốc.
- **Package name:** `vn.edu.tdmu.vita`
- **Tên CSDL SQLite:** `vita_health.db`, kích hoạt `PRAGMA foreign_keys = ON;`, `ON DELETE CASCADE`.
- **Zero-Cloud & Zero-GPS Permission:** Lưu trữ 100% dữ liệu SQLite cục bộ trên máy; chọn thành phố qua Spinner tọa độ cố định, không xin quyền `ACCESS_FINE_LOCATION`.
- **Zero-Boilerplate (Ponytail Debt Ledger):**
  - `ponytail:skip-orm`: Không cài đặt Room Database; sử dụng `SQLiteOpenHelper` truyền thống.
  - `ponytail:skip-viewbinding`: Không cấu hình ViewBinding hay DataBinding; sử dụng `findViewById` truyền thống.
  - `ponytail:skip-heavy-network-libs`: Không cài Retrofit, OkHttp, Gson; sử dụng `HttpURLConnection` và `JSONObject` chuẩn gốc của Android.
  - `ponytail:skip-clean-architecture-bloat`: Không tạo thêm tầng Repository hay UseCase trung gian; Activity/Fragment trực tiếp gọi DAO qua Context.
  - `ponytail:single-third-party-lib`: Thư viện bên ngoài duy nhất được phép có mặt là `MPAndroidChart:v3.1.0` để vẽ đồ thị theo yêu cầu đề tài.

### Quy tắc BẤT KHẢ XÂM PHẠM (Non-Negotiables):
1. **Validation:** Kiểm tra chặt chẽ dữ liệu đầu vào ở mọi form (không rỗng, mật khẩu $\ge$ 6 ký tự, ngày tháng hợp lệ, huyết áp tâm thu > tâm trương).
2. **Error Handling:** Bắt và xử lý mọi ngoại lệ (`IOException`, `SocketTimeoutException`, `NumberFormatException`); không để app bị crash (ANR/Force Close); fallback sang cache SharedPreferences khi offline.
3. **Security:** Mật khẩu bắt buộc băm `SHA-256` kết hợp chuỗi ngẫu nhiên (`salt` 16 bytes). Không bao giờ lưu mật khẩu dạng chuỗi thô.
4. **Accessibility:** Vùng chạm cảm ứng tối thiểu $\ge 48$dp × 48dp, khoảng cách giữa các nút $\ge 8$dp, chữ số Display 32sp, tương phản WCAG AAA / AA, hỗ trợ phóng to font `sp`.
5. **Data Integrity:** Đảm bảo tính đúng đắn của dữ liệu: tính thời lượng ngủ qua đêm chính xác, mặt nạ bit 7 ngày chuẩn xác, khôi phục báo thức sau khi khởi động lại máy (`BootReceiver`).

---

## Review Focus

1. **Khôi phục báo thức sau khi khởi động lại máy:** Sự kiện `ACTION_BOOT_COMPLETED` được đón bởi `BootReceiver`, truy vấn đúng các đơn thuốc `is_active = 1` và kích hoạt lại Alarm chính xác.
2. **Xử lý giấc ngủ qua nửa đêm:** Đi ngủ tối hôm trước (ví dụ 23:30) và thức dậy sáng hôm sau (ví dụ 06:30), thời lượng phải tính đúng $7.0$ giờ thay vì số âm.
3. **Chỉ số sinh hiệu cho phép NULL:** Bỏ trống huyết áp hoặc cân nặng khi ghi nhận thì CSDL lưu giá trị NULL thành công mà không gây `NullPointerException` khi nạp lên giao diện.
4. **Mất kết nối mạng khi tải thời tiết:** Không ném `NetworkOnMainThreadException` và không crash app; tự động hiển thị dữ liệu lưu đệm từ `SharedPreferences` kèm nhãn *"Ngoại tuyến"*.
5. **Xác nhận uống thuốc từ Notification:** Bấm "Đã uống" hoặc "Bỏ qua" trên thanh thông báo hệ thống tự động ghi bản ghi vào `medicine_logs`, hủy Notification và lập lịch cho cữ uống ngày tiếp theo.

---

## Cấu trúc Các Phase Triển khai

- **Phase 1: Project Scaffolding, Models & Tầng CSDL SQLite (6 Bảng & 5 DAOs)**
- **Phase 2: Xác thực Tài khoản, Phiên làm việc & Quản lý Hồ sơ Người dùng**
- **Phase 3: Phân hệ Chỉ số Sức khỏe & Công thức Tính BMI Tự động**
- **Phase 4: Phân hệ Nhắc uống thuốc, Thông báo Tương tác & Nhật ký Nước uống**
- **Phase 5: Phân hệ Nhật ký Giấc ngủ & Trực quan hóa Biểu đồ MPAndroidChart**
- **Phase 6: Phân hệ Thời tiết Open-Meteo & Dashboard Trang chủ**
- **Phase 7: Tích hợp Toàn diện, AndroidManifest & Kiểm thử Hệ thống (Test Matrix)**

---

# Phase 1: Project Scaffolding, Models & Tầng CSDL SQLite (6 Bảng & 5 DAOs)

### Task 1.1: Khởi tạo Project Android Studio & Cấu hình Build Gradle
**Files:**
- Create: `build.gradle`, `app/build.gradle`, `settings.gradle`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/dimens.xml`
- Create: `app/src/main/res/values/styles.xml`
- Test: `app/src/test/java/vn/edu/tdmu/vita/BuildConfigurationTest.java`

**Interfaces:**
- Produces: Project Android SDK 34, thư viện `MPAndroidChart:v3.1.0`, toàn bộ Color & Dimen tokens từ `design-system/vita/SYSTEM_TOKENS.md`.

- [x] **Step 1: Write test kiểm tra cấu hình hằng số package**
  Viết unit test xác nhận hằng số `PACKAGE_NAME = "vn.edu.tdmu.vita"` và phiên bản `MIN_SDK = 24`.
- [x] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.BuildConfigurationTest"`
  Expected: FAIL (lớp chưa tồn tại).
- [x] **Step 3: Khởi tạo cấu hình Gradle và tài nguyên colors/dimens/styles**
  Khai báo `minSdk 24`, `targetSdk 34`, nạp dependency `com.github.PhilJay:MPAndroidChart:v3.1.0`. Sao chép chính xác tài nguyên từ [`SYSTEM_TOKENS.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/SYSTEM_TOKENS.md).
- [x] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.BuildConfigurationTest"`
  Expected: PASS.
- [x] **Step 5: Commit**
  `git commit -m "chore: setup project structure, gradle build and design system tokens"`

---

### Task 1.2: Các Lớp Mô hình Dữ liệu POJO (Models)
**Files:**
- Create: `app/src/main/java/vn/edu/tdmu/vita/models/User.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/models/HealthRecord.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/models/Medicine.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/models/MedicineLog.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/models/WaterLog.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/models/SleepLog.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/models/WeatherData.java`
- Test: `app/src/test/java/vn/edu/tdmu/vita/models/ModelsTest.java`

**Interfaces:**
- Produces: Các POJO có đầy đủ Constructor, Getters/Setters tương ứng với 6 bảng SQLite và dữ liệu khí tượng Open-Meteo.

- [x] **Step 1: Write failing unit test cho các Models**
  Kiểm tra khởi tạo và getter/setter của `User`, `HealthRecord`, `Medicine`, `MedicineLog`, `WaterLog`, `SleepLog`, `WeatherData`.
- [x] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.models.ModelsTest"`
  Expected: FAIL (models chưa được tạo).
- [x] **Step 3: Hiện thực các lớp Models POJO**
  Hiện thực đúng các trường dữ liệu theo Mục 3 của Bản đặc tả kỹ thuật (ví dụ: `HealthRecord` dùng các trường kiểu đối tượng `Float`, `Integer` để cho phép `null`).
- [x] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.models.ModelsTest"`
  Expected: PASS.
- [x] **Step 5: Commit**
  `git commit -m "feat: implement pojo data models for vita"`

---

### Task 1.3: CSDL SQLite - DatabaseHelper & Quản lý Khóa ngoại / Index
**Files:**
- Create: `app/src/main/java/vn/edu/tdmu/vita/database/DatabaseHelper.java`
- Test: `app/src/test/java/vn/edu/tdmu/vita/database/DatabaseHelperTest.java`

**Interfaces:**
- Produces: `DatabaseHelper(Context context)`, `onConfigure(SQLiteDatabase db)` (bật `PRAGMA foreign_keys = ON`), `onCreate(SQLiteDatabase db)` (tạo 6 bảng + 3 Composite Indexes `(user_id, date)`), `onUpgrade(SQLiteDatabase db, int oldV, int newV)`.

- [x] **Step 1: Write failing test cho câu lệnh tạo bảng và index**
  Kiểm tra chuỗi SQL schema chứa đúng 6 bảng (`users`, `health_records`, `medicines`, `medicine_logs`, `water_logs`, `sleep_logs`) và 3 index (`idx_health_user_date`, `idx_water_user_date`, `idx_sleep_user_date`).
- [x] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.database.DatabaseHelperTest"`
  Expected: FAIL.
- [x] **Step 3: Hiện thực `DatabaseHelper.java`**
  Khởi tạo CSDL `vita_health.db`, database version 1, viết đúng cú pháp DDL SQL chuẩn 3NF có ràng buộc `ON DELETE CASCADE`.
- [x] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.database.DatabaseHelperTest"`
  Expected: PASS.
- [x] **Step 5: Commit**
  `git commit -m "feat: implement DatabaseHelper with 6 tables, foreign keys and indexes"`

---

### Task 1.4: Tầng DAO - UserDao & Bảo mật Băm Mật khẩu (SHA-256 + Salt)
**Files:**
- Create: `app/src/main/java/vn/edu/tdmu/vita/utils/SecurityUtils.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/database/UserDao.java`
- Test: `app/src/test/java/vn/edu/tdmu/vita/database/UserDaoTest.java`

**Interfaces:**
- Produces: `SecurityUtils.generateSalt(): String`, `SecurityUtils.hashPassword(String password, String salt): String`
- Produces: `UserDao.register(User user, String password): long`, `UserDao.login(String username, String password): User`, `UserDao.getUserById(long userId): User`, `UserDao.updateProfile(User user): boolean`, `UserDao.updateBmiStandard(long userId, String standard): boolean`.

- [x] **Step 1: Write unit test cho hàm băm SHA-256 và xác thực mật khẩu**
  Kiểm tra cùng một password với 2 salt khác nhau sinh 2 chuỗi băm khác nhau; kiểm tra hàm verify mật khẩu đúng/sai.
- [x] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.database.UserDaoTest"`
  Expected: FAIL.
- [x] **Step 3: Hiện thực `SecurityUtils.java` và `UserDao.java`**
  Sử dụng `MessageDigest.getInstance("SHA-256")` và `SecureRandom` sinh 16-byte hex salt; viết các câu lệnh truy vấn SQLite bằng `insert`, `query`, `update`.
- [x] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.database.UserDaoTest"`
  Expected: PASS.
- [x] **Step 5: Commit**
  `git commit -m "feat: implement SecurityUtils sha256 salt hashing and UserDao"`

---

### Task 1.5: Tầng DAO - HealthDao, MedicineDao, WaterDao & SleepDao
**Files:**
- Create: `app/src/main/java/vn/edu/tdmu/vita/database/HealthDao.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/database/MedicineDao.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/database/WaterDao.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/database/SleepDao.java`
- Test: `app/src/test/java/vn/edu/tdmu/vita/database/DaosTest.java`

**Interfaces:**
- Produces:
  - `HealthDao`: `insert(HealthRecord)`, `update(HealthRecord)`, `delete(long id)`, `getAllByUserId(long, String dateFilter)`, `getLatestWeight(long)`, `getById(long)`
  - `MedicineDao`: `insert(Medicine)`, `update(Medicine)`, `delete(long)`, `getAllByUserId(long)`, `setActive(long, boolean)`, `logMedicine(long medId, String date, String status)`
  - `WaterDao`: `addWater(long userId, String date, int amountMl)`, `getDailyTotal(long userId, String date)`, `getWeeklyLogs(long userId)`
  - `SleepDao`: `insert(SleepLog)`, `getLatest7Days(long userId)`, `delete(long id)`

- [x] **Step 1: Write unit test cho các thao tác CRUD của 4 DAOs**
  Kiểm tra các phương thức insert/update/delete và các truy vấn theo `user_id`.
- [x] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.database.DaosTest"`
  Expected: FAIL.
- [x] **Step 3: Hiện thực 4 DAO**
  Viết logic bóc tách `Cursor` thành các POJO Model, xử lý an toàn giá trị `null` đối với các trường sinh hiệu tùy chọn trong `HealthDao`.
- [x] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.database.DaosTest"`
  Expected: PASS.
- [x] **Step 5: Commit**
  `git commit -m "feat: implement HealthDao, MedicineDao, WaterDao and SleepDao"`

---

# Phase 2: Xác thực Tài khoản, Phiên làm việc & Quản lý Hồ sơ

### Task 2.1: Quản lý Phiên SharedPreferences (SessionManager) & Tiện ích Ngày giờ
**Files:**
- Create: `app/src/main/java/vn/edu/tdmu/vita/utils/SessionManager.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/utils/DateTimeUtils.java`
- Test: `app/src/test/java/vn/edu/tdmu/vita/utils/SessionAndDateTimeTest.java`

**Interfaces:**
- Produces: `SessionManager`: `saveSession(long userId, String username)`, `getUserId(): long`, `isLoggedIn(): boolean`, `clearSession()`
- Produces: `DateTimeUtils`: `getTodayDate(): String (yyyy-MM-dd)`, `getCurrentTime(): String (HH:mm)`, `formatDisplayDate(String yyyyMMdd): String (dd/MM/yyyy)`

- [x] **Step 1: Write unit test cho SessionManager và DateTimeUtils**
  Kiểm tra lưu/xóa phiên và định dạng chuỗi ngày tháng theo chuẩn Việt Nam.
- [x] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.utils.SessionAndDateTimeTest"`
  Expected: FAIL.
- [x] **Step 3: Hiện thực `SessionManager.java` và `DateTimeUtils.java`**
  Sử dụng `SharedPreferences` với chế độ `Context.MODE_PRIVATE` và `SimpleDateFormat` an toàn với múi giờ Việt Nam (`Locale("vi", "VN")`).
- [x] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.utils.SessionAndDateTimeTest"`
  Expected: PASS.
- [x] **Step 5: Commit**
  `git commit -m "feat: implement SessionManager and DateTimeUtils"`

---

### Task 2.2: Giao diện Khởi động (SplashActivity)
**Files:**
- Create: `app/src/main/res/layout/activity_splash.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/SplashActivity.java`
- Ref: [`design-system/vita/pages/01_splash.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/01_splash.md)

**Interfaces:**
- Consumes: `SessionManager.isLoggedIn()`
- Produces: Điều phối Intent mở `MainActivity` (nếu đã đăng nhập) hoặc `LoginActivity` (nếu chưa) sau `1200ms`.

- [x] **Step 1: Thiết kế layout `activity_splash.xml`**
  Dựng Logo 120dp, Tiêu đề Display 32sp, Slogan, ProgressBar theo đúng chuẩn `01_splash.md`.
- [x] **Step 2: Hiện thực logic điều phối trong `SplashActivity.java`**
  Sử dụng `Handler(Looper.getMainLooper()).postDelayed(...)` trong 1200ms để kiểm tra `SessionManager` và gọi Intent điều hướng, kết thúc bằng `finish()`.
- [x] **Step 3: Kiểm thử giao diện trên Emulator**
  Mở app lần đầu vào LoginActivity; sau khi đăng nhập tắt mở lại app vào thẳng MainActivity.
- [x] **Step 4: Commit**
  `git commit -m "feat: implement SplashActivity with session routing"`

---

### Task 2.3: Giao diện Đăng nhập (LoginActivity) & Đăng ký (RegisterActivity)
**Files:**
- Create: `app/src/main/res/layout/activity_login.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/LoginActivity.java`
- Create: `app/src/main/res/layout/activity_register.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/RegisterActivity.java`
- Ref: [`design-system/vita/pages/02_login.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/02_login.md), [`03_register.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/03_register.md)

**Interfaces:**
- Consumes: `UserDao.login(...)`, `UserDao.register(...)`, `SessionManager`
- Produces: Xác thực form, hiển thị lỗi qua `TextInputLayout.setError()`, lưu phiên và mở `MainActivity`.

- [x] **Step 1: Dựng layout XML cho Login và Register**
  Sử dụng `TextInputLayout` Outlined, bo góc `8dp`, chiều cao nút tối thiểu `48dp`, phông chữ `Roboto`.
- [x] **Step 2: Hiện thực `LoginActivity.java`**
  Bắt sự kiện click Đăng nhập, validate rỗng, gọi `UserDao.login()`, lưu session, mở `MainActivity`.
- [x] **Step 3: Hiện thực `RegisterActivity.java`**
  Validate mật khẩu trùng khớp, kiểm tra trùng lặp `username`, thu thập chiều cao/cân nặng cơ sở ban đầu, gọi `UserDao.register()`, mở `MainActivity`.
- [x] **Step 4: Kiểm thử trên máy ảo**
  Kiểm thử ma trận Test case 1 & 2: Mật khẩu băm an toàn, đăng ký trùng tên báo lỗi.
- [x] **Step 5: Commit**
  `git commit -m "feat: implement LoginActivity and RegisterActivity with input validation"`

---

### Task 2.4: Giao diện Hồ sơ Cá nhân (ProfileActivity) & Sửa Hồ sơ (EditProfileActivity)
**Files:**
- Create: `app/src/main/res/layout/activity_profile.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/ProfileActivity.java`
- Create: `app/src/main/res/layout/activity_edit_profile.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/EditProfileActivity.java`
- Ref: [`design-system/vita/pages/09_profile.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/09_profile.md), [`10_edit_profile.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/10_edit_profile.md)

**Interfaces:**
- Consumes: `UserDao.getUserById()`, `UserDao.updateProfile()`, `UserDao.updateBmiStandard()`
- Produces: Đổi chuẩn BMI (Châu Á / WHO) tức thì; cập nhật chiều cao, mục tiêu nước; nút Đăng xuất xóa phiên và xóa sạch back stack.

- [x] **Step 1: Dựng layout `activity_profile.xml` và `activity_edit_profile.xml`**
  Thẻ CardView thông số, RadioGroup chọn chuẩn BMI, nút Đăng xuất màu đỏ `#D32F2F`.
- [x] **Step 2: Hiện thực `ProfileActivity.java`**
  Hiển thị thông tin hồ sơ; xử lý sự kiện đổi RadioButton tự động cập nhật `bmi_standard` vào CSDL và bắn Snackbar.
- [x] **Step 3: Hiện thực `EditProfileActivity.java`**
  Form cập nhật thông tin cá nhân; xử lý nút Đăng xuất kèm Dialog xác nhận, xóa session và mở lại LoginActivity.
- [x] **Step 4: Kiểm thử chuyển đổi chuẩn BMI và Đăng xuất**
  Xác nhận session bị hủy và back stack không thể quay lại Main.
- [x] **Step 5: Commit**
  `git commit -m "feat: implement ProfileActivity and EditProfileActivity with BMI standard selection"`

---

# Phase 3: Phân hệ Chỉ số Sức khỏe & Công thức Tính BMI

### Task 3.1: Thuật toán BMICalculator (Chuẩn Châu Á & WHO)
**Files:**
- Create: `app/src/main/java/vn/edu/tdmu/vita/utils/BMICalculator.java`
- Test: `app/src/test/java/vn/edu/tdmu/vita/utils/BMICalculatorTest.java`

**Interfaces:**
- Produces: `BMICalculator.calculate(float weightKg, float heightCm): float`, `BMICalculator.classify(float bmi, String standard): BmiResult`

- [ ] **Step 1: Write unit test cho công thức tính BMI và phân loại theo 2 chuẩn**
  Test case: Chiều cao 1m70, nặng 68kg -> BMI = 23.53 -> Chuẩn Châu Á: "Thừa cân", Chuẩn WHO: "Bình thường".
- [ ] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.utils.BMICalculatorTest"`
  Expected: FAIL.
- [ ] **Step 3: Hiện thực `BMICalculator.java`**
  Tính $\text{BMI} = \frac{\text{cân nặng (kg)}}{(\text{chiều cao (m)})^2}$; phân loại trả về nhãn tiếng Việt và màu sắc Semantic Status.
- [ ] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.utils.BMICalculatorTest"`
  Expected: PASS.
- [ ] **Step 5: Commit**
  `git commit -m "feat: implement BMICalculator supporting Asian and WHO standards"`

---

### Task 3.2: Giao diện Nhập/Sửa Chỉ số Sinh hiệu (AddHealthActivity)
**Files:**
- Create: `app/src/main/res/layout/activity_add_health.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/AddHealthActivity.java`
- Ref: [`design-system/vita/pages/11_add_health.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/11_add_health.md)

**Interfaces:**
- Consumes: `HealthDao.insert()`, `HealthDao.update()`, `HealthDao.getById()`
- Produces: Form nhập sinh hiệu (chọn ngày qua `DatePickerDialog`), **cho phép bỏ trống trường không đo để lưu NULL vào SQLite**.

- [ ] **Step 1: Dựng layout `activity_add_health.xml`**
  Các trường Tâm thu, Tâm trương, Nhịp tim, Cân nặng, Đường huyết, Ghi chú có `suffixText` rõ ràng.
- [ ] **Step 2: Hiện thực `AddHealthActivity.java`**
  Xử lý mở `DatePickerDialog` khi bấm ô ngày; kiểm tra logic tâm thu > tâm trương; parse chuỗi rỗng thành `null` khi ghi vào `HealthRecord`.
- [ ] **Step 3: Kiểm thử lưu bản ghi khuyết chỉ số**
  Xác nhận lưu thành công khi chỉ nhập huyết áp, các trường khác để trống lưu NULL trong SQLite.
- [ ] **Step 4: Commit**
  `git commit -m "feat: implement AddHealthActivity with nullable metric handling"`

---

### Task 3.3: RecyclerView Adapter, Context Menu & HealthFragment (Trọng tâm Báo cáo Chương 2)
**Files:**
- Create: `app/src/main/res/layout/item_health_record.xml`
- Create: `app/src/main/res/menu/context_menu_health.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/adapters/HealthRecordAdapter.java`
- Create: `app/src/main/res/layout/fragment_health.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/fragments/HealthFragment.java`
- Ref: [`design-system/vita/pages/06_health.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/06_health.md)

**Interfaces:**
- Consumes: `HealthDao.getAllByUserId()`, `HealthDao.delete()`
- Produces: Danh sách ngày giảm dần; Single Click mở `DetailHealthActivity`; Long Click mở Context Menu (Sửa / Xóa kèm Dialog xác nhận).

- [ ] **Step 1: Dựng layout `item_health_record.xml` và `fragment_health.xml`**
  Thẻ CardView bo góc 12dp, padding 16dp, badge màu trạng thái huyết áp, FAB mở `AddHealthActivity`.
- [ ] **Step 2: Hiện thực `HealthRecordAdapter.java`**
  Kế thừa `RecyclerView.Adapter`, triển khai ViewHolder pattern, gán `setOnCreateContextMenuListener` cho item.
- [ ] **Step 3: Hiện thực `HealthFragment.java`**
  Tải dữ liệu từ `HealthDao`, xử lý bộ lọc ngày qua `DatePickerDialog`, xử lý sự kiện Context Menu Sửa và Xóa (kèm AlertDialog xác nhận).
- [ ] **Step 4: Kiểm thử xóa bản ghi qua Context Menu**
  Nhấn giữ item, chọn Xóa -> Dialog xác nhận -> xóa khỏi DB và gọi `notifyItemRemoved()`.
- [ ] **Step 5: Commit**
  `git commit -m "feat: implement HealthRecordAdapter with Context Menu and HealthFragment"`

---

### Task 3.4: Giao diện Chi tiết Bản ghi Sinh hiệu (DetailHealthActivity)
**Files:**
- Create: `app/src/main/res/layout/activity_detail_health.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/DetailHealthActivity.java`
- Ref: [`design-system/vita/pages/12_detail_health.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/12_detail_health.md)

**Interfaces:**
- Consumes: `HealthDao.getById(recordId)`
- Produces: Đánh giá chi tiết huyết áp & nhịp tim; hiển thị `--` cho trường null; nút chuyển sang `AddHealthActivity` sửa bản ghi.

- [ ] **Step 1: Dựng layout `activity_detail_health.xml`**
  Thẻ Huyết áp Display 32sp, Thẻ Thể trạng, Thẻ Ghi chú, Nút Sửa bản ghi.
- [ ] **Step 2: Hiện thực `DetailHealthActivity.java`**
  Đọc dữ liệu từ SQLite; áp dụng thuật toán phân loại huyết áp chuẩn y học Việt Nam; xử lý click nút Sửa.
- [ ] **Step 3: Kiểm thử hiển thị**
  Mở bản ghi từ danh sách, kiểm tra hiển thị đúng thông số và đánh giá.
- [ ] **Step 4: Commit**
  `git commit -m "feat: implement DetailHealthActivity with health evaluation"`

---

# Phase 4: Phân hệ Nhắc uống thuốc, Thông báo & Nước uống

### Task 4.1: Hệ thống Báo thức AlarmManager & BroadcastReceivers
**Files:**
- Create: `app/src/main/java/vn/edu/tdmu/vita/utils/NotificationHelper.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/receivers/MedicineAlarmReceiver.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/receivers/NotificationActionReceiver.java`
- Create: `app/src/main/java/vn/edu/tdmu/vita/receivers/BootReceiver.java`
- Ref: [`design-system/vita/pages/15_notifications.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/15_notifications.md)

**Interfaces:**
- Consumes: `MedicineDao.setActive()`, `MedicineDao.logMedicine()`, `AlarmManager`
- Produces: NotificationChannel `vita_medicine_channel` (High Priority); Thông báo có 2 Action Button "Đã uống" / "Bỏ qua"; Phục hồi báo thức khi `ACTION_BOOT_COMPLETED`.

- [ ] **Step 1: Hiện thực `NotificationHelper.java`**
  Tạo `NotificationChannel` trên Android 8.0+ với `IMPORTANCE_HIGH`, âm thanh và rung nhịp đôi.
- [ ] **Step 2: Hiện thực `MedicineAlarmReceiver.java`**
  Bắn `NotificationCompat.Builder` hiển thị tên thuốc, liều dùng và 2 `PendingIntent` gắn Action Button "Đã uống" / "Bỏ qua".
- [ ] **Step 3: Hiện thực `NotificationActionReceiver.java`**
  Bắt sự kiện click: ghi `medicine_logs` ('TAKEN' / 'SKIPPED'), đóng Notification (`cancel`), tự động đặt lại Alarm cho ngày tiếp theo.
- [ ] **Step 4: Hiện thực `BootReceiver.java`**
  Lắng nghe `ACTION_BOOT_COMPLETED`, truy vấn `MedicineDao` lấy thuốc `is_active = 1` và kích hoạt lại Alarm.
- [ ] **Step 5: Commit**
  `git commit -m "feat: implement MedicineAlarmReceiver, NotificationActionReceiver and BootReceiver"`

---

### Task 4.2: Giao diện Thêm/Sửa Thuốc (AddMedicineActivity)
**Files:**
- Create: `app/src/main/res/layout/activity_add_medicine.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/AddMedicineActivity.java`
- Ref: [`design-system/vita/pages/13_add_medicine.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/13_add_medicine.md)

**Interfaces:**
- Consumes: `MedicineDao.insert()`, `MedicineDao.update()`
- Produces: Chọn giờ qua `TimePickerDialog`, chọn 7 ngày trong tuần qua các nút tương ứng mặt nạ bit `days_mask` (0–127), kích hoạt `AlarmManager.setExactAndAllowWhileIdle()`.

- [ ] **Step 1: Dựng layout `activity_add_medicine.xml`**
  Trường tên thuốc, liều dùng, ô giờ báo thức, 7 nút tròn T2 đến CN, CheckBox "Chọn hàng ngày".
- [ ] **Step 2: Hiện thực logic tính bitmask ngày và hẹn giờ**
  Tính toán mốc `nextTriggerMillis` dựa trên giờ đã chọn và ngày trong tuần gần nhất.
- [ ] **Step 3: Kiểm thử đặt lịch thuốc**
  Tạo đơn thuốc thử nghiệm sau 2 phút, khóa màn hình, xác nhận chuông báo nổ đúng giờ.
- [ ] **Step 4: Commit**
  `git commit -m "feat: implement AddMedicineActivity with TimePicker and days bitmask"`

---

### Task 4.3: Giao diện Nhắc nhở (ReminderFragment) & Tiến độ Nước uống (+250 ml)
**Files:**
- Create: `app/src/main/res/layout/item_medicine.xml`
- Create: `app/src/main/res/menu/context_menu_medicine.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/adapters/MedicineAdapter.java`
- Create: `app/src/main/res/layout/fragment_reminder.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/fragments/ReminderFragment.java`
- Ref: [`design-system/vita/pages/07_reminder.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/07_reminder.md)

**Interfaces:**
- Consumes: `MedicineDao`, `WaterDao.addWater()`, `WaterDao.getDailyTotal()`
- Produces: Switch gạt bật/tắt báo thức trực tiếp; Nút nạp nhanh "+250 ml" cập nhật ProgressBar tức thì.

- [ ] **Step 1: Dựng layout `item_medicine.xml` và `fragment_reminder.xml`**
  Thẻ tiến độ nước bo tròn (ProgressBar), nút nạp nước 48dp, danh sách RecyclerView thuốc kèm Switch.
- [ ] **Step 2: Hiện thực `MedicineAdapter.java`**
  Hiển thị danh sách thuốc; gán sự kiện cho `MaterialSwitch` để bật/tắt báo thức trong CSDL và hệ thống; gán Context Menu Sửa/Xóa.
- [ ] **Step 3: Hiện thực `ReminderFragment.java`**
  Xử lý nút "+250 ml" gọi `WaterDao.addWater()`, cập nhật thanh tiến độ có hoạt ảnh `ObjectAnimator`.
- [ ] **Step 4: Kiểm thử nạp nước và Switch thuốc**
  Bấm nút nạp nước, xác nhận thanh tiến độ dâng lên và lưu đúng vào bảng `water_logs`.
- [ ] **Step 5: Commit**
  `git commit -m "feat: implement ReminderFragment with medicine switch and quick water tracking"`

---

# Phase 5: Phân hệ Giấc ngủ & Trực quan hóa MPAndroidChart

### Task 5.1: Giao diện Ghi nhận Giấc ngủ (AddSleepActivity - Logic Qua Nửa Đêm)
**Files:**
- Create: `app/src/main/res/layout/activity_add_sleep.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/AddSleepActivity.java`
- Ref: [`design-system/vita/pages/14_add_sleep.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/14_add_sleep.md)

**Interfaces:**
- Consumes: `SleepDao.insert()`
- Produces: Chọn giờ ngủ và giờ thức (`TimePickerDialog`), tự động tính `duration_h` qua đêm và lưu theo ngày thức dậy.

- [ ] **Step 1: Dựng layout `activity_add_sleep.xml`**
  Icon mặt trăng tím `#512DA8`, 2 ô chọn giờ, Thẻ kết quả tổng thời lượng tự động nhảy số.
- [ ] **Step 2: Hiện thực thuật toán xử lý qua nửa đêm trong `AddSleepActivity.java`**
  Nếu `wake_time < sleep_time`, thời lượng = `(24 * 60 - sleep_min + wake_min) / 60.0f`; hiển thị kết quả thời gian thực.
- [ ] **Step 3: Kiểm thử logic giờ ngủ**
  Nhập ngủ 23:00, thức 06:30 -> Hiển thị chính xác 7.5 giờ; lưu thành công vào SQLite.
- [ ] **Step 4: Commit**
  `git commit -m "feat: implement AddSleepActivity with overnight sleep calculation"`

---

### Task 5.2: Giao diện Thống kê Trực quan (StatsFragment - MPAndroidChart)
**Files:**
- Create: `app/src/main/res/layout/item_sleep_record.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/adapters/SleepRecordAdapter.java`
- Create: `app/src/main/res/layout/fragment_stats.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/fragments/StatsFragment.java`
- Ref: [`design-system/vita/pages/08_stats.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/08_stats.md)

**Interfaces:**
- Consumes: `HealthDao`, `WaterDao`, `SleepDao`, `MPAndroidChart`
- Produces: LineChart vẽ song song 2 đường Huyết áp (Tâm thu đỏ, Tâm trương xanh), BarChart lượng nước & giấc ngủ, Toggle 7 ngày / 30 ngày.

- [ ] **Step 1: Dựng layout `fragment_stats.xml` và `item_sleep_record.xml`**
  Thẻ LineChart, Thẻ BarChart, Segmented Button chọn 7/30 ngày, RecyclerView danh sách giấc ngủ.
- [ ] **Step 2: Hiện thực pipeline nạp dữ liệu từ SQLite vào MPAndroidChart**
  Chuyển đổi Cursor dữ liệu thành `Entry` và `BarEntry`; cài đặt `ValueFormatter` định dạng ngày `dd/MM` trên trục X; bật `animateY(800)`.
- [ ] **Step 3: Hiện thực `StatsFragment.java`**
  Vẽ biểu đồ đường song song huyết áp; hiển thị `MarkerView` khi chạm vào điểm nút; nạp danh sách 7 giấc ngủ gần nhất.
- [ ] **Step 4: Kiểm thử hiển thị biểu đồ**
  Nhập dữ liệu 5 ngày liên tiếp, mở tab Thống kê, xác nhận biểu đồ vẽ đủ 5 điểm mượt mà.
- [ ] **Step 5: Commit**
  `git commit -m "feat: implement StatsFragment with MPAndroidChart dual line and bar charts"`

---

# Phase 6: Phân hệ Thời tiết Open-Meteo & Dashboard Trang chủ

### Task 6.1: Kết nối API Mạng Ngoại tuyến An toàn (WeatherClient)
**Files:**
- Create: `app/src/main/java/vn/edu/tdmu/vita/network/WeatherClient.java`
- Test: `app/src/test/java/vn/edu/tdmu/vita/network/WeatherClientTest.java`

**Interfaces:**
- Produces: `WeatherClient.fetchWeather(double lat, double lon, Callback callback)` chạy trên Worker Thread (`ExecutorService`), trả kết quả về Main UI Thread qua `Handler(Looper.getMainLooper())`.

- [ ] **Step 1: Write unit test bóc tách chuỗi JSON Open-Meteo**
  Kiểm tra hàm parse JSON trích xuất đúng nhiệt độ, độ ẩm và US AQI từ response mẫu.
- [ ] **Step 2: Run test to verify it fails**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.network.WeatherClientTest"`
  Expected: FAIL.
- [ ] **Step 3: Hiện thực `WeatherClient.java`**
  Sử dụng `HttpURLConnection` với timeout 5000ms; phân tích `JSONObject`; cơ chế lưu đệm JSON vào `SharedPreferences` khi thành công để phục vụ chế độ Offline.
- [ ] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "vn.edu.tdmu.vita.network.WeatherClientTest"`
  Expected: PASS.
- [ ] **Step 5: Commit**
  `git commit -m "feat: implement WeatherClient with ExecutorService and offline caching"`

---

### Task 6.2: Giao diện Trang chủ (HomeFragment) & Gợi ý Bù nước Heuristic
**Files:**
- Create: `app/src/main/res/layout/fragment_home.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/fragments/HomeFragment.java`
- Ref: [`design-system/vita/pages/05_home.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/05_home.md)

**Interfaces:**
- Consumes: `BMICalculator`, `HealthDao.getLatestWeight()`, `UserDao`, `WeatherClient`
- Produces: Lời chào cá nhân; Thẻ BMI tự động; Spinner chọn thành phố (Zero-GPS); Thẻ Thời tiết & AQI; Thẻ Lời khuyên Heuristic (nhiệt độ $\ge 33^\circ C$ gợi ý bù 300ml nước).

- [ ] **Step 1: Dựng layout `fragment_home.xml`**
  Bố cục theo chuẩn `05_home.md`: Thẻ BMI, Thẻ Khí tượng & AQI, Thẻ Gợi ý chăm sóc, Spinner chọn 5 tỉnh thành phố lớn.
- [ ] **Step 2: Hiện thực logic nạp dữ liệu trong `HomeFragment.java`**
  Đọc cân nặng mới nhất tính BMI; gọi `WeatherClient` lấy thời tiết; nếu nhiệt độ cao hiển thị thông điệp khích lệ bù nước; nếu mất mạng lấy cache từ SharedPreferences hiện nhãn "Ngoại tuyến".
- [ ] **Step 3: Kiểm thử chế độ Máy bay (Airplane Mode)**
  Bật chế độ máy bay, mở app, xác nhận HomeFragment vẫn hiển thị dữ liệu cache an toàn không bị dừng đột ngột.
- [ ] **Step 4: Commit**
  `git commit -m "feat: implement HomeFragment with BMI summary, weather AQI and heuristic advice"`

---

# Phase 7: Tích hợp Toàn diện, Khung Host MainActivity & Kiểm thử Hệ thống

### Task 7.1: Giao diện Khung Host MainActivity & BottomNavigationView
**Files:**
- Create: `app/src/main/res/menu/bottom_nav_menu.xml`
- Create: `app/src/main/res/menu/main_toolbar_menu.xml`
- Create: `app/src/main/res/layout/activity_main.xml`
- Create: `app/src/main/java/vn/edu/tdmu/vita/activities/MainActivity.java`
- Ref: [`design-system/vita/pages/04_main.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/design-system/vita/pages/04_main.md)

**Interfaces:**
- Consumes: `HomeFragment`, `HealthFragment`, `ReminderFragment`, `StatsFragment`
- Produces: Khung chứa 4 Tabs điều hướng đáy; Toolbar Menu mở `ProfileActivity`.

- [ ] **Step 1: Dựng menu và layout `activity_main.xml`**
  Cấu hình `BottomNavigationView` cao 56dp với 4 Tab có đầy đủ nhãn chữ (`app:labelVisibilityMode="labeled"`).
- [ ] **Step 2: Hiện thực điều hướng Fragment trong `MainActivity.java`**
  Sử dụng `FragmentManager` và `FragmentTransaction.replace()`; gắn listener icon Toolbar góc phải mở `ProfileActivity`.
- [ ] **Step 3: Kiểm thử chuyển đổi qua lại giữa 4 Tabs**
  Xác nhận chuyển tab mượt mà, không bị giật lag và dữ liệu được nạp đúng.
- [ ] **Step 4: Commit**
  `git commit -m "feat: implement MainActivity hosting BottomNavigationView and 4 fragments"`

---

### Task 7.2: Cấu hình AndroidManifest, Quyền Hệ thống & Kiểm thử Ma trận (Test Matrix)
**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Test: `app/src/androidTest/java/vn/edu/tdmu/vita/VitaSystemIntegrationTest.java`
- Ref: [`docs/superpowers/specs/2026-10-09-vita-mobile-design.md`](file:///d:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/docs/superpowers/specs/2026-10-09-vita-mobile-design.md) (Mục 6: Ma trận Kiểm thử)

**Interfaces:**
- Produces: Khai báo đầy đủ 10 Activities, 3 Receivers, quyền `INTERNET`, `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS` (Android 13+), `SCHEDULE_EXACT_ALARM`.

- [ ] **Step 1: Cấu hình `AndroidManifest.xml`**
  Khai báo toàn bộ 10 Activity, 3 BroadcastReceiver (`MedicineAlarmReceiver`, `NotificationActionReceiver`, `BootReceiver`), App Icon mipmap, và các permissions tối thiểu.
- [ ] **Step 2: Viết test kịch bản tích hợp hệ thống (Integration Test)**
  Kiểm thử chuỗi luồng người dùng: Đăng ký -> Đăng nhập -> Thêm chỉ số sức khỏe -> Đặt lịch thuốc -> Nạp nước -> Kiểm tra biểu đồ.
- [ ] **Step 3: Chạy toàn bộ bộ kiểm thử Ma trận (Test Matrix)**
  Xác nhận 10 kịch bản kiểm thử trong bảng Test Matrix đều đạt trạng thái **PASS**.
- [ ] **Step 4: Đóng gói và kiểm tra file APK**
  Run: `./gradlew assembleDebug`
  Expected: BUILD SUCCESSFUL, sinh tệp `app-debug.apk` sẵn sàng cài đặt trên thiết bị thật.
- [ ] **Step 5: Commit**
  `git commit -m "feat: finalize AndroidManifest and verify comprehensive test matrix"`

---

## Plan Self-Review & Verification

1. **Spec Coverage:**
   - Đủ 10 chức năng cốt lõi: Đăng ký/đăng nhập (Task 1.4, 2.3), Hồ sơ (Task 2.4), Chỉ số (Task 3.2, 3.3), Lịch sử (Task 3.3, 3.4), BMI (Task 3.1), Nhắc thuốc (Task 4.1, 4.2), Nước (Task 4.3), Giấc ngủ (Task 5.1), Biểu đồ (Task 5.2), Thời tiết & AQI (Task 6.1, 6.2).
   - Đủ 14 màn hình (10 Activities + 4 Fragments).
   - 3 điểm nhấn kỹ thuật đều có task riêng: Nhắc thuốc xác nhận (Task 4.1), Thời tiết ngoại tuyến (Task 6.1), Biểu đồ song song (Task 5.2).
2. **Step Scan:** Mọi bước đều có hành động rõ ràng, lệnh chạy kiểm thử và kết quả kỳ vọng, không có bước chung chung "handle edge cases".
3. **Type Consistency:** Tên DAO, bảng SQLite, model POJO và Activity/Fragment đồng bộ tuyệt đối giữa Spec, Design System và các Task.
4. **No Code Rule:** Không triển khai viết code thực thi trước khi được phê duyệt kế hoạch.
