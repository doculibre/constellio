package com.constellio.model.entities.records.wrappers.structure;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminderFactory;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.sdk.tests.ConstellioTest;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

//AFTER Move to task module
public class TaskReminderFactoryAcceptanceTest extends ConstellioTest {

	TaskReminderFactory factory;
	LocalDate fixedDate = LocalDate.now();
	int numberOfDaysInFlexibleDate = 2;
	Boolean beforeFlexibleDate = true;
	String flexibleDateMetadataCode = Task.DUE_DATE;

	@Before
	public void setUp()
			throws Exception {
		factory = new TaskReminderFactory();
	}

	@Test
	public void whenTaskReminderJustCreatedThenNotProcessedAndNumberOfDaysInFlexibleDateEqualsZero() {
		TaskReminder taskReminder = new TaskReminder();
		assertThat(taskReminder.isProcessed()).isFalse();
		assertThat(taskReminder.getNumberOfDaysToRelativeDate()).isEqualTo(0);
	}

	@Test
	public void whenSetAttributeValueThenBecomesDirtyAndValueSet() {
		TaskReminder taskReminder = new TaskReminder();
		assertThat(taskReminder.isDirty()).isFalse();

		taskReminder = new TaskReminder();
		taskReminder.setFixedDate(fixedDate);
		assertThat(taskReminder.isDirty()).isTrue();
		assertThat(taskReminder.getFixedDate()).isEqualTo(fixedDate);

		taskReminder = new TaskReminder();
		taskReminder.setBeforeRelativeDate(beforeFlexibleDate);
		assertThat(taskReminder.isDirty()).isTrue();
		assertThat(taskReminder.isBeforeRelativeDate()).isEqualTo(beforeFlexibleDate);

		taskReminder = new TaskReminder();
		taskReminder.setRelativeDateMetadataCode(flexibleDateMetadataCode);
		assertThat(taskReminder.isDirty()).isTrue();
		assertThat(taskReminder.getRelativeDateMetadataCode()).isEqualTo(flexibleDateMetadataCode);

		taskReminder = new TaskReminder();
		taskReminder.setNumberOfDaysToRelativeDate(numberOfDaysInFlexibleDate);
		assertThat(taskReminder.isDirty()).isTrue();
		assertThat(taskReminder.getNumberOfDaysToRelativeDate()).isEqualTo(numberOfDaysInFlexibleDate);

		taskReminder = new TaskReminder();
		taskReminder.setProcessed(true);
		assertThat(taskReminder.isDirty()).isTrue();
		assertThat(taskReminder.isProcessed()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		TaskReminder taskReminder = new TaskReminder();
		taskReminder.setFixedDate(fixedDate);
		taskReminder.setBeforeRelativeDate(beforeFlexibleDate);
		taskReminder.setRelativeDateMetadataCode(flexibleDateMetadataCode);
		taskReminder.setNumberOfDaysToRelativeDate(numberOfDaysInFlexibleDate);

		String stringValue = factory.toString(taskReminder);
		TaskReminder builtTaskReminder = (TaskReminder) factory.build(stringValue);
		String stringValue2 = factory.toString(builtTaskReminder);

		assertThat(builtTaskReminder).isEqualTo(taskReminder);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtTaskReminder.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {

		TaskReminder taskReminder = new TaskReminder();
		taskReminder.setFixedDate(null);
		taskReminder.setBeforeRelativeDate(null);
		taskReminder.setRelativeDateMetadataCode(null);

		String stringValue = factory.toString(taskReminder);
		TaskReminder builtTaskReminder = (TaskReminder) factory.build(stringValue);
		String stringValue2 = factory.toString(builtTaskReminder);

		assertThat(builtTaskReminder).isEqualTo(taskReminder);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtTaskReminder.isDirty()).isFalse();
		assertThat(builtTaskReminder.getFixedDate()).isNull();
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		TaskReminder TaskReminder = new TaskReminder();

		String stringValue = factory.toString(TaskReminder);
		TaskReminder builtTaskReminder = (TaskReminder) factory.build(stringValue);
		String stringValue2 = factory.toString(builtTaskReminder);

		assertThat(builtTaskReminder).isEqualTo(TaskReminder);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtTaskReminder.isDirty()).isFalse();
		assertThat(builtTaskReminder.getFixedDate()).isNull();
	}
}
