import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;


/*
This class provides an input Scanner object as a static global variable since other classes need to get the user's
input from the CLI. Additionally, this class also has a method to convert strings to hash values using the MD5 algorithm.
The hashing method is used by UserDbInterface class to hash passwords and security answers.

@Author: Waleed R. Alhindi (B00919848)
 */
public class InputReader {


    //Global scanner object used by other classes to read user's inputs from CLI
    public static Scanner reader = new Scanner(System.in);


    /*
    Method to convert passwords and security answers to hash values using the MD5 algorithm
    @Param String plainText --> the actual password or security answer the user supplies
    @Return String hash --> the hashed value of the plainText value generated using MD5
    @CITATION NOTE: The following source was referenced when creating this function.
    URL: https://www.geeksforgeeks.org/md5-hash-in-java/
    Accessed: February 10, 2023
     */
    public static String convertMD5Hash(String plainText) throws NoSuchAlgorithmException{
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(plainText.getBytes());
            BigInteger signum = new BigInteger(1, digest);
            String hash = signum.toString(16);
            while(hash.length()<32){
                hash = "0" + hash;
            }
            return hash;
        }
        catch(NoSuchAlgorithmException e){
            throw new NoSuchAlgorithmException("Error applying MD5 Hash algorithm on credentials!");
        }
    }

}
