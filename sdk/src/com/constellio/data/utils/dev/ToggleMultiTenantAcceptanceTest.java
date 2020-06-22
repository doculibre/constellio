package com.constellio.data.utils.dev;

import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.stream.Stream;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ToggleMultiTenantAcceptanceTest extends ConstellioTest {

	protected void givenMultiCulturalTenantsSystem() {
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
	public void givenTenantsWhenGettingToggleThenModifyingIt() throws Exception {
		givenMultiCulturalTenantsSystem();
		TenantUtils.setTenant("1");

		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.enable()).isEqualTo(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);

		TenantUtils.setTenant("2");

		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.enable()).isEqualTo(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);

		TenantUtils.setTenant("1");

		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.disable()).isEqualTo(true);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);

		TenantUtils.setTenant("2");

		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.set(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.set(true);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.ensureEnabled();
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.reset();
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.ensureDisabled();
	}


	@Test
	public void givenNoTenantsWhenGettingToggleThenModifyingIt() throws Exception {
		givenNoTenantsSystem();

		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.enable()).isEqualTo(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);

		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.disable()).isEqualTo(true);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);

		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.enable()).isEqualTo(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.set(false);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.set(true);
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(true);
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.ensureEnabled();
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.reset();
		assertThat(Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()).isEqualTo(false);
		Toggle.SDK_CACHE_INTEGRITY_VALIDATION.ensureDisabled();
	}
}