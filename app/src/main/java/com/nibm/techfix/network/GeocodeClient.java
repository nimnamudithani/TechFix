package com.nibm.techfix.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit client for the Web Services & Remote Data deliverable - talks to
 * OpenStreetMap's public Nominatim geocoding service to turn a raw GPS
 * lat/lng into a human-readable address.
 */
public class GeocodeClient {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/";

    private static Retrofit retrofit = null;

    public static GeocodeService getService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GeocodeService.class);
    }
}
