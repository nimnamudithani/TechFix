package com.nibm.techfix.models;

public class Technician {
    private int id;
    private String name;
    private int branchId;
    private String specialization; // e.g. "Mobile", "Computer"
    private boolean available;

    public Technician() { }

    public Technician(int id, String name, int branchId, String specialization, boolean available) {
        this.id = id;
        this.name = name;
        this.branchId = branchId;
        this.specialization = specialization;
        this.available = available;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
