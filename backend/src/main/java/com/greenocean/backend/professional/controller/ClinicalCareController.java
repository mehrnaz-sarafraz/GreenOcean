package com.greenocean.backend.professional.controller;
import com.greenocean.backend.professional.service.*;
import com.greenocean.backend.auth.service.RecentAuthentication;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.*;import org.springframework.web.multipart.MultipartFile;import jakarta.validation.Valid;import java.util.*;import java.time.Instant;
@RestController @RequestMapping("/api/v1/clinical")
public class ClinicalCareController {
 private final ClinicalCare care;private final ClinicalFiles files;private final RecentAuthentication recent;private final tools.jackson.databind.ObjectMapper json;
 public ClinicalCareController(ClinicalCare care,ClinicalFiles files,RecentAuthentication recent,tools.jackson.databind.ObjectMapper json){this.care=care;this.files=files;this.recent=recent;this.json=json;}
 private UUID who(Jwt j){return UUID.fromString(j.getSubject());}private UUID session(Jwt j){return UUID.fromString(j.getClaimAsString("sid"));}
 private UUID privileged(Jwt j){recent.require(who(j),session(j),true);return who(j);}
 public record Revision(@jakarta.validation.constraints.Min(1)long revision){}
 public record Consent(@jakarta.validation.constraints.NotNull UUID noticeId,@jakarta.validation.constraints.NotNull UUID retryKey,@jakarta.validation.constraints.Pattern(regexp="ACCEPT|WITHDRAW") @jakarta.validation.constraints.NotNull String decision){}
 public record Message(@jakarta.validation.constraints.NotNull UUID retryKey,@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=3,max=5000)String body){}
 public record Stop(@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Pattern(regexp="PARTICIPANT_ENDED|TECHNICAL_FAILURE")String reason){}
 public record Failure(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=40)String kind){}
 public record Access(@jakarta.validation.constraints.NotNull UUID encounterId,@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=40)String category,@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=10,max=2000)String purpose,@jakarta.validation.constraints.Future @jakarta.validation.constraints.NotNull Instant expiresAt){}
 @PostMapping("/record-policies") public Object policy(@AuthenticationPrincipal Jwt j,@Valid @RequestBody ClinicalCare.RecordPolicy in){return Map.of("id",care.policy(privileged(j),in));}
 @GetMapping("/notice-drafts") public Object drafts(@AuthenticationPrincipal Jwt j){return care.noticeQueue(privileged(j));}
 @GetMapping("/governance") public Object governance(@AuthenticationPrincipal Jwt j){return care.governance(privileged(j),session(j));}
 @GetMapping("/access-requests") public Object accessRequests(@AuthenticationPrincipal Jwt j){return care.accessRequests(privileged(j),session(j));}
 @PostMapping("/encounters/{id}/export") public ResponseEntity<byte[]> export(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestParam(defaultValue="0")long messageAfter,@RequestParam(defaultValue="0")int noteOffset)throws java.io.IOException{var page=care.export(who(j),session(j),id,messageAfter,noteOffset);byte[] bytes=json.writeValueAsBytes(page);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=clinical-record-page.json").contentType(MediaType.APPLICATION_JSON).body(bytes);}
 @PostMapping("/notice-drafts") public Object draft(@AuthenticationPrincipal Jwt j,@Valid @RequestBody ClinicalCare.Notice in){return Map.of("id",care.draftNotice(privileged(j),in));}
 @PostMapping("/notice-drafts/{id}/approve") public Object approve(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Revision in){return Map.of("id",care.approveNotice(privileged(j),id,in.revision()));}
 @GetMapping("/appointments/{id}/notices") public Object notices(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestParam(defaultValue="en")String locale){return care.notices(who(j),session(j),id,locale);}
 @PostMapping("/appointments/{id}/consent") @ResponseStatus(HttpStatus.NO_CONTENT) public void consent(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Consent in){care.consent(who(j),session(j),id,in.noticeId(),in.retryKey(),in.decision());}
 @PostMapping("/appointments/{id}/enter") public Object enter(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return Map.of("encounterId",care.enter(who(j),session(j),id));}
 @GetMapping("/encounters/{id}") public Object status(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return care.status(who(j),session(j),id);}
 @PostMapping("/encounters/{id}/start") @ResponseStatus(HttpStatus.NO_CONTENT) public void start(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Revision in){care.start(who(j),session(j),id,in.revision());}
 @PostMapping("/encounters/{id}/end") @ResponseStatus(HttpStatus.NO_CONTENT) public void end(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Stop in){care.stop(who(j),session(j),id,in.reason());}
 @PostMapping("/encounters/{id}/acknowledge") @ResponseStatus(HttpStatus.NO_CONTENT) public void acknowledge(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){care.acknowledge(who(j),session(j),id);}
 @GetMapping("/encounters/{id}/messages") public Object messages(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestParam(defaultValue="0")long after){return care.messages(who(j),session(j),id,after);}
 @PostMapping("/encounters/{id}/messages") public Object message(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Message in){return Map.of("id",care.message(who(j),session(j),id,in.retryKey(),in.body()));}
 @GetMapping("/encounters/{id}/notes") public Object notes(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestParam String category,@RequestParam(defaultValue="0")int offset){return care.notesPage(who(j),session(j),id,category,offset);}
 @PostMapping("/encounters/{id}/notes") public Object note(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody ClinicalCare.Note in){return Map.of("id",care.note(who(j),session(j),id,in));}
 @GetMapping("/encounters/{id}/attachments") public Object attachments(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return files.list(who(j),session(j),id);}
 @PostMapping(value="/encounters/{id}/attachments",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Object upload(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestParam(required=false)UUID previousId,@RequestPart MultipartFile file){return Map.of("id",files.upload(who(j),session(j),id,previousId,file));}
 @PostMapping("/encounters/{id}/attachments/{file}/scan") public Object scan(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID file){return Map.of("state",files.rescan(who(j),session(j),id,file));}
 @PostMapping("/encounters/{id}/attachments/{file}/download") public ResponseEntity<byte[]> download(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID file){recent.require(who(j),session(j),false);var d=files.download(who(j),session(j),id,file);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(d.filename()).build().toString()).header("X-Content-Type-Options","nosniff").contentType(MediaType.parseMediaType(d.mime())).body(d.bytes());}
 @PostMapping("/access-requests") public Object request(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Access in){return Map.of("id",care.requestAccess(privileged(j),session(j),in.encounterId(),in.category(),in.purpose(),in.expiresAt()));}
 @PostMapping("/access-requests/{id}/approve") public Object approveAccess(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return Map.of("id",care.approveAccess(privileged(j),session(j),id));}
 @PostMapping("/access-grants/{id}/revoke") @ResponseStatus(HttpStatus.NO_CONTENT) public void revoke(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){care.revokeAccess(privileged(j),session(j),id);}
 @PostMapping("/encounters/{id}/media-admission") public Object admission(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return Map.of("id",care.admission(who(j),session(j),id));}
 @PostMapping("/encounters/{id}/media-admission/{admission}/exchange") public ResponseEntity<ClinicalMediaProvider.Admission> exchange(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID admission){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(care.exchange(who(j),session(j),id,admission));}
 @PostMapping("/encounters/{id}/observe-media") @ResponseStatus(HttpStatus.NO_CONTENT) public void observe(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){care.observeMedia(who(j),session(j),id);}
 @PostMapping("/encounters/{id}/connectivity") @ResponseStatus(HttpStatus.NO_CONTENT) public void failure(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Failure in){care.mediaFailure(who(j),session(j),id,in.kind());}
}
