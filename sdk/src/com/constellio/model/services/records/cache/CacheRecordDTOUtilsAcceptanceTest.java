package com.constellio.model.services.records.cache;

import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.KeyLongMap;
import com.constellio.model.services.records.cache.CompiledDTOStats.CompiledDTOStatsBuilder;
import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.stream.Stream;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CacheRecordDTOUtilsAcceptanceTest extends ConstellioTest {

	//ConstellioWebDriver driver;

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
	public void givenTenantsWhenInitializingDTOStatsBuilderThenGettingOtherDTOStatsBuilder() throws Exception {
		String tenantId = "1";
		TenantUtils.setTenant(tenantId);

		assertThat(CacheRecordDTOUtils.getCompiledDTOStatsBuilder()).isNull();

		CacheRecordDTOUtils.startCompilingDTOsStats();
		CompiledDTOStatsBuilder tenant1Builder = CacheRecordDTOUtils.getCompiledDTOStatsBuilder();

		assertThat(tenant1Builder).isNotNull();

		tenantId = "2";
		TenantUtils.setTenant(tenantId);

		assertThat(CacheRecordDTOUtils.getCompiledDTOStatsBuilder()).isNull();

		CacheRecordDTOUtils.startCompilingDTOsStats();
		CompiledDTOStatsBuilder tenant2Builder = CacheRecordDTOUtils.getCompiledDTOStatsBuilder();

		assertThat(tenant2Builder).isNotNull();

		tenantId = "1";
		TenantUtils.setTenant(tenantId);

		assertThat(tenant2Builder).isNotSameAs(CacheRecordDTOUtils.getCompiledDTOStatsBuilder());

		//TenantUtils.setTenant(null);
	}

	@Test
	public void givenTenantsWhenInitializingDTOStatsThenGettingOtherDTOStats() throws Exception {
		String tenantId = "1";
		TenantUtils.setTenant(tenantId);

		CacheRecordDTOUtils.startCompilingDTOsStats();
		CacheRecordDTOUtils.stopCompilingDTOsStats();
		CompiledDTOStats tenant1Stats = CacheRecordDTOUtils.getLastCompiledDTOStats();

		assertThat(tenant1Stats).isNotNull();
		assertThat(CacheRecordDTOUtils.getCompiledDTOStatsBuilder()).isNull();

		tenantId = "2";
		TenantUtils.setTenant(tenantId);

		CacheRecordDTOUtils.startCompilingDTOsStats();
		CacheRecordDTOUtils.stopCompilingDTOsStats();
		CompiledDTOStats tenant2Stats = CacheRecordDTOUtils.getLastCompiledDTOStats();

		assertThat(tenant2Stats).isNotNull();

		tenantId = "1";
		TenantUtils.setTenant(tenantId);

		assertThat(tenant2Stats).isNotSameAs(CacheRecordDTOUtils.getLastCompiledDTOStats());
		assertThat(CacheRecordDTOUtils.getCompiledDTOStatsBuilder()).isNull();

		//TenantUtils.setTenant(null);
	}


	@Test
	public void givenTenantsWhenInitializingFsStoredMetadataUsageThenGettingOtherFsStoredMetadataUsage()
			throws Exception {
		String tenantId = "1";
		TenantUtils.setTenant(tenantId);

		assertThat(CacheRecordDTOUtils.getFilesystemStoredMetadataUsageCounterOrNull()).isNull();

		KeyLongMap<String> tenant1Fs = CacheRecordDTOUtils.getFilesystemStoredMetadataUsageCounterAndInitIfNull();

		assertThat(tenant1Fs).isNotNull();

		tenantId = "2";
		TenantUtils.setTenant(tenantId);

		assertThat(CacheRecordDTOUtils.getFilesystemStoredMetadataUsageCounterOrNull()).isNull();

		KeyLongMap<String> tenant2Fs = CacheRecordDTOUtils.getFilesystemStoredMetadataUsageCounterAndInitIfNull();

		assertThat(tenant2Fs).isNotNull();

		tenantId = "1";
		TenantUtils.setTenant(tenantId);

		assertThat(tenant2Fs).isNotSameAs(CacheRecordDTOUtils.getFilesystemStoredMetadataUsageCounterOrNull());

		//TenantUtils.setTenant(null);
	}
}