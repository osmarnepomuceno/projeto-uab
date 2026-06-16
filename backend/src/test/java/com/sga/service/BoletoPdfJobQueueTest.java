package com.sga.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class BoletoPdfJobQueueTest {

    @Test
    void deveExecutarJobEDevolverBytesGerados() {
        BoletoPdfJobQueue queue = new BoletoPdfJobQueue();

        byte[] bytes = queue.execute(1, () -> new byte[] {7, 8, 9});

        assertArrayEquals(new byte[] {7, 8, 9}, bytes);
        queue.shutdown();
    }

    @Test
    void deveDeduplicarJobsConcorrentesParaMesmoBoleto() throws Exception {
        BoletoPdfJobQueue queue = new BoletoPdfJobQueue();
        AtomicInteger executions = new AtomicInteger();

        Thread first = new Thread(() -> queue.execute(2, () -> {
            executions.incrementAndGet();
            Thread.sleep(100);
            return new byte[] {1};
        }));
        Thread second = new Thread(() -> queue.execute(2, () -> {
            executions.incrementAndGet();
            return new byte[] {2};
        }));

        first.start();
        Thread.sleep(20);
        second.start();
        first.join();
        second.join();

        assertEquals(1, executions.get());
        queue.shutdown();
    }
}
