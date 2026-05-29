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
-- Usando um hash de exemplo para 'admin123'
INSERT INTO usuario (nome, email, senha_hash, perfil, ativo) 
VALUES ('Administrador Master', 'admin@sga.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeF0.XU.T6y6f9Y/V6YvH6YvH6YvH6YvH', 'ADMINISTRADOR', TRUE);
