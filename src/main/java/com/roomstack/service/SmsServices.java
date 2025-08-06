package com.roomstack.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SmsServices {

    private final String apiKey = "vGseJSXyzKcqAlU0kWg56P3xCIRTOwMEd98LBija2nfNtQr1pb9WaRBU28T5qYpMlehQzHEXmfINrC1w";

    public void sendBookingSMS(String phoneNumber, String roomTitle) {
        try {
            String message = "Your room '" + roomTitle + "' has been booked successfully!";
            String encodedMessage = URLEncoder.encode(message, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://www.fast2sms.com/dev/bulkV2"))
                    .header("authorization", apiKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "message=" + encodedMessage +
                                    "&language=english" +
                                    "&route=p" +
                                    "&numbers=" + phoneNumber))
                    .build();

            HttpClient.newHttpClient()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> System.out.println("SMS sent: " + response.body()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
