package com.moonshot.buzz.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Hidden
@Controller
public class RootController {
    // Redirect the site root "/" to the Buzz UI
    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/api/buzz/ui";
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        log.info("Status: OK");
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
