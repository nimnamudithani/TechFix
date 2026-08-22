package com.nibm.techfix.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nibm.techfix.R;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.NominatimAddress;
import com.nibm.techfix.network.GeocodeClient;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows both TechFix branches as pins on a real OpenStreetMap view, plus the
 * user's own location if permission is granted. This is the visible half of
 * the Locations/GPS deliverable - the nearest-branch logic in
 * BookAppointmentActivity already uses real GPS internally; this screen just
 * makes that same kind of location data visible on screen for the demo.
 *
 * Uses osmdroid (OpenStreetMap) instead of Google Maps so no API key or
 * Google Cloud billing account is required.
 */
public class BranchMapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 400;

    private BranchDao branchDao;
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private TextView tvMyAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // osmdroid requires its configuration to be loaded before setContentView
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        // Use the app's own private cache dir so no storage permission is needed
        Configuration.getInstance().setOsmdroidBasePath(getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(new java.io.File(getCacheDir(), "osmdroid_tiles"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_map);

        branchDao = new BranchDao(this);
        mapView = findViewById(R.id.mapView);
        tvMyAddress = findViewById(R.id.tvMyAddress);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        showBranchMarkers();
        enableMyLocationIfPermitted();
    }

    private void showBranchMarkers() {
        List<Branch> branches = branchDao.getAllBranches();
        if (branches.isEmpty()) return;

        double sumLat = 0, sumLng = 0;
        for (Branch branch : branches) {
            GeoPoint point = new GeoPoint(branch.getLatitude(), branch.getLongitude());

            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setTitle(branch.getName());
            marker.setSnippet(branch.getAddress());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);

            sumLat += branch.getLatitude();
            sumLng += branch.getLongitude();
        }

        // Center the map roughly between all branches, zoomed out enough to see both
        GeoPoint center = new GeoPoint(sumLat / branches.size(), sumLng / branches.size());
        mapView.getController().setZoom(9.0);
        mapView.getController().setCenter(center);
    }

    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            addMyLocationOverlay();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }

    private void addMyLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Web Services & Remote Data deliverable: once we have a real GPS fix,
        // send it to OpenStreetMap's Nominatim REST API to resolve it into a
        // readable address instead of just raw coordinates.
        myLocationOverlay.runOnFirstFix(() -> {
            GeoPoint myLocation = myLocationOverlay.getMyLocation();
            if (myLocation != null) {
                reverseGeocodeCurrentLocation(myLocation);
            }
        });
    }

    private void reverseGeocodeCurrentLocation(GeoPoint point) {
        GeocodeClient.getService()
                .reverseGeocode(point.getLatitude(), point.getLongitude(), "json")
                .enqueue(new Callback<NominatimAddress>() {
                    @Override
                    public void onResponse(Call<NominatimAddress> call, Response<NominatimAddress> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getDisplayName() != null) {
                            tvMyAddress.setText("You are near: " + response.body().getDisplayName());
                            tvMyAddress.setVisibility(android.view.View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<NominatimAddress> call, Throwable t) {
                        // No internet or the free public API is unreachable right now -
                        // the map itself still works fine offline via cached tiles, so
                        // we just silently skip showing the address line.
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
            addMyLocationOverlay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
