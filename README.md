# SRMI - Sistema de Reporte e Manutenção Integrada

## Requisitos

### Java
- **JDK 25** ou superior
- Download: https://jdk.java.net/25/

### Maven
- **Maven 3.8+**
- Download: https://maven.apache.org/download.cgi

### Firebase
- Conta no [Firebase Console](https://console.firebase.google.com)
- Arquivo de credenciais `firebase-config.json` (Service Account)
- **Não incluso no repositório** — solicite ao administrador do projeto

---

## Dependências (gerenciadas pelo Maven)

| Dependência | Versão | Descrição |
|---|---|---|
| `firebase-admin` | 9.4.1 | SDK do Firebase Admin para Java (Firestore, Auth) |
| `junit-jupiter` | 5.11.2 | Framework de testes (escopo: test) |

### Plugins Maven

| Plugin | Versão | Função |
|---|---|---|
| `maven-compiler-plugin` | 3.13.0 | Compilação com Java 25 |
| `maven-surefire-plugin` | 3.1.2 | Execução dos testes |
| `maven-jar-plugin` | 3.3.0 | Geração do JAR executável |
| `maven-shade-plugin` | 3.5.0 | Empacota todas as dependências num único JAR (`SRMI-app.jar`) |

---

## Configuração do Firebase

1. Acesse o [Firebase Console](https://console.firebase.google.com)
2. Engrenagem → **Configurações do projeto** → aba **Contas de serviço**
3. Clique em **Gerar nova chave privada**
4. Salve o arquivo como `firebase-config.json` em:
   ```
   src/main/resources/firebase-config.json
   ```

> ⚠️ **Nunca commite esse arquivo no Git.** Ele já está no `.gitignore`.

---

## Como compilar e rodar

```bash
# Compilar e empacotar
mvn package

# Rodar o JAR gerado
java -jar target/SRMI-app.jar
```

### Rodar os testes

```bash
mvn test
```

---

## Estrutura do projeto

```
SRMI Final Version/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── SRMI_Sistema.java       # Classe principal + interface
│   │   │   └── FirebaseInitializer.java # Inicialização do Firebase
│   │   └── resources/
│   │       └── firebase-config.json    # ⚠️ NÃO incluso no repositório
│   └── test/
│       └── java/com/example/           # Testes unitários
├── pom.xml                             # Dependências Maven
└── .gitignore
```

---

## Coleções no Firestore

O sistema utiliza as seguintes coleções no Firestore:

- `usuarios` — cadastro de alunos e administradores
- `chamados` — reportes abertos pelos alunos

NOTA CRUCIAL: adicione sempre depois de baixar o arquivo por aqui, adicione a pasta descompactada "resources" no caminho: "(PASTA ONDE O ARQUIVO FOI BAIXADO)\SRMI Project\src\main" para evitar crashes e erros indesejados
