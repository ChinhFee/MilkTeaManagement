# LỘ TRÌNH PHÁT TRIỂN DỰ ÁN (ROADMAP)

Tài liệu này dùng để theo dõi tiến độ và phân chia công việc giữa **Huân** và **Chính**.

---

## 📌 QUY TẮC CHUNG
- Mỗi khi bắt đầu một mục, hãy tạo nhánh (branch) đúng tên quy định.
- Làm xong phần nào thì tích `[x]` vào ô trống tương ứng.

---

## 🟦 GIAI ĐOẠN 1: GIAO DIỆN CƠ BẢN & ĐĂNG NHẬP (HOÀN THÀNH)
**Mục tiêu:** Giúp người dùng vào được App và xác định quyền (Admin/Khách).

### 👨‍💻 CHÍNH: Phần Khách Hàng (Customer UI)
- [x] **Nhánh:** `feature/auth-ui`
    - Thiết kế màn hình Đăng nhập (Login) & Đăng ký (Register).
    - Thiết kế màn hình Thông tin cá nhân (Profile).
- [x] **Nhánh:** `feature/home-menu`
    - Thiết kế màn hình chính hiển thị danh sách trà sữa (RecyclerView).
    - Tích hợp `MenuRepository` để lấy dữ liệu.

### 👨‍💻 HUÂN: Phần Quản Trị & Logic (Admin & Logic)
- [x] **Nhánh:** `feature/auth-logic`
    - Xử lý Đăng ký/Đăng nhập bằng Firebase Auth.
    - Lưu thông tin User vào Firestore (sử dụng `ProfileRepository`).
- [x] **Nhánh:** `feature/admin-dashboard`
    - Thiết kế màn hình chính của Admin (Dashboard).
    - Tạo các nút điều hướng đến quản lý món ăn, đơn hàng.

---

## 🟨 GIAI ĐOẠN 2: GIỎ HÀNG & ĐẶT HÀNG
**Mục tiêu:** Khách có thể chọn món, thêm topping và đặt hàng.

### 👨‍💻 CHÍNH: Quy trình mua hàng
- [x] **Nhánh:** `feature/product-detail`
    - Màn hình chi tiết món: Chọn Size (M/L), chọn Topping (Checkbox).
    - Nút "Thêm vào giỏ hàng".
- [x] **Nhánh:** `feature/cart-screen`
    - Thiết kế màn hình Giỏ hàng.
    - Hiển thị danh sách món đã chọn, tính tổng tiền (sử dụng `CartManager`).

### 👨‍💻 HUÂN: Xử lý Đơn hàng
- [ ] **Nhánh:** `feature/checkout-logic`
    - Viết logic cho nút "Đặt hàng" (Checkout).
    - Đẩy dữ liệu lên Firebase `Orders` (sử dụng `OrderRepository`).
- [ ] **Nhánh:** `feature/order-history`
    - Màn hình lịch sử đơn hàng cho Khách hàng xem lại các đơn đã đặt.

---

## 🟥 GIAI ĐOẠN 3: QUẢN LÝ (ADMIN) & THỐNG KÊ
**Mục tiêu:** Admin có thể vận hành quán trà sữa.

### 👨‍💻 CHÍNH: Quản lý Menu (Admin UI)
- [ ] **Nhánh:** `feature/manage-menu-ui`
    - Màn hình danh sách món ăn/topping dành cho Admin.
    - Dialog/Màn hình Thêm món mới, Sửa giá, Xóa món.

### 👨‍💻 HUÂN: Vận hành & Doanh thu
- [ ] **Nhánh:** `feature/manage-orders`
    - Màn hình danh sách đơn hàng mới (Pending).
    - Chức năng cập nhật trạng thái đơn: "Đang pha chế" -> "Đang giao" -> "Hoàn thành".
- [ ] **Nhánh:** `feature/analytics`
    - Màn hình báo cáo doanh thu (Biểu đồ hoặc danh sách tổng hợp).
    - Tích hợp `AdminAnalyticsRepository`.

---

## 🟩 GIAI ĐOẠN 4: TỐI ƯU & KIỂM THỬ
- [ ] **Nhánh:** `feature/final-polish`
    - Cả hai cùng kiểm tra lỗi (Bug).
    - Tối ưu tốc độ load ảnh từ Google Drive.
    - Chỉnh sửa màu sắc, icon cho đẹp mắt.

---

## 🛠 HƯỚNG DẪN RẼ NHÁNH NHANH (CHO CHÍNH & HUÂN)
1. Mở Terminal trong Android Studio.
2. Cập nhật code mới nhất: `git pull origin main`.
3. Tạo nhánh làm việc (VD Huân làm phần Auth): `git checkout -b feature/auth-logic`.
4. Sau khi làm xong:
   ```bash
   git add .
   git commit -m "Huân: Hoàn thành logic đăng nhập"
   git push origin feature/auth-logic
   ```
5. Lên GitHub nhấn "Compare & pull request" để người kia vào kiểm tra code.
