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

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class DecomListContainerDetailTest extends ConstellioTest {

	DecomListContainerDetailFactory factory;
	DecomListContainerDetail decomListContainerDetail;

	@Before
	public void setUp()
			throws Exception {
		factory = spy(new DecomListContainerDetailFactory());
		decomListContainerDetail = spy(new DecomListContainerDetail());
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		DecomListContainerDetail decomListContainerDetail = new DecomListContainerDetail();
		assertThat(decomListContainerDetail.isDirty()).isFalse();

		decomListContainerDetail = new DecomListContainerDetail();
		decomListContainerDetail.setContainerRecordId("01");
		assertThat(decomListContainerDetail.isDirty()).isTrue();

		decomListContainerDetail = new DecomListContainerDetail();
		decomListContainerDetail.setFull(true);
		assertThat(decomListContainerDetail.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		DecomListContainerDetail decomListContainerDetail = new DecomListContainerDetail();
		decomListContainerDetail.setContainerRecordId("01");
		decomListContainerDetail.setFull(true);

		String stringValue = factory.toString(decomListContainerDetail);
		DecomListContainerDetail builtDecomListContainerDetail = (DecomListContainerDetail) factory
				.build(stringValue);
		String stringValue2 = factory.toString(builtDecomListContainerDetail);

		assertThat(builtDecomListContainerDetail).isEqualTo(decomListContainerDetail);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtDecomListContainerDetail.isDirty()).isFalse();

	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {

		DecomListContainerDetail decomListContainerDetail = new DecomListContainerDetail();
		decomListContainerDetail.setContainerRecordId(null);

		String stringValue = factory.toString(decomListContainerDetail);
		DecomListContainerDetail builtDecomListContainerDetail = (DecomListContainerDetail) factory
				.build(stringValue);
		String stringValue2 = factory.toString(builtDecomListContainerDetail);

		assertThat(builtDecomListContainerDetail).isEqualTo(decomListContainerDetail);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtDecomListContainerDetail.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		DecomListContainerDetail decomListContainerDetail = new DecomListContainerDetail();

		String stringValue = factory.toString(decomListContainerDetail);
		DecomListContainerDetail builtDecomListContainerDetail = (DecomListContainerDetail) factory
				.build(stringValue);
		String stringValue2 = factory.toString(builtDecomListContainerDetail);

		assertThat(builtDecomListContainerDetail).isEqualTo(decomListContainerDetail);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtDecomListContainerDetail.isDirty()).isFalse();
	}
}
