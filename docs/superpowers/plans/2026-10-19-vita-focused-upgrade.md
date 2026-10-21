# ViTa Focused Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans for inline execution or superpowers:subagent-driven-development if the user explicitly chooses delegation. Steps use checkbox syntax for tracking.

**Goal:** Hoàn thiện toàn bộ tám nhóm chức năng trong bản nâng cấp đã được người dùng duyệt ngày 19/10/2026.

**Architecture:** Giữ Java/XML, Material, SQLite và bốn tab. Nâng cấp từ quyền sở hữu dữ liệu, sửa sai và thời gian tới giao diện tổng hợp; tái sử dụng các DAO/helper có thật và chỉ tách thành phần khi có nhiều caller cần cùng luồng.

**Tech Stack:** Java 17, Android Native, SQLiteOpenHelper, Material, MPAndroidChart, JUnit 4, AndroidX Test/Espresso.

**Spec:** `docs/superpowers/specs/2026-10-19-vita-focused-upgrade-design.md`.

## Global Constraints

- Giữ bốn tab Trang chủ, Chỉ số, Nhắc nhở, Thống kê và lối vào Hồ sơ hiện có.
- Không đưa trở lại thời tiết, AQI, giấc ngủ hoặc sao lưu/khôi phục.
- Không thêm backend/framework/dependency chỉ để triển khai các màn đang có; giữ phong cách xanh ngọc và token Android hiện tại.
- TDD cho logic quan trọng; XML trình bày dùng compile/lint và kiểm tra UI, không ép unit test cho XML.
- Mọi mutation dữ liệu phải ràng buộc chủ sở hữu ở DAO và xác minh phiên ở UI/receiver.
- Lượng nước mỗi lần 50–2.000 ml; mục tiêu nước 100–10.000 ml/ngày. Chỉ có hai chế độ nhắc: mỗi 2 giờ hoặc nhiều mốc trong khung giờ đã chọn.
- So sánh đường huyết chỉ giữa cùng bối cảnh đã xác định; không tạo số 0 thay dữ liệu thiếu hoặc điểm sức khỏe tổng hợp.
- Giữ dữ liệu cũ; không xóa/recreate database, bảng ngủ hoặc cột thời tiết trong migration.
- Instrumentation dùng gói `.verification` hoặc SQLite in-memory; không chạy test xóa database trên app thật.
- Sau mỗi task: Ponytail Review diff + caller, sửa lỗi bắt buộc, ghi lệnh/kết quả và quyết định vào nhật ký tiến độ. Không hỏi tiếp tục giữa các task sau khi kế hoạch được duyệt.
- Giữ nguyên các thay đổi gỡ chức năng từ trước; không sửa hai tài liệu Figma chưa theo dõi. Không push/merge trong phạm vi nâng cấp này.

## Review Focus

1. Activity/notification cũ còn tồn tại khi chuyển A → B: phải từ chối truy cập và ghi nhận bằng ID của A. Test ở Task 1.
2. Dữ liệu cũ có thời điểm thiếu ngày/giờ, log thuốc chưa có snapshot hoặc trùng cữ: không crash, không bịa lịch sử, chỉ sửa đúng cữ. Test ở Task 2–3.
3. Chỉnh sửa/hoàn tác nước gần nửa đêm: đúng hồ sơ, đúng ngày, không xóa mục khác hoặc bắn nhắc ngoài lịch. Test ở Task 2, 6.
4. Xóa lần cân gần nhất và nhiều lần đo cùng ngày: nguồn BMI/lần trước/biểu đồ phải thay đúng, không lấy bản ghi tương lai hay khác bữa ăn. Test ở Task 4–5, 8.
5. Chữ lớn, xoay màn, quay lại tab và thiếu quyền nhắc: giữ bản nháp/bộ lọc, nút không bị che, chỉ dẫn quyền rõ. Test ở Task 4, 6, 9.

## Quy ước đường dẫn và lệnh

Trong các task, `J` = `Vita_Project/app/src/main/java/vn/edu/tdmu/vita`, `R` = `Vita_Project/app/src/main/res`, `U` = `Vita_Project/app/src/test/java/vn/edu/tdmu/vita`, `D` = `Vita_Project/app/src/androidTest/java/vn/edu/tdmu/vita`.

Lệnh unit suite từ root:

```powershell
./scripts/gradle.ps1 -Tasks @(':app:testDebugUnitTest','--offline','--max-workers=2')
```

Lệnh kiểm chứng app/test APK/lint:

```powershell
./scripts/gradle.ps1 -Tasks @(':app:testDebugUnitTest',':app:assembleDebug',':app:assembleDebugAndroidTest',':app:lintDebug','--offline','--max-workers=2')
```

Chỉ thêm `--tests` khi chạy RED/GREEN một JUnit test. Device test chạy bằng adb instrumentation trên gói verification, lọc class/method đang kiểm chứng; ghi exact command sau khi xác minh SDK/device thực tế. Lỗi resolve plugin/JDK/SDK không phải RED của tính năng.

## Task 0: Xác minh nền và chuẩn bị phạm vi làm việc

**Files:** `scripts/gradle.ps1`, cấu hình Gradle hiện có, nhật ký `docs/superpowers/plans/2026-10-19-vita-focused-upgrade-progress.md`.

**Interfaces:** Consumes: working tree hiện tại và cache/JDK/SDK cục bộ. Produces: bộ lệnh build/test sử dụng đúng môi trường; điểm chuẩn kết quả và nhật ký task.

- [ ] Ghi Git HEAD, branch và trạng thái trước triển khai. Chọn nhánh `codex/vita-focused-upgrade` bằng workflow worktree phù hợp, giữ đầy đủ các thay đổi gỡ chức năng đang có; không reset/stash/drop tùy tiện.
- [ ] Đọc đường dẫn wrapper, AGP, Java/SDK và cache. Đối chiếu `GRADLE_USER_HOME` thực tế với cache `C:/Users/Trung Tin/.gradle`; đã quan sát thư mục AGP 9.3.3 tại cache này. Chạy lại baseline bằng lệnh trên với đúng cache, không sửa phiên bản dependency theo phỏng đoán.
- [ ] Nếu lỗi, dùng systematic-debugging: lưu thông báo đầy đủ, xác định lớp wrapper/plugin/SDK/compile/test trước khi sửa một nguyên nhân. Chỉ thay script nếu đã chứng minh script chọn sai môi trường; không bỏ plugin tạm để coi build là đạt.
- [ ] Chuẩn bị gói verification bằng cấu hình test tách biệt, không thay applicationId của APK giao người dùng; xác minh `adb devices` rồi mới chạy device tests.
- [ ] Ghi toàn bộ suite baseline và các hạn chế có thật vào nhật ký; kiểm tra các thay đổi gỡ chức năng compile được khi nền build khả dụng.

## Task 1: Quyền sở hữu dữ liệu và trải nghiệm tài khoản

**Files:** `J/database/{HealthDao,MedicineDao,WaterDao}.java`, `J/activities/{LoginActivity,RegisterActivity,EditProfileActivity,DetailHealthActivity,AddHealthActivity,AddMedicineActivity}.java`, `J/fragments/{HealthFragment,ReminderFragment}.java`, receivers, `R/values/strings.xml`, `R/layout/activity_register.xml`.

**Tests:** tạo `D/AccountIsolationDeviceTest.java`; mở rộng `D/ProfileDeletionDaoDeviceTest.java` và `D/FunctionalRegressionDeviceTest.java`.

**Interfaces:**
- `HealthDao.getById(long id,long userId)`, `update(HealthRecord record,long userId)`, `delete(long id,long userId)`; từ chối sai chủ ở SELECT/UPDATE/DELETE.
- Thuốc: `getById(long id,long userId)`, `update(Medicine medicine,long userId)`, `delete(long id,long userId)`, `toggleActive(long id,long userId,boolean active)`, `getHistory(long medicineId,long userId)`, `getLogForDose(long medicineId,long userId,String date,String time)`, `logMedicineStatus(long medicineId,long userId,String date,String time,String status,String loggedAt)`.
- `WaterDao.getLogsByDate(long userId,String date)` và `deleteLog(long id,long userId)` giữ chữ ký; mọi API mới dùng cùng quy tắc owner.

- [ ] RED: tạo A/B; đăng nhập B rồi mở DetailHealth với ID của A; mong đợi Activity đóng và không hiển thị số đo A. Kiểm tra intent notification cũ của A không tăng log sau đổi B. Chạy thấy assertion thất bại ở mã hiện tại.
- [ ] Sửa DAO và toàn bộ caller/test bằng chữ ký trên; query log thuốc phải xác minh medicine thuộc user. Tránh tiếp tục dùng object/Activity cũ sau đổi phiên.
- [ ] Bổ sung test DAO: ID của A dưới actor B trả null/false, A vẫn còn nguyên dữ liệu sau mutation bị từ chối.
- [ ] Thêm lời giải thích tài khoản cục bộ lúc đăng ký, giữ input khi login sai, chỉnh nội dung logout/delete đúng chức năng hiện tại.
- [ ] GREEN: chạy device tests phân quyền + unit suite; review các path intent trực tiếp, Back và thao tác thông báo, ghi kết quả.

## Task 2: Nhật ký nước, chỉnh sửa và lượng khác

**Files:** `J/database/WaterDao.java`, tạo `J/utils/WaterJournalDialog.java`, `J/fragments/ReminderFragment.java`, `J/receivers/WaterReminderReceiver.java`, `R/layout/fragment_reminder.xml`, tạo `R/layout/dialog_water_journal.xml`, `R/values/strings.xml`.

**Tests:** mở rộng `D/WaterJournalDeviceTest.java`, `U/utils/WaterReminderPolicyTest.java`.

**Interfaces:** `boolean WaterDao.updateLog(long id,long userId,int amountMl,String loggedAt)` cập nhật lượng/giờ của đúng ngày gốc; các hàm static trả void `WaterJournalDialog.show(FragmentActivity activity,long userId,String date,Runnable onChanged)`, `showAddAmount(FragmentActivity activity,long userId,Runnable onChanged)` và `addQuickWater(FragmentActivity activity,long userId,Runnable onChanged)` dùng chung ghi nhanh/Hoàn tác cho hai tab.

- [ ] RED: UI test bấm tổng nước với fixture 250 ml lúc 07:00 và 350 ml lúc 08:00, mong danh sách hai dòng và thao tác sửa; mã cũ không mở nhật ký. Chạy và kiểm tra lỗi thiếu hành vi đúng dự kiến.
- [ ] RED DAO trước mutation mới: kiểm thử literal tổng 600 → sửa mục 350 thành 500 → 750; sai owner không đổi; 49/2.001 ml không ghi; giờ sửa giữ nguyên ngày; thời điểm legacy `08:00` đọc được.
- [ ] Xây dialog danh sách theo ngày, form lượng khác/sửa giờ, xác nhận xóa. Giữ ghi nhanh và Hoàn tác đúng ID, xác minh phiên tại mỗi lần Lưu; chống nhấn Lưu lặp.
- [ ] Thẻ tiến độ ghi rõ đã uống/mục tiêu/còn lại, đạt mục tiêu vẫn cho ghi. Chỉnh dữ liệu hôm nay xong chỉ lên lịch mốc kế tiếp, không phát thông báo ngay; sửa quá khứ không đổi tổng hôm nay.
- [ ] GREEN: test riêng + unit suite; review/cleanup để Trang chủ và Thống kê dùng lại dialog, không sao chép flow.

## Task 3: Vòng đời cữ thuốc và lịch sử

**Files:** `J/database/{DatabaseHelper,MedicineDao}.java`, `J/models/MedicineLog.java`, tạo `J/utils/MedicineDoseState.java`, `J/adapters/MedicineAdapter.java`, `J/fragments/ReminderFragment.java`, `J/activities/AddMedicineActivity.java`, `J/utils/NotificationHelper.java`, layouts thuốc và strings.

**Tests:** tạo `U/utils/MedicineDoseStateTest.java`, mở rộng `D/MedicineDoseDeviceTest.java`, tạo `D/MedicineHistoryMigrationDeviceTest.java`.

**Interfaces:** `MedicineDoseState.classify(String date,String time,String status,long nowMillis)` trả enum UPCOMING/UNCONFIRMED/TAKEN/SKIPPED. `MedicineDao.clearDoseStatus(long medicineId,long userId,String date,String time)` gỡ đúng cữ. `MedicineDao.getNextUnloggedDose(long userId,long nowMillis)` trả `NextDose` gồm medicine, date, time hoặc null; lớp `NextDose` đặt trong MedicineDao. Giữ API log từ Task 1.

- [ ] RED: test 07:00 chưa xác nhận tại 08:00 → UNCONFIRMED; 09:00 → UPCOMING; TAKEN/SKIPPED không phụ thuộc đã qua giờ; không suy ra SKIPPED khi chưa có log. Chạy trước implementation.
- [ ] RED migration: database version 5 có log ngày/giờ khác nhau, nâng 6 vẫn giữ các cữ; tên/liều kế thừa có cờ nguồn, không coi là snapshot đã biết trong quá khứ.
- [ ] Thêm cột `medicine_name_snapshot`, `dosage_snapshot`, `snapshot_inherited` vào medicine_logs; migration 5→6 backfill từ medicine hiện có, đánh dấu inherited=1. Ghi mới snapshot_inherited=0; sửa trạng thái giữ snapshot hiện hữu. Không thêm unique index/deduplicate nếu chưa có bug tái hiện đòi hỏi.
- [ ] Nhóm các cữ theo bốn trạng thái và giữ danh sách quản lý thuốc. Cho sửa trạng thái từ hôm nay/lịch sử, hiển thị giờ dự kiến lẫn lúc xác nhận; thao tác trả chưa ghi nhận chỉ gỡ đúng cữ.
- [ ] Sửa lịch áp dụng tương lai; lịch sử giữ ngày/giờ và snapshot. Công tắc ghi “Bật nhắc”. Banner thông báo/báo thức mở đúng cài đặt, mô tả fallback không chính xác.
- [ ] GREEN: unit + device/migration; kiểm tra nhiều giờ, ngày tuần, tắt/bật, notification trễ qua ngày, đổi phiên, giữ lịch sử sau sửa lịch; review module.

## Task 4: Hồ sơ, nguồn cân nặng và bảo vệ bản nháp

**Files:** `J/database/HealthDao.java`, `J/activities/{ProfileActivity,EditProfileActivity,AddHealthActivity,AddMedicineActivity}.java`, tạo `J/utils/UnsavedChangesGuard.java`, `R/layout/activity_profile.xml`, strings và resource BMI.

**Tests:** mở rộng `D/BmiScopeDeviceTest.java`, tạo `D/UnsavedChangesDeviceTest.java`.

**Interfaces:** `HealthDao.getLatestWeightRecord(long userId)` trả HealthRecord/null; giữ `getLatestWeight(long userId)` cho caller chỉ cần số. `static UnsavedChangesGuard attach(AppCompatActivity activity,BooleanSupplier changed)` và `void requestExit()` dùng cùng callback cho toolbar/system Back; form tự giữ baseline và draft Bundle, không đưa logic test vào production.

- [ ] RED: sửa chiều cao/hồ sơ rồi Back phải còn Activity và có lựa chọn tiếp tục/bỏ; form không đổi phải đóng ngay. Test xoay màn giữ draft và trạng thái bẩn.
- [ ] RED: fixture cân ban đầu 60 kg, log 65 kg ngày 17 và 64 kg ngày 18: hồ sơ hiện 60 riêng, BMI dùng 64 và ngày 18; sửa ban đầu 61 không tạo log mới; xóa log ngày 18 chuyển sang 65.
- [ ] Hiển thị nguồn và thời điểm cân/BMI, giải thích chuẩn bằng nhãn ngắn; giữ ngưỡng/phạm vi BMI hiện hành. Gắn guard vào cả bốn form, bao gồm cấu hình nước trong EditProfile.
- [ ] GREEN unit + device; review guard sau save/error/rotate, password logout/delete không bị lưu trong draft.

## Task 5: Form chỉ số, bộ lọc và đối chiếu lần trước

**Files:** `J/database/HealthDao.java`, `J/activities/{AddHealthActivity,DetailHealthActivity}.java`, `J/fragments/HealthFragment.java`, `J/utils/HealthDisplay.java`, health layouts/strings.

**Tests:** mở rộng `D/MeasurementDeviceTest.java`, tạo `D/HealthComparisonDeviceTest.java`.

**Interfaces:** `HealthDao.getPreviousRecord(long userId,HealthRecord current,HealthChartData.Metric metric)` trả bản ghi trước có số tương ứng; bổ sung enum BLOOD_PRESSURE cho cặp đủ hai trị số nếu dùng chung Metric. Đường huyết yêu cầu context bằng current và khác UNSPECIFIED.

- [ ] RED: bản ghi 95 mg/dL FASTING không so với AFTER_MEAL 140; bản 100 FASTING sau đó so với 95, chênh +5 mg/dL; trước lần đầu và UNSPECIFIED trả null. Có fixture hai lần cùng ngày và bản ghi người khác.
- [ ] Nhóm trường Tất cả/Huyết áp/Nhịp tim/Cân nặng/Đường huyết; đổi nhóm chỉ đổi hiển thị, không xóa số đã nhập. Đơn vị, ngày giờ, bữa ăn và trường thiếu rõ ràng; lỗi số nằm đúng trường và focus được.
- [ ] Chi tiết hiển thị lần trước/chênh lệch kèm ngày giờ, không suy ra tốt/xấu. Danh sách hiển thị khoảng lọc + số kết quả, “Bỏ lọc”, trạng thái chưa có/không khớp bộ lọc khác nhau.
- [ ] GREEN: test đối chiếu + unit suite/device; thêm/sửa/xóa/ghi chú-only hoạt động và nguồn cân cập nhật; review module.

## Task 6: Điều hướng giữ trạng thái và ngày/giờ thay đổi

**Files:** `J/activities/MainActivity.java`, `J/fragments/{HomeFragment,HealthFragment,ReminderFragment,StatsFragment}.java`, `J/receivers/{MedicineAlarmReceiver,WaterReminderReceiver,BootReceiver}.java` nếu test chỉ ra cần sửa, state Bundle của từng màn.

**Tests:** tạo `D/NavigationStateDeviceTest.java`, mở rộng device tests alarm/water.

**Interfaces:** Fragment tag ổn định `home`, `health`, `reminder`, `stats`; `MainActivity.openTab(int itemId)` tái sử dụng fragment và set lifecycle RESUMED cho tab hiện tại, STARTED cho tab ẩn. Callback cập nhật dữ liệu visible qua onResume và thay đổi giờ hệ thống, không event bus/service mới.

- [ ] RED: chọn khoảng lọc chỉ số, cuộn rồi sang tab khác/quay lại: bộ lọc/vị trí giữ nguyên; recreate Main giữ tab đã chọn. Chạy thấy behavior hiện tại mất state.
- [ ] Chuyển sang add/show/hide với tag và restore từ FragmentManager, tránh tạo trùng fragment. Phần reload không reset trạng thái UI đã chọn.
- [ ] Gắn callback thời gian theo vòng đời màn đang thấy để cữ đổi nhóm khi tới giờ và tổng nước chuyển sang ngày mới; quay từ nền cũng refresh. Test clock/fixture có nowMillis literal, không test bằng sleep dài hay đổi giờ hệ thống máy người dùng.
- [ ] GREEN: UI state + pure schedule suite + notification routing tới tab Nhắc nhở; review không receiver/timer hoạt động sau màn bị đóng hoặc phiên đổi.

## Task 7: Trang chủ tổng hợp

**Files:** `J/fragments/HomeFragment.java`, `R/layout/fragment_home.xml`, strings; dùng DAO Task 1/3/4 và WaterJournalDialog Task 2.

**Tests:** tạo `D/HomeDashboardDeviceTest.java`.

**Interfaces:** đọc `getNextUnloggedDose(userId,nowMillis)`, `getLatestWeightRecord(userId)`, `getAllByUserId(userId,null)` để lấy bản mới nhất, `WaterDao.getDailyTotal(userId,date)`; gọi dialog chung và Activity đang có bằng ID đã scoped.

- [ ] RED: hồ sơ fixture có thuốc sắp tới, nước 600/2.000 ml và số đo lúc 08:00; mong Trang chủ hiển thị đúng ba dữ liệu + nút Ghi chỉ số. Test hồ sơ mới chưa có dữ liệu có CTA dùng được.
- [ ] Thứ tự thẻ theo spec: ghi chỉ số, cữ tiếp theo, nước, chỉ số gần nhất, BMI và nguồn. Chạm mở đúng màn; cữ đã xử lý được bỏ khỏi lựa chọn kế tiếp.
- [ ] Ghi nhanh/lượng khác/nhật ký dùng chung flow với tab Nhắc nhở; giữ Hoàn tác chính xác tại chỗ cần thực hiện, tránh sao chép logic cộng/xóa nước giữa hai Fragment.
- [ ] GREEN: test thẻ, routing, số liệu sau sửa/xóa/quay lại/đổi phiên; unit suite; review module.

## Task 8: Thống kê tập trung, dữ liệu điểm và danh sách

**Files:** `J/fragments/StatsFragment.java`, `J/utils/{HealthChartData,HealthDisplay}.java`, `R/layout/fragment_stats.xml`, `R/layout/marker_chart.xml`, strings; có thể tạo adapter dòng thống kê nếu cần RecyclerView cho danh sách.

**Tests:** mở rộng `U/utils/HealthChartDataTest.java`, tạo `D/StatsDataDeviceTest.java`.

**Interfaces:** Metric gồm WEIGHT/HEART_RATE/BLOOD_SUGAR/BLOOD_PRESSURE từ Task 5; giữ Point.record làm metadata tooltip. Chọn WATER ở UI tách khỏi metric sức khỏe; context filter sử dụng HealthRecord.MealContext hiện có.

- [ ] RED: một ngày có hai thời điểm và hai bữa ăn tạo đúng hai điểm/metadata; lọc FASTING bỏ AFTER_MEAL; giá trị null không tạo điểm 0. Kiểm tra 7/30 ngày literal số bản ghi và tổng nước.
- [ ] UI chọn một trong năm nhóm, khoảng 7/30 và bữa ăn khi chọn đường huyết. Tất cả bữa ăn dùng series riêng theo context, không nối khác bối cảnh.
- [ ] Hiển thị số bản ghi/lần mới nhất, tooltip có ngày giờ/đơn vị/context và danh sách tương ứng. Một điểm vẫn hiển thị điểm; zero records và thiếu dữ liệu xu hướng dùng khối text bên ngoài chart.
- [ ] Nước: ngày thiếu log ghi “Chưa ghi nhận”, hôm nay ghi “Đang diễn ra”, mục tiêu hiện tại chỉ là đường tham chiếu. Dòng nước mở dialog ngày; dòng sức khỏe mở chi tiết scoped.
- [ ] GREEN: tests data/UI + suite; chọn nhóm/khoảng/context còn sau quay tab/rotate; review tooltip không mất giờ do ép x về index nguyên.

## Task 9: Đồng bộ giao diện, regression và bàn giao

**Files:** resource styles/dimens/colors/strings/layout thực sự bị ảnh hưởng, tài liệu chức năng hiện hành; nhật ký tiến độ. Không viết lại tài liệu đánh giá lịch sử hoặc file Figma chưa theo dõi để giả vờ là mã đã làm.

**Tests:** `D/FunctionalRegressionDeviceTest.java`, `D/VitaSystemIntegrationTest.java`, toàn bộ U/D liên quan.

- [ ] Đi qua luồng đăng ký → hồ sơ → ghi chỉ số → sửa/xóa → thuốc/ghi trạng thái → nước/nhật ký → Trang chủ → Thống kê → logout/login B; bảng/biểu đồ/tổng khớp.
- [ ] Kiểm tra 48dp, sp, thứ tự TalkBack, nhãn có đơn vị, lỗi gần trường, confirm delete/save/Back nhất quán; chữ lớn và màn nhỏ không che nút. Dùng native guideline và token đang có, không áp dụng các kết quả tra cứu UI lệch web.
- [ ] Chạy toàn bộ unit suite, assembleDebug, assembleDebugAndroidTest, lintDebug. Device run chỉ gói verification đã kiểm chứng. Kiểm tra bootstrap/reboot khi có thiết bị; báo đúng hạn chế khi chưa có.
- [ ] Ponytail Review cuối toàn bộ thay đổi và callers; với finding quan trọng viết RED rồi sửa/GREEN. Dọn tài nguyên/dependency/import không còn dùng và cập nhật mô tả chức năng hiện hành.
- [ ] Bàn giao báo cáo module đã làm, lệnh/kết quả test, đường dẫn APK mới nếu build thành công; không gọi APK cũ là bản nâng cấp.

## Tự rà soát kế hoạch

- Tám nhóm yêu cầu trong spec đều có task: tài khoản (1), hồ sơ (4), Trang chủ (7), chỉ số (5), thuốc (3), nước (2), thống kê (8), điều hướng/dữ liệu (1/6/9).
- Thứ tự dependency: 0→1; 1→2/3/4; 4→5; 1–5→6; 2/3/4/6→7; 2/5/6→8; toàn bộ→9.
- API scope dùng cùng thứ tự `id,userId`; truy vấn danh sách dùng `userId` trước. Không dựa vào global session trong DAO được kiểm thử in-memory.
- Back/rotate, clock rollover, dữ liệu legacy, hai hồ sơ và dòng nhiều lần/ngày đều có test task sở hữu trong Review Focus.
- Kế hoạch và phương thức thực hiện đã được duyệt. Trạng thái thực thi, các ruling và kết quả kiểm chứng nằm trong `2026-10-19-vita-focused-upgrade-progress.md`; các checkbox phía trên giữ vai trò checklist thiết kế ban đầu.
