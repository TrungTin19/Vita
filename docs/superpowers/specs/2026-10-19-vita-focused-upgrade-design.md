# Thiết kế nâng cấp ViTa theo phạm vi hiện tại

Ngày: 19/10/2026. Trạng thái: bản thiết kế để người dùng duyệt, chưa phải báo cáo đã triển khai.

## 1. Mục tiêu và nguồn yêu cầu

Nâng cấp toàn bộ các chức năng hiện có theo bản đánh giá người dùng gửi ngày 19/10/2026: ghi chỉ số → xem diễn biến → duy trì lịch thuốc và nước. Hoàn thiện độ tin cậy, khả năng kiểm tra/sửa sai và sự liên kết giữa các màn hình.

Tệp nguồn yêu cầu: `C:/Users/Trung Tin/.codex/attachments/2c579adb-04f7-4927-ba30-aa8e3f141118/Pasted text.txt`.

Giữ nền tảng Android Native Java/XML, Material và SQLite. Giữ bốn tab Trang chủ, Chỉ số, Nhắc nhở, Thống kê và lối vào Hồ sơ hiện có. Giữ màu xanh ngọc, font hệ thống, bố cục thẻ và các thành phần hiện tại; điều chỉnh thứ bậc nội dung, nhãn và thao tác để dễ sử dụng.

Không đưa trở lại thời tiết, AQI, giấc ngủ hoặc sao lưu/khôi phục. Không thêm máy chủ, Google Login, điểm sức khỏe tổng hợp, AI hay nhóm chức năng mới. Không triển khai lại thiết kế Figma chưa được chọn.

Giả định sử dụng: một người đang đăng nhập tại một thời điểm trên thiết bị; có thể có nhiều hồ sơ cục bộ. Dữ liệu từng hồ sơ phải được tách biệt.

## 2. Phương án và lựa chọn

| Phương án | Đánh đổi |
| --- | --- |
| Nâng cấp lần lượt các module, tái sử dụng DAO/helper/Material đang có — đề xuất | Giữ phong cách app, kiểm chứng từng luồng, giới hạn thay đổi đồng thời. |
| Chỉ sửa giao diện | Ít thay đổi ban đầu nhưng không hoàn thành yêu cầu sửa dữ liệu, bảo vệ hồ sơ và giữ trạng thái. |
| Viết lại kiến trúc hoặc chuyển Compose/framework khác | Nhiều mã và rủi ro chuyển đổi; không cần cho phạm vi đồ án này. |

Chọn phương án đầu. Các thay đổi schema chỉ bổ sung khi dữ liệu hiện có chưa biểu diễn được hành vi cần giữ. Không thêm lớp repository/event bus chung chỉ để bọc lại các DAO.

## 3. Hiện trạng đã đối chiếu

- `HomeFragment` hiện có lời chào và BMI; chưa tổng hợp thuốc, nước, lần đo gần nhất.
- `HealthFragment` có lọc khoảng ngày, xem chi tiết, sửa/xóa; `HealthDao.getById/update/delete` đang dựa vào ID bản ghi, chưa ràng buộc chủ sở hữu ngay tại truy vấn.
- `WaterDao` có thêm, tổng ngày, đọc danh sách theo ngày và xóa đúng người dùng; chưa có cập nhật bản ghi và giao diện nhật ký.
- Thuốc có nhiều giờ, ngày trong tuần, trạng thái theo thuốc/ngày/giờ và lịch sử; lịch sử trên giao diện chưa trình bày đầy đủ thời điểm xác nhận và sửa trạng thái cũ.
- `MainActivity` dùng `replace` với fragment mới khi đổi tab; trạng thái lọc/vị trí xem có thể mất.
- Hồ sơ dùng cân nặng mới nhất cho BMI nhưng chưa hiển thị rõ bản ghi nguồn và thời điểm.
- Đăng xuất hiện có hủy báo thức, thông báo và phiên; tiếp tục dùng luồng này và kiểm tra các trường hợp chuyển tài khoản/thao tác cũ.
- Mã đang có các thay đổi gỡ thời tiết và giấc ngủ chưa commit. Giữ các thay đổi này làm điểm xuất phát; hai tệp Figma chưa theo dõi trong Git thuộc công việc trước đó, không chỉnh trong lần nâng cấp này.
- Lần xác minh trước chưa biên dịch được do cấu hình Gradle/cache/plugin của môi trường. Phải chẩn đoán lại bằng log mới trước khi kết luận nguyên nhân hoặc sửa cấu hình.

## 4. Quy tắc chung về dữ liệu và thời gian

1. Mọi truy vấn đọc/sửa/xóa bản ghi phải ràng buộc ID chủ sở hữu, không chỉ kiểm tra ở giao diện. Áp dụng cho chỉ số, thuốc, nhật ký thuốc và nước; cập nhật toàn bộ caller và test khi đổi chữ ký.
2. Activity đang mở phải xác minh phiên còn đúng chủ sở hữu trước khi hiển thị lại hoặc lưu. Thao tác từ thông báo cũ của tài khoản A không được ghi vào A hoặc B sau khi chuyển sang B.
3. Dữ liệu thiếu lưu bằng `NULL`/giá trị chưa xác định hiện có. Giao diện dùng “Chưa nhập”, “Chưa có bản ghi” đúng ngữ cảnh; không biến dữ liệu thiếu thành số 0.
4. Lần ghi trước/lần mới nhất được xác định bởi ngày, giờ đo và ID để có thứ tự ổn định. Bản ghi thiếu giờ hiển thị “Chưa ghi giờ”; không tự tạo giờ đo giả.
5. “Hôm nay” lấy theo ngày địa phương tại thời điểm thao tác. Khi app quay lại từ nền, khi đổi ngày hoặc giờ/múi giờ hệ thống, các màn tổng quan/lịch nhắc cần cập nhật theo ngày mới.
6. Lưu thành công rồi mới báo thành công và đóng form. Khi lỗi, giữ dữ liệu đang nhập và cho thử lại. Không xóa/recreate database để xử lý lỗi nâng cấp.

## 5. Module tài khoản

### Hành vi

- Đăng ký hiển thị một đoạn ngắn: hồ sơ và dữ liệu lưu trên thiết bị; chưa hỗ trợ khôi phục tài khoản. Dùng tiếng Việt rõ ràng, không tạo một bước hướng dẫn mới.
- Đăng nhập sai giữ nguyên dữ liệu nhập; lỗi rỗng/sai thông tin hiển thị tại trường liên quan.
- Hộp thoại đăng xuất nói rõ dữ liệu vẫn được giữ. Xóa hồ sơ liệt kê dữ liệu hiện có bị xóa và yêu cầu mật khẩu như luồng hiện tại.
- Đăng xuất/xóa hồ sơ hủy báo thức và thông báo của phiên cũ trước khi người dùng tiếp tục bằng tài khoản khác. Không thay đổi thông tin đăng nhập hay cơ chế mật khẩu ngoài phạm vi cần cho luồng này.

### Nghiệm thu

- Tài khoản B không xem, sửa, xóa được dữ liệu A dù truyền trực tiếp ID hoặc giữ một màn hình cũ.
- Đăng xuất rồi đăng nhập lại vẫn có dữ liệu; xóa hồ sơ chỉ xóa dữ liệu thuộc hồ sơ đã xác nhận.
- Nhấn hành động của thông báo cũ sau đổi phiên không tạo bản ghi.

## 6. Module hồ sơ, BMI và bảo vệ form

### Hành vi

- Hiển thị riêng “Cân nặng ban đầu” và “Cân nặng gần nhất”, kèm ngày/giờ nếu có nhật ký cân.
- Thẻ BMI chỉ rõ nguồn cân nặng đang dùng và chuẩn áp dụng. Nếu chưa ghi cân trong nhật ký, ghi rõ dùng cân nặng ban đầu.
- Sửa cân nặng ban đầu chỉ thay dữ liệu nền của hồ sơ, không sinh bản ghi cân mới. Khi đã có lần cân mới nhất, BMI tiếp tục dùng lần cân đó.
- Giải thích ngắn chuẩn Châu Á và WHO khác ngưỡng phân loại; giữ quy tắc phạm vi người lớn đang có. Không thay đổi ngưỡng y tế trong lần nâng cấp UX này.
- Các form sửa hồ sơ, cấu hình nước, thêm/sửa chỉ số và thuốc cảnh báo khi Back hoặc nút quay lại sẽ bỏ thay đổi thực sự chưa lưu. Hai lựa chọn: “Tiếp tục chỉnh sửa” và “Bỏ thay đổi”. Form không thay đổi hoặc lưu thành công không hỏi lại.
- Giữ bản nháp khi tái tạo màn hình theo vòng đời Android. Trạng thái bẩn so với dữ liệu gốc được khôi phục; không hỏi chỉ vì vừa khởi tạo form hoặc chuẩn hóa số/giờ tương đương.

### Nghiệm thu

- Sửa cân nặng ban đầu khi có/không có nhật ký cân cho kết quả và nhãn nguồn đúng.
- Back bằng thao tác hệ thống và nút trên toolbar đều bảo vệ dữ liệu nháp.
- Quay màn hình không mất bản nháp, lịch nhiều giờ và lựa chọn ngày.

## 7. Module chỉ số sức khỏe

### Hành vi

- Giữ một form ghi chỉ số. Thêm lựa chọn nhóm ngay đầu form: “Tất cả”, “Huyết áp”, “Nhịp tim”, “Cân nặng”, “Đường huyết”. Đây là lựa chọn hiển thị/đi tới trường liên quan; chuyển nhóm không xóa giá trị người dùng đã nhập.
- Đơn vị luôn cạnh trường/số đo. Ngày, giờ, bữa ăn có nhãn rõ. Ngữ cảnh bữa ăn hiện khi đang nhập đường huyết hoặc bản ghi có đường huyết.
- Giữ khả năng ghi một hoặc nhiều chỉ số; cho phép bản ghi chỉ có ghi chú như hiện tại. Không tạo dữ liệu mặc định giả cho trường bỏ trống.
- Danh sách hiển thị khoảng ngày đang áp dụng và số bản ghi; có hành động “Bỏ lọc” và trạng thái riêng cho chưa có dữ liệu/không có kết quả trong bộ lọc.
- Chi tiết thêm đối chiếu với lần trước của từng chỉ số có trong bản ghi. Huyết áp chỉ đối chiếu với bản ghi trước có đủ cả hai trị số.
- Đường huyết chỉ đối chiếu khi hai bản ghi có cùng ngữ cảnh bữa ăn đã xác định. Với ngữ cảnh chưa xác định, trình bày số đo/ngày giờ và thông báo chưa có điều kiện so sánh, không tạo chênh lệch gây hiểu nhầm.
- Chênh lệch chỉ là số tăng/giảm cùng đơn vị và thời điểm tham chiếu; không diễn giải thành cải thiện/xấu đi tự động.
- Xóa cần xác nhận; thêm/sửa/xóa cập nhật danh sách, lần ghi gần nhất, thẻ BMI và thống kê khi quay lại.

### Nghiệm thu

- Nhiều lần đo cùng ngày vẫn được giữ riêng và có thứ tự ổn định.
- Không dùng bản ghi khác người, bản ghi tương lai so với lần đang xem hoặc bản ghi khác ngữ cảnh làm “lần trước”.
- Xóa lần cân mới nhất làm BMI dùng lần cân trước; xóa hết lần cân làm BMI dùng cân nặng ban đầu.

## 8. Module thuốc và từng cữ

### Hành vi

- Trong tab Nhắc nhở, phần cữ hôm nay có bốn nhóm: “Chưa ghi nhận” (đã tới giờ), “Sắp tới”, “Đã uống”, “Bỏ qua”. Mỗi cữ chỉ thuộc một nhóm. Bản ghi chưa xác nhận không tự chuyển thành bỏ qua.
- Giữ danh sách quản lý thuốc với nút thêm/sửa/xóa và công tắc mang nhãn “Bật nhắc”. Công tắc chỉ tác động báo thức, không tuyên bố ngừng điều trị và không xóa nhật ký.
- Mỗi cữ hiển thị tên thuốc, liều dùng, giờ dự kiến và trạng thái. Cữ đã xử lý hiển thị thời điểm xác nhận nếu dữ liệu có.
- Có thể sửa trạng thái một cữ từ hôm nay hoặc lịch sử: “Đã uống”, “Bỏ qua”, “Chưa ghi nhận”. Thao tác cuối cùng gỡ xác nhận của đúng cữ; trạng thái khác cập nhật đúng bản ghi, không tạo trùng.
- Nhật ký giữ ngày và giờ dự kiến cũ khi lịch thuốc thay đổi. Bản ghi mới lưu tên/liều tại lúc xác nhận để sửa tên/liều về sau không viết lại ý nghĩa lịch sử.
- Trước khi lưu lịch thuốc đã sửa, có giải thích ngắn: lịch mới dùng cho các lần nhắc tiếp theo, lịch sử đã ghi nhận được giữ.
- Banner quyền thông báo và báo thức có hành động mở đúng cài đặt. Nếu đang dùng báo thức không chính xác do thiếu quyền, phải giải thích có thể chậm; không báo rằng lịch chắc chắn đúng từng phút.

### Nghiệm thu

- Một thuốc có ba giờ được hiển thị/ghi nhận/sửa riêng từng cữ; trạng thái qua nửa đêm không bị gán nhầm ngày.
- Sửa lịch không sửa ngày/giờ dự kiến của log cũ; sửa trạng thái không ảnh hưởng cữ khác.
- Lịch sắp tới bỏ qua cữ đã xử lý; tính được ngày kế tiếp theo ngày trong tuần.
- Tắt nhắc, xóa thuốc, đổi phiên và reboot có hành vi đúng với báo thức đang tồn tại.

## 9. Module nước

### Hành vi

- Chạm thẻ tổng nước hôm nay để mở nhật ký trong dialog/bottom sheet; không thêm tab. Mỗi dòng có giờ, lượng ml, sửa và xóa.
- Có “Thêm lượng khác” bên cạnh ghi nhanh theo cỡ cốc. Ghi nhanh và hành động trên thông báo tiếp tục dùng cỡ cốc đã cấu hình.
- Form thêm lượng khác/sửa một lần ghi dùng lượng 50–2.000 ml, phù hợp giới hạn cỡ cốc hiện có; sửa được giờ trong ngày. Không ghi ngày tương lai. Bản ghi cũ hiển thị được dù định dạng thời điểm cũ thiếu phần ngày.
- Nhật ký mở từ thẻ nước là hôm nay; dữ liệu trong Thống kê có thể mở nhật ký của ngày được chọn để xem/sửa/xóa lần ghi ngày đó, dùng cùng giao diện.
- Xóa có xác nhận; ghi nhanh tiếp tục có Hoàn tác. Hoàn tác xóa đúng ID vừa tạo, không được xóa một mục khác sau khi đã sửa/xóa mục đó.
- Hiển thị mục tiêu, đã uống và còn lại. Đạt/vượt mục tiêu vẫn cho ghi; phần còn lại thấp nhất là 0, tổng thực tế giữ nguyên. Không tự tăng mục tiêu theo thời tiết.
- Giữ đúng hai kiểu lịch đã chốt: mỗi 2 giờ từ giờ bắt đầu, hoặc các mốc tự chọn nằm trong khung giờ. Hỗ trợ khung giờ qua nửa đêm đang có. Sau lưu, hiển thị tóm tắt rõ khung giờ, chế độ, mốc giờ và lượng mỗi lần.
- Sau thêm/sửa/xóa, cập nhật tổng, biểu đồ, Trang chủ và điều kiện nhắc. Nếu sửa/xóa khiến hôm nay chưa đạt mục tiêu, dùng mốc nhắc tiếp theo trong lịch, không bắn thông báo bất ngờ ngay khi sửa.

### Nghiệm thu

- Sửa/xóa một lần ghi chỉ đổi tổng đúng ngày và đúng hồ sơ.
- Ghi lượng khác không đổi cỡ cốc mặc định; đạt mục tiêu vẫn lưu thêm được.
- Các mốc trùng được gộp, sai định dạng/ngoài khung giờ bị từ chối; giờ bắt đầu trùng giờ kết thúc bị từ chối.
- Không thêm trùng do nhấn Lưu nhanh hai lần; dialog chỉ đóng sau ghi thành công.

## 10. Module Trang chủ

Thứ tự nội dung:

1. Lời chào và nút “Ghi chỉ số”.
2. Cữ thuốc chưa xử lý sắp tới: tên, liều, giờ/ngày dự kiến và lối vào Nhắc nhở. Nếu không có thuốc bật nhắc, hiển thị trạng thái tương ứng.
3. Nước hôm nay: đã uống/mục tiêu/còn lại, nút ghi nhanh, “Thêm lượng khác” và mở nhật ký. Dùng chung luồng ghi nước với tab Nhắc nhở.
4. Bản ghi chỉ số gần nhất: các số đo thực sự có, đơn vị và thời điểm; bấm mở chi tiết. Nếu chỉ có ghi chú, hiển thị là ghi chú.
5. BMI: giá trị, chuẩn, nguồn cân nặng/thời điểm và lối vào Hồ sơ.

Không tạo một loại dữ liệu tổng quan độc lập. Các thẻ đọc dữ liệu hiện có của phiên hiện tại, cập nhật khi quay lại màn và sau thao tác trên chính thẻ.

### Nghiệm thu

- Hồ sơ mới chưa có dữ liệu có lời dẫn và nút thực hiện được.
- Ghi nước tại Trang chủ và tab Nhắc nhở cho cùng tổng, cùng hoàn tác.
- Cữ thuốc sắp tới không phải cữ đã uống/bỏ qua; không lấy thuốc của hồ sơ khác.

## 11. Module Thống kê

### Hành vi

- Giữ hai khoảng 7/30 ngày tính đến hôm nay và năm nhóm dữ liệu; thêm lựa chọn nhóm chỉ số. Mỗi lần xem một biểu đồ chính thay cho cuộn năm biểu đồ liên tiếp.
- Hiển thị số bản ghi/số ngày có ghi tương ứng và lần ghi gần nhất kèm thời điểm, đơn vị. Không thêm trung bình tổng quát.
- Với đường huyết, lựa chọn ngữ cảnh “Tất cả”, “Lúc đói”, “Trước ăn”, “Sau ăn”, “Chưa xác định”; khi xem tất cả phải phân biệt nhóm, không nối các ngữ cảnh thành một đường xu hướng duy nhất.
- Chạm điểm hiện số đo, đơn vị, ngày/giờ và bữa ăn. Tooltip cho bản ghi nhiều lần cùng ngày phải dựa vào dữ liệu điểm, không chỉ nhãn ngày trên trục.
- Dưới biểu đồ có danh sách dữ liệu tương ứng, đọc được bằng TalkBack; bấm dòng chỉ số mở chi tiết bản ghi, bấm ngày nước mở nhật ký ngày đó.
- Chưa có bản ghi: thông điệp riêng và nút ghi phù hợp. Một điểm: vẫn hiển thị số đo/điểm và giải thích chưa đủ để thấy diễn biến. Không phụ thuộc đoạn chữ vẽ trong chart dễ bị cắt.
- Dữ liệu thiếu không thành giá trị 0. Với nước, ngày không có log được ghi “Chưa ghi nhận”; cột số 0 nếu dùng để giữ trục ngày phải có nhãn phản ánh việc này. Hôm nay có nhãn “Đang diễn ra”.
- Mục tiêu nước là mục tiêu hiện tại dùng tham chiếu cho khoảng xem; không mô tả như mục tiêu lịch sử từng ngày vì app chưa lưu lịch sử mục tiêu.
- Chọn nhóm/ngữ cảnh/khoảng ngày được giữ khi chuyển tab hoặc quay màn hình.

### Nghiệm thu

- Khoảng 7/30 ngày lấy cùng quy tắc ngày, thứ tự và tổng; bảng dữ liệu khớp từng điểm biểu đồ.
- Không trộn hồ sơ, không biến giá trị thiếu thành phép đo bằng 0, không tạo chênh lệch đường huyết giữa bối cảnh khác nhau.

## 12. Điều hướng, ngày mới và phong cách chung

- Giữ fragment bằng tag và phục hồi qua FragmentManager; tab ẩn không hoạt động như tab đang xem. Khi hiện lại, đọc dữ liệu mới đồng thời giữ bộ lọc và vị trí cuộn phù hợp.
- Các Activity biểu mẫu trả kết quả/thực hiện reload theo vòng đời hiện có. Chỉ tạo helper dùng chung khi ít nhất hai màn thực sự cần cùng quy tắc, ví dụ dialog nhật ký nước.
- Đổi ngày khi app đang hiển thị được cập nhật bằng callback thời gian gắn với vòng đời màn đang thấy; không thêm foreground service hoặc vòng lặp nền thường trực.
- Quyền, báo thức và hành động notification tiếp tục dùng API Android đang có. Chẩn đoán lỗi thực tế trước khi đổi chính sách scheduling.
- Dùng lại token màu/khoảng cách/kích thước trong resources. Vùng chạm tối thiểu 48dp; chữ dùng sp; lỗi gần trường và focus về lỗi đầu; không dùng màu làm tín hiệu duy nhất.
- Nút lưu, xác nhận xóa và bảo vệ bản nháp có nội dung nhất quán. Dialog dài cuộn được; bàn phím, navigation bar và cỡ chữ lớn không che thao tác chính.
- Skill ui-styling thiên về web: chỉ áp dụng nguyên tắc thứ bậc, khoảng cách và khả năng tiếp cận phù hợp Android; không cài React/Tailwind/shadcn.
- Tra cứu ui-ux-pro-max đã chạy nhưng kết quả thiết kế chung và chart không khớp app native. Không lưu hoặc áp dụng các gợi ý đó; dùng quy tắc native trong skill và token hiện có.

## 13. Schema và tương thích

- Cân nặng gần nhất, lần trước, tổng nước và cữ kế tiếp lấy từ dữ liệu hiện có; không thêm bảng cho dữ liệu có thể truy vấn.
- Nếu lưu snapshot tên/liều thuốc, nâng schema từ 5 lên 6 bằng bổ sung cột và backfill log cũ từ thuốc hiện tại; đánh dấu nguồn cũ là dữ liệu kế thừa, không giả vờ khôi phục được tên/liều tại thời điểm trước khi có snapshot.
- Ngày/giờ dự kiến cũ và giá trị chỉ số/nước giữ nguyên. Bổ sung index/uniqueness chỉ sau khi xử lý trường hợp log thuốc cũ trùng một cữ bằng quy tắc giữ bản ghi ID mới nhất và kiểm thử migration không mất các cữ khác.
- Bảng ngủ và cột thời tiết ở database đã cài trước đó vẫn được giữ vật lý nhưng không dùng. Không thêm DROP TABLE/COLUMN cho chúng trong đợt này.
- Mọi API DAO thay đổi phải sửa toàn bộ Activity/Fragment/Receiver/Adapter/test và fixture liên quan. Không giữ API thiếu quyền sở hữu làm đường đi tắt cho giao diện mới.

## 14. Kiểm chứng và điều kiện hoàn thành

### Thứ tự triển khai sau khi thiết kế và kế hoạch được duyệt

1. Chẩn đoán build/test nền; bảo vệ dữ liệu và chuyển phiên.
2. Nước: DAO, nhật ký, chỉnh sửa, lượng khác và đồng bộ tiến độ.
3. Thuốc: trạng thái từng cữ, sửa xác nhận, lịch sử và quyền nhắc.
4. Hồ sơ/BMI và bảo vệ bản nháp; chỉ số và đối chiếu đúng ngữ cảnh.
5. Điều hướng giữ trạng thái và cập nhật theo thời gian.
6. Trang chủ tổng hợp bằng các luồng đã hoàn thiện.
7. Thống kê chọn chỉ số, tooltip và danh sách tương ứng.
8. Đồng bộ UI/tài liệu hiện hành; kiểm thử tích hợp và review cuối.

### Quy trình từng module

- Với logic quan trọng: viết test mô tả hành vi, chạy thấy thất bại đúng lý do, sửa tối thiểu, chạy xanh rồi dọn code.
- Dùng systematic-debugging khi có bug/build/test lỗi: đọc log, tái hiện, truy ngược nguyên nhân và thử một giả thuyết mỗi lần. Lỗi môi trường không được coi là test RED của tính năng.
- XML/layout thuần trình bày không ép TDD; kiểm tra resource, compile, accessibility và giao diện thực tế.
- Dùng Ponytail Review sau mỗi module trên diff và caller liên quan; xử lý lỗi bắt buộc trước module phụ thuộc. Ghi rõ phần chưa kiểm chứng.
- Chạy toàn bộ unit test, build app/test APK và lint phù hợp; sau đó kiểm thử luồng trên emulator/thiết bị nếu môi trường cho phép.
- Instrumentation phải dùng applicationId riêng có hậu tố `.verification` hoặc database in-memory; không chạy test có xóa database trên gói người dùng đang dùng.
- Kiểm tra ít nhất: hồ sơ mới; hai hồ sơ; dữ liệu thiếu; nhiều lần đo/ngày; nhiều cữ/ngày; khung nước qua đêm; sửa/xóa/hoàn tác; đổi ngày; đổi phiên; rotate; quyền tắt/bật; reboot và mở lại app.

Chỉ coi toàn bộ nâng cấp hoàn thành khi các mục đã triển khai, các kiểm tra chạy được đều đạt, và những hạn chế môi trường còn lại được báo rõ. Chưa có kết quả build mới thì không tuyên bố APK đã được xác nhận.
