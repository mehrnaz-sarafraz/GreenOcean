package com.greenocean.backend.auth.config;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;

/** New hashes support long Unicode passwords; old unprefixed BCrypt hashes remain valid. */
public final class CompatiblePasswordEncoder implements PasswordEncoder {
    private final PasswordEncoder modern=Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    private final PasswordEncoder legacy=new BCryptPasswordEncoder();
    private static final String PREFIX="{pbkdf2}";
    public String encode(CharSequence raw) { return PREFIX+modern.encode(raw); }
    public boolean matches(CharSequence raw,String encoded) {
        if(encoded==null) return false;
        try { if(encoded.startsWith(PREFIX)) return modern.matches(raw,encoded.substring(PREFIX.length())); return encoded.startsWith("$2") && legacy.matches(raw,encoded); }
        catch(IllegalArgumentException e) { return false; }
    }
    public boolean upgradeEncoding(String encoded) { return !encoded.startsWith(PREFIX); }
}
