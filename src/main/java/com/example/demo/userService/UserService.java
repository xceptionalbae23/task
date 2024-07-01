package com.example.demo.userService;

import com.example.demo.entity.Visitor;
import com.example.demo.entity.WeatherData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final String API_URL = "http://api.weatherapi.com/v1/current.json";
    //private static  final String API_URL = "https://api.openweathermap.org/v1/current.json";
    private static final String API_KEY = "76c5b43a6c31478dbc142441240407";
    private static final String IP_INFO_API_URL = "https://ipinfo.io/";
    private static final String IP_INFO_TOKEN = "9de2f8edac39e9";


    public ResponseEntity<Visitor> greetings(String visitor_name, HttpServletRequest request) {
        String cleanVisitorName = removeSurroundingQuotes(visitor_name);
        String clientIp = getClientIpAddress(request);
        String city = getCityFromIpInfo(clientIp);
        WeatherData weatherData = getWeatherData(city);
        double temp = weatherData.getTemperatureC();

        Visitor visitor = new Visitor();
        visitor.setClientIp(clientIp);
        visitor.setLocation(city);
        visitor.setGreetings("Hello, " + cleanVisitorName + "! , " + "the temperature is " + temp + " degrees Celsius in " + city);

        return ResponseEntity.ok(visitor);
    }

    private String removeSurroundingQuotes(String input) {
        // Use regex to remove leading and trailing escaped quotes
        Pattern pattern = Pattern.compile("^\"(.*)\"$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return input;
    }

    public String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = getIpFromHeaders(request);
        return ipAddress != null ? ipAddress : request.getRemoteAddr();
    }

    private String getIpFromHeaders(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Forwarded",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return null;
    }

    private String getCityFromIpInfo(String ipAddress) {
        String apiUrl = IP_INFO_API_URL + ipAddress + "/json?token=" + IP_INFO_TOKEN;

        System.out.println("API URL: " + apiUrl); // Debug statement

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                System.out.println("HTTP Response Code: " + response.getCode()); // Debug statement

                if (response.getCode() == 200) {
                    String jsonResponse = EntityUtils.toString(response.getEntity());
                    System.out.println("JSON Response: " + jsonResponse); // Debug statement

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonResponse);

                    // Return the city from the IPinfo response
                    return jsonNode.path("city").asText("Unknown City");
                } else {
                    System.err.println("Non-OK HTTP Response Code: " + response.getCode());
                }
            } catch (org.apache.hc.core5.http.ParseException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown City";
    }


    public WeatherData getWeatherData(String city) {
        WebClient webClient = WebClient.builder()
                .baseUrl(API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String apiUrl = "?key=" + API_KEY + "&q=" + city + "&aqi=no";

        try {
            String response = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Blocking to convert Mono to actual response (consider using non-blocking in real apps)

            JSONObject jsonResponse = new JSONObject(response);
            String locationName = jsonResponse.getJSONObject("location").getString("name");
            int temperatureC = jsonResponse.getJSONObject("current").getInt("temp_c");

            return new WeatherData(locationName, temperatureC);

        } catch (WebClientResponseException e) {
            System.err.println("WebClient Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw e;
        }

    }
}
