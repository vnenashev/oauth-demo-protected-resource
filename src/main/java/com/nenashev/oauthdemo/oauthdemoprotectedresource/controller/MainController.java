package com.nenashev.oauthdemo.oauthdemoprotectedresource.controller;

import com.nenashev.oauthdemo.oauthdemoprotectedresource.db.AccessTokenRepository;
import com.nenashev.oauthdemo.oauthdemoprotectedresource.model.AccessTokenInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/")
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final AccessTokenRepository accessTokenRepository;

    public MainController(final AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @GetMapping(path = "/")
    public String index() {
        return "index";
    }

    @CrossOrigin
    @PostMapping(path = "/resource")
    @ResponseBody
    public Object resource(final @RequestHeader("Authorization") Optional<String> auth,
                           final @RequestParam("access_token") Optional<String> accessToken) {
        final String inToken;

        if (auth.isPresent() && auth.get().toLowerCase().startsWith("bearer")) {
            inToken = auth.get().substring("bearer ".length());
        } else if (accessToken.isPresent()) {
            inToken = accessToken.get();
        } else {
            logger.error("No access token in Authorization header or request parameter");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final Optional<AccessTokenInfo> foundAccessToken = accessTokenRepository.findByAccessToken(inToken);

        if (foundAccessToken.isPresent()) {
            logger.info("Found matching access token: {}", foundAccessToken);
        } else {
            logger.error("No matching toke was found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final Map<String, String> res = new HashMap<>();
        res.put("name", "Protected Resource");
        res.put("description", "This data has been protected by OAuth 2.0");

        return ResponseEntity.ok(res);
    }

}
