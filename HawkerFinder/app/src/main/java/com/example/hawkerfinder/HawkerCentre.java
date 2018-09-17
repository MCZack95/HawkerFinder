package com.example.hawkerfinder;

public class HawkerCentre {

    String stallName,stallAddress,stallPostalCode;

    HawkerCentre(String stallName, String stallAddress, String stallPostalCode) {
        this.stallName = stallName;
        this.stallAddress = stallAddress;
        this.stallPostalCode = stallPostalCode;
    }

    public String getStallName() {
        return stallName;
    }

    public void setStallName(String stallName) {
        this.stallName = stallName;
    }

    public String getStallAddress() {
        return stallAddress;
    }

    public void setStallAddress(String stallAddress) {
        this.stallAddress = stallAddress;
    }

    public String getStallPostalCode() {
        return stallPostalCode;
    }

    public void setStallPostalCode(String stallPostalCode) { this.stallPostalCode = stallPostalCode; }

}
