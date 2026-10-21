# ViTa — báo cáo triển khai file Figma mới

Thiết kế: https://www.figma.com/design/zHHRPnM4rgs3zyr8Oz7v6x/Untitled?node-id=0-1

Bảng 21 frame chính, node mới, màn Android và luồng điều hướng nằm trong [kế hoạch đối chiếu](../superpowers/plans/2026-10-21-vita-new-figma.md). Đã đọc trực tiếp các frame chính, component Button/Input và các dialog, trạng thái biểu đồ trống/một lần đo qua trình duyệt. Không dùng node của file cũ thay cho file mới.

## Thay đổi

- Header dùng chung: cỡ chữ, kiểu đậm, khoảng trái, nút quay lại nền trắng; trang nước/lịch sử thuốc có cùng chiều cao phần header. Giữ các hành động điều hướng và menu đang có.
- Home: chiều cao dòng thực trong Android được điều chỉnh theo line box của Figma; thẻ thuốc, khoảng cách giữa các thẻ và hàng mục tiêu nước sát mẫu hơn. Thanh điều hướng nền trắng, loại màu phủ của Material; thanh tiến độ nước bo tròn cả đầu đang chạy.
- Các form: thống nhất giãn dòng tương thích Android24, nền trợ giúp bo góc, nút cao tối thiểu52dp và có thể tăng chiều cao theo font. Form nước cho phép vùng nội dung co lại khi bàn phím mở.
- Hồ sơ: nhãn theo kiểu chữ của thiết kế, loại viền không có trong mẫu, thu gọn khoảng cách hàng; giữ tên/tài khoản/thông số thật, không thay bằng số mẫu.
- Chi tiết số đo: đưa đơn vị về cùng hàng với giá trị, mỗi card có ID riêng để giữ đúng việc ẩn thông số thiếu. Không đổi đánh giá sức khỏe hay truy vấn bản ghi trước.
- Dialog hồ sơ/nước/xóa thuốc/xóa chỉ số/rời form/cữ thuốc dùng chung vỏ `VitaDialog`, tái sử dụng layout đã có. Bỏ các bản sao tạo vỏ dialog riêng. Password validation, session ownership và các callback dữ liệu vẫn nằm ở nơi xử lý cũ.
- Cữ thuốc có các hành động đã uống/bỏ qua/hủy/bỏ xác nhận theo trạng thái thật. Lịch sử thuốc có nút xóa cuối trang, dùng cùng thao tác xóa với menu cũ. Không đổi DAO hay receiver.
- Xu hướng phân biệt không có dữ liệu, một lần đo và nhiều lần đo; trạng thái trống dẫn tới đúng form hiện có. Một điểm được đặt giữa vùng biểu đồ, không tạo dữ liệu để vẽ đường giả.

## Bằng chứng

- Figma MCP trả lỗi hết hạn mức Starter. Dùng ngoại lệ đọc trình duyệt người dùng đã đồng ý; không dựa trên thông số phỏng đoán khi chưa đọc được frame.
- PNG H01 gốc từ file mới: `.audit-cache/new-figma-H01-reference.png`, 824×1784. Chỉ ảnh đã kiểm tra đúng nội dung mới được dùng đo; các lần xuất còn giữ clipboard của frame trước đã bị loại bỏ.
- Đã chụp 26 màn ở vòng đầu và vòng sửa header/typography; ảnh vòng sau nằm trong `.audit-cache/new-figma-refined-screenshots/`. Home đã được so trực tiếp với PNG Figma; các nhóm khác được so với ảnh frame đọc trong trình duyệt.
- So ở chiều rộng412dp: Android824×1864, density320; status bar thực98px và thanh cử chỉ48px được tính riêng. Figma có status bar mô phỏng64px. Không thêm thanh hệ thống giả vào app.
- Gradle configuration cache từng làm đường dẫn APK/configuration không tương ứng với gói kiểm thử. Các lượt xác minh sau dùng `--no-configuration-cache` và kiểm tra applicationId bằng aapt trước khi cài. Không cài gói chính vào máy để thử với fixture.
- `.audit-cache/new-figma-dialog-device3.txt`: `OK (12 tests)` cho dialog cữ thuốc, hủy xóa, session thay đổi, nhật ký nước, rotation, hoàn tác, hai kiểu lịch nước.
- `.audit-cache/new-figma-empty-red.txt`: kiểm thử thất bại đúng vì thiếu lời mời ghi lần đầu. `.audit-cache/new-figma-empty-green.txt`: `OK (2 tests)` gồm trạng thái trống → form cân nặng → có dữ liệu và bài chụp ảnh mở rộng.
- `.audit-cache/new-figma-final-verification-build.txt`: build verification/app-test và70 unit tests thành công,0 failures/errors.
- Bộ ảnh mở rộng gồm41 PNG, có thêm splash, lịch sử thuốc, cữ thuốc, xóa thuốc/nước, sửa nước, cấu hình nước tùy chọn/lỗi, nhật ký và biểu đồ trống, lỗi đăng nhập. Ảnh có tên login-keyboard ở vòng đầu chưa hiển thị IME, nên không được tính là bằng chứng bàn phím. Fixture chỉ thuộc gói `.verification`; không đưa dữ liệu mẫu vào ứng dụng chính.

## Kiểm tra cuối đang thực hiện

- [ ] Bộ kiểm thử thiết bị đầy đủ trên APK cuối: lượt đầu chạy72 bài,71 qua và1 bài còn đọc nhãn ẩn cũ; đã sửa bài kiểm tra để đọc nhãn so sánh thực sự hiển thị, giữ đủ kiểm tra95/+5/khác bữa/khác hồ sơ. Lượt sau nhóm so sánh qua nhưng bài chụp ảnh mất focus và tiến trình dừng ở ProfileUpgrade do giao dịch cửa sổ Android. Không coi lượt này là toàn bộ thành công.
- [ ] Ảnh màn hình nhỏ và font hệ thống lớn: thử360dp/font130% hai lượt nhưng chưa xác minh được. Lượt sau `takeScreenshot()` trả null; dumpsys window cho thấy `Application Not Responding: system`. Không dùng41 file kéo về làm ảnh kiểm chứng nhỏ vì là ảnh cũ còn lưu. Đã hoàn trả và xác minh physical1080×2400, density420, font1.0; không tắt máy giả lập của người dùng.
- [x] Build APK applicationId gốc và lint, xác minh mã gói `vn.edu.tdmu.vita`. Log: `.audit-cache/new-figma-original-build-lint.txt`, `BUILD SUCCESSFUL`; lint: 0 errors, 198 warnings.

## Khác biệt cần công khai

- Chưa có phép so pixel tự động cho từng frame và mọi tổ hợp dữ liệu/locale. Ngày, tên, giá trị, số bản ghi và cữ thuốc thay theo dữ liệu thật.
- DatePicker/TimePicker và cửa sổ quyền/cài đặt giữ thành phần Android với đầy đủ giá trị hợp lệ; hình thức có thể khác dialog minh họa trong prototype.
- MPAndroidChart vẽ trục/nhãn/marker từ dữ liệu thật; nét chữ và vị trí nhãn có thể khác vector tĩnh của Figma, đặc biệt ở một điểm hoặc dữ liệu thưa.
- Ô nhập vẫn dùng TextInputLayout để giữ lỗi nhập liệu, khả năng tiếp cận và bàn phím. Viền/floating label khi lỗi cần tiếp tục so sát ở từng phiên bản Android.
- W01 không thêm liên kết quay lại chính nhật ký đang mở chỉ để mô phỏng liên kết lặp của prototype. Các đường mở nhật ký từ Home và Nhắc nhở vẫn giữ.
- Chưa xác minh mọi frame biến thể, landscape và Android24 trên thiết bị thực. Không kết luận “giống100%”.

### Xác minh bổ sung cuối

- `.audit-cache/new-figma-final-build2.txt`: app verification, test APK và70 unit tests thành công;85 XML parse hợp lệ, `git diff --check` không có lỗi khoảng trắng.
- Rà soát riêng vỏ dialog/caller/session/card/empty route không tìm thấy lỗi correctness hoặc security; đã xóa field đơn vị huyết áp không dùng.
- `.audit-cache/new-figma-small-font-device.txt`: Android báo `INSTRUMENTATION_ABORTED: System has crashed` / `DeadSystemException`; `/sdcard` từng mất kết nối. Đây là lượt kiểm thử không hợp lệ, không phải bằng chứng UI đạt.

- APK cuối: `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`; SHA256 `CAAC45EAA6FA47F185A6B6258006CC6DAFC957FE3FFDC9F8E166A731A2B5EFAC`.
- Phần còn bị chặn: lượt device cuối đầy đủ, bàn phím thật và chữ lớn cần máy giả lập ổn định/thiết bị thật. Không tuyên bố100% hoặc72 bài đều qua trên APK cuối.
