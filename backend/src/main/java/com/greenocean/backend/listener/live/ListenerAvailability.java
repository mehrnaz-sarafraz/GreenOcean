package com.greenocean.backend.listener.live;
import java.time.Instant;
import java.util.UUID;
public record ListenerAvailability(boolean enabled,String state,UUID leaseId,Instant expiresAt,Instant serverTime,int heartbeatSeconds) {}
