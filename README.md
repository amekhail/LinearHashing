# LinearHashing
---
## How it works
**Prog1A.java** takes the csv file as a command line argument and produces a file of the same name but as a binary file. The purporse of using a binary file is to reduce the file size as well as uniform sizes for each field in the entry.

**Prog21.java** takes the binary file and produces an index file which will store the EIA ID number as the key and the location of the the entry in the binary file. The location will be a pointer which will represent the byte the entry starts.

**Prog22.java** takes the binary file and the index file created in Prog21 and allows for querying of the entries

---

## Files:
**2021-utility-scale-solar-plants.csv**: The entries for different solar plants which hold the EIA ID, Project name, solar COD date, state, location, and power output
**out.txt**: Represents out the index file will look
