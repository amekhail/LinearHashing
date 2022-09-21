/*
 * Entry.java -- A container for the entries which
 * will be used to store the id and pointer in the binary
 * file. 
 * 
 * Author: Adam Mekhail
 * First Version: 2022-09-12
 * 
 * 
 */
import java.io.IOException;
import java.io.RandomAccessFile;

public class Entry {
    /*+----------------------------------------------------------------------
    ||  Class Entry
    ||
    ||         Author:  Adam Mekhail
    ||
    ||         Purpose: A container to store the EIA ID number and pointer to 
    ||                  the location in the binary file for this entry
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
    ||    Constructors: Enrty(int key, int value)
    ||
    ||   Class Methods: None
    ||
    ||   Inst. Methods: int getID()
    ||                  int getPointer()
    ||                  void dumpEntry(RandomAccessFile stream)
    ||
    ++-----------------------------------------------------------------------*/
    private int eiaID;      // The EIA ID
    private int pointer;    // The location (bytes) in the binary file

    /* Getter methods */
    public int getID() { return eiaID; }
    public int getPointer() { return pointer; }
    
    /* Constructor */
    public Entry(int key, int value) {
        eiaID = key;
        pointer = value;
    }

    /*---------------------------------------------------------------------
    |  Method dumpEntry(RandomAccessFile stream)
    |
    |  Purpose: Writes the EIA ID and the pointer to the file stream
    |
    |  Pre-condition:  The file is already open
    |                  
    |
    |  Post-condition: The file remains open
    |
    |  Parameters:
    |        stream -- The binary file stream
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    public void dumpEntry(RandomAccessFile stream) {
        try {
            stream.writeInt(this.eiaID);
            stream.writeInt(this.pointer);
        } catch (IOException e) {
            System.out.println("I/O Error: Could not write to file");
            System.exit(-1);
        }
    }
}