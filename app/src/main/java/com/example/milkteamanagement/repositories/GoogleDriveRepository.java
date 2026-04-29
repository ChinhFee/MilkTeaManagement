package com.example.milkteamanagement.repositories;

public class GoogleDriveRepository {

    public GoogleDriveRepository() {
    }

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

    public interface DriveUploadCallback {
        void onSuccess(String directLink);
        void onFailure(String message);
    }
}
