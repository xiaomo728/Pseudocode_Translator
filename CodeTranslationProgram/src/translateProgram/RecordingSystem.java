
package translateProgram;

import translateUnits.Function;

import java.util.ArrayList;
import java.util.Date;

public class RecordingSystem {
	// Static class to construct history recording system.
	
    public static int recordIdx;

    // function to initialize history recording system.
    public static void init() {
        recordIdx = IOHandler.getRecordIndex();
        System.out.println(">>> Initialize the recording system successful... (3/4)");
    }

    // function to call write record function in IOHandler.
    public static void recordHistory(Date date, String pseudoCode, ArrayList<Function> functions) {
        boolean successful = IOHandler.writeRecord(date, recordIdx, pseudoCode, functions);
        if (successful) {
            recordIdx ++;
            // at most 99999 records.
            if (recordIdx == 99999) {
                clearAllHistory();
            }
        } else {
            System.out.println(">>> Record failed. Please find help to the developer.");
        }
    }

    // function to call delete records function in IO Handler.
    public static void clearAllHistory() {
        boolean successful = IOHandler.deleteRecords();
        if (successful) {
            recordIdx = 1;
            System.out.println(">>> Clear all history successful.");
        } else {
            System.out.println(">>> Clear all history failed. Please find help to the developer.");
        }
    }

    // function to read the recent 10 records from record files.
    public static ArrayList<String> getRecentRecords() {
        ArrayList<String> records = new ArrayList<String>();
        int currentRecordNum = recordIdx - 1;
        if (currentRecordNum <= 0) return null; // no records exist.
        if (currentRecordNum < 10) {
        	// current records number < 10, display all records.
            for (int i = currentRecordNum; i >= 1; i--) {
                String recordString = "";
                ArrayList<String> record = IOHandler.readData("resources/files/records/history" + i + ".txt");
                for (String line: record) {
                    recordString += line + "\r\n";
                }
                records.add(recordString);
            }

        } else {
        	// current records number >= 10, display 10 records.
            for (int i = currentRecordNum; i >= currentRecordNum - 9; i--) {
                String recordString = "";
                ArrayList<String> record = IOHandler.readData("resources/files/records/history" + i + ".txt");
                for (String line: record) {
                    recordString += line + "\r\n";
                }
                records.add(recordString);
            }
        }

        return records;
    }


}
