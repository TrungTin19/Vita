# ViTa — đối chiếu và triển khai file Figma mới

> **For agentic workers:** Dùng executing-plans để thực hiện lần lượt, giữ nguyên nghiệp vụ. Không commit/push trong yêu cầu này.

**Goal:** Khớp sát file `zHHRPnM4rgs3zyr8Oz7v6x`, xác minh bằng ảnh chạy thật và báo rõ phần chưa đạt.
**Architecture:** Java/XML và Material hiện có. Tái sử dụng palette, drawable và form đã có; chỉ hợp nhất vỏ dialog lặp lại. Không sửa DAO, schema, receiver, quyền hay xác thực.
**Tech Stack:** Java 17, Android min24/target34, Material 1.14, MPAndroidChart, SQLite.
**Spec:** Yêu cầu người dùng ngày 21/10; Figma mới đọc trực tiếp qua trình duyệt. MCP báo hết quota Starter, dùng quyền ngoại lệ trình duyệt người dùng đã đồng ý.

## Màn hình và điều hướng

10 Activity, 4 tab Fragment; các màn nhật ký/form nước, lịch sử thuốc và bảng dữ liệu là Dialog/DialogFragment đầy đủ. Splash → đăng nhập hoặc Main. Đăng nhập → đăng ký hai bước → Main. Main có Hôm nay/Chỉ số/Nhắc nhở/Xu hướng; avatar → Hồ sơ → BMI/sửa/cài nước. Nhật ký → thêm/chi tiết → sửa/xóa. Nhắc nhở → danh sách thuốc → lịch sử/sửa/xóa. Nước → nhật ký → thêm/sửa/xóa. Xu hướng → chỉ số/khoảng ngày → dữ liệu → chi tiết bản ghi.

## Bảng đối chiếu file mới

Đã đọc trực quan toàn bộ 21 frame chính dưới đây trong file mới; node không dùng lại từ file cũ. Khung 412×892; hệ thống Android thật được loại riêng khi so sánh. Trạng thái “cần ảnh” chưa phải kết luận đạt.

| Frame | Node mới | Android | Sai lệch/cần kiểm tra |
|---|---|---|---|
| A01 | 1:60 | SplashActivity | Cần ảnh logo, khoảng dọc |
| A02 | 1:82 | LoginActivity | Nút tạo tài khoản và hộp trợ giúp chưa bo góc; cần ảnh input/lỗi |
| A03 | 1:105 | RegisterActivity bước 1 | Cần ảnh form, keyboard, chữ lớn |
| A04 | 1:128 | RegisterActivity bước 2 | Cần ảnh gender, hai ô ngang, lỗi |
| H01 | 1:153 | HomeFragment/MainActivity | Cần ảnh đo card/nav, dữ liệu thật |
| M01 | 1:228 | HealthFragment | Cần ảnh trống/lọc và danh sách |
| M02 | 1:281 | AddHealthActivity | Cần ảnh các nhóm, đơn vị, lỗi trường ẩn |
| M03 | 1:323 | DetailHealthActivity | Đơn vị đang ẩn/thiếu ở cạnh giá trị; dialog xóa native |
| R01 | 1:369 | ReminderFragment hôm nay | Dialog cữ còn danh sách native |
| R02 | 1:448 | ReminderFragment danh sách | Dialog xóa native; kiểm tra pill bật/tắt |
| R03 | 1:499 | MedicineDoseDialogs.history | Thiếu nút xóa cuối trang; cần ảnh lịch sử |
| R04 | 1:532 | AddMedicineActivity | Cần ảnh chip giờ, ngày, keyboard |
| W01 | 1:579 | WaterJournalDialog.journal | Cần ảnh mục tiêu/nhật ký/hoàn tác |
| W02 | 1:642 | WaterJournalDialog.entry thêm | Cần ảnh form, keyboard |
| W03 | 1:664 | WaterJournalDialog.entry sửa | Dialog đã đổi từ vòng trước, cần ảnh mới |
| W04 | 1:685 | EditProfileActivity water-only | Cần ảnh hai kiểu nhắc, qua đêm, lỗi |
| T01 | 1:716 | StatsFragment | Cần ảnh biểu đồ, một điểm/trống, 7/30 ngày |
| T02 | 1:784 | dialog_stats_data | Cần ảnh danh sách và đúng bản ghi khi chạm |
| P01 | 1:807 | ProfileActivity | Cần ảnh thông tin, action, nội dung dài |
| P02 | 1:848 | BmiActivity dùng layout_profile | Cần ảnh chuẩn/BMI không đủ tuổi |
| P03 | 1:882 | EditProfileActivity | Cần ảnh form/keyboard |

## Thành phần và trạng thái đã đọc thêm

- Button 1:40: primary/secondary/danger/disabled, góc bo và hai màu chữ/nền theo component. Input 1:49: empty/filled/error, nhãn nằm trong ô, viền mảnh.
- Dialog xóa thuốc/chỉ số: rộng364, padding20, gap12, radius24, nút xếp dọc. Nội dung tên thuốc, ngày/giờ lấy dữ liệu thật.
- DDraft: tiếp tục chỉnh sửa / bỏ thay đổi; giữ đúng màn gọi khi Back.
- DDose: đã uống / hủy / bỏ qua. DDoseEdit: đổi trạng thái / hủy / bỏ xác nhận. Giữ đủ trạng thái nghiệp vụ cũ dù prototype chỉ mô phỏng một trường hợp.
- T01NoData/T01One: lời nhắc ghi lần đầu hoặc một điểm và lời giải thích, không dựng đường xu hướng giả.
- Các biến thể còn lại đã thấy tên trong Layers: dữ liệu thuốc trống, tắt nhắc, quyền nhắc, nhập lỗi, số đo một phần/ghi chú, nước ngày trước/trống/vượt mục tiêu, WHO, 30 ngày và các chỉ số. Chưa coi là đã xác minh trực quan từng frame.

## Thực hiện

- [x] Đọc cấu trúc, manifest, layout, các caller điều hướng; không có AGENTS.md trong nguồn dự án.
- [x] Đọc 21 frame chính, component và các dialog ưu tiên; lập bảng trên.
- [x] Hợp nhất vỏ dialog hiện có cho hồ sơ/nước/xóa thuốc/xóa chỉ số/nháp/cữ thuốc; giữ callback/validation/ownership.
- [x] Bổ sung trình bày đơn vị cạnh giá trị và nút xóa trong R03 qua cùng luồng xóa cũ.
- [x] Sửa bo góc, giãn dòng tương thích Android24 và nút co giãn khi font lớn.
- [ ] Build verification; chụp ảnh với viewport phù hợp, đối chiếu từng nhóm và sửa sai lệch.
- [ ] Chạy đầy đủ unit/device/lint; kiểm tra trống/lỗi/keyboard/chữ lớn; cập nhật báo cáo giới hạn.

### Tiến độ kiểm chứng

- 70 unit tests qua. 12 bài dialog/nước qua trên đúng package verification; đã xác minh applicationId trước khi cài.
- Xu hướng trống được kiểm thử RED trước khi sửa, GREEN sau khi thêm trạng thái và đường mở form cân nặng.
- Đã chụp26 màn rồi mở rộng ảnh trạng thái/lịch sử/bàn phím. H01 có PNG gốc xác minh đúng frame từ file mới; đã so trực tiếp cùng chiều rộng.
- Các lượt build kiểm thử dùng `--no-configuration-cache` vì đã phát hiện cấu hình/đường dẫn APK được tái sử dụng sai khi chuyển gói chính sang verification.
- Báo cáo chi tiết và giới hạn: `docs/reviews/2026-10-21-new-figma-implementation.md`.

## Rủi ro cần kiểm tra

- Đổi lớp chứa TextView có thể làm hỏng logic ẩn card đang dựa parent; chuyển sang ID card rõ ràng khi thay hàng giá trị.
- Dialog không được trùng ID nút với panel AlertDialog native; phải kiểm tra hủy/xác nhận/password sai.
- Không chuyển thao tác xóa trực tiếp sang xóa thật để thử dữ liệu người dùng; mọi fixture nằm trong applicationId `.verification`.
- Máy 8GB đã nhiều lần System UI ANR khi chạy Figma cùng emulator; đóng tab do agent mở trước khi chạy Android, không đóng ứng dụng người dùng.
- Không giảm assertion hoặc báo “100%” khi ảnh bị ANR/không có focus.

### Kết quả lượt cuối

- Build APK chính và lint thành công; mã gói xác minh đúng,70 unit tests qua. Lint0 errors/198 warnings.
- 41 ảnh ở viewport chuẩn đã chụp trong vòng trước; có đối chiếu Home với PNG gốc Figma. Những ảnh nhỏ/font lớn kéo sau lỗi không được coi là ảnh mới.
- Device lượt đầu72 bài/1 thất bại nhãn cũ đã sửa; lượt sau so sánh qua nhưng tiến trình Android crash giữa suite. Màn nhỏ bị system ANR, chưa xác minh bàn phím/font130%. Cấu hình emulator đã trả về1080×2400/density420/font1.0.
- Chi tiết log, giới hạn và APK trong báo cáo triển khai.
