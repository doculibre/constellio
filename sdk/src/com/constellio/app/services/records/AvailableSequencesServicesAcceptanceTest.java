package com.constellio.app.services.records;

import static com.constellio.model.entities.Language.French;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.extensions.sequence.AvailableSequenceForRecordParams;
import com.constellio.app.extensions.sequence.AvailableSequenceForSystemParams;
import com.constellio.app.extensions.sequence.CollectionSequenceExtension;
import com.constellio.app.extensions.sequence.SystemSequenceExtension;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class AvailableSequencesServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	AvailableSequence sequence1 = new AvailableSequence("sequence1", asMap(French, "Ze séquence"));
	AvailableSequence sequence2 = new AvailableSequence("sequence2", asMap(French, "Autre séquence"));
	AvailableSequence sequence3 = new AvailableSequence("sequence3", asMap(French, "Troisième séquence"));
	AvailableSequence sequence4 = new AvailableSequence("sequenceRubrique1", asMap(French, "Séquence rubrique 1"));
	AvailableSequence sequence5 = new AvailableSequence("sequenceRubrique2", asMap(French, "Séquence rubrique 2"));
	AvailableSequence sequence6 = new AvailableSequence("sequenceRubrique3", asMap(French, "Séquence rubrique 1"));
	AvailableSequence sequence7 = new AvailableSequence("sequenceRègles", asMap(French, "Séquence règles"));

	AvailableSequencesServices services;
	AppLayerExtensions extensions;
	AppLayerSystemExtensions systemExtensions;
	AppLayerCollectionExtensions zeCollectionExtensions;
	AppLayerCollectionExtensions anotherCollectionExtensions;
	MetadataSchemasManager metadataSchemasManager;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withConstellioRMModule()
		);
		services = new AvailableSequencesServices(getAppLayerFactory());
		extensions = getAppLayerFactory().getExtensions();
		systemExtensions = extensions.getSystemWideExtensions();
		zeCollectionExtensions = extensions.forCollection(zeCollection);
		anotherCollectionExtensions = extensions.forCollection("anotherCollection");
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
	}

	@Test
	public void whenDeclaredSequenceTablesUsingExtensionsThenRetrievedInAvailableSequencesServices()
			throws Exception {

		systemExtensions.systemSequenceExtensions.add(new SystemSequenceExtension() {
			@Override
			public List<AvailableSequence> getAvailableSequences(AvailableSequenceForSystemParams params) {
				return asList(sequence1, sequence2);
			}
		});

		systemExtensions.systemSequenceExtensions.add(new SystemSequenceExtension() {
			@Override
			public List<AvailableSequence> getAvailableSequences(AvailableSequenceForSystemParams params) {
				return asList(sequence1, sequence3);
			}
		});

		zeCollectionExtensions.collectionSequenceExtensions.add(new CollectionSequenceExtension() {
			@Override
			public List<AvailableSequence> getAvailableSequencesForRecord(AvailableSequenceForRecordParams params) {
				if (params.isSchemaType(Category.SCHEMA_TYPE)) {
					return asList(sequence4, sequence5);

				} else {
					return new ArrayList<AvailableSequence>();
				}
			}
		});

		zeCollectionExtensions.collectionSequenceExtensions.add(new CollectionSequenceExtension() {
			@Override
			public List<AvailableSequence> getAvailableSequencesForRecord(AvailableSequenceForRecordParams params) {
				if (params.isSchemaType(RetentionRule.SCHEMA_TYPE)) {
					return asList(sequence7);

				} else {
					return new ArrayList<AvailableSequence>();
				}
			}
		});

		zeCollectionExtensions.collectionSequenceExtensions.add(new CollectionSequenceExtension() {
			@Override
			public List<AvailableSequence> getAvailableSequencesForRecord(AvailableSequenceForRecordParams params) {
				return null;
			}
		});

		anotherCollectionExtensions.collectionSequenceExtensions.add(new CollectionSequenceExtension() {
			@Override
			public List<AvailableSequence> getAvailableSequencesForRecord(AvailableSequenceForRecordParams params) {
				return asList(sequence6);
			}
		});

		assertThat(services.getAvailableSequencesForRecord(records.getCategory_Z().getWrappedRecord()))
				.usingFieldByFieldElementComparator().containsOnly(sequence4, sequence5);

		assertThat(services.getAvailableSequencesForRecord(records.getRule1().getWrappedRecord()))
				.usingFieldByFieldElementComparator().containsOnly(sequence7);

		assertThat(services.getAvailableSequencesForRecord(records.getUnit10().getWrappedRecord()))
				.usingFieldByFieldElementComparator().isEmpty();

		assertThat(services.getAvailableGlobalSequences()).usingFieldByFieldElementComparator()
				.containsOnly(sequence1, sequence2, sequence3);
	}

	@Test
	public void whenDeclaredSequenceTablesUsingSequenceMetadatasThenRetrievedInAvailableSequencesServices()
			throws Exception {

		metadataSchemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("sequentialNumber").setType(STRING)
						.setLabels(asMap(French, "Numéro séquentiel de la rubrique"))
						.defineDataEntry().asSequenceDefinedByMetadata(Folder.CATEGORY);

				types.getSchema(Folder.DEFAULT_SCHEMA).create("sequentialNumberNotBasedOnAReferemce").setType(STRING)
						.setLabels(asMap(French, "Numéro séquentiel de la rubrique non-basé sur une référence"))
						.defineDataEntry().asSequenceDefinedByMetadata(Folder.CATEGORY_CODE);

				types.getSchema(Folder.DEFAULT_SCHEMA).create("sequentialNumber2").setType(STRING)
						.setLabels(asMap(French, "Numéro séquentiel de l'unité "))
						.defineDataEntry().asSequenceDefinedByMetadata(Folder.ADMINISTRATIVE_UNIT);

				types.getSchema(Folder.DEFAULT_SCHEMA).create("globalSequentialNumber").setType(STRING)
						.setLabels(asMap(French, "Numéro séquentiel global"))
						.defineDataEntry().asFixedSequence("globalSequence");

				types.getSchema(RetentionRule.DEFAULT_SCHEMA).create("globalSequentialNumber").setType(STRING)
						.setLabels(asMap(French, "Numéro séquentiel global"))
						.defineDataEntry().asFixedSequence("globalSequence");

				types.getSchema(RetentionRule.DEFAULT_SCHEMA).create("globalSequentialNumber2").setType(STRING)
						.setLabels(asMap(French, "Numéro séquentiel global"))
						.defineDataEntry().asFixedSequence("globalSequence2");

				types.getSchema(Document.DEFAULT_SCHEMA).create("sequentialNumber").setType(STRING)
						.setLabels(asMap(French, "Numéro séquentiel de l'unité"))
						.defineDataEntry().asSequenceDefinedByMetadata(Document.FOLDER_ADMINISTRATIVE_UNIT);
			}
		});

		assertThat(services.getAvailableSequencesForRecord(records.getCategory_X100().getWrappedRecord())).containsOnly(
				new AvailableSequence(records.categoryId_X100, asMap(French, "Séquence utilisée par le type de schéma Dossier"))
		);

		assertThat(services.getAvailableSequencesForRecord(records.getUnit10().getWrappedRecord())).containsOnly(
				new AvailableSequence(records.unitId_10,
						asMap(French, "Séquence utilisée par les types de schéma Document, Dossier"))
		);

		assertThat(services.getAvailableGlobalSequences()).containsOnly(
				new AvailableSequence("globalSequence",
						asMap(French, "Séquence utilisée par les types de schéma Dossier, Règle de conservation")),
				new AvailableSequence("globalSequence2",
						asMap(French, "Séquence utilisée par le type de schéma Règle de conservation"))
		);

		assertThat(services.getAvailableSequencesForRecord(records.getFolder_A01().getWrappedRecord())).isEmpty();
	}
}
