package com.example.sjakkwebapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {

    @Value("${azure.ai.key:}")
    private String azureAiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String getBestMoveFromStockfish(String fen) {
        String url = "https://stockfish-app.happyfield-52deb28b.eastus.azurecontainerapps.io/analyze";
        String jsonPayload = "{\"fen\": \"" + fen + "\", \"depth\": 15}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                String move = "not found";
                String score = "unknown";

                Pattern movePattern = Pattern.compile("\"best_move\":\\s*\"(.*?)\"");
                Matcher moveMatcher = movePattern.matcher(body);
                if (moveMatcher.find()) {
                    move = moveMatcher.group(1);
                }

                Pattern scorePattern = Pattern.compile("\"score_cp\":\\s*(-?\\d+)");
                Matcher scoreMatcher = scorePattern.matcher(body);
                if (scoreMatcher.find()) {
                    score = scoreMatcher.group(1);
                }

                return move + " (Score CP: " + score + ")";
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Stockfish request failed: " + e.getMessage());
        }
        return "not found";
    }

    public String askAzureAI(String fen, String bestMove) throws IOException, InterruptedException {
        String url = "https://dstub-8074-resource.services.ai.azure.com/api/projects/dstub-8074/applications/agent-torr/protocols/openai/responses?api-version=2025-11-15-preview";
        
        String apiKey = azureAiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("AZURE_AI_KEY");
        }

        //String prompt = "FEN: " + fen + ". Stockfish's best move: " + bestMove;
        String prompt = "FEN: " + fen; // For testing - The agent decides when to use the tool.
        String json = "{" +
                "\"input\": [" +
                "  {\"role\": \"user\", \"content\": \"" + prompt + "\"}" +
                "]" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            Pattern pattern = Pattern.compile("\"text\":\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(response.body());
            if (matcher.find()) {
                return matcher.group(1);
            }
            return response.body();
        } else {
            throw new IOException("Error from Azure AI (Status " + response.statusCode() + "): " + response.body());
        }
    }
}
