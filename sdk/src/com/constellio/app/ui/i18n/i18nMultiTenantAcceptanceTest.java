package com.constellio.app.ui.i18n;


import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.Locale;
import java.util.stream.Stream;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class i18nMultiTenantAcceptanceTest extends ConstellioTest {

	private void givenMultiCulturalTenantsSystem() {
		givenTwoTenants();

		Stream.of("1", "2").forEach(tenantId -> {
			String codeLang = tenantId.equals("1") ? "fr" : "en";

			TenantUtils.setTenant(tenantId);

			givenSystemLanguageIs(codeLang);
			givenCollectionWithTitle(zeCollection, asList(codeLang), "Collection de test tenant " + tenantId);

			i18n.setLocale(new Locale(codeLang));

			TenantUtils.setTenant(null);
		});
	}

	private void givenNoTenantsSystemWithNoSetLocale() {
		givenSystemLanguageIs("fr");
		givenCollectionWithTitle(zeCollection, asList("fr"), "Collection de test tenant ");
	}

	private void givenNoTenantsSystem() {
		givenSystemLanguageIs("fr");
		givenCollectionWithTitle(zeCollection, asList("fr"), "Collection de test tenant ");

		i18n.setLocale(new Locale("fr"));

	}

	@Test
	public void givenTenantsWithDifferentLanguagesWhenInitializingCollectionThenAllHaveTheirDefaultLanguage()
			throws Exception {
		givenMultiCulturalTenantsSystem();

		String tenantId = "1";
		TenantUtils.setTenant(tenantId);
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));

		tenantId = "2";
		TenantUtils.setTenant(tenantId);
		assertThat(i18n.getLocale()).isEqualTo(new Locale("en"));

		//TenantUtils.setTenant(null);
	}

	@Test
	public void givenTenantswhenChangingDefaultLanguageThenOnlyChangedForCurrentTenant() throws Exception {
		givenMultiCulturalTenantsSystem();

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

	@Test
	public void givenNoTenantsWithDifferentLanguagesWhenInitializingCollectionThenAllHaveTheirDefaultLanguage()
			throws Exception {
		givenNoTenantsSystem();
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));
	}

	@Test
	public void givenNoTenantsWhenChangingDefaultLanguageThenOnlyChangedForCurrentTenant() throws Exception {
		givenNoTenantsSystem();

		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));
		i18n.setLocale(new Locale("en"));
		assertThat(i18n.getLocale()).isEqualTo(new Locale("en"));
		i18n.setLocale(new Locale("fr"));
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));
	}


	@Test
	public void givenNoTenantsNoSetLocaleWithDifferentLanguagesWhenInitializingCollectionThenHaveCorrectDefaultLanguage()
			throws Exception {
		givenNoTenantsSystemWithNoSetLocale();
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));
	}

	@Test
	public void givenNoTenantsNoSetLocalewhenChangingDefaultLanguageThenOnlyChangedForCurrentTenant() throws Exception {
		givenNoTenantsSystemWithNoSetLocale();

		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));
		i18n.setLocale(new Locale("en"));
		assertThat(i18n.getLocale()).isEqualTo(new Locale("en"));
		i18n.setLocale(new Locale("fr"));
		assertThat(i18n.getLocale()).isEqualTo(new Locale("fr"));
	}

}