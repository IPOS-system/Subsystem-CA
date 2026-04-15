package api_impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private String token;

    public SAService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public boolean connect(String username, String password) {
        try {
            String body = objectMapper.writeValueAsString(new LoginRequest(username, password));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SA_BASE_URL + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return false;
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode tokenNode = json.get("token");

            if (tokenNode == null || tokenNode.asText().isBlank()) {
                return false;
            }

            this.token = tokenNode.asText();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
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