package com.example.milkteamanagement.models;

public class TableModel {
    private int tableNumber;
    private String status;
    private int floor;

    public TableModel(int tableNumber, String status, int floor) {
        this.tableNumber = tableNumber;
        this.status = status;
        this.floor = floor;
    }

    public int getTableNumber() { return tableNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getFloor() { return floor; }
}
