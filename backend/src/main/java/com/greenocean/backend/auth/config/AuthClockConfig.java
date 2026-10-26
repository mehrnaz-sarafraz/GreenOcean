package com.greenocean.backend.auth.config;
import java.time.Clock;
import org.springframework.context.annotation.*;
@Configuration public class AuthClockConfig { @Bean public Clock authenticationClock(){return Clock.systemUTC();} }
