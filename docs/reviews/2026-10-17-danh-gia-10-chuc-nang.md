# Đánh giá 10 nhóm chức năng ViTa

Ngày đánh giá và kiểm tra nguồn tham chiếu: **17/10/2026**, múi giờ Việt Nam. Mã nguồn được khảo sát tại commit `9dcf9e6` (nhánh làm việc sạch trước khi tạo báo cáo này).

## Kết luận chính

ViTa có đủ luồng xử lý cho **10 nhóm chức năng trong đặc tả**. Không nhóm nào trong danh sách này chỉ là giao diện trống. Tuy nhiên, “có triển khai” khác với “hoàn thiện để phát hành”: vẫn có lỗi diễn giải chỉ số, thiếu sót về vòng đời màn hình, thông báo và khả năng tiếp cận.

Trong phạm vi đồ án, việc kết hợp nhật ký chỉ số, nhiều cữ thuốc, nước, ngủ và biểu đồ là hợp lý. Giá trị của ViTa nằm ở quy trình chăm sóc hằng ngày bằng tiếng Việt, dữ liệu chính hoạt động ngoại tuyến. Chưa có bằng chứng để khẳng định chức năng độc quyền, hiệu quả điều trị hoặc độ tin cậy ngang sản phẩm thương mại.

Không sửa mã nguồn trong lượt đánh giá. Chỉ tạo báo cáo này và chạy các tác vụ kiểm tra.

## 1. Phạm vi và bằng chứng xác minh

Đã đọc các lớp ứng dụng thuộc activities, fragments, adapters, database, models, receivers, network, utils; cấu hình Gradle/manifest, layout/menu, tài nguyên chữ/màu/theme, nguồn kiểm thử và tài liệu kỹ thuật liên quan. Tổng cộng có **48 file Java ứng dụng, 16 file unit test, 8 file Android test**. Không tính thư viện, build sinh tự động và công cụ agent là chức năng sản phẩm.

### Kết quả thực sự chạy trong lượt này

- `scripts/gradle.ps1 -Tasks @(':app:testDebugUnitTest', ':app:assembleDebug', ':app:lintDebug')`: **BUILD SUCCESSFUL**. Build debug dùng các đầu ra được Gradle xác nhận UP-TO-DATE; lint thực sự thực thi.
- Chạy tiếp `scripts/gradle.ps1 -Tasks @(':app:testDebugUnitTest', '--rerun-tasks')`: **BUILD SUCCESSFUL**, 22 task được thực thi. Đọc XML kết quả: **77 test, 0 failure, 0 error**.
- Lint SARIF: **0 error, 74 warning**. Trong đó có 23 UnusedResources, 15 Overdraw, 8 KeyboardInaccessibleWidget, 7 HardcodedText, 5 SetTextI18n, 3 NotifyDataSetChanged và 2 LabelFor. Cảnh báo không đồng nghĩa tất cả đều là lỗi người dùng nhìn thấy.
- Tính độ tương phản trực tiếp từ mã màu sRGB trong tài nguyên, không dựa vào số công bố trong tài liệu.

### Giới hạn

Không chạy Android instrumentation/UI test trong lượt này; một số fixture có `deleteDatabase`, nên không chạy chúng trên gói ứng dụng chứa dữ liệu thật. Kết quả emulator ghi trong tài liệu cũ được xem là lịch sử, không phải kết quả mới. Chưa đo thời gian khởi động, độ mượt, pin, RAM; chưa kiểm tra TalkBack, chữ lớn, xoay màn hình, Doze, reboot, máy OEM và việc chuyển thiết bị bằng thực nghiệm. Chưa kiểm định y khoa độc lập toàn bộ ngưỡng sức khỏe. File Word mẫu báo cáo/đề cương không được dùng để khẳng định chức năng đã hiện thực.

Các tình huống lỗi dưới đây được xác định từ luồng code, trừ khi có ghi rõ đã chạy hàm hoặc phép tính. Không gọi một tình huống UI là “đã tái hiện trên máy” nếu chưa thực hiện.

## 2. Bản đồ sản phẩm

- **Mục tiêu:** ghi nhật ký và hỗ trợ duy trì việc chăm sóc sức khỏe tại nhà. Nhóm người dùng trong đặc tả gồm người quan tâm sức khỏe, người lớn tuổi và người theo dõi bệnh mạn tính; chưa thấy cơ chế giới hạn tuổi tương ứng với từng thuật toán.
- **Công nghệ:** Android native Java/XML, Java source 17, minSdk 24, targetSdk 34, compileSdk 36; AndroidX, Material Components, MPAndroidChart 3.1.0. Gradle daemon dùng cấu hình toolchain riêng; không đồng nhất “source Java 17” với phiên bản JVM chạy Gradle.
- **Kiến trúc:** Activity/Fragment điều phối giao diện và nghiệp vụ; DAO truy cập SQLite; tiện ích chia sẻ cho BMI, biểu đồ, lịch thuốc và nước. Mô hình tinh gọn phù hợp nhóm sinh viên, nhưng một phần thao tác SQLite đang chạy đồng bộ trên UI thread.
- **Dữ liệu:** 6 bảng users, health_records, medicines, medicine_logs, water_logs, sleep_logs; database version 4; khóa ngoại, xóa liên đới, chỉ mục theo người dùng/ngày, nâng cấp từng bước. `medicine_logs` phân biệt thuốc/ngày/giờ; ghi trạng thái dùng transaction.
- **Màn hình:** 10 Activity (Splash, Login, Register, Main, Profile, EditProfile, AddHealth, DetailHealth, AddMedicine, AddSleep), 4 Fragment (Home, Health, Reminder, Stats). Đây là 14 thành phần màn hình, không phải 14 nhóm chức năng.
- **Luồng:** đăng ký/đăng nhập → Main → 4 tab; từ tab mở form thêm/sửa; hồ sơ ở toolbar; thông báo thuốc đưa về tab Nhắc nhở.
- **Quyền:** Internet, trạng thái mạng, thông báo, báo thức chính xác, nhận boot. Không yêu cầu GPS trong manifest; thời tiết dùng 5 vị trí cố định.
- **API:** hai endpoint forecast và air-quality của Open-Meteo; HttpURLConnection, JSON, ExecutorService/Handler; bộ nhớ đệm theo thành phố.
- **Ngoại tuyến:** tài khoản, chỉ số, thuốc, nước, ngủ và biểu đồ dùng dữ liệu cục bộ; thời tiết chỉ còn dữ liệu đã lưu nếu có. Ngoại tuyến không có nghĩa toàn bộ app không dùng Internet.

## 3. Danh mục bằng chứng mã nguồn

Các mã E dưới đây được dùng trong từng nhận xét để tránh lặp đường dẫn dài. Tên phương thức nêu kèm là điểm cần đọc.

| Mã | File và nội dung liên quan |
|---|---|
| E01 | [LoginActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/LoginActivity.java): `handleLogin` |
| E02 | [RegisterActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/RegisterActivity.java): `handleRegister` |
| E03 | [UserDao.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/database/UserDao.java): `register`, `login`, `updateProfile`, `updateBmiStandard` |
| E04 | [SecurityUtils.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/SecurityUtils.java): `generateSalt`, `hashPassword` |
| E05 | [SessionManager.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/SessionManager.java): lưu phiên, `matchesUser` |
| E06 | [EditProfileActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/EditProfileActivity.java): `handleSave`, đăng xuất/hủy nhắc |
| E07 | [ProfileActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/ProfileActivity.java): `renderBmiCard`, chọn chuẩn |
| E08 | [AddHealthActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/AddHealthActivity.java): `loadIntentData`, `handleSave`, `onSaveInstanceState` |
| E09 | [HealthDao.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/database/HealthDao.java): NULL, giờ đo, lọc/sắp xếp, cân nặng mới nhất |
| E10 | [HealthFragment.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/fragments/HealthFragment.java): khoảng ngày, context menu |
| E11 | [DetailHealthActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/DetailHealthActivity.java): `displayRecord` |
| E12 | [HealthEvaluator.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/HealthEvaluator.java): phân loại và lời khuyên huyết áp/nhịp tim |
| E13 | [BMICalculator.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/BMICalculator.java): `calculateAndClassify`, `classify`, `currentWeight` |
| E14 | [AddMedicineActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/AddMedicineActivity.java): chip giờ, ngày lặp, hủy lịch cũ |
| E15 | [MedicineSchedule.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/MedicineSchedule.java): chuẩn hóa, loại trùng và tính lịch |
| E16 | [MedicineAlarmReceiver.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/receivers/MedicineAlarmReceiver.java): lịch độc lập, exact/inexact fallback |
| E17 | [MedicineDao.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/database/MedicineDao.java): `logMedicineStatus`, `getLogForDose`, `getHistory` |
| E18 | [ReminderFragment.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/fragments/ReminderFragment.java): quyền thông báo, `handleQuickLog`, `handleQuickAddWater` |
| E19 | [MedicineAdapter.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/adapters/MedicineAdapter.java): từng dòng cữ và trạng thái |
| E20 | [NotificationHelper.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/NotificationHelper.java), [NotificationActionReceiver.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/receivers/NotificationActionReceiver.java): định danh và xác nhận cữ |
| E21 | [WaterReminderReceiver.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/receivers/WaterReminderReceiver.java), [WaterReminderPolicy.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/WaterReminderPolicy.java): khung giờ và kiểm tra mục tiêu |
| E22 | [HydrationGoal.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/HydrationGoal.java), [WaterDao.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/database/WaterDao.java): mục tiêu, nhật ký và `deleteLog` |
| E23 | [AddSleepActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/AddSleepActivity.java), [DateTimeUtils.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/DateTimeUtils.java): giờ ngủ, thời lượng, ngày bắt đầu |
| E24 | [SleepDao.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/database/SleepDao.java), [SleepEvaluator.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/SleepEvaluator.java): lưu và phân loại thời lượng |
| E25 | [StatsFragment.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/fragments/StatsFragment.java), [HealthChartData.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/utils/HealthChartData.java): 6 biểu đồ, vị trí theo giờ và cộng giấc ngủ |
| E26 | [WeatherClient.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/network/WeatherClient.java), [HomeFragment.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/fragments/HomeFragment.java): API, cache, lỗi, lời khuyên |
| E27 | [DatabaseHelper.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/database/DatabaseHelper.java): version 4, DDL và migration |
| E28 | [AndroidManifest.xml](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/AndroidManifest.xml), [backup_rules.xml](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/res/xml/backup_rules.xml), [data_extraction_rules.xml](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/res/xml/data_extraction_rules.xml): quyền và chính sách sao lưu |
| E29 | [colors.xml](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/res/values/colors.xml), [themes.xml](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/res/values/themes.xml), [strings.xml](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/res/values/strings.xml): màu, chữ và thông điệp |
| E30 | [MainActivity.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/activities/MainActivity.java), [BootReceiver.java](D:/Cong_Nghe_Thong_Tin/School/Phat_trien_ung_dung_di_dong/Do_An/Vita/Vita_Project/app/src/main/java/vn/edu/tdmu/vita/receivers/BootReceiver.java): điều hướng, khôi phục nhắc |

### Bảng chức năng thực tế

“Đã triển khai luồng chính” nghĩa là có giao diện, xử lý và lưu/đọc dữ liệu trong code; không đồng nghĩa toàn bộ tình huống thiết bị đã được xác minh.

| Chức năng | Mục đích | Trạng thái triển khai | File/màn hình | Bằng chứng và giới hạn |
|---|---|---|---|---|
| 1. Tài khoản | Vào hồ sơ cục bộ đúng người | Đã triển khai luồng chính | E01–E05, E06 | Hash/salt, phiên, đăng xuất; chưa có khôi phục mật khẩu |
| 2. Hồ sơ | Thông số và cấu hình cá nhân | Đã triển khai luồng chính | E03, E06–E07 | Lưu SQLite; năm sinh bỏ trống bị thay bằng 2000 khi đăng ký |
| 3. Chỉ số | Ghi và đọc các lần đo | Đã triển khai; diễn giải còn thiếu | E08–E12 | Có giờ/bối cảnh, NULL; huyết áp thấp bị gắn nhãn tốt |
| 4. Lịch sử | Tra cứu, lọc, sửa/xóa | Đã triển khai luồng chính | E09–E11 | Khoảng ngày gồm hai biên; chưa phân trang/xuất |
| 5. BMI | Phép tính và phân loại | Đã triển khai; thiếu trường hợp biên | E07, E13, E26 | Dùng cân mới nhất; thiếu độ III nhánh WHO |
| 6. Thuốc | Nhắc và ghi từng cữ | Đã triển khai luồng chính | E14–E20, E30 | Nhiều giờ/thuốc, lịch sử; chưa xác minh báo thức thực tế lượt này |
| 7. Nước | Nhật ký và nhắc có cấu hình | Đã triển khai luồng chính | E06, E18, E21–E22 | Khung giờ/mục tiêu/opt-in; chưa có UI hoàn tác nhật ký |
| 8. Ngủ | Ghi giờ và thời lượng | Đã triển khai; trạng thái form còn thiếu | E23–E25 | Qua đêm, CRUD; chưa giữ các biến form khi Activity tái tạo |
| 9. Biểu đồ | Xem xu hướng | Đã triển khai luồng chính | E25 | 6 biểu đồ 7/30 ngày; ngày không ghi ngủ/nước được biểu diễn bằng 0 |
| 10. Môi trường | Thời tiết, AQI, gợi ý | Đã triển khai luồng chính | E26, E22 | Cache theo thành phố; lỗi mạng chưa có trạng thái lời khuyên riêng |

Chưa thấy xuất báo cáo, sao lưu/khôi phục chủ động, Health Connect/Bluetooth, chia sẻ người chăm sóc. Đây là phần chưa có, không phải lỗi chỉ vì sản phẩm thương mại có chúng.

## 4. Ứng dụng tham chiếu và nguồn

Chọn **5 ứng dụng theo từng nhiệm vụ**, không ép ViTa cạnh tranh toàn bộ với một hệ sinh thái. Chỉ dùng tính năng công khai; không suy đoán database, kiến trúc, cách băm mật khẩu hoặc thuật toán bí mật của đối thủ.

| App | Vì sao chọn | Thông tin xác minh và giới hạn |
|---|---|---|
| Samsung Health | Hồ sơ, tổng quan, giấc ngủ | Có nền tảng tổng hợp hoạt động/ngủ/dinh dưỡng/sinh hiệu; một số phép đo cần wearable tương thích. App công bố tải miễn phí; tính năng phụ thuộc thị trường/thiết bị. [Trang chính thức](https://www.samsung.com/us/apps/samsung-health/) |
| OMRON connect | Chỉ số, lịch sử, biểu đồ | Trang EMEA công bố dashboard và báo cáo PDF/CSV trong nhóm miễn phí; phân tích nâng cao/nhắc thuốc thuộc Premium. Khả năng kết nối tùy thiết bị và khu vực. [Trang chính thức](https://www.omron-healthcare.com/omronconnect) |
| Medisafe | Lịch thuốc và dữ liệu tài khoản | Có lịch thuốc và Medfriend hỗ trợ người chăm sóc. Thông báo chính thức áp dụng thuê bao ngoài Mỹ từ 01/01/2026; không coi là đối thủ miễn phí tại Việt Nam. [Medfriend](https://help-center.medisafe.com/en/articles/8395236-all-about-medfriends), [chính sách trả phí](https://help-center.medisafe.com/en/articles/13052285-medisafe-is-now-a-fully-paid-app-for-users-outside-the-u-s) |
| WaterMinder | Nước và cá nhân hóa mục tiêu | Công bố nhắc dừng khi đạt mục tiêu, mục tiêu theo thời tiết, nhiều loại đồ uống; có Android và hệ sinh thái Apple. Không mặc định mọi tính năng Apple Watch có trên Android; chưa xác minh đầy đủ paywall Android tại Việt Nam. [Trang chính thức](https://waterminder.com/) |
| IQAir AirVisual | AQI và thời tiết | Có thông tin hiện tại, dự báo và cảnh báo AQI; dùng để đối chiếu độ sâu thông tin môi trường. Chưa xác minh từng tính năng trả phí theo khu vực. [Trang chính thức](https://www.iqair.com/in-en/learn-about-air-quality) |

Các bảng chức năng dưới đây tham chiếu các nguồn này. Với tính năng không có bằng chứng công khai cụ thể, ghi chưa xác minh thay vì suy ra có/không.

## 5. Đánh giá lần lượt

Ưu tiên: **P0** nghiêm trọng cần xử lý ngay; **P1** trước demo/phát hành tùy điều kiện nêu rõ; **P2** nâng chất lượng; **P3** mở rộng tùy chọn. Chưa có bằng chứng đủ để gán P0 cho dự án.

### 1. Đăng ký, đăng nhập, đăng xuất và lưu phiên

**Hoạt động:** form nhận username/password và thông số ban đầu; E03 lưu hash/salt; E01 kiểm tra mật khẩu; E05 giữ phiên. Đăng xuất E06 hủy lịch thuốc, nước và thông báo rồi xóa phiên. Dữ liệu nhật ký không bị xóa khi đăng xuất.

**Điểm tốt:** không lưu mật khẩu rõ; salt 16 byte từ SecureRandom; truy vấn dùng tham số. Đăng xuất xử lý cả nhắc nền, tránh chỉ thay màn hình trong khi thông báo vẫn tác động tài khoản cũ. Luồng offline phù hợp đồ án.

**Hạn chế:** E04 chỉ băm SHA-256 một lần. Salt ngăn dùng chung bảng băm nhưng không làm việc dò mật khẩu chậm đáng kể nếu database bị lấy. Đây là khoảng cách bảo mật trước phát hành, không phải bằng chứng app đang bị tấn công. OWASP khuyến cáo hàm chuyên dụng như Argon2id/PBKDF2 thay cho hash nhanh. [Nguồn](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)

Chưa có đổi/quên mật khẩu; “tài khoản” thực chất gắn với dữ liệu trên máy, nên cần giải thích để tránh kỳ vọng đăng nhập máy khác sẽ khôi phục dữ liệu. Không cần thêm OTP/email chỉ để mô phỏng dịch vụ cloud.

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Quản lý dữ liệu tài khoản | Hồ sơ SQLite, phiên trên máy | Medisafe có xuất và xóa dữ liệu trong hướng dẫn quản lý tài khoản | Không phụ thuộc mạng cho đăng nhập cục bộ | Chưa có quản lý xuất/xóa toàn bộ trong UI | E01–E06; chính sách Medisafe ở mục 4 |

**Cải thiện:** P1 trước phát hành, độ khó trung bình: đổi sang cơ chế dẫn xuất mật khẩu chậm phù hợp Android hỗ trợ; lưu phiên bản hash và nâng cấp khi người dùng đăng nhập thành công, không làm mất tài khoản cũ. Kiểm tra: tài khoản cũ vẫn vào được, hash được nâng, mật khẩu sai bị từ chối, thao tác không khóa UI. P2, thấp: ghi rõ tài khoản cục bộ và chính sách mất dữ liệu; người thử phải hiểu đúng việc đổi máy.

**Hai góc nhìn:** đồ án đáp ứng luồng xác thực; ứng dụng thật còn thiếu quản lý vòng đời tài khoản và bảo vệ mật khẩu đủ mạnh.

### 2. Hồ sơ và cấu hình cá nhân

**Hoạt động:** E07 đọc thông số; E06 sửa họ tên, năm sinh, giới tính, chiều cao, cân cơ sở, mục tiêu nước, khung giờ, lượng uống và tùy chọn thời tiết; E03 ghi lại. Chuẩn BMI đổi riêng từ Profile.

**Điểm tốt:** kiểm tra khoảng giá trị; giữ mục tiêu cá nhân khi áp dụng gợi ý thời tiết; lưu cấu hình theo từng user. Phân biệt cân nặng ban đầu với cân nặng nhật ký mới nhất là đúng về mô hình dữ liệu.

**Lỗi cụ thể:** E02 `setBirthYear(birthYear > 0 ? birthYear : 2000)` biến thông tin bỏ trống thành một năm sinh bịa đặt. E07 sau đó hiển thị tuổi như dữ liệu thật. Ví dụ đăng ký không nhập năm sinh → hồ sơ hiện sinh năm 2000. Mức P1 trước demo nếu trình diễn trường tùy chọn.

**UX:** cài đặt nước nằm trong form sửa toàn bộ hồ sơ và vẫn đòi chiều cao/cân nặng; nút từ Nhắc nhở mở form này. Dùng được, nhưng chưa phải tác vụ nước độc lập. Chưa có xóa hồ sơ và dữ liệu chủ động; đăng xuất không phải xóa dữ liệu (E03, E06, E27).

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Vai trò hồ sơ | Cấu hình cho nhật ký tại nhà | Samsung Health tổ chức dữ liệu nhiều nhóm sức khỏe | Ít thành phần cần học | Chưa có hệ kết nối rộng; không phải yêu cầu bắt buộc đồ án | E03/E06/E07; Samsung mục 4 |

**Cải thiện:** P1, thấp: giữ năm sinh chưa biết bằng giá trị trống/0 và hiển thị “Chưa cập nhật”, không suy đoán lại dữ liệu người dùng cũ. Kiểm tra đăng ký bỏ trống, có năm hợp lệ và năm vượt giới hạn. P2, trung bình: xóa tài khoản có xác nhận và xóa liên đới đúng user; kiểm tra tài khoản khác nguyên vẹn, lịch và thông báo của user bị xóa được hủy.

**Hai góc nhìn:** đủ cấu hình cần thiết cho đồ án; tính trung thực dữ liệu tùy chọn cần sửa trước khi mở rộng hồ sơ.

### 3. Ghi nhận và diễn giải chỉ số sức khỏe

**Hoạt động:** E08 nhận ngày/giờ, các chỉ số không bắt buộc, bối cảnh đường huyết và ghi chú; kiểm tra số/quan hệ huyết áp rồi E09 lưu NULL cho trường trống. E11/E12 hiển thị và phân loại huyết áp, nhịp tim. App ghi chỉ số do người dùng cung cấp, không tự đo huyết áp/đường huyết bằng điện thoại.

**Điểm tốt:** không buộc đo tất cả; giữ giờ và bối cảnh thay vì gộp cả ngày; đơn vị hiện rõ; hiển thị có điều kiện tránh số 0 giả cho trường không đo. Dữ liệu có thể được xem lại/sửa.

**Lỗi quan trọng:** E12 xếp mọi cặp dưới 120/80 vào “Bình thường tối ưu”. Đầu vào hợp lệ 80/50 đi qua E08 và được gán `STATUS_GOOD`, lời khuyên “Huyết áp ổn định…”. Đây là thiếu nhánh huyết áp thấp, không phải vấn đề chỉ thiếu tính năng. AHA mô tả huyết áp dưới 90/60 là huyết áp thấp và lưu ý cần xét triệu chứng; không được thay bằng lời trấn an tự động. [Nguồn](https://www.heart.org/en/health-topics/high-blood-pressure/the-facts-about-high-blood-pressure/low-blood-pressure-when-blood-pressure-is-too-low)

E08 cho nhập một thành phần huyết áp, trong khi E11 và adapter chỉ hiện cặp đầy đủ: nhập riêng tâm thu có thể lưu được nhưng không hiện giá trị trong phần huyết áp. Đây là bất nhất giữa đầu vào và hiển thị, P2. Đường huyết hiện dùng mg/dL; chuyển mmol/L là mở rộng tùy nhu cầu, không phải lỗi công thức hiện tại.

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Nguồn lần đo | Nhập tay, có giờ/bối cảnh | OMRON connect tích hợp thiết bị tương thích | Không buộc mua thiết bị kết nối | Dễ nhập nhầm, chưa tự nhận dữ liệu | E08/E09; OMRON mục 4 |

**Cải thiện:** P1, trung bình: thêm nhánh thấp và rà soát hệ ngưỡng/nguồn/phiên bản, lời khuyên trung tính theo tình huống; kiểm tra 80/50 không còn được gọi tối ưu, cùng các biên phân loại hiện hành. P2, thấp: hoặc yêu cầu đủ cặp huyết áp, hoặc hiện thành phần còn lại là chưa đo; nhập riêng tâm thu phải có kết quả nhất quán. Không tự chẩn đoán bệnh từ một lần đo.

**Hai góc nhìn:** nhật ký có độ sâu phù hợp đồ án; độ chính xác lời diễn giải đang là điểm cần ưu tiên hơn thêm chỉ số mới.

### 4. Lịch sử, lọc và xem chi tiết

**Hoạt động:** E10 tải danh sách đúng user, mở khoảng ngày bằng MaterialDatePicker, đổi mốc UTC sang chuỗi ngày; E09 lọc gồm hai biên và sắp xếp ngày/giờ/id; E11 xem chi tiết. Nhấn giữ để sửa/xóa, xóa có xác nhận.

**Điểm tốt:** có bộ lọc thật ở truy vấn SQLite; không chỉ lọc giả trên giao diện. Trường trống được xử lý; bản ghi nhiều lần trong ngày có thứ tự. HealthFragment lưu lại khoảng lọc khi Activity tái tạo.

**Hạn chế:** tải toàn bộ lịch sử và `notifyDataSetChanged` trên UI thread, nên rủi ro tăng khi tích lũy nhiều dữ liệu; chưa có đo đạc để khẳng định hiện đang chậm. Sửa/xóa chủ yếu qua nhấn giữ khó phát hiện với người mới. E11 đọc theo id chưa kiểm tra user hiện hành ngay trong `loadAndDisplay`; E08 kiểm tra owner lúc nạp nhưng chưa lặp ở nhánh lưu. Đây là đề xuất phòng vệ P2; Activity không exported nên chưa chứng minh được đường khai thác từ ứng dụng ngoài.

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Sử dụng lịch sử ngoài app | Đọc/lọc/sửa trên máy | OMRON EMEA công bố xuất PDF/CSV | Luồng tra cứu cơ bản đầy đủ | Chưa mang báo cáo đến buổi khám được thuận tiện | E09–E11; OMRON mục 4 |

**Cải thiện:** P2, thấp: bổ sung nút menu nhìn thấy và owner check nhất quán ở đọc/ghi/xóa; kiểm tra id user khác bị từ chối mà dữ liệu không đổi. P2, trung bình: xuất CSV trước PDF; chỉ xuất phạm vi và người dùng đã chọn, dùng luồng chia sẻ có chủ đích. Với hiệu năng, đo danh sách 10.000 bản ghi rồi mới quyết định phân trang; không thêm lớp trừu tượng chỉ để chuẩn bị giả định.

**Hai góc nhìn:** đủ nội dung RecyclerView/context menu/SQL cho đồ án; xuất dữ liệu và khả năng dùng với lịch sử dài là khoảng cách thực tế.

### 5. BMI tự động và lựa chọn chuẩn

**Hoạt động:** E13 lấy cân mới nhất từ E09, thiếu thì dùng cân cơ sở; chiều cao từ hồ sơ; tính BMI và phân loại theo lựa chọn. E07 và E26 dùng chung helper; phân loại dùng giá trị chưa làm tròn.

**Điểm tốt:** dùng chung thuật toán tránh hai màn hình cho kết quả khác; ưu tiên cân thực tế mới nhất; phân loại trước khi làm tròn tránh sai ở biên 23/25. Có test các biên chính và cân fallback.

**Lỗi:** trong nhánh WHO, BMI từ 35 trở lên đều trả “Béo phì độ II”; không có nhánh từ 40 trở lên cho độ III. CDC công bố phân loại người lớn 35–<40 là class 2, từ 40 là class 3, và nhấn mạnh BMI chỉ là công cụ sàng lọc. [Nguồn](https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html)

**Phạm vi:** thuật toán không nhận tuổi, nhưng đăng ký chấp nhận năm sinh trẻ em. Vì vậy chưa thể nói phân loại hợp với mọi nhóm tuổi. Không suy từ nhãn “WHO” rằng toàn bộ phân loại đã được kiểm định. Chuẩn châu Á cũng cần trích dẫn rõ tài liệu nhóm lựa chọn.

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Đặt BMI trong tổng quan | Chọn chuẩn, hiện phân loại và cân tham chiếu | Samsung Health đưa BMI trong tổng quan tim mạch trên trang sản phẩm | Giải thích trực tiếp bằng tiếng Việt | Độ sâu thuật toán chuẩn châu Á của Samsung: chưa xác minh; không kết luận ViTa chính xác hơn | E07/E13/E26; Samsung mục 4 |

**Cải thiện:** P1, thấp: sửa nhánh độ III và thêm test 34,99/35/39,99/40/45. P1 về diễn giải, thấp–trung bình: ghi giới hạn áp dụng người lớn theo nguồn được chọn, không tự áp dụng phân loại đó cho trẻ em; test hồ sơ ngoài phạm vi phải có thông báo giới hạn thay vì nhãn người lớn. Chưa cần xây mô hình BMI trẻ em nếu ngoài đề tài.

**Hai góc nhìn:** cấu trúc và phép tính tốt cho đồ án; còn lỗi biên và giới hạn đối tượng cần sửa.

### 6. Quản lý thuốc, nhiều cữ và xác nhận

**Hoạt động:** E14 thêm thuốc/liều/ngày lặp và nhiều chip giờ; E15 sắp xếp, loại trùng; E16 tạo báo thức riêng theo thuốc/giờ, mang ngày/giờ dự kiến sang thông báo. E17 lưu từng cữ trong transaction; E18/E19 hiển thị trạng thái và lịch sử. E20 xác nhận trực tiếp từ thông báo; E30 có khôi phục sau boot.

**Điểm tốt:** đã giải quyết được nhiều giờ cho một thuốc; không phải tạo thuốc trùng. Hành động thông báo giữ ngày cũ nên không mặc định ghi sang hôm nay khi bấm muộn. Có kiểm tra chủ sở hữu, hủy lịch trước sửa/xóa, permission handling và fallback khi thiếu exact alarm. Đây là nhóm có giá trị kỹ thuật nổi bật của đồ án.

**Bất nhất P1:** E18 `handleQuickLog` lưu trạng thái nhưng không hủy thông báo tương ứng E20. Nếu thông báo cữ đó đang còn, bấm “Đã uống” trong app vẫn để lại thông báo; bấm “Bỏ qua” trên thông báo cũ sau đó có thể ghi đè trạng thái qua E17. Chưa tái hiện UI lượt này, nhưng đường xử lý code có đủ các bước trên.

**Giới hạn P2/P3:** adapter hiển thị mọi cữ cùng trạng thái “Hôm nay”, không xét ngày lặp tại `bindDoses`; cần phân biệt hôm nay không có lịch với chưa uống. Chưa thấy receiver cho đổi múi giờ/giờ máy/cấp lại exact alarm. Có BOOT_COMPLETED không chứng minh tất cả thay đổi hệ thống đều được xử lý. Android quy định exact alarm có quyền riêng và có thể bị thu hồi. [Nguồn](https://developer.android.com/develop/background-work/services/alarms)

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Theo dõi uống thuốc | Nhiều giờ, xác nhận từng cữ, lịch sử | Medisafe có hỗ trợ người chăm sóc qua Medfriend | Đủ quy trình cá nhân tại nhà | Chưa có liên kết người chăm sóc; không bắt buộc trong phạm vi offline | E14–E20; Medfriend mục 4 |

**Cải thiện:** P1, thấp: khi ghi nhanh trong app thành công, hủy đúng notification theo thuốc/ngày/giờ; giữ cho phép sửa lịch sử một cách có chủ đích. Kiểm tra notification biến mất và không tác động cữ khác. P2, trung bình: phân biệt cữ có lịch hôm nay và xử lý các sự kiện đổi giờ/quyền; kiểm tra lịch tuần, đổi múi giờ và thu hồi/cấp lại quyền trên emulator. P3, cao: người chăm sóc/cloud chỉ thêm khi thực sự có nhu cầu và quyền chia sẻ.

**Hai góc nhìn:** mạnh về kỹ thuật môn Android; còn cần kiểm thử thiết bị trước khi hứa nhắc đúng giờ trong sử dụng thật.

### 7. Nhật ký và nhắc uống nước

**Hoạt động:** E06 lưu mục tiêu, khung giờ, lượng uống mỗi lần, opt-in thời tiết. E18/E21 ghi nhật ký từ app/thông báo; E21 kiểm tra khung giờ và tổng đã uống trước khi hiện nhắc; E22 chỉ cộng 300 ml khi bật điều chỉnh và có dữ liệu nóng đúng ngày/thành phố.

**Điểm tốt:** không cộng dồn 300 ml qua mỗi lần mở app; không ghi đè mục tiêu cơ bản; mặc định opt-in tắt. Có dừng hiển thị nhắc khi đạt mục tiêu, hỗ trợ khung giờ qua đêm, lượng ghi nhanh theo hồ sơ và hướng dẫn hạn chế dịch trong UI. Đây là tiến bộ rõ so với bản cũ chỉ có nút 250 ml và lịch lặp cố định.

**Hạn chế:** bấm nhầm một lần làm tổng tăng nhưng UI không có hoàn tác/sửa, dù E22 `WaterDao.deleteLog` đã tồn tại. Thời tiết là quy tắc gợi ý cố định, chưa phải phép tính nhu cầu dịch cá nhân được xác minh; không nên gọi là cá nhân hóa y khoa. Chu kỳ cơ bản vẫn khoảng 2 giờ, không có lựa chọn riêng; đây là giới hạn phạm vi chứ không mặc định là lỗi.

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Nhắc và mục tiêu | Khung giờ, dừng khi đủ, thời tiết opt-in | WaterMinder công bố cùng hướng, thêm nhiều loại đồ uống | Luồng gọn, tích hợp cùng thuốc/chỉ số | Chưa có undo UI, nhiều loại đồ uống; thời tiết không phải khác biệt độc quyền | E18/E21/E22; WaterMinder mục 4 |

**Cải thiện:** P2, thấp: Snackbar “Hoàn tác” dùng id bản ghi vừa tạo và `deleteLog`, sau đó cập nhật tổng; kiểm tra uống 350 ml rồi undo trở đúng số trước đó, không xóa bản ghi khác. P2, thấp: ghi rõ “gợi ý theo thời tiết” và giới hạn nguồn; test tắt opt-in, cache hôm qua, thành phố khác đều không tăng mục tiêu. P3: tùy chỉnh chu kỳ chỉ khi thử người dùng cho thấy cần.

**Hai góc nhìn:** khá hoàn chỉnh trong phạm vi nhật ký nước; ưu tiên sửa thao tác nhầm hơn thêm gamification.

### 8. Nhật ký giấc ngủ

**Hoạt động:** E23 nhận ngày thức, giờ ngủ/thức, tính thời lượng và lưu; giờ ngủ lớn hơn giờ thức được hiểu qua đêm. E24/E25 cho sửa/xóa, xem danh sách và tổng các phiên theo ngày thức. Chỉ phân loại bằng thời lượng, không đo giai đoạn ngủ.

**Điểm tốt:** xử lý qua nửa đêm và ngày đầu năm; phân biệt ngày bắt đầu/ngày thức; nhiều phiên trong cùng ngày được cộng cho biểu đồ. Không cần wearable nên ai cũng nhập được.

**Thiếu sót vòng đời P1:** `AddSleepActivity` có biến `selectedDate`, `sleepTime`, `wakeTime`; onCreate luôn nạp lại từ database hoặc mặc định 23:00–06:30, chưa có lưu/khôi phục các biến này từ savedInstanceState. Thay giờ rồi xoay màn hình/tái tạo Activity có thể mất dữ liệu đang nhập hoặc lệch với text Android tự phục hồi. Đây là kết luận từ code; cần test tái tạo màn hình để xác nhận biểu hiện cụ thể.

**Hạn chế P2:** danh sách `getLatest7Days` thực tế `LIMIT 7` bản ghi, không phải 7 ngày; nhiều phiên có thể làm thiếu các ngày gần đây. Chưa chặn phiên trùng/chồng, nên 23:00–07:00 nhập hai lần được cộng thành 16 giờ. Nhãn thời lượng không đủ chứng minh “chất lượng giấc ngủ”.

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Nguồn và độ sâu giấc ngủ | Giờ do người dùng nhập, tổng thời lượng | Samsung Health có dữ liệu ngủ sâu hơn khi dùng thiết bị tương thích | Không cần phần cứng | Không được nhận là đo REM/ngủ sâu/chất lượng thực | E23–E25; Samsung mục 4 |

**Cải thiện:** P1, thấp: lưu/khôi phục ngày/giờ form; test nhập 22:15–05:45, `ActivityScenario.recreate()`, lưu và đọc lại đúng 7,5 giờ. P2, trung bình: cảnh báo phiên trùng/chồng trước khi cộng tổng; test cùng phiên hai lần không âm thầm nhân đôi. P2, thấp: tên hiển thị theo “thời lượng” và “7 bản ghi gần nhất”, hoặc sửa truy vấn đúng 7 ngày.

**Hai góc nhìn:** đủ bài toán TimePicker và qua đêm cho đồ án; cần trung thực về cái app đo được và giữ dữ liệu nhập.

### 9. Thống kê và biểu đồ

**Hoạt động:** E25 truy vấn 7/30 ngày, vẽ cân nặng, nhịp tim, đường huyết, huyết áp hai đường, nước và ngủ. Điểm đo dùng phần lẻ theo giờ; marker có ngày/giờ/bối cảnh; nước có đường tham chiếu mục tiêu hôm nay.

**Điểm tốt:** đã có biểu đồ đường huyết; không gộp mất nhiều lần đo trong ngày. Huyết áp có nét liền/nét đứt, không chỉ dựa màu. Có đơn vị và trạng thái trống. Nhãn đường mục tiêu nói rõ là hôm nay, tránh giả định đó là mục tiêu của mọi ngày lịch sử.

**Hạn chế:** E25 thêm cột 0 cho ngày không có nhật ký ngủ/nước; marker không nói “chưa ghi nhận”. Người xem có thể hiểu không ngủ/không uống thay vì không nhập. Chữ trục 10sp nhỏ so với nhóm người dùng lớn tuổi; chưa có bảng dữ liệu tương đương cho người dùng trình đọc màn hình. Một số biểu đồ có contentDescription tiêu đề nhưng không thay thế việc đọc được từng điểm dữ liệu. 6 biểu đồ xếp dọc tăng thao tác cuộn, cần thử thực tế trước khi thiết kế lại.

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Từ dữ liệu đến hành động | Đồ thị từng nhóm, marker | OMRON có dashboard trung bình và báo cáo | Đủ kiểm tra xu hướng cơ bản | Chưa có tóm tắt/báo cáo; thiếu phân biệt không có dữ liệu | E25; OMRON mục 4 |

**Cải thiện:** P2, thấp–trung bình: marker “Chưa ghi nhận” hoặc cách biểu diễn khuyết riêng; test ngày không có dòng khác với ngày có dữ liệu. P2, trung bình: cung cấp danh sách/bảng đọc được cùng khoảng ngày, nhãn TalkBack có ngày/giờ/giá trị/đơn vị; kiểm tra người dùng đọc được thông tin không cần nhìn chart. P3: thêm chọn nhóm chỉ số thay vì cuộn cả 6, nếu thử nghiệm chứng minh có ích.

**Hai góc nhìn:** thể hiện tốt SQLite → xử lý → trực quan hóa; chưa nên gọi là hệ phân tích sức khỏe chuyên sâu.

### 10. Thời tiết, AQI và gợi ý chăm sóc

**Hoạt động:** E26 chọn một trong 5 thành phố, gọi API forecast và AQI trên worker, lưu cache; cập nhật UI qua main thread, bỏ callback cũ. AQI lỗi không làm mất toàn bộ kết quả nhiệt độ; mất mạng thì dùng cache tương ứng nếu có.

**Điểm tốt:** không cần quyền vị trí; có timeout; có nhãn offline và mốc tải cache; chống dữ liệu thành phố cũ đè UI. E22 không dùng cache ngày cũ để tăng mục tiêu nước.

**Lỗi trạng thái P1:** khi forecast lỗi và không có cache, `HomeFragment.onError` đặt lời khuyên `home_advice_good`, nội dung khẳng định “Hôm nay thời tiết dễ chịu…”. Không có dữ liệu không phải bằng chứng thời tiết dễ chịu. Nhiệt độ ≥33 khi opt-in tắt cũng có thể rơi vào lời khuyên chung đó nếu AQI không cao. Nên tách việc mô tả nóng khỏi quyết định có thay đổi mục tiêu nước hay không.

**Giới hạn:** cache hiển thị chưa có hạn tuổi tối đa; `updatedAt` lấy lúc app tải chứ không dùng `current.time` của API. Mốc “Cập nhật” cần nói rõ tải lúc nào và dữ liệu áp dụng lúc nào. AQI là dữ liệu mô hình theo vị trí, không phải đo không khí ngay tại nhà; tài liệu Open-Meteo nêu nguồn CAMS. Chưa thấy dòng ghi nguồn Open-Meteo/CAMS trong strings. [Tài liệu nguồn dữ liệu và ghi công](https://open-meteo.com/en/docs/air-quality-api)

| Tiêu chí | ViTa | Tham chiếu | Điểm tốt ViTa | Khoảng cách | Bằng chứng |
|---|---|---|---|---|---|
| Độ sâu môi trường | Hiện tại, 5 địa điểm, gợi ý | IQAir AirVisual có dự báo và cảnh báo AQI | Đủ làm ngữ cảnh cho chăm sóc tại nhà | Không có bản đồ/dự báo/cảnh báo chi tiết; không cần sao chép toàn bộ app chuyên môi trường | E26; IQAir mục 4 |

**Cải thiện:** P1, thấp: trạng thái lỗi nói “Chưa có dữ liệu thời tiết”, chỉ đưa lời nhắc chung không khẳng định điều kiện; test lỗi không cache và nóng khi opt-in tắt. P2, thấp–trung bình: ghi nguồn, thời gian dữ liệu/thời gian tải, cảnh báo dữ liệu cũ; kiểm tra đúng thành phố và cache hôm qua có nhãn rõ. P3: chỉ mở rộng địa điểm khi nhóm mục tiêu cần; không bắt buộc thêm GPS.

**Hai góc nhìn:** tích hợp API hợp lý và có xử lý offline; giá trị thực tế phụ thuộc việc diễn giải đúng dữ liệu, không nằm ở có API hay không.

## 6. Các vấn đề xuyên suốt

### Giao diện và khả năng tiếp cận

E29 dùng bảng màu thống nhất, chữ sp, nhiều nút có chiều cao 48dp; E19 bổ sung tên thuốc/giờ vào contentDescription cho nút xác nhận. Đây là bằng chứng tốt ở mức triển khai, chưa phải xác nhận mọi vùng chạm và TalkBack đã đạt chuẩn.

Tính theo công thức tương phản sRGB của WCAG từ màu khai báo:

| Cặp màu | Tỷ lệ tính được | Nhận xét |
|---|---|---|
| Trắng / teal #00897B | 4,317:1 | Dưới mốc 4,5:1 cho chữ thường; tài liệu công bố 4,6:1 là không đúng |
| Cam #F57C00 / nền #FFF3E0 | 2,466:1 | Nhãn cảnh báo nhỏ khó đọc |
| Vàng #FBC02D / trắng | 1,656:1 | Số AQI mức vừa phải dễ nhạt trên nền trắng |
| Chữ #1C1B1F / trắng | 17,130:1 | Chữ chính có tương phản cao |

Đây là phép tính trên màu phẳng khai báo, chưa đo screenshot hay màu theme khi compositing. Dùng WCAG làm mốc tham chiếu khả năng đọc, không tuyên bố chứng nhận toàn bộ app Android. [W3C](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum)

P1 cho chữ cảnh báo và P2 cho đồng bộ tài liệu: dùng biến thể màu chữ đậm hơn, đo lại trên nền thật. Không cần đổi toàn bộ nhận diện. Lint còn phát hiện 8 widget clickable không focusable và 2 thiếu labelFor; cần thử bàn phím/TalkBack, không đơn thuần thêm `focusable=true` một cách máy móc làm bật bàn phím cho ô chọn giờ.

### Vòng đời và quyền riêng tư dữ liệu

E28 hiện tắt Android Backup và loại trừ cloud/device transfer. Đây là cấu hình bảo vệ việc sao chép tự động; chưa thực nghiệm mọi OEM. Nó cũng làm việc khôi phục sau gỡ app/đổi máy khó hơn vì chưa có công cụ sao lưu chủ động. Đóng app và đăng xuất khác với gỡ app; không nên hứa dữ liệu tự quay lại khi đăng nhập máy mới.

Database SQLite không được mã hóa ở tầng ứng dụng trong code đã đọc. Android vẫn cung cấp sandbox/bảo vệ hệ thống; không suy ra người dùng bình thường hay app bất kỳ đọc được database. Nếu mô hình đe dọa gồm thiết bị bị chiếm quyền/trích xuất dữ liệu, cần thiết kế bảo vệ phù hợp. Mã hóa DB không thay thế hash mật khẩu đúng, owner check và quản lý khóa.

P2: cung cấp xuất dữ liệu và thông báo rõ hậu quả trước khi người dùng chuyển/gỡ app; cân nhắc backup do người dùng chủ động chọn, có bảo vệ phù hợp. Đây không phải lý do bật lại cloud backup mặc định trái mục tiêu riêng tư.

### Chất lượng mã và kiểm thử

DAO, helper tính toán và receiver được tách tương đối rõ; các màn hình cùng gọi BMICalculator/HydrationGoal/HealthChartData. Schema migration không drop toàn bộ dữ liệu. Đó là nền tảng tốt trong quy mô một người dùng trên một thiết bị.

77 test không tương đương 77 tình huống sản phẩm toàn diện. `BuildConfigurationTest` so sánh biến hằng do chính test khai báo, chưa thực sự đọc cấu hình build; `DaosTest` chủ yếu kiểm tra model, chưa chạy SQL thật. Một số kiểm thử SQLite/permission nằm trong androidTest và chưa chạy lượt này. Cần bổ sung test cho các lỗi cụ thể ở báo cáo trước khi tăng số test hình thức.

E09/E18/E25 thực hiện truy vấn và một số thao tác xử lý trên luồng giao diện; E18 có một truy vấn trạng thái cho mỗi cữ thuốc. Với dữ liệu nhỏ chưa có bằng chứng chậm; nếu nhiều thuốc/lịch sử dài, cần đo. Không đề xuất chuyển toàn bộ kiến trúc sang MVVM/Room chỉ để “đúng xu hướng”.

## 7. Chấm điểm có căn cứ

Đây là điểm đánh giá kỹ thuật tạm thời cho **bản hiện tại trong phạm vi đồ án**, không phải điểm nghiệm thu lâm sàng hoặc điểm giảng viên. Mốc 9–10 đòi luồng/biên đầy đủ và bằng chứng thực nghiệm mạnh; 7–8 có lõi tốt, còn khoảng trống; 5–6 có lỗi ảnh hưởng đáng kể. Không tính điểm tổng vì dữ liệu hiệu năng và trải nghiệm thực chưa đủ.

| Khía cạnh | Điểm /10 | Căn cứ | Tin cậy |
|---|---:|---|---|
| Chức năng và độ chính xác | 7,0 | 10 nhóm có xử lý; có nhiều cữ, bối cảnh đo; còn lỗi BP thấp, BMI độ III và lời khuyên thời tiết | Trung bình–khá; phần lớn đọc code và unit test |
| UX/UI và tiếp cận | 6,5 | Nhãn, đơn vị, Material, phản hồi; màu cảnh báo tương phản thấp, lint a11y và thiếu xác minh người lớn tuổi | Trung bình cho mã/tương phản; chưa đủ cho trải nghiệm thực |
| Dữ liệu và độ tin cậy | 6,5 | SQLite, FK, transaction cữ thuốc, migration; còn vòng đời ngủ, năm sinh giả và thiếu khôi phục chủ động | Trung bình; migration thiết bị chưa chạy lại |
| Hiệu năng | Chưa đủ dữ liệu | Thấy truy vấn main thread và lịch sử không giới hạn nhưng chưa đo latency, frame, RAM, pin | Không chấm bằng suy đoán |
| Bảo mật và riêng tư | 6,0 | Ít quyền, dữ liệu cục bộ, tắt backup, salt; SHA-256 nhanh và owner check chưa nhất quán | Trung bình; chưa pentest/kiểm tra thiết bị bị xâm nhập |
| Chất lượng mã | 7,0 | Helper dùng chung, DAO, test 77 đạt; còn test hình thức, lớp màn hình làm nhiều việc, 74 cảnh báo lint | Khá cho cấu trúc tĩnh; không lấy số warning làm thước đo duy nhất |
| Giá trị sử dụng | 7,5 | Ghi–nhắc–xem xu hướng tích hợp, tiếng Việt/offline; chưa có xuất dữ liệu hoặc bằng chứng giữ chân người dùng | Trung bình; chưa nghiên cứu người dùng |

Các điểm này không dựa vào việc thiếu AI, cloud hay wearable: đó là phạm vi sản phẩm tùy chọn. Ứng dụng thương mại được dùng làm tham chiếu nhiệm vụ, không phải danh sách bắt buộc phải sao chép.

## 8. Tổng hợp ưu và nhược điểm

| Nhóm | Ưu điểm có bằng chứng | Hạn chế quan trọng |
|---|---|---|
| 1. Tài khoản | Salt, hash, phiên, hủy nhắc khi logout | Hash mật khẩu nhanh; thiếu giải thích tài khoản cục bộ |
| 2. Hồ sơ | Cấu hình lưu theo user, mục tiêu nước độc lập | Bỏ trống năm sinh bị biến thành 2000 |
| 3. Chỉ số | NULL, giờ đo, bối cảnh đường huyết | BP thấp bị gắn nhãn tốt; chỉ nhập một vế BP chưa hiện rõ |
| 4. Lịch sử | Lọc gồm hai biên, sắp xếp giờ, CRUD | Không xuất/phân trang; phòng vệ owner chưa đều |
| 5. BMI | Cân mới nhất, helper chung, xét trước làm tròn | Thiếu độ III chuẩn quốc tế; giới hạn tuổi chưa rõ |
| 6. Thuốc | Nhiều cữ độc lập, lịch sử, action đúng ngày cữ | Quick log trong app chưa hủy notification cữ đó |
| 7. Nước | Khung giờ, lượng tùy chỉnh, dừng nhắc khi đủ, opt-in | Chưa undo UI; gợi ý nhiệt độ chỉ là quy tắc cố định |
| 8. Ngủ | Qua đêm, cộng nhiều phiên, sửa/xóa | Chưa lưu biến form khi tái tạo; trùng phiên cộng đôi |
| 9. Biểu đồ | 6 chart, giờ đo, marker, nét huyết áp phân biệt | Không ghi dữ liệu đang bị biểu diễn thành 0; thiếu cách đọc thay thế |
| 10. Môi trường | Worker, cache từng thành phố, bỏ callback cũ | Lỗi mạng vẫn khẳng định dễ chịu; thời gian và nguồn cần rõ |

### Điểm mạnh nổi bật

1. Lịch thuốc có định danh từng cữ và giữ ngày dự kiến khi xác nhận muộn: E16/E17/E20.
2. Dữ liệu lần đo có NULL, giờ và bối cảnh đường huyết đi tới danh sách/marker: E08/E09/E25.
3. Nhắc nước có cấu hình thực sự và không phá mục tiêu cá nhân: E06/E21/E22.
4. Tách helper dùng chung, migration giữ dữ liệu, unit test có thể chạy lại bằng Gradle: E13/E15/E22/E25/E27.

### Việc quan trọng nhất theo tác động

1. **P1:** lời diễn giải sức khỏe sai: BP thấp và BMI quốc tế ≥40. Sửa thuật toán và thêm test biên, ghi nguồn.
2. **P1:** giữ đúng dữ liệu đang nhập giấc ngủ khi Activity tái tạo; xác minh bằng UI test.
3. **P1:** đồng bộ trạng thái cữ thuốc giữa app và notification đang hiển thị.
4. **P1:** không bịa năm sinh, không khẳng định thời tiết tốt khi chưa có dữ liệu.
5. **P1/P2:** cải thiện tương phản chữ cảnh báo, kiểm tra TalkBack/chữ lớn.
6. **P1 trước phát hành thật:** nâng bảo vệ mật khẩu; giữ tương thích tài khoản cũ.
7. **P2:** xuất/khôi phục dữ liệu có chủ đích, undo nước và phân biệt không ghi nhật ký.

Không có phát hiện nào chứng minh dữ liệu hiện tại đã bị rò rỉ hay báo thức đã thất bại trên một điện thoại cụ thể.

## 9. Kế hoạch cải thiện ba giai đoạn

| Giai đoạn | Việc nên làm | Tiêu chí hoàn thành |
|---|---|---|
| Trước bảo vệ | Sửa BP thấp/BMI biên, năm sinh trống, thời tiết lỗi; lưu state ngủ; hủy notification sau quick log; màu cảnh báo | Test cho từng tình huống lỗi; demo trên app id riêng; test lại quyền thông báo, 3 cữ, qua đêm, xoay màn hình và offline |
| Nâng chất lượng trong phạm vi sinh viên | Undo nước; nguồn/ngày dữ liệu thời tiết; CSV; kiểm tra owner; danh sách thay thế chart; rà test hình thức và lint a11y | Không mất dữ liệu khi thao tác nhầm; xuất đúng user/khoảng ngày; người dùng TalkBack đọc được kết quả; đo trên tập dữ liệu lớn trước tối ưu |
| Sau này nếu có nhu cầu | Backup chủ động được bảo vệ; Health Connect/thiết bị tương thích; người chăm sóc; iOS/cloud | Có nhu cầu người dùng, mô hình quyền/đồng ý chia sẻ, test tương thích và kế hoạch vận hành rõ ràng |

Các cải tiến nhỏ dùng DAO/helper hiện có trước, không bắt buộc đổi framework hoặc database. Khó cao nằm ở tích hợp nhiều thiết bị và chia sẻ nhạy cảm, không phải thêm một màn hình mới.

### Chưa nên thêm

- Chatbot “chẩn đoán AI” khi dữ liệu đầu vào và ngưỡng cơ bản còn cần sửa.
- Mạng xã hội, bảng xếp hạng sức khỏe, gamification phức tạp: chưa có bằng chứng giải quyết nhu cầu chính.
- Quét đơn thuốc/tương tác thuốc tự động: cần nguồn chuyên môn, độ chính xác và quy trình kiểm chứng lớn.
- GPS liên tục, bản đồ AQI toàn quốc và dự báo dài ngày chỉ để tăng số tính năng.
- Backend/đồng bộ nhiều nền tảng hoặc viết lại MVVM/Room nếu mục tiêu trước mắt là hoàn thiện đồ án offline.

## 10. Câu hỏi giảng viên có thể phản biện

| Câu hỏi | Gợi ý trả lời trung thực |
|---|---|
| App có 10 hay 14 chức năng? | 10 nhóm nghiệp vụ; 14 thành phần màn hình gồm 10 Activity và 4 Fragment. |
| ViTa tự đo huyết áp/đường huyết được không? | Không; người dùng nhập kết quả đo. App lưu, hiển thị và hỗ trợ theo dõi. |
| Một thuốc uống 3 lần thì lưu thế nào? | Một thuốc có chuỗi giờ đã chuẩn hóa; mỗi giờ có alarm độc lập; trạng thái phân biệt bằng thuốc, ngày, giờ dự kiến. |
| Bấm thông báo hôm qua vào hôm nay có ghi sai ngày không? | Action thuốc mang ngày/giờ của cữ, không mặc định lấy ngày bấm; có test nguồn cho tình huống này. Chưa chạy lại Android test lượt đánh giá này. |
| Đăng nhập trên máy khác có dữ liệu không? | Không có đồng bộ cloud. Tài khoản và nhật ký chính ở SQLite của máy hiện tại. |
| Mất mạng thì còn gì? | Các chức năng nhật ký/nhắc/biểu đồ vẫn dùng dữ liệu cục bộ; thời tiết chỉ có cache nếu đã tải. |
| 33°C cộng 300 ml có phải công thức y khoa? | Là quy tắc gợi ý của đồ án, người dùng phải bật; không thay thế mục tiêu của nhân viên y tế. |
| Vì sao chọn hai chuẩn BMI? | Để minh họa lựa chọn ngưỡng; cần công bố nguồn và giới hạn đối tượng. Bản hiện tại còn lỗi độ III cần sửa. |
| Đã bảo đảm nhắc thuốc đúng giờ trên mọi máy chưa? | Có AlarmManager, kiểm tra quyền, fallback và boot recovery trong code; chưa chứng minh mọi OEM/Doze/quyền bị thu hồi. |
| SQLite cục bộ có nghĩa bảo mật tuyệt đối không? | Không. Có sandbox và cấu hình hạn chế backup; chưa mã hóa DB ở tầng app, hash mật khẩu cần nâng nếu phát hành. |
| Vì sao 77 test đạt mà vẫn có lỗi? | Bộ test chỉ kiểm tra những trường hợp đã viết; còn thiếu BP thấp, BMI≥40 và một số state/notification. Một số test cũ chỉ kiểm tra hằng/model. |
| Vì sao không thêm AI/cloud để khác biệt? | Ưu tiên tính đúng, dữ liệu và thao tác đơn giản cho phạm vi người dùng tại nhà; mở rộng khi có nhu cầu và năng lực vận hành. |

**Kết luận dành cho bảo vệ:** ViTa có phạm vi vừa sức và luồng nghiệp vụ liên kết tốt. Cần ưu tiên diễn giải đúng chỉ số, giữ dữ liệu nhập, thống nhất thông báo và khả năng đọc, trước khi tăng số chức năng.

**Chưa kiểm tra:** UI/thiết bị thật, Android test trong lượt này, OEM/Doze/reboot, hiệu năng và kiểm định y khoa đầy đủ. **Rủi ro cần biết:** test/build đạt không chứng minh an toàn lâm sàng; chưa có phương án khôi phục dữ liệu chủ động khi gỡ app/đổi máy.

