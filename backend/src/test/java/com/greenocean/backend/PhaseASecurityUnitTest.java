package com.greenocean.backend;
import com.greenocean.backend.auth.config.CompatiblePasswordEncoder;
import com.greenocean.backend.auth.service.PasswordPolicy;
import com.greenocean.backend.assistance.service.OutboxCipher;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.*;

class PhaseASecurityUnitTest {
    @Test @Tag("happy") void modernPasswordHashRoundTrip() {var e=new CompatiblePasswordEncoder();String h=e.encode("a long secret passphrase");assertThat(e.matches("a long secret passphrase",h)).isTrue();assertThat(e.matches("different passphrase",h)).isFalse();}
    @Test @Tag("creative") void legacyBcryptRemainsCompatibleAndCanUpgrade() {var e=new CompatiblePasswordEncoder();String h=new BCryptPasswordEncoder().encode("old password 123");assertThat(e.matches("old password 123",h)).isTrue();assertThat(e.upgradeEncoding(h)).isTrue();}
    @Test @Tag("creative") void longUnicodePasswordDoesNotTruncateAt72Bytes() {var e=new CompatiblePasswordEncoder();String p="رمز عبور امن و بسیار طولانی 🌊".repeat(3);String h=e.encode(p);assertThat(e.matches(p,h)).isTrue();assertThat(e.matches(p+"different",h)).isFalse();}
    @Test @Tag("confused") void weakOrShortPasswordIsRejected() {assertThatThrownBy(()->PasswordPolicy.validate("passwordpassword")).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->PasswordPolicy.validate("short")).isInstanceOf(IllegalArgumentException.class);}
    @Test @Tag("happy") void passphraseHasNoCompositionRequirement() {assertThatCode(()->PasswordPolicy.validate("my rather lengthy passphrase")).doesNotThrowAnyException();}
    @Test @Tag("malicious") void oversizedPasswordIsBounded() {assertThatThrownBy(()->PasswordPolicy.validate("x".repeat(129))).isInstanceOf(IllegalArgumentException.class);}
    @Test @Tag("happy") void outboxEncryptionRoundTripAndRandomNonce() {var c=new OutboxCipher("synthetic-test-key-only");String a=c.encrypt("123456"),b=c.encrypt("123456");assertThat(a).isNotEqualTo(b);assertThat(c.decrypt(a)).isEqualTo("123456");}
    @Test @Tag("malicious") void outboxRejectsTamperingAndWrongKey() {var a=new OutboxCipher("synthetic-a");var b=new OutboxCipher("synthetic-b");assertThatThrownBy(()->b.decrypt(a.encrypt("123456"))).isInstanceOf(IllegalStateException.class);assertThatThrownBy(()->a.decrypt("malformed")).isInstanceOf(IllegalStateException.class);}
}
