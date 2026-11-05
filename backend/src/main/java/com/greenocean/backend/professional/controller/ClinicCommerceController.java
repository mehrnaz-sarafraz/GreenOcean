package com.greenocean.backend.professional.controller;
import com.greenocean.backend.professional.service.*;
import com.greenocean.backend.auth.service.RecentAuthentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import java.util.*;

@RestController @RequestMapping("/api/v1/clinic")
public class ClinicCommerceController {
 private final ClinicCommerce commerce;private final RecentAuthentication recent;
 public ClinicCommerceController(ClinicCommerce commerce,RecentAuthentication recent){this.commerce=commerce;this.recent=recent;}
 private UUID who(Jwt j){return UUID.fromString(j.getSubject());}
 private UUID privileged(Jwt j){UUID who=who(j);recent.require(who,UUID.fromString(j.getClaimAsString("sid")),true);return who;}
 public record Offer(@jakarta.validation.constraints.NotNull UUID policyId,boolean approvalRequired){}
 public record Reason(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=3,max=2000)String reason){}
 public record Resolution(boolean refund,@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=3,max=2000)String reason){}
 public record NoShow(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=10,max=2000)String evidence,boolean outageReconciled){}
 public record Plan(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=80)String version,@jakarta.validation.constraints.Size(min=1,max=4)List<String> entitlements,@jakarta.validation.constraints.Min(0)long priceUnits,@jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(366)int periodDays){}
 @GetMapping("/policies") public Object policies(@AuthenticationPrincipal Jwt j){return commerce.policies(who(j));}
 @PostMapping(value="/provider-callback",consumes="application/octet-stream") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void callback(@RequestHeader(value="X-Provider-Signature",required=false)String signature,@RequestBody byte[] body){commerce.callback(body,signature);}
 @PostMapping("/policies") public Object policy(@AuthenticationPrincipal Jwt j,@Valid @RequestBody ClinicCommerce.Policy p){return Map.of("id",commerce.policy(privileged(j),p));}
 @PutMapping("/slots/{id}/offer") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void offer(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Offer p){commerce.offer(who(j),id,p.policyId(),p.approvalRequired());}
 @GetMapping("/professionals/{id}/offers") public Object offers(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return commerce.offers(who(j),id);}
 @PostMapping("/appointments") public Object book(@AuthenticationPrincipal Jwt j,@Valid @RequestBody ClinicCommerce.Booking p){return commerce.book(who(j),p);}
 @GetMapping("/appointments") public Object appointments(@AuthenticationPrincipal Jwt j){return commerce.appointments(who(j));}
 @GetMapping("/appointments/{id}") public Object appointment(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return commerce.appointment(who(j),id);}
 @PostMapping("/appointments/{id}/transition") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void change(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody ClinicCommerce.Change p){commerce.change(who(j),id,p);}
 @PostMapping("/appointments/{id}/payment-intent") public Object intent(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return commerce.payment(who(j),id);}
 @PostMapping("/appointments/{id}/reconcile") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void reconcile(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){commerce.reconcile(privileged(j),id);}
 @PostMapping("/appointments/{id}/disputes") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void dispute(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Reason p){commerce.dispute(who(j),id,p.reason());}
 @PostMapping("/appointments/{id}/resolve-dispute") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void resolve(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Resolution p){commerce.resolve(privileged(j),id,p.refund(),p.reason());}
 @PostMapping("/appointments/{id}/no-show-review") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void noShow(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody NoShow p){commerce.noShow(privileged(j),id,p.evidence(),p.outageReconciled());}
 @PostMapping("/appointments/{id}/refund") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void refund(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){commerce.requestRefund(privileged(j),id);}
 @PostMapping("/appointments/{id}/reconcile-refund") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void refundEvidence(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){commerce.reconcileRefund(privileged(j),id);}
 @PostMapping("/expire-reservations") public Object expire(@AuthenticationPrincipal Jwt j){return Map.of("expired",commerce.expireReservations(privileged(j)));}
 @GetMapping("/my-payments") public Object payments(@AuthenticationPrincipal Jwt j){return commerce.finances(who(j),false);}
 @GetMapping("/professional-earnings") public Object earnings(@AuthenticationPrincipal Jwt j){return commerce.finances(who(j),true);}
 @GetMapping("/subscriptions") public Object subscriptions(@AuthenticationPrincipal Jwt j){return commerce.subscriptions(who(j));}
 public record Subscribe(@jakarta.validation.constraints.NotNull UUID planId,@jakarta.validation.constraints.NotNull UUID retryKey,boolean confirmed){}
 @GetMapping("/subscription-plans") public Object plans(@AuthenticationPrincipal Jwt j){return commerce.plans(who(j));}
 @PostMapping("/subscriptions") public Object subscribe(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Subscribe p){return commerce.subscribe(who(j),p.planId(),p.retryKey(),p.confirmed());}
 @PostMapping("/subscriptions/{id}/reconcile") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void subscriptionEvidence(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){commerce.reconcileSubscription(privileged(j),id);}
 @PostMapping("/appointments/{id}/settlement") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void settlement(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){commerce.settle(privileged(j),id);}
 @PostMapping("/appointments/{id}/reconcile-settlement") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void settlementEvidence(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){commerce.reconcileSettlement(privileged(j),id);}
 @PostMapping("/subscription-plans") public Object plan(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Plan p){return Map.of("id",commerce.plan(privileged(j),p.version(),p.entitlements(),p.priceUnits(),p.periodDays()));}
 @GetMapping("/finance-operations") public Object operations(@AuthenticationPrincipal Jwt j){return commerce.operations(privileged(j));}
}
