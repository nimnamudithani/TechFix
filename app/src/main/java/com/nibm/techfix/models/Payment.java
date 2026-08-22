package com.nibm.techfix.models;

public class Payment {
    private int id;
    private int appointmentId;
    private double amount;
    private String method; // "Cash", "Card"
    private String status; // "Pending", "Paid"
    private String paidAt;

    public Payment() { }

    public Payment(int id, int appointmentId, double amount, String method, String status, String paidAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.paidAt = paidAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
}
