package com.constellio.app.modules.rm.wrappers.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.TimeProvider;
import com.constellio.sdk.tests.ConstellioTest;

public class PendingAlertFactoryTest extends ConstellioTest {

	PendingAlertFactory factory;

	LocalDateTime nowDateTime = TimeProvider.getLocalDateTime();

	@Before
	public void setUp()
			throws Exception {
		factory = new PendingAlertFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		PendingAlert pendingAlert = new PendingAlert();
		assertThat(pendingAlert.isDirty()).isFalse();

		pendingAlert = new PendingAlert();
		pendingAlert.setAlertCode(AlertCode.LATE_FOLDER);
		assertThat(pendingAlert.isDirty()).isTrue();

		pendingAlert = new PendingAlert();
		pendingAlert.setOn(nowDateTime);
		assertThat(pendingAlert.isDirty()).isTrue();

		pendingAlert = new PendingAlert();
		pendingAlert.setReminderCount(1.0);
		assertThat(pendingAlert.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		PendingAlert pendingAlert = new PendingAlert();
		pendingAlert.setOn(nowDateTime);
		pendingAlert.setAlertCode(AlertCode.LATE_FOLDER);
		pendingAlert.setReminderCount(1.0);

		String stringValue = factory.toString(pendingAlert);
		PendingAlert builtPendingAlert = (PendingAlert) factory.build(stringValue);
		String stringValue2 = factory.toString(builtPendingAlert);

		assertThat(builtPendingAlert).isEqualTo(pendingAlert);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtPendingAlert.isDirty()).isFalse();

	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {

		PendingAlert pendingAlert = new PendingAlert();
		pendingAlert.setOn(null);
		pendingAlert.setReminderCount(null);
		pendingAlert.setReminderCount(null);

		String stringValue = factory.toString(pendingAlert);
		PendingAlert builtPendingAlert = (PendingAlert) factory.build(stringValue);
		String stringValue2 = factory.toString(builtPendingAlert);

		assertThat(builtPendingAlert).isEqualTo(pendingAlert);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtPendingAlert.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		PendingAlert pendingAlert = new PendingAlert();

		String stringValue = factory.toString(pendingAlert);
		PendingAlert builtPendingAlert = (PendingAlert) factory.build(stringValue);
		String stringValue2 = factory.toString(builtPendingAlert);

		assertThat(builtPendingAlert).isEqualTo(pendingAlert);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtPendingAlert.isDirty()).isFalse();
	}
}
