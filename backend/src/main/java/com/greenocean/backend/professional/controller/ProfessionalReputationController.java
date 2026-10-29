package com.greenocean.backend.professional.controller;
import com.greenocean.backend.professional.service.ProfessionalReputation;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.http.HttpStatus;import jakarta.validation.Valid;import java.util.*;import java.time.LocalDate;
@RestController @RequestMapping("/api/v1/clinic-reputation")
public class ProfessionalReputationController {
 private final ProfessionalReputation reputation;public ProfessionalReputationController(ProfessionalReputation reputation){this.reputation=reputation;}
 private UUID who(Jwt j){return UUID.fromString(j.getSubject());}private UUID session(Jwt j){return UUID.fromString(j.getClaimAsString("sid"));}
 public record Text(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=3,max=2000)String body){}
 public record ResponseReview(boolean approve,boolean privacyChecked,@jakarta.validation.constraints.Size(max=2000)String publicText){}
 public record Case(@jakarta.validation.constraints.NotNull UUID professionalId,UUID feedbackId,UUID appointmentId,@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Pattern(regexp="REVIEW_CONCERN|RANKING_APPEAL|CONDUCT|PRIVACY")String kind,@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=3,max=3000)String body){}
 public record Finding(@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Pattern(regexp="DISMISSED|NO_SHOW_CONFIRMED|FEATURE_HOLD|REVERSED")String result,@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=10,max=2000)String evidence){}
 public record Weekly(@jakarta.validation.constraints.NotNull String profession,@jakarta.validation.constraints.NotNull String mode,@jakarta.validation.constraints.NotNull LocalDate week,@jakarta.validation.constraints.NotNull String category){}
 @GetMapping("/eligible-appointments") public Object eligible(@AuthenticationPrincipal Jwt j){return reputation.eligible(who(j));}
 @PostMapping("/feedback") public Object feedback(@AuthenticationPrincipal Jwt j,@Valid @RequestBody ProfessionalReputation.Feedback in){return Map.of("id",reputation.feedback(who(j),in));}
 @GetMapping("/feedback/{id}") public Object ownOrReview(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return reputation.privateFeedback(who(j),session(j),id);}
 @PostMapping("/feedback/{id}/withdraw") @ResponseStatus(HttpStatus.NO_CONTENT) public void withdraw(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){reputation.withdraw(who(j),id);}
 @GetMapping("/moderation") public Object queue(@AuthenticationPrincipal Jwt j){return reputation.queue(who(j),session(j));}
 @PostMapping("/feedback/{id}/review") @ResponseStatus(HttpStatus.NO_CONTENT) public void review(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody ProfessionalReputation.Review in){reputation.review(who(j),session(j),id,in);}
 @GetMapping("/professionals/{id}/feedback") public Object publicFeedback(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestParam(defaultValue="TEXT")String mode){return reputation.publicFeedback(who(j),id,mode);}
 @PostMapping("/feedback/{id}/responses") public Object response(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Text in){return Map.of("id",reputation.respond(who(j),id,in.body()));}
 @GetMapping("/response-moderation") public Object responses(@AuthenticationPrincipal Jwt j){return reputation.responseQueue(who(j),session(j));}
 @PostMapping("/responses/{id}/review") @ResponseStatus(HttpStatus.NO_CONTENT) public void reviewResponse(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody ResponseReview in){reputation.reviewResponse(who(j),session(j),id,in.approve(),in.publicText(),in.privacyChecked());}
 @PostMapping("/cases") public Object openCase(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Case in){return Map.of("id",reputation.openCase(who(j),in.professionalId(),in.feedbackId(),in.appointmentId(),in.kind(),in.body()));}
 @GetMapping("/my-cases") public Object ownCases(@AuthenticationPrincipal Jwt j){return reputation.ownCases(who(j));}
 @GetMapping("/cases") public Object cases(@AuthenticationPrincipal Jwt j){return reputation.cases(who(j),session(j));}
 @PostMapping("/cases/{id}/finding") @ResponseStatus(HttpStatus.NO_CONTENT) public void finding(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Finding in){reputation.finding(who(j),session(j),id,in.result(),in.evidence());}
 @GetMapping("/methods") public Object methods(@AuthenticationPrincipal Jwt j){return reputation.methods(who(j));}
 @PostMapping("/methods") public Object method(@AuthenticationPrincipal Jwt j,@Valid @RequestBody ProfessionalReputation.Method in){return Map.of("id",reputation.method(who(j),session(j),in));}
 @PostMapping("/methods/{id}/approve") @ResponseStatus(HttpStatus.NO_CONTENT) public void approve(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){reputation.approveMethod(who(j),session(j),id);}
 @GetMapping("/rankings") public Object rankings(@AuthenticationPrincipal Jwt j,@RequestParam String profession,@RequestParam(defaultValue="TEXT")String mode){return reputation.rankings(who(j),profession,mode);}
 @GetMapping("/weekly") public Object weekly(@AuthenticationPrincipal Jwt j,@RequestParam String profession,@RequestParam(defaultValue="TEXT")String mode){return reputation.weekly(who(j),profession,mode);}
 @GetMapping("/weekly-queue") public Object weeklyQueue(@AuthenticationPrincipal Jwt j){return reputation.weeklyQueue(who(j),session(j));}
 @PostMapping("/weekly") public Object propose(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Weekly in){return Map.of("id",reputation.proposeWeekly(who(j),session(j),in.profession(),in.mode(),in.week(),in.category()));}
 @PostMapping("/weekly/{id}/publish") @ResponseStatus(HttpStatus.NO_CONTENT) public void publish(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){reputation.publishWeekly(who(j),session(j),id);}
 @PostMapping("/weekly/{id}/withdraw") @ResponseStatus(HttpStatus.NO_CONTENT) public void withdrawWeekly(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){reputation.withdrawWeekly(who(j),session(j),id);}
}
