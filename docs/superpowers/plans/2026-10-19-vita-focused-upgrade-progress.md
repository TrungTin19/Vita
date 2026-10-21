# SDD ledger — plan: docs/superpowers/plans/2026-10-19-vita-focused-upgrade.md

Spec: ../specs/2026-10-19-vita-focused-upgrade-design.md. Phương thức: trực tiếp, người dùng duyệt bằng “tiếp” ngày 19/10/2026.

## Pre-flight

- BASE: c69abbe; tiếp nhận nguyên trạng các thay đổi gỡ thời tiết/giấc ngủ và source test MedicineStatusUiTest. Hai tài liệu Figma chưa theo dõi không thuộc thay đổi nâng cấp.
- Ruling: dùng nhánh codex/vita-focused-upgrade trên checkout hiện tại để giữ các thay đổi chưa commit; sandbox chặn ghi Git/cache nên dùng quyền thực thi được auto-review chấp nhận cho tạo nhánh và build. Không reset dữ liệu hoặc lịch sử.
- Interface 1→2/3/4/5: DAO id,userId; danh sách userId trước. 2→7/8: dialog nước chung. 3→7: NextDose. 4→5/7: latest weight record. 5→8: dữ liệu cùng context. 6→7/8: lifecycle visible refresh. Không thấy xung đột chữ ký.
- Ruling: không thêm unique index hoặc xóa log trùng khi chưa có test tái hiện cần sửa; giữ cách upsert theo cữ hiện tại và chỉ bổ sung snapshot.
- Task 0: đang chạy baseline bằng GRADLE_USER_HOME=C:/Users/Trung Tin/.gradle. Compile Java đã chạy được; nguyên nhân thiếu plugin trước đây là chọn nhầm cache/quyền truy cập, không phải thiếu phiên bản dependency.

## Trạng thái

- Task 0: complete — baseline testDebugUnitTest + assembleDebug + assembleDebugAndroidTest BUILD SUCCESSFUL; Pixel_6 booted read-only; app/test IDs verified from APK metadata before instrumentation. Init script uses androidComponents.finalizeDsl because earlier defaultConfig callback was overwritten by the project script. No instrumentation ran against the normal application ID.
- Task 1: complete — RED AccountIsolationDeviceTest expected DESTROYED but got RESUMED. GREEN scoped DAO/callers, form session check and local-account/logout copy. Full unit suite/build passed. Emulator: AccountIsolationDeviceTest, MeasurementDeviceTest, MedicineDoseDeviceTest, FunctionalRegressionDeviceTest → OK (7 tests). Ponytail Review found a SQL bind-count mismatch in the edited log update; fixed by scoping its WHERE and verified in MedicineDoseDeviceTest. No remaining finding for this module.
- Task 2: complete — journalLetsUserCorrectAnEntryAfterUndoHasGone RED: no matching row after tapping total. Initial focus errors were caused by emulator SystemUI ANR, diagnosed using dumpsys/UI hierarchy and cleared by restarting SystemUI on emulator only. GREEN WaterJournalDeviceTest → OK (6 tests), full unit suite + app/test APK builds pass. Shared DialogFragment preserves amount/time draft, scoped update keeps original date, quick-add Undo only deletes an unchanged entry. Review checked detached-fragment callbacks and uses Activity context after journal dismissal.
- Task 3: complete — MedicineDoseStateTest first lacked the classifier, then executable RED with an interface-only UPCOMING implementation produced two assertion failures; completed classifier passed full unit suite. Schema 6 adds name/dosage snapshots and inherited provenance without dropping data. Device: MedicineDoseDeviceTest, MedicineHistoryMigrationDeviceTest, MedicineStatusUiTest, ReviewImprovementsDeviceTest → OK (11 tests). Review removed duplicate daily-dose UI from the prescription adapter and moved status/history actions into a shared helper; existing tests updated for intentional grouping UI.
- Task 4: complete — ProfileUpgradeDeviceTest RED thiếu nguồn BMI/dialog bản nháp, GREEN cùng BmiScope và WaterSettingsScreen: OK (7 tests); unit/build xanh. Ruling: kiểm tra Back bằng điều kiện lifecycle/visible thay vì đọc trạng thái ngay sau key event bất đồng bộ. Guard chuẩn hóa số và giữ baseline qua rotation.
- Task 5: complete — RED HealthComparisonDeviceTest thiếu lần trước và mất huyết áp một vế; GREEN cùng MeasurementDeviceTest: OK (3 tests), full unit/build pass. Thêm nhóm nhập không xóa draft, đếm kết quả, no-match và reload sau xóa. Review: sửa cả adapter cho BP thiếu vế; đối chiếu scoped, trước tuple ngày/giờ/id và cùng meal context.
- Task 6–7: complete — executable RED NavigationState/HomeDashboard thiếu giữ lựa chọn và nước; GREEN OK (2 tests), unit/build xanh. Giữ tab bằng tag/maxLifecycle, state range qua rotation; Home dùng chung DAO/dialog nước.
- Task 8: complete — RED thiếu metric selector; GREEN StatsData+Navigation+Home OK (3 tests), unit/build xanh. Một chart được chọn, context đường huyết tách series; dữ liệu thiếu không gán zero. Ruling: Xem dữ liệu mở native list dialog cuộn được thay vì nhét mọi hàng vào ScrollView — dễ đọc/accessibility và tái dùng journal ngày.
- Task 9: complete — review độc lập + full regression/lint. RED journal legacy/full timestamp expected250 got350; sửa sort theo giờ chung. Review tại chỗ phát hiện refresh Reminder chỉ nước; đã dùng refreshToday gồm cữ thuốc. TIME_SET/TIMEZONE_CHANGED cancel/reschedule; giữ pending schedule khi resume thường. Gradle script chọn cache profile khi env chưa đặt và restore env khi xong.

Mỗi task ghi RED/GREEN, review, lệnh xác minh và giới hạn thực tế tại đây trước khi đánh dấu hoàn thành.

## Review cuối và hồi quy (20/10/2026)

- Reviewer độc lập upgrade_final_review phát hiện owner draft EditProfile qua recreate, thiếu onResume owner guard của form chỉ số/thuốc, và validation tại nhóm ẩn. Đã viết RED: owner draft RESUMED thay DESTROYED; sugar group GONE thay VISIBLE. Sửa và reviewer targeted xác nhận không còn finding.
- Lượt full đầu: 56 device tests, 54 pass, hai failure: ActivityScenario.recreate chờ RESUMED trong khi form phải đóng (test đã cho phép đúng DESTROYED), và focus trường đường huyết. Truy nguyên focus: meal label/spinner nằm nhầm trong horizontal row với hai ô weight=1, khiến ô nhập không còn chiều rộng. Tách meal controls thành vertical group ngoài hàng số đo.
- Check chữ 130%: NavigationState 2 tests và ScheduleRecovery 1 test pass; đã xem ảnh Home/Reminder. Không có thao tác đổi thời gian thiết bị thật; ScheduleRecovery xác minh PendingIntent được thay mới bằng receiver callback timezone.
- RED bổ sung xóa thuốc: Panadol vẫn tồn tại trong daily group sau khi xóa prescription. Sửa bằng reload danh sách và nhóm cữ chung, bỏ cập nhật theo position cũ.
- Unit suite hiện 70 tests pass. Lint gần nhất 0 errors/50 warnings, chủ yếu cảnh báo API/dependency, hardcoded text cũ, notifyDataSetChanged và resource menu dùng để khai báo ID. Không tăng SDK/dependency hoặc đổi kiến trúc ngoài phạm vi.
- Ruling: tests/fixtures/destructive database runs chỉ applicationId .verification. Không gỡ/cài đè gói ứng dụng người dùng. Không chỉnh hai tài liệu Figma ngoài phạm vi.

## Kết quả bàn giao

- `scripts/gradle.ps1 -Tasks @(':app:testDebugUnitTest',':app:lintDebug',':app:assembleDebug','--offline','--max-workers=2')`: BUILD SUCCESSFUL; 70 unit tests, 0 failures/errors/skips. Chạy khi GRADLE_USER_HOME chưa đặt để xác nhận script tự dùng đúng cache.
- Verification app/test APK: BUILD SUCCESSFUL. Lượt cuối toàn bộ instrumentation trên `vn.edu.tdmu.vita.verification`: **OK (59 tests)**, 264.049 giây. Bao gồm báo thức thực, chuyển phiên/recreate, migration, xóa cữ, nước legacy, thống kê và clock recovery.
- Hai test xóa thuốc/field focus chạy riêng với chữ 130%: **OK (2 tests)** sau sửa gốc layout; Navigation+ScheduleRecovery 130% trước đó **OK (3 tests)**. Đã xem ảnh Home/Reminder và giữ palette teal/card trắng.
- Fresh-context review và targeted re-review không còn finding bắt buộc; git diff --check sạch. Dọn 10 chuỗi UI cũ không còn caller; giữ menu resource vì nó vẫn khai báo ID cho menu tạo bằng Java.
- Lint bản chính: **0 errors, 50 warnings**. Báo cáo: `Vita_Project/app/build/reports/lint-results-debug.html`. Không tăng SDK/dependency chỉ để xóa cảnh báo.
- APK cuối: `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`, 7.857.022 bytes. Kiểm tra trực tiếp bằng aapt: applicationId **vn.edu.tdmu.vita**, versionName 1.0/versionCode 1, minSdk24/target34. Đây là APK chính; không phải gói verification.
- Chưa kiểm chứng điện thoại thật, reboot thực hoặc chính sách tiết kiệm pin của hãng. Clock/reboot recovery kiểm qua callback receiver; không thay giờ hệ thống máy người dùng. Không push/merge trong đợt này.
