package com.greenocean.backend.listener.live;
import java.time.Instant;
import java.util.UUID;
public record LiveSupportView(UUID id,String status,String reasonCode,Instant serverTime,Instant expiresAt,
 Instant activationExpiresAt,Instant interruptedAt,Instant endedAt,UUID conversationId,UUID peerId,String peerName,
 boolean listenerSide,int presenceSeconds,int reconnectSeconds) {}
