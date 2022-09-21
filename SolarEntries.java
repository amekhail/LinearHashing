/*
 * SolarEnteries.java -- A container for each solar entry which will either
 * be read from a csv file or a bin file. There will be a total of 9 different
 * fields for each entry which are:
 *  EIA ID               -> int
 *  Project Name         -> string
 *  Solar COD            -> string
 *  State                -> string
 *  Latitude             -> double
 *  Longitude            -> double
 *  Avg GHI              -> double
 *  Solar Capacity MW-DC -> double
 *  Solar Capacity MW-AC -> double
 * 
 * These fields will be read by a provided stream which will contain a pointer
 * to the current entry in the file. For the string fields, they will be padded
 * to the length of the longest field for the enteries to create a uniform size
 * for all enteries.
 * 
 * 
 * Author: Adam Mekhail
 * First Version: 2022-08-23
 * 
 * 
 */

import java.io.*;

/*+----------------------------------------------------------------------
||  Class DataRecord
||
||         Author:  Adam Mekhail
||
||         Purpose: To have a class hold all the data for a solar entry.
||                  The data being the EIA ID, Project name, Solar COD,
||                  State, Latitute, Longitude, Average GHI, Solar Capacity
||                  in DC as well as AC. Also contains methods for parsing
||                  the data from a csv file to be stored here as well as
||                  parsing a binary file to be read back as 
||                  ASCII characters.
||
||   Inherits from: None.
||
||      Interfaces: Comparable in order to use Collections(Sort)
||                  for sorting an arraylist of SolarEnteries objects
||                  based on EIA ID numbers
||
|+-----------------------------------------------------------------------*
||
||       Constants: None.
|| 
|+-----------------------------------------------------------------------*
||
||    Constructors: the default constructor which will initilize all fields
||                  with default values
||
||   Class Methods: None.
||
||   Inst. Methods: int getEIAID() 
||                  String getProjectName() 
||                  String getSolarCOD() 
||                  String getState() 
||                  double getLatitude() 
||                  double getLongitude() 
||                  double getAvgGHI() 
||                  double getSolarCapacityDC() 
||                  double getSolarCapacityAC() 
||                    void setEIAID(int id) 
||                    void setProjectName(String name) 
||                    void setSolarCOD(String newSolarCOD) 
||                    void setState(String s) 
||                    void setLatitude(double lat) 
||                    void setLongitude(double lon) 
||                    void setAvgGHI(double GHI)
||                    void setSolarCapacityDC(double cap) 
||                    void setSolarCapacityAC(double cap)
||
||                    void dumpObject(RandomAccessFile stream, 
||                                    int nameLen, int codLen, 
||                                    int stateLen)
||                    void fetchObject(RandomAccessFile stream, 
||                                     int nameLen, int codLen, 
||                                     int stateLen)
||                    int getSolarRecordLength(int nameLen, int codLen, 
||                                             int stateLen)
||                    int compareTo(SolarEntries o)
++-----------------------------------------------------------------------*/
public class SolarEntries implements Comparable<SolarEntries> {
    
    // Fields for DataRecord class. Contains 9 fields for each solar plant
    private    int EIAID;           // The EIAID

    private String projectName;     // The project name
    private String solarCOD;        // The Solar COD
    private String state;           // The sate the project is in

    private double latitude;        // The latitude
    private double longitude;       // The longitude
    private double avgGHI;          // The average GHI
    private double solarCapacityDC; // The Solar Capacity in DC
    private double solarCapacityAC; // The Solar Capacity in DC

    // 'Getters' for the data field values
    public int    getEIAID() { return (EIAID); }
    
    public String getProjectName() { return (projectName); }
    public String getSolarCOD() { return (solarCOD); }
    public String getState() { return (state); }

    public double getLatitude() { return (latitude); }
    public double getLongitude() { return(longitude); }
    public double getAvgGHI() { return (avgGHI); }
    public double getSolarCapacityDC() { return (solarCapacityDC); }
    public double getSolarCapacityAC() { return (solarCapacityAC); }

    // 'Setters' for the data field values
    public void setEIAID(int id) { EIAID = id; }

    public void setProjectName(String name) { projectName = name; }
    public void setSolarCOD(String newSolarCOD) { solarCOD = newSolarCOD; }
    public void setState(String s) { state = s; }

    public void setLatitude(double lat) { latitude = lat; }
    public void setLongitude(double lon) { longitude = lon; }
    public void setAvgGHI(double GHI) { avgGHI = GHI; }
    public void setSolarCapacityDC(double cap) { solarCapacityDC = cap; }
    public void setSolarCapacityAC(double cap) { solarCapacityAC = cap; }

       /*---------------------------------------------------------------------
        |  Method dumpObject(stream, nameLen, codLen, stateLen)
        |
        |  Purpose:  Writes the contents of the entry in the CSV file
        |            provided by a RandomAcessFile object reference. Primitive
        |            types like ints and doubles are written directly while
        |            strings are converted to the maximum size and then written
        |            meaning most strings will be padded with whitespace to
        |            keep a uniform size for all strings. Unicode is not 
        |            supported.
        |
        |  Pre-condition:  Fields have been populated, stream is writeable,
        |                  file pointer is positioned to new data's location,
        |                  len fields are provided and represent the length of
        |                  the longest entry for the field.
        |
        |  Post-condition: Stream contains field data in sequence, file pointer
        |                  is left at the end of the written data.
        |
        |  Parameters:
        |      stream -- This is the stream object representing the data file
        |                to which the data is being written.
        |     nameLen -- The length of the longest name for all enteries
        |     codeLen -- The length of the lonest solar cod for all enteries
        |    stateLen -- The length of the longest state name for all entereies
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
    public void dumpObject(RandomAccessFile stream, int nameLen, 
                           int codLen, int stateLen) {

        // Padding all string fields for the entry
        StringBuffer name = new StringBuffer(projectName);
        StringBuffer cod = new StringBuffer(solarCOD);
        StringBuffer s = new StringBuffer(state);

        try {
            stream.writeInt(EIAID);
            name.setLength(nameLen); // Pads the right with nulls
            cod.setLength(codLen);
            s.setLength(stateLen);
            stream.writeBytes(name.toString()); // Only ASCII, no unicode
            stream.writeBytes(cod.toString());
            stream.writeBytes(s.toString());
            stream.writeDouble(latitude);
            stream.writeDouble(longitude);
            stream.writeDouble(avgGHI);
            stream.writeDouble(solarCapacityDC);
            stream.writeDouble(solarCapacityAC);
        } catch (IOException e) {
            System.out.println("I/O Error: Could not write to file");
            System.exit(-1);
        }
    } // dumpObject

       /*---------------------------------------------------------------------
        |  Method fetchObject(stream, nameLen, codLen, stateLen)
        |
        |  Purpose:  Read the entries contents from the bin file provided
        |            by RandomAccessFile stream starting at the current pos
        |            in the file. Each string field will be read into an
        |            array of bytes of a predetermined size.
        |
        |  Pre-condition:  Stream is readable, file pointer is positioned
        |                  to the record's first field's first byte, 
        |                  len fields are provided and represent the length
        |                  of the longest entry for the field.
        |
        |  Post-condition: Object fields are populated, file pointer
        |                  is left at the end of the read data
        |
        |  Parameters:
        |      stream -- This is the stream object representing the data file
        |                from which the data is being read.
        |     nameLen -- The length of the longest name for all enteries
        |     codeLen -- The length of the lonest solar cod for all enteries
        |    stateLen -- The length of the longest state name for all entereies
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
    public void fetchObject(RandomAccessFile stream, int nameLen, 
                            int codLen, int stateLen) {
        
        // using byte for ASCII only
        // setting predetermined length for each string field                        
        byte[] name = new byte[nameLen];
        byte[] cod = new byte[codLen];
        byte[] s = new byte[stateLen];

        try {
            
            EIAID = stream.readInt();
            
            // reads all the bytes needed
            stream.readFully(name); 
            stream.readFully(cod);
            stream.readFully(s);

            // turns bytes into strings
            projectName = new String(name);
            solarCOD = new String(cod);
            state = new String(s);

            latitude = stream.readDouble();
            longitude = stream.readDouble();
            avgGHI = stream.readDouble();
            solarCapacityDC = stream.readDouble();
            solarCapacityAC = stream.readDouble();

        } catch (IOException e) {
            System.out.println("I/O Error: Could not read from file");
            System.exit(-1);
        }
    } // fetchObject


    /*---------------------------------------------------------------------
        |  Method getSolarRecordLength(stream, nameLen, codLen, stateLen)
        |
        |  Purpose: Calculates the total size of the data record since the
        |           size will be dependent on the length of the strings.
        |           The size will be calculated by adding the total number
        |           of bytes (chars) for each string and adding the sizeof
        |           the doubles (*8) and the size of an integer (4 bytes)
        |           to get the total size.
        |
        |  Pre-condition: The project name, solar cod, and state lengths are
        |                 already calculated
        |
        |  Post-condition: The size of the Solar entries for the file will
        |                  be provided
        |
        |  Parameters:
        |      stream -- This is the stream object representing the data file
        |                from which the data is being read.
        |     nameLen -- The length of the longest name for all enteries
        |     codeLen -- The length of the lonest solar cod for all enteries
        |    stateLen -- The length of the longest state name for all entereies
        |
        |  Returns:  The size of the solar records for the given file
        *-------------------------------------------------------------------*/
    public static int getSolarRecordLength(int nameLen, int codLen, int stateLen) {
        // double -> 8 bytes, int -> 4 bytes
        // string length varies by longest length of inputed data
        return (8 * 5) + (nameLen + codLen + stateLen) + 4;
    } // getSolarRecordLength

    /*---------------------------------------------------------------------
        |  Method compareTo(SolarEntries o) 
        |
        |  Purpose: Compares two SolarEntries objects by looking at their 
        |           EIA ID to determine which one is larger which will be
        |           used for sorting.
        |
        |  Pre-condition: Both this and o are SolarEntries and they have
        |                 Valid EIA IDs
        |
        |  Post-condition: 1, -1, or 0 wil be returned depending on if this
        |                  object is greater, less than, or equal to the other
        |                  objects EIA ID
        |
        |  Parameters:
        |           o -- The other SolarEntries object to be compared to
        |
        |  Returns:  If this Solarentries objects EIA ID number is greater 
                     than, less than, or equal to.
        *-------------------------------------------------------------------*/
    @Override
    public int compareTo(SolarEntries o) {
        if (this.EIAID > o.EIAID) {
            return 1;
        }
        else if (this.EIAID < o.EIAID) {
            return -1;
        } else {
            return 0;
        }
    }

} // class DataRecord