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
package com.constellio.app.modules.rm.wrappers.structures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListFolderDetailTest extends ConstellioTest {

	@Mock User bob;
	DecomListFolderDetailFactory factory;
	DecomListFolderDetail decomListFolderDetail;

	@Before
	public void setUp()
			throws Exception {
		factory = spy(new DecomListFolderDetailFactory());
		decomListFolderDetail = spy(new DecomListFolderDetail());

		when(bob.getId()).thenReturn("bobId");
		when(bob.getUsername()).thenReturn("bob");

	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		DecomListFolderDetail decomListFolderDetail = new DecomListFolderDetail();
		assertThat(decomListFolderDetail.isDirty()).isFalse();

		decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setFolderId("01");
		assertThat(decomListFolderDetail.isDirty()).isTrue();

		decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setFolderIncluded(true);
		assertThat(decomListFolderDetail.isDirty()).isTrue();

		decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setValidationUser(bob);
		assertThat(decomListFolderDetail.isDirty()).isTrue();

		decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setValidationDate(TimeProvider.getLocalDate());
		assertThat(decomListFolderDetail.isDirty()).isTrue();

		decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setContainerRecordId("containerRecordId");
		assertThat(decomListFolderDetail.isDirty()).isTrue();

		decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setReversedSort(true);
		assertThat(decomListFolderDetail.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		DecomListFolderDetail decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setFolderId("01");
		decomListFolderDetail.setFolderIncluded(true);
		decomListFolderDetail.setValidationUser(bob);
		decomListFolderDetail.setValidationDate(TimeProvider.getLocalDate());
		decomListFolderDetail.setContainerRecordId("containerRecordId");
		decomListFolderDetail.setReversedSort(true);

		String stringValue = factory.toString(decomListFolderDetail);
		DecomListFolderDetail builtDecomListFolderDetail = (DecomListFolderDetail) factory
				.build(stringValue);
		String stringValue2 = factory.toString(builtDecomListFolderDetail);

		assertThat(builtDecomListFolderDetail).isEqualTo(decomListFolderDetail);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtDecomListFolderDetail.isDirty()).isFalse();

	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {

		DecomListFolderDetail decomListFolderDetail = new DecomListFolderDetail();
		decomListFolderDetail.setFolderId(null);
		decomListFolderDetail.setValidationUser(null);
		decomListFolderDetail.setValidationDate(null);
		decomListFolderDetail.setContainerRecordId(null);

		String stringValue = factory.toString(decomListFolderDetail);
		DecomListFolderDetail builtDecomListFolderDetail = (DecomListFolderDetail) factory
				.build(stringValue);
		String stringValue2 = factory.toString(builtDecomListFolderDetail);

		assertThat(builtDecomListFolderDetail).isEqualTo(decomListFolderDetail);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtDecomListFolderDetail.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		DecomListFolderDetail decomListFolderDetail = new DecomListFolderDetail();

		String stringValue = factory.toString(decomListFolderDetail);
		DecomListFolderDetail builtDecomListFolderDetail = (DecomListFolderDetail) factory
				.build(stringValue);
		String stringValue2 = factory.toString(builtDecomListFolderDetail);

		assertThat(builtDecomListFolderDetail).isEqualTo(decomListFolderDetail);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtDecomListFolderDetail.isDirty()).isFalse();
	}
}
