package com.constellio.model.services.records;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordIdTest extends ConstellioTest {

	RecordId id1 = new IntegerRecordId(1);
	RecordId id2 = new IntegerRecordId(2);
	RecordId id42_1 = new IntegerRecordId(42);
	RecordId id42_2 = new IntegerRecordId(42);

	RecordId strId1_1 = new StringRecordId("1");
	RecordId strIdA1 = new StringRecordId("A1");
	RecordId strIdA10 = new StringRecordId("A10");
	RecordId strId1_2 = new StringRecordId("1");

	@Test
	public void whenComparingUsingLesserThanThenOk() {
		assertThat(id42_1.lesserThan(id42_2)).isFalse();
		assertThat(id1.lesserThan(id2)).isTrue();
		assertThat(id1.lesserThan(id42_1)).isTrue();
		assertThat(id42_1.lesserThan(id1)).isFalse();
		assertThat(id1.lesserThan(strId1_1)).isTrue();
		assertThat(id1.lesserThan(strIdA1)).isTrue();
		assertThat(strId1_1.lesserThan(id1)).isFalse();
		assertThat(strIdA1.lesserThan(id1)).isFalse();
		assertThat(strId1_1.lesserThan(strIdA1)).isTrue();
		assertThat(strIdA1.lesserThan(strId1_1)).isFalse();
		assertThat(strIdA1.lesserThan(strIdA10)).isTrue();
		assertThat(strIdA10.lesserThan(strIdA1)).isFalse();
		assertThat(strId1_1.lesserThan(strId1_2)).isFalse();
	}

	@Test
	public void whenComparingUsingLesserOrEqualThenOk() {
		assertThat(id42_1.lesserOrEqual(id42_2)).isTrue();
		assertThat(id1.lesserOrEqual(id2)).isTrue();
		assertThat(id1.lesserOrEqual(id42_1)).isTrue();
		assertThat(id42_1.lesserOrEqual(id1)).isFalse();
		assertThat(id1.lesserOrEqual(strId1_1)).isTrue();
		assertThat(id1.lesserOrEqual(strIdA1)).isTrue();
		assertThat(strId1_1.lesserOrEqual(id1)).isFalse();
		assertThat(strIdA1.lesserOrEqual(id1)).isFalse();
		assertThat(strId1_1.lesserOrEqual(strIdA1)).isTrue();
		assertThat(strIdA1.lesserOrEqual(strId1_1)).isFalse();
		assertThat(strIdA1.lesserOrEqual(strIdA10)).isTrue();
		assertThat(strIdA10.lesserOrEqual(strIdA1)).isFalse();
		assertThat(strId1_1.lesserOrEqual(strId1_2)).isTrue();
	}

	@Test
	public void whenComparingUsingGreaterThanThenOk() {
		assertThat(id42_1.greaterThan(id42_2)).isFalse();
		assertThat(id1.greaterThan(id2)).isFalse();
		assertThat(id1.greaterThan(id42_1)).isFalse();
		assertThat(id42_1.greaterThan(id1)).isTrue();
		assertThat(id1.greaterThan(strId1_1)).isFalse();
		assertThat(id1.greaterThan(strIdA1)).isFalse();
		assertThat(strId1_1.greaterThan(id1)).isTrue();
		assertThat(strIdA1.greaterThan(id1)).isTrue();
		assertThat(strId1_1.greaterThan(strIdA1)).isFalse();
		assertThat(strIdA1.greaterThan(strId1_1)).isTrue();
		assertThat(strIdA1.greaterThan(strIdA10)).isFalse();
		assertThat(strIdA10.greaterThan(strIdA1)).isTrue();
		assertThat(strId1_1.greaterThan(strId1_2)).isFalse();
	}

	@Test
	public void whenComparingUsingGreaterOrEqualThenOk() {
		assertThat(id42_1.greaterOrEqual(id42_2)).isTrue();
		assertThat(id1.greaterOrEqual(id2)).isFalse();
		assertThat(id1.greaterOrEqual(id42_1)).isFalse();
		assertThat(id42_1.greaterOrEqual(id1)).isTrue();
		assertThat(id1.greaterOrEqual(strId1_1)).isFalse();
		assertThat(id1.greaterOrEqual(strIdA1)).isFalse();
		assertThat(strId1_1.greaterOrEqual(id1)).isTrue();
		assertThat(strIdA1.greaterOrEqual(id1)).isTrue();
		assertThat(strId1_1.greaterOrEqual(strIdA1)).isFalse();
		assertThat(strIdA1.greaterOrEqual(strId1_1)).isTrue();
		assertThat(strIdA1.greaterOrEqual(strIdA10)).isFalse();
		assertThat(strIdA10.greaterOrEqual(strIdA1)).isTrue();
		assertThat(strId1_1.greaterOrEqual(strId1_2)).isTrue();
	}


	@Test
	public void whenComparingUsingEqualThenOk() {
		assertThat(id42_1.equals(id42_2)).isTrue();
		assertThat(id1.equals(id2)).isFalse();
		assertThat(id1.equals(id42_1)).isFalse();
		assertThat(id42_1.equals(id1)).isFalse();
		assertThat(id1.equals(strId1_1)).isFalse();
		assertThat(id1.equals(strIdA1)).isFalse();
		assertThat(strId1_1.equals(id1)).isFalse();
		assertThat(strIdA1.equals(id1)).isFalse();
		assertThat(strId1_1.equals(strIdA1)).isFalse();
		assertThat(strIdA1.equals(strId1_1)).isFalse();
		assertThat(strIdA1.equals(strIdA10)).isFalse();
		assertThat(strIdA10.equals(strIdA1)).isFalse();
		assertThat(strId1_1.equals(strId1_2)).isTrue();
	}

	@Test
	public void whenComparingUsingCompareToThenOk() {
		assertThat(id42_1.compareTo(id42_2)).isZero();
		assertThat(id1.compareTo(id2)).isNegative();
		assertThat(id1.compareTo(id42_1)).isNegative();
		assertThat(id42_1.compareTo(id1)).isPositive();
		assertThat(id1.compareTo(strId1_1)).isNegative();
		assertThat(id1.compareTo(strIdA1)).isNegative();
		assertThat(strId1_1.compareTo(id1)).isPositive();
		assertThat(strIdA1.compareTo(id1)).isPositive();
		assertThat(strId1_1.compareTo(strIdA1)).isNegative();
		assertThat(strIdA1.compareTo(strId1_1)).isPositive();
		assertThat(strIdA1.compareTo(strIdA10)).isNegative();
		assertThat(strIdA10.compareTo(strIdA1)).isPositive();
		assertThat(strId1_1.compareTo(strId1_2)).isZero();
	}

	@Test
	public void whenGetStringValueThenOk() {
		assertThat(id1.stringValue()).isEqualTo("00000000001");
		assertThat(id2.stringValue()).isEqualTo("00000000002");
		assertThat(id42_1.stringValue()).isEqualTo("00000000042");
		assertThat(id42_2.stringValue()).isEqualTo("00000000042");
		assertThat(strId1_1.stringValue()).isEqualTo("1");
		assertThat(strIdA1.stringValue()).isEqualTo("A1");
		assertThat(strIdA10.stringValue()).isEqualTo("A10");
		assertThat(strId1_2.stringValue()).isEqualTo("1");
	}

	@Test
	public void whenEvaluatingIfIntegerThenOk() {
		assertThat(id1.isInteger()).isTrue();
		assertThat(strId1_1.isInteger()).isFalse();
		assertThat(strIdA10.isInteger()).isFalse();
	}
}
