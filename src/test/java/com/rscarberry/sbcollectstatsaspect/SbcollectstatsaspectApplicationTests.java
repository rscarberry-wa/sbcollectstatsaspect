package com.rscarberry.sbcollectstatsaspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SbcollectstatsaspectApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	void contextLoads() {
	}

	@ParameterizedTest()
	@MethodSource("provideArgsForFindWordsStartingWith")
	void findWordsStartingWith(String prefix, HttpStatus expectedStatus, String[] expectedWords) {
		getAndVerifyWords("/startingwith/" + prefix, expectedStatus, expectedWords);
	}

	@ParameterizedTest()
	@MethodSource("provideArgsForFindWordsContaining")
	void findWordsContaining(String value, HttpStatus expectedStatus, String[] expectedWords) {
		getAndVerifyWords("/containing/" + value, expectedStatus, expectedWords);
	}

	@ParameterizedTest()
	@MethodSource("provideArgsForFindWordsOfLength")
	void findWordsOfLength(int length, HttpStatus expectedStatus, String[] expectedWords) {
		getAndVerifyWords("/oflength/" + length, expectedStatus, expectedWords);
	}

	private void getAndVerifyWords(
		String wordQueryPath, HttpStatus expectedStatus, String[] expectedWords) {
			
		WebTestClient.BodyContentSpec bcs = client.get()
			.uri("/findwords" + wordQueryPath)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	
		if (expectedWords != null) {
			int i = 0;
			for (String expectedWord: expectedWords) {
				bcs = bcs.jsonPath("$[" + i +"]").isEqualTo(expectedWord);
				i++;		
			}
			bcs.jsonPath("$[" + i + "]").doesNotExist();
		}
	}

	private static Stream<Arguments> provideArgsForFindWordsOfLength() {
		return Stream.of(
			Arguments.of(17, HttpStatus.OK, new String[] {
				"misunderstandings",
				"superstitiousness",
				"unapproachability",
				"contemporaneously"
			}),
			Arguments.of(18, HttpStatus.OK, new String[] {
				"characteristically"
			})
		);
	}

	private static Stream<Arguments> provideArgsForFindWordsStartingWith() {
		return Stream.of(
			Arguments.of("cart", HttpStatus.OK, new String[] {
				"carted",
				"carters",
				"cartload",
				"cartloads",
				"carting",
				"cartridges",
				"carts",
				"cart"			
			}),
			Arguments.of("bree", HttpStatus.OK, new String[] {
				"breed",
				"breeding",
				"breeches",
				"breeze",
				"breech"
			},
			Arguments.of("zl", HttpStatus.OK, new String[0]))
		);
	}

	private static Stream<Arguments> provideArgsForFindWordsContaining() {
		return Stream.of(
			Arguments.of("craw", HttpStatus.OK, new String[] {
				"crawling",
				"crawled",
				"scrawled",
				"crawl"
			}),
			Arguments.of("blar", HttpStatus.OK, new String[0])
		);
	}
	
}
