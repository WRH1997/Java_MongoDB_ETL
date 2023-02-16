/*
This is the main class that will be executed and serves as the aggregate interface for the other classes.
When the main function is executed, it will create an instance of the UserDbInterface class which will
create a login/registration menu and serves as an interface to the DB_Users.txt file.

After a user has successfully logged in or registered, a QueryInterface object will be created. This object will act
as the MySQL CLI for the user, where it will accept create, select, update, insert, and delete commands. Additionally,
this object also interfaces with the Tables.txt file to retrieve and store the user's table data.

@Author: Waleed R. Alhindi (B00919848)
 */

public class Main {
    public static void main(String[] args) throws Exception{
        UserDbInterface userDB = new UserDbInterface();
        QueryInterface tables = new QueryInterface();
        InputReader.reader.close();
    }
}