package com.constellio.model.services.thesaurus;

import com.constellio.app.api.search.SearchWebServiceAcceptTest;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static org.assertj.core.api.Assertions.assertThat;

public class ThesaurusManagerAcceptanceTest extends ConstellioTest {

	public static final String SKOS_XML_FILE_PATH = "SKOS destination 21 juillet 2017.xml";

	@Test
	public void givenMultipleInstancesWhenSavingAThesaurusThenUpdatedOnAllInstances()
			throws Exception {
		prepareSystem(withZeCollection());

		linkEventBus(getModelLayerFactory(), getModelLayerFactory("other-instance"));
		ThesaurusManager thesaurusManager = getModelLayerFactory().getThesaurusManager();
		ThesaurusManager otherInstanceThesaurusManager = getModelLayerFactory("other-instance").getThesaurusManager();

		thesaurusManager.set(getTestResourceInputStream(SearchWebServiceAcceptTest.class, SKOS_XML_FILE_PATH), zeCollection);
		thesaurusManager.set(getTestResourceInputStream(SearchWebServiceAcceptTest.class, SKOS_XML_FILE_PATH), zeCollection);

		assertThat(thesaurusManager.get(zeCollection).getAllConcepts()).hasSize(9849);
		assertThat(otherInstanceThesaurusManager.get(zeCollection).getAllConcepts()).hasSize(9849);

	}

	@Test
	public void givenSameThesaurusUsedInTwoCollectionsThenThesaurusServiceIsSameObject() throws Exception {
		givenCollection("constellio");

		prepareSystem(withZeCollection());

		ThesaurusManager thesaurusManager = getModelLayerFactory().getThesaurusManager();

		thesaurusManager.set(getTestResourceInputStream(SearchWebServiceAcceptTest.class, SKOS_XML_FILE_PATH), zeCollection);
		thesaurusManager.set(getTestResourceInputStream(SearchWebServiceAcceptTest.class, SKOS_XML_FILE_PATH), "constellio");

		assertThat(thesaurusManager.get(zeCollection)).isSameAs(thesaurusManager.get("constellio"));
	}


}
