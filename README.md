# JDK Project

A simple Maven-based JDK project.

## Build

Use `mvn package` to compile and package the application.

## Run

Use `mvn exec:java -Dexec.mainClass=com.example.App` if you install the Maven Exec Plugin, or run the compiled class directly.

## Firebase

Este projeto já carrega o Firebase usando o arquivo `src/main/resources/firebase-config.json`.
- Verifique que o arquivo existe e contém as credenciais de service account do Firebase.
- O arquivo deve estar disponível no classpath quando o JAR for executado.
- Se a inicialização falhar, o aplicativo exibirá uma mensagem de erro e não abrirá a interface.
