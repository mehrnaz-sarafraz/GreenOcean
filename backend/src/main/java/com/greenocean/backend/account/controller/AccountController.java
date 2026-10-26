package com.greenocean.backend.account.controller;

import com.greenocean.backend.account.dto.AccountArchiveResponse;
import com.greenocean.backend.account.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import com.greenocean.backend.account.dto.DeleteAccountRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
    private final AccountService service;private final com.greenocean.backend.auth.service.RecentAuthentication recent;

    public AccountController(AccountService service,com.greenocean.backend.auth.service.RecentAuthentication recent) {
        this.service = service;this.recent=recent;
    }

    @GetMapping("/archive")
    public AccountArchiveResponse archive(@AuthenticationPrincipal Jwt jwt) {
        return service.archive(UUID.fromString(jwt.getSubject()));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody DeleteAccountRequest request) {
        if(recent.privileged(UUID.fromString(jwt.getSubject())))recent.require(UUID.fromString(jwt.getSubject()),UUID.fromString(jwt.getClaimAsString("sid")),true);
        recent.password(UUID.fromString(jwt.getSubject()),UUID.fromString(jwt.getClaimAsString("sid")),request.password());
        service.delete(UUID.fromString(jwt.getSubject()), request.password());
        return ResponseEntity.noContent().build();
    }
}
