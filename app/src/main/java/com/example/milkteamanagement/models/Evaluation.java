package com.example.milkteamanagement.models;

public class Evaluation {
    private String evaluationId;
    private String orderId;
    private String customerId;
    private String customerName;
    private float rating;
    private String comment;
    private long timestamp;

    public Evaluation() {
    }

    public Evaluation(String evaluationId, String orderId, String customerId, String customerName, float rating, String comment, long timestamp) {
        this.evaluationId = evaluationId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getEvaluationId() { return evaluationId; }
    public void setEvaluationId(String evaluationId) { this.evaluationId = evaluationId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
