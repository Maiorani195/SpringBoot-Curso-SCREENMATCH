package br.com.alura.screenmatch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsultaChatGPT {

    public static String obterTraducao(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }

        String apiKey = System.getenv("GEMINI_API_KEY");
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Monta o JSON de forma segura com o Jackson (trata aspas, barras e caracteres especiais)
            ObjectNode root = mapper.createObjectNode();
            ArrayNode contents = root.putArray("contents");
            ObjectNode contentItem = contents.addObject();
            ArrayNode parts = contentItem.putArray("parts");
            ObjectNode textPart = parts.addObject();
            textPart.put("text", "Traduza para o português apenas o texto a seguir, mantendo o sentido original: " + texto);

            String jsonBody = mapper.writeValueAsString(root);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Erro na chamada da API Gemini (" + response.statusCode() + "): " + response.body());
                return texto;
            }

            JsonNode rootNode = mapper.readTree(response.body());
            JsonNode candidates = rootNode.path("candidates");

            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode partsNode = candidates.get(0).path("content").path("parts");
                if (partsNode.isArray() && !partsNode.isEmpty()) {
                    return partsNode.get(0).path("text").asText().trim();
                }
            }

            return texto;

        } catch (Exception e) {
            System.err.println("Erro ao obter tradução: " + e.getMessage());
            return texto;
        }
    }
}