package com.rscarberry.sbcollectstatsaspect.controller;

import com.rscarberry.sbcollectstatsaspect.aspects.CollectStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Tag(name = "FindWords", description = "REST API for playing around with words from War and Peace")
@RequestMapping("findwords")
public class WordFinder {

    public static final String INPUT_RESOURCE = "/war_and_peace.txt";

    private final static Set<String> wordSet;

    static {
        Set<String> tmpSet = new HashSet<>();
        try {
            InputStream inputStream = WordFinder.class.getResourceAsStream(INPUT_RESOURCE);
            if (inputStream != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                    String[] words = br.lines().collect(Collectors.joining("\n")).split("\\PL+");
                    for (String word : words) {
                        tmpSet.add(word);
                    }
                } catch (IOException e) {
                    log.error("Failure reading War and Peace", e);
                }
            }
        } finally {
            wordSet = tmpSet;
            log.info("{} unique words found in War and Peace", wordSet.size());
        }
    }

    @CollectStats
    @GetMapping(
        value = "/startingwith/{prefix}", 
        produces = "application/json")
    @Operation(
        summary = "Find words starting a prefix",
        description = "Returns all the words in War and Peace that begin with a prefix."
    )
    @ApiResponses(value ={
        @ApiResponse(responseCode = "200", description = "No problems encountered")
    })
    public List<String> findWordsStartingWith(@PathVariable String prefix) {
        if (wordSet == null) {
            return Collections.emptyList();
        }
        log.info("Finding words starting with: {}", prefix);
        return wordSet.stream().filter(w -> w.startsWith(prefix)).collect(Collectors.toList());
    }

    @CollectStats
    @GetMapping(
        value = "/oflength/{length}",
        produces = "application/json"
        )
    @Operation(
        summary = "Find words of a given length",
        description = "Returns all the words in War and Peace that have the specified length."
    )
    @ApiResponses(value ={
        @ApiResponse(responseCode = "200", description = "No problems encountered")
    })
    public List<String> findWordsOfLength(@PathVariable int length) {
        if (wordSet == null) {
            return Collections.emptyList();
        }
        log.info("Finding words of length {}", length);
        return wordSet.stream().filter(w -> w.length() == length).collect(Collectors.toList());
    }

    @CollectStats
    @GetMapping("/containing/{value}")
    @Operation(
        summary = "Find words containing a value",
        description = "Returns all the words in War and Peace that contain the specified string."
    )
    @ApiResponses(value ={
        @ApiResponse(responseCode = "200", description = "No problems encountered")
    })
    public List<String> findWordsContaining(@PathVariable String value) {
        if (wordSet == null || value.isBlank()) {
            return Collections.emptyList();
        }
        log.info("Finding words that contain {}", value);
        return wordSet.stream().filter(w -> w.indexOf(value) >= 0).collect(Collectors.toList());
    }
}
