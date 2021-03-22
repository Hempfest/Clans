package com.youtube.hempfest.clans.util.misc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
public class URLParser {

    private static String formatObject(InputStream inputStream) {
        return new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
    }

    public static JsonObject getJson(String urlQueryString) {
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
            json = formatObject(inStream); // input stream to string
            Gson gson = new Gson();
            JsonElement element = gson.fromJson(json, JsonElement.class);
            jsonObject = element.getAsJsonObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return jsonObject;
    }
}