package com.greenocean.backend;

import com.greenocean.backend.auth.dto.RegisterRequest;
import com.greenocean.backend.auth.service.EmailIdentity;
import com.greenocean.backend.auth.service.PasswordPolicy;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

class PhaseCValidationTest {
    @Test @Tag("creative") void normalizationPreservesPlusDotsAndOnlyFoldsCaseAndEdges() {
        assertThat(EmailIdentity.normalize("  A.B+Tag@EXAMPLE.TEST  ")).isEqualTo("a.b+tag@example.test");
        assertThat(EmailIdentity.normalize(null)).isNull();
    }
    @Test @Tag("creative") void supportedEmailPolicyIncludesPunycodeAndPlusAddressing() {
        for(String value:new String[]{"member+tag@example.test","a.b@xn--mgbh0fb.xn--kgbechtv","o'brien@example.test"})
            assertThat(value.matches(EmailIdentity.PATTERN)).isTrue();
    }
    @Test @Tag("malicious") void invalidAndUnsupportedMailboxesFailTheBoundedPolicy() {
        for(String value:new String[]{"a..b@example.test","a@-example.test","a@example..test","a@domain","فارسی@example.test","x".repeat(65)+"@example.test","x@example.test\n"})
            assertThat(value.matches(EmailIdentity.PATTERN)).as(value).isFalse();
    }
    @Test @Tag("happy") void dtoNormalizesBeforeBeanValidation() {
        var value=new RegisterRequest("  MEMBER@EXAMPLE.TEST ","a long safe passphrase",null,null,null," ir ",null,null,true,1);
        assertThat(value.email()).isEqualTo("member@example.test");assertThat(value.countryCode()).isEqualTo("IR");
    }
    @Test @Tag("creative") void passwordBoundariesCountCodePointsAndAllowSpaces() {
        assertThatCode(()->PasswordPolicy.validate("🌊🌍🌱🔑".repeat(32))).doesNotThrowAnyException();
        assertThatCode(()->PasswordPolicy.validate("a plain lowercase passphrase")).doesNotThrowAnyException();
        assertThatThrownBy(()->PasswordPolicy.validate("🌊🌍🌱🔑".repeat(32)+"x")).isInstanceOf(IllegalArgumentException.class);
    }
}
