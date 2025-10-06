package com.moonshot.buzz;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class BuzzErrorController implements ErrorController {

    @RequestMapping(path = "/error", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> handleError() {
        //do something like logging
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            "<center style=\"font-family: Helvetica Neue Light,Helvetica Neue,sans-serif; font-weight:300\">" +
                "<h1 style=\"font-weight:400\">404</h1>" +
                "<p>🙁 Sorry, this URL does nor exist 🙁</p></center>");
    }

}
