package api_impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import service.Result;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SAService {
    private static final String SA_BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    int merchantId = -1;

    private String token;

    public SAService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Result connect(String username, String password) {
        try {
            String body = objectMapper.writeValueAsString(new LoginRequest(username, password));
            System.out.println(username + password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SA_BASE_URL + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Result.fail("");
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode tokenNode = json.get("token");

            merchantId = json.get("merchantId").asInt();

            if (tokenNode == null || tokenNode.asText().isBlank()) {
                return Result.fail("");
            }

            this.token = tokenNode.asText();
            return Result.success(this.token);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("");
        }
    }

    public <T> T get(String path, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = authorizedRequest(path)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), responseType);
    }

    public <T> T get(String path, com.fasterxml.jackson.core.type.TypeReference<T> typeRef)
            throws IOException, InterruptedException {
        HttpRequest request = authorizedRequest(path)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), typeRef);
    }
    public String post(String path, Object payload) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(payload);

        HttpRequest request = authorizedRequest(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public String put(String path, Object payload) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(payload);

        System.out.println("PUT URL: " + SA_BASE_URL + path);
        System.out.println("PUT BODY: " + body);
        System.out.println("TOKEN: " + token);

        HttpRequest request = authorizedRequest(path)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("PUT STATUS: " + response.statusCode());
        System.out.println("PUT RESPONSE: " + response.body());

        return response.body();
    }

    public int getMerchantId(){
        return merchantId;
    }

    public boolean isConnected() {
        return token != null && !token.isBlank();
    }

    public void disconnect() {
        token = null;
    }

    private HttpRequest.Builder authorizedRequest(String path) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SA");
        }

        return HttpRequest.newBuilder()
                .uri(URI.create(SA_BASE_URL + path))
                .header("Authorization", "Bearer " + token);
    }

    private record LoginRequest(String username, String password) {}
}