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
package com.constellio.model.entities.schemas;

import static com.constellio.sdk.tests.TestUtils.asSet;
import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.sdk.tests.ConstellioTest;

public class MetadataSchemaTest extends ConstellioTest {

	@Test
	public void whenGetTaxonomySchemasMetadataWithChildOfRelationshipThenReturnCorrectMetadatas()
			throws Exception {

		Metadata firstTypeParentRelationToFirstType = mockMetadata("first_default_parentRelationToFirst");
		Metadata secondTypeParentRelationToFirstType = mockMetadata("second_default_parentRelationToFirst");
		Metadata secondTypeParentRelationToSecondType = mockMetadata("second_default_parentRelationToSecond");
		Metadata secondTypeRelationToSecondType = mockMetadata("second_default_relationToSecond");
		Metadata secondTypeParentRelationToThirdType = mockMetadata("second_default_parentRelationToThird");
		when(firstTypeParentRelationToFirstType.isChildOfRelationship()).thenReturn(true);
		when(secondTypeParentRelationToFirstType.isChildOfRelationship()).thenReturn(true);
		when(secondTypeParentRelationToSecondType.isChildOfRelationship()).thenReturn(true);
		when(secondTypeParentRelationToThirdType.isChildOfRelationship()).thenReturn(true);
		when(firstTypeParentRelationToFirstType.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(secondTypeParentRelationToFirstType.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(secondTypeParentRelationToSecondType.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(secondTypeParentRelationToSecondType.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(secondTypeParentRelationToThirdType.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(firstTypeParentRelationToFirstType.getAllowedReferences()).thenReturn(new AllowedReferences("first", null));
		when(secondTypeParentRelationToFirstType.getAllowedReferences()).thenReturn(new AllowedReferences("first", null));
		when(secondTypeParentRelationToSecondType.getAllowedReferences())
				.thenReturn(new AllowedReferences("second", null));
		when(secondTypeParentRelationToThirdType.getAllowedReferences())
				.thenReturn(new AllowedReferences(null, asSet("third_custom1")));

		Taxonomy firstTaxonomy = Taxonomy.createPublic("taxo1", "taxo1", "zeCollection", Arrays.asList("first", "second"));
		Taxonomy secondTaxonomy = Taxonomy.createPublic("taxo1", "taxo1", "zeCollection", Arrays.asList("third"));

		List<Metadata> metadatas = Arrays.asList(secondTypeParentRelationToFirstType, secondTypeParentRelationToSecondType,
				secondTypeParentRelationToThirdType, secondTypeRelationToSecondType);
		List<Taxonomy> taxonomies = Arrays.asList(firstTaxonomy, secondTaxonomy);

		MetadataSchema schema = new MetadataSchema("default", "second_default", "zeCollection", "zeLabel", metadatas, false,
				new HashSet<RecordValidator>(), new ArrayList<Metadata>());

		MetadataSchemaType schemaType = new MetadataSchemaType("second", "zeCollection", "titre", new ArrayList<MetadataSchema>(),
				schema, true, true);

		List<Metadata> returnedMetadatas = schemaType.getTaxonomySchemasMetadataWithChildOfRelationship(taxonomies);

		assertThat(returnedMetadatas).containsOnly(secondTypeParentRelationToFirstType, secondTypeParentRelationToSecondType);
	}

	@Test
	public void whenGetTaxonomyReferencesThenReturnCorrectReferences()
			throws Exception {

		Taxonomy firstTaxonomy = Taxonomy.createPublic("taxo1", "taxo1", "zeCollection", Arrays.asList("t1", "t2"));
		Taxonomy secondTaxonomy = Taxonomy.createPublic("taxo2", "taxo2", "zeCollection", Arrays.asList("t3", "t4"));
		List<Taxonomy> taxonomies = Arrays.asList(firstTaxonomy, secondTaxonomy);

		Metadata taxonomyRelationToT4 = mockTaxonomyRefMetadata("t5_default_taxoRelationToT4", "t4");
		Metadata relationToT4 = mockRefMetadata("t5_default_relationToT4", "t4");
		Metadata relationToT3Custom = mockRefMetadata("t5_default_relationToT3Custom", asSet("t3_custom"));
		Metadata taxonomyRelationToT3Custom = mockTaxonomyRefMetadata("t5_default_taxoRelationToT3Custom", asSet("t3_custom"));
		Metadata relationToOtherSchema = mockRefMetadata("t5_default_relationToOther", "other");
		Metadata textMetadata = mockedTextMetadata();
		List<Metadata> metadatas = Arrays
				.asList(relationToT4, taxonomyRelationToT4, relationToT3Custom, taxonomyRelationToT3Custom, relationToOtherSchema,
						textMetadata);

		MetadataSchema schema = new MetadataSchema("default", "zeType_default", "zeCollection", "zeLabel", metadatas, false,
				new HashSet<RecordValidator>(), new ArrayList<Metadata>());

		List<Metadata> returnedMetadatas = schema.getTaxonomyRelationshipReferences(taxonomies);
		assertThat(returnedMetadatas).containsOnly(taxonomyRelationToT4, taxonomyRelationToT3Custom);

	}

	@Test
	public void whenGetTaxonomyReferencesOfSchemaPartOfAnotherTaxonomiesThenOnlyReturnTaxonomiesForWhichTheSchemaIsntPartsOf()
			throws Exception {

		Taxonomy firstTaxonomy = Taxonomy.createPublic("taxo1", "taxo1", "zeCollection", Arrays.asList("t1", "t2"));
		Taxonomy secondTaxonomy = Taxonomy.createPublic("taxo2", "taxo2", "zeCollection", Arrays.asList("t3", "t4"));
		List<Taxonomy> taxonomies = Arrays.asList(firstTaxonomy, secondTaxonomy);

		Metadata relationToT4 = mockRefMetadata("t2_default_relationToT4", "t4");
		Metadata taxonomyRelationToT4 = mockTaxonomyRefMetadata("t2_default_taxoRelationToT4", "t4");
		Metadata relationToT3Custom = mockRefMetadata("t2_default_relationToT3Custom", asSet("t3_custom"));
		Metadata taxonomyRelationToT3Custom = mockTaxonomyRefMetadata("t2_default_taxoRelationToT3Custom", asSet("t3_custom"));
		Metadata relationToOtherSchema = mockRefMetadata("t2_default_relationToOther", "other");
		Metadata relationToT1 = mockRefMetadata("t2_default_parentT1", "t1");
		Metadata relationToT2 = mockRefMetadata("t2_default_parentT2", "t2");
		Metadata textMetadata = mockedTextMetadata();
		List<Metadata> metadatas = Arrays
				.asList(relationToT4, taxonomyRelationToT4, relationToT3Custom, taxonomyRelationToT3Custom, relationToOtherSchema,
						textMetadata, relationToT1, relationToT2);

		MetadataSchema schema = new MetadataSchema("default", "t2_default", "zeCollection", "zeLabel", metadatas, false,
				new HashSet<RecordValidator>(), new ArrayList<Metadata>());

		List<Metadata> returnedMetadatas = schema.getTaxonomyRelationshipReferences(taxonomies);
		assertThat(returnedMetadatas).containsOnly(taxonomyRelationToT4, taxonomyRelationToT3Custom);

	}

	private Metadata mockedTextMetadata() {
		Metadata textMetadata = mock(Metadata.class, "textMetadata");
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);
		return textMetadata;
	}

	private Metadata mockTaxonomyRefMetadata(String code, String type) {
		Metadata metadata = mockRefMetadata(code, type);
		when(metadata.isTaxonomyRelationship()).thenReturn(true);
		return metadata;
	}

	private Metadata mockTaxonomyRefMetadata(String code, Set<String> schemas) {
		Metadata metadata = mockRefMetadata(code, schemas);
		when(metadata.isTaxonomyRelationship()).thenReturn(true);
		return metadata;
	}

	private Metadata mockRefMetadata(String code, String type) {
		String localCode = code.split("_")[2];
		Metadata metadata = mock(Metadata.class, code);
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getLocalCode()).thenReturn(localCode);
		when(metadata.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata.getAllowedReferences()).thenReturn(new AllowedReferences(type, null));
		return metadata;
	}

	private Metadata mockRefMetadata(String code, Set<String> schemas) {
		String localCode = code.split("_")[2];
		Metadata metadata = mock(Metadata.class, code);
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getLocalCode()).thenReturn(localCode);
		when(metadata.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata.getAllowedReferences()).thenReturn(new AllowedReferences(null, schemas));
		return metadata;
	}
}
