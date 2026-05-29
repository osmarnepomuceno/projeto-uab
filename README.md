# Sistema de Gestão de Associados (SGA) - Ambiente de Desenvolvimento

Este projeto consiste em um backend Quarkus, um frontend Angular e um banco de dados MySQL.

## Estrutura do Projeto

- `backend/`: Código-fonte do backend (Quarkus)
- `frontend/`: Código-fonte do frontend (Angular)
- `docker/`: Scripts e configurações de contêineres
- `docker-compose.yml`: Orquestração do ambiente completo

## Como Executar

### Pré-requisitos
- Docker e Docker Compose
- Java 17+
- Node.js 22+

### Usando Docker Compose (Recomendado)

Para subir todo o ambiente (Banco, Backend e Frontend):
```bash
docker compose up --build
```

*Nota: O primeiro build do frontend pode demorar um pouco devido à instalação de dependências e compilação.*

### Executando Manualmente para Desenvolvimento

#### 1. Banco de Dados
```bash
docker compose up mysql-db -d
```

#### 2. Backend (Quarkus Dev Mode)
```bash
cd backend
./mvnw quarkus:dev
```
O backend estará disponível em `http://localhost:8080`.

#### 3. Frontend (Angular Dev Server)
```bash
cd frontend
npm install
npm start
```
O frontend estará disponível em `http://localhost:4200`.

## Configurações Iniciais

- **MySQL:** 
  - Porta: 3306
  - Database: `sga_db`
  - Usuário: `sga_user`
  - Senha: `sga_password`
- **Administrador Inicial:**
  - Email: `admin@sga.com`
  - Senha: `admin123` (conforme hash no `init.sql`)

## Tecnologias Utilizadas
- **Backend:** Quarkus (Java 17), Hibernate ORM (Panache), RESTEasy Reactive, SmallRye JWT.
- **Frontend:** Angular 19+, TypeScript, SSR/SSG habilitados.
- **Banco de Dados:** MySQL 8.0.
