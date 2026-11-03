package com.springboot.blog.security;

import com.springboot.blog.exception.BlogAPIException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app-jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // how long after expiry a token may still be exchanged for a new one
    @Value("${app.jwt-refresh-grace-milliseconds:86400000}")
    private long refreshGraceMillis;

    private static final ObjectMapper CLAIMS_READER = new ObjectMapper();

    // generate JWT token
    public String generateToken(Authentication authentication){

        String username = authentication.getName();

        Date currentDate = new Date();

        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key())
                .compact();

        return token;
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // get username from JWT token
    public String getUsername(String token){

        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // validate JWT token
    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parse(token);
            return true;
        }catch (MalformedJwtException malformedJwtException){
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Invalid JWT Token");
        }catch (ExpiredJwtException expiredJwtException){
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Expired JWT token");
        }catch (UnsupportedJwtException unsupportedJwtException){
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
        }catch (IllegalArgumentException illegalArgumentException){
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Jwt claims string is null or empty");
        }
    }

    /**
     * Reads the subject and expiry of a token without rejecting it for being expired,
     * so the refresh endpoint can renew tokens that lapsed a moment ago.
     */
    public RefreshCandidate readRefreshCandidate(String token){
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if(parts.length < 2){
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Invalid JWT Token");
        }
        try{
            JsonNode claims = CLAIMS_READER.readTree(
                    new String(Decoders.BASE64URL.decode(parts[1]), StandardCharsets.UTF_8));
            String subject = claims.path("sub").asText(null);
            long expiresAtMillis = claims.path("exp").asLong(0) * 1000L;
            if(subject == null || expiresAtMillis == 0){
                throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Invalid JWT Token");
            }
            return new RefreshCandidate(subject, new Date(expiresAtMillis));
        }catch (java.io.IOException | IllegalArgumentException e){
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Invalid JWT Token");
        }
    }

    public boolean isWithinRefreshWindow(RefreshCandidate candidate){
        long now = System.currentTimeMillis();
        return candidate.expiresAt().getTime() + refreshGraceMillis >= now;
    }

    public record RefreshCandidate(String subject, Date expiresAt) {}
}
