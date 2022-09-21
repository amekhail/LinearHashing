/*
 * Prog1A.java -- Takes a CSV file (comma separeted value) and turns it into a
 * binary file for easy read and write by any program as well as the added 
 * benifit of taking up less space since there is no use of ASCII or UNICODE
 * characters. The entries will be sorted by EIA ID number
 * 
 * Looking at the file "2021-utlity-scale-solar-plants.csv", each entry has 9
 * fields of information which are separtated by commas. Field types will be 
 * limited to int, double, and string. Strings will be padded to the length
 * of the longest entry in the field.
 * 
 * Author: Adam Mekhail
 * First Version: 2022-08-23
 * 
 * 
 */

import java.io.*;
import java.util.*;

/*+----------------------------------------------------------------------
||  Class Prog1A
||
||         Author:  Adam Mekhail
||
||         Purpose: To read a CSV file and create a bin file where all
||                  the entries are the same size for the purpose of easy
||                  writting and reading. Doing this will will store the 
||                  as binary meaning no ASCII or UNICODE values will be
||                  saved.
||
||   Inherits from: None.
||
||      Interfaces: None.
||
|+-----------------------------------------------------------------------*
||
||       Constants: None.
|| 
|+-----------------------------------------------------------------------*
||
||    Constructors: Default Constructor
||
||   Class Methods: None
||
||   Inst. Methods: solarEntries parseLine(String line)
||                  void writeToBin(String fileName, ArrayList<SolarEntries> se)
||                  ArrayList<SolarEntries> readBin(String fileName)
||
||
++-----------------------------------------------------------------------*/

public class Prog1A {

    // stores the max length of each string field
    private static int solarCODLen;
    private static int projectNameLen;
    private static int stateLen;

    /*---------------------------------------------------------------------
    |  Method main(String[] args)
    |
    |  Purpose:  Takes a csv file as an argument and turns it into a bin
    |            file
    |
    |  Pre-condition:  The file is valid
    |                  
    |
    |  Post-condition: The file is closed
    |
    |  Parameters:
    |          args -- The command line argument, 0 should be the file name
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    public static void main(String[] args) {

        String fileName = args[0];

        ArrayList<SolarEntries> solarEntries = new ArrayList<>();

        File input = new File(fileName + ".csv");

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(input));
        } catch (IOException e) {
            System.out.println("Could not open file. Exiting.");
            System.exit(-1);
        }

        try {
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                if (line.charAt(0) != ',') { // skip empty EIA ID entries
                    SolarEntries d = parseLine(line);
                    if (d != null) {
                        solarEntries.add(d);
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error parsing line");
            System.exit(-1);
        }
        Collections.sort(solarEntries);
        writeToBin(fileName, solarEntries);

        System.out.println("Successfuly wrote to bin!");
    } // main()

    /*---------------------------------------------------------------------
     |  Method parseLine (String line)
     |
     |  Purpose:  Takes the current line and splits it by comma
     |            and fills in the field of the current SolarEntries
     |            object. Any bad data such as no EIA ID, more than 9
     |            fields, etc, will be ignored and returned as NULL
     |
     |  Pre-condition:  The provided line is just the current line of
     |                  the csv file.
     |                  
     |
     |  Post-condition: A SolarEntries object with all the fields filled
     |                  in is returned or null is returned
     |
     |  Parameters:
     |          line -- The current line in the CSV file
     |
     |  Returns: An entry representing the current line in the file 
     *-------------------------------------------------------------------*/
    private static SolarEntries parseLine(String line) {

        // SKIP missing EIA ID, fill empty string values with whitespace, fill empty
        // doubles with 0.0
        SolarEntries newEntry = new SolarEntries();
        // Order: eaid, project name, solar cod, state, lat, lon, avg ghi, solar cap ac,
        // solar cap dc
        // do not split with comma in ""
        String[] fields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        if (fields.length > 9) { // skip bad entries
            return null;
        }

        // EIA ID
        if (!fields[0].matches("[0-9]+")) { // make sure EIA ID is int
            return null;
        }
        newEntry.setEIAID(Integer.valueOf(fields[0]));

        // Project Name
        newEntry.setProjectName(fields[1]);
        // compare current length for this string field with the highest
        // entry seen so far
        if (fields[1].length() > projectNameLen) {
            projectNameLen = fields[1].length();
        }

        // Solar Cod
        newEntry.setSolarCOD(fields[2]);
        if (fields[2].length() > solarCODLen) {
            solarCODLen = fields[2].length();
        }
        // state
        newEntry.setState(fields[3]);
        if (fields[3].length() > stateLen) {
            stateLen = fields[3].length();
        }
        // lat
        if (fields[4].isEmpty()) {
            newEntry.setLatitude(0);
        } else {
            newEntry.setLatitude(Double.valueOf(fields[4]));
        }
        // lon
        if (fields[5].isEmpty()) {
            newEntry.setLongitude(0);
        } else {
            newEntry.setLongitude(Double.valueOf(fields[5]));
        }
        // average ghi
        if (fields[6].isEmpty()) {
            newEntry.setAvgGHI(0);
        } else {
            newEntry.setAvgGHI(Double.valueOf(fields[6]));
        }
        // solar cap (ac)
        if (fields[7].isEmpty()) {
            newEntry.setSolarCapacityAC(0);
        } else {
            newEntry.setSolarCapacityAC(Double.valueOf(fields[7]));
        }

        // solar cap (dc)
        if (fields[8].isEmpty()) {
            newEntry.setSolarCapacityDC(0);
        } else {
            newEntry.setSolarCapacityDC(Double.valueOf(fields[8]));
        }

        return newEntry;
    } // parseLine

    /*---------------------------------------------------------------------
     |  Method writeToBin (fileName, se)
     |
     |  Purpose:  Create and populate a binary file which shares the same
     |            name as the csv file that contains the data from the 
     |            supplied csvContent list in the same order. The EIA ID
     |            will be stored as a 4 byte int, the strings will be stored
     |            as n bytes for each where n is the length of the longest
     |            entry for that field and the rest of the fields will be
     |            stored as 8 byte doubles 
     |
     |  Pre-condition:  The given fileName string is just the filename, with
     |                  no extension.  The file is in the current directory.
     |
     |  Post-condition: The file is created in the current directory.
     |                  The file has the same content as the csvContent list.
     |                  The records all have the same length.
     |
     |  Parameters:
     |      fileName -- file name only of the binary file, no extension 
     |      se       -- An ArrayList of SolarEntries objects, containing
     |                    the data from the given CSV file.
     |
     |  Returns: None.
     *-------------------------------------------------------------------*/
    private static void writeToBin(String fileName,
            ArrayList<SolarEntries> se) {
        File fileRef = null;
        RandomAccessFile binFile = null;

        try { // remove bin file if already exists
            fileRef = new File("./" + fileName + ".bin");
            if (fileRef.exists()) {
                fileRef.delete();
            }
        } catch (Exception e) {
            System.out.println("I/O Error: Something went wrong with the "
                    + "deletion of previous file.");
            System.exit(-1);
        }

        try {
            binFile = new RandomAccessFile(fileRef, "rw");
        } catch (IOException e) {
            System.out.println("I/O Error: Something went wrong creating "
                    + "RandomAccessFile object.");
            System.exit(-1);
        }

        // Write length of string fields to first line in file
        // as well as total number of entries
        try {
            binFile.seek(0);
            binFile.writeInt(se.size());
            binFile.writeInt(projectNameLen);
            binFile.writeInt(solarCODLen);
            binFile.writeInt(stateLen);
        } catch (IOException e) {
            System.out.println("I/O Error: Could not write to file");
            System.exit(-1);
        }

        for (SolarEntries s : se) { // format each entry to be written to bin
            s.dumpObject(binFile, projectNameLen, solarCODLen, stateLen);
        }

        try { // close file
            binFile.close();
        } catch (IOException e) {
            System.out.println("I/O Error: Could not close file.");
            System.exit(-1);
        }

    } // writeToBin

} // class Prog1A
