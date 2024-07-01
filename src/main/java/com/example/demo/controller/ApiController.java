package com.example.demo.controller;

import com.example.demo.entity.Visitor;
import com.example.demo.userService.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserService userService;

    public ApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello")
    public ResponseEntity<Visitor> greetings(@RequestParam(name = "visitor_name") String visitor_name, HttpServletRequest request){
        return userService.greetings(visitor_name, request);
    }
    //private final OkHttpClient httpClient = new OkHttpClient();


    /*@GetMapping("/api/hello")
    public String greetings(@RequestParam(value = "clientsName", defaultValue = "Client") String clientName,
                            HttpServletRequest request) {
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
    }*/
}
