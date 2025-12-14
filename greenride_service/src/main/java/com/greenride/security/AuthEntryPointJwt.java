package com.greenride.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This component handles unauthenticated access attempts.
 * Instead of redirecting to a login page (default for UI),
 * it returns a 401 Unauthorized error, which is correct for a REST API.
 */
//Εδω αυτο ειναι για να κανει handle τα unauthorized access attempts
    //Αντι για να κάνει redirect στο Login απλα πετάει ενα 401 Unauthorized error
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage());
        //Στέλνει 401 με ενα απλο μηνυματακι
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}