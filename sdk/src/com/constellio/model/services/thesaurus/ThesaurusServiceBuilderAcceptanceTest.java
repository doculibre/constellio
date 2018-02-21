package com.constellio.model.services.thesaurus;

import static com.constellio.data.utils.AccentApostropheCleaner.removeAccents;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.olap4j.metadata.NamedList;

/**
 * Tests for searching in Thesaurus. Search should be permissive at all time (ignore case, accents and trim spaces).
 */
public class ThesaurusServiceBuilderAcceptanceTest extends ConstellioTest {

	public static final String SKOS_XML_FILE_PATH = "C:\\Workspace\\Projets\\SCOS_ServiceQC\\Fichiers depart\\SKOS destination 21 juillet 2017.xml";
	private static ThesaurusService thesaurusService;
	private static Map<String, SkosConcept> allConcepts;

	@Before
	public void setUp()
			throws Exception {
		// prevent parsing each time
		if(thesaurusService ==null){
			thesaurusService = ThesaurusBuilder.getThesaurus(new FileInputStream(SKOS_XML_FILE_PATH));
			allConcepts = thesaurusService.getAllConcepts();
		}
	}

	@Test
	public void whenGetPrefLabelsThatContainsThenCorrespondingConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("déclaration de revenus".substring(1));

		for(String searchValue : searchValues) {

			Set<SkosConcept> concepts = thesaurusService.getPrefLabelsThatContains(searchValue);

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

			Set<SkosConcept> concepts = thesaurusService.getPrefLabelsThatEqualsOrSpecify(searchValue);

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

			Set<SkosConcept> concepts = thesaurusService.getPrefLabelsThatEqualsOrSpecify(searchValue);

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

			Set<SkosConcept> concepts = thesaurusService.getAltLabelsThatContains(searchValue);

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

			Set<SkosConcept> concepts = thesaurusService.getAltLabelsThatEquals(searchValue);

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

			Set<SkosConcept> concepts = thesaurusService.getAltLabelsThatEquals(searchValue);

			assertThat(concepts.isEmpty()).isTrue();
		}
	}

	@Test
	public void whenGetAllLabelsThatContainsWithOnlyPrefLabelResultsThenCorrespondingConceptsFound()
			throws Exception {
		String searchValue = " iMpÔt ";
		Set<SkosConcept> allLabelResults = thesaurusService.getAllLabelsThatContains(searchValue);
		Set<SkosConcept> expectedLabelResults = thesaurusService.getPrefLabelsThatContains(searchValue);

		assertThat(allLabelResults.equals(expectedLabelResults));
	}

	@Test
	public void whenGetAllLabelsThatContainsWithOnlyAltLabelResultsThenCorrespondingConceptsFound()
			throws Exception {
		String searchValue = " RAPPORT D'iMpÔt ";
		Set<SkosConcept> allLabelResults = thesaurusService.getAllLabelsThatContains(searchValue);
		Set<SkosConcept> expectedLabelResults = thesaurusService.getAltLabelsThatContains(searchValue);

		assertThat(allLabelResults.equals(expectedLabelResults));
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
