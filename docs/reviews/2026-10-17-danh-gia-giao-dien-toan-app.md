# Đánh giá giao diện toàn app ViTa

Ngày: 17/10/2026. Phạm vi: Android hiện tại; đánh giá để lên kế hoạch nâng cấp, không sửa mã ứng dụng.

## 1. Kết luận

ViTa có nền tảng giao diện phù hợp một ứng dụng sức khỏe cá nhân: tiếng Việt, màu xanh ngọc, phân nhóm chức năng, đơn vị đo và các luồng nhập liệu tương đối rõ. Khoảng cách với ứng dụng thương mại nằm chủ yếu ở thứ tự ưu tiên thông tin, độ gọn của thao tác lặp lại và sự nhất quán giữa các màn hình. Chỉ đổi màu, bo góc hoặc thêm animation sẽ không giải quyết được khoảng cách này.

Hướng đề xuất: giao diện sáng, điềm tĩnh, ưu tiên việc cần làm hôm nay; số liệu dễ đọc; mỗi khu vực có một hành động chính; giải thích chuyên sâu có thể mở khi cần. Giữ Android XML/Material và cấu trúc hiện có. Chưa có lý do từ cuộc đánh giá này để đổi framework hoặc thêm thư viện giao diện.

## 2. Bằng chứng và giới hạn

- Đã đối chiếu 10 Activity, 4 Fragment, chế độ cấu hình nước riêng, các layout danh sách, hộp thoại, biểu đồ và luồng thông báo. Thêm/sửa dùng chung màn hình được đánh giá cả hai ngữ cảnh.
- Đã xem ảnh QA trong `.audit-cache`, bao gồm ảnh hồ sơ ở màn hình nhỏ/chữ lớn và cấu hình nước mới hơn. Ảnh QA cũ chỉ dùng để nhận diện vấn đề cần đối chiếu lại mã.
- Đã chụp khởi động trên emulator: splash hệ thống còn hình Android mặc định, sau đó splash riêng của ViTa. Cấu hình launcher hiện tại vẫn tham chiếu vector Android mẫu.
- Việc lấy cây giao diện emulator lỗi `null root node`; chưa thao tác trực tiếp đầy đủ mọi luồng của bản đang chạy. Không coi toàn bộ ảnh cũ là ảnh của bản hiện tại.
- So sánh đối thủ dựa trên tài liệu và hình giới thiệu chính thức công khai; chưa thực hiện nghiên cứu người dùng hoặc đo thời gian thao tác đối đầu trên cùng thiết bị.
- Đây là đánh giá UI/UX, không phải chứng nhận khả năng tiếp cận, tính chính xác y khoa hay kiểm toán bảo mật.

Mức ưu tiên: **P1** ảnh hưởng thao tác thường ngày, đọc dữ liệu hoặc khả năng tiếp cận; **P2** ảnh hưởng tính nhất quán, độ gọn, khả năng học; **P3** hoàn thiện sau. Không dùng các mức này để khẳng định lỗi gây sự cố nghiêm trọng.

Các thay đổi gần đây đã được tính đến: hồ sơ tách cấu hình nước, chiều cao/cân nặng ở màn sửa hồ sơ xếp dọc, đăng xuất khác màu xóa tài khoản, năm sinh có thể bỏ trống, thông tin phạm vi BMI rõ hơn, nước có hoàn tác và giờ tùy chọn. Không liệt kê lại những điểm đã sửa này như lỗi còn tồn tại.

## 3. So với ứng dụng ngoài thị trường

| Tham chiếu | Điểm giống ViTa | Khác biệt hữu ích để học | Cách áp dụng phù hợp |
|---|---|---|---|
| Samsung Health | Nhiều nhóm dữ liệu sức khỏe, trang tổng quan, lịch sử | Trang Home tổ chức bằng các tracker và hỗ trợ sắp xếp mục yêu thích | Cho việc hôm nay và dữ liệu quan trọng lên trước; chưa cần xây bộ tùy biến dashboard |
| OMRON connect | Chỉ số sức khỏe, theo dõi diễn biến, biểu đồ | Tập trung vào tổng quan chỉ số và lịch sử đo | Làm rõ lần đo gần nhất, thời điểm, đơn vị và xu hướng; không sao chép tính năng kết nối thiết bị ngoài phạm vi |
| Medisafe | Lịch thuốc, nhắc và xác nhận uống | Trọng tâm sản phẩm là quản lý lịch thuốc, kể cả lịch phức tạp | Ưu tiên cữ tiếp theo và hành động xác nhận; giảm số nút lặp lại trên danh sách |
| WaterMinder | Mục tiêu nước, ghi lượng uống, nhắc nước | Trình bày tiến độ và ghi nhanh lượng uống là trọng tâm | Đưa tổng đã uống/mục tiêu và nút cộng nhanh thành một cụm gọn, giữ hoàn tác |

Nguồn chính thức: [Samsung Health](https://www.samsung.com/us/support/answer/ANS10001351/), [OMRON connect](https://www.omron-healthcare.com/omronconnect), [Medisafe](https://medisafeapp.com/), [WaterMinder](https://waterminder.com/).

Các nhận xét “nên học” là suy luận thiết kế cho ViTa, không phải kết quả thử nghiệm chứng minh app nào dễ dùng hơn. Ảnh sản phẩm có thể khác giữa Android, iOS, khu vực và phiên bản. Không sao chép nguyên giao diện iOS vào Android.

ViTa có lợi thế về phạm vi gọn và tiếng Việt. Nên tận dụng việc thuốc, nước, chỉ số và giấc ngủ cùng nằm trong một app, thay vì làm từng khu vực trở thành một công cụ rời rạc. Đưa thêm mạng xã hội, bảng xếp hạng, AI hoặc thiết bị đeo chưa phải ưu tiên giao diện hiện nay.

## 4. Đánh giá từng màn hình

### 4.1 Khởi động — P2

**Giữ:** logo ViTa và nhận diện xanh ngọc.

**Vấn đề:** launcher/splash hệ thống còn biểu tượng Android mẫu trong khi splash sau đó dùng nhận diện ViTa. Đã nhìn thấy trực tiếp; tạo hai nhận diện liên tiếp. Splash riêng có nhiều khoảng trống và nội dung trang trí, chưa giúp người dùng bắt đầu công việc. Không có phép đo thời gian nên không kết luận khởi động chậm.

**Nâng cấp:** đồng nhất adaptive icon, monochrome icon và system splash bằng tài sản ViTa phù hợp; chỉ giữ màn chờ riêng khi có công việc tải thật. Không dùng bộ đếm chờ chỉ để phô logo. Kiểm tra cold/warm launch và quay lại sau khi app bị hệ thống hủy. Android hướng dẫn tránh splash trùng khi chuyển sang hệ thống splash mới: [tài liệu Android](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate).

**Nguồn mã:** `activity_splash.xml`, `SplashActivity.java`, `mipmap-anydpi-v26/ic_launcher.xml`, `drawable/ic_launcher_foreground.xml`.

### 4.2 Đăng nhập — P2

**Giữ:** hai trường chính, nhãn tiếng Việt, ẩn/hiện mật khẩu và liên kết đăng ký.

**Vấn đề:** logo ảnh vuông và phần giới thiệu chiếm diện tích; cần kiểm tra khi bàn phím mở. Thuộc tính bàn phím có Next/Done nhưng chưa thấy xử lý editor action tương ứng trong Activity. Người dùng cần hiểu tài khoản thuộc mô hình lưu trữ hiện tại; không tạo cảm giác có đồng bộ nếu app chưa có.

**Nâng cấp:** logo gọn, một câu giới thiệu có ích, đưa form lên vị trí thuận tiện khi mở bàn phím; Done thực hiện đăng nhập; lỗi nằm tại trường sai và giữ lại tên đăng nhập. Không thêm liên kết “quên mật khẩu” khi chưa có luồng khôi phục thật. Trạng thái chờ chỉ cần nếu xử lý thực tế đủ lâu để người dùng nhận thấy.

**Nguồn:** `activity_login.xml`, `LoginActivity.java`.

### 4.3 Đăng ký — P1

**Giữ:** có kiểm tra dữ liệu và đơn vị chiều cao/cân nặng; năm sinh hiện cho phép bỏ trống.

**Vấn đề:** nhiều trường nối tiếp khiến tài khoản và hồ sơ sức khỏe có trọng lượng ngang nhau. Chiều cao/cân nặng vẫn chia hai cột trong layout đăng ký, dễ chật trên màn nhỏ/chữ lớn. Không suy diễn lỗi nhập liệu từ ảnh QA cũ thành lỗi kiểm tra dữ liệu hiện tại.

**Nâng cấp:** chia rõ “Tài khoản” và “Thông tin cá nhân”; đánh dấu bắt buộc/tùy chọn ngay nhãn; xếp các trường dễ dài thành một cột. Ưu tiên mật khẩu/nhập lại và hướng dẫn ngắn, hiển thị lỗi tại chỗ, đưa focus đến trường sai đầu tiên. Chưa cần biến thành nhiều bước nếu bố cục một trang sau khi rút gọn đã đủ dễ dùng.

**Nguồn:** `activity_register.xml`, `RegisterActivity.java`.

### 4.4 Khung chính và điều hướng — P1/P2

**Giữ:** bốn tab là số lượng hợp lý; hồ sơ qua biểu tượng trên toolbar giúp thanh đáy không quá tải.

**Vấn đề:** thanh điều hướng bị cố định 56dp. Đây là điểm cần kiểm tra với nhãn, chữ lớn và thanh điều hướng hệ thống, chưa phải bằng chứng rằng mọi thiết bị đều cắt chữ. XML hiện đã bật nhãn; ảnh cũ chỉ có icon không chứng minh bản hiện tại thiếu nhãn. Mỗi lần chọn tab, MainActivity tạo Fragment mới, làm trải nghiệm giữ vị trí và bộ lọc thiếu liên tục.

**Nâng cấp:** cho component đủ chiều cao theo nội dung/insets; giữ trạng thái tab, phạm vi ngày và vị trí cuộn phù hợp; nút hồ sơ có nhãn tiếp cận. Quy định rõ nút Back khi đang nhập, khi xem chi tiết và khi ở tab gốc. Giữ bốn tab trước khi cân nhắc bổ sung tab mới.

**Nguồn:** `activity_main.xml`, `MainActivity.java`, `values/dimens.xml`.

### 4.5 Trang chủ — P1, ưu tiên cao nhất

**Giữ:** lời chào, tính cá nhân hóa, thông tin BMI/thời tiết đã có.

**Vấn đề:** BMI và thời tiết chiếm vị trí mạnh trong khi cữ thuốc sắp tới, tiến độ nước và lần đo mới nhất ở nơi khác. Người mở app vẫn phải tìm việc cần làm. Nội dung giải thích BMI mới giúp chính xác hơn nhưng làm card tổng quan dày chữ nếu luôn mở toàn bộ.

**Nâng cấp đề xuất theo thứ tự:**

1. Tiêu đề “Hôm nay” và ngày, hồ sơ ở góc.
2. Cữ thuốc sắp tới: tên, liều, giờ, một hành động rõ. Không có thuốc thì thay bằng lời mời thiết lập, không để ô trống vô nghĩa.
3. Nước: đã uống/mục tiêu, tiến độ và cộng nhanh, hoàn tác sau ghi.
4. Lần đo gần nhất: số chính, thời gian, đơn vị và “Ghi chỉ số”. Chưa có dữ liệu thì hướng dẫn ghi lần đầu.
5. Giấc ngủ gần nhất và lối vào ghi giấc ngủ nếu phù hợp chiều cao trang.
6. BMI và thời tiết dạng tóm tắt, mở thêm để đọc chi tiết.

Đây chủ yếu là tái sử dụng dữ liệu hiện có. Chưa cần thêm điểm sức khỏe tổng hợp hoặc thuật toán khuyến nghị mới. Không che điều kiện áp dụng BMI; đưa nhãn điều kiện ngắn cạnh kết quả và giải thích đầy đủ khi mở thêm.

**Nguồn:** `fragment_home.xml`, `HomeFragment.java`.

### 4.6 Hồ sơ — P2

**Giữ:** đã tách chỉnh thông tin cá nhân khỏi cấu hình nước; có đơn vị và phạm vi áp dụng BMI rõ hơn.

**Vấn đề:** phần nhận diện và các đoạn giải thích dài khiến màn mang tính biểu mẫu/thông tin hơn trang quản lý cá nhân. Nhiều lựa chọn tiêu chuẩn BMI cần giải thích nhưng không nên cạnh tranh với tên và số liệu chính.

**Nâng cấp:** đầu trang là hàng avatar–tên–chỉnh sửa; thông tin cơ thể có nhãn ngắn; nhóm thiết lập dạng hàng có tiêu đề, mô tả một dòng và mũi tên. Tiêu chuẩn BMI hiển thị lựa chọn hiện tại, mở phần giải thích khi cần. Tránh lạm dụng card bao quanh từng nhãn nhỏ.

**Nguồn:** `activity_profile.xml`, `ProfileActivity.java`.

### 4.7 Sửa hồ sơ cá nhân — P2

**Giữ:** chiều cao/cân nặng hiện đã xếp dọc; đăng xuất là outline thông thường, xóa tài khoản là hành động nguy hiểm. Đây là cải thiện đã có.

**Nâng cấp tiếp:** nhất quán nhãn tùy chọn, đơn vị ở cuối ô, nhóm năm sinh/giới tính thích ứng màn nhỏ; một nút “Lưu thay đổi” rõ ràng. Khi quay lại sau khi sửa chưa lưu, cần có cách tránh mất thay đổi. Tách phần tài khoản khỏi nhóm dữ liệu cơ thể bằng tiêu đề/khoảng cách. Kiểm tra nút lưu khi bàn phím mở và thông báo lỗi khi cuộn dài.

**Nguồn:** `activity_edit_profile.xml`, `EditProfileActivity.java`, chế độ thông tin cá nhân.

### 4.8 Cấu hình nước — P1/P2

**Giữ:** có màn chế độ riêng, lịch theo khoảng và giờ tùy chọn, giải thích phần tăng nước theo thời tiết và tùy chọn bật/tắt.

**Vấn đề:** nhiều giải thích, radio, chip giờ và công tắc trên một trang dài; ảnh QA mới cho thấy một số trạng thái điều khiển vẫn có sắc mặc định khác xanh ngọc. Người dùng khó tóm tắt lịch cuối cùng sau khi chỉnh.

**Nâng cấp:** ba nhóm “Mục tiêu và cỡ cốc”, “Lịch nhắc”, “Tùy chọn thời tiết”; hiển thị bản tóm tắt lịch ngay dưới lựa chọn. Chip giờ có trạng thái chọn/xóa rõ, đủ vùng chạm. Rút gọn phần mô tả luôn hiện, mở thêm giải thích đầy đủ; giữ rõ giới hạn của gợi ý theo thời tiết. Nút lưu dễ tìm và không che nội dung/bàn phím. Không gắn công tắc tăng nước với một lời hứa y khoa.

**Nguồn:** `activity_edit_profile.xml`, `EditProfileActivity.java`, `values/water_schedule.xml`, `values/weather_advice.xml`.

### 4.9 Nhật ký sức khỏe — P1

**Giữ:** lọc khoảng ngày và mở từng bản ghi là mô hình quen thuộc.

**Vấn đề:** trạng thái trống dựa nhiều vào icon/nội dung mờ và nút nổi; chưa diễn đạt rõ khác biệt giữa chưa từng ghi và không có kết quả trong khoảng lọc. Dòng thời gian đi kèm giờ/bối cảnh bữa ăn có thể dài và cạnh tranh badge trạng thái. Các thao tác ẩn qua nhấn giữ khó khám phá.

**Nâng cấp:** trống lần đầu có nút “Ghi chỉ số đầu tiên”; trống do lọc có “Xóa bộ lọc”. Mỗi mục gồm số liệu chính, đơn vị, hàng thời gian riêng và nhãn trạng thái bằng chữ. Dùng menu rõ cho thao tác phụ thay vì chỉ nhấn giữ. Giữ chạm cả hàng để mở chi tiết.

**Nguồn:** `fragment_health.xml`, `item_health_record.xml`, `HealthFragment.java`, `HealthRecordAdapter.java`.

### 4.10 Thêm/sửa chỉ số — P1

**Giữ:** ngày giờ và đơn vị đo đã hiện diện, có lỗi nhập tại trường.

**Vấn đề:** nhiều phép đo khác loại cùng một form; cân nặng và đường huyết cạnh nhau không tạo nhóm ý nghĩa. Nhiều hàng hai cột dễ chật. Ngữ cảnh bữa ăn được trình bày ngay cả khi chưa nhập đường huyết, làm tăng tải đọc.

**Nâng cấp:** nhóm “Huyết áp và nhịp tim”, “Cân nặng”, “Đường huyết”; trường tùy chọn được nói rõ. Ngữ cảnh bữa ăn liên kết trực quan với đường huyết. Dùng suffix đơn vị thay vì lặp trong cả nhãn lẫn gợi ý. Giữ ngày/giờ dễ sửa và phân biệt tiêu đề thêm với sửa. Không ẩn các trường đã có giá trị khi chỉnh sửa. Ưu tiên một cột khi chữ lớn, bàn phím số phù hợp và chuyển focus đúng thứ tự.

**Nguồn:** `activity_add_health.xml`, `AddHealthActivity.java`.

### 4.11 Chi tiết chỉ số — P1/P2

**Giữ:** tách kết quả, đánh giá và ghi chú giúp người dùng đọc sâu sau khi chọn bản ghi.

**Vấn đề:** số lớn, badge, thời điểm và bối cảnh dài có thể tranh chiều ngang. Nhiều khối tư vấn có trọng lượng gần số đo. Nút cao cố định cần thử chữ lớn. Màu cảnh báo không nên là cách duy nhất để hiểu kết quả.

**Nâng cấp:** hàng ngày/giờ riêng; số đo và đơn vị rõ; trạng thái bằng từ ngắn có icon hỗ trợ; giải thích và lưu ý ở dưới, mở thêm nếu dài. Phân biệt dữ liệu người dùng nhập với nhận xét tự động. Sửa là hành động chính; xóa đặt trong menu có xác nhận. Nội dung nhạy cảm không bị làm mờ đến mức khó đọc.

**Nguồn:** `activity_detail_health.xml`, `DetailHealthActivity.java`, `HealthDisplay.java`.

### 4.12 Nhắc nhở: nước và thuốc — P1

**Giữ:** nước ghi nhanh, có hoàn tác; thuốc có trạng thái từng cữ, lịch sử và điều khiển nhắc.

**Vấn đề:** nước ở đầu cộng danh sách thuốc bên dưới tạo trang dài. Mỗi thuốc có nhiều giờ, trạng thái, hai hành động cho từng cữ và nút lịch sử, khiến số điều khiển tăng nhanh khi có nhiều thuốc. Bật/tắt nhắc dễ bị hiểu nhầm thành ngừng liệu trình nếu nhãn không rõ.

**Nâng cấp:** phần nước gọn; thuốc theo “Sắp tới”, “Đã xử lý” hoặc nhóm rõ theo ngày. Cữ cần làm nổi bật, “Đã uống” chính và “Bỏ qua” phụ; cữ đã xử lý gọn, vẫn có đường sửa trạng thái rõ. Lịch sử là hành động phụ. Công tắc ghi “Nhắc uống thuốc”, không hàm ý đã dừng dùng thuốc. Giải thích quyền thông báo đúng lúc; không chiếm toàn bộ màn khi quyền chưa bật.

**Tình huống nghiệm thu:** 5 thuốc × 3 cữ/ngày; tên thuốc dài; một cữ bỏ qua; toàn bộ đã uống; chưa có thuốc; quyền thông báo tắt.

**Nguồn:** `fragment_reminder.xml`, `item_medicine.xml`, `item_medicine_dose.xml`, `ReminderFragment.java`, `MedicineAdapter.java`.

### 4.13 Thêm/sửa thuốc — P1

**Giữ:** nhiều giờ và chọn ngày trong tuần đáp ứng lịch cơ bản, có dữ liệu liều lượng.

**Vấn đề:** giờ được lặp ở ô tổng hợp và chip, làm thao tác thêm/sửa giờ thiếu rõ ràng. Bảy ô ngày chia đều một hàng: với màn 320dp, lề ngang 16dp và khoảng cách 4dp, mỗi ô chỉ khoảng 38dp, nhỏ hơn mục tiêu chạm 48dp dù chiều cao là 48dp. Nhãn ngày cũng dễ chật khi tăng chữ.

**Nâng cấp:** một danh sách chip giờ và nút “Thêm giờ”; chạm chip để sửa, xóa có nhãn tiếp cận. Chọn ngày có vùng chạm đủ rộng bằng bố cục xuống dòng/hai hàng phù hợp. Dưới nhóm hiển thị lịch tóm tắt, ví dụ “3 lần/ngày • Thứ 2–Thứ 6”. Không để ô tổng hợp trông như thay giờ nhưng thực tế thêm giờ. Giữ dữ liệu đang nhập khi xoay màn hình hoặc chọn giờ.

**Nguồn:** `activity_add_medicine.xml`, `AddMedicineActivity.java`.

### 4.14 Thống kê — P1, ưu tiên cao

**Giữ:** có phạm vi ngày và nhiều chỉ số, biểu đồ giúp nhìn lại dữ liệu.

**Vấn đề:** sáu biểu đồ xếp liên tiếp thành trang dài; người dùng phải tìm chỉ số cần xem. Ảnh QA cũ cho thấy thông báo thiếu dữ liệu bị cắt; mã hiện vẫn dùng no-data text của thư viện, nên cần kiểm tra lại chứ chưa coi đã xác nhận bằng ảnh mới. Trục/chú giải nhỏ, nhiều đường lưới; ngày không có bản ghi cần phân biệt với giá trị bằng 0. Ghi giấc ngủ qua nút nổi trong thống kê khó khám phá.

**Nâng cấp:** chọn chỉ số bằng chip/tab nội bộ; một biểu đồ chính cùng lần ghi gần nhất, số bản ghi và thông tin tóm tắt phù hợp. Không tính/hiển thị trung bình đường huyết trộn các bối cảnh như thể tương đương. Trạng thái ít dữ liệu dùng khối chữ có xuống dòng và CTA ghi dữ liệu, không phụ thuộc chữ vẽ trong canvas. Có danh sách số liệu đọc được bằng TalkBack thay cho biểu đồ. Dùng nhãn “Ghi giấc ngủ” rõ, bổ sung lối vào từ nơi phù hợp.

**Tình huống nghiệm thu:** 0, 1, 2 và 30 bản ghi; thiếu ngày; giá trị cực trị; phạm vi dài; chữ lớn; một màu vẫn đọc được nhờ nhãn/kiểu đường.

**Nguồn:** `fragment_stats.xml`, `StatsFragment.java`, `marker_chart.xml`, `ChartMarker.java`.

### 4.15 Thêm/sửa giấc ngủ — P1/P2

**Giữ:** chọn giờ ngủ/thức và xem thời lượng ngay là tương tác dễ hiểu.

**Vấn đề:** tiêu đề theo ngữ cảnh “đêm qua” không phù hợp mọi bản ghi sửa hoặc giấc ngủ khác. Hai giờ không có ngày đủ rõ sẽ khiến người dùng tự suy ra thời điểm qua nửa đêm. Icon trang trí và kết luận có thể chiếm diện tích của dữ liệu nhập.

**Nâng cấp:** tiêu đề trung tính “Ghi giấc ngủ”/“Sửa giấc ngủ”; hiển thị ngày cùng giờ hai đầu và thời lượng tính ra. Cho người dùng kiểm tra một khoảng cụ thể, ví dụ 16/10 23:00 → 17/10 06:30. Giảm icon trang trí, giữ kết quả dễ đọc, giải thích ngắn. Không dùng màu/chất lượng giấc ngủ như kết luận chẩn đoán.

**Nguồn:** `activity_add_sleep.xml`, `AddSleepActivity.java`, `item_sleep_record.xml`.

### 4.16 Hộp thoại, chọn ngày/giờ và thông báo — P1/P2

**Xóa tài khoản:** giữ yêu cầu mật khẩu và giải thích dữ liệu mất. Hành động xác nhận xóa trong AlertDialog hiện theo màu mặc định của theme; nên dùng vai trò nguy hiểm, hủy trung tính. Giữ phần nội dung cuộn được khi chữ lớn/bàn phím hiện. Gợi ý mật khẩu đã được rút ngắn trong mã mới; không báo lại lỗi cắt nhãn của ảnh cũ như lỗi hiện tại.

**Xóa bản ghi/đăng xuất:** thông điệp nói đúng đối tượng và hậu quả; không dùng cùng màu/nhấn mạnh cho xóa dữ liệu và đăng xuất. Ưu tiên hoàn tác cho thao tác có thể hoàn tác; giữ xác nhận cho thao tác mất dữ liệu đáng kể.

**Lịch sử thuốc:** dùng danh sách dễ quét ngày–giờ–trạng thái, có trạng thái trống, nội dung dài không đẩy nút đóng ra khỏi màn.

**Chọn ngày/giờ:** nhất quán định dạng địa phương, kiểm tra 12/24 giờ và khoảng qua nửa đêm; vùng bấm không chỉ là icon nhỏ.

**Thông báo hệ thống:** nội dung đầu tiên là tên việc, thời gian và hành động; icon đơn sắc đúng nghĩa. Không hứa toàn bộ màu sắc/bố cục theo ý app vì Android kiểm soát phần này. Ngôn ngữ của hộp thoại xin quyền phụ thuộc hệ điều hành; tiếng Anh ở ảnh quyền không đủ để kết luận app dịch thiếu. Kiểm tra nhãn thao tác, quyền bị từ chối và màn khóa trên thiết bị thực trước khi kết luận khả năng tiếp cận/quyền riêng tư.

**Nguồn:** `dialog_delete_profile.xml`, `EditProfileActivity.java`, các Activity xóa bản ghi, `NotificationHelper.java`, các receiver.

## 5. Đánh giá xuyên suốt

| Khía cạnh | Nhận xét | Quyết định nâng cấp |
|---|---|---|
| Kiến trúc thông tin | Bốn tab hợp lý nhưng chưa gom việc hôm nay | Tái tổ chức Home; giữ số tab |
| Thứ bậc | Card, giải thích, số đo và thao tác phụ cạnh tranh nhau | Mỗi vùng một số liệu/hành động chính; nội dung phụ mở thêm |
| Màu | Nhận diện teal phù hợp; vai trò màu và control mặc định chưa đều | Một hệ semantic cho primary, nền, viền, chữ, nguy hiểm, cảnh báo |
| Typography | Có thang cỡ chữ; chữ giải thích/biểu đồ và hàng hai cột cần cải thiện | Body chính 16sp làm điểm khởi đầu; chữ lớn theo hệ thống; không ép toàn bộ về cỡ nhỏ |
| Khoảng cách | Có token 4–48dp nhưng độ dày từng màn chưa cân bằng | Lề 16–24dp, nhóm 24dp, phần tử 8–16dp tùy nội dung |
| Card và shape | Có phong cách Material nhưng nhiều bề mặt và độ bo chưa tạo hệ rõ | Tái sử dụng style; ít elevation, card chỉ khi giúp chia nhóm |
| Icon và thương hiệu | Logo ViTa có, launcher mặc định làm giảm hoàn thiện | Đồng nhất tài sản, icon đơn sắc và nét; nhãn cho hành động ít quen |
| Nhập liệu | Nhiều form dài, nhiều cột, đơn vị/ngữ cảnh chưa nhất quán | Nhóm theo ý nghĩa, tùy chọn rõ, focus lỗi, bàn phím đúng |
| Trạng thái | Có empty/error nhưng nhiều hướng dẫn mờ hoặc chung chung | Phân biệt trống lần đầu, không có kết quả, mất quyền, lỗi mạng |
| Phản hồi | Nước đã có hoàn tác; thuốc nhiều nút | Phản hồi ngắn gọn, giữ trạng thái; kiểm tra thông báo bằng TalkBack |
| Dữ liệu | Biểu đồ và số đo đã có nhưng khó quét khi dài/ít dữ liệu | Đơn vị, thời điểm, ngữ cảnh; không nối thiếu dữ liệu thành giá trị 0 |
| Khả năng tiếp cận | Có mục tiêu 48dp trong tokens nhưng chưa bảo đảm mọi chiều | Kiểm tra vùng chạm thực, tương phản, thứ tự đọc và font scale |
| Thích ứng | Chưa thấy hệ layout rộng/dark hoàn chỉnh trong phạm vi kiểm tra | Ưu tiên điện thoại nhỏ/chữ lớn; tablet và dark làm sau khi nền vững |
| Cảm nhận tốc độ | Không có đo latency, không thể kết luận chậm | Bỏ chờ trang trí; giữ vị trí tab; loading chỉ ở công việc chờ thật |

### Tương phản cần sửa trước khi đổi bảng màu

Từ mã màu khai báo, theo phép tính tương phản sRGB trên nền phẳng:

| Cặp màu | Tỷ lệ xấp xỉ | Ý nghĩa |
|---|---:|---|
| Chữ trắng / primary `#00897B` | 4,32:1 | Chưa đạt mốc 4,5:1 cho chữ thường theo WCAG; chữ lớn có yêu cầu khác |
| Cam `#F57C00` / nền cảnh báo `#FFF3E0` | 2,47:1 | Không phù hợp làm chữ cảnh báo nhỏ |
| Vàng `#FBC02D` / trắng | 1,66:1 | Không dùng làm chữ hoặc dấu hiệu duy nhất trên nền trắng |
| Chữ chính `#1C1B1F` / trắng | 17,13:1 | Nền tảng đọc tốt |

Đây là kiểm tra cặp token, không khẳng định mọi pixel thực tế có cùng nền; phải đo lại các trạng thái rendered, disabled, pressed và background tint. Mốc tham khảo: [WCAG contrast minimum](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum/). Vùng chạm tham khảo Android: [48 × 48dp](https://support.google.com/accessibility/android/answer/7101858?hl=en).

Giữ xanh ngọc làm nhận diện, dùng sắc đậm hơn cho nút có chữ trắng nếu cần; cảnh báo dùng chữ đậm dễ đọc trên nền nhạt. Màu xanh dương cho nước và tím cho giấc ngủ có thể giữ như màu chuyên mục, nhưng nhãn/đơn vị phải đủ để hiểu khi không phân biệt màu. Đổi màu theo vai trò, không đổi tất cả về một màu.

## 6. Thứ tự nâng cấp khuyến nghị

### Đợt 1 — nền đọc và thao tác

- Sửa cặp màu khó đọc, các vùng chạm ngày trong tuần và form chật.
- Đồng nhất nút, trường nhập, card, tiêu đề, nhãn trạng thái và icon.
- Kiểm tra chữ lớn, bàn phím, thanh điều hướng, thanh trạng thái và nút lưu.
- Đồng nhất launcher/system splash ViTa.

### Đợt 2 — ba luồng tạo khác biệt lớn

- Home theo việc hôm nay, tận dụng dữ liệu đang có.
- Nhắc thuốc ưu tiên cữ tiếp theo; nước gọn và dễ ghi nhanh.
- Thống kê chọn chỉ số, trạng thái ít dữ liệu rõ, có danh sách thay biểu đồ.

### Đợt 3 — hoàn thiện phần còn lại

- Đăng nhập/đăng ký và các form nhập theo cùng một hệ.
- Hồ sơ/cấu hình giảm chữ luôn hiện, giữ giải thích đầy đủ khi mở.
- Ngữ cảnh giấc ngủ, các hộp thoại và thông báo nhất quán.
- Sau đó mới quyết định dark mode, tablet và animation có ích.

Không cần bắt đầu bằng việc vẽ lại toàn bộ. Chọn Home → Reminder → Stats làm luồng mẫu, chốt các component dùng lại rồi áp dụng sang các màn còn lại.

## 7. Tiêu chí nghiệm thu sau nâng cấp

1. Mở Home biết việc tiếp theo và truy cập ghi nước/ghi chỉ số mà không phải dò nhiều tab.
2. Màn 320dp, 360dp, điện thoại lớn; chữ 100%, 130%, 200%: thông tin chính và hành động không bị cắt/mất. Cho phép cuộn thay vì thu nhỏ chữ.
3. Bàn phím mở: biết trường đang sửa, xem được lỗi và có đường hoàn tất/lưu.
4. Toàn bộ nút/icon/chip tương tác đạt vùng chạm phù hợp; không chỉ kiểm tra chiều cao.
5. TalkBack đọc tên, giá trị, đơn vị, trạng thái chọn/bật và lỗi đúng thứ tự; biểu đồ có cách đọc dữ liệu thay thế.
6. Kiểm tra không dữ liệu, dữ liệu ít, tên dài, nhiều thuốc, ngày thiếu bản ghi, thời tiết lỗi, quyền tắt, mất mạng và quay lại từ nền.
7. Quay lại tab giữ ngữ cảnh hợp lý; thêm và sửa được phân biệt; thao tác mất dữ liệu có bảo vệ rõ.
8. Không dùng màu làm tín hiệu duy nhất; đo tương phản trên giao diện rendered.
9. Thử với người dùng mục tiêu, gồm người ít quen công nghệ nếu đây là nhóm hướng tới: yêu cầu tìm cữ thuốc, ghi nước, ghi huyết áp, xem lại số đo và sửa hồ sơ. Ghi lại chỗ do dự/sai thao tác; chưa đặt mục tiêu thời gian tùy ý khi chưa có baseline.

## 8. Phạm vi mã đối chiếu

Gốc nguồn: `Vita_Project/app/src/main/`. Các tên Activity/Fragment trong phần trên tương ứng thư mục `java/vn/edu/tdmu/vita/activities` và `fragments`; layout trong `res/layout`. Các thành phần dùng chung: `res/values/themes.xml`, `colors.xml`, `dimens.xml`, `strings.xml`, `res/values-v29/themes.xml`, adapter và helper hiển thị.

Ảnh mới chụp để kiểm tra khởi động: `.audit-cache/ui-review-2026-10-17/current.png` và `live.png`. Ảnh QA hỗ trợ: `.audit-cache/profile-qa/personal-compact.png`, `.audit-cache/profile-qa/delete-compact.png`, `.audit-cache/water-qa/custom-schedule.png`. Những ảnh này không thay thế việc kiểm thử toàn bộ bản hiện tại.

Chưa sửa mã ứng dụng, chưa chạy lại build/test cho một thay đổi UI vì lượt này chỉ đánh giá. Các khuyến nghị cần được kiểm chứng bằng bản render mới và kiểm thử khả năng tiếp cận khi triển khai.
