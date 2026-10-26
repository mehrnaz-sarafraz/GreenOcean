package com.greenocean.backend.auth.service;

import java.util.Locale;
import java.util.Set;

public final class PasswordPolicy {
    private PasswordPolicy() {}
    private static final Set<String> BLOCKED = Set.of("passwordpassword", "123456789012345", "1234567890123456", "qwertyuiopasdfgh", "letmeinletmeinletmein");
    private static final Set<String> COMPROMISED = loadBlocklist();
    private static Set<String> loadBlocklist() {
        String file=System.getenv("GREENOCEAN_PASSWORD_BLOCKLIST_SHA256");
        if(file==null || file.isBlank()) return Set.of();
        try(var lines=java.nio.file.Files.lines(java.nio.file.Path.of(file))) {
            var hashes=lines.map(String::trim).filter(v->!v.isEmpty()).map(v->v.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());
            if(hashes.isEmpty() || hashes.stream().anyMatch(v->!v.matches("[a-f0-9]{64}"))) throw new IllegalArgumentException();
            return hashes;
        } catch(Exception e) {throw new IllegalStateException("Password blocklist must contain SHA-256 hashes, one per line");}
    }
    public static boolean hasCompromisedCorpus() { return !COMPROMISED.isEmpty(); }
    public static void validate(String password) {
        int length = password == null ? 0 : password.codePointCount(0, password.length());
        if (length < 15 || length > 128) throw new IllegalArgumentException("Use a password between 15 and 128 characters");
        if (BLOCKED.contains(password.toLowerCase(Locale.ROOT)))
            throw new IllegalArgumentException("Choose a less common password");
        try {
            String hash=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            if(COMPROMISED.contains(hash)) throw new IllegalArgumentException("This password is known to be compromised. Choose another password.");
        } catch(java.security.NoSuchAlgorithmException e) {throw new IllegalStateException("Password validation is unavailable");}
    }
}
