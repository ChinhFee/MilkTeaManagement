# HƯỚNG DẪN SỬ DỤNG GIT CHI TIẾT (CHO NHÓM)

Tài liệu này tổng hợp toàn bộ các lệnh Git cần thiết để hai bạn (Huân & Chính) phối hợp làm việc mà không lo mất code.

---

## 1. Cấu trúc nhánh (Branching Strategy)
- **`main`**: Nhánh chính, chứa code đã chạy ổn định. **Không bao giờ** code trực tiếp trên này.
- **`feature/ten-tinh-nang`**: Nhánh để phát triển tính năng mới.
- **`bugfix/ten-loi`**: Nhánh để sửa lỗi.

---

## 2. Thiết lập ban đầu (Chỉ làm 1 lần)
Nếu bạn là người tạo Repository đầu tiên:
```bash
git init
git remote add origin [LINK_GITHUB_CUA_BAN]
git add .
git commit -m "Initial commit: Hoàn thiện Backend và Data Layer"
git push -u origin main
```

Nếu bạn là người nhận dự án (Clone về máy):
```bash
git clone [LINK_GITHUB_CUA_BAN]
```

---

## 3. Quy trình làm việc hàng ngày (Quan trọng)

### Bước 1: Trước khi bắt đầu làm việc
Phải cập nhật code mới nhất từ bạn mình về máy mình:
```bash
git checkout main
git pull origin main
```

### Bước 2: Tạo nhánh mới để làm tính năng
```bash
git checkout -b feature/ten-cua-ban
# Ví dụ: git checkout -b feature/login-screen
```

### Bước 3: Lưu lại code khi đang làm (Commit)
Nên commit sau mỗi khi hoàn thành một phần nhỏ (ví dụ xong Layout thì commit 1 lần, xong Logic thì commit 1 lần):
```bash
git status               # Kiểm tra xem có file nào thay đổi
git add .                # Đưa tất cả thay đổi vào hàng đợi
git commit -m "Mô tả việc vừa làm" 
# Ví dụ: git commit -m "Feat: Thiết kế xong giao diện đăng nhập"
```

### Bước 4: Đẩy code lên GitHub
```bash
git push origin feature/ten-cua-ban
```

---

## 4. Các lệnh bổ trợ cần biết

### Kiểm tra trạng thái và lịch sử
- `git status`: Xem các file đang sửa, chưa lưu.
- `git log --oneline`: Xem lịch sử các lần lưu code ngắn gọn.
- `git branch`: Xem mình đang ở nhánh nào.

### Hủy bỏ thay đổi (Khi lỡ tay làm sai)
- `git checkout -- [ten-file]`: Hủy thay đổi của 1 file cụ thể.
- `git reset --hard HEAD`: Xóa sạch mọi thay đổi chưa commit (Cẩn thận!).

### Xử lý nhánh
- `git checkout [ten-nhanh]`: Chuyển sang một nhánh đã có.
- `git branch -D [ten-nhanh]`: Xóa một nhánh ở máy cục bộ (Sau khi đã xong việc).

---

## 5. Quy tắc đặt tên (Convention)

### Tên Nhánh (Branch Name):
- `feature/...` : Thêm tính năng.
- `bugfix/...` : Sửa lỗi.
- `refactor/...`: Tối ưu code (không đổi tính năng).

### Thông điệp Lưu (Commit Message):
Hãy viết theo công thức: **Loại: Mô tả ngắn gọn**
- `Feat: Thêm màn hình giỏ hàng`
- `Fix: Sửa lỗi không load được ảnh`
- `Docs: Cập nhật hướng dẫn sử dụng`

---

## 6. Cách Gộp Nhánh Thủ Công (Manual Merge)
Dùng khi bạn đã hoàn thành tính năng ở nhánh `feature/...` và muốn đưa nó vào nhánh chính `main` mà không dùng Pull Request trên GitHub:

**Bước 1: Chuyển về nhánh main**
```bash
git checkout main
```

**Bước 2: Cập nhật code mới nhất của main từ server**
```bash
git pull origin main
```

**Bước 3: Gộp nhánh tính năng vào main**
```bash
git merge feature/ten-cua-ban
```

**Bước 4: Xử lý Xung đột (Conflict) - Nếu có**
- Nếu Git báo "Conflict", hãy mở file bị đỏ lên, chọn giữ lại code của bạn hoặc của bạn mình (hoặc cả hai).
- Sau khi sửa xong, lưu lại:
  ```bash
  git add .
  git commit -m "Fix: Resolve merge conflicts"
  ```

**Bước 5: Đẩy code đã gộp lên GitHub**
```bash
git push origin main
```

---

## 7. Lưu ý sống còn
1. **PULL trước khi PUSH**: Luôn kéo code mới về trước khi đẩy code mình lên.
2. **Không đẩy file rác**: File `google-services.json` và thư mục `build/` đã được chặn bởi `.gitignore`, đừng cố gắng ép chúng lên.
3. **Commit thường xuyên**: Đừng để cả tuần mới commit một lần, lỡ máy hỏng là mất sạch code.
