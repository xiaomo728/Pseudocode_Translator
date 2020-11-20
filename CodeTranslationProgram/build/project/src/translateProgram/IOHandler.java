
package translateProgram;

import translateUnits.Function;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

public class IOHandler {
	// Class to read or write files by IO stream.

	// function to read contents in files and take them into ArrayList data structure.
    public static ArrayList<String> readData(String path) {
        ArrayList<String> lines = new ArrayList<String>();

        try {
            FileInputStream inputStream = new FileInputStream(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // read line by line.
            String line = null;
            while((line = bufferedReader.readLine()) != null)
            {
                lines.add(line);
            }

            inputStream.close();
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(">>> Record system is broken, please find help to the developer.");
        }

        return lines;
    }

    // function to read contents in files and take them into Map data structure.
    public static LinkedHashMap<String, String> readDataMap(String path) {
        LinkedHashMap<String, String> lines = new LinkedHashMap<String, String>();

        try {
            FileInputStream inputStream = new FileInputStream(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // read line by line.
            String line = null;
            while((line = bufferedReader.readLine()) != null)
            {
                String[] splitLine = line.split(",");
                lines.put(splitLine[0], splitLine[1]);
            }

            inputStream.close();
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(">>> Record system is broken, please find help to the developer.");
        }

        return lines;
    }

    // function to get the stored record index. Record index = current history index. 
    public static int getRecordIndex() {
        int index = 0;

        try {
            FileInputStream inputStream = new FileInputStream("resources/files/records/index.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String indexNum = bufferedReader.readLine();

            try {
                index = Integer.parseInt(indexNum);
            } catch (NumberFormatException e) {
                System.out.println(">>> Record system is broken, please find help to the developer.");
                e.printStackTrace();
            }

            inputStream.close();
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(">>> Record system is broken, please find help to the developer.");
        }

        return index;
    }
    
    // function to set the record index to 1. Use when user 'clear history'.
    public static boolean setRecordIndex(int index) {
        try {
            File file = new File("resources/files/records/index.txt");
            if (file.delete()) {
                file.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(String.valueOf(index));
                writer.flush();
                writer.close();

                return true;

            } else {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(">>> Record system is broken, please find help to the developer.");
            return false;
        }
    }
    
    // function to write history record.
    public static boolean writeRecord(Date date, int recordIdx, String record, ArrayList<Function> functions) {
        boolean writeSuccessful = false;
        try {
        	// create file.
            File file = new File("resources/files/records/history" + recordIdx + ".txt");
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            // Date + Content + Function
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
            writer.write("[Date] " + sdf.format(date) + "\r\n" + record);
            if (functions.size() != 0) {
                for (Function f: functions) {
                    writer.write("\r\n\r\nDEFINE FUNCTION " + f.getName() + " WITH PARAMETER " + f.getOriginalParam() + "\r\n");
                    writer.write(f.getContents() + "\r\n");
                    writer.write("END DEFINE AND RETURN " + f.getReturnValue() + "\r\n");
                }
            }
            writer.flush();
            writer.close();

            if (setRecordIndex(++ recordIdx)) {
                writeSuccessful = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(">>> Record system is broken, please find help to the developer.");
        }

        return writeSuccessful;
    }

    // function to delete all records.
    public static boolean deleteRecords() {
        File file = new File("resources/files/records");
        File files[] = file.listFiles();
        for (File f: files) {
            if (f.isFile()) {
                if (f.getName().startsWith("history") && f.getName().endsWith(".txt")) {
                    System.out.println(">>> Record has been deleted: " + f.getName());
                    f.delete();
                }
            }
        }

        return setRecordIndex(1);
    }

    // function to initialize the running files (.java file, .class file). 
    public static void initJavaPrograms() {
        File file = new File("resources/runs");
        File files[] = file.listFiles();
        try {
        	for (File f: files) {
                if (f.isFile()) {
                    System.out.println(">>> File has been deleted: " + f.getName());
                    f.delete();
                }
            }
        } catch (NullPointerException e) {
        	System.out.println(">>> No file exist, init finished.");
        }
        
    }
    
    // function to write code files (.java file).
    public static boolean writeJavaProgram(String codes) {
        boolean writeSuccessful = false;
        try {
            File file = new File("resources/runs/Main.txt");
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            writer.write(codes);
            writer.flush();
            writer.close();

            // Main.txt -> Main.java
            File javaFile = new File("resources/runs/Main.java");
            if (file.renameTo(javaFile)) {
                writeSuccessful = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return writeSuccessful;
    }

}
