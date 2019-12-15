package com.constellio.model.services.schemas;

import com.constellio.app.services.schemas.bulkImport.DummyCalculator;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluator;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MetadataListTest extends ConstellioTest {

	MetadataList metadataList;
	Collection<Metadata> metadatas1;
	Collection<Metadata> metadatas2;
	Metadata metadata1 = mockMetadata("type1_default_metadata1");
	Metadata metadata2 = mockMetadata("type1_default_metadata2");
	Metadata metadata3 = mockMetadata("type1_default_metadata3");
	Metadata USRmetadata4 = mockMetadata("type1_default_USRmetadata4");
	@Mock AllowedReferences allowedReferencesMetadata1;
	@Mock AllowedReferences allowedReferencesMetadata2;
	@Mock AllowedReferences allowedReferencesMetadata3;
	@Mock AllowedReferences allowedReferencesUSRmetadata4;

	@Before
	public void setup()
			throws Exception {

		when(metadata1.getDataStoreCode()).thenReturn("metadata1_s");
		when(metadata2.getDataStoreCode()).thenReturn("metadata2_s");
		when(metadata3.getDataStoreCode()).thenReturn("metadata3_s");
		when(USRmetadata4.getDataStoreCode()).thenReturn("USRmetadata4_s");

		metadatas1 = new ArrayList<>();
		metadatas1.add(metadata1);
		metadatas1.add(metadata2);
		metadatas2 = new ArrayList<>();
		metadatas2.add(metadata3);
		metadatas2.add(USRmetadata4);

		metadataList = spy(new MetadataList());
	}

	@Test
	public void whenNewMetadataListWithCollectionThenTheyAreAdded()
			throws Exception {

		metadataList = spy(new MetadataList(metadatas1));

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get("metadata1_s")).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get("metadata2_s")).isEqualTo(metadata2);
	}

	@Test
	public void whenEvaluatingIfContainingAMetadataThenWorkNoMatterIfAMetadataIsFromACustomSchema()
			throws Exception {

		Metadata defaultMetadata1 = mockMetadata("type1_default_metadata1");
		Metadata customMetadata1 = mockMetadata("type1_custom_metadata1");
		doReturn(defaultMetadata1).when(customMetadata1).getInheritance();

		Metadata defaultMetadata2 = mockMetadata("type1_default_metadata2");
		Metadata customMetadata2 = mockMetadata("type1_custom_metadata2");
		doReturn(defaultMetadata2).when(customMetadata2).getInheritance();

		Metadata customMetadataWithoutInheritance = mockMetadata("type1_custom_metadataWithoutInheritance");

		Metadata defaultUSRmetadata4 = mockMetadata("type1_default_USRmetadata4");
		Metadata customUSRmetadata4 = mockMetadata("type1_custom_USRmetadata4");
		Metadata defaultMetadata1InAnotherType = mockMetadata("type2_default_metadata1");
		Metadata customMetadata1InAnotherType = mockMetadata("type2_custom_metadata1");

		metadataList.add(defaultMetadata1);
		metadataList.add(customMetadata2);
		metadataList.add(customMetadataWithoutInheritance);

		assertThat(metadataList.contains(defaultMetadata1)).isTrue();
		assertThat(metadataList.contains(customMetadata1)).isTrue();
		assertThat(metadataList.contains(defaultMetadata2)).isTrue();
		assertThat(metadataList.contains(customMetadata2)).isTrue();
		assertThat(metadataList.contains(customMetadataWithoutInheritance)).isTrue();

		assertThat(metadataList.contains(defaultUSRmetadata4)).isFalse();
		assertThat(metadataList.contains(customUSRmetadata4)).isFalse();
		assertThat(metadataList.contains(defaultMetadata1InAnotherType)).isFalse();
		assertThat(metadataList.contains(customMetadata1InAnotherType)).isFalse();
	}

	@Test
	public void whenAddTwiceSameMetadataThenAddedOnce()
			throws Exception {

		metadataList.add(metadata1);
		metadataList.add(metadata1);

		assertThat(metadataList.nestedList).hasSize(1);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get("metadata1_s")).isEqualTo(metadata1);
	}

	@Test
	public void whenAddAllTwiceThenAddOnce()
			throws Exception {

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas1);

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get("metadata1_s")).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get("metadata2_s")).isEqualTo(metadata2);
	}

	@Test
	public void givenMetadatasWhenAddAllToIndexThenAddAll()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.addAll(1, metadatas2);

		assertThat(metadataList.nestedList).hasSize(4);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata3);
		assertThat(metadataList.nestedList.get(2)).isEqualTo(USRmetadata4);
		assertThat(metadataList.nestedList.get(3)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get("metadata1_s")).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get("metadata2_s")).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata3")).isEqualTo(metadata3);
		assertThat(metadataList.codeIndex.get("type1_default_metadata3")).isEqualTo(metadata3);
		assertThat(metadataList.datastoreCodeIndex.get("metadata3_s")).isEqualTo(metadata3);
		assertThat(metadataList.codeIndex.get("type1_default_USRmetadata4")).isEqualTo(USRmetadata4);
		assertThat(metadataList.codeIndex.get("type1_default_USRmetadata4")).isEqualTo(USRmetadata4);
		assertThat(metadataList.datastoreCodeIndex.get("USRmetadata4_s")).isEqualTo(USRmetadata4);
	}

	@Test
	public void givenMetadatasWhenAddAllTwiceToIndexThenAddAllOnce()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.addAll(1, metadatas1);

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get("metadata1_s")).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get("metadata2_s")).isEqualTo(metadata2);

	}

	@Test
	public void whenContainsThenReturnFalse()
			throws Exception {

		assertThat(metadataList.nestedList).isEmpty();
		assertThat(metadataList.contains(metadata1)).isFalse();
		assertThat(metadataList.containsAll(metadatas1)).isFalse();
	}

	@Test
	public void givenMetadatasWhenContainsThenReturnTrue()
			throws Exception {

		metadataList.add(metadata1);

		assertThat(metadataList.nestedList).hasSize(1);
		assertThat(metadataList.contains(metadata1)).isTrue();
	}

	@Test
	public void givenMetadatasWhenAddToIndexThenAdded()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.add(1, USRmetadata4);

		assertThat(metadataList.nestedList).hasSize(3);
		assertThat(metadataList.codeIndex.get("type1_default_USRmetadata4")).isEqualTo(USRmetadata4);
		assertThat(metadataList.codeIndex.get("type1_default_USRmetadata4")).isEqualTo(USRmetadata4);
		assertThat(metadataList.datastoreCodeIndex.get("USRmetadata4_s")).isEqualTo(USRmetadata4);
	}

	@Test
	public void givenMetadatasWhenAddAnAlreadyExistentToIndexThenAdded()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.add(1, metadata1);

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get("type1_default_metadata1")).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get("metadata1_s")).isEqualTo(metadata1);

		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get("type1_default_metadata2")).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get("metadata2_s")).isEqualTo(metadata2);

		assertThat(metadataList.codeIndex.get("type1_default_metadata3")).isNull();
		assertThat(metadataList.codeIndex.get("type1_default_metadata3")).isNull();
		assertThat(metadataList.datastoreCodeIndex.get("metadata3_s")).isNull();
	}

	@Test
	public void givenMetadatasWhenRemoveThenRemoved()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.remove(metadata1);

		assertThat(metadataList.nestedList).hasSize(1);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata2);
		assertThat(metadataList.contains(metadata1)).isFalse();
	}

	@Test
	public void givenMetadatasWhenRemoveFromIndexThenRemoved()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.remove(0);

		assertThat(metadataList.nestedList).hasSize(1);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata2);
		assertThat(metadataList.contains(metadata1)).isFalse();
	}

	@Test
	public void givenMetadatasWhenRemoveAllThenTheyAreRemoved()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.removeAll(metadatas1);

		assertThat(metadataList.nestedList).isEmpty();
		assertThat(metadataList.contains(metadata1)).isFalse();
	}

	@Test
	public void givenMetadatasWhenRetainAllThenTheyAreRetained()
			throws Exception {

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		metadataList.retainAll(metadatas1);

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.contains(metadata1)).isTrue();
		assertThat(metadataList.contains(metadata2)).isTrue();
		assertThat(metadataList.contains(metadata3)).isFalse();
		assertThat(metadataList.contains(USRmetadata4)).isFalse();
	}

	@Test
	public void givenMetadatasWhenClearThenAllAreRemovedInIndexes()
			throws Exception {

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		metadataList.clear();

		assertThat(metadataList.nestedList).isEmpty();
		assertThat(metadataList.contains(metadata1)).isFalse();
		assertThat(metadataList.contains(metadata2)).isFalse();
		assertThat(metadataList.contains(metadata3)).isFalse();
		assertThat(metadataList.contains(USRmetadata4)).isFalse();
	}

	@Test
	public void givenMetadatasWhenSetThenSetToIndexes()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.set(0, metadata3);

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata3);
		assertThat(metadataList.contains(metadata3)).isTrue();
		assertThat(metadataList.contains(metadata1)).isFalse();
	}

	@Test
	public void whenFilterReferencesToThenReturnTwo()
			throws Exception {

		when(metadata1.getReferencedSchemaTypeCode()).thenReturn("type2");
		when(metadata2.getReferencedSchemaTypeCode()).thenReturn("type2");
		when(metadata3.getReferencedSchemaTypeCode()).thenReturn("type1");
		when(USRmetadata4.getReferencedSchemaTypeCode()).thenReturn("type1");
		when(metadata1.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata2.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata3.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(USRmetadata4.getType()).thenReturn(MetadataValueType.REFERENCE);

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		MetadataList filteredMetadataList = metadataList.onlyReferencesToType("type1");

		assertThat(filteredMetadataList.nestedList).hasSize(2);
		assertThat(filteredMetadataList.nestedList.get(0)).isEqualTo(metadata3);
		assertThat(filteredMetadataList.nestedList.get(1)).isEqualTo(USRmetadata4);
		assertThat(filteredMetadataList.contains(metadata3)).isTrue();
		assertThat(filteredMetadataList.contains(USRmetadata4)).isTrue();
	}

	@Test
	public void whenFilterChildOfThenReturnTwo()
			throws Exception {

		when(metadata1.isChildOfRelationship()).thenReturn(false);
		when(metadata2.isChildOfRelationship()).thenReturn(true);
		when(metadata3.isChildOfRelationship()).thenReturn(false);
		when(USRmetadata4.isChildOfRelationship()).thenReturn(true);
		when(metadata1.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata2.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata3.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(USRmetadata4.getType()).thenReturn(MetadataValueType.REFERENCE);

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		MetadataList filteredMetadataList = metadataList.onlyParentReferences();

		assertThat(filteredMetadataList.nestedList).hasSize(2);
		assertThat(filteredMetadataList.nestedList.get(0)).isEqualTo(metadata2);
		assertThat(filteredMetadataList.nestedList.get(1)).isEqualTo(USRmetadata4);
		assertThat(filteredMetadataList.contains(metadata2)).isTrue();
		assertThat(filteredMetadataList.contains(USRmetadata4)).isTrue();
	}

	@Test
	public void whenFilterOnTypesThenOK()
			throws Exception {

		when(metadata1.getType()).thenReturn(MetadataValueType.NUMBER);
		when(metadata2.getType()).thenReturn(MetadataValueType.BOOLEAN);
		when(metadata3.getType()).thenReturn(MetadataValueType.BOOLEAN);
		when(USRmetadata4.getType()).thenReturn(MetadataValueType.STRING);

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		assertThat(metadataList.onlyWithType(MetadataValueType.BOOLEAN, MetadataValueType.STRING))
				.containsOnly(metadata2, metadata3, USRmetadata4);

		assertThat(metadataList.excludingValueTypes(MetadataValueType.BOOLEAN, MetadataValueType.STRING))
				.containsOnly(metadata1);
	}

	@Test
	public void whenFilterNonParentReferencesThenReturnTwo()
			throws Exception {

		when(metadata1.isChildOfRelationship()).thenReturn(false);
		when(metadata2.isChildOfRelationship()).thenReturn(true);
		when(metadata3.isChildOfRelationship()).thenReturn(false);
		when(USRmetadata4.isChildOfRelationship()).thenReturn(true);
		when(metadata1.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata2.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata3.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(USRmetadata4.getType()).thenReturn(MetadataValueType.REFERENCE);

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		MetadataList filteredMetadataList = metadataList.onlyNonParentReferences();

		assertThat(filteredMetadataList.nestedList).hasSize(2);
		assertThat(filteredMetadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(filteredMetadataList.nestedList.get(1)).isEqualTo(metadata3);
		assertThat(filteredMetadataList.contains(metadata1)).isTrue();
		assertThat(filteredMetadataList.contains(metadata3)).isTrue();
	}

	@Test
	public void whenFilterEnableThenReturnTwo()
			throws Exception {

		when(metadata1.isEnabled()).thenReturn(false);
		when(metadata2.isEnabled()).thenReturn(true);
		when(metadata3.isEnabled()).thenReturn(false);
		when(USRmetadata4.isEnabled()).thenReturn(true);

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		MetadataList filteredMetadataList = metadataList.onlyEnabled();

		assertThat(filteredMetadataList.nestedList).hasSize(2);
		assertThat(filteredMetadataList.nestedList.get(0)).isEqualTo(metadata2);
		assertThat(filteredMetadataList.nestedList.get(1)).isEqualTo(USRmetadata4);
		assertThat(filteredMetadataList.contains(metadata2)).isTrue();
		assertThat(filteredMetadataList.contains(USRmetadata4)).isTrue();
	}

	@Test
	public void whenGetOnlyUSRMetadatasThenOK()
			throws Exception {

		metadataList.add(metadata1);
		metadataList.add(metadata2);
		metadataList.add(metadata3);
		metadataList.add(USRmetadata4);

		assertThat(metadataList.onlyUSR().toLocalCodesList()).containsOnly("USRmetadata4");
	}

	@Test
	public void whenGetManualAndCalculatedWithEvaluatorMetadatasThenOK() {
		when(metadata1.getDataEntry()).thenReturn(new ManualDataEntry());
		when(metadata2.getDataEntry()).thenReturn(new CalculatedDataEntry(new DummyCalculatorWithEvaluator()));
		when(metadata3.getDataEntry()).thenReturn(new CalculatedDataEntry(new DummyCalculator()));
		when(USRmetadata4.getDataEntry()).thenReturn(new SequenceDataEntry(null, null));

		metadataList.add(metadata1);
		metadataList.add(metadata2);
		metadataList.add(metadata3);

		assertThat(metadataList.onlyManualsAndCalculatedWithEvaluator()).containsOnly(metadata1, metadata2);
	}

	private class DummyCalculatorWithEvaluator extends AbstractMetadataValueCalculator {

		DummyCalculatorWithEvaluator() {
			calculatorEvaluator = new CalculatorEvaluator() {
				@Override
				public List<? extends LocalDependency> getDependencies() {
					return Collections.emptyList();
				}

				@Override
				public boolean isAutomaticallyFilled(CalculatorEvaluatorParameters parameters) {
					return false;
				}
			};
		}

		@Override
		public Object calculate(CalculatorParameters parameters) {
			return "test";
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Collections.emptyList();
		}
	}

}
