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
package com.constellio.model.services.schemas;

import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static org.assertj.core.api.Assertions.assertThat;
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
	Metadata metadata4 = mockMetadata("type1_default_metadata4");
	@Mock AllowedReferences allowedReferencesMetadata1;
	@Mock AllowedReferences allowedReferencesMetadata2;
	@Mock AllowedReferences allowedReferencesMetadata3;
	@Mock AllowedReferences allowedReferencesMetadata4;

	@Before
	public void setup()
			throws Exception {

		when(metadata1.getDataStoreCode()).thenReturn("metadata1_s");
		when(metadata2.getDataStoreCode()).thenReturn("metadata2_s");
		when(metadata3.getDataStoreCode()).thenReturn("metadata3_s");
		when(metadata4.getDataStoreCode()).thenReturn("metadata4_s");

		metadatas1 = new ArrayList<>();
		metadatas1.add(metadata1);
		metadatas1.add(metadata2);
		metadatas2 = new ArrayList<>();
		metadatas2.add(metadata3);
		metadatas2.add(metadata4);

		metadataList = spy(new MetadataList());
	}

	@Test
	public void whenNewMetadataListWithCollectionThenTheyAreAdded()
			throws Exception {

		metadataList = spy(new MetadataList(metadatas1));

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get(metadata1.getDataStoreCode())).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get(metadata2.getDataStoreCode())).isEqualTo(metadata2);
	}

	@Test
	public void whenAddTwiceSameMetadataThenAddedOnce()
			throws Exception {

		metadataList.add(metadata1);
		metadataList.add(metadata1);

		assertThat(metadataList.nestedList).hasSize(1);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get(metadata1.getDataStoreCode())).isEqualTo(metadata1);
	}

	@Test
	public void whenAddAllTwiceThenAddOnce()
			throws Exception {

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas1);

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get(metadata1.getDataStoreCode())).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get(metadata2.getDataStoreCode())).isEqualTo(metadata2);
	}

	@Test
	public void givenMetadatasWhenAddAllToIndexThenAddAll()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.addAll(1, metadatas2);

		assertThat(metadataList.nestedList).hasSize(4);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata3);
		assertThat(metadataList.nestedList.get(2)).isEqualTo(metadata4);
		assertThat(metadataList.nestedList.get(3)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get(metadata1.getDataStoreCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get(metadata2.getDataStoreCode())).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata3.getCode())).isEqualTo(metadata3);
		assertThat(metadataList.codeIndex.get(metadata3.getCode())).isEqualTo(metadata3);
		assertThat(metadataList.datastoreCodeIndex.get(metadata3.getDataStoreCode())).isEqualTo(metadata3);
		assertThat(metadataList.codeIndex.get(metadata4.getCode())).isEqualTo(metadata4);
		assertThat(metadataList.codeIndex.get(metadata4.getCode())).isEqualTo(metadata4);
		assertThat(metadataList.datastoreCodeIndex.get(metadata4.getDataStoreCode())).isEqualTo(metadata4);
	}

	@Test
	public void givenMetadatasWhenAddAllTwiceToIndexThenAddAllOnce()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.addAll(1, metadatas1);

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get(metadata1.getDataStoreCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get(metadata2.getDataStoreCode())).isEqualTo(metadata2);

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

		metadataList.add(1, metadata4);

		assertThat(metadataList.nestedList).hasSize(3);
		assertThat(metadataList.codeIndex.get(metadata4.getCode())).isEqualTo(metadata4);
		assertThat(metadataList.codeIndex.get(metadata4.getCode())).isEqualTo(metadata4);
		assertThat(metadataList.datastoreCodeIndex.get(metadata4.getDataStoreCode())).isEqualTo(metadata4);
	}

	@Test
	public void givenMetadatasWhenAddAnAlreadyExistentToIndexThenAdded()
			throws Exception {

		metadataList.addAll(metadatas1);

		metadataList.add(1, metadata1);

		assertThat(metadataList.nestedList).hasSize(2);
		assertThat(metadataList.nestedList.get(0)).isEqualTo(metadata1);
		assertThat(metadataList.nestedList.get(1)).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata1.getCode())).isEqualTo(metadata1);
		assertThat(metadataList.datastoreCodeIndex.get(metadata1.getDataStoreCode())).isEqualTo(metadata1);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.codeIndex.get(metadata2.getCode())).isEqualTo(metadata2);
		assertThat(metadataList.datastoreCodeIndex.get(metadata2.getDataStoreCode())).isEqualTo(metadata2);

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
		assertThat(metadataList.contains(metadata4)).isFalse();
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
		assertThat(metadataList.contains(metadata4)).isFalse();
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
		when(metadata4.getAllowedReferences()).thenReturn(allowedReferencesMetadata4);
		when(allowedReferencesMetadata4.getTypeWithAllowedSchemas()).thenReturn("type1");
		when(metadata1.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata2.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata3.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata4.getType()).thenReturn(MetadataValueType.REFERENCE);

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		MetadataList filteredMetadataList = metadataList.onlyReferencesToType("type1");

		assertThat(filteredMetadataList.nestedList).hasSize(2);
		assertThat(filteredMetadataList.nestedList.get(0)).isEqualTo(metadata3);
		assertThat(filteredMetadataList.nestedList.get(1)).isEqualTo(metadata4);
		assertThat(filteredMetadataList.contains(metadata3)).isTrue();
		assertThat(filteredMetadataList.contains(metadata4)).isTrue();
	}

	@Test
	public void whenFilterChildOfThenReturnTwo()
			throws Exception {

		when(metadata1.isChildOfRelationship()).thenReturn(false);
		when(metadata2.isChildOfRelationship()).thenReturn(true);
		when(metadata3.isChildOfRelationship()).thenReturn(false);
		when(metadata4.isChildOfRelationship()).thenReturn(true);
		when(metadata1.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata2.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata3.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata4.getType()).thenReturn(MetadataValueType.REFERENCE);

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		MetadataList filteredMetadataList = metadataList.onlyParentReferences();

		assertThat(filteredMetadataList.nestedList).hasSize(2);
		assertThat(filteredMetadataList.nestedList.get(0)).isEqualTo(metadata2);
		assertThat(filteredMetadataList.nestedList.get(1)).isEqualTo(metadata4);
		assertThat(filteredMetadataList.contains(metadata2)).isTrue();
		assertThat(filteredMetadataList.contains(metadata4)).isTrue();
	}

	@Test
	public void whenFilterNonParentReferencesThenReturnTwo()
			throws Exception {

		when(metadata1.isChildOfRelationship()).thenReturn(false);
		when(metadata2.isChildOfRelationship()).thenReturn(true);
		when(metadata3.isChildOfRelationship()).thenReturn(false);
		when(metadata4.isChildOfRelationship()).thenReturn(true);
		when(metadata1.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata2.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata3.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadata4.getType()).thenReturn(MetadataValueType.REFERENCE);

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
		when(metadata4.isEnabled()).thenReturn(true);

		metadataList.addAll(metadatas1);
		metadataList.addAll(metadatas2);

		MetadataList filteredMetadataList = metadataList.onlyEnabled();

		assertThat(filteredMetadataList.nestedList).hasSize(2);
		assertThat(filteredMetadataList.nestedList.get(0)).isEqualTo(metadata2);
		assertThat(filteredMetadataList.nestedList.get(1)).isEqualTo(metadata4);
		assertThat(filteredMetadataList.contains(metadata2)).isTrue();
		assertThat(filteredMetadataList.contains(metadata4)).isTrue();
	}
}
