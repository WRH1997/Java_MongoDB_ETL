import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
This class is used as an interface to the DB_Users.txt file (which stores users' credentials). It is responsible for
the login/registration CLI menu, where users can log in or sign-up. To do this, it reads from and writes to the DB_Users.txt
file.

@Author: Waleed R. Alhindi (B00919848)
 */
public class UserDbInterface {

    //Reference to DB_Users.txt file
    private File userDBFile;

    //Map to store existing and new users where the key is a user's username and the value
    //is an array of their [hashed password, security question, hashed security answer]
    private Map<String, String[]> userCredentials;


    /*
    UserDbInterface constructor which will first read from the Db_Users.txt file then invoke the login/registration menu.
    @Param no parameters
    @Return no return value since it is a constructor
     */
    UserDbInterface() throws Exception{
        userCredentials = new HashMap<>();
        try{
            readDBFile();
        }
        catch(IOException e){
            throw e;
        }
        try{
            loginMenu();
        }
        catch(Exception e){
            throw e;
        }
    }


    /*
    Method to read from the DB_Users.txt file. The method first checks whether the file exists and whether it has read
    and write access to it. If so, it then reads the contents of that file, then parses it based on the custom delimiters
    which extracts user data which is then stored in the userCredentials map.

    @Param no parameters
    @Return no return values since it operates on the object's instance variables
     */
    private void readDBFile() throws IOException{
        /*
        @CITATION NOTE: The following source was referenced to construct the code below that gets a file's absolute path.
        URL: https://stackoverflow.com/questions/31324451/how-to-get-absolute-path-of-existing-folder
        Accessed: February 10, 2023
         */
        String filePath = new File("DB_Users.txt").getAbsolutePath();
        this.userDBFile = new File(filePath);
                /*
        @CITATION NOTE: The following source was referenced to construct the code below that checks if a file
        exists and whether the method has read and write access to it.
        URL: https://javarevisited.blogspot.com/2012/01/how-to-file-permission-in-java-with.html#axzz7seG9Ilj0
        Accessed: February 10, 2023
         */
        if(!userDBFile.exists()){
            throw new FileNotFoundException("Error: User DB File not found!");
        }
        if(!userDBFile.canRead() || !userDBFile.canWrite()){
            throw new FileNotFoundException("Error: User DB File found but cannot be accessed!");
        }
        userCredentials.clear();
        Scanner fileReader;
        try{
            fileReader = new Scanner(userDBFile);
        }
        catch(IOException e){
            throw new IOException("Error: Could not read from User DB File!");
        }
        String fileContent = "";
        while(fileReader.hasNext()){
            fileContent += fileReader.nextLine();
        }
        String eachUserCred[] = fileContent.split("@");
        for(String singleUser: eachUserCred){
            if(singleUser.equals("")){
                continue;
            }
            String userCred[] = singleUser.split(",");
            userCredentials.put(userCred[0],  Arrays.copyOfRange(userCred, 1, 4));
        }
        fileReader.close();
    }


    /*
    Method that presents user with login/registration menu. The menu will give the user 2 options, either inputting 1
    to log in or inputting 2 to register. This method will then invoke either the register() or login() method based
    on that input. Additionally, the menu will keep looping and asking the user to either log in or register until the
    user has successfully done so (i.e., until they have successfully supplied the right credentials)

    @Param no parameters
    @Return no return value since it is just a menu
     */
    private void loginMenu() throws Exception{
        boolean credentialsCheck = false;
        while(!credentialsCheck){
            System.out.println("Please login or register:\n1 - Login\n2 - Register");
            String input = InputReader.reader.nextLine();
            input = input.trim();
            if(!input.equals("1") && !input.equals("2")){
                System.out.println("Error: Invalid option!");
                continue;
            }
            if(input.equals("1")){
                if(login()){
                    credentialsCheck = true;
                }
            }
            else if(input.equals("2")){
                if(register()){
                    credentialsCheck = true;
                }
            }
        }
    }


    /*
    Method invoked during the login/registration menu when the user input (2) to register. The method will ask for
    the user's username, password, security question, and security answer. It will check if the username already exists,
    and if it does will give the user an error and return to the login/registration menu. If it does not, then the method
    will hash the supplied password and security answer, insert a new key-value pair into the userCredentials map, invoke
    the LogsInterface class to log the registration into the Log.txt file, and write the new user's information into the
    DB_Users file.

    @Param no parameters
    @Return boolean value to denote whether the registration was successful or not
     */
    private boolean register() throws Exception{
        System.out.println("Username:");
        String username = InputReader.reader.nextLine();
        if(userCredentials.containsKey(username)){
            System.out.println("Error: username already exists!");
            return false;
        }
        if(!validInputCheck(username)){
            System.out.println("Error: invalid username!\nUsername cannot be empty, contain commas, or contain '@'");
            return false;
        }
        System.out.println("Password:");
        String password = InputReader.reader.nextLine();
        if(!validInputCheck(password)){
            System.out.println("Error: invalid password!\nPassword cannot be empty, contain commas, or contain '@'");
            return false;
        }
        System.out.println("Security Question:");
        String SQ = InputReader.reader.nextLine();
        if(!validInputCheck(SQ)){
            System.out.println("Error: invalid security question!\nSecurity Question cannot be empty, contain commas, or contain '@'");
            return false;
        }
        System.out.println("Answer for ["+SQ+"]:");
        String SA = InputReader.reader.nextLine();
        if(!validInputCheck(SA)){
            System.out.println("Error: invalid security answer!\nSecurity Answer cannot be empty, contain commas, or contain '@'");
            return false;
        }
        String hashedPassword;
        String hashedSA;
        try{
            hashedPassword = InputReader.convertMD5Hash(password);
            hashedSA = InputReader.convertMD5Hash(SA);
        }
        catch(NoSuchAlgorithmException e){
            throw e;
        }
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(userDBFile, true));
            String lineToWrite = "@"+username+","+hashedPassword+","+SQ+","+hashedSA+"\n";
            writer.append(lineToWrite);
            writer.close();
        }
        catch(IOException e){
            throw new IOException("Error: Could not write to User DB File during registration!");
        }
        String[] credentials = {hashedPassword, SQ, hashedSA};
        userCredentials.put(username, credentials);
        System.out.println("Registration successful!\nLogged in as ["+username+"]");
        LogsInterface.currentUser = username;
        String logString = "Registered user [" + username + "]";
        LogsInterface.updateLog(logString, "registration");
        return true;
    }

    /*
    Method to check whether inputs during user registration are valid or not. Specifically, it checks whether the characters
    "," "@" or "" are present in the input string since those characters are used as delimiters in the DB_Users.txt file.
    So, it will reject inputs that contain them.

    @Param String input --> the input string that needs to be validated
    @Return boolean to denote whether the input is valid or not
     */
    private boolean validInputCheck(String input){
        input = input.trim();
        if(input.equals("") || input.contains(",") || input.contains("@")){
            return false;
        }
        return true;
    }


    /*
    Method invoked when the user inputs (1) during the login/registration menu. The method is a login method that
    will ask the user for their username and password.  It then hashes the password supplied and checks it against the
    corresponding value in the userCredentials map. If the passwords match, it then asks the user their security question (which
    is stored in the userCredentials map). The security answer inputted is then hashed and checked against the corresponding
    value in the userCredentials map. If the user provides the correct credentials, then it will print out a "Access Denied" statement
    and boot them back to the login/registration menu.

    @Param no parameters
    @Return boolean to denote if log in was successful or not
     */
    private boolean login() throws Exception{
        System.out.println("Username:");
        String username = InputReader.reader.nextLine();
        if(!userCredentials.containsKey(username)){
            System.out.println("Access Denied: username does not exist!");
            return false;
        }
        System.out.println("Password:");
        String password = InputReader.reader.nextLine();
        String hashedPassword = InputReader.convertMD5Hash(password);
        if(!hashedPassword.equals(userCredentials.get(username)[0])){
            System.out.println("Access Denied: incorrect password!");
            return false;
        }
        System.out.println("Answer for security question ["+userCredentials.get(username)[1]+"]:");
        String SA = InputReader.reader.nextLine();
        String hashedSA = InputReader.convertMD5Hash(SA);
        if(!hashedSA.equals(userCredentials.get(username)[2])){
            System.out.println("Access Denied: incorrect answer for security question!");
            return false;
        }
        LogsInterface.currentUser = username;
        String logStr = "[" + username + "] logged in.";
        LogsInterface.updateLog(logStr, "login");
        System.out.println("Logged in as [" + username + "]");
        return true;
    }
}
