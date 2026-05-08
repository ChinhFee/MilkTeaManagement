package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.Evaluation;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EvaluationRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference evaluationsRef = db.collection(FirebaseConstants.COL_EVALUATIONS);

    public interface EvaluationCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface EvaluationListCallback {
        void onSuccess(List<Evaluation> evaluations);
        void onFailure(String message);
    }

    public void addEvaluation(Evaluation evaluation, EvaluationCallback callback) {
        String id = evaluationsRef.document().getId();
        evaluation.setEvaluationId(id);
        evaluationsRef.document(id).set(evaluation)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getEvaluationByOrder(String orderId, EvaluationListCallback callback) {
        evaluationsRef.whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Evaluation> list = queryDocumentSnapshots.toObjects(Evaluation.class);
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getAllEvaluations(EvaluationListCallback callback) {
        evaluationsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Evaluation> list = queryDocumentSnapshots.toObjects(Evaluation.class);
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
