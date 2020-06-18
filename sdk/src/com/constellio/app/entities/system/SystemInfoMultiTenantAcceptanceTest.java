package com.constellio.app.entities.system;

import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.stream.Stream;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SystemInfoMultiTenantAcceptanceTest extends ConstellioTest {

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
	public void givenTenantsWhenInitializingSystemInfoThenGettingOtherSystemInfo() throws Exception {
		givenMultiCulturalTenantsSystem();

		TenantUtils.setTenant("1");

		SystemInfo insTenant1 = SystemInfo.getInstance();
		assertThat(insTenant1).isNotNull();

		TenantUtils.setTenant("2");

		SystemInfo insTenant2 = SystemInfo.getInstance();
		assertThat(insTenant2).isNotNull();

		assertThat(insTenant1).isNotSameAs(insTenant2);
	}


	@Test
	public void givenNoTenantsWhenInitializingSystemInfoThenGettingOtherSystemInfo() throws Exception {
		givenNoTenantsSystem();

		SystemInfo insTenant1 = SystemInfo.getInstance();
		assertThat(insTenant1).isNotNull();
	}
}