# Plano de Testes - TDD First

## 1. Contexto

Este plano de testes foi elaborado com base no arquivo `especificacao.md` do Sistema de Gerenciamento de Associacao. O projeto utiliza arquitetura client-server, com backend em Java/Quarkus, frontend em Angular, banco MySQL e execucao via Docker.

O objetivo e orientar a evolucao do sistema por TDD First, garantindo que testes automatizados sejam escritos antes da implementacao ou ajuste funcional. A estrategia prioriza cenarios criticos de autenticacao, autorizacao, regras de negocio, integridade de dados e comunicacao entre frontend e backend.

## 2. Observacao Sobre Dependencias

O prompt original solicita revisao de `requirements.txt`, porem este projeto nao e Python e nao possui esse arquivo. As dependencias de teste devem ser controladas pelos arquivos reais do projeto:

- Backend: `backend/pom.xml`
- Frontend: `frontend/package.json`
- Infraestrutura: `docker-compose.yml`

Dependencias ja identificadas:

- Backend possui `quarkus-junit5`, suficiente para iniciar testes de unidade e integracao com Quarkus.
- Para testes REST automatizados do backend, recomenda-se adicionar `io.rest-assured:rest-assured` com escopo `test`.
- Para testes unitarios Angular via `ng test`, recomenda-se configurar o target `test` no `angular.json` e adicionar dependencias de teste como `jasmine-core`, `@types/jasmine`, `karma`, `karma-jasmine`, `karma-chrome-launcher`, `karma-coverage` e `karma-jasmine-html-reporter`.

Como este documento define o plano tecnico, alteracoes em dependencias devem ser feitas junto da criacao efetiva dos primeiros testes automatizados, evitando dependencias nao utilizadas.

## 3. Analise Do Prompt Original

### Prompt Original

> Com base nas especificacoes do arquivo especificacao.md, elabore um Plano de Testes focado em TDD First, conforme as diretrizes a seguir.
>
> Para cada funcionalidade, deve-se definir um teste com prioridade a cenarios criticos. Quando necessario pode se fazer uso de mocks para simular dependencias.
>
> A estrategia de teste deve ser automatizada e capaz de validar alteracoes evitando regressoes.
>
> Revise o arquivo requirements.txt e inclua eventuais dependencias necessarias para a execucao dos testes.
>
> O plano de testes gerado deve ser documentado, de forma tecnica e estruturada, em arquivo especifico chamado testing.md.

### Melhorias Identificadas

- O projeto nao possui `requirements.txt`; o prompt deve mencionar `pom.xml`, `package.json` e `docker-compose.yml`.
- O plano precisa separar testes de unidade, integracao, contrato REST, frontend e fluxo end-to-end.
- O plano deve mapear papeis RBAC: Administrador, Atendente e usuario nao autenticado.
- O plano deve priorizar seguranca: JWT, rotas protegidas, hash de senha e expiracao de token.
- A estrategia deve prever execucao automatizada em pipeline CI.
- Os testes devem ser escritos antes do codigo de producao ou antes de qualquer correcao funcional relevante.

### Prompt Revisado

> Com base nas especificacoes do arquivo `especificacao.md` e na arquitetura real do projeto Java/Quarkus + Angular + MySQL, elabore um Plano de Testes focado em TDD First.
>
> Para cada funcionalidade, defina cenarios de teste priorizando riscos criticos, seguranca, regras de negocio, integridade de dados, autorizacao RBAC e contratos REST. Quando necessario, use mocks para dependencias externas como banco, JWT, servicos de boleto e chamadas HTTP do frontend.
>
> A estrategia deve ser automatizada, executavel localmente e adequada para CI/CD, validando alteracoes e evitando regressoes.
>
> Revise os arquivos reais de dependencias do projeto, incluindo `backend/pom.xml`, `frontend/package.json`, `angular.json` e `docker-compose.yml`, indicando dependencias necessarias para testes unitarios, integrados, REST, frontend e end-to-end.
>
> Documente o resultado tecnico e estruturado em `testing.md`, incluindo matriz de funcionalidades, tipos de teste, prioridade, criterios de aceite, comandos de execucao e ordem recomendada de implementacao TDD.

## 4. Estrategia Geral De Testes

### Piramide De Testes

1. Testes unitarios: validam regras de negocio isoladas em services, guards e componentes.
2. Testes de integracao: validam controllers, persistencia, configuracoes Quarkus e serializacao JSON.
3. Testes de contrato REST: validam endpoints, status HTTP, payloads e autorizacao.
4. Testes de frontend: validam componentes, guards, services Angular e interceptors HTTP.
5. Testes end-to-end: validam fluxos criticos completos usando ambiente Docker.

### Politica TDD First

Para cada nova funcionalidade ou correcao:

1. Escrever teste automatizado que falha.
2. Implementar o menor codigo necessario para passar.
3. Refatorar mantendo testes verdes.
4. Executar suite local.
5. Submeter alteracao somente com testes automatizados relevantes.

## 5. Matriz De Funcionalidades E Testes

| Funcionalidade | Tipo | Prioridade | Cenario Critico | Resultado Esperado |
|---|---|---:|---|---|
| Seed do banco | Integracao/infra | Alta | Banco sobe com administrador inicial | Usuario admin existe, ativo e com perfil `ADMINISTRADOR` |
| Configuracao JWT | Integracao | Alta | Backend inicia com chave publica/privada validas | Token assinado e validado sem erro de chave nula |
| Login | Unitario/integracao | Alta | Credenciais validas | Retorna JWT, nome e perfil |
| Login invalido | Unitario/integracao | Alta | Senha incorreta ou email inexistente | Retorna HTTP 401 |
| Expiracao JWT | Unitario/integracao | Alta | Token expirado acessa rota protegida | Retorna HTTP 401 |
| RBAC Administrador | Contrato REST | Alta | Admin acessa `/usuarios` | Acesso permitido |
| RBAC Atendente | Contrato REST | Alta | Atendente acessa `/usuarios` | Acesso negado |
| CRUD Usuarios | Unitario/contrato | Alta | Admin cria usuario | Usuario persistido com senha BCrypt |
| CRUD Associados | Unitario/contrato | Alta | Admin/Atendente cadastra associado | Associado persistido com status valido |
| Validacao Associado | Unitario | Alta | CPF/email duplicado | Operacao rejeitada |
| Boleto | Unitario/integracao | Alta | Criar boleto para associado existente | Boleto salvo como `PENDENTE` |
| Boleto PDF | Integracao | Media | Baixar PDF de boleto existente | Retorna `application/pdf` |
| AdminGuard Angular | Unitario frontend | Alta | Perfil nao admin acessa usuarios | Redireciona para `/unauthorized` |
| AuthInterceptor | Unitario frontend | Alta | Chamada protegida com token salvo | Header `Authorization` incluido |
| AuthInterceptor login | Unitario frontend | Alta | Chamada `/auth/login` | Header `Authorization` nao incluido |
| AssociadoService Angular | Unitario frontend | Media | Listar/cadastrar/remover associado | Chamadas HTTP corretas |
| Menu frontend | Unitario frontend | Media | Usuario admin autenticado | Menu exibe Usuarios |
| Menu frontend atendente | Unitario frontend | Media | Usuario atendente autenticado | Menu oculta Usuarios |
| Fluxo completo | E2E | Alta | Login admin, criar associado, criar boleto | Dados aparecem nas telas e API responde corretamente |

## 6. Plano TDD Por Camada

### 6.1 Backend - AuthService

Testes a escrever antes da implementacao/correcao:

- `deveGerarTokenQuandoCredenciaisForemValidas`
- `deveRejeitarLoginComSenhaIncorreta`
- `deveRejeitarLoginComUsuarioInexistente`
- `deveIncluirPerfilNoToken`
- `deveDefinirExpiracaoDoToken`

Mocks recomendados:

- Mock ou fixture de `UsuarioModel.find`.
- Hash BCrypt conhecido para validar senha.

Criterios de aceite:

- Senha nunca deve ser retornada em payload.
- Token deve conter `iss`, `upn`, `groups`, `full_name` e `exp`.

### 6.2 Backend - UsuarioService E UsuarioController

Testes:

- `deveListarUsuariosSomenteComoAdministrador`
- `deveCriarUsuarioComSenhaCriptografada`
- `naoDevePermitirAtendenteCriarUsuario`
- `deveBuscarUsuarioPorIdExistente`
- `deveRetornar404ParaUsuarioInexistente`
- `deveRemoverUsuarioExistente`

Mocks recomendados:

- Para unidade: mock de persistencia Panache.
- Para contrato REST: `@QuarkusTest` com tokens JWT de admin e atendente.

Criterios de aceite:

- Perfil `ATENDENTE` nao executa CRUD de usuarios.
- Campo `senhaHash` deve ser BCrypt, nunca texto puro.

### 6.3 Backend - AssociadoService E AssociadoController

Testes:

- `deveCadastrarAssociadoComStatusAtivoPadrao`
- `deveListarAssociadosComoAdmin`
- `deveListarAssociadosComoAtendente`
- `deveRejeitarCpfDuplicado`
- `deveRejeitarEmailDuplicado`
- `devePermitirExclusaoSomenteParaAdmin`

Mocks recomendados:

- Mock de repositorio/Panache para unidade.
- Banco de teste para integracao.

Criterios de aceite:

- CPF deve possuir 11 caracteres.
- Status deve estar entre `ATIVO`, `INADIMPLENTE`, `INATIVO`.

### 6.4 Backend - BoletoService E BoletoController

Testes:

- `deveCriarBoletoPendenteParaAssociadoExistente`
- `deveRejeitarBoletoSemAssociado`
- `deveListarBoletosPorAssociado`
- `deveGerarPdfParaBoletoExistente`
- `deveRetornarErroParaPdfDeBoletoInexistente`

Mocks recomendados:

- Mock da camada jRimum/Bopepo para evitar dependencia externa em teste unitario.
- Fixture de `AssociadoModel` e `EmpresaModel`.

Criterios de aceite:

- Boleto novo deve iniciar como `PENDENTE`.
- PDF deve retornar bytes e content-type `application/pdf`.
- Integracao com jRimum deve ficar isolada em service.

### 6.5 Frontend - AuthService E AuthInterceptor

Testes:

- `deveSalvarUsuarioNoLocalStorageAposLogin`
- `deveLimparUsuarioAntesDeLogin`
- `deveRemoverUsuarioNoLogout`
- `deveInformarUsuarioLogadoQuandoTokenExiste`
- `deveAdicionarBearerTokenEmRotasProtegidas`
- `naoDeveAdicionarBearerTokenNoLogin`

Mocks recomendados:

- `HttpTestingController`.
- Mock de `localStorage`.

Criterios de aceite:

- Login nao deve carregar token expirado antigo.
- Interceptor deve proteger chamadas exceto `/auth/login`.

### 6.6 Frontend - Guards E Menu

Testes:

- `authGuardDevePermitirUsuarioLogado`
- `authGuardDeveRedirecionarUsuarioNaoLogado`
- `adminGuardDevePermitirAdministrador`
- `adminGuardDeveRedirecionarAtendente`
- `menuDeveExibirUsuariosParaAdministrador`
- `menuDeveOcultarUsuariosParaAtendente`

Mocks recomendados:

- Mock de `Router`.
- Mock de `AuthService`.

Criterios de aceite:

- Usuario sem token nao acessa area interna.
- Atendente nao visualiza nem acessa rota de usuarios.

### 6.7 Frontend - Associados, Usuarios E Boletos

Testes:

- `associadoServiceDeveChamarEndpointDeListagem`
- `associadoServiceDevePostarNovoAssociado`
- `associadoComponentDeveAtualizarTabelaAposCadastro`
- `usuarioComponentDeveCriarUsuarioComoAdmin`
- `boletoComponentDeveCarregarAssociados`
- `boletoComponentDeveCriarBoletoComAssociadoSelecionado`
- `boletoComponentDeveMontarLinkPdf`

Mocks recomendados:

- Services mockados em teste de componente.
- `HttpTestingController` em teste de service.

Criterios de aceite:

- Componentes devem tratar erro de API sem quebrar a tela.
- Formularios devem enviar payload compativel com a API.

## 7. Estrategia De Automacao

### Backend

Comandos previstos:

```bash
cd backend
./mvnw test
./mvnw verify
```

Suites recomendadas:

- Unitarios: services e validacoes.
- Integracao: controllers com `@QuarkusTest`.
- Contrato REST: endpoints com tokens admin/atendente.

### Frontend

Comandos previstos:

```bash
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
npm run build
```

Suites recomendadas:

- Services Angular com `HttpTestingController`.
- Guards com mocks de `Router`.
- Componentes standalone com `TestBed`.

### Docker/E2E

Comandos previstos:

```bash
docker compose up -d --build
```

Cenarios E2E minimos:

- Login como administrador.
- Criar associado.
- Criar boleto para associado.
- Acessar link de PDF.
- Verificar que rota de usuarios exige admin.

## 8. Ordem Recomendada De Implementacao TDD

1. AuthService backend e validacao JWT.
2. AuthInterceptor e AuthGuard frontend.
3. AdminGuard e RBAC em controllers.
4. UsuarioService e UsuarioController.
5. AssociadoService e AssociadoController.
6. BoletoService com mock de jRimum.
7. Componentes Angular de menu, associados, usuarios e boletos.
8. E2E Docker para fluxo administrativo principal.

## 9. Criterios De Qualidade

- Cada regra critica deve ter pelo menos um teste automatizado.
- Cada bug corrigido deve gerar um teste que reproduz o problema antes da correcao.
- Endpoints protegidos devem ter testes para admin, atendente e usuario sem token.
- Testes de frontend devem validar comportamento e nao apenas criacao de componentes.
- Build do frontend e testes do backend devem executar em CI antes de merge.
- Dados sensiveis, como senha, nao podem aparecer em logs nem respostas JSON.

## 10. Proposta De Pipeline CI

```yaml
name: ci

on:
  pull_request:
  push:
    branches:
      - main
      - develop

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - run: ./mvnw test
        working-directory: backend

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: frontend/package-lock.json
      - run: npm ci
        working-directory: frontend
      - run: npm run build
        working-directory: frontend
```

## 11. Status Do Plano

Este plano esta adequado para iniciar a estrategia TDD First do projeto. As melhorias principais em relacao ao prompt inicial foram:

- Adaptacao do item `requirements.txt` para a stack real.
- Inclusao de testes por camada.
- Priorizacao de RBAC, JWT, senha BCrypt e contratos REST.
- Inclusao de estrategia de automacao local e CI.
- Definicao da ordem recomendada de implementacao TDD.

## 12. Fluxos De Otimizacao Cobertos

Novos cenarios adicionados ao plano:

| Funcionalidade | Tipo | Prioridade | Cenario Critico | Resultado Esperado |
|---|---|---:|---|---|
| Cache de boleto PDF | Unitario | Alta | PDF ja existe no diretorio temporario | Bytes sao retornados sem nova geracao |
| Fila de boleto PDF | Unitario | Alta | Duas requisicoes concorrentes para o mesmo boleto | Apenas um job gera o PDF e ambas recebem bytes |
| Storage de boleto PDF | Unitario | Alta | Nome de arquivo com travessia de diretorio | Caminho e normalizado dentro do diretorio temporario |
| Estilos compartilhados Angular | Build | Media | Componentes usam CSS global comum | `npm run build` compila sem regressao |

Testes automatizados implementados no backend:

- `BoletoStorageServiceTest.deveResolverPdfDentroDoDiretorioConfigurado`
- `BoletoStorageServiceTest.deveLerPdfEmCacheQuandoArquivoExistir`
- `BoletoStorageServiceTest.deveNormalizarNomeComTentativaDeTravessiaDeDiretorio`
- `BoletoPdfJobQueueTest.deveExecutarJobEDevolverBytesGerados`
- `BoletoPdfJobQueueTest.deveDeduplicarJobsConcorrentesParaMesmoBoleto`

Criterios de aceite especificos:

- Geracao de PDF deve consultar cache antes de acionar a biblioteca de boleto.
- Jobs concorrentes do mesmo boleto devem compartilhar a mesma execucao.
- Arquivos de PDF devem permanecer dentro de `sga.boletos.tmp-dir`.
- Componentes Angular devem manter apenas estilos especificos apos a centralizacao do CSS comum.

Comandos executados apos as etapas de refatoracao:

```bash
cd backend
./mvnw.cmd test

cd frontend
npm run build
```

Observacao atual: `npm test -- --watch=false --browsers=ChromeHeadless` foi executado e falhou com `Unknown arguments: watch, browsers`. Ate que o target de testes Angular/Karma seja configurado no `angular.json`, a verificacao automatizada disponivel para frontend e `npm run build`.
