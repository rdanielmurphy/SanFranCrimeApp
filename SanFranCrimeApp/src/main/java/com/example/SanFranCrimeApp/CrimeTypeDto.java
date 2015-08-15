package com.example.SanFranCrimeApp;

/**
 * Created by Danny on 2/22/14.
 *
 * Stores the crime type.  We want to be able to display crime based on type.
 */
public class CrimeTypeDto {
    private String _crimeType;
    private int _numberOfCrimes;

    public CrimeTypeDto(String crimeType, int numberOfCrimes) {
        _crimeType = crimeType;
        _numberOfCrimes = numberOfCrimes;
    }

    public String getCrimeType() {
        return _crimeType;
    }

    public void setCrimeType(String _crimeType) {
        this._crimeType = _crimeType;
    }

    public int getNumberOfCrimes() {
        return _numberOfCrimes;
    }

    public void setNumberOfCrimes(int numberOfCrimes) {
        this._numberOfCrimes = numberOfCrimes;
    }

    @Override
    public String toString() {
        return _crimeType + " (" + _numberOfCrimes + ")";
    }
}
