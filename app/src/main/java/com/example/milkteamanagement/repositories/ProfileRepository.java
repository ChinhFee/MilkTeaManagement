package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileRepository {
    private final FirebaseFirestore db;

    public interface ProfileCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public ProfileRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy thông tin User
     */
    public void getUserProfile(String userId, ProfileCallback callback) {
        db.collection(FirebaseConstants.COL_USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Cập nhật thông tin User
     */
    public void updateUserInfo(User user, ProfileCallback callback) {
        if (user.getPhone() == null || user.getPhone().length() < 10) {
            callback.onError("Số điện thoại không hợp lệ");
            return;
        }

        db.collection(FirebaseConstants.COL_USERS).document(user.getUid()).set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
