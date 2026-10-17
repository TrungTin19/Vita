# Cải thiện BMI, thời tiết và uống nước — 17/10/2026

## Phạm vi đã thống nhất

- Mục 5: sửa phân loại WHO từ BMI 40 lên độ III; giữ số BMI nhưng không áp dụng nhãn người lớn hoặc gợi ý cân nặng cho hồ sơ ngoài phạm vi tuổi. Giữ các ngưỡng Châu Á hiện có.
- Mục 10, chỉ P1: thiếu dữ liệu phải có thông báo trung tính; mô tả nóng độc lập với tùy chọn tăng mục tiêu nước; hiện đồng thời cảnh báo nóng và AQI; xóa lời khuyên thành phố cũ ngay khi bắt đầu tải.
- Mục 7: hoàn tác đúng bản ghi vừa uống; giải thích gợi ý theo thời tiết là quy tắc cố định. Hai cách nhắc trong khung giờ: mỗi 2 giờ từ giờ bắt đầu, hoặc nhiều mốc tùy chọn. Cho phép mốc tại giờ kết thúc và khung qua nửa đêm; giữ lựa chọn khi tạo lại màn hình.

## Thực hiện

Dùng Android Java/XML và Material sẵn có, không thêm thư viện. Dùng lại MedicineSchedule để chuẩn hóa giờ và tìm báo thức kế tiếp. Schema 5 thêm water_times với mặc định rỗng cho chế độ 2 giờ; nâng cấp bằng ALTER TABLE, giữ dữ liệu cũ. Các cột nước tiếp tục được lưu riêng với thông tin cá nhân.

Snackbar Hoàn tác giữ id bản ghi và id chủ sở hữu, kiểm tra phiên trước khi xóa, rồi cập nhật tổng nước. Chế độ giờ riêng dùng chip có thể thêm, sửa, xóa và loại trùng; không cho lưu danh sách rỗng hoặc mốc ngoài khung.

BMI dùng phạm vi từ 20 tuổi theo nguồn CDC, tuổi ước tính theo năm sinh và được giải thích trên cả hai màn. Không xây mô hình BMI trẻ em. Không mở rộng P2 về thời gian quan sát API hoặc hạn tuổi cache thời tiết.

Giữ nguyên thay đổi hồ sơ của đợt trước. Không commit/push trong đợt này.

## Bằng chứng TDD và review

- BMI: 11 test, ban đầu 3 thất bại đúng lỗi, sau sửa cả 11 đạt. Review độc lập không thấy lỗi cần sửa.
- Lịch nước: test bắt được lịch 07:20 bị đẩy sang 09:20 thay vì mốc 09:00, và mốc 22:00 bị loại. API giờ riêng và xóa theo chủ sở hữu ban đầu chưa có. Sau sửa, toàn bộ 86 unit test đạt.
- Thiết bị: cả 5 test nước đạt sau RED/GREEN, gồm migration 4→5, giới hạn giờ và người dùng, nhiều mốc/loại trùng/tạo lại màn hình/đổi chế độ, xóa đúng log và Snackbar Hoàn tác. Review độc lập không thấy lỗi cần sửa.
- Thời tiết: đã tái hiện cả 4 lỗi trên app cũ. Test chuyển thành phố được sửa để kiểm tra ngay trong cùng lượt UI, tránh callback lỗi chạy trước assertion. Sau đó thấy RED với OLD_CITY_ADVICE. Review mã sau sửa không thấy lỗi cần sửa.
- Bản tích hợp: 86 unit test đạt; assembleDebug, assembleDebugAndroidTest và lintDebug thành công, 0 lỗi và 73 cảnh báo toàn dự án.
- Lượt tổng thể 45 bước: 44 đạt, 1 lỗi Espresso mất tiêu điểm ở bài thêm mốc giờ. Log ghi nhận Android chuyển màn chậm và pause timeout; chạy riêng lại đúng bài, giữ nguyên mã, đạt OK (1 test). Cả 44 test chức năng có kết quả đạt. Bước ảnh QA đạt; đã xem ảnh chữ 130%, hai chế độ, chip, mô tả và nút lưu đọc rõ. Mã chụp ảnh tạm đã gỡ khỏi source; ảnh/log trong .audit-cache được gitignore.

## Môi trường kiểm thử

Chỉ dùng gói .verification trên emulator-5554 và các fixture riêng. Không xóa dữ liệu gói app thường. Đã dừng tiến trình app thường ở nền sau khi log ghi nhận timeout chuyển màn, giữ nguyên dữ liệu.

Đã khôi phục WiFi=1, mobile_data=1, font_scale=1.0 và đọc lại xác nhận. Đã gỡ thành công hai gói .verification.test và .verification; giữ dữ liệu app thường.

## Nguồn đã kiểm tra

- https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html — phạm vi người lớn 20+, BMI 35–<40 độ II, từ 40 độ III.
- https://open-meteo.com/en/docs/air-quality-api — nguồn dữ liệu mô hình và các giới hạn; thay đổi P2 về timestamp/cache không thuộc đợt này.


