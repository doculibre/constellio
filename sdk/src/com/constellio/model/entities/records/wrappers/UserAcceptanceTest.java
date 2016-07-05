package com.constellio.model.entities.records.wrappers;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class UserAcceptanceTest extends ConstellioTest {

	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withAllTest(users));

	}

	@Test
	public void whenCallingWraperMethodsForConfiguringVisibleColumnsThenOK()
			throws Exception {

		User admin = users.adminIn(zeCollection);
		assertThat(admin.getVisibleTableColumns()).isNull();
		assertThat(admin.isVisibleTableColumnsConfiguredFor("zeTable")).isFalse();
		assertThat(admin.isDirty()).isFalse();

		admin.setVisibleTableColumns("zeTable", asList("column1", "column2"));
		assertThat(admin.getVisibleTableColumnsFor("zeTable")).containsOnly("column1", "column2");
		assertThat(admin.isVisibleTableColumnsConfiguredFor("zeTable")).isTrue();
		assertThat(admin.isVisibleTableColumnsConfiguredFor("otherTable")).isFalse();
		assertThat(admin.isDirty()).isTrue();

		admin.setVisibleTableColumns("otherTable", new ArrayList<String>());
		assertThat(admin.isVisibleTableColumnsConfiguredFor("otherTable")).isFalse();

		admin.setVisibleTableColumns("otherTable", null);
		assertThat(admin.isVisibleTableColumnsConfiguredFor("otherTable")).isFalse();

		assertThat(users.adminIn(zeCollection).getVisibleTableColumns()).isNull();

	}
}
