package com.cruzny;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

public class LogParser {
    private static BufferedWriter writer;
        public static void main(String[] args) throws IOException {
            String filePath = "2024-11-04_22-02-23.json";
            String cutoffTimestamp = "2024-11-04T22:02:25.9819853";
            String outputFile = String.format("%s-parsed.json",filePath.split("\\.")[0]);
            LocalDateTime givenTime = LocalDateTime.parse(cutoffTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray logEntries = new JSONArray(content);
            writer = new BufferedWriter(new FileWriter(outputFile, true));
        for (int i = 0; i < logEntries.length(); i++) {
            JSONObject entry = logEntries.getJSONObject(i);
            LocalDateTime entryTime = LocalDateTime.parse(entry.getString("timestamp"),DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (entryTime.isAfter(givenTime)) {
                // System.out.println(entry);
                writer.write(entry.toString());
            }
        }
        writer.close();
        System.out.println("Done!");
    }
}
