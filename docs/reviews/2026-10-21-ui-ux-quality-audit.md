# ViTa — UI/UX quality audit

Ngày: 21/10/2026. Phạm vi: giao diện Android hiện có; không sửa mã ứng dụng.

## 1. Kết luận và phạm vi bằng chứng

**66/100 — điểm tạm thời, độ tin cậy trung bình.** ViTa có nền tảng thị giác tương đối thống nhất, các hành động chính dễ nhận biết và một số trạng thái trống/lỗi tốt. Tuy nhiên, khả năng đọc chữ phụ, vùng chạm, cách trình bày dữ liệu sức khỏe và tính nhất quán giữa các màn chưa đủ tốt để kết luận đạt chuẩn sử dụng thực tế.

Đây là đánh giá chuyên gia dựa trên ảnh và mã nguồn, không phải kết quả nghiên cứu với người dùng hay chứng nhận accessibility. Các mục chưa kiểm tra không được mặc định đạt điểm tối đa; điểm không có nghĩa mọi khả năng chưa kiểm tra đều đang lỗi.

### Bằng chứng đã đọc

- Cấu trúc `Vita_Project/app`: Java17/XML Views, Material, MPAndroidChart, SQLite, Activity/Fragment/Adapter/DAO; minSdk24, targetSdk34. Không đề xuất thay MVC, dependency, dữ liệu hay nghiệp vụ.
- Manifest, build config, 23 layout XML, màu/dimensions/theme và các đoạn xử lý hiển thị liên quan ở Home, Reminder, Stats, HealthRecordAdapter, AddMedicine và form.
- Đã áp dụng checklist của ui-ux-pro-max, design-system, ponytail-review và ponytail-audit. Chỉ audit UI và các đường hiển thị liên quan; không gọi đây là audit bảo mật toàn repo.
- 39 file `figma-*.png` trong `.audit-cache/new-figma-final-screenshots/`; đã mở bốn bảng ảnh `.audit-cache/ui-audit-sheet-1.png` đến `ui-audit-sheet-4.png`. Đây là ảnh chạy Android, không phải ảnh thiết kế dù có tiền tố figma.
- Ảnh chủ yếu ở824×1864px, density320, chiều rộng412dp; font mặc định. Fixture được ghi thật vào SQLite trong gói verification; không phải dữ liệu sức khỏe thật của người dùng. Do bộ dữ liệu ít và tên ngắn, chưa chứng minh được trải nghiệm với lịch sử dài/nhiều thuốc.
- `figma-BmiActivity.png` trống: loại khỏi chấm màn BMI; không kết luận màn BMI trong sản phẩm bị trắng. `figma-login-keyboard.png` không có IME: loại khỏi kiểm chứng bàn phím. Ảnh splash có toast từ luồng trước: không quy toast đó thành lỗi splash.
- Có ảnh mặc định/trống của nhật ký sức khỏe, xu hướng, nhật ký nước; lỗi đăng nhập và cấu hình giờ nước; dialog xóa/đăng xuất/cữ thuốc. Không có đủ ảnh loading, lưu thất bại, mọi success hoặc bản ghi cảnh báo sức khỏe.
- Lượt emulator cuối bị system ANR/DeadSystemException. Không chạy thêm ứng dụng trong audit này để tránh coi ảnh hệ thống lỗi là UI sản phẩm.

### Cách phân loại

- **Đã xác minh:** ảnh cho thấy vấn đề và mã liên quan xác nhận được khi cần.
- **Nghi ngờ/chưa xác minh:** cần tương tác, cây accessibility hoặc kích thước khác.
- **Đề xuất:** quyết định thiết kế có thể cải thiện trải nghiệm, không gọi là bug.

## 2. Bảng điểm

| Tiêu chí | Điểm | Bằng chứng và giới hạn |
|---|---:|---|
| Visual hierarchy, khả năng đọc | 11/15 | Giá trị sức khỏe và CTA chính nổi bật; thời điểm, đơn vị, hướng dẫn và nhãn biểu đồ nhỏ/nhạt. Home ưu tiên thuốc và nước nhưng thông tin mới nhất nằm thấp. |
| Layout, spacing, alignment | 10/15 | Gutter24dp, card padding20dp tương đối đều; status huyết áp nằm ngoài card số đo, các form và phần nước chưa cùng nhịp. Nhiều khoảng trống cố định; chưa có ảnh nhỏ/font lớn hợp lệ. |
| Màu sắc, typography, nhất quán | 10/15 | Teal/mint làm chủ đạo, xanh dương dành cho nước; không lạm dụng gradient. Chữ phụ trên nền app/nước thiếu tương phản; form thuốc và form hồ sơ khác hệ ô nhập; phần trăm nước khác nhau. |
| Hữu dụng, luồng thao tác | 14/20 | Ghi số đo, thêm nước, lịch thuốc và sửa hồ sơ có đường vào rõ; dialog có hủy/xác nhận; nhật ký huyết áp thiếu đơn vị. Chưa đo được thời gian hoàn thành tác vụ, lỗi bấm hoặc hiệu quả với nhiều dữ liệu. |
| Android conventions, accessibility | 8/15 | Có native picker, nút chính tối thiểu52dp, nút avatar48dp, nhãn tab; tab Nhắc nhở40dp. TalkBack, Switch Access, keyboard, font lớn chưa kiểm chứng; không cho điểm tối đa bằng đọc XML. |
| Nhận diện riêng, tránh đại trà | 6/10 | Giọng văn Việt, icon lá, màu nước và khái niệm từng cữ thuốc tạo nền nhận diện. Card/pill/helper box lặp nhiều, lời chào chung và hero leaf gần phong cách wellness template. Đây là nhận định thiết kế, không phải chứng cứ dùng AI. |
| Loading, empty, error, success | 7/10 | Empty có hành động; login/water error có chữ; dialog xóa có ngữ cảnh. Chưa kiểm chứng đầy đủ loading/lỗi lưu/quyền bị từ chối/success và cách đọc thông báo bằng trợ năng. |
| **Tổng** | **66/100** | **Tin cậy trung bình với UI ở412dp; thấp với tương tác thực tế và accessibility.** |

## 3. Top5 cần ưu tiên

### 1 — P1: Chữ phụ thiếu tương phản trên các nền đang dùng [đã xác minh]

**Bằng chứng:** `colors.xml` khai báo chữ phụ `#627979`. Ảnh Home, Nhắc nhở, form nước và xu hướng có chữ phụ trên `#F4F8F7` hoặc `#EAF3FD`. Tính relative luminance từ token: trắng4,64:1; nền app4,33:1; nền nước4,14:1. Các nhãn này là chữ nhỏ, không thuộc nhóm chữ lớn. WCAG dùng mốc4,5:1 cho chữ thường; xem [W3C](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum).

**Ảnh hưởng:** thời điểm đo, hướng dẫn và lượng nước còn lại khó đọc hơn phần số lớn, đặc biệt với người thị lực yếu.

**Sửa nhỏ nhất:** xác định token chữ phụ theo nền hoặc chọn màu đủ tối cho tất cả nền đang dùng; kiểm tra từng cặp nền thực tế. Đưa thời điểm, đơn vị và hướng dẫn quan trọng lên cỡ14–16sp như một lựa chọn sản phẩm, không gọi12sp tự động vi phạm chuẩn.

**Nếu bỏ qua:** app vẫn có số lớn đẹp nhưng phần giúp hiểu và thao tác đúng lại khó đọc.

### 2 — P1: Tab Nhắc nhở cao40dp [đã xác minh]

**Bằng chứng:** `fragment_reminder.xml:17–18` đặt hai MaterialButton cao40dp; ảnh `figma-reminders.png` và `figma-medicine-list.png` cho thấy bộ chọn thấp. Không tìm thấy TouchDelegate mở vùng chạm cho chúng trong mã ứng dụng. Android khuyến nghị vùng chạm tối thiểu48×48dp; xem [Android Developers](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views).

**Ảnh hưởng:** hai mục dùng thường xuyên khó chạm hơn CTA lớn xung quanh.

**Sửa nhỏ nhất:** tăng vùng tương tác lên tối thiểu48dp, cho phép chiều cao tăng khi chữ lớn; có thể giữ hình pill nhỏ bên trong nếu cần. Xác minh hit bounds bằng Accessibility Scanner.

**Nếu bỏ qua:** thao tác chuyển giữa cữ hôm nay và danh sách thuốc không nhất quán về độ dễ bấm.

**Không đánh đồng:** chip giờ thuốc đã gọi `setEnsureMinTouchTargetSize(true)`. Bảy nút ngày ở412dp rộng xấp xỉ48,6dp theo layout hiện tại, chưa kết luận chúng vi phạm tại kích thước đã chụp. Khi360dp, tính toán cho thấy nguy cơ hẹp hơn48dp, nhưng cần ảnh/runtime để xác minh.

### 3 — P1: Nhật ký sức khỏe bỏ đơn vị huyết áp [đã xác minh]

**Bằng chứng:** `figma-health.png` hiển thị `118 / 78` trong khi các số khác có kg/bpm/mg/dL. `HealthRecordAdapter.java:101` format `%d / %d`; nhánh số đo một phần cũng không bổ sung đơn vị. Chi tiết có mmHg, nhưng người dùng phải mở thêm màn mới đọc đủ ngữ cảnh.

**Ảnh hưởng:** cách biểu diễn số đo không thống nhất và kém rõ với người mới.

**Sửa nhỏ nhất:** hiển thị mmHg sát số, tách tên chỉ số/giá trị/đơn vị rõ; giữ đơn vị khi chỉ có một giá trị, không thay đổi cách đánh giá huyết áp.

**Nếu bỏ qua:** bản ghi cần được hiểu nhờ kiến thức người dùng thay vì tự giải thích trên UI.

### 4 — P2: Cùng lượng nước nhưng Home63%, Nhắc nhở62% [đã xác minh]

**Bằng chứng:** ảnh Home và Nhắc nhở cùng1250/2000ml; `HomeFragment.java:141` dùng Math.round, `ReminderFragment.java:237` chia số nguyên. Cách gọi nhanh cũng khác: `+250ml` và `Nạp250ml`; cách card, chữ và hành động phụ khác nhau.

**Ảnh hưởng:** dữ liệu trông thiếu nhất quán khi đổi tab; giảm niềm tin dù tổng ml giống nhau.

**Sửa nhỏ nhất:** dùng cùng quy tắc làm tròn hiển thị và một quy ước nhãn hành động, không đổi mục tiêu/lịch/DAO. Thêm một kiểm tra1250/2000 và vượt mục tiêu; cân nhắc tái sử dụng layout hiện có khi hợp lý, không tạo framework component mới.

**Nếu bỏ qua:** người dùng phải tự suy luận tại sao hai màn không khớp.

### 5 — P2: Form không cùng hệ thành phần [đã xác minh]

**Bằng chứng:** ảnh AddMedicine có ô outline và floating label nhỏ; Login/EditProfile/AddHealth dùng ô trắng gần như không viền; WaterAdd chứa helper trong khung; DeleteProfile có password field nhìn giống dòng chữ hơn ô nhập. XML AddMedicine chọn trực tiếp OutlinedBox, các form khác dùng style riêng.

**Ảnh hưởng:** cùng thao tác nhập dữ liệu nhưng dấu hiệu có thể nhập, trạng thái và nhịp form khác nhau.

**Sửa nhỏ nhất:** thống nhất các style XML sẵn có cho label/value/helper/error/focus và kích thước, giữ inputType, validation, password toggle và picker hiện có. Không chỉ thêm viền toàn app theo cảm tính.

**Nếu bỏ qua:** UI vẫn tạo cảm giác các module ghép từ nhiều mẫu khác nhau.

## 4. Vì sao có cảm giác template

Đây là đánh giá thị giác, không xác định ai/công cụ nào tạo giao diện.

- Card bo20dp được dùng cho cả bản ghi, con số tóm tắt và các đoạn trợ giúp; pill mint xuất hiện ở nhiều cấp hành động. Khi cùng hình dáng phục vụ nhiều vai trò, thứ bậc bị phẳng.
- Home có lời chào, slogan, CTA, hai card lớn; khá giống dashboard wellness phổ biến. Khả năng hỗ trợ quyết định chưa được tận dụng bằng ngữ cảnh cụ thể như cữ chưa ghi nhận, mà không cần thêm chức năng.
- Icon lá và câu “một chút quan tâm” phù hợp sức khỏe nhưng chưa đủ riêng để nhận diện ViTa nếu bỏ tên app.
- Nhiều khoảng trắng và chữ phụ nhạt làm bộ ảnh mẫu ít dữ liệu trông thoáng; chưa chứng minh được khi nội dung dài.
- **Không thấy bằng chứng lạm dụng gradient/bóng đổ lớn.** Màu nhìn chung có vai trò, không nên thay bằng palette mới chỉ để trông khác. Native icon và icon riêng cần kiểm stroke trước khi kết luận trộn bộ gây lỗi.

Hướng cải thiện đề xuất: giữ teal/mint và xanh nước, dùng card cho nhóm dữ liệu cần phân biệt; dùng heading/divider cho thông tin nhẹ. Giọng văn Việt nên cụ thể, ngắn và giúp hành động. Ưu tiên tính hữu ích của cữ thuốc/ngày đo hơn trang trí mới. Đây là hướng cần người dùng xác nhận, không ghi đè yêu cầu bám Figma cũ.

## 5. Đề xuất theo màn hình/component

| Màn có ảnh | Nhận xét có bằng chứng | Sửa đề xuất sau phê duyệt |
|---|---|---|
| Splash | Icon lá/tên/slogan rõ; helper box thêm một tầng trang trí. Toast trong ảnh thuộc fixture trước, không coi là bug. | Giữ nhận diện; cân nhắc bỏ khung trợ giúp nếu không giúp quyết định; kiểm thời gian vào app riêng. |
| Login | CTA rõ, error có chữ; lời chào dài, header và nội dung cùng nói chào; input/password dialog khác nhau. | Một tiêu đề chính; đồng bộ input; lỗi không chỉ dựa màu, giữ dữ liệu đã nhập. Keyboard/focus cần test trước sửa. |
| Register1/2 | Có tiến trình2bước, giữ đơn vị chiều cao/cân nặng; helper khá dài. | Nhất quán helper và nhãn bắt buộc/tùy chọn; test bàn phím số, Next/Done và Back không mất nháp. |
| Home | CTA ghi chỉ số rõ; thuốc và nước có độ ưu tiên; dữ liệu mới nhất thấp dưới vùng đầu. | Ưu tiên nội dung theo tác vụ hiện có, thu gọn trang trí; đồng bộ phần trăm/nhãn nước. Không gọi nội dung cuộn ra ngoài viewport là bị mất nếu chưa thử cuộn. |
| Nhật ký sức khỏe | Đọc được ngày/giờ/giá trị; huyết áp thiếu đơn vị, các chỉ số phụ không có tên rõ trong card. | Bổ sung mmHg và nhãn ngắn; giữ nhấn toàn card để mở; test ghi chú dài/số đo một phần. |
| Chi tiết lần đo | Đơn vị có; badge đánh giá BP nằm ngoài card, tách khỏi giá trị. | Đưa badge vào nhóm huyết áp; nhất quán vị trí thời điểm/đơn vị/so sánh giữa card. |
| Nhắc nhở hôm nay | Giờ lớn, có “chưa ghi nhận/sắp tới”; phần nước khác Home. | Vùng chạm tab48dp; đồng bộ nước; cân nhắc chỉ dẫn thao tác chạm cữ khi người dùng chưa quen. |
| Danh sách thuốc/lịch sử | CTA thêm/sửa/xóa rõ; thẻ thuốc chỉ có một mẫu. | Kiểm tên thuốc/liều dài, nhiều giờ, nhiều thuốc; giảm box trợ giúp lặp; không gộp trạng thái lịch và trạng thái từng cữ. |
| Form thuốc | Hướng dẫn sửa/xóa chip có; input khác hệ form; ngày hiển thị bằng chữ. | Đồng bộ input; giữ48dp cho chip; thử layout ngày ở màn hẹp và cỡ chữ lớn, wrap nếu cần; không thay nghiệp vụ7ngày. |
| Nhật ký nước | Tổng/ngày/giờ và thao tác nhanh dễ hiểu; empty phân biệt0lần/0ml. | Đồng bộ trình bày với Home, thêm dấu cách quanh ngày; kiểm hoàn tác ở success thực tế. |
| Thêm/sửa nước | Có lượng, giờ, ngữ cảnh giữ nguyên ngày; hero thêm nước chiếm diện tích. | Đơn vị sát giá trị; helper nhất quán; cân nhắc giảm hero; xác minh bàn phím và nút lưu không bị che. |
| Cài đặt nước | Hai chế độ và lỗi thiếu giờ đã có ảnh; nhãn chọn chế độ dài, sát nhau; helper nhiều đoạn. | Gọi ngắn “Mỗi2giờ”/“Giờ tùy chọn”, mô tả dưới; vùng chọn đủ lớn; đặt lỗi cạnh nhóm giờ; không đổi cách đặt lịch. |
| Xu hướng/dữ liệu | Empty CTA tốt, một lần đo được giải thích; trục chữ10sp trong code, đơn vị ở xa số; danh sách30ngày lặp nhiều card “chưa ghi nhận”. | Chữ biểu đồ dễ đọc, legend BP có ký hiệu ngoài màu; đơn vị gần số; cân nhắc hàng gọn cho ngày không có bản ghi. Giữ phân biệt “chưa ghi nhận” với0. |
| Hồ sơ/chỉnh sửa | Dữ liệu nhóm rõ, hành động nguy hiểm có màu/chữ; nhiều nút mint cùng cấp. | Nhóm thông tin, cài đặt và tài khoản bằng khoảng cách/heading; giảm độ nhấn logout, giữ delete tách biệt; test tên/tài khoản dài. |
| Dialog chung | Hành động cụ thể, hủy riêng, nội dung xóa có ngữ cảnh. | Đồng bộ input xác nhận mật khẩu; test focus, đọc thông báo, cuộn nội dung và nút với font lớn; không đổi callback xóa. |
| BMI | Ảnh trống, không có bằng chứng hợp lệ. | Chụp lại trước khi đưa nhận xét hoặc sửa màn này. |

## 6. Các vấn đề nghi ngờ và giới hạn cần thử

- Bàn phím che form/nút, Next/Done, focus vào lỗi đầu: chưa có ảnh IME hợp lệ.
- Màn nhỏ360dp, font130–200%, landscape: chưa có kết quả đáng tin. Phép tính chiều rộng nút ngày chỉ cho thấy nguy cơ, chưa chứng minh bấm nhầm/vỡ layout.
- TalkBack: thứ tự focus, role của TextView clickable, selected day, nội dung biểu đồ và lỗi được đọc: chưa chạy. Không suy ra “hoàn toàn không dùng được” từ thiếu contentDescription ở chart; có đường xem dữ liệu dạng chữ.
- Loading, lỗi lưu SQLite, từ chối quyền notification/exact alarm, success/undo, nhắc bị tắt: chưa có đủ ảnh để chấm từng màn.
- Nhiều thuốc, ngày qua đêm,100+ bản ghi, tên dài, ghi chú dài, dấu thập phân locale: chưa có bộ ảnh đủ.
- Tài liệu `design-system/vita/MASTER.md` mô tả palette/phạm vi cũ (có thời tiết, gia đình); khác tài nguyên đang chạy. Đây là lệch tài liệu đã xác minh, không dùng nó để tự ý phục hồi chức năng đã xóa. Sau phê duyệt, cập nhật tài liệu theo phạm vi hiện tại.

## 7. Checklist nghiệm thu sau khi được phép sửa

- [ ] Xác nhận phạm vi các đề xuất ảnh hưởng bố cục so với Figma hiện hành.
- [ ] Chụp lại từng màn với build/source xác định; fixture riêng, không sửa dữ liệu người dùng.
- [ ] Kiểm contrast trên mọi nền thực tế; nhãn/hướng dẫn thường đạt4,5:1; phân biệt yêu cầu chữ và icon/viền.
- [ ] Đo hit bounds48×48dp cho tab, icon, chip, ngày; không chỉ đo phần tô màu.
- [ ] Ngày/giờ, tên chỉ số, giá trị và đơn vị rõ; dữ liệu thiếu không biến thành0.
- [ ]1250/2000ml hiển thị cùng tỷ lệ trên mọi màn; kiểm0/đạt/vượt mục tiêu và locale.
- [ ] Input có default/focus/filled/error/disabled khi áp dụng; giữ nhãn khi nhập và giữ dữ liệu khi lỗi.
- [ ] Kiểm trạng thái trống/1bản ghi/nhiều bản ghi/tên dài/ghi chú dài/quyền bị từ chối.
- [ ]360dp/412dp, landscape, font100/130/200%; chụp bàn phím đang mở, cuộn tới nút cuối.
- [ ] TalkBack đọc đúng giá trị+đơn vị+ngày, role, trạng thái selected; lỗi được thông báo; có cách đọc biểu đồ bằng chữ.
- [ ] Kiểm Back, hủy dialog, bỏ nháp, xoay màn và lưu; không mất dữ liệu, không làm thay đổi lịch nhắc ngoài ý muốn.
- [ ] Kiểm success, lỗi lưu, hoàn tác và thao tác lặp; phản hồi không chỉ bằng màu/toast thoáng qua.
- [ ] Chạy unit/device cần thiết và lint; phân biệt system ANR với lỗi app; không ghi pass khi test bị gián đoạn.
- [ ] Rà soát component dùng lại; không thêm dependency/framework hay thay MVC/DB.

**Quyết định:** ưu tiên sửa1–3, sau đó4–5; chưa kết luận UI đạt chuẩn. Chờ người dùng xác nhận báo cáo trước khi sửa code.

**Chưa kiểm tra:** giao diện chạy mới trong phiên audit, BMI hợp lệ, TalkBack, bàn phím và font lớn; điểm66/100 chỉ áp dụng cho bằng chứng đã nêu, không phải xác nhận accessibility toàn app.
