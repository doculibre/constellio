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

//TODO remove this class
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
		borrowing.setBorrowingType(BorrowingType.BORROW);
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
		borrowing.setBorrowingType(BorrowingType.BORROW);
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
