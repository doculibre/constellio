package com.constellio.model.services.thesaurus;

import java.util.HashSet;
import java.util.Set;

import static com.constellio.data.utils.AccentApostropheCleaner.removeAccents;
import static java.util.Arrays.asList;

public class ThesaurusServiceAcceptanceTestUtils {

	public static Set<String> getStringPermissiveCases(String searchTerm) {
		return new HashSet<>(asList(searchTerm, mixCase(searchTerm), removeAccents(searchTerm), addSpaces(searchTerm)));
	}

	public static String addSpaces(String searchTerm) {
		return "  " + searchTerm + "  ";
	}

	public static String mixCase(String input) {

		String output = "";

		if (input != null && !input.isEmpty()) {
			char[] charArray = input.toCharArray();
			for (int i = 0; i < charArray.length; i++) {
				char currentChar = charArray[i];

				if (i % 2 == 0) {
					currentChar = Character.toUpperCase(currentChar);
				}

				output += currentChar;
			}
		}

		return output;
	}
}
