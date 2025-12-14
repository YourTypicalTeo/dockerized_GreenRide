package com.greenride.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

/**
 * Υπεύθυνη κλάση για τη δημιουργία (issuing) και επαλήθευση (parsing) των JWTs.
 * Χρησιμοποιεί συμμετρική κρυπτογράφηση (HMAC-SHA).
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final String audience;
    private final long ttlMinutes;

    // Constructor: Διαβάζει τις μυστικές ρυθμίσεις από το application.properties
    public JwtService(@Value("${greenride.app.jwt.secret}") String secret,
                      @Value("${greenride.app.jwt.issuer}") String issuer,
                      @Value("${greenride.app.jwt.audience}") String audience,
                      @Value("${greenride.app.jwt.ttl-minutes}") long ttlMinutes) {
        // Μετατροπή του string secret σε κρυπτογραφικό κλειδί
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
        this.ttlMinutes = ttlMinutes;
    }

    /**
     * Δημιουργεί ένα νέο Token όταν ο χρήστης κάνει Login.
     * Το Token περιέχει ΟΛΕΣ τις πληροφορίες που χρειάζεται ο server (Username + Roles).
     */
    public String issue(String subject, Collection<String> roles) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)               // Ποιος είναι ο χρήστης (Username)
                .issuer(this.issuer)            // Ποιος εξέδωσε το token (GreenRide API)
                .audience().add(this.audience).and() // Για ποιον προορίζεται (Client Apps) - Ασφάλεια
                .claim("roles", roles)          // ΚΡΙΣΙΜΟ: Ενσωματώνουμε τους ρόλους μέσα στο token (Stateless)
                .issuedAt(Date.from(now))       // Πότε δημιουργήθηκε
                .expiration(Date.from(now.plus(Duration.ofMinutes(this.ttlMinutes)))) // Πότε λήγει
                .signWith(this.key)             // Ψηφιακή υπογραφή με το μυστικό κλειδί
                .compact();
    }

    /**
     * Ελέγχει αν ένα Token είναι έγκυρο και επιστρέφει τα δεδομένα του (Claims).
     * Αν το token έχει πειραχτεί ή λήξει, αυτή η μέθοδος πετάει Exception.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .requireAudience(this.audience) // Ασφάλεια: Απορρίπτει tokens που φτιάχτηκαν για άλλη εφαρμογή
                .requireIssuer(this.issuer)     // Ασφάλεια: Απορρίπτει tokens από άγνωστους εκδότες
                .verifyWith(this.key)           // Έλεγχος υπογραφής: Απορρίπτει tokens που έχουν αλλοιωθεί
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
