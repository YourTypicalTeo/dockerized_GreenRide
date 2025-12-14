package com.greenride.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


    // Ensures users see a pretty error page
@ControllerAdvice(basePackages = "com.greenride.controller.web")
@Order(2)
public class GlobalWebExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model, HttpServletRequest request, HttpServletResponse response) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred.";

        //Συγκεκριμένα Spring exceptions
        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason();
        } else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            message = "You do not have permission to perform this action.";
        }

        //response status code
        response.setStatus(status.value());

        //Δίνει τα data στο error.html
        model.addAttribute("status", status.value());
        model.addAttribute("error", status.getReasonPhrase());
        model.addAttribute("message", message);
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("path", request.getRequestURI());

        return "error";
    }
}