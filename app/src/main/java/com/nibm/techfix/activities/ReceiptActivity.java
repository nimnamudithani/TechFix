package com.nibm.techfix.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nibm.techfix.R;
import com.nibm.techfix.database.AppointmentDao;
import com.nibm.techfix.database.PaymentDao;
import com.nibm.techfix.database.CatalogDao;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.database.UserDao;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.RepairAppointment;
import com.nibm.techfix.models.RepairService;
import com.nibm.techfix.models.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        AppointmentDao appointmentDao = new AppointmentDao(this);
        PaymentDao paymentDao = new PaymentDao(this);
        CatalogDao catalogDao = new CatalogDao(this);
        BranchDao branchDao = new BranchDao(this);
        UserDao userDao = new UserDao(this);
        int appointmentId = getIntent().getIntExtra("appointmentId", -1);
        RepairAppointment appt = appointmentDao.getAppointmentById(appointmentId);
        if (appt == null) {
            Toast.makeText(this, "Receipt unavailable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String[] payment = paymentDao.getPaymentForAppointment(appointmentId);
        if (payment == null) {
            Toast.makeText(this, "Receipt is available after payment", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RepairService service = catalogDao.getServiceById(appt.getRepairServiceId());
        Branch branch = branchDao.getBranchById(appt.getBranchId());
        User user = userDao.getUserById(appt.getUserId());

        ((TextView)findViewById(R.id.tvReceiptNo)).setText("Receipt #TF-" + String.format(Locale.getDefault(), "%05d", appointmentId));
        ((TextView)findViewById(R.id.tvReceiptCustomer)).setText("Customer: " + (user != null ? user.getName() : "Customer"));
        ((TextView)findViewById(R.id.tvReceiptService)).setText("Service: " + (service != null ? service.getName() : "Repair Service"));
        ((TextView)findViewById(R.id.tvReceiptBranch)).setText("Branch: " + (branch != null ? branch.getName() : ""));
        ((TextView)findViewById(R.id.tvReceiptAppointment)).setText("Appointment: " + appt.getRequestedDate());
        ((TextView)findViewById(R.id.tvReceiptAmount)).setText("Total: Rs. " + String.format(Locale.getDefault(), "%.2f", Double.parseDouble(payment[0])));
        ((TextView)findViewById(R.id.tvReceiptMethod)).setText("Payment method: " + payment[1]);
        ((TextView)findViewById(R.id.tvReceiptStatus)).setText("✓ " + payment[2]);

        try {
            long paidAt = Long.parseLong(payment[3]);
            String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date(paidAt));
            ((TextView)findViewById(R.id.tvReceiptPaidAt)).setText("Paid: " + date);
        } catch (Exception e) {
            ((TextView)findViewById(R.id.tvReceiptPaidAt)).setText("Paid");
        }
    }
}
