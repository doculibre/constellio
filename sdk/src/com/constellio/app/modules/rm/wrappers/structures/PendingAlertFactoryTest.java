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
