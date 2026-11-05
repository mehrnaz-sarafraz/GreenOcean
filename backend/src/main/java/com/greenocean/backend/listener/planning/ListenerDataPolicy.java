package com.greenocean.backend.listener.planning;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Component;import java.util.*;
/** No production purpose or retention duration is inferred from enabling a UI. */
@Component public class ListenerDataPolicy {
 private final boolean approved,purge;private final String reference;private final Map<String,Integer> days=new LinkedHashMap<>();
 public ListenerDataPolicy(@Value("${greenocean.listener-support.processing-approved:false}")boolean approved,@Value("${greenocean.listener-support.purge-enabled:false}")boolean purge,@Value("${greenocean.listener-support.policy-reference:}")String reference,
 @Value("${greenocean.listener-support.retention.calendar-days:}")String calendar,@Value("${greenocean.listener-support.retention.booking-days:}")String booking,@Value("${greenocean.listener-support.retention.queue-days:}")String queue,@Value("${greenocean.listener-support.retention.feedback-days:}")String feedback,@Value("${greenocean.listener-support.retention.extension-days:}")String extension){this.approved=approved;this.purge=purge;this.reference=reference.trim();put("CALENDAR",calendar);put("BOOKING",booking);put("QUEUE",queue);put("FEEDBACK",feedback);put("EXTENSION",extension);}
 private void put(String category,String input){if(input==null||input.isBlank()){days.put(category,null);return;}int value;try{value=Integer.parseInt(input);}catch(NumberFormatException invalid){throw new IllegalArgumentException("Retention days must be explicitly configured integers");}if(value<1||value>3650)throw new IllegalArgumentException("Retention days must be bounded between 1 and 3650");days.put(category,value);}
 public Map<String,Integer> days(){return Collections.unmodifiableMap(days);}
 public boolean approved(){return approved&&!reference.isBlank()&&days.values().stream().allMatch(Objects::nonNull);}
 public boolean purgeEnabled(){return purge&&approved();}
 public Map<String,Object> report(){var value=new LinkedHashMap<String,Object>();value.put("processingApproved",approved());value.put("policyReferenceConfigured",!reference.isBlank());value.put("purgeEnabled",purgeEnabled());value.put("retentionDays",days());value.put("privateNotesEnabled",false);return value;}
}
