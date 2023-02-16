import java.io.*;
import java.text.SimpleDateFormat;

/*
Class to interface with the Log.txt file. Essentially, this class is used by all other classes to insert
logs into the Log.txt file.

@Author: Waleed R. Alhindi (B00919848)
 */
public class LogsInterface{

    //global variable to track current user who is logged in
    public static String currentUser = null;


    /*
    Method to insert a log into the Log.txt file. This method is invoked during login, registration, and SQL queries.
    It logs any action the user does into the Log.txt file as the following format:

    [Timestamp] User: [currentUser]
    Command: [SQL Command]
    Outcome: [Result of that SQL Command]

    OR

    [TimeStamp] [currentUser] Logged in/registered.

    @Param String logInput --> the sql command to be stored in the log
    @Param String calledDuring --> the method that called the updateLog method
    @Return no return values
     */
    public static void updateLog(String logInput, String calledDuring) throws IOException{
        try{
            String filePath = new File("Log.txt").getAbsolutePath();
            File logFile = new File(filePath);
            if(!logFile.exists()){
                throw new FileNotFoundException("Error during "+calledDuring+": Log File not found!");
            }
            if(!logFile.canRead() || !logFile.canWrite()){
                throw new FileNotFoundException("Error during "+calledDuring+": Log File found but not accessible!");
            }
            /*
            @CITATION NOTE: The following source was referenced to construct the code below that creates
            a timestamp string of the current time and date.
            URL: https://stackoverflow.com/questions/23068676/how-to-get-current-timestamp-in-string-format-in-java-yyyy-mm-dd-hh-mm-ss
            Accessed: February 11, 2023
             */
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            String logString = "[" + timeStamp + "]: " + logInput + "\n\n";
            writer.append(logString);
            writer.close();
        }
        catch(IOException e){
            throw e;
        }
    }
}
