package com.nibm.techfix.models;

import com.google.gson.annotations.SerializedName;

/**
 * Maps the JSON response from OpenStreetMap's Nominatim "reverse" endpoint.
 * We only need a couple of fields for the UI, but Gson happily ignores any
 * extra fields Nominatim sends back that we don't declare here.
 *
 * Example response shape:
 * {
 *   "display_name": "45, Main Street, Galle, Southern Province, Sri Lanka",
 *   "address": { "road": "Main Street", "city": "Galle", "country": "Sri Lanka" }
 * }
 */
public class NominatimAddress {

    @SerializedName("display_name")
    private String displayName;

    private Address address;

    public String getDisplayName() {
        return displayName;
    }

    public Address getAddress() {
        return address;
    }

    public static class Address {
        private String road;
        private String suburb;
        private String city;
        private String country;

        public String getRoad() { return road; }
        public String getSuburb() { return suburb; }
        public String getCity() { return city; }
        public String getCountry() { return country; }
    }
}
