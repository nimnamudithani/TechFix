package com.nibm.techfix.network;

import com.nibm.techfix.models.NominatimAddress;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Web Services & Remote Data deliverable.
 *
 * Calls OpenStreetMap's free Nominatim REST API to turn a raw GPS
 * lat/lng (already captured by the Locations/GPS deliverable) into a
 * human-readable address. No API key or account needed - just a
 * required User-Agent header per Nominatim's usage policy.
 *
 * https://nominatim.org/release-docs/latest/api/Reverse/
 */
public interface GeocodeService {

    @Headers("User-Agent: TechFixAndroidApp/1.0")
    @GET("reverse")
    Call<NominatimAddress> reverseGeocode(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("format") String format);
}
