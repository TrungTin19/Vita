# ViTa — chạy lại toàn bộ kiểm thử Android

Ngày 23/10/2026. Mã nguồn được kiểm tra: `a21c54f` trên nhánh `main`.

## Kết luận

Đã có một lượt chạy hoàn tất đủ **85 ca: 84 đạt, 1 thất bại, 0 bỏ qua**. Ca thất bại đạt khi chạy riêng và khi chạy lại cả nhóm 8 ca giao diện. Tuy nhiên, lượt xác nhận toàn bộ tiếp theo bị hệ thống Android chấm dứt với `INSTRUMENTATION_ABORTED: System has crashed`. **Chưa xác nhận được một lượt tổng 85/85 đạt.**

Không sửa mã sản phẩm, kiểm thử, assertion hoặc dependency trong lượt này. Không bỏ qua ca lỗi hoặc cộng các lượt chạy riêng thành kết quả của một lượt tổng.

## Môi trường và an toàn dữ liệu

- AVD Pixel_6, Android 13 / API 33, x86_64; kích thước 1080×2400, density 420, font 1.0.
- Khởi chạy không cửa sổ, không âm thanh, 2 lõi CPU, RAM 1.536 MB; không đọc/ghi snapshot và không xóa dữ liệu AVD.
- Lúc bắt đầu không có thiết bị kết nối. Máy host có khoảng 500 MB RAM trống; trong quá trình boot có lúc còn khoảng 200 MB. Đây là yếu tố môi trường được ghi nhận, chưa chứng minh là nguyên nhân duy nhất.
- Cold boot đầu xuất hiện ANR của `com.android.systemui` trước khi chạy ViTa. Đã lưu log và khởi động lại emulator; sau đó xác nhận boot hoàn tất và launcher có focus trước khi cài APK.
- Sử dụng init script verification đã có. Gói đích `vn.edu.tdmu.vita.verification`, gói test `vn.edu.tdmu.vita.verification.test`; không cài đè APK chính và không chạy fixture trên dữ liệu sản phẩm.
- Trong lúc chẩn đoán, emulator báo Awake, màn hình bật, không tự khóa; `screen_off_timeout=2147483647`, `stay_on_while_plugged_in=7`. Không thay các cấu hình này trong lượt kiểm tra.

## Build

Build mới `assembleDebug` và `assembleDebugAndroidTest` cho gói verification: **BUILD SUCCESSFUL in 1m27s**.

Log: `.audit-cache/2026-10-23-device-build.txt`.

Không chạy lại unit test trong lượt này: yêu cầu hiện tại là kiểm thử Android trên emulator. Kết quả unit cũ không được tính thành kiểm thử thiết bị mới.

## Các lượt thực thi

| Lượt | Kết quả | Thời gian / giới hạn |
|---|---|---|
| Toàn bộ 85 ca | 84 đạt, 1 thất bại, 0 bỏ qua | 424,36 giây; hoàn tất toàn bộ |
| Riêng ca trạng thái trống bị lỗi | OK (1 test) | 13,176 giây |
| Toàn bộ FigmaScreenshotsDeviceTest | OK (8 tests) | 198,223 giây |
| Xác nhận lại toàn bộ | 10 đạt, 1 thất bại; ca 12 đang chạy thì hệ thống crash | Chưa có kết quả cho 74 ca còn lại; không tính là một lượt 85 ca hoàn tất |

Runner của lượt đầu có thời hạn mỗi ca 120 giây. Các lượt chẩn đoán sau dùng 180 giây cho ca đơn và 240 giây cho nhóm/lượt xác nhận để không chặn sớm ca chụp nhiều màn. Không sửa assertion và không dùng cơ chế tự bỏ qua lỗi.

### Kết quả theo nhóm của lượt tổng đã hoàn tất

| Nhóm kiểm thử | Đạt | Thất bại |
|---|---:|---:|
| AccessibilityDeviceTest | 3 | 0 |
| AccountIsolationDeviceTest | 3 | 0 |
| BmiScopeDeviceTest | 2 | 0 |
| FigmaDialogDeviceTest | 2 | 0 |
| FigmaScreenshotsDeviceTest | 7 | 1 |
| FormClippingDeviceTest | 1 | 0 |
| FunctionalRegressionDeviceTest | 3 | 0 |
| HealthComparisonDeviceTest | 3 | 0 |
| HomeDashboardDeviceTest | 1 | 0 |
| MeasurementDeviceTest | 1 | 0 |
| MedicineAlarmDeviceTest | 1 | 0 |
| MedicineDoseDeviceTest | 1 | 0 |
| MedicineHistoryMigrationDeviceTest | 3 | 0 |
| MedicineStatusUiTest | 3 | 0 |
| NavigationStateDeviceTest | 2 | 0 |
| ProfileBirthYearDeviceTest | 5 | 0 |
| ProfileBmiNavigationDeviceTest | 1 | 0 |
| ProfileDeletionDaoDeviceTest | 3 | 0 |
| ProfileDeletionScreenDeviceTest | 4 | 0 |
| ProfileSettingsDaoDeviceTest | 2 | 0 |
| ProfileUpgradeDeviceTest | 3 | 0 |
| RegistrationStepsDeviceTest | 2 | 0 |
| ReviewImprovementsDeviceTest | 7 | 0 |
| ScheduleRecoveryDeviceTest | 1 | 0 |
| StatsDataDeviceTest | 3 | 0 |
| UiAuditDeviceTest | 1 | 0 |
| VitaSystemIntegrationTest | 1 | 0 |
| WaterJournalDeviceTest | 10 | 0 |
| WaterSettingsDeviceTest | 1 | 0 |
| WaterSettingsScreenDeviceTest | 4 | 0 |
| **Tổng** | **84** | **1** |

## Những lỗi cần giữ lại để theo dõi

1. Lượt đầu: `FigmaScreenshotsDeviceTest.emptyStatesKeepTheirEntryActionsAndDoNotCreateData`, dòng 168. Espresso báo `RootViewWithoutFocusException` khi xác nhận thanh điều hướng sau khi quay lại từ form chỉ số. Ca đạt khi chạy riêng và cả nhóm 8 ca đạt khi chạy lại; chưa xác định được nguyên nhân gốc khiến focus chập chờn trong lượt tổng.
2. Lượt xác nhận: `dashboardAndStatsTableKeepTheirFullHeadings`, dòng 127. `ActivityScenario.close()` chờ màn hình về `DESTROYED` nhưng trạng thái cuối là `PAUSED`. Ca này đã đạt trong lượt tổng đầu và lượt nhóm; không coi việc đạt lại là bằng chứng đã sửa lỗi vòng đời.
3. Sau lỗi đóng màn hình ở lượt xác nhận, ca `captureMainFlows` đang chạy thì runner nhận `System has crashed`. Không có tổng kết 85 ca của lượt này. Không suy diễn từ thông báo này rằng ViTa tự crash; cũng chưa có bằng chứng đầy đủ để loại trừ mọi lỗi phía ứng dụng.

## Bằng chứng lưu cục bộ

- `.audit-cache/2026-10-23-device-build.txt`
- `.audit-cache/2026-10-23-emulator.txt`
- `.audit-cache/2026-10-23-emulator-error.txt`
- `.audit-cache/2026-10-23-boot-anr-logcat.txt`
- `.audit-cache/2026-10-23-full-device-suite.txt`
- `.audit-cache/2026-10-23-empty-isolated.txt`
- `.audit-cache/2026-10-23-figma-class-rerun.txt`
- `.audit-cache/2026-10-23-full-device-confirmation.txt`
- `.audit-cache/2026-10-23-confirmation-system-logcat.txt`

Các log trong `.audit-cache` là bằng chứng cục bộ, không được đưa vào Git trong lượt này.

## Phần còn lại

**Cập nhật sau sửa ngày 23/10:** bộ kiểm thử đã được bổ sung thành 90 ca và chạy đạt 90/90 trong cùng một lượt trên emulator. Xem [báo cáo sửa lỗi và dọn mã](2026-10-23-fix-and-cleanup.md). Nội dung bên dưới ghi lại trạng thái của lượt kiểm tra trước sửa.

Cần xử lý/tái hiện lỗi focus và vòng đời trên môi trường ổn định hơn, rồi chạy lại để có một lượt tổng 85/85 sạch. Lượt này chưa kiểm điện thoại thật, TalkBack/Switch Access hoặc các cấu hình màn hình/font khác; không phải xác nhận độ khớp Figma.
