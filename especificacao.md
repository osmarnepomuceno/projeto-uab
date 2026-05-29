Aqui está a especificação detalhada e determinística do sistema, alinhada com as correções da arquitetura (Angular no frontend) e as convenções de nomenclatura solicitadas.

---

# 4. Especificação (Spec)

### 4.1 Infraestrutura e Banco de Dados

`/docker/init.sql`

* **ação:** criar
* **descrição:** Script SQL de Definição de Dados (DDL) executado na subida do contêiner MySQL para estruturar as tabelas do sistema e garantir a existência do Administrador inicial.
* **pseudocódigo:**

```sql
CREATE DATABASE IF NOT EXISTS sga_db;
USE sga_db;

CREATE TABLE empresa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cnpj VARCHAR(14) UNIQUE NOT NULL,
    razao_social VARCHAR(255) NOT NULL,
    endereco TEXT NOT NULL
);

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    perfil ENUM('ADMINISTRADOR', 'ATENDENTE') NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE TABLE associado (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    status ENUM('ATIVO', 'INADIMPLENTE', 'INATIVO') DEFAULT 'ATIVO'
);

CREATE TABLE boleto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    associado_id INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    status ENUM('PENDENTE', 'PAGO', 'CANCELADO') DEFAULT 'PENDENTE',
    FOREIGN KEY (associado_id) REFERENCES associado(id)
);

-- Inserção do Administrador Inicial (A senha deve ser o hash BCrypt real em produção)
INSERT INTO usuario (nome, email, senha_hash, perfil, ativo) 
VALUES ('Administrador Master', 'admin@sga.com', '$2a$10$HASH_DA_SENHA_INICIAL', 'ADMINISTRADOR', TRUE);

```

### 4.2 Backend - Configurações

`/backend/src/main/resources/application.properties`

* **ação:** criar
* **descrição:** Configurações centrais do framework Quarkus, definindo conexão com o banco de dados, chaves de autenticação JWT e flags de sistema.
* **pseudocódigo:**

```properties
# Configuração de Banco de Dados MySQL
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=${MYSQL_USER}
quarkus.datasource.password=${MYSQL_PASSWORD}
quarkus.datasource.jdbc.url=jdbc:mysql://mysql-db:3306/sga_db

# Configuração JWT
mp.jwt.verify.issuer=https://sga-api.com
smallrye.jwt.sign.key.location=privateKey.pem

# Variáveis de Ambiente do Sistema
sga.system.debugMode=${DEBUG_MODE:false}

```

### 4.3 Backend - Modelos (JPA)

`/backend/src/main/java/com/sga/model/UsuarioModel.java`

* **ação:** criar
* **descrição:** Mapeamento da entidade de banco de dados `usuario` para a classe Java correspondente utilizando anotações JPA.
* **pseudocódigo:**

```java
@Entity
@Table(name = "usuario")
CLASS UsuarioModel
    @Id @GeneratedValue
    id: Integer
    nome: String
    @Column(unique = true)
    email: String
    senhaHash: String
    perfil: String
    ativo: Boolean

    METHOD getId(): Integer
        RETURN id
    
    METHOD setNome(novoNome: String): Void
        nome = novoNome
    
    // Demais getters e setters seguindo o padrão camelCase

```

### 4.4 Backend - Serviços (Regras de Negócio)

`/backend/src/main/java/com/sga/service/BoletoService.java`

* **ação:** criar
* **descrição:** Serviço que contém a lógica de integração com a biblioteca JRimum. Busca os dados da empresa e do associado para construir o documento de cobrança.
* **pseudocódigo:**

```java
@ApplicationScoped
CLASS BoletoService
    DEPENDENCIES: AssociadoRepository, EmpresaRepository
    CONSTANT BOLETO_PENDENTE = "PENDENTE"

    METHOD gerarBoletoPdf(associadoId: Integer, valorBoleto: BigDecimal): byte[]
        associado = AssociadoRepository.findById(associadoId)
        IF associado IS NULL:
            THROW AssociadoNotFoundException("Associado não localizado")
            
        empresa = EmpresaRepository.findFirst()
        
        tituloBoleto = NEW Titulo(associado, empresa, valorBoleto)
        boletoFisico = NEW Boleto(tituloBoleto)
        
        RETURN boletoFisico.getAsPdfBytes()

```

### 4.5 Backend - Controladores (API REST)

`/backend/src/main/java/com/sga/controller/UsuarioController.java`

* **ação:** criar
* **descrição:** Endpoint REST para gerenciamento de usuários. Aplica a restrição de acesso (RBAC) para que apenas perfis autorizados executem o CRUD.
* **pseudocódigo:**

```java
@Path("/api/v1/usuarios")
CLASS UsuarioController
    DEPENDENCIES: UsuarioService
    CONSTANT ADMIN_ROLE = "ADMINISTRADOR"

    @POST
    @RolesAllowed({ADMIN_ROLE})
    METHOD cadastrarUsuario(dadosUsuario: UsuarioDto): Response
        TRY:
            usuarioCriado = UsuarioService.salvarUsuario(dadosUsuario)
            RETURN Response.status(201).entity(usuarioCriado).build()
        CATCH Exception as erro:
            RETURN Response.status(400).entity(erro.getMessage()).build()

```

### 4.6 Frontend - Segurança e Autorização (Angular)

`/frontend/src/app/core/guards/admin.guard.ts`

* **ação:** criar
* **descrição:** Guardião de rota (Route Guard) do Angular para impedir que usuários da classe "Atendente" renderizem ou acessem pelo navegador componentes exclusivos de "Administrador".
* **pseudocódigo:**

```typescript
@Injectable()
CLASS AdminGuard IMPLEMENTS CanActivate
    DEPENDENCIES: AuthService, Router
    CONSTANT ADMIN_ROLE = "ADMINISTRADOR"

    METHOD canActivate(): Boolean
        tokenDecodificado = AuthService.getDecodedToken()
        
        IF tokenDecodificado.perfil === ADMIN_ROLE:
            RETURN TRUE
        ELSE:
            Router.navigate(['/unauthorized'])
            RETURN FALSE

```

### 4.7 Frontend - Integração (Angular)

`/frontend/src/app/modules/associados/associado.service.ts`

* **ação:** criar
* **descrição:** Serviço responsável por disparar requisições HTTP do frontend (Angular) para a API REST (Quarkus) referente aos associados.
* **pseudocódigo:**

```typescript
@Injectable()
CLASS AssociadoService
    DEPENDENCIES: HttpClient
    apiUrl = environment.apiUrl + '/associados'

    METHOD listarTodosAssociados(): Observable
        RETURN HttpClient.get(apiUrl)

    METHOD criarNovoAssociado(payloadAssociado: Object): Observable
        RETURN HttpClient.post(apiUrl, payloadAssociado)
        
    METHOD desativarAssociado(associadoId: Integer): Observable
        RETURN HttpClient.delete(apiUrl + '/' + associadoId)

```