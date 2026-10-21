# ViTa — bước tiếp theo sau audit UI/UX

## Thay đổi

- Chi tiết số đo: badge huyết áp và lời giải thích nằm trong cùng card với số đo. Badge tương tác có minHeight48dp. Giữ hành động mở/đóng lời giải thích, phân loại và quy tắc ẩn thông số thiếu.
- Đơn vị của từng giá trị ở chi tiết và số gần nhất trong xu hướng nằm ngay dưới số, cỡ14sp. Loại cột đơn vị chiếm không gian của huyết áp khi font lớn. Không gộp hoặc thay đổi giá trị.
- Nhãn trục biểu đồ tăng từ10 lên12 và theo fontScale; chiều cao chart tăng tương ứng. Legend huyết áp có nhãn tâm thu/tâm trương với đường liền/đứt. Legend đường huyết cũng dùng cỡ chữ/word wrap chung. Không đổi lọc, dữ liệu, marker, query hoặc cách tính xu hướng.
- Hồ sơ: có heading Quản lý hồ sơ; đăng xuất giảm về text action, xóa vẫn tách biệt bằng hành động nguy hiểm và xác nhận mật khẩu cũ.
- Bổ sung tên accessibility cho chart BP/nước, giữ đường xem dữ liệu dạng chữ hiện có. Đây không phải xác nhận TalkBack đã đọc mọi điểm biểu đồ.

Giữ MVC, Java/XML, DB, xác thực, thông báo và quyền. Không thêm dependency hoặc dữ liệu mẫu vào sản phẩm; fixture nằm trong gói verification.

## Bằng chứng

- Đã xem ảnh BMI hợp lệ từ lượt chạy sau; không còn dùng ảnh trắng trong audit đầu làm căn cứ đánh giá màn BMI.
- `.audit-cache/followup-build.txt`: build verification/test/unit thành công.
- `.audit-cache/followup-font200-device.txt`: bài ảnh tên dài/BMI/chi tiết/xu hướng qua ở font200%. Ảnh cho thấy tên không bị ellipsis nhưng huyết áp chi tiết xuống hai dòng vì cột đơn vị; đã tiếp tục sửa layout thay vì coi bài ảnh pass là toàn bộ UI đạt.
- `.audit-cache/followup-regression-device.txt`: `OK (11 tests)` gồm StatsData, HealthComparison, ProfileBmiNavigation, ProfileDeletionScreen. Chạy trên bản trước tinh chỉnh cột đơn vị cuối; không thay đổi logic trong tinh chỉnh đó.
- Lượt ảnh cuối thêm cuộn tới khoảng cân nặng và chart để có bằng chứng cho các vùng nằm ngoài viewport. Có assertion huyết áp118/78 trên một dòng cho fixture này; không suy ra mọi giá trị/kích thước đều được đảm bảo một dòng.

## Giới hạn

- Không chấm lại điểm66/100 hoặc xác nhận UI đạt chuẩn toàn bộ. TalkBack thực, Switch Access và IME vẫn cần một lượt riêng.
- Không tự thiết kế lại dashboard, thay palette hoặc thay nội dung sức khỏe dựa trên cảm tính.
- Font lớn làm trang dài hơn; nội dung vẫn được cuộn. Không nhầm phần nằm ngoài viewport với phần bị mất.
- Kết quả build chính/lint và ảnh cuối được ghi thêm sau khi xác minh.

## Kết quả cuối

- `.audit-cache/followup-verified-device2.txt`: OK(1test) ở font200%, sau khi đổi bước đợi khung hình sang Choreographer. Lượt trước lỗi registry Activity được giữ trong log, không tính là pass.
- Đã xem ảnh chi tiết cuối `.audit-cache/followup-verified2/figma-followup-detail.png`:118/78 trên một dòng, đơn vị ở dưới, badge cùng card. Tên dài và hành động tài khoản có ảnh ở lượt font200 trước.
- Ảnh có tên stats-chart vẫn chưa thể hiện đầy đủ chart, còn ảnh BMI range chỉ cho thấy một phần vùng cuối do cuộn chưa ổn định; không coi tên file/bài test pass là xác minh pixel hai vùng đó. Không khẳng định toàn bộ chart/BMI ở200% đã được kiểm chứng trực quan.
- Khoảng cân nặng tham khảo BMI giảm về24sp/line33,6sp, giữ giá trị, đơn vị và công thức. Chỉ số BMI chính giữ cỡ lớn.
- `.audit-cache/followup-final-build.txt`: BUILD SUCCESSFUL57s;70unit tests/0failures/errors; lint0errors/198warnings. APK đúng package vn.edu.tdmu.vita tại `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`.
- Không commit/push hoặc cài fixture vào gói chính. Chưa kiểm TalkBack thực, Switch Access và IME trong lượt này.

## Kiểm chứng bổ sung: cuộn và bàn phím thực

- Chỉ sửa bài kiểm thử, không đổi thêm code ứng dụng trong lượt này. Thay thao tác cuộn không được kiểm chứng bằng Espresso scrollTo/vuốt và yêu cầu vùng mục tiêu hiển thị ít nhất90%.
- `.audit-cache/visibility-build.txt`: build test APK thành công. `.audit-cache/visibility-device.txt`: OK(2tests) ở font200%.
- Đã xem trực tiếp ảnh trong `.audit-cache/visibility-font200/`: `figma-followup-stats-chart.png` (trục, điểm đo, chú giải đường liền/đứt và nút xem dữ liệu), `figma-followup-bmi-range.png` (50.4–62.3kg một dòng), `figma-verified-login-keyboard.png` (IME thực và nút đăng nhập nhìn thấy), `figma-verified-login-validation.png` (lỗi thiếu mật khẩu, giữ username).
- Kiểm tra bàn phím dùng WindowInsetsCompat.Type.ime(), không suy ra từ con trỏ ô nhập. Sau khi IME hiện, scrollTo nút đăng nhập phải đạt ít nhất90% hiển thị.
- Các giới hạn trước về chưa có ảnh chart/range200% và IME được giải quyết cho các fixture/cấu hình này. Không suy rộng thành mọi kích thước/keyboard/OEM đều đạt.
- APK sản phẩm giữ nguyên bản build thành công cuối của lượt trước; không cần build lại APK chính vì không có thay đổi production. Chưa chạy TalkBack/Switch Access thực và chưa chấm lại điểm tổng thể.


## Bổ sung: trợ năng cho ngày lặp lịch thuốc

- Xác nhận lỗi bằng kiểm thử trước sửa: các ngày dùng TextView, không công bố vai trò checkable.
- Chuyển sang CheckBox native, giữ nền/màu hiện có và bitmask lịch cũ. Nhãn trợ năng dùng tên ngày đầy đủ; trạng thái checked do Android cung cấp.
- Bố trí 4 + 3 ngày thành hai hàng, chiều cao tự tăng và vùng chạm tối thiểu 48dp.
- Kiểm thử thiết bị tại 360dp, font 200%: cả bảy vùng chạm đạt ít nhất 48dp, trạng thái chọn/bỏ chọn đúng, lịch thứ Hai + thứ Sáu lưu daysMask = 17. Kết quả: OK (1 test).
- Đã xem ảnh `.audit-cache/a11y-medicine-days.png`: các ngày không bị che hoặc cắt chữ ở cấu hình trên. Tiêu đề toolbar vẫn rút gọn bằng dấu ba chấm ở font 200%; chưa kết luận toàn màn hoàn hảo ở mức chữ này.
- Rà soát đường gọi: ID view được giữ nguyên; không đổi DAO, database, thuật toán lịch hoặc quyền thông báo. Không thêm dependency.
- Chưa kiểm tra lời đọc/điều khiển bằng TalkBack hoặc Switch Access; kiểm tra AccessibilityNodeInfo chỉ xác minh ngữ nghĩa mà view cung cấp.

Kết quả build sau bổ sung: BUILD SUCCESSFUL; 70 unit test, 0 lỗi; lint 0 errors / 200 warnings. APK chính xác nhận package vn.edu.tdmu.vita. Đã khôi phục emulator 1080x2400, density 420, font 1.0. Không có commit hoặc push trong bước này.


## Bổ sung: tiêu đề và thao tác giờ uống thuốc

- Tiêu đề form thuốc dùng TextView theo typography toolbar hiện có, cho phép xuống dòng và chiều cao toolbar tự tăng (tối thiểu 100dp). Giữ nguyên tiêu đề riêng khi thêm/sửa; không giảm cỡ chữ để che lỗi.
- Ô mở bộ chọn giờ nhận focus, bỏ KeyListener nhập văn bản để phím Enter kích hoạt hành động. Lỗi đã được tái hiện: trước sửa Enter nhập xuống dòng thay vì mở dialog.
- Dấu xóa giờ tăng từ 10dp lên 24dp, thêm 12dp khoảng chạm mỗi bên; vẫn dùng close action của Chip native và nhãn “Bỏ cữ …”.
- `.audit-cache/medicine-actions-red-device.txt`: kiểm thử thất bại đúng lỗi thiếu focus. Lượt đầu sau bật focus cũng bắt được lỗi Enter; bản sửa cuối `.audit-cache/medicine-actions-green-device.txt`: OK (1 test) tại 360dp / font 200%.
- Test xác minh Enter mở TimePicker, vùng xóa có tổng chiều rộng ít nhất 48dp, bảy ngày có vùng chạm 48dp, trạng thái checked và lịch thứ Hai + thứ Sáu lưu đúng. Không thay đổi DAO, database hoặc tính lịch/nhắc thuốc.
- Đã xem ảnh `.audit-cache/medicine-actions-font200.png`: tiêu đề “Lịch uống thuốc mới” đủ hai dòng, dấu xóa rõ hơn, các ngày không bị cắt. Phần ô liều dùng ở mép trên viewport đã cuộn không được coi là mất nội dung.
- Rà soát theo ponytail-review: giữ ID của toolbar, ô giờ và ChipGroup; các caller của setTitle chỉ có trong AddMedicineActivity và đã chuyển sang title view. Không thêm dependency hoặc abstraction.
- Chưa kiểm chứng giọng đọc TalkBack, Switch Access hoặc mọi bàn phím/OEM. Đây là kiểm thử phím Enter và ngữ nghĩa view, không thay thế kiểm tra trợ năng toàn diện.

Build cuối của lượt thao tác giờ: BUILD SUCCESSFUL (1 phút), 70 unit test / 0 failures / 0 errors; lint 0 errors / 200 warnings. APK chính xác nhận package vn.edu.tdmu.vita. Emulator khôi phục 1080x2400, density 420, font 1.0. Chưa commit/push.


## Bổ sung: bộ chọn ngày/giờ, vùng thao tác thuốc và bố cục chữ lớn

### Lỗi được tái hiện

- `.audit-cache/health-picker-red-device.txt`: 3 bài kiểm thử, 2 thất bại đúng lỗi ô ngày/giờ không nhận focus và trạng thái liều bị giới hạn chiều cao 32dp.
- Ảnh chạy thật 360dp / font 200% (`a11y-health-pickers.png`, `a11y-dose-status.png`, `a11y-medicine-switch.png` trong `.audit-cache/`) cho thấy nhãn nhóm chỉ số bị cắt; header Nhắc nhở cao cố định làm subtitle chồng lên tab; tên thuốc bị ép vào cột hẹp. Vùng bật nhắc và trạng thái đã được mở rộng nhưng chưa đủ để kết luận toàn màn đạt.

### Thay đổi

- Ô ngày và giờ đo nhận focus; bỏ bộ nhập văn bản để Enter mở DatePicker/TimePicker. Giữ validation, ngày/giờ và cách lưu số đo.
- Nút bật nhắc dùng wrap_content, cao tối thiểu 48dp, chữ 14sp. Trạng thái liều tự tăng chiều cao, chữ 14sp; bỏ focus riêng của nhãn thụ động, hành động mở chỉnh sửa vẫn nằm ở cả card.
- Chip nhóm chỉ số có chiều rộng đủ cho chữ đã scale và tự xuống hàng; giữ lựa chọn đơn và logic hiển thị nhóm.
- Khi fontScale > 1.3, ngày/giờ và hai ô huyết áp xếp dọc; giờ uống và tên thuốc cũng xếp dọc. Ở font thông thường giữ cách tổ chức hai cột.
- Header Nhắc nhở dùng wrap_content/minHeight 100dp. Tab cho phép nhãn nhiều dòng sau khi ToggleGroup khởi tạo (nhóm này ép một dòng khi thêm child).
- Kiểm thử cửa sổ picker dùng isDialog và nút Hủy của dialog. Hai lượt trước gặp lỗi chọn root/Back được giữ trong log, không tính là pass hay kết luận lỗi nghiệp vụ.
- Không thay MVC, DAO, database, tính lịch, quyền, nội dung sức khỏe hoặc thêm dependency. Chỉ dùng dữ liệu fixture trong package `.verification`, xóa sau test.


### Kết quả xác minh cuối lượt

- `.audit-cache/large-text-green-device.txt`: **OK (3 tests)** trên 360dp / font 200%. Có assertion độ rộng chip theo chữ, chiều cao hai tab bằng nhau và không có ellipsis, header chứa đủ subtitle, vùng thao tác 48dp, Enter mở picker, hủy giữ dữ liệu, lưu nhịp tim72 và thời điểm gốc, tắt nhắc được ghi xuống DAO.
- Đã xem ảnh `large-a11y-health-groups.png`, `large-a11y-health-pickers.png`, `large-a11y-dose-status.png`, `large-a11y-medicine-switch.png` trong `.audit-cache/`. Chip đủ nhãn; ngày một dòng và giờ không bị ép; hai tab đều chiều cao; tên Vitamin C không còn tách thành các mảnh chữ. Nội dung dài vẫn cần cuộn ở font200%; không coi phần ngoài viewport là đã mất. Ảnh trạng thái yêu cầu cuộn và hiển thị ít nhất90% trước chụp.
- Lượt native chung `.audit-cache/large-text-native-device.txt` có2/3 bài qua; bài health gặp RootViewWithoutFocusException sau chuyển dialog. Chạy riêng cùng bài trên cấu hình gốc: `.audit-cache/large-text-native-health-isolated.txt`: **OK (1 test)**. Chưa giải quyết triệt để độ ổn định focus của bộ test chung; không báo lượt native chung là pass.
- Đã xem ảnh native `native-a11y-health-groups.png`, `native-a11y-dose-status.png`, `native-a11y-medicine-switch.png`: bố cục thông thường giữ3+2 nhóm chỉ số, ngày/giờ và huyết áp hai cột, giờ/tên thuốc cùng hàng.
- `.audit-cache/large-text-final-build.txt`: **BUILD SUCCESSFUL (1 phút)**, 70 unit tests /0 failures /0 errors; lint0 errors /198 warnings. APK chính đúng package `vn.edu.tdmu.vita` tại `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`.
- Rà soát theo ponytail-review: các parent được đổi orientation vẫn là LinearLayout trong XML, giữ ID/caller và listener cũ; không sửa phân loại liều hoặc nghiệp vụ lưu dữ liệu. Không có phát hiện chặn bàn giao trong phạm vi diff này; rủi ro kiểm thử focus nêu riêng ở trên.
- Emulator đã khôi phục1080x2400, density420, font1.0. Không commit/push. Chưa kiểm TalkBack/Switch Access thực, các OEM/bàn phím khác; chưa chấm lại điểm66/100 hoặc tuyên bố UI đạt chuẩn toàn bộ.


## Bổ sung: chỉnh sửa hồ sơ và cài đặt nước ở chữ 200%

- Đã xem ảnh baseline trong `.audit-cache/before-*.png`: tiêu đề Chỉnh sửa hồ sơ bị ellipsis, nhãn năm sinh gộp hướng dẫn bị cắt, hai nhãn giờ nhắc bị rút gọn. Nút Lưu vẫn cuộn tới và hiển thị phía trên IME thực.
- Tiêu đề EditProfileActivity dùng TextView theo typography toolbar hiện có, tự xuống dòng; toolbar wrap_content/min64dp. Cả chế độ hồ sơ và water-only vẫn có đúng tiêu đề; giữ nút Back và cảnh báo chưa lưu.
- Tái sử dụng `edit_profile_hint_birth_year` và `register_helper_birth_year` làm nhãn ngắn + helper. Giữ nguyên resource gộp đang dùng ở màn Đăng ký; không đổi ràng buộc năm sinh.
- Với fontScale >1.3, hai ô giờ nhắc xếp dọc để giữ nhãn đầy đủ. Ở chữ thường vẫn hai cột.
- Chip mốc nước dùng icon xóa24dp và khoảng chạm12dp mỗi bên, thống nhất với chip lịch thuốc. Giữ listener/chức năng thêm, sửa, xóa và hai chế độ nhắc.
- `.audit-cache/water-ui-green-device.txt`: **OK (5 tests)** tại360dp /font200%, gồm lượt ảnh tương tác và4 hồi quy cài đặt nước. Test xóa18:00, đổi sang chu kỳ2giờ rồi về tùy chọn, sửa lượng thành300ml và lưu; DAO giữ08:00,12:00 và họ tên cũ. IME được xác nhận bằng WindowInsetsCompat.Type.ime(), nút Lưu hiển thị ít nhất90%.
- Đã xem ảnh after-edit-profile-large, after-water-settings-top-large và after-water-settings-keyboard-large: tiêu đề/nhãn giờ đọc đủ, hướng dẫn năm sinh tách riêng, Lưu phía trên bàn phím. Ô họ tên giữ hành vi nhập một dòng/cuộn ngang, không tuyên bố toàn bộ tên dài cùng hiện trên một dòng.
- Ảnh after-water-settings-custom-large quay về phần đầu form nên không dùng làm bằng chứng phần chip. Lượt chụp bổ sung đặt focus vào nút thêm mốc và yêu cầu hiển thị trước khi chụp; kết quả ghi tiếp bên dưới.
- `.audit-cache/water-ui-final-build.txt`: **BUILD SUCCESSFUL (1m2s)**;70 unit tests/0 failures/0 errors; lint0 errors/197 warnings. APK đúng package `vn.edu.tdmu.vita` tại `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`.
- Rà soát ponytail-review: không thay database, DAO, nghiệp vụ nhắc, validation hoặc dependency; title caller water-only đã chuyển sang view mới, ID các control chức năng giữ nguyên. Các string đã có được tái sử dụng để tránh sửa nhãn đăng ký ngoài phạm vi.
- Chưa kiểm TalkBack/Switch Access thực hoặc mọi OEM/bàn phím; chưa chấm lại điểm audit. Không commit/push.

Lượt chụp bổ sung water-ui-capture-device.txt: OK (1 test). Đã xem verified-water-settings-custom-large.png (đủ ba mốc và dấu xóa lớn, nút thêm mốc) và verified-water-settings-preview-large.png (đủ lịch 07:00–21:00, không cắt). Phần phía trên viewport là nội dung đã cuộn, không phải mất dữ liệu. Chỉ sửa test ở lượt chụp bổ sung; APK chính giữ bản build thành công đã nêu. Emulator khôi phục 1080x2400/density420/font1.0.


## Bổ sung: Đăng ký và Đăng nhập trên màn nhỏ/chữ lớn

- Baseline `.audit-cache/auth-large-baseline-device.txt`: đăng ký giữ dữ liệu giữa hai bước và validation hoạt động; kiểm thử đăng nhập thiếu bước cuộn tới username ở viewport360dp, không phải kết luận chức năng đăng nhập hỏng.
- Đã xem `before-register-account-large.png`, `before-register-profile-large.png`, `before-register-validation-large.png`: toolbar hai bước bị ellipsis; badge Bước1/2 xuống hai dòng do rộng cố định100dp; nhãn năm sinh bị cắt; hai ô cao/nặng quá hẹp làm giá trị, đơn vị và icon lỗi chen nhau ở font200%.
- Toolbar Đăng ký dùng title view theo text appearance hiện có, tự xuống dòng và chiều cao tự tăng/min64dp. showStep cập nhật title view cho đúng bước, giữ nguyên Back, validation và dữ liệu draft.
- Badge bước dùng wrap_content/min100dp. Năm sinh dùng nhãn ngắn + helper có sẵn, không thêm resource/dependency. Hai ô cao/nặng chuyển dọc khi fontScale >1.3, giữ hai cột ở chữ thường.
- Ảnh kiểm tra vòng đầu sau sửa xác nhận2000kg hiển thị đủ, lỗi đọc được, CTA phía trên IME thật. Ảnh Đăng nhập khi đó cho thấy toolbar còn rút gọn; đã sửa title view wrap_content/min64dp tương tự. Không đổi logic xác thực.
- Bài kiểm tra đăng ký nhập giá trị không hợp lệ, xác nhận lỗi rồi Back/Next để kiểm tra username và hai số đo được giữ; không thực hiện đăng ký thành công hoặc tạo hồ sơ mới. Bài đăng nhập kiểm tra bàn phím thực và lỗi thiếu mật khẩu, giữ username.
- Gộp kiểm tra IME lặp lại của các ca ảnh vào một helper test dùng WindowInsetsCompat.Type.ime(); không thêm helper vào ứng dụng.
- Chưa kiểm TalkBack/Switch Access thực, mọi bàn phím/OEM hoặc toàn bộ luồng đăng ký thành công ở lượt này. Giữ nguyên thang điểm audit hiện có.


### Xác minh cuối luồng xác thực

- `.audit-cache/auth-large-green-device.txt`: **OK (2 tests)** tại360dp /font200%, sau khi đợi IME hiện và chiều cao viewport ổn định trước cuộn. Lượt trước có lỗi ScrollToAction trong lúc resize; không tính lượt đó là pass.
- Đã xem ảnh after-auth-register-profile-large, after-auth-register-validation-large, after-auth-register-keyboard-large, after-auth-verified-login-keyboard trong `.audit-cache/`: tiêu đề đúng và không ellipsis; badge bước một dòng; số2000 +kg +icon lỗi tách rõ; nút xác nhận Đăng ký/Đăng nhập hiển thị trên bàn phím. Đoạn nằm ngoài viewport sau cuộn không được coi là nội dung mất.
- Chữ lớn làm các trang dài và cần cuộn; không giảm font hệ thống hoặc cắt nội dung để giữ số phần tử trong một màn hình.
- Rà soát ponytail-review: title caller của RegisterActivity nằm trong showStep và đã cập nhật; LoginActivity chỉ dùng toolbar cho Back, giữ nguyên ID. Điều kiện layout chỉ tác động trình bày; không đổi password, username, giới hạn số đo, database hoặc xác thực. Không thêm dependency.
- Emulator đã trả về1080x2400/density420/font1.0. Ảnh chẩn đoán tạm trên sdcard đã xóa; ảnh bằng chứng nằm trong `.audit-cache/`. Không commit/push.

Build cuối xác thực: BUILD SUCCESSFUL (1m2s); 70 unit tests, 0 failures/0 errors; lint 0 errors/199 warnings. APK chính tại Vita_Project/app/build/outputs/apk/debug/app-debug.apk.


## Bổ sung: lịch sử thuốc, hộp thoại liều và nhật ký nước

- Baseline `.audit-cache/journals-large-baseline-device.txt`: OK(1test), chỉ xác nhận mở/đóng và dữ liệu giữ nguyên. Đã xem ảnh before-medicine-history-large, before-medicine-dose-dialog-large, before-water-journal-large: tên thuốc dài trên toolbar bị ellipsis; tổng2,250 bị tách thành2,25 và0; các nút nước bị xuống dòng giữa từ ở font200%.
- Dùng layout `view_page_toolbar.xml` chung cho MedicineDoseDialogs.history và WaterJournalDialog.page; tái sử dụng typography/navigation hiện có, tiêu đề xuống dòng, chiều cao wrap/min64dp. Scroll nội dung lịch sử thuốc dùng phần chiều cao còn lại.
- Khi fontScale >1.3, tổng nước/mục tiêu và hai nút ghi nước xếp dọc; font thường giữ tỷ lệ/cột cũ. Không đổi số liệu, công thức phần trăm, goal, số ml, thời điểm, lưu/hoàn tác hoặc quyền sở hữu dữ liệu.
- `.audit-cache/journals-large-green-device.txt`: **OK(1test)** tại360dp/font200%; assert title không ellipsis, tổng2,250 và nhãn Lượng khác trên một dòng; Back/Cancel giữ liều07:00 chưa ghi, liều20:00 TAKEN và tổng2250ml.
- Đã xem after-medicine-history-large.png (tên thuốc mẫu đầy đủ4dòng), after-water-journal-large.png (2,250 một dòng), after-water-journal-actions-large.png (hai nút ghi nước dễ đọc) trong `.audit-cache/`. Cần cuộn để xem các phần cuối ở cỡ chữ lớn; không coi nội dung ngoài viewport là bị mất.
- Hộp thoại liều với tên dài đã có ScrollView dùng chung; thao tác Hủy được test thực, không sửa shell hoặc nghiệp vụ khi chưa có lỗi cần sửa.
- `.audit-cache/journals-large-final-build.txt`: **BUILD SUCCESSFUL(1m6s)**;70 unit tests,0failures/0errors; lint0errors/202warnings. APK chính đúng package `vn.edu.tdmu.vita` tại `Vita_Project/app/build/outputs/apk/debug/app-debug.apk`.
- Rà soát ponytail-review: giữ callback Back/dismiss và body tag; title mới chỉ được tìm trong toolbar của từng trang; không thêm dependency. Header dùng chung thay hai khối dựng toolbar lặp lại. DAO/save/delete/session guards giữ nguyên.
- Chưa kiểm TalkBack/Switch Access thực, các tên thuốc dài hơn mẫu, mọi kích thước/font/OEM. Không chấm lại điểm audit hoặc tuyên bố toàn bộ UI đạt chuẩn. Không commit/push.


### Hồi quy các trang dùng chung thanh tiêu đề

- Lượt `.audit-cache/journals-native-regression.txt` đã hoàn tất ba ca hoàn tác, giữ draft sau recreate và xác nhận xóa; ca sửa một lần cũ kẹt khi tìm input trong hai dialog lồng nhau. Đã dừng riêng package verification, không coi lượt này là pass toàn bộ.
- Ảnh `.audit-cache/journal-diagnostic.png` xác nhận form sửa350ml lúc08:00 hiển thị đủ input, Lưu và Xóa. Chưa có bằng chứng app làm sai dữ liệu ở bước đang chờ.
- Bài WaterJournalDeviceTest.journalLetsUserCorrectAnEntryAfterUndoHasGone được đổi từ root isDialog chung sang decorView của DialogFragment tag water_entry cho thao tác nhập/lưu. Đây là sửa lựa chọn cửa sổ của test, không sửa chức năng ứng dụng. Build test lại thành công; lượt chạy lại giới hạn60s mỗi ca.

Hồi quy cuối journals-native-regression-final.txt: OK (4 tests), 55,132 giây; TestRunner báo 0 failed. Đã xác minh ghi nhanh/hoàn tác, draft sau recreate/lưu, xác nhận xóa chỉ bản ghi đã chọn, sửa350→500ml giữ dữ liệu hồ sơ khác. Chỉ sửa test root sau build APK chính, không đổi thêm production. Tổng lượt này:1 kiểm thử ảnh chữ200% +4 hồi quy native;70 unit test. Lượt bị dừng trước không được tính.
