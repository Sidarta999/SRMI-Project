package com.example;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FirebaseInitializer {
    private static final String CONFIG_PATH_1 = "src/main/resources/firebase-config1.json";
    private static final String CONFIG_PATH_2 = "firebase-config1.json";
    private static final String CONFIG_PATH_3 = "target/classes/firebase-config1.json";
    private static volatile boolean initialized = false;

    public static void inicializar() throws IOException {
        if (initialized) {
            System.out.println("[Firebase] Já está inicializado.");
            return;
        }

        try {
            // Tenta encontrar o arquivo de configuração
            String configPath = null;
            if (Files.exists(Paths.get(CONFIG_PATH_1))) {
                configPath = CONFIG_PATH_1;
            } else if (Files.exists(Paths.get(CONFIG_PATH_2))) {
                configPath = CONFIG_PATH_2;
            } else if (Files.exists(Paths.get(CONFIG_PATH_3))) {
                configPath = CONFIG_PATH_3;
            } else {
                throw new IOException("Arquivo firebase-config1.json não encontrado em nenhum dos locais esperados: " +
                    CONFIG_PATH_1 + ", " + CONFIG_PATH_2 + ", " + CONFIG_PATH_3);
            }

            System.out.println("[Firebase] Usando configuração de: " + configPath);
            FileInputStream serviceAccount = new FileInputStream(configPath);

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("[Firebase] App inicializado com sucesso!");
            } else {
                System.out.println("[Firebase] App já estava inicializado.");
            }

            // Verifica conexão com Firestore
            verificarConexaoFirestore();
            initialized = true;
            System.out.println("[Firebase] ✓ Firebase inicializado e conectado com sucesso!");

        } catch (IOException e) {
            System.err.println("[Firebase] ERRO ao inicializar: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void verificarConexaoFirestore() throws IOException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            // Tenta fazer uma query simples para verificar conectividade
            db.collection("_healthcheck").limit(1).get().get();
            System.out.println("[Firebase] ✓ Conexão com Firestore verificada!");
        } catch (Exception e) {
            System.err.println("[Firebase] ⚠ Aviso: Não foi possível verificar conexão com Firestore: " + e.getMessage());
            // Não lança exceção aqui porque pode ser um erro temporário ou permissão de leitura
        }
    }

    public static boolean estaInicializado() {
        return initialized && !FirebaseApp.getApps().isEmpty();
    }
}