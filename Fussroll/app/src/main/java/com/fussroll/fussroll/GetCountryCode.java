package com.fussroll.fussroll;

/**
 * Created by kunal on 29/12/16.
 */

class GetCountryCode {
    private String countryCodeReceived;
    GetCountryCode(String countryCodeReceived) {
        this.countryCodeReceived = countryCodeReceived;
    }
    String getCountryCode() {
        switch (countryCodeReceived) {
            case "India":
                return "+91";
            case "United Kingdom":
                return "+44";
            case "United States":
            case "Canada":
                return "+1";
            default:
                return "+++";
        }
    }
}
