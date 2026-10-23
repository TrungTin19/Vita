# ViTa — sửa lỗi và rà soát mã ngày 23/10/2026

## Phạm vi

Rà cấu trúc dự án, 48 file Java ứng dụng, manifest, Gradle, tài nguyên, các đường gọi liên quan và kiểm thử. Đánh giá theo ứng dụng SQLite cục bộ cho một người dùng trên thiết bị; không xem đây là dịch vụ nhiều người dùng đồng thời. Giữ kiến trúc Java/XML MVC, dữ liệu và các chức năng hiện có.

## Các lỗi đã xử lý

| Nhóm | Nguyên nhân và thay đổi |
|---|---|
| Huyết áp | Giá trị 80/50 bị coi là tối ưu vì chỉ có ngưỡng phía trên. Bổ sung nhóm huyết áp thấp với lời nhắc đo lại, không đưa lời khuyên dành cho tăng huyết áp. Giữ ưu tiên nhóm huyết áp cao khi các chỉ số rơi vào mức cao hơn. |
| Thông báo thuốc | Thao tác trên thông báo cũ đọc tên/liều mới từ database. Thông báo nay mang thông tin lịch đã hiển thị; receiver đối chiếu tên, liều, giờ, ngày trong tuần và trạng thái hoạt động trước khi ghi. Thông báo không còn khớp sẽ yêu cầu mở app kiểm tra, không tạo bản ghi sai. Chỉ báo thành công sau khi lưu được dữ liệu. |
| Ghi nước | Biểu mẫu mới dùng giờ lúc mở nhưng ngày lúc lưu. Nay lấy một timestamp tại lúc lưu và lấy ngày từ chính timestamp đó; áp dụng cả ghi nhanh và nút ghi từ thông báo. Sửa bản ghi cũ vẫn giữ ngày và giờ người dùng chọn. |
| Back trên màn ngang | Toolbar căn nút theo mép trên và chiều cao tối thiểu khiến nút tràn vùng hiển thị. Căn giữa theo chiều dọc trong style dùng chung; giữ nguyên kích thước vùng chạm và kiểm thử không nới điều kiện click. |
| Mật khẩu | Thay SHA-256 một lần bằng PBKDF2: SHA256/600.000 vòng trên nền tảng có hỗ trợ, SHA1/1.400.000 vòng để tương thích Android 7. Chuỗi lưu có phiên bản, kiểm tra định dạng trước khi tính, so sánh bằng `MessageDigest.isEqual`. Tài khoản SHA-256 cũ chỉ được nâng cấp sau khi xác minh đúng mật khẩu, không đổi schema hoặc xóa hồ sơ. |
| Tác vụ tài khoản | Đăng nhập, đăng ký và xác nhận xóa chạy nền. Đăng ký giữ tác vụ/kết quả qua tạo lại Activity và ngăn gửi lặp. Xóa hồ sơ vẫn hủy báo thức, thông báo và phiên của đúng chủ tài khoản khi Activity cũ đã bị hủy; không đăng xuất một tài khoản khác. |

Mốc huyết áp thấp tham khảo [NHS](https://www.nhs.uk/conditions/low-blood-pressure-hypotension/). Tham số PBKDF2 đối chiếu [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html). SHA1 là đường tương thích nền tảng cũ, không phải lựa chọn ưu tiên cho nền tảng mới.

## Dọn mã và file

- Bỏ khai báo trực tiếp ConstraintLayout và hai dòng catalog tương ứng: không có layout hoặc Java ứng dụng nào tham chiếu ConstraintLayout. Thư viện khác vẫn tự quản lý dependency bắc cầu của chúng.
- Bỏ 5 import không dùng trong các adapter, ReminderFragment và HealthChartData.
- Bỏ điều kiện SDK lặp trong thao tác mở cài đặt báo thức; dùng inflater của DialogFragment cho toolbar của nhật ký nước.
- Cursor đăng nhập được đóng bằng try-with-resources, kể cả khi không tìm thấy tài khoản.
- Kiểm tra tham chiếu Java, manifest và Lint không phát hiện file ứng dụng nào đủ bằng chứng là không dùng để xóa. Không xóa file chỉ vì ngắn hoặc tên giống nhau.
- `BmiActivity` vẫn là đích điều hướng riêng, được khai báo trong manifest và dùng từ Home/Hồ sơ. Các thư mục mipmap cung cấp icon theo mật độ và adaptive icon. Hai XML adaptive icon có cùng nội dung nhưng là hai tên tài nguyên được manifest sử dụng.
- Không còn thư mục `network` trong mã ứng dụng. Không phát hiện file APK, class, log hay bản sao tạm được Git theo dõi.
- Giữ tài liệu môn học, hồ sơ thiết kế/lịch sử và cấu hình công cụ của người dùng. Thư mục build/cache và bằng chứng kiểm thử được Git bỏ qua, không được coi là mã sản phẩm.

## Kiểm chứng trước bước dọn cảnh báo Lint

- Đã thấy kiểm thử mới thất bại trước sửa ở huyết áp thấp, định dạng băm mới, thao tác thuốc cũ và thời điểm lưu nước.
- Ca Back trên viewport ngang 2400×1080 thất bại trước sửa và đạt sau sửa bằng cùng kiểm thử click, không giảm yêu cầu vùng hiển thị.
- Đăng ký/xóa hồ sơ qua tạo lại Activity được kiểm tra bằng chặn tác vụ DAO, tạo lại màn hình rồi cho tác vụ tiếp tục; không phụ thuộc vào thời gian băm nhanh/chậm.
- 73/73 unit test đạt; 0 lỗi, 0 bỏ qua. Có vector PBKDF2-SHA1 với mật khẩu tiếng Việt và kiểm thử tương thích mật khẩu cũ.
- Build debug và build Android test thành công. Lint: 0 lỗi, 55 cảnh báo, không có `UnusedResources`.
- Lượt Android tổng thể: **90/90 đạt trong cùng một lượt chạy**, thời gian 860,532 giây; không ghép kết quả các lượt chạy lẻ. Package kiểm thử riêng: `vn.edu.tdmu.vita.verification`, không sử dụng database app chính.
- Bản cuối cùng trên viewport ngang 2400×1080, density 420, font 100%: **5/5 đạt**, 83,645 giây. Đã xem ảnh bảng dữ liệu: nút Back hiển thị trọn vẹn, tiêu đề và giá trị có đơn vị đọc được. Ảnh lưu ở `.audit-cache/fix-final-visual/wide/`.
- Review độc lập lần hai không còn phát hiện bắt buộc sửa trong diff; review này là đọc mã, không thay thế kiểm thử chạy thật.

## Giới hạn và nợ kỹ thuật

- Chưa chạy trực tiếp provider mật mã trên thiết bị API 24; vector SHA1 đã được kiểm chứng ở JVM. Chưa xác minh trên điện thoại thật hoặc toàn bộ ROM/OEM.
- Không tuyên bố giao diện khớp Figma 100% hoặc đã kiểm chứng đầy đủ TalkBack/Switch Access.
- Lượt đầu còn 55 cảnh báo Lint; sau bước dọn bổ sung bên dưới còn 36. Các cảnh báo cần phân biệt theo ngữ cảnh, không đồng nghĩa cùng số lượng lỗi chức năng.
- Một `shortcut:` còn lại: tuổi BMI được ước lượng từ năm sinh; cần ngày sinh đầy đủ nếu phạm vi sản phẩm sau này đòi hỏi tuổi chính xác. Không tự bổ sung trường dữ liệu ngoài yêu cầu.
- Chưa commit hoặc push các sửa đổi của lượt này.

Bằng chứng chi tiết trong `.audit-cache/fix-*.txt` (bỏ qua bởi Git) và báo cáo build/unit test trong `Vita_Project/app/build`.

## Bổ sung: dọn cảnh báo Lint

Sau lượt 90/90 nói trên, đã thực hiện thêm một nhóm chỉnh sửa hiển thị có phạm vi nhỏ:

- Chuyển chuỗi ghép và nhãn viết trực tiếp sang tài nguyên: huyết áp một phần, thông tin tài khoản, lịch nước, nút BMI, lượng nước mới nhất và nhãn điểm biểu đồ. Giữ định dạng và dữ liệu thực tế hiện có.
- Dùng tài nguyên cho giới tính mặc định và dấu chưa ghi nhận; sửa dấu ba chấm trong gợi ý ghi chú.
- Để AppCompatDialog nạp layout vào đúng khung nội dung; form mật khẩu xóa hồ sơ được nạp với FrameLayout cha thực tế. Giữ các nút, màu và quy tắc kích thước hộp thoại.
- Đưa MPAndroidChart vào version catalog, giữ nguyên phiên bản, không thêm thư viện.

Build debug thành công; **73/73 unit test đạt**, Lint còn **36 cảnh báo, 0 lỗi** (giảm 19 cảnh báo từ mốc 55). Không dùng suppression để giảm số lượng cảnh báo. Review mã không phát hiện lỗi bắt buộc sửa trong nhóm này.

| Cảnh báo còn lại | Số lượng | Lý do chưa tự thay đổi |
|---|---:|---|
| Overdraw | 15 | Lint báo khả năng vẽ nền trùng; không xóa nền của màn/fragment/toolbar dùng chung chỉ để giảm cảnh báo, khi chưa đo và kiểm từng ngữ cảnh hiển thị. |
| UnusedAttribute | 5 | Thuộc tính heading native có từ API 28. Giữ thông tin trợ năng cho nền tảng hỗ trợ; chưa xác minh khả năng trợ năng tương ứng trên Android 7. |
| UseCompoundDrawables | 4 | Gợi ý tối ưu cấu trúc, không phải lỗi chức năng; chưa thay thành phần icon/chữ vì có thể ảnh hưởng bố cục khi phóng chữ. |
| RtlSymmetry | 3 | Nút văn bản căn sát lề đầu có padding bất đối xứng; chưa thay khoảng cách hiển thị theo một giá trị đoán. |
| NotifyDataSetChanged | 2 | Danh sách cục bộ được tải lại toàn bộ. Không thêm DiffUtil chỉ để xóa cảnh báo khi chưa có vấn đề hiệu năng được xác nhận. |
| PluralsCandidate | 2 | Tiếng Việt dùng cùng dạng từ cho số tuổi/ngày; chưa cần đổi sang cấu trúc plurals. |
| CustomSplashScreen | 1 | Giữ màn splash hiện có theo thiết kế. |
| TypographyFractions | 1 | “1/2” là số thứ tự bước đăng ký, không đổi thành ký hiệu phân số “½”. |
| ViewConstructor | 1 | ChartMarker được tạo bằng mã với nhãn và đơn vị, không được nạp trực tiếp từ XML. Không thêm constructor thiếu dữ liệu chỉ để phục vụ công cụ preview. |
| OldTargetApi / GradleDependency | 2 | Nâng target/compile SDK cần kiểm tra tương thích riêng; giữ cấu hình hiện tại của dự án môn học. |

Môi trường chỉ có system image Android 13/API 33; không có Android 7/API 24. Không tự tải thêm image dung lượng lớn. Sau nhóm sửa mới, **20/20 ca hồi quy Android đạt trong một lượt**, 164,606 giây: hộp thoại thuốc, xóa hồ sơ, vòng đời tài khoản, hiển thị huyết áp một phần, bảng dữ liệu, đăng ký, sửa hồ sơ và ảnh hồ sơ dài/chi tiết/BMI/biểu đồ. Lượt 90/90 phía trên thuộc bản trước nhóm dọn Lint này; không cộng hai lượt thành số ca kiểm thử mới.

Đã xem ảnh hồ sơ mới tại `.audit-cache/lint-cleanup-visual/profile.png`: tên dài xuống dòng, tên tài khoản có ký tự `@`, nút BMI giữ đúng số và phân loại. Dữ liệu ảnh là fixture kiểm thử, không được chèn vào ứng dụng chính.

Lượt hồi quy đầu của nhóm dọn Lint bị dừng vì cửa sổ `Application Not Responding: com.android.systemui` chiếm focus; log sự kiện ghi nhận nhiều dịch vụ hệ thống ANR ngay sau khởi động. Đã lưu `.audit-cache/lint-cleanup-anr-window.txt` và `lint-cleanup-anr-events.txt`, khởi động lại emulator rồi chạy lại cùng kiểm thử, không thay điều kiện kiểm tra. Không tính lượt bị dừng là kết quả kiểm thử chức năng đầy đủ.

APK debug mới: `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`. SHA-256: `974EA46D90E623D4665590AF91269D395EAB849F0EAF51E7AFBEC6972AA599D3`.

## Review và xác minh cuối ngày 23/10

Đã thực hiện Ponytail, Ponytail Review, review độc lập, Verification Before Completion, Ponytail Audit và quét ghi chú nợ kỹ thuật. Review độc lập không phát hiện lỗi bắt buộc sửa còn lại trong các thay đổi sau bản vá cuối.

Audit toàn dự án xác nhận thêm lỗi P2: nếu tiến trình bị dừng sau khi SQLite xóa hồ sơ nhưng trước khi xóa SharedPreferences, phiên cũ vẫn mở trang chính dù chủ hồ sơ không còn tồn tại. Bổ sung kiểm thử `deletedProfileSessionIsClearedAndReturnsToLogin`: trước sửa thất bại đúng tại điều kiện phiên chưa được xóa; sau sửa đạt trong bộ kiểm thử đầy đủ. `MainActivity.onResume()` kiểm tra hồ sơ còn tồn tại trước khi tiếp tục phiên, xóa phiên không hợp lệ rồi chuyển về đăng nhập bằng luồng hiện có. Không thay đổi schema hoặc dữ liệu hồ sơ còn hợp lệ.

Kết quả trên bản mã cuối:

- Build APK debug và APK kiểm thử riêng đều thành công.
- **73/73 unit test đạt**, không lỗi, không bỏ qua.
- **91/91 kiểm thử Android đạt trong một lượt**, 1.018,515 giây trên emulator API 33. Bao gồm ca phiên hồ sơ đã xóa vừa bổ sung và các thay đổi chuỗi/dialog sau lượt 90 ca trước đó. Log: `.audit-cache/final-reviewed-full-device.txt`.
- Lint **0 lỗi, 36 cảnh báo**, phân loại và lý do giữ lại ở bảng phía trên. `git diff --check` không phát hiện lỗi khoảng trắng.
- Quét file Git không thấy APK, class, log hoặc tmp được theo dõi. Không tìm thêm file ứng dụng đủ bằng chứng để xóa an toàn; giữ Activity BMI và tài nguyên launcher đang được tham chiếu.

APK cuối: `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`. SHA-256: `67462CC8B3FFC9CB9BA649C3D698668C6FD90025A450440F67CCACCF5663A141` (thay thế mã hash ở mốc trước).

Giới hạn: kiểm thử thiết bị lần này chỉ trên API 33; không thực hiện bước Android 7 theo yêu cầu bỏ qua của người dùng. Chưa xác nhận trên điện thoại thật, TalkBack/Switch Access toàn bộ luồng hoặc mọi cỡ màn hình. Ảnh kiểm thử không chứng minh khớp Figma 100%. Nhánh hiện tại là `main`; thay đổi chưa commit/push, không có thao tác merge hoặc xóa nhánh trong lượt review này.

## Đơn giản hóa theo Ponytail

Theo yêu cầu tiếp theo, đã rút gọn 4 file, giảm ròng 47 dòng:

- `HealthDao`, `MedicineDao`, `WaterDao`: dùng try-with-resources thay cho các nhánh kiểm tra và đóng Cursor thủ công. SQLiteDatabase trả Cursor hoặc ném lỗi; không cần nhánh Cursor null. Cursor vẫn được đóng nếu xử lý dữ liệu ném lỗi.
- `HealthDao`: dùng trực tiếp các overload ContentValues.put nhận Integer/Float nullable, thay cho các nhánh put/putNull lặp lại. Giữ giá trị SQL NULL cho chỉ số chưa đo.
- `MedicineSchedule`: hàm private nhận giờ đã được normalizeTimes kiểm tra, bỏ việc tách/kiểm tra lại định dạng và khoảng giờ. Giữ validation ở hàm công khai, quy tắc ngày trong tuần và thời điểm nhắc kế tiếp.

Không thêm lớp hoặc thư viện. Giữ cấu trúc MVC, SQL, điều kiện quyền sở hữu và các hàm đang được gọi. Không tìm thêm hàm đủ bằng chứng để xóa hoàn toàn. Các file giao diện dài chưa bị chia lớp chỉ để giảm độ dài file.

Xác minh sau refactor: **73/73 unit test**, **23/23 kiểm thử Android liên quan** trong một lượt 199,303 giây; build APK chính thành công; Lint 0 lỗi, 36 cảnh báo; git diff --check đạt. Các nhóm Android gồm AccountIsolation, Measurement, FunctionalRegression, WaterJournal, MedicineDose, HealthComparison. Log: `.audit-cache/simplify-verify-build.txt`, `simplify-device-tests.txt`, `simplify-final-apk.txt`. Lượt 91 ca phía trên thuộc bản trước refactor; không tuyên bố đã chạy lại toàn bộ 91 ca sau nhóm thay đổi này. Chưa commit/push.

### Tiếp tục rút gọn màn Xu hướng

`StatsFragment`: dùng chung cấu hình biểu đồ đường/cột qua lớp nền có sẵn của MPAndroidChart; giữ các thuộc tính riêng của từng loại. Dữ liệu nước chỉ truy vấn một lần trong mỗi lượt tải, dùng cho cả biểu đồ, tóm tắt và bảng. Danh sách điểm đường huyết được tính một lần rồi phân nhóm theo bữa ăn, thay vì tính lại cho từng nhóm. Không thay layout, giá trị màu/cỡ chữ, truy vấn lọc hoặc quy tắc nghiệp vụ.

Build APK chính và gói verification thành công; **73/73 unit test và 6/6 ca Android liên quan đạt**, lượt Android 65,212 giây. Bao gồm ba ca StatsData, giữ khoảng ngày qua đổi tab/tạo lại màn hình, tiêu đề/bảng nước 30 ngày và chụp biểu đồ huyết áp. Đã xem ảnh `.audit-cache/simplify-stats-chart.png` và `simplify-stats-water-table.png`; số đo 118/78 mmHg, tổng nước 2250 ml và trạng thái chưa ghi nhận hiển thị trong ảnh fixture. Lint vẫn 0 lỗi, 36 cảnh báo. Log: `.audit-cache/simplify-stats-build.txt`, `simplify-stats-device-tests.txt`. Chưa chạy lại toàn bộ 91 ca trên bản này; chưa commit/push.

### Tiếp tục rút gọn luồng lưu chỉ số sức khỏe

`AddHealthActivity.handleSave`: gộp phần gán giá trị bản ghi, phản hồi thành công/thất bại và đóng màn hình giữa thêm mới và chỉnh sửa; giảm ròng 14 dòng ứng dụng. Giữ validation riêng từng trường, kiểm tra phiên/chủ dữ liệu, không tạo bản ghi mới khi bản ghi cần sửa không còn tồn tại, giữ thông báo lỗi riêng cho thêm/sửa.

Thêm kiểm thử hành vi `healthEditor_addThenEditKeepsOneRecordAndClearsRemovedMetric`: đạt trên mã trước refactor (15,407 giây), sau đó đạt lại trên mã đã rút gọn. Kiểm tra chỉ có một bản ghi sau thêm/sửa, chỉ số bị xóa lưu NULL, giữ ngày/giờ đo và bối cảnh bữa ăn. Bộ hồi quy sau sửa **6/6 ca Android đạt** trong 91,662 giây, gồm thêm/sửa, giữ giờ/bữa ăn sau tạo lại màn hình, ba ca cách ly tài khoản và lỗi nhập form. **73/73 unit test đạt**, build APK chính và verification thành công; Lint 0 lỗi, 36 cảnh báo; git diff --check đạt. Log: `.audit-cache/simplify-health-baseline-test.txt`, `simplify-health-build.txt`, `simplify-health-device-tests.txt`. Tổng số ca Android trong mã nguồn hiện là 92 do bổ sung một ca, chưa chạy lại toàn bộ sau nhóm refactor này. Chưa commit/push.

## Rà soát toàn dự án và xác minh sau tất cả refactor

ViTa là ứng dụng sức khỏe Android Java/XML, SQLite cục bộ; đánh giá theo tải một người dùng trên thiết bị, có nhiều hồ sơ và báo thức nền. Kiểm kê mã ứng dụng hiện có 48 file Java, 77 file XML và 18 ảnh PNG/WebP. Đã rà cấu trúc, Manifest/quyền, cấu hình Gradle, tài nguyên qua Lint, tham chiếu mã/model, các luồng nhập/lưu/đọc dữ liệu, phiên tài khoản, thuốc/nước và migration. Không đọc lại từng dòng tài liệu lịch sử hoặc mã thư viện bên ngoài.

Các phần dọn thêm trong lượt này:

1. `UserDao`: thay đóng Cursor thủ công bằng try-with-resources cho kiểm tra tên và tải hồ sơ; bỏ kiểm tra Cursor null trùng trong đăng nhập. Giữ truy vấn có tham số, kiểm tra mật khẩu và nâng cấp hash cũ.
2. `MedicineAdapter`: bỏ tham số position không dùng của bind; bỏ listener nhấn giữ và biến cardView dư vì chính onCreateContextMenu đã lấy vị trí hiện tại. Kiểm thử xóa thuốc qua menu nhấn giữ đạt trong lượt đầy đủ.
3. Bỏ bốn biến chỉ khai báo/gán nhưng không đọc: tilNote của AddHealthActivity, tilDosage của AddMedicineActivity, tilFullname của EditProfileActivity và RegisterActivity. Giữ các view/layout và dữ liệu ô nhập.
4. `WaterReminderReceiver`: bỏ kiểm tra user null lần hai sau nhánh đã return khi user null. Giữ kiểm tra phiên, chủ hồ sơ, khung giờ và mục tiêu.

Không tìm thêm file ứng dụng đủ bằng chứng để xóa. Constructor model còn được unit test sử dụng; Activity BMI và mipmap còn được Manifest/điều hướng tham chiếu. Giữ migration và hủy báo thức cũ để bảo toàn dữ liệu/cài đặt đã có. Các XML backup_rules/data_extraction_rules là quy tắc loại trừ dữ liệu khỏi sao lưu hệ thống, không phải chức năng sao lưu người dùng. Không thấy quyền INTERNET hoặc mã API thời tiết/đặt lịch giấc ngủ trong nguồn main. Không thấy APK/class/log/tmp được Git theo dõi.

Review độc lập không có phát hiện có thể hành động trong các refactor đã kiểm tra, gồm các luồng DAO/quyền sở hữu, số đo nullable, lưu thêm/sửa, tính lịch, biểu đồ, menu thuốc, hash mật khẩu và vòng đời tài khoản. Đây không phải bằng chứng dự án không thể có lỗi.

**Kết quả cuối thay thế các mốc kiểm thử riêng phía trên:**

- Build APK chính và APK verification thành công.
- **73/73 unit test đạt**, 0 lỗi, 0 bỏ qua.
- **92/92 kiểm thử Android đạt trong một lượt trên API 33**, 762,813 giây (khoảng 12 phút 43 giây), sau toàn bộ các refactor được ghi trong báo cáo. Không cộng số ca giữa các lượt.
- Lint **0 lỗi, 36 cảnh báo**; phân loại/lý do giữ ở bảng trước. git diff --check không báo lỗi khoảng trắng.
- Log: `.audit-cache/whole-review-complete-build.txt`, `whole-review-complete-device-build.txt`, `whole-review-full-device.txt`.
- APK: `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `94D8F3589388F6CA389B654FB29985B15DD482B851F0E57D2A806C873037FCEF`.

Chưa commit/push. Không xác nhận Android 7 (người dùng đã bỏ qua bước này), thiết bị thật, trợ năng toàn diện hoặc Figma 100%. Giới hạn tuổi BMI ước lượng từ năm sinh vẫn được giữ theo phạm vi hiện tại; không tự thêm trường ngày sinh hoặc kiến trúc mới chỉ để giảm độ dài file.
