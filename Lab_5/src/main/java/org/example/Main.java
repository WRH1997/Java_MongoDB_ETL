package org.example;

import org.apache.spark.sql.SparkSession;
import java.io.*;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;

public class Main {
    public static void main(String[] args) throws Exception{

        SparkSession spark = SparkSession.builder().appName("Lab 5").config("spark.master", "local").getOrCreate();
        String filePath = new File("weather.json").getAbsolutePath();
        JSONObject weatherJson = null;
        String weatherJsonStr = "";
        try{
            Scanner scr = new Scanner(new File(filePath));
            while(scr.hasNextLine()){
                weatherJsonStr += scr.nextLine();
            }
            weatherJson = new JSONObject(weatherJsonStr);
            scr.close();
        }
        catch(Exception e){
            throw new Exception("Error trying to access weather.json file! " + e.getMessage());
        }
        JSONArray dailyWeather = weatherJson.getJSONArray("daily");
        for(int i=0; i<dailyWeather.length(); i++){
            JSONObject temp = dailyWeather.getJSONObject(i);
            if(temp.getJSONObject("feels_like").getDouble("day")>-5.0){
                dailyWeather.remove(i);
            }
        }
        filePath = filePath.replace("weather.json", "cold_weather.json");
        FileWriter writer = new FileWriter(filePath);
        writer.write(weatherJson.toString());
        writer.flush();
        writer.close();

    }

}