package com.constellio.model.entities.records.wrappers;

import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withAllTest(users));
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void givenUserThenHavePersonalEmailsMetadata()
			throws Exception {

		User admin = users.adminIn(zeCollection);
		admin.setPersonalEmails(Arrays.asList("admin@gmail.com"));
		recordServices.update(admin);

		admin = users.adminIn(zeCollection);
		assertThat(admin.getPersonalEmails()).isEqualTo(Arrays.asList("admin@gmail.com"));
	}
}
