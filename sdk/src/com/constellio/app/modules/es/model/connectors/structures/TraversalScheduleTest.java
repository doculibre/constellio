package com.constellio.app.modules.es.model.connectors.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class TraversalScheduleTest extends ConstellioTest {

	TraversalScheduleFactory factory;
	TraversalSchedule traversalSchedule;

	@Before
	public void setUp() {
		factory = new TraversalScheduleFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		traversalSchedule = new TraversalSchedule();
		assertThat(traversalSchedule.isDirty()).isFalse();

		traversalSchedule = new TraversalSchedule();
		traversalSchedule.setWeekDay(DateTimeConstants.WEDNESDAY);
		assertThat(traversalSchedule.isDirty()).isTrue();

		traversalSchedule = new TraversalSchedule();
		traversalSchedule.setStartTime("12:00");
		assertThat(traversalSchedule.isDirty()).isTrue();

		traversalSchedule = new TraversalSchedule();
		traversalSchedule.setEndTime("13:00");
		assertThat(traversalSchedule.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {
		traversalSchedule = new TraversalSchedule();
				traversalSchedule.setWeekDay(DateTimeConstants.WEDNESDAY);
				traversalSchedule.setStartTime("12:00");
				traversalSchedule.setEndTime("13:00");

		String stringValue = factory.toString(traversalSchedule);
		TraversalSchedule deserialized = (TraversalSchedule) factory.build(stringValue);

		assertThat(deserialized).isEqualTo(traversalSchedule);
		assertThat(deserialized.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {
		traversalSchedule = new TraversalSchedule();

		String stringValue = factory.toString(traversalSchedule);
		TraversalSchedule deserialized = (TraversalSchedule) factory.build(stringValue);

		assertThat(deserialized).isEqualTo(traversalSchedule);
	}

}
