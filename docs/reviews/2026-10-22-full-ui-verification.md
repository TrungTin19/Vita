# ViTa — kiểm tra giao diện toàn ứng dụng

Ngày 22/10/2026. Tiếp nối báo cáo UI/UX đã được duyệt. Phạm vi: Java/XML Views, MVC hiện tại; app sức khỏe cá nhân lưu cục bộ. Không phải kiểm thử tải nhiều người dùng hoặc chứng nhận accessibility.

## Danh mục và kế hoạch kiểm tra

Manifest hiện có **10 Activity**, không phải 11: Splash, Main, Login, Register, Profile, Bmi, EditProfile, AddHealth, DetailHealth, AddMedicine. Main có 4 tab: Hôm nay, Chỉ số, Nhắc nhở, Xu hướng. Có 24 layout XML; dialog/form dùng chung tạo thêm các trạng thái, không tính thành Activity riêng.

| Nhóm | Phạm vi |
|---|---|
| Khởi động, xác thực | Splash, login lỗi/bàn phím, đăng ký hai bước, Back và bản nháp |
| Hôm nay | Lời chào, dữ liệu thuốc/nước/BMI/số đo, hành động nhanh |
| Chỉ số | Danh sách, trống, lọc, nhập/sửa, thời gian, đơn vị, validation, chi tiết |
| Thuốc | Cữ hôm nay, danh sách, lịch nhiều giờ/ngày, thêm/sửa, bật/tắt, lịch sử, xác nhận/xóa |
| Nước | Ghi nhanh, lượng khác, sửa/xóa/hoàn tác, ngày trống, hai chế độ nhắc, validation |
| Xu hướng | 5 chỉ số, 7/30 ngày, trống/một lần đo, biểu đồ, bảng dữ liệu, mở chi tiết |
| Hồ sơ/BMI | Hồ sơ dài, sửa, nguồn cân nặng, chuẩn BMI, tài khoản/đăng xuất/xóa |
| Khả năng sử dụng | Vùng chạm, nhãn/trạng thái native, focus/picker, màn nhỏ, font lớn, bàn phím |

Thứ tự: chạy bộ thiết bị hiện có → chụp lại luồng chính → kiểm tra màn nhỏ/chữ lớn → sửa lỗi có bằng chứng nếu cần → build/unit/lint cuối. Không suy diễn ảnh cũ là bằng chứng của bản hiện tại.

Các bài dùng fixture chỉ được chạy trên package `vn.edu.tdmu.vita.verification`; APK chính và dữ liệu người dùng không bị xóa hay ghi fixture.

## Nhật ký triển khai

- Đã đọc manifest, cấu hình build, các entry point, điều hướng Main và các shell dialog; áp dụng checklist ui-ux-pro-max, design-system, ponytail-review/audit. Không có AGENTS.md được tìm thấy trong workspace.
- Bắt đầu với 76 ca thiết bị hiện có, tách riêng nhóm chụp ảnh. Log `.audit-cache/full-ui-native-suite.txt`; kết quả và các lượt chạy lại ghi bên dưới.
- Một ca chọn Cancel của TimePicker bị RootViewWithoutFocusException; đây là lỗi lựa chọn/focus cửa sổ trong kiểm thử, chưa phải bằng chứng chức năng app sai. Giữ nguyên thất bại trong kết quả tổng, chạy xác minh riêng sau.
- Bổ sung kiểm tra độ đầy đủ của heading Home và bảng dữ liệu Xu hướng. Chưa sửa mã sản phẩm ở thời điểm lập danh mục này.

## Chưa thể kết luận

TalkBack/Switch Access thực, mọi hãng máy/bàn phím, mọi trạng thái lỗi SQLite hoặc lỗi hệ thống; độ khớp Figma không được đánh giá lại khi chưa có đối chiếu frame thiết kế mới.

## Build và kiểm tra tài nguyên

- `.audit-cache/full-ui-product-build.txt`: BUILD SUCCESSFUL in 1m5s; báo cáo unit70 tests/0 failures/0 errors/0 skipped. Gradle giữ cache cho unit không đổi; không tính đây là 70 ca thiết bị mới.
- Lint XML: 0 errors/202 warnings. Các nhóm nhiều nhất:145 UnusedResources,15 Overdraw,10 SetTextI18n. Không xóa tài nguyên theo cảnh báo đơn thuần trong lượt kiểm giao diện.
- Kiểm tra lại token màu hiện hành: chữ phụ/trắng5.72:1, chữ phụ/nền app5.34:1, chữ phụ/nền nước5.10:1; chữ nước/nền nước4.83:1, chữ nguy hiểm/nền tương ứng5.12:1, cảnh báo5.41:1, trắng/teal5.35:1. Đây là phép tính trên token màu phẳng, không thay thế kiểm tra mọi pixel/trạng thái opacity hoặc TalkBack.

## Lỗi mới đã tái hiện và sửa

Baseline hợp lệ tại360dp, font200%: `.audit-cache/full-ui-large-baseline-valid.txt` thất bại với `Home date must fit inside its toolbar`. Đã xem trực tiếp cả3 ảnh `before-dashboard-heading.png`, `before-dashboard-water-total.png`, `before-stats-table-heading.png`:

1. Ngày Home bị cắt gần như toàn bộ vì toolbar cố định100dp. Đổi toolbar và khối con thành wrap_content, giữ min100dp. Đối chiếu tiếp phát hiện custom child dùng layout_gravity=top bị Toolbar đẩy lên khi vượt minHeight; đã bỏ gravity này và kiểm cả bounds tiêu đề/ngày/avatar.
2. Tổng2.250ml bị chia thành `2,25` và `0`. Ở fontScale>1.3, hai nhóm tổng/mục tiêu và nút nước xếp dọc, dùng spacing_sm hiện có. Nhánh font thường giữ bố cục hai cột. Không đổi làm tròn, mục tiêu, validation, DAO hoặc lịch nhắc.
3. Tiêu đề bảng bị ellipsis thành “Dữ liệu huy…”. Tái sử dụng view_page_toolbar đã có, set nội dung vào tv_page_title; giữ toolbar ID, Back, ListView và callback mở bản ghi.

Lượt baseline đầu lỗi fixture do thêm2.250ml trong một lần vượt giới hạn DAO2.000ml, không dùng làm bằng chứng UI. Đã sửa test thành1.000+1.250ml và bọc cleanup cả khi fixture lỗi. Lượt sau sửa đầu tiên lỗi UiAutomation.takeScreenshot trả null; chưa được tính đạt và không sử dụng ảnh cũ được pull lại làm ảnh sau sửa.

## Bộ thiết bị chạy gộp và kiểm tra lại

- Lượt76 ca bị dừng sau61 ca hoàn tất:57 qua,4 thất bại; ca nước tiếp theo đang chạy không được tính hoàn tất. Lý do dừng: timeout bảng thống kê dẫn đến ca sau chờ idle dây chuyền. Không gọi đây là một lượt76/76 đạt.
- Các thất bại: weekdayControls… RootViewWithoutFocusException; medicineEditor_keepsTimesAfterRecreation… close còn PAUSED; newestFirstRowsOpenTheirOwnRecord… onData chờ quá60s; waterPercentageAgreesAcrossTabs… chờ idle sau ca bảng.
- Ảnh chẩn đoán full-ui-diagnostic.png cho thấy màn chi tiết bản ghi07:00/60kg đã mở trong lúc onData còn chờ. Chưa có bằng chứng app chọn sai dữ liệu; test được đổi sang xác minh thứ tự adapter và bấm nhãn dòng thật bằng onView, giữ kiểm tra đúng record ID.
- Test Cancel TimePicker giữ kiểm tra isDisplayed nhưng chọn chính xác decorView lấy từ TimePicker; không bỏ assertion hoặc thay hành vi app.
- Nhóm chạy mới `.audit-cache/full-ui-water-suite.txt`: **OK (16 tests),150.96s**, gồm10 WaterJournal,1 WaterSettings,4 WaterSettingsScreen và1 UiAudit. Bao phủ15 ca còn lại và chạy lại ca phần trăm bị ảnh hưởng bởi timeout trước. Không thay mã sản phẩm để né lỗi kiểm thử.

## Kết quả rà ảnh và chạy riêng

Thư mục log: `.audit-cache/full-ui-final-runs/`. Cấu hình native1080×2400/density420/font1.0; cấu hình chữ lớn1080×2160/density480/font2.0 (360dp).

| Nhóm | Kết quả mới | Giới hạn |
|---|---|---|
| Ngày thuốc, trạng thái native | native-weekdays OK1 | Kiểm node checked và vùng chạm, chưa kiểm giọng đọc TalkBack |
| Sửa thuốc sau recreate | native-medicine-recreation OK1 | Ca từng lỗi close PAUSED đã qua khi chạy riêng |
| Home | native-home OK1; large-headings OK1 | Đã xem ảnh header/avatar đủ bounds, tổng2.250 một dòng, hai nút nước xếp dọc |
| Luồng chính/ảnh native | native-all-screens OK1,163.366s;39 PNG mới | Không mặc định mọi PNG đều đúng trạng thái chỉ dựa vào tên |
| Nhật ký thuốc/nước chữ lớn | large-journals OK1 | Tên dài và tổng nước được kiểm bằng assertion |
| Đăng ký chữ lớn | large-register OK1 | Có validation, bản nháp hai bước, IME thực |
| Sửa hồ sơ/cài nước chữ lớn | large-profile-water OK1 | Có thao tác chip, đổi chế độ, IME và lưu đúng dữ liệu |
| Đăng nhập chữ lớn | large-login OK1,26.585s | IME xác nhận bằng WindowInsets, không dựa tên ảnh |
| Form lỗi | large-invalid-forms OK1,27.076s | Lỗi giờ nước đúng ô, sửa xóa lỗi cũ; không phải mọi lỗi SQLite |
| Thống kê chạy lại theo class | native-stats không đạt | Ca empty đóng khi PAUSED; ca sau chờ dây chuyền, đã dừng riêng package test |
| Empty ở chữ lớn | large-empty không đạt | Framework TopResumedActivityChangeItem báo Activity client record null, chưa kết luận app sai dữ liệu |
| Hồ sơ/BMI/chi tiết/biểu đồ chữ lớn | large-profile-bmi-stats không đạt toàn ca | Các ảnh trước đó có giá trị; bước cuối chỉ thấy50% chart sau2 lần vuốt, chưa phải bằng chứng biểu đồ không thể cuộn |
| Accessibility chữ lớn cuối | chưa hoàn tất | Timeout khi launch Activity; sau đó xác minh hệ thống emulator ANR |

Đã xem4 bảng ảnh native mới và ảnh gốc ở những chỗ nghi ngờ. BMI hiện có ảnh hợp lệ. Ảnh water-add gốc có đầy đủ Back, label/helper và nút ghi, không quy ảnh thumbnail thành lỗi.

Các ảnh không dùng để kết luận trạng thái tương ứng:

- `splash.png` đã chuyển sang Home: chưa xác minh hình ảnh Splash mới qua ảnh này.
- `login-keyboard.png` không có IME: chỉ là ảnh login có focus; dùng lượt large-login để xác minh IME thực.
- `stats-data.png` có vùng đen ở dưới: chỉ phần chữ hiện rõ được dùng nhận diện lỗi nhãn, không dùng để kết luận bố cục toàn bảng hoặc quy vùng đen thành bug app.

## Chỉnh sửa cuối và phạm vi chưa chạy lại

Qua ảnh gốc ở chữ200% và mã nguồn còn xác minh được:

4. Tiêu đề Chi tiết lần đo bị ellipsis. Dùng lại view_page_toolbar, giữ toolbar ID và toàn bộ menu/Back; set nội dung title từ resource hiện có.
5. Bảng nước hiển thị30 lần ghi dù bảng chứa30 ngày và chỉ một ngày có dữ liệu. Dùng resource riêng30 ngày; không sửa dữ liệu hoặc phép tổng hợp.
6. Dòng hôm nay lặp `· ·` vì caller và resource cùng thêm phân cách. Bỏ phân cách trong resource; đã tìm toàn bộ caller, chỉ có một nơi dùng.
7. Ngày trong bảng dùng AbsoluteSizeSpan12dp nên không theo font hệ thống. Đổi sang kích thước12sp tính bằng TypedValue của Android, giữ nguyên màu/span nội dung.

Test được bổ sung trước các sửa nhãn: kiểm30 ngày, không lặp dấu và date span theo font lớn. Hai lần thử baseline nhãn bị chặn bởi lỗi focus/ảnh null, không gọi đây là RED nghiệp vụ hợp lệ. Các kiểm tra này **chưa được xác nhận GREEN trên thiết bị**.

Test empty đã thêm closeSoftKeyboard và xác nhận tab nền thực sự hiện lại trước thao tác tiếp. Test biểu đồ thay2 lần vuốt cố định bằng scrollTo đúng chart, vẫn giữ điều kiện hiển thị ít nhất90%. Các thay đổi test cuối đã compile, chưa chạy lại sạch vì emulator lỗi hệ thống.

## Nguyên nhân chặn xác minh cuối

`dumpsys window` ghi focus thuộc `Application Not Responding: system`. Ảnh console `.audit-cache/Screenshot_1792607965.png` xác nhận hộp thoại **Process system isn't responding**, không phải ViTa báo ANR. Máy host có khoảng474MB RAM vật lý trống trên8GB lúc kiểm tra; chưa chứng minh đây là nguyên nhân duy nhất.

Đã chọn đúng **Wait**, không chọn Close app; hộp thoại hệ thống xuất hiện lại với window ID khác. Không restart/kill emulator, không xóa dữ liệu hoặc đóng app chính. Không dùng lỗi này để bỏ qua mọi thất bại trước đó; các ca chưa qua vẫn cần chạy lại trên môi trường ổn định.

## Build cuối và bàn giao

- `.audit-cache/full-ui-delivery-build.txt`: **BUILD SUCCESSFUL in1m4s**, gồm assembleDebug, assembleDebugAndroidTest, testDebugUnitTest, lintDebug.
- **70 unit tests,0 failures,0 errors,0 skipped**. Toàn bộ85 ca Android hiện có compile được; không có một lượt85/85 đạt được xác nhận.
- Lint **0 errors/203 warnings**; tăng1 PluralsCandidate do nhãn đếm ngày mới, không phải lỗi build. Tiếng Việt hiện dùng cùng từ “ngày” cho các số lượng; chưa mở rộng ngôn ngữ trong lượt này.
- APK7.99MB (7,990,809bytes): `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`. AAPT xác nhận package **vn.edu.tdmu.vita**, không phải verification.
- Rà soát ponytail-review: chỉnh8 file sản phẩm thuộc XML/title/hiển thị Home/Stats/Detail và resource; không đổi DAO/database/schema, xác thực, quyền, thông báo, lịch nhắc hoặc dependency. Các caller toolbar/title và chuỗi phân cách đã được đối chiếu; giữ menu mở/xóa bản ghi và callback cũ.

**Kết luận:** đã rà danh mục màn/luồng và triển khai các lỗi hiển thị có bằng chứng. Home ở360dp/font200% đã có ảnh xác minh sau sửa. Tiêu đề chi tiết, nhãn/ngày bảng dữ liệu và các sửa test cuối đã build/compile nhưng chưa có ảnh chạy lại sau cùng. Chưa kết luận toàn app đạt chuẩn accessibility, chưa chấm lại điểm66/100 và chưa tuyên bố khớp Figma100%.
