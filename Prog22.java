/*
 * Prog22.java -- Allows the user to query the binary file by providing
 * an EIA ID number which will be searched through the index file using 
 * linear hashing. The index file will contain a pointer or location to
 * where the entry is in the binary file and if there is an entry, 
 * the entry will be printed.
 * 
 * Author: Adam Mekhail
 * First Version: 2022-09-12
 * 
 * 
 */

import java.io.*;
import java.util.Scanner;

class Prog22 {
    /*+----------------------------------------------------------------------
    ||  Class Prog22
    ||
    ||         Author:  Adam Mekhail
    ||
    ||         Purpose: To read the contents of the .idx file that was created
    ||                  in part 1 and allow the user to provide as many keys as
    ||                  they wish to query for a solar entry. If a solar entry
    ||                  exists, it will display the EIA ID, Project ID, and the
    ||                  Solar Cap AC. If not, it will display that the entry was
    ||                  not found or does not exist.
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
    ||   Inst. Methods: RandomAccessFile openFile(String name)
    ||                  void closeFile(RandomAccessFile filePtr, String name)
    ||                  void printRecord(RandomAccessFile binFile, int ptr)
    ||                  int seekRecord(RandomAccessFile idxFile, int id)
    ||                  int getHashCode(int k)
    ||
    ++-----------------------------------------------------------------------*/


    private static int solarCODLen;
    private static int projectNameLen;
    private static int stateLen;
    private static int numRecords;

    private static int hVal;

    /*---------------------------------------------------------------------
    |  Method main(String args[]) 
    |
    |  Purpose:  Takes the index file and the binary file as arguments in this
    |            order and prompts the user to enter EIA ID's to be searched.
    |            if found, it will display the EIA ID, project name, and solar
    |            Cap AC to the screen, if not it will let the user know the
    |            records aren't found
    |
    |  Pre-condition:  File names are valid
    |                  
    |  Post-condition: Both files are closed
    |
    |  Parameters:
    |         args -- The command line arguments which contain the file
    |                 names
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    public static void main(String args[]) {
        String idxFileName = args[0];
        String binFileName = args[1];

        // open the files
        RandomAccessFile binFile = openFile(binFileName);
        RandomAccessFile indexFile = openFile(idxFileName);

        // read the Hval from bottom of index file
        try {
            indexFile.seek(indexFile.length() - 4);
            hVal = indexFile.readInt();
        } catch (IOException e) {
            System.out.println("Error: Could not read the H value from .idx file");
            System.exit(-1);
        }

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

        Scanner scanner = new Scanner(System.in);
        System.out.println(numRecords + " records found.");
        System.out.println("Enter the EIA ID to search. To end the search, type:'-1': ");
        String line = scanner.next();
        while (!line.equals("-1")) {
            try {
                int id = Integer.parseInt(line);
                int ptr = seekRecord(indexFile, id);
                if (ptr == -1) {
                    System.out.println("The target value " + id + " was not found.");
                } else {
                    printRecord(binFile, ptr);
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter the integer represting the EIA ID number.");
            }
            // prompt user again
            System.out.println("Enter the EIA ID to search. To end the search, type:'-1': ");
            line = scanner.next();
        }
        scanner.close();

        // close the files
        closeFile(binFile, binFileName);
        closeFile(indexFile, idxFileName);
    } // main()

     /*---------------------------------------------------------------------
    |  Method openFile(String name)
    |
    |  Purpose: Takes the file name as a parameter and opens the 
    |           Random access file returning the pointer to the file
    |
    |  Pre-condition: The name is of a valid file
    |                  
    |
    |  Post-condition: The file remains open
    |
    |  Parameters:
    |         name -- The Name of the file to be opened
    |
    |  Returns: Pointer to RandomAccessFile object
    *-------------------------------------------------------------------*/
    private static RandomAccessFile openFile(String name) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(name, "r");
        } catch (IOException e) {
            System.out.println("Error: Could not open file: " + name);
            System.exit(-1);
        }

        return raf;
    } // openFile()

    /*---------------------------------------------------------------------
    |  Method closeFile(RandomAccessFile filePtr, String name)
    |
    |  Purpose:  Closes the file by taking the file pointer and name as
    |            arguments. If the file cannot be closed, it will display
    |            the name of the file that could not be closed
    |
    |  Pre-condition:  The file is already open
    |                  
    |
    |  Post-condition: The file is closed
    |
    |  Parameters:
    |       filePtr -- The binary file stream
    |          name -- The name of the file
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    private static void closeFile(RandomAccessFile filePtr, String name) {
        try {
            filePtr.close();
        } catch (IOException e) {
            System.out.println("Error: Could not close the file " + name);
            System.exit(-1);
        }
    } // closeFile()

    /*---------------------------------------------------------------------
    |  Method seekRecord(RandomAccessFile idxFile, int id)
    |
    |  Purpose:  Searches the index file for a record with the proved ID as
    |            the argument. If the record is found, the pointer to where
    |            it is in the .bin file will be returned, otherwise -1 will
    |            be returned
    |
    |  Pre-condition:  id is the EIA id number and id is not negative
    |                  
    |
    |  Post-condition: The location of the record in the .bin file will be
    |                  returned and both files will remain open
    |
    |  Parameters:
    |       idFile -- The indexFile file stream
    |           id -- The key which should be the EIA ID
    |           
    |  Returns: The pointer to the location of the entry or -1 if not found
    *-------------------------------------------------------------------*/
    private static int seekRecord(RandomAccessFile idxFile, int id) {
        int loc = getHashCode(id) * 20 * 8;
        boolean isFound = false;
        try {
            idxFile.seek(loc);
            for (int i = 0; i < 20 && isFound == false; i++) {
                int eiaID = idxFile.readInt();
                int ptr = idxFile.readInt();
                loc = (int) idxFile.getFilePointer();
                if (eiaID == id) {
                    return ptr;
                }
            }

        } catch (IOException e) {
            System.out.println("Error: Could not read .idx file");
            System.exit(-1);
        }
        return -1;
    } // seekRecord()

    /*---------------------------------------------------------------------
    |  Method getHashCode(int k)
    |
    |  Purpose:  Computes the hash code for a provided key to determine which
    |            block the entry resides in. This is determined by the H value
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
    |  Returns: An integer which is the bucket the entry will be in
    *-------------------------------------------------------------------*/
    private static int getHashCode(int k) {
        return k % (int) Math.pow(2, hVal + 1);
    } // getHashCode()

    /*---------------------------------------------------------------------
    |  Method printRecord(RandomAccessFile binFile, int ptr)
    |
    |  Purpose:  Goes to the location in the .bin file for the entry
    |            which is passed as an argument and prints out the
    |            EIA ID number, the name, and Solar Cap AC
    |
    |  Pre-condition:  The file is open and ptr is valid 
    |                  
    |  Post-condition: The file remains open
    |
    |  Parameters:
    |       binFile -- The binary file stream
    |           ptr -- The location in file
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
    private static void printRecord(RandomAccessFile binFile, int ptr) {
        int solarCapACPointer = (ptr + 4 + projectNameLen +
                solarCODLen + stateLen) + (8 * 3);
        int id;
        String name;
        byte[] nameBytes = new byte[projectNameLen];
        double capAC;

        try {
            binFile.seek(ptr);
            id = binFile.readInt();
            binFile.readFully(nameBytes);
            name = new String(nameBytes);

            binFile.seek(solarCapACPointer);
            capAC = binFile.readDouble();

            System.out.println("[" + id + "] [" + name + "] [" + capAC + "]");

        } catch (IOException e) {
            System.out.println("I/O Error: Could not read from file");
            System.exit(-1);
        }
    } // printRecord()
} // Class Prog22