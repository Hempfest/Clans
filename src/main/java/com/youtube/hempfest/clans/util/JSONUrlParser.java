package com.youtube.hempfest.clans.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
public class JSONUrlParser {

    private static String streamToString(InputStream inputStream) {
        String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
        return text;
    }

    public static JsonObject jsonGetRequest(String urlQueryString) {
        String json = null;
        JsonObject jsonObject = null;
        try {
            URL url = new URL(urlQueryString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            InputStream inStream = connection.getInputStream();
            json = streamToString(inStream); // input stream to string
            Gson gson = new Gson();
            JsonElement element = gson.fromJson(json, JsonElement.class);
            jsonObject = element.getAsJsonObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return jsonObject;
    }
}