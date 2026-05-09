package com.agrotech.api.ai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    @Value("${geminiApiKey}")
    private String apiKey;

    private Client client;

    AIService() {}

    public void instanceGemini() {
        client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String generateContent(String prompt) {
        try {
            GenerateContentResponse response =
                    client.models.generateContent("gemini-2.5-flash", "Eres AgroBot y estás generando sugerencia una sugerencia de asesor para la plataforma AgroTech.\n"
                            + prompt + "\nEnvia la respuesta en texto plano, no en estilo markdown. Y que sea respuesta corta ya que toma en cuenta que estás en un chat en AgroTech.\n"
                            + "Añade una última linea en la cual coloques el userId del usuario que has recomendado. colocalo como este ejemplo \"userId: 4\"", null);
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al procesar la solicitud a Gemini: " + e.getMessage());
            return "No se pudo procesar tu solicitud en este momento. Intenta nuevamente más tarde.";
        }
    }

}
