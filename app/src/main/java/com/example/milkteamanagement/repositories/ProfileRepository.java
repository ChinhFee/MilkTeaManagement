package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileRepository {
    private static ProfileRepository instance;
    private final FirebaseFirestore db;
    private final CollectionReference usersRef;

    private ProfileRepository() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("Users");
    }

    public static synchronized ProfileRepository getInstance() {
        if (instance == null) {
            instance = new ProfileRepository();
        }
        return instance;
    }

    public Task<Void> saveUserProfile(User user) {
        return usersRef.document(user.getUid()).set(user);
    }

    public Task<DocumentSnapshot> getUserProfile(String uid) {
        return usersRef.document(uid).get();
    }

    public Task<Void> updateUserProfile(User user) {
        return usersRef.document(user.getUid()).set(user);
    }
}
