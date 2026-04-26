package com.example.milkteamanagement.repositories;

import android.net.Uri;

public class GoogleDriveRepository {

    public GoogleDriveRepository() {
    }

    /**
     * Chuyển đổi link chia sẻ Google Drive thông thường sang link ảnh trực tiếp (Direct Link)
     * Để Android có thể hiển thị bằng các thư viện như Glide hoặc Picasso.
     * 
     * Link mẫu: https://drive.google.com/file/d/FILE_ID/view?usp=sharing
     * Link trực tiếp: https://drive.google.com/uc?id=FILE_ID
     */
    public String convertToDirectLink(String driveLink) {
        if (driveLink == null || !driveLink.contains("drive.google.com")) {
            return driveLink;
        }

        try {
            String fileId = "";
            if (driveLink.contains("/d/")) {
                fileId = driveLink.split("/d/")[1].split("/")[0];
            } else if (driveLink.contains("id=")) {
                fileId = driveLink.split("id=")[1].split("&")[0];
            }

            if (!fileId.isEmpty()) {
                return "https://drive.google.com/uc?id=" + fileId;
            }
        } catch (Exception e) {
            return driveLink;
        }
        return driveLink;
    }

    /**
     * Lưu ý: Để Upload trực tiếp lên Google Drive từ App Android, bạn cần:
     * 1. Cấu hình Google Cloud Console (OAuth 2.0).
     * 2. Thêm thư viện 'com.google.apis:google-api-services-drive'.
     * 3. Xin quyền truy cập Drive của người dùng.
     * 
     * Vì quy trình này rất phức tạp và cần bảo mật API Key,
     * cách tốt nhất hiện tại là Admin tải ảnh lên Drive, lấy Link và dán vào App.
     */
    public interface DriveUploadCallback {
        void onSuccess(String directLink);
        void onFailure(String message);
    }
}
