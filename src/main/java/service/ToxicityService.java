package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ToxicityService {
    private static final String API_URL = "https://api-inference.huggingface.co/models/unitary/toxic-bert";
    private static final String API_TOKEN = "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";

    // Seuil pour détecter la toxicité (ajusté à 0.8 pour réduire les faux positifs)
    private static final double THRESHOLD = 0.8;

    // Liste de mots offensifs connus
    private static final List<String> OFFENSIVE_WORDS = Arrays.asList(
            "badword1", "badword2", "badword3" // Ajoutez vos mots offensifs ici
    );

    /**
     * Analyse et détecte si un texte est toxique ou non.
     *
     * @param text Texte à analyser.
     * @return Vrai si le texte est jugé toxique, faux sinon.
     * @throws Exception Lorsqu'un problème survient avec l'API ou la connectivité.
     */
    public boolean isToxic(String text) throws Exception {
        // Vérifier si le texte contient des mots offensifs connus
        if (containsOffensiveWords(text)) {
            return true;
        }

        // Appeler l'API pour une analyse plus approfondie
        double score = analyzeToxicity(text);
        System.out.println("Score de toxicité : " + score + " pour le texte : \"" + text + "\"");
        return score >= THRESHOLD;
    }

    /**
     * Analyse la toxicité d'un texte en appelant l'API.
     *
     * @param text Texte déjà nettoyé à analyser.
     * @return Score de toxicité [0.0 à 1.0].
     * @throws Exception Si une erreur survient lors de l'appel à l'API.
     */
    public double analyzeToxicity(String text) throws Exception {
        HttpURLConnection connection = null;

        try {
            // Configuration de la connexion à l'API
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Préparation des données d'entrée
            String jsonInput = "{\"inputs\": \"" + text + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Vérification du code de réponse
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Échec de l'appel à l'API. Code de réponse : " + responseCode);
            }

            // Lecture de la réponse de l'API
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Log de la réponse brute
            System.out.println("Réponse de l'API : " + response.toString());

            // Parsing du JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.toString());

            // Vérification et extraction du score du label "toxic"
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode results = jsonResponse.get(0); // Accéder au tableau de résultats
                for (JsonNode result : results) {
                    String label = result.get("label").asText();
                    if ("toxic".equalsIgnoreCase(label)) {
                        double score = result.get("score").asDouble();
                        return score; // Retourner le score du label "toxic"
                    }
                }
            }
            return 0.0; // Retourner 0.0 si le label "toxic" n'est pas trouvé
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'analyse de la toxicité : " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    /**
     * Nettoie le texte à analyser pour éviter des erreurs ou des faux positifs.
     *
     * @param text Texte brut.
     * @return Texte nettoyé.
     */
    private String preprocessText(String text) {
        return text.trim() // Supprimer les espaces au début/fin
                .toLowerCase() // Mettre en minuscule
                .replaceAll("[^\\p{L}\\p{N}\\p{Z}\\p{P}]", ""); // Garder lettres, chiffres, espaces et ponctuation
    }

    /**
     * Vérifie si le texte contient des mots offensifs connus.
     *
     * @param text Texte à vérifier.
     * @return Vrai si le texte contient des mots offensifs, faux sinon.
     */
    private boolean containsOffensiveWords(String text) {
        String cleanedText = preprocessText(text);
        for (String word : OFFENSIVE_WORDS) {
            if (cleanedText.contains(word)) {
                return true;
            }
        }
        return false;
    }
}