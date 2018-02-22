package com.constellio.model.services.thesaurus;

import static com.constellio.data.utils.AccentApostropheCleaner.removeAccents;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.util.*;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.olap4j.metadata.NamedList;

/**
 * Tests for searching in Thesaurus. Search should be permissive at all time (ignore case, accents and trim spaces).
 */
public class ThesaurusServiceAcceptanceTest extends ConstellioTest {

	public static final String SKOS_XML_FILE_PATH = "C:\\Workspace\\Projets\\SCOS_ServiceQC\\Fichiers depart\\SKOS destination 21 juillet 2017.xml";
	private static ThesaurusService thesaurusService;
	private static Map<String, SkosConcept> allConcepts;
	private static final Locale DEFAULT_LOCALE = new Locale("fr");
	private static final List<String> AVAILABLE_LOCALES = asList(DEFAULT_LOCALE.getLanguage());

	@Before
	public void setUp()
			throws Exception {
		// prevent parsing each time
		if(thesaurusService == null){
			thesaurusService = ThesaurusServiceBuilder.getThesaurus(new FileInputStream(SKOS_XML_FILE_PATH));
			allConcepts = thesaurusService.getAllConcepts();
		}
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

		assertThat(allLabelResults.equals(expectedLabelResults));
	}

	@Test
	public void whenGetAllLabelsThatContainsWithOnlyAltLabelResultsThenCorrespondingConceptsFound()
			throws Exception {
		String searchValue = " RAPPORT D'iMpÔt ";
		Set<SkosConcept> allLabelResults = thesaurusService.getAllLabelsThatContains(searchValue, DEFAULT_LOCALE);
		Set<SkosConcept> expectedLabelResults = thesaurusService.getAltLabelsThatContains(searchValue, DEFAULT_LOCALE);

		assertThat(allLabelResults.equals(expectedLabelResults));
	}

	@Test
	public void whenGetSkosConceptsThenCorrespondingConceptsFound()
			throws Exception {
		String searchValue = " carte ";//" RAPPORT D'iMpÔt ";
		ResponseSkosConcept concepts = thesaurusService.getSkosConcepts(searchValue, AVAILABLE_LOCALES);
		assertThat(concepts.disambiguations.values()).containsOnly(asList("Carte (lieu)","Carte (identification)"));
		assertThat(concepts.suggestions.get(DEFAULT_LOCALE)).contains("Carte routière");
//		Set<SkosConcept> expectedLabelResults = thesaurusService.getAltLabelsThatContains(searchValue, DEFAULT_LOCALE);
//		assertThat(allLabelResults.equals(expectedLabelResults));
	}

	private String addSpaces(String searchTerm) {
		return " "+searchTerm+" ";
	}

	private String mixCase(String input) {

		String output = "";

		if(input!=null && !input.isEmpty()){
			char[] charArray = input.toCharArray();
			for(int i = 0; i< charArray.length; i++){
				char currentChar = charArray[i];

				if(i%2==0){
					currentChar = Character.toUpperCase(currentChar);
				}

				output += currentChar;
			}
		}

		return output;
	}

	private Set<String> getStringPermissiveCases(String searchTerm) {
		return new HashSet<>(asList(searchTerm, mixCase(searchTerm), removeAccents(searchTerm), addSpaces(searchTerm)));
	}
}
