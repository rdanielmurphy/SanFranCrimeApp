/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.example.SanFranCrimeApp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import au.com.bytecode.opencsv.CSVReader;

public class DataBackend {
    private static DataBackend _instance;
    private static Context _context;

    private static Map<String, List<CrimeDto>> _crimesByType;
    private static List<CrimeTypeDto> _crimeTypes;
    
    private DataBackend() {
    }

    // singleton class which can be used in different activities.  calling getInstance() the first time will initiate everything
    public static DataBackend getInstance(Context c) throws IOException, ParseException {
        if (_instance == null)
            _instance = new DataBackend();

        _context = c;
        
        if (_crimesByType == null)
            readAllData();
        
        return _instance;
    }
    
    // return all crimes found in file
    public List<CrimeDto> getCrimes() {
        List<CrimeDto> crimes = new ArrayList<CrimeDto>();
        
        for (Map.Entry<String, List<CrimeDto>> entry : _crimesByType.entrySet()) {
            for (CrimeDto crime : entry.getValue())
                crimes.add(crime);
        }
        
        return crimes;
    }
    
    // return list of crimes based on type
    public List<CrimeDto> getCrimes(CrimeTypeDto type) {
        return _crimesByType.get(type.getCrimeType());
    }

    // return list of crime based on a list of crime types
    public List<CrimeDto> getCrimes(List<CrimeTypeDto> types) {
        List<CrimeDto> newList = new ArrayList<CrimeDto>();

        for (CrimeTypeDto type : types)
            newList.addAll(_crimesByType.get(type.getCrimeType()));

        return newList;
    }

    // return all the different types of crimes found in the data
    public List<CrimeTypeDto> getCrimeTypes() {
        return _crimeTypes;
    }

    // read all the data from the CSV files that were created from the KML file
    private static void readAllData() throws IOException, ParseException {
        _crimesByType = new HashMap<String, List<CrimeDto>>();
        _crimeTypes = new ArrayList<CrimeTypeDto>();

        Map<String, CrimeDto> crimes = new HashMap<String, CrimeDto>();
        
        // read placemark file
        CSVReader reader = new CSVReader(new InputStreamReader(_context.getResources().openRawResource(R.raw.placemark)));
        String [] nextLine;
        nextLine = reader.readNext();//skip first
        while ((nextLine = reader.readNext()) != null) {
            CrimeDto crime = new CrimeDto();
            
            String crimeId = nextLine[2];
            crime.setName(nextLine[0]);
            crime.setDescription(nextLine[1]);
            crimes.put(crimeId, crime);
        }
        
        // read data file
        reader = new CSVReader(new InputStreamReader(_context.getResources().openRawResource(R.raw.data)));
        nextLine = reader.readNext();//skip first
        while ((nextLine = reader.readNext()) != null) {
            String crimeId = nextLine[2];
            
            // read two lines since type and link are on separate lines
            crimes.get(crimeId).setType(nextLine[1]);
            nextLine = reader.readNext();
            crimes.get(crimeId).setUrl(nextLine[1]);
        }
        
        // read point file
        reader = new CSVReader(new InputStreamReader(_context.getResources().openRawResource(R.raw.point)));
        nextLine = reader.readNext();//skip first
        while ((nextLine = reader.readNext()) != null) {
            String crimeId = nextLine[1];
            String coordinates = nextLine[0];
            
            crimes.get(crimeId).setLatitude(Double.parseDouble(coordinates.split(",")[1]));
            crimes.get(crimeId).setLongitude(Double.parseDouble(coordinates.split(",")[0]));
        }
        
        // read timestamp file
        reader = new CSVReader(new InputStreamReader(_context.getResources().openRawResource(R.raw.timestamp)));
        nextLine = reader.readNext();//skip first
        while ((nextLine = reader.readNext()) != null) {
            String crimeId = nextLine[1];
            crimes.get(crimeId).setWhen(nextLine[0]);
        }
        
        // put into master dictionary
        for (Map.Entry<String, CrimeDto> entry : crimes.entrySet()) {
            String crimeType = entry.getValue().getType();
            if (!_crimesByType.containsKey(crimeType))
                _crimesByType. put(crimeType, new ArrayList<CrimeDto>());

            _crimesByType.get(crimeType).add(entry.getValue());
        }

        // create list of crime types
        for (Map.Entry<String, List<CrimeDto>> entry : _crimesByType.entrySet())
            _crimeTypes.add(new CrimeTypeDto(entry.getKey(), entry.getValue().size()));

        // sort types by number of crimes
        Collections.sort(_crimeTypes, new Comparator<CrimeTypeDto>() {
            @Override
            public int compare(CrimeTypeDto lhs, CrimeTypeDto rhs) {
                if (lhs.getNumberOfCrimes() < rhs.getNumberOfCrimes())
                    return 1;
                else if (lhs.getNumberOfCrimes() > rhs.getNumberOfCrimes())
                    return -1;
                return 0;
            }
        });
    }   
}
