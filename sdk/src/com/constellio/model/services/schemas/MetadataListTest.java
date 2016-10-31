package com.constellio.model.services.schemas;

import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;

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

		when(metadata1.getAllowedReferences()).thenReturn(allowedReferencesMetadata1);
		when(allowedReferencesMetadata1.getTypeWithAllowedSchemas()).thenReturn("type2");
		when(metadata2.getAllowedReferences()).thenReturn(allowedReferencesMetadata2);
		when(allowedReferencesMetadata2.getTypeWithAllowedSchemas()).thenReturn("type2");
		when(metadata3.getAllowedReferences()).thenReturn(allowedReferencesMetadata3);
		when(allowedReferencesMetadata3.getTypeWithAllowedSchemas()).thenReturn("type1");
		when(USRmetadata4.getAllowedReferences()).thenReturn(allowedReferencesUSRmetadata4);
		when(allowedReferencesUSRmetadata4.getTypeWithAllowedSchemas()).thenReturn("type1");
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
}
