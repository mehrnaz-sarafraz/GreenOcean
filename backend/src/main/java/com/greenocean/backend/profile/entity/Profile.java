package com.greenocean.backend.profile.entity;

import com.greenocean.backend.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "country_code", length = 2, columnDefinition = "char(2)")
    private String countryCode;

    @Column(length = 100)
    private String city;

    @Column(name = "birth_year")
    private Short birthYear;

    @Column(name = "is_profile_private", nullable = false)
    private boolean profilePrivate;

    @Column(name = "show_location", nullable = false)
    private boolean showLocation;

    @Column(name = "show_birth_year", nullable = false)
    private boolean showBirthYear;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Profile() {
    }

    public Profile(User user, String username, String displayName, String countryCode, String city, short birthYear) {
        Instant now = Instant.now();
        this.user = user;
        this.username = username;
        this.displayName = displayName;
        this.countryCode = countryCode;
        this.city = city;
        this.birthYear = birthYear;
        this.profilePrivate = false;
        this.showLocation = false;
        this.showBirthYear = false;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(
            String username,
            String displayName,
            String bio,
            String avatarUrl,
            String countryCode,
            String city,
            Short birthYear,
            Boolean profilePrivate,
            Boolean showLocation,
            Boolean showBirthYear
    ) {
        if (username != null) this.username = username;
        if (displayName != null) this.displayName = displayName;
        if (bio != null) this.bio = emptyToNull(bio);
        if (avatarUrl != null) this.avatarUrl = emptyToNull(avatarUrl);
        if (countryCode != null) this.countryCode = emptyToNull(countryCode);
        if (city != null) this.city = emptyToNull(city);
        if (birthYear != null) this.birthYear = birthYear;
        if (profilePrivate != null) this.profilePrivate = profilePrivate;
        if (showLocation != null) this.showLocation = showLocation;
        if (showBirthYear != null) this.showBirthYear = showBirthYear;
        this.updatedAt = Instant.now();
    }

    private String emptyToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCity() {
        return city;
    }

    public Short getBirthYear() {
        return birthYear;
    }

    public boolean isProfilePrivate() {
        return profilePrivate;
    }

    public boolean isShowLocation() {
        return showLocation;
    }

    public boolean isShowBirthYear() {
        return showBirthYear;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
