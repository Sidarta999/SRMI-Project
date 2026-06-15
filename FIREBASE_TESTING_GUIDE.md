# Firebase Connection Testing Guide

## Quick Test: Verify Firebase is Working

### 1. Check Application Startup
Run the application and look for these log messages in the console:

```
[Firebase] Usando configuração de: src/main/resources/firebase-config1.json
[Firebase] App inicializado com sucesso!
[Firebase] ✓ Conexão com Firestore verificada!
[Firebase] ✓ Firebase inicializado e conectado com sucesso!
```

If you see these messages, **Firebase is working correctly!**

### 2. Test Login Flow
1. Open the application
2. Try logging in with test credentials
3. Check for these messages:
   - **Success:** "Login bem-sucedido para: [email]"
   - **Failure:** "Falha de autenticação para login: [login]" (check credentials)
   - **Network Error:** "Erro Firebase: ..." (check internet/config)

### 3. Test Report Submission
1. Login as a regular user
2. Go to "REPORTAR PROBLEMA"
3. Fill in the form and submit
4. Should see "Reporte cadastrado com sucesso!" message

### 4. Test Admin Panel
1. Login as admin (type = Admin)
2. Go to admin dashboard
3. Should load pending reports from Firestore
4. Try approving/declining a report

## Troubleshooting

### Error: "Arquivo firebase-config1.json não encontrado"
**Solution:** 
- Ensure `firebase-config1.json` is in `src/main/resources/`
- Run: `mvn clean compile` to copy to target/classes/

### Error: "Firestore não inicializado corretamente"
**Solution:**
1. Check Firebase credentials file validity
2. Verify JSON syntax is correct
3. Check Google Cloud project permissions
4. Try restarting the application

### Error: "Falha ao conectar ao Firebase Firestore"
**Possible Causes:**
- Internet connection issue
- Firestore database disabled in Google Cloud
- Credentials don't have Firestore read/write permissions
- Firebase project ID mismatch

**Solution:**
1. Verify internet connectivity
2. Check Google Cloud Console → Firestore is enabled
3. Check IAM permissions for service account
4. Verify credentials file has correct project_id

### Password Issues During Login
- Passwords are case-sensitive
- Ensure email is in format: `user@ufrpe.br`
- Check that user account exists in Firestore

## Database Structure Expected

### Collection: `usuarios`
```
Document ID: (CPF number)
Fields:
  - nome: string
  - email: string (format: user@ufrpe.br)
  - senha: string (plaintext - consider hashing in production)
  - tipo: string ("Simples" or "Admin")
  - saldoCoins: integer (for Simples users)
```

### Collection: `chamados`
```
Document ID: (auto-generated or specified)
Fields:
  - categoria: string
  - descricao: string
  - localizacao: string
  - status: string ("Pendente", "Aprovado", "Recusado")
  - autorCpf: string
  - fotos: array of strings (base64 encoded)
```

## Performance Tips

1. **Reduce Firestore Reads:**
   - Cache user data after login
   - Implement pagination for large lists

2. **Optimize Image Storage:**
   - Consider storing Base64 images in Cloud Storage instead
   - Current implementation stores in Firestore (slower for large images)

3. **Connection Pooling:**
   - Firestore SDK handles connection reuse
   - Multiple SwingWorkers can safely use getFirestore()

## Logging Configuration

The application uses Java's built-in logging. To enable more detailed logs:

### In code:
```java
Logger logger = Logger.getLogger(SRMI_Sistema.class.getName());
logger.setLevel(Level.FINE);  // Show more detail
```

### Via JVM argument:
```bash
java -Djava.util.logging.level=FINE -cp ... com.example.SRMI_Sistema
```

## Common Error Messages & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "Erro ao inicializar o Firebase" | Config file not found | Check file path |
| "Firestore não inicializado" | Initialization failed | Check credentials |
| "Erro ao conectar com o banco de dados" | Network issue | Check internet |
| "Usuário não cadastrado ou senha inválida" | Wrong credentials | Verify login info |
| "Este CPF já possui uma conta ativa" | Duplicate registration | Use different CPF |
| "Não foi possível atualizar as moedas" | Firestore write error | Check permissions |

## Firebase Admin SDK Version

Current: `firebase-admin:9.4.1`

Check for updates:
```bash
mvn versions:display-dependency-updates
```

## Security Reminders

⚠️ Never commit `firebase-config1.json` to version control!

Add to `.gitignore`:
```
firebase-config*.json
src/main/resources/firebase-config*.json
```

## Support Resources

- [Firebase Admin SDK Documentation](https://firebase.google.com/docs/admin/setup)
- [Firestore Documentation](https://cloud.google.com/firestore/docs)
- [Firebase Console](https://console.firebase.google.com/)
- [Google Cloud Console](https://console.cloud.google.com/)
