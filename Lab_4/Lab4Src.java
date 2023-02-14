import java.sql.*;
import java.util.*;
import java.io.*;

public class Lab4Src {

    public static void main(String[] args) throws Exception{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://34.29.75.174:3306?serverTimezone=UTC&useSSL=false", "root", "");
            Statement statement = conn.createStatement();
            statement.execute("use eCommerce");
            ResultSet res = statement.executeQuery("select item_name, available_quantity from inventory where item_id=1;");
            res.next();
            String item = res.getString("item_name");
            int quantity = res.getInt("available_quantity");
            statement.execute("update inventory set available_quantity=available_quantity-3 where item_name='"+item+"';");
            conn.close();
            Connection localConn = DriverManager.getConnection("jdbc:mysql://localhost:3306?serverTimezone=UTC&useSSL=false", "root", "Pokemon878!");
            Statement statement1 = localConn.createStatement();
            statement1.execute("use DbLab4");
            statement1.execute("insert into Order_Info values(1, 1, '"+ item + "', 3, 'Feb-14-2023');");
            localConn.close();
        }
        catch(Exception e){
            throw e;
        }
    }
}
