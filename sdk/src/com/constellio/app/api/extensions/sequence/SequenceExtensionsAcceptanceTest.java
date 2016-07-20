package com.constellio.app.api.extensions.sequence;

import static com.constellio.sdk.tests.TestUtils.asMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

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
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.Language;
import com.constellio.sdk.tests.ConstellioTest;

public class SequenceExtensionsAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	AvailableSequence sequence1 = new AvailableSequence("sequence1", asMap(Language.French, "Ze séquence"));
	AvailableSequence sequence2 = new AvailableSequence("sequence2", asMap(Language.French, "Autre séquence"));
	AvailableSequence sequence3 = new AvailableSequence("sequence3", asMap(Language.French, "Troisième séquence"));
	AvailableSequence sequence4 = new AvailableSequence("sequenceRubrique1", asMap(Language.French, "Séquence rubrique 1"));
	AvailableSequence sequence5 = new AvailableSequence("sequenceRubrique2", asMap(Language.French, "Séquence rubrique 2"));
	AvailableSequence sequence6 = new AvailableSequence("sequenceRubrique3", asMap(Language.French, "Séquence rubrique 1"));
	AvailableSequence sequence7 = new AvailableSequence("sequenceRègles", asMap(Language.French, "Séquence règles"));

	@Test
	public void whenDeclareSequenceTablesThenRetrievedUsingExtensionMethods()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule()
		);

		AppLayerExtensions extensions = getAppLayerFactory().getExtensions();
		AppLayerSystemExtensions systemExtensions = extensions.getSystemWideExtensions();
		AppLayerCollectionExtensions zeCollectionExtensions = extensions.forCollection(zeCollection);
		AppLayerCollectionExtensions anotherCollectionExtensions = extensions.forCollection("anotherCollection");

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

		assertThat(zeCollectionExtensions.getAvailableSequencesForRecord(records.getCategory_Z().getWrappedRecord()))
				.usingFieldByFieldElementComparator().containsOnly(sequence4, sequence5);

		assertThat(zeCollectionExtensions.getAvailableSequencesForRecord(records.getRule1().getWrappedRecord()))
				.usingFieldByFieldElementComparator().containsOnly(sequence7);

		assertThat(zeCollectionExtensions.getAvailableSequencesForRecord(records.getUnit10().getWrappedRecord()))
				.usingFieldByFieldElementComparator().isEmpty();

		assertThat(systemExtensions.getAvailableSequences()).usingFieldByFieldElementComparator()
				.containsOnly(sequence1, sequence2, sequence3);
	}
}
