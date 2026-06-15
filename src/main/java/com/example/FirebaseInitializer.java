package com.example;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Inicializa o Firebase Admin SDK para uso com Cloud Firestore.
 */
public class FirebaseInitializer {

    public static boolean inicializar() {
        // Carrega o arquivo diretamente do Classpath (essencial para projetos Maven/JAR)
        try (InputStream serviceAccount = FirebaseInitializer.class.getResourceAsStream("/firebase-config.json")) {
            
            if (serviceAccount == null) {
                System.out.println("Erro: O arquivo 'firebase-config.json' não foi encontrado na raiz dos recursos (src/main/resources/). Pulando inicialização do Firebase.");
                return false;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Conexão com o Firebase realizada com sucesso!");
            }
            return true;
            
        } catch (IOException e) {
            System.err.println("Erro ao inicializar o Firebase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}