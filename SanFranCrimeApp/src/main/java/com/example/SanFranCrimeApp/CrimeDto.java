/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.example.SanFranCrimeApp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Danny on 2/22/14.
 *
 * Stores the crime information.
 */
public class CrimeDto {
    private String _type, _url, _name, _description;
    private double _longitude, _latitude;
    private Date _when;
    
    public CrimeDto(){
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public double getLongitude() {
        return _longitude;
    }

    public void setLongitude(double longitude) {
        _longitude = longitude;
    }

    public double getLatitude() {
        return _latitude;
    }

    public void setLatitude(double latitude) {
        _latitude = latitude;
    }

    public Date getWhen() {
        return _when;
    }

    public void setWhen(String when) throws ParseException {
        // remove a couple things from the date string that I don't understand
        when = when.replace("-07:00","");
        when = when.replace("T"," ");

        // get date from string
        _when = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(when);
    }
}
