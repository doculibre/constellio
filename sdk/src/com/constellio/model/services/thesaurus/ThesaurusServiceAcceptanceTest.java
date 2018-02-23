package com.constellio.model.services.thesaurus;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.*;

import static com.constellio.data.utils.AccentApostropheCleaner.removeAccents;
import static com.constellio.model.services.thesaurus.ThesaurusServiceAcceptanceTestUtils.addSpaces;
import static com.constellio.model.services.thesaurus.ThesaurusServiceAcceptanceTestUtils.mixCase;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for searching in Thesaurus. Search should be permissive at all time (ignore case, accents and trim spaces).
 */
public class ThesaurusServiceAcceptanceTest extends ConstellioTest {

	public static final String SKOS_XML_FILE_PATH = "C:\\Users\\constellios\\Documents\\SKOS\\SKOS destination 21 juillet 2017.xml";
	private static ThesaurusService thesaurusService;
	private static Map<String, SkosConcept> allConcepts;
	private static final Locale DEFAULT_LOCALE = new Locale("fr");
	private static final List<String> AVAILABLE_LOCALES = asList(DEFAULT_LOCALE.getLanguage());


	@Before
	public void setUp()
			throws Exception {
		// prevent parsing each time
			thesaurusService = ThesaurusServiceBuilder.getThesaurus(new FileInputStream(SKOS_XML_FILE_PATH));
			allConcepts = thesaurusService.getAllConcepts();
	}

	@Test
	public void whenGetPrefLabelsThatContainsThenCorrespondingConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("déclaration de revenus".substring(1));

		for(String searchValue : searchValues) {

			Set<SkosConcept> concepts = thesaurusService.getPrefLabelsThatContains(searchValue, DEFAULT_LOCALE);

			assertThat(concepts).extracting("rdfAbout").containsOnly(
					"http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=3736"
			);
		}
	}

	@Test
	public void whenGetPrefLabelsThatEqualsOrSpecifyWithEqualsThenCorrespondingConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("déclaration de revenus");

		for(String searchValue : searchValues) {

			Set<SkosConcept> concepts = thesaurusService.getPrefLabelsThatEqualsOrSpecify(searchValue, DEFAULT_LOCALE);

			assertThat(concepts).extracting("rdfAbout").containsOnly(
					"http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=3736"
			);
		}
	}

	@Test
	public void whenGetPrefLabelsThatEqualsOrSpecifyWithSpecifyThenCorrespondingConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("carte");

		for(String searchValue : searchValues) {

			Set<SkosConcept> concepts = thesaurusService.getPrefLabelsThatEqualsOrSpecify(searchValue, DEFAULT_LOCALE);

			assertThat(concepts).extracting("rdfAbout").containsOnly(
					"http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=1990",
					"http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=1991"
			);

		}
	}

	@Test
	public void whenGetAltLabelsThatContainsThenCorrespondingConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("rapport d'impôt".substring(1));

		for(String searchValue : searchValues) {

			Set<SkosConcept> concepts = thesaurusService.getAltLabelsThatContains(searchValue, DEFAULT_LOCALE);

			assertThat(concepts).extracting("rdfAbout").containsOnly(
					"http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=3736"
			);
		}
	}

	@Test
	public void whenGetAltLabelsThatEqualsThenCorrespondingConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("rapport d'impôt");

		for(String searchValue : searchValues) {

			Set<SkosConcept> concepts = thesaurusService.getAltLabelsThatEquals(searchValue, DEFAULT_LOCALE);

			assertThat(concepts).extracting("rdfAbout").containsOnly(
					"http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=3736"
			);
		}
	}

	@Test
	public void whenGetAltLabelsThatEqualsNotThenNoConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("rapport d'impôt".substring(1));

		for(String searchValue : searchValues) {

			Set<SkosConcept> concepts = thesaurusService.getAltLabelsThatEquals(searchValue, DEFAULT_LOCALE);

			assertThat(concepts.isEmpty()).isTrue();
		}
	}

	@Test
	public void whenGetAllLabelsThatContainsWithOnlyPrefLabelResultsThenCorrespondingConceptsFound()
			throws Exception {
		String searchValue = " iMpÔt ";
		Set<SkosConcept> allLabelResults = thesaurusService.getAllLabelsThatContains(searchValue, DEFAULT_LOCALE);
		Set<SkosConcept> expectedLabelResults = thesaurusService.getPrefLabelsThatContains(searchValue, DEFAULT_LOCALE);

		assertThat(allLabelResults).containsAll(expectedLabelResults);
	}

	@Test
	public void whenGetAllLabelsThatContainsWithOnlyAltLabelResultsThenCorrespondingConceptsFound()
			throws Exception {
		String searchValue = " RAPPORT D'iMpÔt ";
		Set<SkosConcept> allLabelResults = thesaurusService.getAllLabelsThatContains(searchValue, DEFAULT_LOCALE);
		Set<SkosConcept> expectedLabelResults = thesaurusService.getAltLabelsThatContains(searchValue, DEFAULT_LOCALE);

		assertThat(allLabelResults).containsAll(expectedLabelResults);
	}

	@Test
	public void whenGetSkosConceptsWithSpecificationDesambiguationThenCorrespondingTermsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("carte");

		for(String searchValue : searchValues) {
			ResponseSkosConcept concepts = thesaurusService.getSkosConcepts(searchValue, AVAILABLE_LOCALES);
			assertThat(concepts.disambiguations.get(DEFAULT_LOCALE)).containsOnly("Carte (lieu)", "Carte (identification)");
		}
	}

	@Test
	public void whenGetSkosConceptsWithNoDesambiguationAndOneSuggestionThenCorrespondingTermsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("rapport d'impôt");

		for(String searchValue : searchValues) {
			ResponseSkosConcept concepts = thesaurusService.getSkosConcepts(searchValue, AVAILABLE_LOCALES);
			assertThat(concepts.disambiguations.get(DEFAULT_LOCALE)).isEmpty();
			assertThat(concepts.suggestions.get(DEFAULT_LOCALE)).containsOnly("Déclaration de revenus");
		}
	}

	@Test
	public void whenGetSkosConceptsWithNoDesambiguationAndMultipleSuggestionsThenCorrespondingTermsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("Déclaration de revenus");

		for(String searchValue : searchValues) {
			ResponseSkosConcept concepts = thesaurusService.getSkosConcepts(searchValue, AVAILABLE_LOCALES);
			assertThat(concepts.disambiguations.get(DEFAULT_LOCALE)).isEmpty();
			assertThat(concepts.suggestions.get(DEFAULT_LOCALE)).containsOnly("Relevé", "Déclaration de pourboires", "Formulaire", "Avis de cotisation", "Impôt sur le revenu", "Impôt", "Revenu");
		}
	}

	// 1094 1094 1095 1117

	@Test
	public void givenSkosConceptMatchThesaurusLabel() {
		List<String> matchedThesaurusLabelId = thesaurusService.matchThesaurusLabels("lalal " +
				"\n " +
				"\n ASSURANCE-EMPloi gouvernement " +
				"\nASSURANCE-EMPloi RESTAURATEUR ASSURANCE-HOSPITALISATION hgfhgfhgf dgdfgfdg " +
				"\ngfdgdfghfhf d'adoption PARENTAL INSURANCE + RESSOURCES DOCUMENTAIRES RESTAURATEUR " +
				"\ngouvernement gouvernement " +
				"\n  RESTAURATEUR", new Locale("fr"));
		assertThat(matchedThesaurusLabelId).containsOnly("11133", "11133", "11134", "11134", "1094", "1094", "1095", "11117", "6156", "6156", "6156");
	}

	@Test
	public void givenSkosConceptMatchThesaurusLabelWithDeniedTerms() {
		thesaurusService.setDeniedTerms(asList("gouvernement"));

		List<String> matchedThesaurusLabelId = thesaurusService.matchThesaurusLabels("lalal " +
				"\n " +
				"\n ASSURANCE-EMPloi gouvernement " +
				"\nASSURANCE-EMPloi RESTAURATEUR ASSURANCE-HOSPITALISATION hgfhgfhgf dgdfgfdg " +
				"\ngfdgdfghfhf d'adoption PARENTAL INSURANCE + RESSOURCES DOCUMENTAIRES RESTAURATEUR " +
				"\ngouvernement gouvernement " +
				"\n  RESTAURATEUR", new Locale("fr"));
		assertThat(matchedThesaurusLabelId).containsOnly("11133", "11133", "11134", "11134", "1094", "1094", "1095", "11117");
	}

	private Set<String> getStringPermissiveCases(String searchTerm) {
		return new HashSet<>(asList(searchTerm, mixCase(searchTerm), removeAccents(searchTerm), addSpaces(searchTerm)));
	}


}
