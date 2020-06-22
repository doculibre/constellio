package com.constellio.app.extensions.api.scripts;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ScriptsMultiTenantAcceptanceTest extends ConstellioTest {

	private class CustomTestScript extends ScriptWithLogOutput {

		public CustomTestScript(AppLayerFactory appLayerFactory) {
			super(appLayerFactory, "TMP", "Create file");
		}

		@Override
		protected void execute()
				throws Exception {

			IOServices ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
			File file = ioServices.newTemporaryFile("tempfile1");
			file.createNewFile();
		}
	}

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
	public void givenTenantsWhenInitializingScriptsThenGettingOtherScripts() throws Exception {
		givenMultiCulturalTenantsSystem();

		TenantUtils.setTenant("1");

		assertThat(Scripts.getScripts()).isEmpty();
		CustomTestScript scriptTenant1 = new CustomTestScript(ConstellioFactories.getInstance("1").getAppLayerFactory());
		Scripts.registerScript(scriptTenant1);

		List<Script> lstTenant1 = Scripts.getScripts();
		assertThat(lstTenant1).hasSize(1);
		assertThat(lstTenant1.get(0)).isSameAs(scriptTenant1);

		TenantUtils.setTenant("2");

		assertThat(Scripts.getScripts()).isEmpty();
		CustomTestScript scriptTenant2 = new CustomTestScript(ConstellioFactories.getInstance("2").getAppLayerFactory());
		Scripts.registerScript(scriptTenant2);

		List<Script> lstTenant2 = Scripts.getScripts();
		assertThat(lstTenant2).hasSize(1);
		assertThat(lstTenant2.get(0)).isSameAs(scriptTenant2);

		assertThat(lstTenant1.get(0)).isNotSameAs(lstTenant2.get(0));

		TenantUtils.setTenant("1");
		Scripts.removeScripts();
		lstTenant1 = Scripts.getScripts();

		assertThat(lstTenant1).hasSize(0);

		TenantUtils.setTenant("2");

		lstTenant2 = Scripts.getScripts();
		assertThat(lstTenant2).hasSize(1);

		//TenantUtils.setTenant(null);
	}


	@Test
	public void givenNoTenantsWhenInitializingScriptsThenGettingOtherScripts() throws Exception {
		givenNoTenantsSystem();

		assertThat(Scripts.getScripts()).isEmpty();
		CustomTestScript scriptTenant1 = new CustomTestScript(ConstellioFactories.getInstance().getAppLayerFactory());
		Scripts.registerScript(scriptTenant1);

		List<Script> lstTenant1 = Scripts.getScripts();
		assertThat(lstTenant1).hasSize(1);
		assertThat(lstTenant1.get(0)).isSameAs(scriptTenant1);

		Scripts.removeScripts();
		lstTenant1 = Scripts.getScripts();

		assertThat(lstTenant1).hasSize(0);

		//TenantUtils.setTenant(null);
	}
}