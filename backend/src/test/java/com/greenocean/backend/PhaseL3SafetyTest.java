package com.greenocean.backend;
import com.greenocean.backend.post.service.ContentSafety;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.*;
class PhaseL3SafetyTest {
 private final ContentSafety safety=new ContentSafety();
 @ParameterizedTest @CsvSource(delimiter='|',value={
  "امروز خیلی عصبانی‌ام ولی می‌خواهم حرف بزنم|ALLOW",
  "I feel hopeless and want someone to listen|ALLOW",
  "I was hurt by what happened|ALLOW",
  "وقتی بهم گفت تو احمقی ناراحت شدم|HOLD_FOR_REVIEW",
  "تو بيارزشي|REQUEST_REVISION",
  "I will ki‌ll you|BLOCK_PROHIBITED",
  "I will kіll you|HOLD_FOR_REVIEW",
  "Never tell someone go kill yourself|HOLD_FOR_REVIEW",
  "Phone: +1 202 555 0123|HOLD_FOR_REVIEW",
  "درآمد تضمینی بدون کار|REQUEST_REVISION",
  "<script>alert('synthetic')</script>|HOLD_FOR_REVIEW",
  "من نیاز به همدلی دارم 💙|ALLOW"
 }) void contextualExamples(String text,String outcome){assertThat(safety.assess(text).outcome().name()).isEqualTo(outcome);}
 @Test void boundedInvalidAndLongInputs(){
  for(String body:new String[]{null,""," ","x".repeat(10001)})assertThatThrownBy(()->safety.assess(body)).isInstanceOf(IllegalArgumentException.class);
  org.junit.jupiter.api.Assertions.assertTimeout(java.time.Duration.ofSeconds(2),()->safety.assess("a@".repeat(4900)));
 }
}
