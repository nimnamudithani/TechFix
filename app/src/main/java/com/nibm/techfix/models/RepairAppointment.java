package com.nibm.techfix.models;

public class RepairAppointment {

    // Status constants used throughout the app
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_ASSIGNED = "Assigned";
    public static final String STATUS_IN_PROGRESS = "InProgress";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_PAID = "PaidOff";

    private int id;
    private int userId;
    private int branchId;
    private int technicianId;
    private int deviceCategoryId;
    private int repairServiceId;
    private String status;
    private String requestedDate;   // store as "yyyy-MM-dd HH:mm"
    private String beforeImagePath; // local file path or remote URL
    private String afterImagePath;

    public RepairAppointment() { }

    public RepairAppointment(int id, int userId, int branchId, int technicianId, int deviceCategoryId,
                              int repairServiceId, String status, String requestedDate) {
        this.id = id;
        this.userId = userId;
        this.branchId = branchId;
        this.technicianId = technicianId;
        this.deviceCategoryId = deviceCategoryId;
        this.repairServiceId = repairServiceId;
        this.status = status;
        this.requestedDate = requestedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public int getTechnicianId() { return technicianId; }
    public void setTechnicianId(int technicianId) { this.technicianId = technicianId; }

    public int getDeviceCategoryId() { return deviceCategoryId; }
    public void setDeviceCategoryId(int deviceCategoryId) { this.deviceCategoryId = deviceCategoryId; }

    public int getRepairServiceId() { return repairServiceId; }
    public void setRepairServiceId(int repairServiceId) { this.repairServiceId = repairServiceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestedDate() { return requestedDate; }
    public void setRequestedDate(String requestedDate) { this.requestedDate = requestedDate; }

    public String getBeforeImagePath() { return beforeImagePath; }
    public void setBeforeImagePath(String beforeImagePath) { this.beforeImagePath = beforeImagePath; }

    public String getAfterImagePath() { return afterImagePath; }
    public void setAfterImagePath(String afterImagePath) { this.afterImagePath = afterImagePath; }
}
