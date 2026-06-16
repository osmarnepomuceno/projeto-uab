package com.sga.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BoletoStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void deveResolverPdfDentroDoDiretorioConfigurado() {
        BoletoStorageService storage = storage();

        Path pdfPath = storage.resolvePdf("boleto-10");

        assertTrue(pdfPath.startsWith(tempDir.toAbsolutePath().normalize()));
        assertEquals("boleto-10.pdf", pdfPath.getFileName().toString());
    }

    @Test
    void deveLerPdfEmCacheQuandoArquivoExistir() throws Exception {
        BoletoStorageService storage = storage();
        Path pdfPath = storage.resolvePdf("boleto-11");
        byte[] expected = new byte[] {1, 2, 3};
        Files.write(pdfPath, expected);

        byte[] cachedPdf = storage.readCachedPdf("boleto-11").orElseThrow();

        assertArrayEquals(expected, cachedPdf);
    }

    @Test
    void deveNormalizarNomeComTentativaDeTravessiaDeDiretorio() {
        BoletoStorageService storage = storage();

        Path pdfPath = storage.resolvePdf("../boleto-12");

        assertTrue(pdfPath.startsWith(tempDir.toAbsolutePath().normalize()));
        assertEquals("boleto-12.pdf", pdfPath.getFileName().toString());
    }

    private BoletoStorageService storage() {
        BoletoStorageService storage = new BoletoStorageService();
        storage.tmpDir = tempDir.toString();
        storage.init();
        return storage;
    }
}
