package com.rscarberry.sbcollectstatsaspect.controller;

import com.rscarberry.sbcollectstatsaspect.aspects.CollectStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/startingwith/{prefix}")
    public List<String> findWordsStartingWith(@PathVariable String prefix) {
        if (wordSet == null) {
            return Collections.emptyList();
        }
        log.info("Finding words starting with: {}", prefix);
        return wordSet.stream().filter(w -> w.startsWith(prefix)).collect(Collectors.toList());
    }

    @CollectStats
    @GetMapping("/oflength/{length}")
    public List<String> findWordsOfLength(@PathVariable String length) {
        if (wordSet == null) {
            return Collections.emptyList();
        }
        int intLength = Integer.parseInt(length);
        return wordSet.stream().filter(w -> w.length() == intLength).collect(Collectors.toList());
    }

    @CollectStats
    @GetMapping("/containing/{value}")
    public List<String> findWordsContaining(@PathVariable String value) {
        if (wordSet == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return wordSet.stream().filter(w -> w.indexOf(value) >= 0).collect(Collectors.toList());
    }
}
