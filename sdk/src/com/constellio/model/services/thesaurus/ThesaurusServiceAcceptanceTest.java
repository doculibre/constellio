package com.constellio.model.services.thesaurus;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.thesaurus.ThesaurusServiceAcceptanceTestUtils.getStringPermissiveCases;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for searching in Thesaurus. Search should be permissive at all time (ignore case, accents and trim spaces).
 */
public class ThesaurusServiceAcceptanceTest extends ConstellioTest {

	private static ThesaurusService thesaurusService;
	private static final Locale DEFAULT_LOCALE = new Locale("fr");
	private static final List<String> AVAILABLE_LOCALES = asList(DEFAULT_LOCALE.getLanguage());
	private final String COLLECTION = zeCollection;

	@Before
	public void setUp()
			throws Exception {
		FoldersLocator foldersLocator = new FoldersLocator();
		String skosDestination = foldersLocator.getPluginsSDKProject()
										 .getAbsoluteFile() + File.separator + "sdk-resources" + File.separator + "SKOS destination.xml";
		File file = new File(skosDestination);
		Assume.assumeTrue(file.exists());

		if (thesaurusService == null) { // prevent parsing each time (time consuming task)
			thesaurusService = ThesaurusServiceBuilder.getThesaurus(new FileInputStream(file));
		}
	}

	public void setupConstellio()
			throws Exception {
		prepareSystem(withZeCollection().withAllTest(new Users()));
	}

	@Test
	public void whenGetPrefLabelsThatContainsThenCorrespondingConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("déclaration de revenus".substring(1));

		for (String searchValue : searchValues) {

			Set<SkosConcept> concepts = thesaurusService.getPrefLabelsThatContains(searchValue, DEFAULT_LOCALE);

			assertThat(concepts).extracting("rdfAbout").containsOnly(
					"http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=3736"
			);
		}
	}

	@Test
	public void whenGetAllTheConceptDomainThenValidatePresenceOfAllRootDomainConcept() {
		List<SkosConcept> domainFound = new ArrayList<>();
		for (Map.Entry<String, SkosConcept> mapStringSkos : thesaurusService.getAllConcepts().entrySet()) {
			thesaurusService.findDomainOfSkosConcept(mapStringSkos.getValue(), ThesaurusService.DOMAINE_LABEL, domainFound);
		}

		String[] domainsAsArray = {"http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=6817",
								   "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=11380",
								   "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=11130",
								   "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=12589",
								   "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=4682",
								   "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=7506",
								   "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=4627",
								   "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=6155",
								   "http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=11950"};

		for (int i = 0; i < domainsAsArray.length; i++) {
			boolean isFound = false;

			for (SkosConcept rootSkosConcept : domainFound) {
				if (rootSkosConcept.getRdfAbout().equalsIgnoreCase(domainsAsArray[i])) {
					isFound = true;
					break;
				}
			}

			assertThat(isFound).isTrue();
		}
	}

	@Test
	public void withSkosConceptMatchFindDomainFromSkosId() {
		List<SkosConcept> skosConcept = thesaurusService.findRootDomain("10032");
		assertThat(skosConcept.get(0).getRdfAbout()).isEqualTo("http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=7506");
	}

	@Test
	public void whenGetPrefLabelsThatEqualsOrSpecifyWithEqualsThenCorrespondingConceptsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("déclaration de revenus");

		for (String searchValue : searchValues) {

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

		for (String searchValue : searchValues) {

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

		for (String searchValue : searchValues) {

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

		for (String searchValue : searchValues) {

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

		for (String searchValue : searchValues) {

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

		for (String searchValue : searchValues) {
			ResponseSkosConcept concepts = thesaurusService.getSkosConcepts(searchValue, AVAILABLE_LOCALES);
			assertThat(concepts.disambiguations.get(DEFAULT_LOCALE)).containsOnly("Carte (lieu)", "Carte (identification)");
		}
	}

	@Test
	public void whenGetSkosConceptsWithNoDesambiguationAndOneSuggestionThenCorrespondingTermsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("rapport d'impôt");

		for (String searchValue : searchValues) {
			ResponseSkosConcept concepts = thesaurusService.getSkosConcepts(searchValue, AVAILABLE_LOCALES);
			assertThat(concepts.disambiguations.get(DEFAULT_LOCALE)).isEmpty();
			assertThat(concepts.suggestions.get(DEFAULT_LOCALE)).containsOnly("Déclaration de revenus");
		}
	}

	@Test
	public void whenGetSkosConceptsWithNoDesambiguationAndMultipleSuggestionsThenCorrespondingTermsFound()
			throws Exception {

		Set<String> searchValues = getStringPermissiveCases("Déclaration de revenus");

		for (String searchValue : searchValues) {
			ResponseSkosConcept concepts = thesaurusService.getSkosConcepts(searchValue, AVAILABLE_LOCALES);
			assertThat(concepts.disambiguations.get(DEFAULT_LOCALE)).isEmpty();
			assertThat(concepts.suggestions.get(DEFAULT_LOCALE))
					.containsOnly("Relevé", "Déclaration de pourboires", "Formulaire", "Avis de cotisation",
							"Impôt sur le revenu", "Impôt", "Revenu");
		}
	}

	@Test
	public void whenGetSkosConceptsWithSpecificationDesambiguationAndExclusionsThenExcludedTermsNotFound()
			throws Exception {

		thesaurusService.setDeniedTerms(asList("Carte (identification)", "Carte routière"));
		Set<String> searchValues = getStringPermissiveCases("carte");

		for (String searchValue : searchValues) {
			ResponseSkosConcept concepts = thesaurusService.getSkosConcepts(searchValue, AVAILABLE_LOCALES);
			assertThat(concepts.disambiguations.get(DEFAULT_LOCALE)).containsOnly("Carte (lieu)");
			assertThat(concepts.suggestions.get(DEFAULT_LOCALE)).doesNotContain("Carte routière");
		}
	}

	@Test
	public void whenSuggestSimpleSearchThenCorrespondingTermsFound()
			throws Exception {

		setupConstellio();

		Map<String, Integer> searchValuesWithOccurences = new HashMap<>();
		searchValuesWithOccurences.put("searchTermNotInThesaurusAutocomplete1", new Integer(10));
		searchValuesWithOccurences.put("searchTermNotInThesaurusAutocomplete2", new Integer(5));
		searchValuesWithOccurences.put("searchTermNotInThesaurusAutocomplete3", new Integer(4));
		searchValuesWithOccurences.put("searchTermNotInThesaurusAutocomplete4", new Integer(3));
		searchValuesWithOccurences.put("searchTermNotInThesaurusAutocomplete5", new Integer(2));
		searchValuesWithOccurences.put("searchTermNotInThesaurusAutocomplete6", new Integer(1));
		searchValuesWithOccurences.put("searchTermNotInThesaurusAutocomplete7", new Integer(1));

		// prepares transaction
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(COLLECTION, getModelLayerFactory());
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(
				OptimisticLockingResolution.EXCEPTION); // changes transatction limit from 1 000 to 100 000

		// makes transaction
		for (Map.Entry searchValueWithOccurence : searchValuesWithOccurences.entrySet()) {

			String searchValue = (String) searchValueWithOccurence.getKey();
			Integer searchOccurence = (Integer) searchValueWithOccurence.getValue();

			// determines if should be concluent search
			int numFound = 1;

			// searches for number of times indicated
			for (int i = 0; i < searchOccurence; i++) {
				SearchEvent searchEvent = schemasRecordsServices.newSearchEvent();
				searchEvent.setQuery(StringUtils.stripAccents(searchValue.toLowerCase())).setNumFound(numFound);
				searchEvent.setOriginalQuery(StringUtils.stripAccents(searchValue.toLowerCase()));
				transaction.add(searchEvent);
			}
		}
		recordServices.execute(transaction);

		thesaurusService.setDeniedTerms(asList("searchTermNotInThesaurusAutocomplete5"));
		Set<String> searchValues = getStringPermissiveCases("searchTermNotInThesaurus");

		for (String searchValue : searchValues) {
			List<String> suggestions = thesaurusService.suggestSimpleSearch(searchValue, DEFAULT_LOCALE, 3, 5, true,
					new SearchEventServices(COLLECTION, getModelLayerFactory()));
			assertThat(suggestions)
					.containsExactly("Searchtermnotinthesaurusautocomplete1", "Searchtermnotinthesaurusautocomplete2",
							"Searchtermnotinthesaurusautocomplete3", "Searchtermnotinthesaurusautocomplete4",
							"Searchtermnotinthesaurusautocomplete6");
		}
	}

	@Test
	public void givenSkosConceptMatchThesaurusLabel() {
		List<String> matchedThesaurusLabelId = thesaurusService.matchThesaurusLabels("lalal " +
																					 "\n " +
																					 "\n ASSURANCE-EMPloi gouvernement " +
																					 "\nASSURANCE-EMPloi RESTAURATEUR ASSURANCE-HOSPITALISATION hgfhgfhgf dgdfgfdg " +
																					 "\ngfdgdfghfhf d'adoption PARENTAL INSURANCE + RESSOURCES DOCUMENTAIRES RESTAURATEUR " +
																					 "\ngouvernement gouvernement " +
																					 "\n  RESTAURATEUR", new Locale("fr"));
		assertThat(matchedThesaurusLabelId)
				.containsOnly("11133", "11133", "11134", "11134", "1094", "1094", "1095", "11117", "6156", "6156", "6156");
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
}