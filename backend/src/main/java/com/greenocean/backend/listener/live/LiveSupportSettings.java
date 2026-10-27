package com.greenocean.backend.listener.live;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public record LiveSupportSettings(boolean enabled,int offerSeconds,int activationSeconds,int presenceSeconds,int reconnectSeconds,int heartbeatSeconds,int sweepMillis) {
 @Autowired
 public LiveSupportSettings(@Value("${greenocean.live-support.enabled:false}") boolean enabled,
  @Value("${greenocean.live-support.offer-seconds:90}") int offerSeconds,
  @Value("${greenocean.live-support.activation-seconds:60}") int activationSeconds,
  @Value("${greenocean.live-support.presence-seconds:45}") int presenceSeconds,
  @Value("${greenocean.live-support.reconnect-seconds:120}") int reconnectSeconds,
  @Value("${greenocean.live-support.heartbeat-seconds:15}") int heartbeatSeconds,
  @Value("${greenocean.live-support.sweep-ms:10000}") int sweepMillis) {
  if(offerSeconds<30||offerSeconds>120||activationSeconds<15||activationSeconds>90||presenceSeconds<30||presenceSeconds>120||reconnectSeconds<30||reconnectSeconds>600||heartbeatSeconds<5||heartbeatSeconds>30||presenceSeconds<heartbeatSeconds*2||sweepMillis<1000||sweepMillis>30000)
   throw new IllegalArgumentException("Live support timing is outside approved technical bounds");
  this.enabled=enabled;this.offerSeconds=offerSeconds;this.activationSeconds=activationSeconds;this.presenceSeconds=presenceSeconds;this.reconnectSeconds=reconnectSeconds;this.heartbeatSeconds=heartbeatSeconds;this.sweepMillis=sweepMillis;
 }
 public LiveSupportSettings(boolean enabled,int offer,int activation,int presence,int reconnect){this(enabled,offer,activation,presence,reconnect,15,10000);}
}
