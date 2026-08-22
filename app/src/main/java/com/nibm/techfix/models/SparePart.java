package com.nibm.techfix.models;

public class SparePart {
    private int id;
    private String name;
    private int branchId;
    private int stockQty;
    private double unitPrice;

    public SparePart() { }

    public SparePart(int id, String name, int branchId, int stockQty, double unitPrice) {
        this.id = id;
        this.name = name;
        this.branchId = branchId;
        this.stockQty = stockQty;
        this.unitPrice = unitPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
