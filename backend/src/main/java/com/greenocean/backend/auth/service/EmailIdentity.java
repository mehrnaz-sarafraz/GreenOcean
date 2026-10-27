package com.greenocean.backend.auth.service;

import java.util.Locale;

/** Existing identities are case-insensitive. Dots and plus tags retain their meaning.
 * Registration supports ASCII dot-atom mailboxes and DNS/punycode domains, not SMTPUTF8.
 */
public final class EmailIdentity {
    private EmailIdentity() {}
    public static final String PATTERN = "(?=.{1,320}$)(?=.{1,64}@)[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?";
    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
