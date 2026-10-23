package com.greenocean.backend.local;

import com.greenocean.backend.BackendApplication;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;

/**
 * Development-only entry point. It starts a temporary real PostgreSQL 14 instance,
 * then lets Flyway create and seed the application database.
 */
public final class LocalBackendApplication {
    private static Process postgres;
    private static Path runtimeDirectory;

    private LocalBackendApplication() {}

    public static void main(String[] args) throws IOException {
        runtimeDirectory = Files.createTempDirectory("greenocean-postgres-");
        Path postgresHome = runtimeDirectory.resolve("postgres");
        Path dataDirectory = runtimeDirectory.resolve("data");
        extractPostgres(postgresHome);
        initialize(postgresHome, dataDirectory);
        int port = availablePort();
        start(postgresHome, dataDirectory, port);

        System.setProperty("spring.datasource.url", "jdbc:postgresql://127.0.0.1:" + port + "/postgres");
        System.setProperty("spring.datasource.username", "postgres");
        System.setProperty("spring.datasource.password", "");
        System.setProperty("greenocean.auth.jwt-secret", randomSecret());
        Runtime.getRuntime().addShutdownHook(new Thread(LocalBackendApplication::closePostgres));
        BackendApplication.main(args);
    }

    private static void extractPostgres(Path destination) throws IOException {
        InputStream resource = LocalBackendApplication.class.getClassLoader()
                .getResourceAsStream("postgres-windows-x86_64.txz");
        if (resource == null) throw new IOException("Embedded PostgreSQL Windows binary was not found");
        Files.createDirectories(destination);
        try (resource;
             XZCompressorInputStream xz = new XZCompressorInputStream(resource);
             TarArchiveInputStream tar = new TarArchiveInputStream(xz)) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                Path target = destination.resolve(entry.getName()).normalize();
                if (!target.startsWith(destination)) throw new IOException("Unsafe PostgreSQL archive entry");
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    try (BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(target))) {
                        tar.transferTo(output);
                    }
                }
            }
        }
    }

    private static void initialize(Path home, Path data) throws IOException {
        try {
            int exit = process(home.resolve("bin/initdb.exe").toString(), "-D", data.toString(),
                    "-U", "postgres", "-A", "trust", "-E", "UTF8").waitFor();
            if (exit != 0) throw new IOException("Embedded PostgreSQL initialization failed with exit code " + exit);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while initializing PostgreSQL", exception);
        }
    }

    private static void start(Path home, Path data, int port) throws IOException {
        postgres = process(home.resolve("bin/postgres.exe").toString(), "-D", data.toString(),
                "-h", "127.0.0.1", "-p", Integer.toString(port));
        Instant deadline = Instant.now().plus(Duration.ofSeconds(20));
        while (Instant.now().isBefore(deadline)) {
            if (!postgres.isAlive()) throw new IOException("Embedded PostgreSQL stopped before startup completed");
            try (Socket ignored = new Socket("127.0.0.1", port)) {
                return;
            } catch (IOException ignored) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting for PostgreSQL", exception);
                }
            }
        }
        throw new IOException("Timed out waiting for embedded PostgreSQL");
    }

    private static Process process(String... command) throws IOException {
        return new ProcessBuilder(command).redirectErrorStream(true).inheritIO().start();
    }

    private static int availablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }

    private static String randomSecret() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static void closePostgres() {
        try {
            if (postgres != null && postgres.isAlive()) {
                postgres.destroy();
                if (!postgres.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) postgres.destroyForcibly();
            }
            if (runtimeDirectory != null && Files.exists(runtimeDirectory)) {
                try (var paths = Files.walk(runtimeDirectory)) {
                    paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (IOException ignored) { }
                    });
                }
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException ignored) { }
    }
}
