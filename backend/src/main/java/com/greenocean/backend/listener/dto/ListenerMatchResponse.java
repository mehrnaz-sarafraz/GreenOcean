package com.greenocean.backend.listener.dto;

import java.util.UUID;

public record ListenerMatchResponse(UUID conversationId, ListenerResponse listener) {}
