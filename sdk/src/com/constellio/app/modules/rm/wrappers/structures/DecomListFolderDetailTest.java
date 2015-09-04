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

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class DecomListFolderDetailTest extends ConstellioTest {
	DecomListFolderDetailFactory factory;
	DecomListFolderDetail detail;

	@Before
	public void setUp() {
		factory = new DecomListFolderDetailFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		detail = new DecomListFolderDetail();
		assertThat(detail.isDirty()).isFalse();

		detail = new DecomListFolderDetail();
		detail.setFolderId("01");
		assertThat(detail.isDirty()).isTrue();

		detail = new DecomListFolderDetail();
		detail.setFolderIncluded(false);
		assertThat(detail.isDirty()).isTrue();

		detail = new DecomListFolderDetail();
		detail.setContainerRecordId("containerRecordId");
		assertThat(detail.isDirty()).isTrue();

		detail = new DecomListFolderDetail();
		detail.setReversedSort(true);
		assertThat(detail.isDirty()).isTrue();

		detail = new DecomListFolderDetail();
		detail.setFolderLinearSize(123.0);
		assertThat(detail.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {
		detail = new DecomListFolderDetail()
				.setFolderId("01")
				.setFolderIncluded(false)
				.setContainerRecordId("containerRecordId")
				.setReversedSort(true)
				.setFolderLinearSize(123.4d);

		String serialized = factory.toString(detail);
		DecomListFolderDetail deserialized = (DecomListFolderDetail) factory.build(serialized);

		assertThat(deserialized).isEqualTo(detail);
	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {
		detail = new DecomListFolderDetail();

		String serialized = factory.toString(detail);
		DecomListFolderDetail deserialized = (DecomListFolderDetail) factory.build(serialized);

		assertThat(deserialized).isEqualTo(detail);
	}
}
