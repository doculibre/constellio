package com.constellio.model.entities.records.wrappers;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

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
	public void whenCallingWraperMethodsForConfiguringVisibleColumnsThenOK()
			throws Exception {

		User admin = users.adminIn(zeCollection);
		assertThat(admin.getVisibleTableColumns()).isNull();
		assertThat(admin.isVisibleTableColumnsConfiguredFor("zeTable")).isFalse();
		assertThat(admin.isDirty()).isFalse();

		admin.setVisibleTableColumns("zeTable", asList("column1", "column2"));
		assertThat(admin.isDirty()).isTrue();
		recordServices.update(admin);
		recordServices.refresh(admin);
		assertThat(admin.getVisibleTableColumnsFor("zeTable")).containsOnly("column1", "column2");
		assertThat(admin.isVisibleTableColumnsConfiguredFor("zeTable")).isTrue();
		assertThat(admin.isVisibleTableColumnsConfiguredFor("otherTable")).isFalse();

		admin.setVisibleTableColumns("otherTable", new ArrayList<String>());
		recordServices.update(admin);
		recordServices.refresh(admin);
		assertThat(admin.isVisibleTableColumnsConfiguredFor("otherTable")).isFalse();

		admin.setVisibleTableColumns("otherTable", null);
		recordServices.update(admin);
		recordServices.refresh(admin);
		assertThat(admin.isVisibleTableColumnsConfiguredFor("otherTable")).isFalse();

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
