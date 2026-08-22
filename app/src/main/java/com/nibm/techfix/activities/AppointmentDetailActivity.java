package com.nibm.techfix.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.nibm.techfix.R;
import com.nibm.techfix.database.AppointmentDao;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.database.CatalogDao;
import com.nibm.techfix.database.TechnicianDao;
import com.nibm.techfix.database.PaymentDao;
import com.nibm.techfix.database.ReviewDao;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.RepairAppointment;
import com.nibm.techfix.models.RepairService;
import com.nibm.techfix.models.Technician;
import com.nibm.techfix.utils.RepairInfoUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AppointmentDetailActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 200;

    private AppointmentDao appointmentDao;
    private BranchDao branchDao;
    private CatalogDao catalogDao;
    private TechnicianDao technicianDao;
    private PaymentDao paymentDao;
    private ReviewDao reviewDao;
    private RepairAppointment appointment;
    private Uri photoUri;

    private TextView tvServiceName, tvBranchName, tvTechnicianName, tvDate, tvPrice, tvStatus, tvEstimatedCompletion, tvWarranty;
    private ImageView ivDevicePhoto;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    ivDevicePhoto.setVisibility(android.view.View.VISIBLE);
                    ivDevicePhoto.setImageURI(photoUri);
                    // Save path against the appointment. "before" if none set yet, else "after".
                    String slot = appointment.getBeforeImagePath() == null ? "before" : "after";
                    String savedUri = photoUri.toString();
                    appointmentDao.updateAppointmentImage(appointment.getId(), slot, savedUri);
                    // Keep the in-memory object in sync so a second photo in the same
                    // screen is correctly stored as the after-repair image.
                    if ("before".equals(slot)) {
                        appointment.setBeforeImagePath(savedUri);
                    } else {
                        appointment.setAfterImagePath(savedUri);
                    }
                    Toast.makeText(this, "Photo saved (" + slot + ")", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        appointmentDao = new AppointmentDao(this);
        branchDao = new BranchDao(this);
        catalogDao = new CatalogDao(this);
        technicianDao = new TechnicianDao(this);
        paymentDao = new PaymentDao(this);
        reviewDao = new ReviewDao(this);
        int appointmentId = getIntent().getIntExtra("appointmentId", -1);
        boolean isStaffView = getIntent().getBooleanExtra("isStaffView", false);
        appointment = appointmentDao.getAppointmentById(appointmentId);

        tvServiceName = findViewById(R.id.tvServiceName);
        tvBranchName = findViewById(R.id.tvBranchName);
        tvTechnicianName = findViewById(R.id.tvTechnicianName);
        tvDate = findViewById(R.id.tvDate);
        tvPrice = findViewById(R.id.tvPrice);
        tvStatus = findViewById(R.id.tvStatus);
        ivDevicePhoto = findViewById(R.id.ivDevicePhoto);
        tvEstimatedCompletion = findViewById(R.id.tvEstimatedCompletion);
        tvWarranty = findViewById(R.id.tvWarranty);

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        Button btnMarkCompleted = findViewById(R.id.btnMarkCompleted);
        Button btnMarkPaid = findViewById(R.id.btnMarkPaid);
        Button btnContactBranch = findViewById(R.id.btnContactBranch);
        Button btnViewReceipt = findViewById(R.id.btnViewReceipt);
        LinearLayout reviewSection = findViewById(R.id.reviewSection);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextView tvReviewTechnician = findViewById(R.id.tvReviewTechnician);
        EditText etReviewComment = findViewById(R.id.etReviewComment);
        Button btnSubmitReview = findViewById(R.id.btnSubmitReview);

        if (appointment == null) {
            Toast.makeText(this, "Appointment not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindAppointmentData();

        Branch contactBranch = branchDao.getBranchById(appointment.getBranchId());
        btnContactBranch.setOnClickListener(v -> {
            if (contactBranch == null || contactBranch.getContactNumber() == null) {
                Toast.makeText(this, "Branch contact number unavailable", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactBranch.getContactNumber().replace("-", "")));
            startActivity(dial);
        });

        if (!isStaffView && RepairAppointment.STATUS_PAID.equals(appointment.getStatus())) {
            btnViewReceipt.setVisibility(android.view.View.VISIBLE);
            btnViewReceipt.setOnClickListener(v -> {
                Intent receipt = new Intent(AppointmentDetailActivity.this, ReceiptActivity.class);
                receipt.putExtra("appointmentId", appointment.getId());
                startActivity(receipt);
            });
        }

        if (!isStaffView && (RepairAppointment.STATUS_COMPLETED.equals(appointment.getStatus()) || RepairAppointment.STATUS_PAID.equals(appointment.getStatus()))) {
            Technician reviewedTechnician = technicianDao.getTechnicianById(appointment.getTechnicianId());
            if (reviewedTechnician != null) {
                reviewSection.setVisibility(android.view.View.VISIBLE);
                tvReviewTechnician.setText("Your technician: " + reviewedTechnician.getName());
            }
            String[] existingReview = reviewDao.getReviewForAppointment(appointment.getId());
            if (existingReview != null) {
                ratingBar.setRating(Float.parseFloat(existingReview[0]));
                etReviewComment.setText(existingReview[1]);
                ratingBar.setIsIndicator(true);
                etReviewComment.setEnabled(false);
                btnSubmitReview.setEnabled(false);
                btnSubmitReview.setText("Review Submitted");
            } else {
                btnSubmitReview.setOnClickListener(v -> {
                    long reviewId = reviewDao.addReview(appointment.getId(), appointment.getUserId(), (int) ratingBar.getRating(), etReviewComment.getText().toString());
                    if (reviewId != -1) {
                        Toast.makeText(this, "Thanks! Your rating was assigned to your technician.", Toast.LENGTH_SHORT).show();
                        ratingBar.setIsIndicator(true); etReviewComment.setEnabled(false); btnSubmitReview.setEnabled(false); btnSubmitReview.setText("Review Submitted");
                    } else Toast.makeText(this, "Review already submitted", Toast.LENGTH_SHORT).show();
                });
            }
        }

        if (isStaffView) {
            btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndCapture());
            btnMarkCompleted.setOnClickListener(v -> {
                appointmentDao.updateAppointmentStatus(appointment.getId(), RepairAppointment.STATUS_COMPLETED);
                appointment.setStatus(RepairAppointment.STATUS_COMPLETED);
                tvStatus.setText("Status: " + RepairAppointment.STATUS_COMPLETED);
                Toast.makeText(this, "Marked as completed", Toast.LENGTH_SHORT).show();
            });
            btnMarkPaid.setOnClickListener(v -> {
                RepairService service = catalogDao.getServiceById(appointment.getRepairServiceId());
                double amount = service != null ? service.getBasePrice() : 0;
                long paymentId = paymentDao.recordPayment(appointment.getId(), amount, "Cash");
                if (paymentId == -1) {
                    Toast.makeText(this, "Could not record payment", Toast.LENGTH_SHORT).show();
                    return;
                }
                appointment.setStatus(RepairAppointment.STATUS_PAID);
                tvStatus.setText("Status: " + RepairAppointment.STATUS_PAID);
                Toast.makeText(this, "Payment recorded", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Customers can view their appointment but not edit its status,
            // take repair photos, or record payment - that's staff's job.
            btnTakePhoto.setVisibility(android.view.View.GONE);
            btnMarkCompleted.setVisibility(android.view.View.GONE);
            btnMarkPaid.setVisibility(android.view.View.GONE);
        }
    }

    private void bindAppointmentData() {
        RepairService service = catalogDao.getServiceById(appointment.getRepairServiceId());
        Branch branch = branchDao.getBranchById(appointment.getBranchId());
        Technician technician = technicianDao.getTechnicianById(appointment.getTechnicianId());

        tvServiceName.setText(service != null ? service.getName() : "Repair Service");
        tvBranchName.setText("Branch: " + (branch != null ? branch.getName() : ""));
        tvTechnicianName.setText("Technician: " + (technician != null ? technician.getName() : "Unassigned"));
        tvDate.setText("Requested: " + appointment.getRequestedDate());
        tvPrice.setText("Price: Rs. " + (service != null ? (int) service.getBasePrice() : 0));
        tvStatus.setText("Status: " + appointment.getStatus());
        if (service != null) {
            tvEstimatedCompletion.setText("⏱ Estimated completion: " + RepairInfoUtils.estimatedTime(service.getName()));
            int warrantyDays = RepairInfoUtils.warrantyDays(service.getName());
            if (RepairAppointment.STATUS_COMPLETED.equals(appointment.getStatus()) || RepairAppointment.STATUS_PAID.equals(appointment.getStatus())) {
                tvWarranty.setText("🛡 Repair warranty: " + warrantyDays + " days from repair completion");
            } else {
                tvWarranty.setText("🛡 Warranty after completion: " + warrantyDays + " days");
            }
        }

        if (appointment.getAfterImagePath() != null) {
            ivDevicePhoto.setVisibility(android.view.View.VISIBLE);
            ivDevicePhoto.setImageURI(Uri.parse(appointment.getAfterImagePath()));
        } else if (appointment.getBeforeImagePath() != null) {
            ivDevicePhoto.setVisibility(android.view.View.VISIBLE);
            ivDevicePhoto.setImageURI(Uri.parse(appointment.getBeforeImagePath()));
        }
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return;
        }
        launchCamera();
    }

    private void launchCamera() {
        try {
            File photoFile = File.createTempFile(
                    "repair_" + appointment.getId() + "_", ".jpg", getExternalFilesDir("Pictures"));
            photoUri = FileProvider.getUriForFile(this, "com.nibm.techfix.fileprovider", photoFile);
            takePictureLauncher.launch(photoUri);
        } catch (IOException e) {
            Toast.makeText(this, "Couldn't open camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            Toast.makeText(this, "Camera permission is needed to take a photo", Toast.LENGTH_SHORT).show();
        }
    }
}
