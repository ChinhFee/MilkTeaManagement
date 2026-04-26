# HƯỚNG DẪN SỬ DỤNG GIT & QUY TẮC NHÓM

Tài liệu này giúp các thành viên trong nhóm thống nhất cách làm việc để tránh xung đột code (conflict).

## 1. Quy tắc đặt tên Nhánh (Branch Naming)
Không bao giờ code trực tiếp trên nhánh `master` hoặc `main`. Hãy tạo nhánh mới cho mỗi tính năng:

- **Tính năng mới**: `feature/ten-tinh-nang` (VD: `feature/login-screen`, `feature/cart-logic`)
- **Sửa lỗi**: `bugfix/ten-loi` (VD: `bugfix/fix-image-loading`)
- **Cập nhật gấp**: `hotfix/ten-loi`

## 2. Quy trình làm việc cơ bản
Mỗi khi bắt đầu làm việc hoặc bắt đầu một tính năng mới:

1. **Cập nhật code mới nhất từ nhóm**:
   ```bash
   git checkout main
   git pull origin main
   ```

2. **Tạo nhánh mới để làm việc**:
   ```bash
   git checkout -b feature/ten-cua-ban
   ```

3. **Lưu lại công việc (Commit)**:
   Hãy commit thường xuyên, đừng để quá nhiều code rồi mới commit.
   ```bash
   git add .
   git commit -m "Mô tả ngắn gọn việc vừa làm (VD: Thêm giao diện chọn Topping)"
   ```

4. **Đẩy code lên GitHub**:
   ```bash
   git push origin feature/ten-cua-ban
   ```

## 3. Quy tắc đặt tên Commit (Commit Message)
Hãy viết có ý nghĩa để khi nhìn lại biết mình đã làm gì:
- `Feat: ...` cho tính năng mới.
- `Fix: ...` cho việc sửa lỗi.
- `Docs: ...` cho việc sửa tài liệu/hướng dẫn.
- `Refactor: ...` cho việc tối ưu lại code nhưng không đổi tính năng.

## 4. Cách gộp code (Merge)
Khi làm xong tính năng và muốn đưa vào bản chính:
1. Lên GitHub tạo một **Pull Request (PR)**.
2. Nhờ thành viên khác kiểm tra (Review).
3. Nếu OK thì nhấn **Merge**.

## 5. Lưu ý quan trọng
- **Trước khi commit**: Hãy đảm bảo code chạy được, không bị lỗi đỏ (Build error).
- **Khi bị Conflict (Xung đột)**: Nếu 2 người cùng sửa 1 dòng, Git sẽ báo lỗi. Lúc này hãy bình tĩnh mở file bị lỗi lên, chọn giữ lại code của ai hoặc kết hợp cả hai, sau đó commit lại.
- **File nhạy cảm**: Không bao giờ đẩy file `google-services.json` cá nhân hoặc API Key lên các kho lưu trữ công khai (Nếu dự án này là công khai).
