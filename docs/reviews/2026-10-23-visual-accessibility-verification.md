# ViTa — xác minh giao diện thực tế và trợ năng

> Cập nhật sau sửa ngày 23/10: lỗi Back đã được sửa ở style dùng chung. Bản cuối chạy đạt 5/5 ca trên viewport ngang 2400×1080 và đã xem ảnh xác nhận nút không còn bị cắt. Bộ Android tổng thể cũng đạt 90/90. Xem [báo cáo sửa lỗi và dọn mã](2026-10-23-fix-and-cleanup.md). Các giới hạn về thiết bị thật, Figma và kiểm chứng trợ năng toàn diện vẫn còn; nội dung dưới đây là bằng chứng trước sửa.

Ngày 23/10/2026. Kiểm tra commit `a21c54f`, Java/XML Views, MVC hiện có. Lượt này chỉ kiểm tra và ghi báo cáo; không sửa mã ứng dụng hoặc kiểm thử.

## Kết quả

Các sửa cuối ở tiêu đề chi tiết, tiêu đề bảng xu hướng, nhãn 30 ngày và cách hiển thị ngày đã có ảnh chạy thật mới. Hai cấu hình dọc đạt các ca trọng điểm. Viewport ngang thật phát hiện một lỗi vùng hiển thị nút Back trong bảng dữ liệu. Chưa đủ bằng chứng để xác nhận giao diện đạt chuẩn trợ năng toàn diện hoặc khớp Figma 100%.

| Cấu hình / lượt | Kết quả | Phạm vi thực tế |
|---|---|---|
| 1080×2400, density 420, font 100% | **5/5 đạt**, 43,638 giây | Tiêu đề/bảng dữ liệu, hồ sơ dài/BMI/chi tiết/biểu đồ, 3 ca thuộc tính trợ năng và bàn phím |
| 1080×2160, density 480, font 200% — chiều rộng 360dp | **9/9 đạt**, 49,938 giây | Nhóm trên; thêm đăng nhập với IME thật, đăng ký/validation, sửa hồ sơ/cài nước, form lỗi |
| Yêu cầu xoay bằng setting, kích thước gốc | 5/5 đạt nhưng **không tính là kiểm tra ngang** | Ảnh mới vẫn là 1080×2400; tên thư mục `landscape` không chứng minh hướng ngang |
| Viewport 2400×1080, density 420, font 100% | **4/5 đạt, 1 lỗi** | Đã xem ảnh ngang thật; lỗi bấm Back trong bảng dữ liệu, không bỏ điều kiện vùng hiển thị 90% |
| TalkBack thực | **Xác minh một phần** | Dịch vụ bind, touch exploration bật, thấy khung focus ở màn đăng nhập, Home và khi mở hồ sơ; chưa xác minh lời đọc và toàn bộ thứ tự duyệt |
| Switch Access thực | **Chưa kiểm được** | Không có dịch vụ Switch Access trong emulator này |

Đây là các lượt kiểm tra riêng theo cấu hình, không cộng thành một lượt hồi quy toàn app. Kết quả 85 ca ở bước trước nằm trong `2026-10-23-android-device-rerun.md`.

## Môi trường và dữ liệu

- Pixel_6, Android 13 / API 33, emulator không cửa sổ, 2 lõi CPU, RAM 1.536 MB.
- Sử dụng hai APK verification đã build ở bước trước từ cùng commit; không đổi mã nguồn nên không build lại hoặc chạy lại unit test trong lượt này.
- Package đích `vn.edu.tdmu.vita.verification`. Dữ liệu mẫu chỉ thuộc gói kiểm thử; không cài đè, xóa hoặc thêm fixture vào app chính.
- Lượt kiểm tra tập trung không xuất hiện thông báo ANR trong những lần kiểm tra cửa sổ được ghi nhận; không suy ra emulator ổn định tuyệt đối.
- Đã hoàn trả và đọc lại: kích thước vật lý 1080×2400, density 420, font 1.0, tự xoay 1, user_rotation 0, enabled_accessibility_services null, accessibility_enabled 0. Đã tắt emulator do lượt này khởi động; không xóa AVD.

## Các sửa cuối đã xác minh bằng ảnh mới

1. **Chi tiết lần đo:** tiêu đề đầy đủ ở dọc chuẩn, dọc 360dp/font 200% và viewport ngang. Ở chữ lớn tiêu đề xuống dòng; không dùng dấu ba chấm. Fixture 118/78 giữ giá trị trên một dòng, kèm đơn vị riêng.
2. **Bảng huyết áp:** tiêu đề “Dữ liệu huyết áp” đọc được; giá trị có `mmHg`, ngày và trạng thái chưa ghi giờ hiện đúng theo fixture. Ở font 200%, đơn vị có thể xuống dòng, không bị xóa.
3. **Bảng nước:** ảnh dọc mới ghi đúng “30 ngày”; dòng hiện tại không lặp `· ·`; ngày theo cỡ chữ hệ thống. Nội dung dài xuống dòng và danh sách có thể cuộn. Lượt ngang thất bại ở bước đóng bảng huyết áp trước khi tạo ảnh bảng nước, nên không dùng ảnh bảng nước cũ làm bằng chứng ngang.
4. **Hồ sơ/BMI/Xu hướng:** đã xem ảnh hồ sơ tên dài, BMI và biểu đồ huyết áp ở dọc chuẩn; ảnh biểu đồ ngang cho thấy đơn vị, trục và chú giải hiển thị. Ca tương ứng cũng kiểm tra cuộn để thấy tối thiểu 90% biểu đồ.
5. **Bàn phím/form:** ca IME xác nhận bàn phím qua WindowInsets, không dựa tên ảnh. Ảnh đăng nhập chữ lớn được chụp sau khi test chủ động cuộn tới nút đăng nhập; không dùng phần ô tên bị cuộn ra khỏi viewport trong ảnh này để kết luận lỗi tự che focus.

## Lỗi mới được tái hiện

**P1 khi hỗ trợ ngang — nút Back của bảng dữ liệu bị cắt một phần.**

- Log `2026-10-23-visual-wide-viewport.txt`: `dashboardAndStatsTableKeepTheirFullHeadings` thất bại ở dòng 101 khi Espresso bấm nút có mô tả “Quay lại” trong dialog.
- Nguyên nhân trực tiếp do runner báo: vùng nhìn thấy của nút không đạt điều kiện 90%. Target `AppCompatImageButton` có kích thước 127×126 px.
- Ảnh `wide-viewport/figma-stats-table-heading.png` cho thấy nền trắng của nút Back bị cắt ở phía dưới. Tiêu đề và dữ liệu huyết áp vẫn hiện đầy đủ.
- Không coi đây là lỗi chập chờn focus của lượt 85 ca trước: lần này lỗi cụ thể là ràng buộc vùng nhìn thấy.
- Chưa xác định xong nguyên nhân gốc giữa chiều cao/padding/gravity của Toolbar dùng chung và bố cục cửa sổ ngang. Cần kiểm bounds thực của Toolbar/nút trước khi sửa. Chưa sửa app hoặc nới assertion để làm test qua; chưa kiểm thao tác Back hệ thống như một cách thay thế.

Đây là lỗi còn mở sau lượt kiểm tra, không phải một sửa đã được xác nhận.

## TalkBack và Switch Access

Đã liệt kê dịch vụ cài sẵn: TalkBack, Accessibility Menu và Select to Speak; không có Switch Access.

Đã bật `com.google.android.marvin.talkback/.TalkBackService`, xác nhận `touchExplorationEnabled=true`, dịch vụ nằm trong Bound services và không có Crashed services. Ảnh thấy khung focus xanh ở nút Back của đăng nhập; sau các thao tác trên ứng dụng có khung focus trên heading Home và nút Back của hồ sơ.

Giới hạn: thao tác được gửi qua ADB; các ảnh không chứng minh đầy đủ cử chỉ vật lý, thứ tự đọc, cách phát âm tiếng Việt, thông báo lỗi được đọc hoặc việc duyệt toàn app không bị mắc kẹt. Không thu/nghe audio trong lượt này. Các màn Home/hồ sơ ở phiên smoke test này có thể đã mất fixture sau teardown; không dùng các trường trống đó làm bằng chứng đánh giá dữ liệu nghiệp vụ.

Ba ca AccessibilityDeviceTest kiểm vùng chạm, checked state, focus bàn phím và mở picker bằng Enter ở các cấu hình nêu trên. Chúng không thay thế kiểm thử bằng TalkBack/Switch Access thực.

## Đối chiếu Figma

Đã đọc metadata của file `zHHRPnM4rgs3zyr8Oz7v6x`, trang `0:1`: bộ thiết kế mô tả Android tiếng Việt, frame 412×892, Roboto và các thành phần nút/input dùng chung.

Lần đọc tiếp bị MCP trả lỗi **“You've reached the Figma MCP tool call limit on the Starter plan”**. Chưa lấy được ảnh frame mới để đối chiếu ảnh Android trong lượt này. Metadata chỉ mô tả cấu trúc/kích thước; không đủ xác nhận màu, font, khoảng cách hay mức khớp từng pixel. Không kết luận khớp Figma 100%.

## Bằng chứng cục bộ

Ảnh nằm trong `.audit-cache/visual-2026-10-23/`:

- `native/`: ảnh của lượt dọc chuẩn 5 ca.
- `small-font200/`: ảnh mới từ lượt 360dp/font 200%.
- `landscape/`: ảnh của lượt đặt rotation nhưng thực tế vẫn dọc; không dùng làm bằng chứng ngang.
- `wide-viewport/`: ảnh 2400×1080 thật của bảng huyết áp, chi tiết, biểu đồ và ngày thuốc. Không có ảnh bảng nước mới của lượt này.
- `talkback-initial.png`, `talkback-focus1.png`, `talkback-profile-button.png`: các trạng thái focus đã xem. Ảnh khác trong cùng thư mục là dữ liệu chẩn đoán, không tự mặc định là đạt chỉ vì tên tệp.

Log:

- `.audit-cache/2026-10-23-visual-native.txt`
- `.audit-cache/2026-10-23-visual-small-font200.txt`
- `.audit-cache/2026-10-23-visual-landscape.txt`
- `.audit-cache/2026-10-23-visual-wide-viewport.txt`
- `.audit-cache/2026-10-23-accessibility-before.txt`
- `.audit-cache/2026-10-23-talkback-active.txt`
- `.audit-cache/2026-10-23-talkback-active-portrait.txt`
- `.audit-cache/2026-10-23-talkback-final-state.txt`

## Phần chưa thể khép lại

- Sửa và xác minh lại lỗi nút Back của bảng dữ liệu ở viewport ngang.
- Chưa kiểm xoay giữa chừng với form đang nhập; đặt viewport ngang từ đầu không thay thế kiểm tra phục hồi trạng thái sau xoay.
- Chưa kiểm Switch Access, giọng đọc/thứ tự đọc toàn app, thiết bị thật, mọi cỡ màn hình, tablet/foldable hoặc chữ lớn đồng thời với ngang.
- Chưa audit ảnh đầy đủ mọi màn/trạng thái sau cùng, chưa đối chiếu ảnh Figma mới và chưa chấm lại điểm UI/UX 66/100.
