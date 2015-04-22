/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.services.parser.LanguageDetectionServicesRuntimeException.LanguageDetectionManagerRuntimeException_CannotDetectLanguage;
import com.constellio.sdk.tests.ConstellioTest;

public class LanguageDetectionManagerAcceptTest extends ConstellioTest {

	LanguageDetectionManager languageDetectionManager;

	@Before
	public void setUp()
			throws Exception {
		languageDetectionManager = getModelLayerFactory().getLanguageDetectionManager();

	}

	@Test
	public void whenDetectingFrenchContentThenFrenchLanguageIsReturned()
			throws Exception {

		assertThatDetectedLanguageOfTextIs("Bonjour princesse", "fr");
		assertThatDetectedLanguageOfTextIs("C'est de l'intimidation!", "fr");
		assertThatDetectedLanguageOfTextIs("Francis est ici", "fr");
		assertThatDetectedLanguageOfTextIs("Le Squatre novembre au soir", "fr");
		assertThatDetectedLanguageOfTextIs("Le Squatre novembre au soir", "fr");
		assertThatDetectedLanguageOfTextIs("L'indien Dakota est un grand parmi les grands", "fr");
		assertThatDetectedLanguageOfTextIs("Dakota est grand", "fr");
		assertThatDetectedLanguageOfTextIs("Botte la balle Sandy. Parfait Tom", "fr");
		assertThatDetectedLanguageOfTextIs("Je suis Gandalf le blanc", "fr");

	}

	@Test
	public void whenDetectingEnglishContentThenEnglishLanguageIsReturned()
			throws Exception {

		assertThatDetectedLanguageOfTextIs("The last show was awesome", "en");
		assertThatDetectedLanguageOfTextIs("Nice, it is working", "en");
		assertThatDetectedLanguageOfTextIs("Chuck Norris", "en");
		assertThatDetectedLanguageOfTextIs("The legend of Bob", "en");
		assertThatDetectedLanguageOfTextIs("Gandalf the white", "en");

	}

	@Test(expected = LanguageDetectionManagerRuntimeException_CannotDetectLanguage.class)
	public void givenNoTextThenThrowException()
			throws Exception {

		languageDetectionManager.detectLanguage("");
	}

	private void assertThatDetectedLanguageOfTextIs(String text, String language) {
		for (int i = 0; i < 1000; i++) {
			assertThat(languageDetectionManager.detectLanguage(text)).isEqualTo(language);
		}
	}

}
