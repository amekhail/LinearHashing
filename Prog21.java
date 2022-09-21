/*
 * Prog21.java -- Reading from disk is very taxing. It takes a lot of time
 * and pulling only little data at a time is not possible. An entire block
 * of data will be pulled because, chances are, if you want to read something,
 * you probably want to read what comes next. Using this, creating an index
 * which uses buckets to represent blocks will achieve the same thing. Using
 * the EIA ID as a key, the block it should reside in will be calculated using
 * the formula: 
 * block = key % (2^(H + 1)) will point you to the start of the block.
 * Iterating through the block, it can be determined if the entry resides there
 * or not and if so, the pointer to where it is in the binary file can easily 
 * allow access to the entire entry within the file.
 * 
 * Author: Adam Mekhail
 * First Version: 2022-09-12
 * 
 * 
 */

import java.io.*;

public class Prog21 {
    /*+----------------------------------------------------------------------
    ||  Class Prog21
    ||
    ||         Author:  Adam Mekhail
    ||
    ||         Purpose: To read a .bin file and store each EIA ID as a key in
    ||                  the index file created by this program and the location
    ||                  in bytes of the entry corresponding with the EIA ID as
    ||                  the value for quick look up. There is a blocking factor
    ||                  of 20 for the table, so when a table becomes full, all
    ||                  the data is redistributed using the same formula used to
    ||                  insert the entries to the table. The formula being:
    ||                  location = key % (2^(H + 1))
    ||                  Each time the table expands, the H value (initially at 0)
    ||                  increases by 1, and the number of buckets in the table gets
    ||                  doubled. This is done so that when query for a specific key,
    ||                  rather than pulling the entire table from memory, only the 
    ||                  block is retrieved from memory and it can be quickly
    ||                  if the key is in the table or not, and if so, the location
    ||                  in the .bin file can easily be accessed.
    ||
    ||   Inherits from: None.
    ||
    ||      Interfaces: None.
    ||
    |+-----------------------------------------------------------------------*
    ||
    ||       Constants: BLOCKING_FACTOR: the number of elements in each bucket
    ||                                   does not change at all during the 
    ||                                   duration of the program
    || 
    |+-----------------------------------------------------------------------*
    ||
    ||    Constructors: Default Constructor
    ||
    ||   Class Methods: None
    ||
    ||   Inst. Methods: void createIndex(RandomAccessFile binFile)
    ||                  void insert(Entry se)
    ||                  void resize()
    ||                  void initBuckets(int startLoc)
    ||                  void reInsert(Entry se, int newLoc)
    ||                  int getHashCode(int k)
    ||
    ++-----------------------------------------------------------------------*/


    // Constant field for blocking factor. Does not change at all
    private static final int BLOCKING_FACTOR = 20;
    // Store the hVal and number of buckets for the table
    // hVal increases by 1 each time the table needs to be expanded
    // buckets are doubled each time the table needs to be expanded
    private static int hVal;
    private static int numBuckets;
    // Stores the max length of each string field
    private static int solarCODLen;
    private static int projectNameLen;
    private static int stateLen;
    private static int numRecords;

    private static RandomAccessFile indexFile;

    /*---------------------------------------------------------------------
    |  Method main(String[] args)
    |
    |  Purpose:  Takes the .bin file as arg[0] and stores each entries EIA ID
    |            as the key and the location in the file as the value in the
    |            created index file for quick look up. Writes the index file
    |            and saves it as lhl.idx in the current directory
    |
    |  Pre-condition:  The file is valid
    |                  
    |
    |  Post-condition: Both files are closed
    |
    |  Parameters:
    |          args -- The command line argument, 0 should be the file name
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    public static void main(String args[]) {
        String fileName = args[0];

        RandomAccessFile binFile = null;
        indexFile = null;
        File fileRef = null;
        // Delete file if already exists
        try {
            fileRef = new File("./lhl.idx");
            if (fileRef.exists()) {
                fileRef.delete();
            }
        } catch (Exception e) {
            System.out.println("Error: Something went wrong deleting the previous .idx file");
            System.exit(-1);
        }

        // create the file
        try {
            indexFile = new RandomAccessFile(fileRef, "rw");
        } catch (IOException e) {
            System.out.println("Error: Could not create the .idx file.");
            System.exit(-1);
        }

        try {
            binFile = new RandomAccessFile("./" + fileName, "r");
        } catch (IOException e) {
            System.out.println("Error: Could not open file.");
            System.exit(-1);
        }

        // get length of all strings and total number of records in the file
        try {
            binFile.seek(0);
            numRecords = binFile.readInt();
            projectNameLen = binFile.readInt();
            solarCODLen = binFile.readInt();
            stateLen = binFile.readInt();
        } catch (IOException e) {
            System.out.println("Error: Could not read .bin file");
            System.exit(-1);
        }

        hVal = 0; // initial value is 0
        numBuckets = 2; // initial num buckets is 2
        initBuckets(0);
        createIndex(binFile);

        // close the file
        try {
            binFile.close();
        } catch (IOException e) {
            System.out.println("Error: Could not close the .bin file.");
            System.exit(-1);
        }

        System.out.println("Successfuly wrote lhl.idx");
    } // main()

    /*---------------------------------------------------------------------
    |  Method createIndex(RandomAccessFile binFile)
    |
    |  Purpose:  Reads the bin file and creates an entry object for each
    |            entry. Stores the EIA ID as the key and the pointer to 
    |            the location in the file as the value. Using the 
    |            getHashCode() function, it finds the block the entry should
    |            go in and uses the insert() function to insert the key.
    |
    |  Pre-condition:  The file is valid
    |                  
    |
    |  Post-condition: The bin file remains open, index file is closed
    |
    |  Parameters:
    |       binFile -- The binary file stream
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    private static void createIndex(RandomAccessFile binFile) {
        int startLocation;

        try {
            for (int i = 0; i < numRecords; i++) {
                // get pointer for entry i in the file
                startLocation = (4 * 4)
                        + i * (SolarEntries.getSolarRecordLength(
                                projectNameLen, solarCODLen, stateLen));

                // go to location and save EIA id and location
                binFile.seek(startLocation);
                int id = binFile.readInt();
                int pointer = startLocation;

                // create entry
                Entry se = new Entry(id, pointer);
                insert(se);

            }
            // write h val to bottom of file:
            indexFile.seek(indexFile.length());
            indexFile.writeInt(hVal);
        } catch (IOException e) {
            System.out.println("Error: Could not read .bin file");
            System.exit(-1);
        }

        // close the idx file
        try {
            indexFile.close();
        } catch (IOException e) {
            System.out.println("Error: Could not close the .idx file.");
            System.exit(-1);
        }
    } // createIndex()

     /*---------------------------------------------------------------------
    |  Method getHashCode(int k)
    |
    |  Purpose:  Computes the hash code for a provided key to determine which
    |            block the entry will go in. This is determined by the H value
    |            because as the table expands, the h value will increase 
    |            meaning that the bucket a entry would go into at H = 0
    |            might not be the same at H = 1
    |
    |  Pre-condition:  The arg 'k' is the EIA ID number
    |                  
    |
    |  Post-condition: The integer returned is a valid bucket that exists in
    |                  the table
    |
    |  Parameters:
    |            k -- The key which should be the EIA ID
    |
    |  Returns: An integer which is the bucket the entry will go into
    *-------------------------------------------------------------------*/
    private static int getHashCode(int k) {
        return k % (int) Math.pow(2, hVal + 1);
    } // getHashCode()

    /*---------------------------------------------------------------------
    |  Method insert(Entry se)
    |
    |  Purpose:  Inserts an entry by getting the key from the entry and passing
    |            it as the parameter for insert(). It will return the bucket.
    |            Multiplying this by the blocking factor will get the starting
    |            index for the bucket. If the bucket is full, the table will be
    |            resized and it will attempt to try adding the entry again.
    |
    |  Pre-condition:  The entry has a valid EIA ID
    |                  
    |
    |  Post-condition: The buckets will remain appropriate sizes, not over
    |                  20 elements
    |
    |  Parameters:
    |           se -- The solar entry to be added to the table
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    private static void insert(Entry se) {
        // getHashcode returns the bucket
        // multiplying with blocking factor gets to start of bucket
        // have to multiply by size of key and value (4 bytes each)
        int loc = getHashCode(se.getID()) * (BLOCKING_FACTOR * (4 + 4));
        try {
            indexFile.seek(loc);
            int i = 0;
            boolean isFound = false;
            while (i < BLOCKING_FACTOR && !isFound) {
                int currentLoc = (int) indexFile.getFilePointer();
                Entry temp = new Entry(indexFile.readInt(), indexFile.readInt());
                if (temp.getID() == -1) {
                    indexFile.seek(currentLoc);
                    indexFile.writeInt(se.getID());
                    indexFile.writeInt(se.getPointer());
                    isFound = true;
                }
                i++;
            }
            if (!isFound) {
                resize();
                insert(se);
            }
        } catch (IOException e) {
            System.out.println("Error: Could write to .idx file");
            System.exit(-1);
        }
    } // insert()

    /*---------------------------------------------------------------------
    |  Method resize()
    |
    |  Purpose:  Resizes the table by doubling the number of buckets and
    |            incrementing the H value by 1.
    |
    |  Pre-condition:  At least one bucket is full
    |                  
    |
    |  Post-condition: Each bucket will contain no more than 20 elements
    |
    |  Parameters: None
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    private static void resize() {
        hVal++; // increase hVal by 1
        numBuckets *= 2; // double number of buckets
        try {
            int currentEnd = (int) indexFile.length();
            indexFile.seek(currentEnd);
            initBuckets(currentEnd); // append new buckets to end of file
            indexFile.seek(0);
            long curLoc = 0;
            while (curLoc < currentEnd) { // reinsert all elements
                indexFile.seek(curLoc);
                Entry se = new Entry(indexFile.readInt(), indexFile.readInt());
                if (se.getID() != -1) {
                    reInsert(se, currentEnd);
                }
                curLoc += (4 + 4);
            }
            // move all reinserted elements up in the index file
            curLoc = 0;
            while (currentEnd < indexFile.length()) {
                indexFile.seek(currentEnd);
                Entry se = new Entry(indexFile.readInt(), indexFile.readInt());
                indexFile.seek(curLoc);
                indexFile.writeInt(se.getID());
                indexFile.writeInt(se.getPointer());
                curLoc += (4 + 4);
                currentEnd += (4 + 4);
            }
            // truncate the file to remove old buckets
            indexFile.setLength(numBuckets * BLOCKING_FACTOR * (4 + 4));
        } catch (IOException e) {
            System.out.println("Error: Could not resize index file");
            System.exit(-1);
        }
    } // resize()

    /*---------------------------------------------------------------------
    |  Method initBuckets()
    |
    |  Purpose:  Takes a location in the file and creates n empty buckets
    |            depending on what the number of buckets currently is
    |
    |  Pre-condition:  Location is not in the middle of data, only top of
    |                  file for starting or end of file
    |
    |  Post-condition: Each bucket will be empty
    |
    |  Parameters: 
    |      startLoc -- The starting location for the new buckets
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    private static void initBuckets(int startLoc) {
        try {
            indexFile.seek(startLoc);
            for (int i = 0; i < numBuckets * BLOCKING_FACTOR; i++) {
                indexFile.writeInt(-1);
                indexFile.writeInt(-1);
            }
        } catch (IOException e) {
            System.out.println("Error increasing number of buckets");
            System.exit(-1);
        }
    } // initBuckets

    /*---------------------------------------------------------------------
    |  Method reInsert()
    |
    |  Purpose:  Re-inserts an element by taking an offset as the parameter
    |            to place into the appropriate bucket
    |
    |  Pre-condition:  Index file must be resized before calling this method
    |
    |  Post-condition: All elements will be reinserted based on new h values
    |
    |  Parameters: 
    |           se -- The element to be inserted
    |       newLoc -- The starting location for the new buckets
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    private static void reInsert(Entry se, int newLoc) {
        int loc = getHashCode(se.getID()) * (BLOCKING_FACTOR * (4 + 4)) + newLoc;

        try {
            indexFile.seek(loc);
            int i = 0;
            boolean isFound = false;
            while (i < BLOCKING_FACTOR && !isFound) {
                int currentLoc = (int) indexFile.getFilePointer();
                Entry temp = new Entry(indexFile.readInt(), indexFile.readInt());
                if (temp.getID() == -1) {
                    indexFile.seek(currentLoc);
                    indexFile.writeInt(se.getID());
                    indexFile.writeInt(se.getPointer());
                    isFound = true;
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error: Problem moving entries in index file.");
            System.exit(-1);
        }
    }
} // class Prog21
