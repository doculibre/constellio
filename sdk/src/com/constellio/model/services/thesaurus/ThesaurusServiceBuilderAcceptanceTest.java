package com.constellio.model.services.thesaurus;

import static com.constellio.sdk.tests.TestUtils.asSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;

public class ThesaurusServiceBuilderAcceptanceTest extends ConstellioTest {

	public static final String SKOS_XML_FILE_PATH = "C:\\Users\\constellios\\Documents\\SKOS destination 21 juillet 2017.xml";
	private static ThesaurusService thesaurus;
	private static Map<String, SkosConcept> allConcepts;

	@Before
	public void setUp()
			throws Exception {
		// prevent parsing each time
		if(thesaurus==null){
			thesaurus = ThesaurusBuilder.getThesaurus(new FileInputStream(SKOS_XML_FILE_PATH));
			allConcepts = thesaurus.getAllConcepts();
		}
	}

	@Test
	public void whenSearchPrefLabelThenCorrespondingConceptsFound()
			throws Exception {

		String searchValue = "déclAratiON de Revenus";

		List<SkosConcept> allConceptsFound = thesaurus.searchPrefLabel(searchValue);
		assertThat(allConceptsFound.size()).isEqualTo(1);

		Set<ThesaurusLabel> concepts = allConceptsFound.get(0).getPrefLabels();
		assertThat(concepts.iterator().next().getValues().values()).containsAll(asList("DÉCLARATION DE REVENUS","INCOME TAX RETURN"));
	}

	@Test
	public void whenSearchChildPrefLabelThenCorrespondingConceptsFound()
			throws Exception {

		String searchValue = "caRte";

		List<SkosConcept> allConceptsFound = thesaurus.searchChildPrefLabel(searchValue);
		assertThat(allConceptsFound.size()).isEqualTo(2);

		Set<ThesaurusLabel> concepts = allConceptsFound.get(0).getPrefLabels();
		assertThat(concepts.iterator().next().getValues().values()).containsAll(asList("CARTE (lieu)","CARTE (identification)"));
	}

	@Test
	public void whenSearchAltLabelThenCorrespondingConceptsFound()
			throws Exception {

		String searchValue = "rapPort d'iMpÔt";

		List<SkosConcept> allConceptsFound = thesaurus.searchAllLabels(searchValue);
		assertThat(allConceptsFound.size()).isEqualTo(1);

		Set<SkosConceptAltLabel> concepts = allConceptsFound.get(0).getAltLabels();

		assertThat(concepts).extracting("skosConcept.rdfAbout","values").containsOnly(
				tuple("http://www.thesaurus.gouv.qc.ca/tag/terme.do?id=3736", asSet("déclaration fiscale", "déclaration d'impôt", "rapport d'impôt")
				));
	}

	@Test
	public void whenSearchAllLabelsWithOnlyPrefLabelResultsThenCorrespondingConceptsFound()
			throws Exception {
		String searchValue;
		List<SkosConcept> allLabelResults;
		List<SkosConcept> altLabelResults;
		List<SkosConcept> prefLabelResults;

		searchValue = "iMpÔt";
		allLabelResults = thesaurus.searchAllLabels(searchValue);
		prefLabelResults = thesaurus.searchPrefLabel(searchValue);

		assertThat(allLabelResults.equals(prefLabelResults));
	}

	@Test
	public void whenSearchAllLabelsWithOnlyAltLabelResultsThenCorrespondingConceptsFound()
			throws Exception {
		String searchValue;
		List<SkosConcept> allLabelResults;
		List<SkosConcept> altLabelResults;
		List<SkosConcept> prefLabelResults;

		searchValue = "RAPPORT D'iMpÔt";
		allLabelResults = thesaurus.searchAllLabels(searchValue);
		altLabelResults = thesaurus.searchAltLabel(searchValue);

		assertThat(allLabelResults.equals(altLabelResults));
	}

}
