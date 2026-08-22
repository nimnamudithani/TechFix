package com.nibm.techfix.utils;

import android.location.Location;

import com.nibm.techfix.models.Branch;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for the "assign to nearest branch" requirement.
 * Combine this with FusedLocationProviderClient in BookAppointmentActivity to
 * get the user's current lat/lng, then call sortBranchesByDistance() and walk
 * the list checking technician/spare-part availability at each, nearest first.
 */
public class LocationUtils {

    public static Branch findNearestBranch(double userLat, double userLng, List<Branch> branches) {
        Branch nearest = null;
        float shortestDistance = Float.MAX_VALUE;

        for (Branch branch : branches) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLat, userLng,
                    branch.getLatitude(), branch.getLongitude(),
                    results
            );
            float distanceInMeters = results[0];

            if (distanceInMeters < shortestDistance) {
                shortestDistance = distanceInMeters;
                nearest = branch;
            }
        }
        return nearest;
    }

    /** Returns branches sorted nearest-to-farthest from the given location. */
    public static List<Branch> sortBranchesByDistance(double userLat, double userLng, List<Branch> branches) {
        List<Branch> sorted = new ArrayList<>(branches);
        sorted.sort((a, b) -> {
            float[] distA = new float[1];
            float[] distB = new float[1];
            Location.distanceBetween(userLat, userLng, a.getLatitude(), a.getLongitude(), distA);
            Location.distanceBetween(userLat, userLng, b.getLatitude(), b.getLongitude(), distB);
            return Float.compare(distA[0], distB[0]);
        });
        return sorted;
    }
}
