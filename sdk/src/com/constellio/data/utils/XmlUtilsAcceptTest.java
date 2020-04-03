package com.constellio.data.utils;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class XmlUtilsAcceptTest extends ConstellioTest {

	@Before
	public void setUp() {

	}

	@Test
	public void whenContainsUnescapedSlashAndSpaceThenEscape() {
		List<String> baseStrings = asList("ASD/QWE", "AAAsdd / asdd", "fff/ q", "K /PPP", " ", null);
		List<String> expectedResultStrings =
				asList("ASD__FSL__QWE", "AAAsdd__SPC____FSL____SPC__asdd", "fff__FSL____SPC__q", "K__SPC____FSL__PPP", "__SPC__", null);

		List<String> result = new ArrayList<>();
		for (String value : baseStrings) {
			result.add(XmlUtils.escapeAttributeName(value));
		}

		assertThat(result).isEqualTo(expectedResultStrings);
	}

	@Test
	public void whenContainsEscapedSlashAndSpaceThenUnescape() {
		List<String> baseStrings =
				asList("ASD__FSL__QWE", "AAAsdd__SPC____FSL____SPC__asdd", "fff__FSL____SPC__q", "K__SPC____FSL__PPP", "__SPC__", null);
		List<String> expectedResultStrings = asList("ASD/QWE", "AAAsdd / asdd", "fff/ q", "K /PPP", " ", null);

		List<String> result = new ArrayList<>();
		for (String value : baseStrings) {
			result.add(XmlUtils.unescapeAttributeName(value));
		}

		assertThat(result).isEqualTo(expectedResultStrings);
	}

	@Test
	public void whenContainsUnescapedBackslashAndSpaceThenEscape() {
		List<String> baseStrings = asList("ASD\\QWE", "AAAsdd \\ asdd", "fff\\ q", "K \\PPP", " ", null);
		List<String> expectedResultStrings =
				asList("ASD__BSL__QWE", "AAAsdd__SPC____BSL____SPC__asdd", "fff__BSL____SPC__q", "K__SPC____BSL__PPP", "__SPC__", null);

		List<String> result = new ArrayList<>();
		for (String value : baseStrings) {
			result.add(XmlUtils.escapeAttributeName(value));
		}

		assertThat(result).isEqualTo(expectedResultStrings);
	}

	@Test
	public void whenContainsEscapedBackslashAndSpaceThenUnescape() {
		List<String> baseStrings =
				asList("ASD__BSL__QWE", "AAAsdd__SPC____BSL____SPC__asdd", "fff__BSL____SPC__q", "K__SPC____BSL__PPP", "__SPC__", null);
		List<String> expectedResultStrings = asList("ASD\\QWE", "AAAsdd \\ asdd", "fff\\ q", "K \\PPP", " ", null);

		List<String> result = new ArrayList<>();
		for (String value : baseStrings) {
			result.add(XmlUtils.unescapeAttributeName(value));
		}

		assertThat(result).isEqualTo(expectedResultStrings);
	}

	@Test
	public void whenDoesNotContainUnescapedOrEscapedCharsThenToNothing() {
		List<String> baseStrings =
				asList("Annee_Imposition", "Ecole_Contact", "Nom_Contact", "Prenom_Eleve1", "PrenomEleve_2", "DateNaissanceEleve1");

		List<String> result = new ArrayList<>();
		for (String value : baseStrings) {
			result.add(XmlUtils.escapeAttributeName(value));
		}

		assertThat(result).isEqualTo(baseStrings);

		result.clear();
		for (String value : baseStrings) {
			result.add(XmlUtils.escapeAttributeName(value));
		}

		assertThat(result).isEqualTo(baseStrings);
	}
}
