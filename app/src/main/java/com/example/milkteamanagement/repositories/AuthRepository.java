package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressWarnings("unused")
public class AuthRepository {
    private static AuthRepository instance;
    private final FirebaseAuth mAuth;
    private final ProfileRepository profileRepository;

    private AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        profileRepository = ProfileRepository.getInstance();
    }

    public static synchronized AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
        }
        return instance;
    }

    public Task<AuthResult> register(String email, String password, String fullName, String phoneNumber, String role) {
        return mAuth.createUserWithEmailAndPassword(email, password)
                .onSuccessTask(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        User newUser = new User(firebaseUser.getUid(), fullName, email, phoneNumber, "", role);
                        return profileRepository.saveUserProfile(newUser).continueWith(task -> authResult);
                    }
                    return Tasks.forResult(authResult);
                });
    }

    public Task<AuthResult> login(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<Void> forgotPassword(String email) {
        return mAuth.sendPasswordResetEmail(email);
    }

    public Task<User> getUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return Tasks.forResult(null);

        return profileRepository.getUserProfile(firebaseUser.getUid())
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(User.class);
                    }
                    return null;
                });
    }

    public void logout() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}
