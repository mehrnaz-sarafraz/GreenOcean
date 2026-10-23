package com.greenocean.backend.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProfessionalResponse(
        UUID id, String displayName, String username, String title, List<String> specialties, String avatarUrl,
        BigDecimal rating, int reviewCount, int greenOceanScore, int experienceYears, List<String> languages,
        boolean verified, boolean promoted, String promotedReason, String bio, String gender, String country,
        String city, String workplace, String clinicName, String clinicAddress, List<String> education,
        String licenseNumber, List<String> consultationModes, boolean acceptingNewClients
) {
}
