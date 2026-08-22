package com.nibm.techfix.models;

public class RepairService {
    private int id;
    private int deviceCategoryId;
    private String name;
    private String description;
    private double basePrice;

    public RepairService() { }

    public RepairService(int id, int deviceCategoryId, String name, String description, double basePrice) {
        this.id = id;
        this.deviceCategoryId = deviceCategoryId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDeviceCategoryId() { return deviceCategoryId; }
    public void setDeviceCategoryId(int deviceCategoryId) { this.deviceCategoryId = deviceCategoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
}
