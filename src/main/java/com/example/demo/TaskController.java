package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class TaskController {
    private final OkHttpClient httpClient = new OkHttpClient();


    @GetMapping("/api/hello")
    public String greetings(@RequestParam(value = "clientsName", defaultValue = "Client") String clientName,
                            HttpServletRequest request) throws IOException {
        String clientIp = request.getRemoteAddr();
        String location = "Unknown Location";
        try {
            Request locationRequest = new Request.Builder()
                    .url("https://ipinfo.io/" + clientIp + "/geo")
                    .build();
            try (Response response = httpClient.newCall(locationRequest).execute()) {
                if (response.body() != null) {

                    JSONObject json = new JSONObject(response.body().string());
                    location = json.optString("city", "Unknown Location");


                }


            } catch (Exception e) {
                e.printStackTrace();

            }
            int temperature = 11;

            return String.format("{\"client_ip\": \"%s\", \"location\": \"%s\", \"greeting\": \"Hello, %s!, the temperature is %d degrees Celsius in %s\"}",
                    clientIp, location, clientName, temperature, location);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
