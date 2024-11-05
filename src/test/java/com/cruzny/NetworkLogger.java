package com.cruzny;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v130.network.Network;
import org.openqa.selenium.devtools.v130.network.model.Request;
import org.openqa.selenium.devtools.v130.network.model.Response;

public class NetworkLogger {
    private BufferedWriter logWriter;
    private DevTools devTools;
    private boolean isJsonFormat;

    public NetworkLogger(WebDriver driver, boolean isJsonFormat) throws IOException {
        this.isJsonFormat = isJsonFormat;
        devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timeStamp = LocalDateTime.now().format(formatter);
        String finalLogFilePath = timeStamp + (isJsonFormat ? ".json" : ".log");
        logWriter = new BufferedWriter(new FileWriter(finalLogFilePath, true));
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        logWriter.write("[");
        setupNetworkListeners();
    }

    private void setupNetworkListeners() {
        devTools.addListener(Network.requestWillBeSent(), request -> {
            if (!isFilteredResource(request.getRequest())) {
                logRequest(request.getRequest());
            }
        });

        devTools.addListener(Network.responseReceived(), response -> {
            if (!isFilteredResource(response.getResponse())) {
                logResponse(response.getResponse());
            }
        });
    }

    private void logRequest(Request request) {
        try {
            if (isJsonFormat) {
                JSONObject logEntry = new JSONObject();
                logEntry.put("type", "Request");
                logEntry.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                logEntry.put("url", request.getUrl());
                logEntry.put("method", request.getMethod());
                logEntry.put("headers", request.getHeaders());
                logWriter.write(logEntry.toString() + ",\n");
            } else {
                String logEntry = String.format(
                    "[Request] %s\nTime: %s\nURL: %s\nMethod: %s\nHeaders: %s\n",
                    LocalDateTime.now(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    request.getUrl(),
                    request.getMethod(),
                    request.getHeaders()
                );
                logWriter.write(logEntry);
            }
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("Error logging request: " + e.getMessage());
        }
    }

    private void logResponse(Response response) {
        try {
            if (isJsonFormat) {
                JSONObject logEntry = new JSONObject();
                logEntry.put("type", "Response");
                logEntry.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                logEntry.put("url", response.getUrl());
                logEntry.put("status", response.getStatus());
                logEntry.put("headers", response.getHeaders());
                logWriter.write(logEntry.toString() + ",\n");
            } else {
                String logEntry = String.format(
                    "[Response] %s\nTime: %s\nURL: %s\nStatus: %d\nHeaders: %s\n",
                    LocalDateTime.now(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    response.getUrl(),
                    response.getStatus(),
                    response.getHeaders()
                );
                logWriter.write(logEntry);
            }
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("Error logging response: " + e.getMessage());
        }
    }
    private boolean isFilteredResource(Request request) {
        String url = request.getUrl().toLowerCase();
        return url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg") ||
               url.endsWith(".gif") || url.endsWith(".css") || url.endsWith(".svg") || url.endsWith(".img");
    }

    private boolean isFilteredResource(Response response) {
        String mimeType = response.getMimeType().toLowerCase();
        return mimeType.startsWith("image/") || mimeType.equals("text/css");
    }
    public void close(){
        try {
            if (logWriter != null) {
                logWriter.write("]");
                logWriter.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }    
}
