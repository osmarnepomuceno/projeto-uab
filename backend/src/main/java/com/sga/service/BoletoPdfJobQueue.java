package com.sga.service;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@ApplicationScoped
public class BoletoPdfJobQueue {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "boleto-pdf-job");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<Integer, Future<byte[]>> jobs = new ConcurrentHashMap<>();

    public byte[] execute(Integer boletoId, Callable<byte[]> job) {
        Future<byte[]> future = jobs.computeIfAbsent(boletoId, ignored -> executor.submit(job));

        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Job de boleto interrompido: " + boletoId, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Job de boleto falhou: " + boletoId, e.getCause());
        } finally {
            jobs.remove(boletoId, future);
        }
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
