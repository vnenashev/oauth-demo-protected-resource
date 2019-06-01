package com.nenashev.oauthdemo.oauthdemoprotectedresource.controller;

import com.nenashev.oauthdemo.oauthdemoprotectedresource.model.AccessTokenInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(path = "/")
@CrossOrigin
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final List<String> savedWords = new CopyOnWriteArrayList<>();

    @GetMapping(path = "/")
    public String index() {
        return "index";
    }

    @GetMapping(path = "/words", produces = "application/json")
    public ResponseEntity<?> getWords(final @RequestAttribute("access_token") AccessTokenInfo accessTokenInfo) {
        logger.info("Received GET /words");
        final boolean hasReadAccess = hasAccess(accessTokenInfo.getScope(), "read");
        if (!hasReadAccess) {
            logger.warn("Access token has no 'read' access");
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header("WWW-Authenticate",
                    "Bearer error=\"insufficient_scope\", scope=\"read\"")
                .build();
        }

        final Map<String, Object> res = new HashMap<>();

        res.put("words", String.join(" ", savedWords));
        res.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(res);
    }

    @PostMapping(path = "/words")
    public ResponseEntity<?> addWord(final @RequestAttribute("access_token") AccessTokenInfo accessTokenInfo,
                                     final @RequestParam("word") Optional<String> word) {
        logger.info("Received POST /words");
        final boolean hasWriteAccess = hasAccess(accessTokenInfo.getScope(), "write");
        if (!hasWriteAccess) {
            logger.warn("Access token has no 'write' access");
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header("WWW-Authenticate",
                    "Bearer error=\"insufficient_scope\", scope=\"write\"")
                .build();
        }

        word.filter(StringUtils::hasText).ifPresent(w -> {
            logger.info("Adding word {}", w);
            savedWords.add(w);
        });

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(path = "/words")
    public ResponseEntity<?> deleteWord(final @RequestAttribute("access_token") AccessTokenInfo accessTokenInfo) {
        logger.info("Received DELETE /words");
        final boolean hasDeleteAccess = hasAccess(accessTokenInfo.getScope(), "delete");
        if (!hasDeleteAccess) {
            logger.warn("Access token has no 'delete' access");
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header("WWW-Authenticate",
                    "Bearer error=\"insufficient_scope\", scope=\"delete\"")
                .build();
        }

        final String w = savedWords.remove(savedWords.size() - 1);

        logger.info("Deleted word {}", w);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private boolean hasAccess(final String scope, final String access) {
        return Optional.ofNullable(scope)
            .filter(StringUtils::hasText)
            .map(s -> s.split(" "))
            .map(Stream::of)
            .filter(Predicate.isEqual(access))
            .flatMap(Stream::findAny)
            .orElse(null) != null;
    }

}
