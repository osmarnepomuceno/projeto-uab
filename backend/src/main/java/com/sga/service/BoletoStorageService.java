package com.sga.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@ApplicationScoped
public class BoletoStorageService {

    @ConfigProperty(name = "sga.boletos.tmp-dir")
    String tmpDir;

    private Path boletoTmpPath;

    @PostConstruct
    void init() {
        boletoTmpPath = Path.of(tmpDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(boletoTmpPath);
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel criar o diretorio temporario de boletos: " + boletoTmpPath, e);
        }
    }

    public Path getBoletoTmpPath() {
        return boletoTmpPath;
    }

    public File getBoletoTmpDir() {
        return boletoTmpPath.toFile();
    }

    public Path createTempPdf(String prefix) {
        try {
            return Files.createTempFile(boletoTmpPath, prefix, ".pdf");
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel criar arquivo temporario de boleto em: " + boletoTmpPath, e);
        }
    }

    public Path resolvePdf(String fileName) {
        String safeFileName = normalizePdfFileName(fileName);
        Path resolvedPath = boletoTmpPath.resolve(safeFileName).normalize();

        if (!resolvedPath.startsWith(boletoTmpPath)) {
            throw new IllegalArgumentException("Nome de arquivo de boleto invalido: " + fileName);
        }

        return resolvedPath;
    }

    public Optional<byte[]> readCachedPdf(String fileName) {
        Path pdfPath = resolvePdf(fileName);

        if (!Files.isRegularFile(pdfPath)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Files.readAllBytes(pdfPath));
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler boleto em cache: " + pdfPath, e);
        }
    }

    public byte[] readPdf(Path pdfPath) {
        try {
            return Files.readAllBytes(pdfPath);
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler boleto PDF gerado em: " + pdfPath, e);
        }
    }

    private String normalizePdfFileName(String fileName) {
        String safeFileName = fileName.endsWith(".pdf") ? fileName : fileName + ".pdf";
        return Path.of(safeFileName).getFileName().toString();
    }
}
