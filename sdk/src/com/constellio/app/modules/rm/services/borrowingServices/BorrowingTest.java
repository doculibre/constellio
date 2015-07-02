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
package com.constellio.app.modules.rm.services.borrowingServices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;

public class BorrowingTest extends ConstellioTest {

	@Mock User bob;
	BorrowingFactory factory;

	LocalDateTime nowDateTime = TimeProvider.getLocalDateTime();

	@Before
	public void setUp()
			throws Exception {
		factory = spy(new BorrowingFactory());

		when(bob.getId()).thenReturn("bobId");
		when(bob.getUsername()).thenReturn("bob");

	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		Borrowing borrowing = new Borrowing();
		assertThat(borrowing.isDirty()).isFalse();

		borrowing = new Borrowing();
		borrowing.setBorrowingType(BorrowingType.FOLDER);
		assertThat(borrowing.isDirty()).isTrue();

		borrowing = new Borrowing();
		borrowing.setBorrower(bob);
		assertThat(borrowing.isDirty()).isTrue();

		borrowing = new Borrowing();
		borrowing.setBorrowDateTime(nowDateTime);
		assertThat(borrowing.isDirty()).isTrue();

		borrowing = new Borrowing();
		borrowing.setReturnDateTime(nowDateTime.plusDays(1));
		assertThat(borrowing.isDirty()).isTrue();

		borrowing = new Borrowing();
		borrowing.setPreviewReturnDateTime(nowDateTime.plusDays(1));
		assertThat(borrowing.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		Borrowing borrowing = new Borrowing();
		borrowing.setBorrowingType(BorrowingType.FOLDER);
		borrowing.setBorrower(bob);
		borrowing.setBorrowDateTime(nowDateTime);
		borrowing.setReturnDateTime(nowDateTime.plusDays(1));
		borrowing.setPreviewReturnDateTime(nowDateTime.plusDays(1));

		String stringValue = factory.toString(borrowing);
		Borrowing builtBorrowing = (Borrowing) factory.build(stringValue);
		String stringValue2 = factory.toString(builtBorrowing);

		assertThat(builtBorrowing).isEqualTo(borrowing);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtBorrowing.isDirty()).isFalse();

	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {

		Borrowing borrowing = new Borrowing();
		borrowing.setBorrowingType(null);
		borrowing.setBorrower(null);
		borrowing.setBorrowDateTime(null);
		borrowing.setReturnDateTime(null);
		borrowing.setPreviewReturnDateTime(null);

		String stringValue = factory.toString(borrowing);
		Borrowing builtBorrowing = (Borrowing) factory.build(stringValue);
		String stringValue2 = factory.toString(builtBorrowing);

		assertThat(builtBorrowing).isEqualTo(borrowing);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtBorrowing.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		Borrowing borrowing = new Borrowing();

		String stringValue = factory.toString(borrowing);
		Borrowing builtBorrowing = (Borrowing) factory.build(stringValue);
		String stringValue2 = factory.toString(builtBorrowing);

		assertThat(builtBorrowing).isEqualTo(borrowing);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtBorrowing.isDirty()).isFalse();
	}
}
