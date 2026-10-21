# Sửa form lệch/che chữ theo ảnh điện thoại

## Nguyên nhân và thay đổi

- Hai ô huyết áp hẹp chứa giá trị, suffix mmHg và icon báo lỗi cùng lúc. Chuyển đơn vị lên tiêu đề nhóm `Huyết áp · mmHg`, bỏ suffix ở hai ô; lỗi vẫn có viền đỏ và thông báo chữ, bỏ icon lỗi riêng ở style Health.Input để không chiếm chỗ. Các nhãn nhịp tim/cân nặng/đường huyết ghi rõ đơn vị.
- Padding dọc còn theo kiểu filled cũ sau khi chuyển outline. Đồng bộ EditText và các override XML về16dp trên/dưới, giữ minHeight và wrap_content để thông báo phía dưới được tăng chiều cao.
- Hai lựa chọn nhắc nước chia ngang có thể xuống dòng không cùng chiều cao. Xếp thành hai hàng rộng, padding12dp và minHeight48dp; giữ RadioGroup và checked state cũ.
- Lỗi cỡ cốc không tự xóa khi sửa từ giá trị sai sang đúng. Bổ sung watcher xóa lỗi cho lượng nước và giờ bắt đầu/kết thúc. Tách kiểm tra khung giờ và lượng nước, hiển thị thông báo tại đúng nhóm thay vì dùng thông báo chung ở cỡ cốc.

Không đổi giới hạn số đo, lượng nước, quy tắc lịch, DB, quyền hay dữ liệu người dùng.

## Kiểm chứng

- `.audit-cache/clipping-red-device.txt`: bài mới thất bại đúng vì lỗi cũ còn tồn tại sau khi sửa lượng thành250ml.
- `.audit-cache/clipping-green-build.txt`: build verification/test và unit thành công.
- `.audit-cache/clipping-green-device.txt`: `OK (5 tests)` ở360dp/font130%, gồm WaterSettingsScreenDeviceTest và FormClippingDeviceTest.
- Đã xem ảnh `clipping-health-errors.png`, `clipping-water-settings.png`, `clipping-water-window-error.png` trong `.audit-cache/clipping-360-font130/`: đơn vị BP không bị cắt, thông báo nằm dưới ô, hai lựa chọn không lệch nhau; lỗi giờ trùng xuất hiện tại giờ kết thúc và mất khi sửa.
- Ảnh `clipping-water-add.png` ở lượt đổi kích thước xuất hiện ghép hình Home bên dưới nên không dùng làm bằng chứng đạt. Chạy lại cùng bài ở cấu hình máy gốc: `.audit-cache/clipping-native-device.txt`, `OK (1 test)`; ảnh `.audit-cache/clipping-water-native.png` đã xem, helper tách khỏi viền và nút lưu hiển thị đầy đủ.
- Đã hoàn trả và xác minh1080×2400, density420, font1.0; giá trị gốc lưu tại `.audit-cache/clipping-device-original.json`.
- Build chính/unit/lint cuối lưu ở `.audit-cache/clipping-final-build.txt`: BUILD SUCCESSFUL,70 unit tests/0failures/errors, lint0errors/200warnings. APK chính xác minh applicationId `vn.edu.tdmu.vita`, tại `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`.

## Giới hạn

Chưa chạy trực tiếp trên điện thoại OEM trong ảnh người dùng hoặc font200%. Ảnh được xem là bằng chứng cho cấu hình đã nêu, không suy rộng thành mọi thiết bị/font đều đã xác minh. Không dùng ảnh bị ghép hình làm kết quả đạt.
