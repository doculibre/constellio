package com.constellio.model.entities.schemas;

import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.asSet;
import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataSchemaTest extends ConstellioTest {

	Map<Language, String> labels;

	@Before
	public void setUp()
			throws Exception {
		labels = new HashMap<>();
		labels.put(Language.French, "zeLabel");
	}

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
		when(secondTypeRelationToSecondType.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(secondTypeParentRelationToSecondType.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(secondTypeParentRelationToThirdType.getType()).thenReturn(MetadataValueType.REFERENCE);

		when(firstTypeParentRelationToFirstType.getDataStoreCode()).thenReturn("parentRelationToFirstId_s");
		when(secondTypeParentRelationToFirstType.getDataStoreCode()).thenReturn("parentRelationToFirstId_s");
		when(secondTypeRelationToSecondType.getDataStoreCode()).thenReturn("parentRelationToSecondId_s");
		when(secondTypeParentRelationToSecondType.getDataStoreCode()).thenReturn("relationToSecondId_s");
		when(secondTypeParentRelationToThirdType.getDataStoreCode()).thenReturn("parentRelationToThirdId_s");

		when(firstTypeParentRelationToFirstType.getAllowedReferences()).thenReturn(new AllowedReferences("first", null));
		when(secondTypeParentRelationToFirstType.getAllowedReferences()).thenReturn(new AllowedReferences("first", null));
		when(secondTypeParentRelationToSecondType.getAllowedReferences())
				.thenReturn(new AllowedReferences("second", null));
		when(secondTypeParentRelationToThirdType.getAllowedReferences())
				.thenReturn(new AllowedReferences(null, asSet("third_custom1")));

		Map<Language, String> mapLangueTitle1 = new HashMap<>();
		mapLangueTitle1.put(Language.French, "taxo1");
		Map<Language, String> mapLangueTitle2 = new HashMap<>();
		mapLangueTitle2.put(Language.French, "taxo2");

		Taxonomy firstTaxonomy = Taxonomy.createPublic("taxo1", mapLangueTitle1, "zeCollection", Arrays.asList("first", "second"));
		Taxonomy secondTaxonomy = Taxonomy.createPublic("taxo1", mapLangueTitle2, "zeCollection", Arrays.asList("third"));

		List<Metadata> metadatas = Arrays.asList(secondTypeParentRelationToFirstType, secondTypeParentRelationToSecondType,
				secondTypeParentRelationToThirdType, secondTypeRelationToSecondType);
		List<Taxonomy> taxonomies = Arrays.asList(firstTaxonomy, secondTaxonomy);

		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		MetadataSchema schema = new MetadataSchema((short) 1, "default", "second_default", zeCollectionInfo, labels, metadatas, false,
				true, new HashSet<RecordValidator>(), null, DataStore.RECORDS, true);

		MetadataSchemaType schemaType = new MetadataSchemaType((short) 0, "second", null, zeCollectionInfo, asMap(Language.French, "titre"),
				new ArrayList<MetadataSchema>(), schema, true, true, RecordCacheType.NOT_CACHED, true, false, "records");

		List<Metadata> returnedMetadatas = schemaType.getTaxonomySchemasMetadataWithChildOfRelationship(taxonomies);

		assertThat(returnedMetadatas).containsOnly(secondTypeParentRelationToFirstType, secondTypeParentRelationToSecondType);
	}

	@Test
	public void whenGetTaxonomyReferencesThenReturnCorrectReferences()
			throws Exception {

		Map<Language, String> mapLangueTitle1 = new HashMap<>();
		mapLangueTitle1.put(Language.French, "taxo1");
		Map<Language, String> mapLangueTitle2 = new HashMap<>();
		mapLangueTitle2.put(Language.French, "taxo2");

		Taxonomy firstTaxonomy = Taxonomy.createPublic("taxo1", mapLangueTitle1, "zeCollection", Arrays.asList("t1", "t2"));
		Taxonomy secondTaxonomy = Taxonomy.createPublic("taxo2", mapLangueTitle2, "zeCollection", Arrays.asList("t3", "t4"));
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

		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		MetadataSchema schema = new MetadataSchema((short) 1, "default", "zeType_default", zeCollectionInfo, labels, metadatas, false,
				true, new HashSet<RecordValidator>(), null, DataStore.RECORDS, true);

		List<Metadata> returnedMetadatas = schema.getTaxonomyRelationshipReferences(taxonomies);
		assertThat(returnedMetadatas).containsOnly(taxonomyRelationToT4, taxonomyRelationToT3Custom);

	}

	@Test
	public void whenGetTaxonomyReferencesOfSchemaPartOfAnotherTaxonomiesThenOnlyReturnTaxonomiesForWhichTheSchemaIsntPartsOf()
			throws Exception {

		Map<Language, String> mapLangueTitle1 = new HashMap<>();
		mapLangueTitle1.put(Language.French, "taxo1");
		Map<Language, String> mapLangueTitle2 = new HashMap<>();
		mapLangueTitle2.put(Language.French, "taxo2");

		Taxonomy firstTaxonomy = Taxonomy.createPublic("taxo1", mapLangueTitle1, "zeCollection", Arrays.asList("t1", "t2"));
		Taxonomy secondTaxonomy = Taxonomy.createPublic("taxo2", mapLangueTitle2, "zeCollection", Arrays.asList("t3", "t4"));
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
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		MetadataSchema schema = new MetadataSchema((short) 1, "default", "t2_default", zeCollectionInfo, labels, metadatas, false,
				true, new HashSet<RecordValidator>(), null, DataStore.RECORDS, true);

		List<Metadata> returnedMetadatas = schema.getTaxonomyRelationshipReferences(taxonomies);
		assertThat(returnedMetadatas).containsOnly(taxonomyRelationToT4, taxonomyRelationToT3Custom);

	}

	short nextMetadataId = 1;

	private Metadata mockedTextMetadata() {
		Metadata textMetadata = mock(Metadata.class, "textMetadata");
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(textMetadata.getId()).thenReturn(nextMetadataId++);
		return textMetadata;
	}

	private Metadata mockTaxonomyRefMetadata(String code, String type) {
		Metadata metadata = mockRefMetadata(code, type);
		when(metadata.isTaxonomyRelationship()).thenReturn(true);
		when(metadata.getId()).thenReturn(nextMetadataId++);
		return metadata;
	}

	private Metadata mockTaxonomyRefMetadata(String code, Set<String> schemas) {
		Metadata metadata = mockRefMetadata(code, schemas);
		when(metadata.isTaxonomyRelationship()).thenReturn(true);
		when(metadata.getId()).thenReturn(nextMetadataId++);
		return metadata;
	}

	private Metadata mockRefMetadata(String code, String type) {
		String localCode = code.split("_")[2];
		Metadata metadata = mock(Metadata.class, code);
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getLocalCode()).thenReturn(localCode);
		when(metadata.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata.getAllowedReferences()).thenReturn(new AllowedReferences(type, null));
		when(metadata.getId()).thenReturn(nextMetadataId++);
		return metadata;
	}

	private Metadata mockRefMetadata(String code, Set<String> schemas) {
		String localCode = code.split("_")[2];
		Metadata metadata = mock(Metadata.class, code);
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getLocalCode()).thenReturn(localCode);
		when(metadata.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata.getAllowedReferences()).thenReturn(new AllowedReferences(null, schemas));
		when(metadata.getId()).thenReturn(nextMetadataId++);
		return metadata;
	}
}
