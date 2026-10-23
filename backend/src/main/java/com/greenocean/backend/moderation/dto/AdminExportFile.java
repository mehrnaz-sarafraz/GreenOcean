package com.greenocean.backend.moderation.dto;

public record AdminExportFile(String filename, byte[] content) {
}
