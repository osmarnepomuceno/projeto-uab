# Inspecao Superficial de Ciberseguranca

Data: 2026-06-16  
Escopo: todos os arquivos do projeto, incluindo backend Quarkus, frontend Angular, Docker, scripts SQL, configuracoes e dependencias.

## Resumo Executivo

| Severidade | Quantidade |
|---|---:|
| Critica | 1 |
| Alta | 7 |
| Media | 6 |
| Baixa | 2 |

### 5 Acoes Mais Urgentes

1. Remover `privateKey.pem` do repositorio, rotacionar as chaves JWT e carregar segredo por Secret Manager/variavel segura.
2. Remover credenciais fixas de banco/admin, rotacionar senhas e parametrizar `docker-compose.yml`, `application.properties` e `init.sql`.
3. Impedir que `senhaHash` seja serializado em respostas REST; usar DTOs de entrada/saida.
4. Desabilitar dev mode, debug remoto, logs TRACE/DEBUG e exposicao da porta 5006 fora do ambiente local.
5. Atualizar dependencias vulneraveis do frontend e revisar dependencias Java legadas (`log4j:1.2.16`, `itext:2.1.7`, `commons-lang:2.4`).

## Achados

### 1. Chave privada JWT versionada no repositorio

- **Localizacao:** `backend/src/main/resources/privateKey.pem`, linhas 1-28; `backend/src/main/resources/application.properties`, linhas 12-14.
- **Funcao/area:** assinatura e verificacao JWT.
- **OWASP:** A04 Cryptographic Failures, A08 Software or Data Integrity Failures.
- **Severidade:** critica.
- **Descricao:** a chave privada usada para assinar tokens JWT esta armazenada em texto claro dentro do codigo-fonte. Qualquer pessoa com acesso ao repositorio pode assinar tokens validos.
- **Evidencia:**

```text
backend/src/main/resources/privateKey.pem:1
-----BEGIN PRIVATE KEY-----

backend/src/main/resources/application.properties:13-14
mp.jwt.verify.publickey.location=publicKey.pem
smallrye.jwt.sign.key.location=privateKey.pem
```

- **Impacto potencial:** comprometimento total de autenticacao/autorizacao; emissao de tokens com perfil `ADMINISTRADOR`; persistencia do risco ate rotacao das chaves.
- **Recomendacao:**

```properties
# application.properties
mp.jwt.verify.publickey.location=${JWT_PUBLIC_KEY_LOCATION}
smallrye.jwt.sign.key.location=${JWT_PRIVATE_KEY_LOCATION}
```

Remover os arquivos PEM do repositorio, adicionar `*.pem` ao `.gitignore`, rotacionar o par de chaves e injetar o segredo por Secret Manager, volume seguro ou variavel de ambiente protegida.
- **Referencias:** CWE-798, CWE-321, OWASP A04.

### 2. Credenciais fixas de banco e administrador inicial

- **Localizacao:** `backend/src/main/resources/application.properties`, linhas 3-4; `docker-compose.yml`, linhas 8-12 e 29-32; `docker/init.sql`, linhas 37-40; `README.md`, linhas 52-59.
- **Funcao/area:** configuracao de banco e bootstrap administrativo.
- **OWASP:** A02 Security Misconfiguration, A04 Cryptographic Failures, A07 Authentication Failures.
- **Severidade:** alta.
- **Descricao:** usuario, senha do banco, senha de root MySQL e credenciais do administrador inicial estao fixos e documentados.
- **Evidencia:**

```yaml
docker-compose.yml:10-12
MYSQL_ROOT_PASSWORD: rootpassword
MYSQL_USER: sga_user
MYSQL_PASSWORD: sga_password
```

```sql
docker/init.sql:38-40
-- Usando um hash de exemplo para 'admin123'
INSERT INTO usuario (...) VALUES (..., 'admin@sga.com', '...', 'ADMINISTRADOR', TRUE);
```

- **Impacto potencial:** acesso nao autorizado ao banco ou painel administrativo em ambientes reaproveitados; vazamento de dados pessoais e alteracao de boletos/usuarios.
- **Recomendacao:**

```yaml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
  MYSQL_USER: ${MYSQL_USER}
  MYSQL_PASSWORD: ${MYSQL_PASSWORD}
```

Exigir troca da senha inicial no primeiro login, nao documentar senha real no README e manter valores apenas em `.env` local nao versionado ou Secret Manager.
- **Referencias:** CWE-798, CWE-259, OWASP A02/A07.

### 3. Hash de senha exposto nas respostas de usuarios

- **Localizacao:** `backend/src/main/java/com/sga/model/UsuarioModel.java`, linhas 20-21; `backend/src/main/java/com/sga/controller/UsuarioController.java`, linhas 20-30 e 33-41.
- **Funcao/area:** listagem, criacao e busca de usuarios.
- **OWASP:** A01 Broken Access Control, A04 Cryptographic Failures.
- **Severidade:** alta.
- **Descricao:** o controller retorna diretamente `UsuarioModel`. Como `senhaHash` e campo publico e nao ha `@JsonIgnore` ou DTO de saida, hashes BCrypt podem ser expostos para administradores e qualquer integracao com esse endpoint.
- **Evidencia:**

```java
// UsuarioModel.java:20-21
@Column(name = "senha_hash", nullable = false)
public String senhaHash;

// UsuarioController.java:22-30
public List<UsuarioModel> listar() {
    return usuarioService.listarTodos();
}
```

- **Impacto potencial:** vazamento de hashes para ataque offline; ampliacao de dano em caso de conta administrativa comprometida.
- **Recomendacao:**

```java
public record UsuarioResponse(Integer id, String nome, String email, UsuarioModel.Perfil perfil, Boolean ativo) {}
```

Mapear entidades para DTOs de resposta ou marcar o campo:

```java
@JsonIgnore
public String senhaHash;
```

Preferir DTOs para tambem evitar mass assignment.
- **Referencias:** CWE-200, CWE-359, OWASP A01/A04.

### 4. Dev mode, debug remoto e logs verbosos expostos

- **Localizacao:** `docker-compose.yml`, linhas 28 e 33-35; `backend/Dockerfile.jvm`, linhas 20-22; `backend/src/main/resources/application.properties`, linhas 25 e 45-47.
- **Funcao/area:** execucao backend e observabilidade.
- **OWASP:** A02 Security Misconfiguration, A09 Security Logging and Alerting Failures.
- **Severidade:** alta.
- **Descricao:** o backend roda em `quarkus:dev`, expõe porta de debug remota `5006/5005`, habilita `DEBUG_MODE=true`, Hibernate `DEBUG` e Quarkus `TRACE`.
- **Evidencia:**

```yaml
docker-compose.yml:28
command: mvn quarkus:dev ... -Ddebug=5006 -DdebugHost=0.0.0.0

docker-compose.yml:35
- "5006:5006"
```

```properties
application.properties:46-47
quarkus.log.category."org.hibernate".level=DEBUG
quarkus.log.category."io.quarkus".level=TRACE
```

- **Impacto potencial:** depuracao remota indevida, exposicao de SQL/dados sensiveis em logs, superficie extra para execucao ou introspeccao de codigo.
- **Recomendacao:**

```yaml
backend:
  command: java -jar /deployments/quarkus-run.jar
  ports:
    - "8080:8080"
  environment:
    DEBUG_MODE: "false"
```

Remover portas de debug em ambientes compartilhados/produtivos e usar `INFO`/`WARN` com mascaramento de dados sensiveis.
- **Referencias:** CWE-489, CWE-215, CWE-532, OWASP A02/A09.

### 5. Dependencias frontend com vulnerabilidades conhecidas

- **Localizacao:** `frontend/package.json`, linhas 13-30; `frontend/package-lock.json`.
- **Funcao/area:** cadeia de suprimentos JavaScript.
- **OWASP:** A03 Software Supply Chain Failures.
- **Severidade:** alta.
- **Descricao:** `npm audit --json` reportou 50 vulnerabilidades: 28 altas, 15 moderadas e 7 baixas. Exemplos: Angular 18.2.14 com XSS em i18n/SVG e vazamentos via `HttpTransferCache`; `vite`, `tar`, `rollup`, `serialize-javascript` e `webpack-dev-server` vulneraveis em dependencias transitivas.
- **Evidencia:**

```json
frontend/package.json:13-20
"@angular/core": "^18.0.0",
"@angular/common": "^18.0.0",
"@angular/compiler": "^18.0.0"
```

Resultado de `npm audit --json`:

```text
metadata.vulnerabilities: high=28, moderate=15, low=7, total=50
Angular advisories: GHSA-g93w-mfhg-p222, GHSA-jrmj-c5cx-3cw6, GHSA-v4hv-rgfq-gp49
```

- **Impacto potencial:** XSS, leitura/escrita arbitraria em ambiente de build, vazamento de codigo-fonte em dev server, riscos de integridade da cadeia de build.
- **Recomendacao:**

```bash
cd frontend
npm audit
npm update
ng update @angular/core @angular/cli
```

Planejar upgrade compatível para versoes corrigidas e reexecutar `npm audit` em CI.
- **Referencias:** CWE-79, CWE-22, CWE-918, GHSA-g93w-mfhg-p222, GHSA-jrmj-c5cx-3cw6, GHSA-v4hv-rgfq-gp49, OWASP A03.

### 6. Dependencias Java legadas e repositorio local sem governanca

- **Localizacao:** `backend/pom.xml`, linhas 64-82; `backend/local-maven-repo/**`.
- **Funcao/area:** cadeia de suprimentos Java.
- **OWASP:** A03 Software Supply Chain Failures, A08 Software or Data Integrity Failures.
- **Severidade:** alta.
- **Descricao:** o backend depende de bibliotecas antigas: `log4j:1.2.16`, `itext:2.1.7`, `commons-lang:2.4` e `jrimum:0.2.0-local`. Artefatos JAR estao versionados em repositorio local, sem verificacao de assinatura, SBOM ou politica de atualizacao.
- **Evidencia:**

```xml
backend/pom.xml:64-82
<artifactId>jrimum</artifactId><version>0.2.0-local</version>
<artifactId>commons-lang</artifactId><version>2.4</version>
<artifactId>log4j</artifactId><version>1.2.16</version>
<artifactId>itext</artifactId><version>2.1.7</version>
```

- **Impacto potencial:** exploracao de vulnerabilidades conhecidas ou transitivas, inclusao de artefatos adulterados e dificuldade de auditoria.
- **Recomendacao:** remover `log4j` 1.x se nao for indispensavel; migrar para logging gerenciado pelo Quarkus/SLF4J; atualizar bibliotecas legadas; gerar SBOM e adicionar scanner de dependencias no CI.

```bash
cd backend
./mvnw.cmd dependency:tree
./mvnw.cmd org.owasp:dependency-check-maven:check
```

- **Referencias:** CWE-1104, CWE-494, CVE-2021-4104 e CVE-2019-17571 para cenarios especificos de Log4j 1.x, OWASP A03.

### 7. Token JWT armazenado em localStorage e API em HTTP

- **Localizacao:** `frontend/src/app/core/services/auth.service.ts`, linhas 13 e 20-23; `frontend/src/environments/environment.ts`, linhas 1-3; `frontend/src/app/core/interceptors/auth.interceptor.ts`, linhas 15-20.
- **Funcao/area:** armazenamento de sessao no frontend.
- **OWASP:** A04 Cryptographic Failures, A07 Authentication Failures.
- **Severidade:** alta.
- **Descricao:** o JWT e persistido em `localStorage` e enviado por `Authorization`. Qualquer XSS no frontend consegue ler o token. A URL de API usa `http://`, sem TLS.
- **Evidencia:**

```typescript
// auth.service.ts:13,22
currentUser = signal<any>(JSON.parse(localStorage.getItem('currentUser') || '{}'));
localStorage.setItem('currentUser', JSON.stringify(user));

// environment.ts:3
apiUrl: 'http://localhost:8080/api/v1'
```

- **Impacto potencial:** sequestro de sessao por XSS, interceptacao de token em rede sem TLS, replay de tokens ate expiracao.
- **Recomendacao:** usar HTTPS e, se possivel, cookie `HttpOnly; Secure; SameSite=Lax/Strict` para sessao. Se mantiver Bearer token, reduzir TTL, usar CSP forte e nao persistir token em armazenamento acessivel por JavaScript.

```http
Set-Cookie: access_token=...; HttpOnly; Secure; SameSite=Lax; Path=/
```

- **Referencias:** CWE-922, CWE-319, CWE-79, OWASP A04/A07.

### 8. Login sem protecao contra brute force

- **Localizacao:** `backend/src/main/java/com/sga/controller/AuthController.java`, linhas 22-30; `backend/src/main/java/com/sga/service/AuthService.java`, linhas 19-35.
- **Funcao/area:** autenticacao.
- **OWASP:** A07 Authentication Failures.
- **Severidade:** alta.
- **Descricao:** nao ha rate limiting, bloqueio temporario, contagem de tentativas, CAPTCHA adaptativo ou logging estruturado de falhas de login.
- **Evidencia:**

```java
// AuthController.java:24-30
public Response login(LoginDto loginDto) {
    try {
        AuthResponseDto response = authService.login(loginDto);
        return Response.ok(response).build();
    } catch (RuntimeException e) {
        return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    }
}
```

- **Impacto potencial:** ataques de senha por forca bruta, credential stuffing e descoberta de credenciais do administrador inicial.
- **Recomendacao:** adicionar rate limit por IP/email, atraso progressivo, bloqueio temporario e auditoria de tentativas.

```java
// Exemplo conceitual
if (loginRateLimiter.isBlocked(loginDto.email, clientIp)) {
    return Response.status(429).build();
}
```

- **Referencias:** CWE-307, CWE-799, OWASP A07.

### 9. Ausencia de cabecalhos de seguranca HTTP

- **Localizacao:** `frontend/nginx.conf`, linhas 1-14; `backend/src/main/resources/application.properties`, linhas 28-43.
- **Funcao/area:** configuracao HTTP frontend/backend.
- **OWASP:** A02 Security Misconfiguration.
- **Severidade:** media.
- **Descricao:** nao ha configuracao de `Content-Security-Policy`, `Strict-Transport-Security`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` ou `Permissions-Policy`.
- **Evidencia:**

```nginx
frontend/nginx.conf:1-9
server {
    listen 80;
    server_name localhost;
    ...
}
```

- **Impacto potencial:** maior impacto de XSS, clickjacking, MIME sniffing e vazamento de referer.
- **Recomendacao:**

```nginx
add_header Content-Security-Policy "default-src 'self'; connect-src 'self' https://api.exemplo.com; frame-ancestors 'none'" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-Frame-Options "DENY" always;
add_header Referrer-Policy "no-referrer" always;
add_header Permissions-Policy "geolocation=(), microphone=(), camera=()" always;
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
```

- **Referencias:** CWE-1021, CWE-693, CWE-79, OWASP A02.

### 10. CORS com credenciais habilitadas

- **Localizacao:** `backend/src/main/resources/application.properties`, linhas 29-33.
- **Funcao/area:** politica CORS da API.
- **OWASP:** A02 Security Misconfiguration.
- **Severidade:** media.
- **Descricao:** CORS esta habilitado com `access-control-allow-credentials=true`. Embora a origem esteja restrita a `http://localhost:4200`, esta configuracao e perigosa se copiada para producao ou ampliada para curingas/domínios menos confiaveis.
- **Evidencia:**

```properties
quarkus.http.cors.enabled=true
quarkus.http.cors.origins=http://localhost:4200
quarkus.http.cors.access-control-allow-credentials=true
```

- **Impacto potencial:** exposicao de respostas autenticadas para origens indevidas em caso de configuracao incorreta; ampliacao de risco em ataques XSS/CSRF quando cookies forem adotados.
- **Recomendacao:** parametrizar origem por ambiente, manter lista estrita e desabilitar credenciais se nao houver cookies/autenticacao baseada em credenciais de navegador.

```properties
quarkus.http.cors.origins=${CORS_ORIGINS:https://app.exemplo.com}
quarkus.http.cors.access-control-allow-credentials=false
```

- **Referencias:** CWE-942, OWASP A02.

### 11. Validacao insuficiente de entradas do usuario

- **Localizacao:** `backend/src/main/java/com/sga/dto/LoginDto.java`, linhas 3-5; `UsuarioModel.java`, linhas 14-27; `AssociadoModel.java`, linhas 14-25; `BoletoModel.java`, linhas 16-28; services `salvar` em `UsuarioService.java`, linhas 16-22, `AssociadoService.java`, linhas 15-18, `BoletoService.java`, linhas 57-60.
- **Funcao/area:** criacao de usuario, associado, boleto e login.
- **OWASP:** A05 Injection, A06 Insecure Design, A10 Mishandling of Exceptional Conditions.
- **Severidade:** media.
- **Descricao:** nao ha Bean Validation (`@NotBlank`, `@Email`, `@Pattern`, `@Positive`, `@FutureOrPresent`, `@Valid`) nos DTOs/modelos recebidos pela API. A validacao fica quase toda no banco ou no frontend.
- **Evidencia:**

```java
// LoginDto.java:3-5
public class LoginDto {
    public String email;
    public String password;
}

// AssociadoModel.java:17-21
public String cpf;
public String email;
```

- **Impacto potencial:** dados invalidos persistidos, erros 500 por constraint do banco, entrada malformada em PDFs, maior superficie para injection em bibliotecas externas.
- **Recomendacao:**

```java
public class LoginDto {
    @Email @NotBlank
    public String email;

    @NotBlank
    public String password;
}
```

Aplicar `@Valid` nos controllers e regras como CPF com `@Pattern(regexp = "\\d{11}")`, boleto com `@Positive` e data de vencimento valida.
- **Referencias:** CWE-20, CWE-1284, OWASP A05/A06.

### 12. Tratamento de excecoes inconsistente e mensagens internas ao cliente

- **Localizacao:** `backend/src/main/java/com/sga/controller/AuthController.java`, linhas 24-30; `backend/src/main/java/com/sga/service/BoletoService.java`, linhas 68-77 e 176-190.
- **Funcao/area:** erros REST e geracao de boleto.
- **OWASP:** A10 Mishandling of Exceptional Conditions.
- **Severidade:** media.
- **Descricao:** excecoes de servico sao lancadas como `RuntimeException`; no login, a mensagem da excecao e enviada diretamente ao cliente. Nao ha `ExceptionMapper` global para padronizar respostas e logging seguro.
- **Evidencia:**

```java
// AuthController.java:28-30
} catch (RuntimeException e) {
    return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
}

// BoletoService.java:70-76
throw new RuntimeException("Boleto nao encontrado");
throw new RuntimeException("Associado do boleto nao encontrado");
```

- **Impacto potencial:** vazamento de detalhes internos, respostas inconsistentes, dificuldade de monitorar falhas reais e possivel exposicao de caminhos/erros de bibliotecas.
- **Recomendacao:** criar `ExceptionMapper` global com mensagens genericas e logs internos com correlation id.

```java
@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {
    public Response toResponse(RuntimeException e) {
        return Response.status(500).entity(Map.of("erro", "Erro interno")).build();
    }
}
```

- **Referencias:** CWE-209, CWE-755, OWASP A10.

### 13. Banco MySQL exposto na porta do host

- **Localizacao:** `docker-compose.yml`, linhas 13-14.
- **Funcao/area:** infraestrutura Docker.
- **OWASP:** A02 Security Misconfiguration.
- **Severidade:** media.
- **Descricao:** MySQL e publicado em `3306:3306`. Em maquinas compartilhadas ou servidores, isso expõe o banco fora da rede interna Docker.
- **Evidencia:**

```yaml
docker-compose.yml:13-14
ports:
  - "3306:3306"
```

- **Impacto potencial:** acesso direto ao banco se credenciais vazarem ou forem fracas; bypass da camada de autorizacao da API.
- **Recomendacao:** remover a publicacao da porta por padrao e usar redes internas; publicar apenas em perfil local.

```yaml
mysql-db:
  expose:
    - "3306"
```

- **Referencias:** CWE-200, CWE-284, OWASP A02.

### 14. Dados bancarios/identificadores de cobranca hardcoded

- **Localizacao:** `backend/src/main/java/com/sga/service/BoletoService.java`, linhas 40-45.
- **Funcao/area:** geracao de boleto.
- **OWASP:** A04 Cryptographic Failures, A02 Security Misconfiguration.
- **Severidade:** media.
- **Descricao:** conta, agencia, CNPJ e dados do cedente estao fixos no codigo-fonte. Embora nao sejam necessariamente segredos criptograficos, sao dados sensiveis de negocio e tendem a variar por ambiente/empresa.
- **Evidencia:**

```java
private static final String CONTA = "047829";
private static final String AGENCIA = "2525";
private static final String CNPJ = "07.969.101/0001-14";
```

- **Impacto potencial:** exposicao de dados bancarios, erro operacional em ambientes diferentes e dificuldade de rotacao/segregacao por empresa.
- **Recomendacao:** mover para configuracao protegida ou tabela `empresa`, validada e acessada pelo service.

```properties
sga.boleto.conta=${BOLETO_CONTA}
sga.boleto.agencia=${BOLETO_AGENCIA}
```

- **Referencias:** CWE-200, CWE-798, OWASP A02/A04.

### 15. Guards frontend confiam em papel armazenado no cliente para exibicao de UI

- **Localizacao:** `frontend/src/app/core/services/auth.service.ts`, linhas 32-45; `frontend/src/app/core/guards/admin.guard.ts`, linhas 11-17; `frontend/src/app/shared/components/app-shell.component.ts`, linhas 21-27.
- **Funcao/area:** roteamento e menu frontend.
- **OWASP:** A01 Broken Access Control.
- **Severidade:** baixa.
- **Descricao:** o menu e os guards usam `perfil` vindo do `localStorage`. O backend possui `@RolesAllowed`, entao o risco principal e manipulacao de UI/experiencia, mas nao deve ser tratado como controle de seguranca.
- **Evidencia:**

```typescript
// admin.guard.ts:11-12
if (this.authService.isLoggedIn() && this.authService.getRole() === 'ADMINISTRADOR') {
```

- **Impacto potencial:** usuario altera `localStorage` para ver menu/rota administrativa, gerando chamadas que o backend deve negar; risco aumenta se algum endpoint ficar sem RBAC.
- **Recomendacao:** manter RBAC obrigatorio no backend, derivar papel do JWT validado e tratar guards apenas como conveniencia de UX.
- **Referencias:** CWE-602, OWASP A01.

### 16. Permissao OPTIONS ampla para todas as rotas

- **Localizacao:** `backend/src/main/resources/application.properties`, linhas 35-37.
- **Funcao/area:** configuracao HTTP/CORS.
- **OWASP:** A02 Security Misconfiguration.
- **Severidade:** baixa.
- **Descricao:** todas as rotas permitem `OPTIONS` publicamente. Isso e comum para CORS, mas deve ser revisado junto das origens permitidas e metodos expostos.
- **Evidencia:**

```properties
quarkus.http.auth.permission.options.paths=/*
quarkus.http.auth.permission.options.methods=OPTIONS
quarkus.http.auth.permission.options.policy=permit
```

- **Impacto potencial:** facilita enumeracao de superficie HTTP e pode mascarar configuracoes CORS permissivas.
- **Recomendacao:** manter apenas se necessario, com CORS estrito por ambiente e sem credenciais quando nao usadas.
- **Referencias:** CWE-693, OWASP A02.

## Observacoes Complementares

- Nao foi identificada concatenacao direta de SQL com entrada do usuario nos trechos inspecionados; o uso de Panache como `find("email", loginDto.email)` reduz risco classico de SQL injection. Ainda assim, faltam validacoes de entrada.
- Nao foi encontrado uso direto de `innerHTML`, `bypassSecurityTrust*`, `eval` ou `document.write` no frontend. O risco de XSS observado vem principalmente de dependencias vulneraveis, ausencia de CSP e armazenamento de token em `localStorage`.
- A inspecao foi superficial, conforme solicitado; recomenda-se complementar com SAST, DAST, secret scanning, dependency scanning Maven/NPM e revisao manual de fluxos de autorizacao.
