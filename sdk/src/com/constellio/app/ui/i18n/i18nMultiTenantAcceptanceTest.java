package com.constellio.app.ui.i18n;


import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.stream.Stream;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class i18nMultiTenantAcceptanceTest extends ConstellioTest {

	protected void givenMultiCulturalTenantsSystem() {
		givenTwoTenants();
		prepareSystemWithoutHyperTurbo();

		Stream.of("1", "2").forEach(tenantId -> {
			String codeLang = tenantId.equals("1") ? "fr" : "en";

			TenantUtils.setTenant(tenantId);

			givenSystemLanguageIs(codeLang);
			givenCollectionWithTitle(zeCollection, asList(codeLang), "Collection de test tenant " + tenantId);

			i18n.setLocale(new Locale(codeLang));

			TenantUtils.setTenant(null);
		});
	}

	@Before
	public void setUp()
			throws Exception {

		givenMultiCulturalTenantsSystem();

	}

	@Test
	public void givenTenantsWithDifferentLanguagesWhenInitializingCollectionThenAllHaveTheirDefaultLanguage()
			throws Exception {
		String tenantId = "1";
		TenantUtils.setTenant(tenantId);
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));

		tenantId = "2";
		TenantUtils.setTenant(tenantId);
		assertThat(i18n.getLocale()).isEqualTo(new Locale("en"));

		//TenantUtils.setTenant(null);
	}

	@Test
	public void whenChangingDefaultLanguageThenOnlyChangedForCurrentTenant() throws Exception {
		String tenantId = "1";
		TenantUtils.setTenant(tenantId);

		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));
		i18n.setLocale(new Locale("en"));
		assertThat(i18n.getLocale()).isEqualTo(new Locale("en"));
		i18n.setLocale(new Locale("fr"));
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));

		tenantId = "2";
		TenantUtils.setTenant(tenantId);
		assertThat(i18n.getLocale()).isEqualTo(new Locale("en"));
		i18n.setLocale(new Locale("fr"));
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));
		i18n.setLocale(new Locale("en"));
		assertThat(i18n.getLocale()).isEqualTo(new Locale("en"));

		tenantId = "1";
		TenantUtils.setTenant(tenantId);
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));

		//TenantUtils.setTenant(null);
	}
}