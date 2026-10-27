package com.greenocean.backend.post.service;

import org.springframework.stereotype.Component;
import java.text.Normalizer;
import java.util.Locale;

/** Local bounded screening, NOT a comprehensive semantic classifier or a clinical assessment. */
@Component
public class ContentSafety implements ContentClassifier {
    public static final String VERSION = "local-screen-v1";
    public enum Outcome { ALLOW, REQUEST_REVISION, HOLD_FOR_REVIEW, BLOCK_PROHIBITED }
    public record Assessment(Outcome outcome, String reason) {}
    public Assessment assess(String body) {
        if (body == null || body.isBlank() || body.length() > 10000)
            throw new IllegalArgumentException("Content must contain between 1 and 10000 characters");
        // Match a copy. Never rewrite the member's stored/displayed Persian text or ZWNJ.
        String text = Normalizer.normalize(body, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
                .replace('ي','ی').replace('ك','ک').replaceAll("[\\p{M}\\p{Cf}]", "")
                .replaceAll("\\s+", " ");
        if (text.contains("<script") || text.contains("javascript:") || text.contains("http://") || text.contains("https://")
                || text.matches("(?s).*\\b[\\w.+-]{1,80}@[\\w.-]{1,100}\\.[a-z]{2,20}\\b.*")
                || text.matches("(?s).*\\d[\\d ()+-]{7,30}\\d.*"))
            return new Assessment(Outcome.HOLD_FOR_REVIEW, "PRIVACY_OR_LINK_REVIEW");
        boolean severe = contains(text,"kill you","hurt you","go kill yourself","بکشمت","میکشمت","خودتو بکش","نشانی خانه","home address","rape you");
        boolean targeted = contains(text,"you are worthless","you are disgusting","تو بیارزشی","تو احمقی","خفه شو");
        boolean quoted = contains(text,"said","told me","called me","گفت","بهم گفت","نقل قول","«","\"","not ","never ","نمی");
        if (severe) return new Assessment(quoted ? Outcome.HOLD_FOR_REVIEW : Outcome.BLOCK_PROHIBITED, "THREAT_OR_HARM_REVIEW");
        if (targeted) return new Assessment(quoted ? Outcome.HOLD_FOR_REVIEW : Outcome.REQUEST_REVISION, "RESPECTFUL_WORDING");
        if (contains(text,"buy now","earn money","خرید فوری","درآمد تضمینی"))
            return new Assessment(Outcome.REQUEST_REVISION,"PROMOTION_REVIEW");
        // Obfuscation/mixed confusables and explicit uncertainty go to people, never unchecked publication.
        if (text.codePoints().anyMatch(c -> c >= 0x400 && c <= 0x52f) || contains(text,"k i l l","k1ll","ب ک ش","تهدید","harass","hate ","نفرت"))
            return new Assessment(Outcome.HOLD_FOR_REVIEW,"CONTEXT_REVIEW");
        return new Assessment(Outcome.ALLOW, "LOCAL_SCREEN_PASSED");
    }
    private boolean contains(String text, String... patterns) {
        for (String pattern : patterns) if (text.contains(pattern)) return true;
        return false;
    }
}
