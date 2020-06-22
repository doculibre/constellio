package com.constellio.model.entities.configs;

import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.stream.Stream;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SystemConfigurationMultiTenantAcceptanceTest extends ConstellioTest {

	private void givenMultiCulturalTenantsSystem() {
		givenTwoTenants();

		Stream.of("1", "2").forEach(tenantId -> {
			String codeLang = tenantId.equals("1") ? "fr" : "en";

			TenantUtils.setTenant(tenantId);

			givenSystemLanguageIs(codeLang);
			givenCollectionWithTitle(zeCollection, asList(codeLang), "Collection de test tenant " + tenantId);

			TenantUtils.setTenant(null);
		});
	}

	private void givenNoTenantsSystem() {
		givenSystemLanguageIs("fr");
		givenCollectionWithTitle(zeCollection, asList("fr"), "Collection de test tenant ");
	}

	@Test
	public void givenTenantsWhenInitializingSysConfThenTogglingHiddenStatus() throws Exception {
		givenMultiCulturalTenantsSystem();

		TenantUtils.setTenant("1");

		SystemConfigurationGroup aGroup = new SystemConfigurationGroup("zeModule", "aGroup");
		SystemConfiguration number1WithDefaultValue = aGroup.createInteger("number1WithDefaultValue").withDefaultValue(42);
		SystemConfiguration number2WithDefaultValue = aGroup.createInteger("number2WithDefaultValue").withDefaultValue(40).whichIsHidden();

		assertThat(number1WithDefaultValue.isHidden()).isFalse();
		assertThat(number2WithDefaultValue.isHidden()).isTrue();

		number1WithDefaultValue.hide();
		number2WithDefaultValue.setHidden(false);

		assertThat(number2WithDefaultValue.isHidden()).isFalse();
		assertThat(number1WithDefaultValue.isHidden()).isTrue();

		TenantUtils.setTenant("2");

		assertThat(number1WithDefaultValue.isHidden()).isFalse();
		assertThat(number2WithDefaultValue.isHidden()).isTrue();

		number1WithDefaultValue.hide();
		number2WithDefaultValue.setHidden(false);

		assertThat(number2WithDefaultValue.isHidden()).isFalse();
		assertThat(number1WithDefaultValue.isHidden()).isTrue();

		TenantUtils.setTenant("1");

		assertThat(number2WithDefaultValue.isHidden()).isFalse();
		assertThat(number1WithDefaultValue.isHidden()).isTrue();

	}

	@Test
	public void givenNoTenantsWhenInitializingSysConfThenTogglingHiddenStatus() throws Exception {
		givenNoTenantsSystem();

		SystemConfigurationGroup aGroupWithNoTenantSetted = new SystemConfigurationGroup("zeModule", "aGroup");
		SystemConfiguration number1WithDefaultValueWithNoTenantSetted = aGroupWithNoTenantSetted.createInteger("number1WithDefaultValue").withDefaultValue(42);
		SystemConfiguration number2WithDefaultValueWithNoTenantSetted = aGroupWithNoTenantSetted.createInteger("number2WithDefaultValue").withDefaultValue(40).whichIsHidden();

		assertThat(number1WithDefaultValueWithNoTenantSetted.isHidden()).isFalse();
		assertThat(number2WithDefaultValueWithNoTenantSetted.isHidden()).isTrue();

		number1WithDefaultValueWithNoTenantSetted.hide();
		number2WithDefaultValueWithNoTenantSetted.setHidden(false);

		assertThat(number2WithDefaultValueWithNoTenantSetted.isHidden()).isFalse();
		assertThat(number1WithDefaultValueWithNoTenantSetted.isHidden()).isTrue();

	}
}