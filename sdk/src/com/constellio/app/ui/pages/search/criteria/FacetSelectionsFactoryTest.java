package com.constellio.app.ui.pages.search.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class FacetSelectionsFactoryTest extends ConstellioTest {

	FacetSelectionsFactory factory;
	Set<String> selectedValues;

	@Before
	public void setUp()
			throws Exception {

		selectedValues = new HashSet<>();
		selectedValues.add("value1");
		selectedValues.add("value2");
		selectedValues.add("value3");
		selectedValues.add("value4");

		factory = new FacetSelectionsFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		FacetSelections facetSelections = new FacetSelections();
		assertThat(facetSelections.isDirty()).isFalse();

		facetSelections = new FacetSelections();
		facetSelections.setFacetField("type_default_code");
		assertThat(facetSelections.isDirty()).isTrue();

		facetSelections = new FacetSelections();
		facetSelections.setSelectedValues(selectedValues);
		assertThat(facetSelections.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		FacetSelections facetSelections = new FacetSelections();
		facetSelections.setFacetField("type_default_code");
		facetSelections.setSelectedValues(selectedValues);

		String stringValue = factory.toString(facetSelections);
		FacetSelections builtFacetSelections = (FacetSelections) factory.build(stringValue);
		String stringValue2 = factory.toString(builtFacetSelections);

		assertThat(builtFacetSelections).isEqualTo(facetSelections);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtFacetSelections.isDirty()).isFalse();

	}
}
