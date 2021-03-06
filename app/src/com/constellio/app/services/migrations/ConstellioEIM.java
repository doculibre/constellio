package com.constellio.app.services.migrations;

import com.constellio.app.api.content.GetRecordContentServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.CertifyPdfJSSignaturesServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.GetPdfJSAnnotationsConfigServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.GetPdfJSAnnotationsServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.GetPdfJSSignatureServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.RemovePdfJSSignatureServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.SavePdfJSAnnotationsServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.SavePdfJSSignatureServlet;
import com.constellio.app.api.search.CachedSearchWebService;
import com.constellio.app.api.systemManagement.services.SystemInfoWebService;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.extensions.core.CoreMenuItemActionsExtension;
import com.constellio.app.extensions.ui.AppSupportedExtensionExtension;
import com.constellio.app.extensions.ui.CoreConstellioUIExtension;
import com.constellio.app.services.extensions.AppRecordExtension;
import com.constellio.app.services.extensions.core.CoreSearchFieldExtension;
import com.constellio.app.services.extensions.core.CoreUserProfileFieldsExtension;
import com.constellio.app.services.extensions.core.CoreUserProfileSignatureFieldsExtension;
import com.constellio.app.services.extensions.core.CoreUserRecordExtension;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_4_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_14;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_19;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_21;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_22;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_42;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_50;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1_3_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_3_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_2_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_6_45;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_9;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_0_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_4_11;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_0_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_42;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_3_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_3_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_0_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_40;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_417;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_427;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_428;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_89;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_2_11;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_2_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_3_14;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_3_22;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_42_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_42_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_1_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_1_20;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_2_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_2_11;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_2_12;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_2_13;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_2_20;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_2_900;
import com.constellio.app.services.migrations.scripts.*;
import com.constellio.app.servlet.ConstellioSignInSuccessServlet;
import com.constellio.app.servlet.userSecurity.UserSecurityInfoWebServlet;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import java.util.ArrayList;
import java.util.List;

public class ConstellioEIM {

	public List<MigrationScript> getMigrationScripts() {

		List<MigrationScript> scripts = new ArrayList<>();

		scripts.add(new CoreMigrationTo_5_0_1());
		scripts.add(new CoreMigrationTo_5_0_4());
		scripts.add(new CoreMigrationTo_5_0_5());
		scripts.add(new CoreMigrationTo_5_0_6_6());
		scripts.add(new CoreMigrationTo_5_0_7());
		scripts.add(new CoreMigrationTo_5_1_0());
		scripts.add(new CoreMigrationTo_5_1_1_3());
		scripts.add(new CoreMigrationTo_5_1_2());
		scripts.add(new CoreMigrationTo_5_1_3());
		scripts.add(new CoreMigrationTo_5_1_4());
		scripts.add(new CoreMigrationTo_5_1_6());
		scripts.add(new CoreMigrationTo_5_1_7());
		scripts.add(new CoreMigrationTo_5_2());
		scripts.add(new CoreMigrationTo_6_0());
		scripts.add(new CoreMigrationTo_6_1());
		scripts.add(new CoreMigrationTo_6_3());
		scripts.add(new CoreMigrationTo_6_4());
		scripts.add(new CoreMigrationTo_6_4_1());
		scripts.add(new CoreMigrationTo_6_5());
		scripts.add(new CoreMigrationTo_6_5_14());
		scripts.add(new CoreMigrationTo_6_5_19());
		scripts.add(new CoreMigrationTo_6_5_21());
		scripts.add(new CoreMigrationTo_6_5_50());
		scripts.add(new CoreMigrationTo_6_5_22());
		scripts.add(new CoreMigrationTo_6_5_42());
		scripts.add(new CoreMigrationTo_6_6());
		scripts.add(new CoreMigrationTo_7_0());
		scripts.add(new CoreMigrationTo_7_0_1());
		scripts.add(new CoreMigrationTo_7_1());
		scripts.add(new CoreMigrationTo_7_1_1());
		scripts.add(new CoreMigrationTo_7_1_3_1());
		scripts.add(new CoreMigrationTo_7_2());
		scripts.add(new CoreMigrationTo_7_3());
		scripts.add(new CoreMigrationTo_7_3_0_1());
		scripts.add(new CoreMigrationTo_7_4());
		scripts.add(new CoreMigrationTo_7_4_2());
		scripts.add(new CoreMigrationTo_7_4_3());
		scripts.add(new CoreMigrationTo_7_5());
		scripts.add(new CoreMigrationTo_7_6());
		scripts.add(new CoreMigrationTo_7_6_2());
		scripts.add(new CoreMigrationTo_7_6_2_1());
		scripts.add(new CoreMigrationTo_7_6_6());
		scripts.add(new CoreMigrationTo_7_6_6_45());
		scripts.add(new CoreMigrationTo_7_6_9());
		scripts.add(new CoreMigrationTo_7_7_0_2());
		scripts.add(new CoreMigrationTo_7_7_1());
		scripts.add(new CoreMigrationTo_7_7_1_2());
		scripts.add(new CoreMigrationTo_7_7_1_6());
		scripts.add(new CoreMigrationTo_7_7_2());
		scripts.add(new CoreMigrationTo_7_7_4());
		scripts.add(new CoreMigrationTo_7_7_4_11());
		scripts.add(new CoreMigrationTo_7_7_5());
		scripts.add(new CoreMigrationTo_7_7_6());
		scripts.add(new CoreMigrationTo_7_7_7());
		scripts.add(new CoreMigrationTo_8_0());
		scripts.add(new CoreMigrationTo_8_0_1());
		scripts.add(new CoreMigrationTo_8_0_2());
		scripts.add(new CoreMigrationTo_8_1());
		scripts.add(new CoreMigrationTo_8_1_0_1());
		scripts.add(new CoreMigrationTo_8_1_2());
		scripts.add(new CoreMigrationTo_8_1_3());
		scripts.add(new CoreMigrationTo_8_2());
		scripts.add(new CoreMigrationTo_8_2_1());
		scripts.add(new CoreMigrationTo_8_2_1_1());
		scripts.add(new CoreMigrationTo_8_2_3());
		scripts.add(new CoreMigrationTo_8_2_42());
		scripts.add(new CoreMigrationTo_8_3());
		scripts.add(new CoreMigrationTo_8_3_1());
		scripts.add(new CoreMigrationTo_8_3_1_1());
		scripts.add(new CoreMigrationTo_9_0());
		//scripts.add(new CoreMigrationTo_8_3_0_1());
		scripts.add(new CoreMigrationTo_9_0_0_5());
		scripts.add(new CoreMigrationTo_9_0_1_1());
		scripts.add(new CoreMigrationTo_9_0_1_2());
		scripts.add(new CoreMigrationTo_9_0_1_3());
		scripts.add(new CoreMigrationTo_9_0_1_4());
		scripts.add(new CoreMigrationTo_9_0_1_40());
		scripts.add(new CoreMigrationTo_9_0_1_417());
		scripts.add(new CoreMigrationTo_9_0_1_427());
		scripts.add(new CoreMigrationTo_9_0_1_428());
		scripts.add(new CoreMigrationTo_9_0_2_7());
		scripts.add(new CoreMigrationTo_9_0_2_11());
		scripts.add(new CoreMigrationTo_9_0_3_14());
		scripts.add(new CoreMigrationTo_9_0_3_22());

		scripts.add(new CoreMigrationTo_9_0_42_1());
		scripts.add(new CoreMigrationTo_9_0_1_89());
		scripts.add(new CoreMigrationTo_9_0_42_2());
		scripts.add(new CoreMigrationTo_9_0_3());
		scripts.add(new CoreMigrationTo_9_1_0());
		scripts.add(new CoreMigrationTo_9_1_20());
		scripts.add(new CoreMigrationTo_9_2());
		scripts.add(new CoreMigrationTo_9_2_0_1());
		scripts.add(new CoreMigrationTo_9_2_11());
		scripts.add(new CoreMigrationTo_9_2_12());
		scripts.add(new CoreMigrationTo_9_2_13());
		scripts.add(new CoreMigrationTo_9_2_20());
		scripts.add(new CoreMigrationTo_9_2_21());
		scripts.add(new CoreMigrationTo_9_2_42());
		scripts.add(new CoreMigrationTo_9_2_900());
		scripts.add(new CoreMigrationTo_9_3_0());
		scripts.add(new CoreMigrationTo_9_3_1());
		scripts.add(new CoreMigrationFrom_9_4_updateServerPing());
		scripts.add(new CoreMigrationFrom9_4_AddMenuDisplayManagerToAppLayerFactory());
		scripts.add(new CoreMigrationFrom9_4_AddMetadataHasSeenMessageAtLogin());
		scripts.add(new CoreMigrationFrom10_0_FixReportsI18n());

		return scripts;
	}

	public List<SystemConfiguration> getConfigurations() {
		return ConstellioEIMConfigs.configurations;
	}

	static public void start(AppLayerFactory appLayerFactory) {
		ApplicationStarter.registerServlet("/cachedSelect", new CachedSearchWebService());
		FilterHolder filterHolder = new FilterHolder(new CrossOriginFilter());
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET");
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "content-type,access-control-allow-origin,token,serviceKey");
		filterHolder.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "false");
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		ApplicationStarter.registerFilter("/cachedSelect", filterHolder);

		ApplicationStarter.registerServlet("/systemInfo", new SystemInfoWebService());
		filterHolder = new FilterHolder(new CrossOriginFilter());
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET");
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "content-type,access-control-allow-origin,token,serviceKey");
		filterHolder.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "false");
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		ApplicationStarter.registerFilter("/systemInfo", filterHolder);

		ApplicationStarter.registerServlet("/userSecurityInfo", new UserSecurityInfoWebServlet());
		filterHolder = new FilterHolder(new CrossOriginFilter());
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET");
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "content-type,access-control-allow-origin,authorization");
		filterHolder.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "false");
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		ApplicationStarter.registerFilter("/userSecurityInfo", filterHolder);

		ApplicationStarter.registerServlet("/getRecordContent", new GetRecordContentServlet());

		ApplicationStarter.registerServlet(CertifyPdfJSSignaturesServlet.PATH, new CertifyPdfJSSignaturesServlet());
		ApplicationStarter.registerServlet(GetPdfJSAnnotationsConfigServlet.PATH, new GetPdfJSAnnotationsConfigServlet());
		ApplicationStarter.registerServlet(GetPdfJSAnnotationsServlet.PATH, new GetPdfJSAnnotationsServlet());
		ApplicationStarter.registerServlet(GetPdfJSSignatureServlet.PATH, new GetPdfJSSignatureServlet());
		ApplicationStarter.registerServlet(RemovePdfJSSignatureServlet.PATH, new RemovePdfJSSignatureServlet());
		ApplicationStarter.registerServlet(SavePdfJSAnnotationsServlet.PATH, new SavePdfJSAnnotationsServlet());
		ApplicationStarter.registerServlet(SavePdfJSSignatureServlet.PATH, new SavePdfJSSignatureServlet());
		ApplicationStarter.registerServlet(ConstellioSignInSuccessServlet.PATH, new ConstellioSignInSuccessServlet());

		setupAppLayerSystemExtensions(appLayerFactory);
	}

	static public void start(AppLayerFactory appLayerFactory, String collection) {
		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			configureBaseExtensions(appLayerFactory, collection);
		}
	}

	private static void configureBaseExtensions(AppLayerFactory appLayerFactory, String collection) {
		configureBaseAppLayerExtensions(appLayerFactory, collection);
		configureBaseModelLayerExtensions(appLayerFactory, collection);
		configureBaseDataLayerExtensions(appLayerFactory);

	}

	private static void setupAppLayerSystemExtensions(AppLayerFactory appLayerFactory) {
		AppLayerSystemExtensions extensions = appLayerFactory.getExtensions().getSystemWideExtensions();
		extensions.constellioUIExtentions.add(new CoreConstellioUIExtension(appLayerFactory));
	}

	private static void configureBaseAppLayerExtensions(AppLayerFactory appLayerFactory, String collection) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);

		extensions.pagesComponentsExtensions.add(new CoreUserProfileFieldsExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new CoreUserProfileSignatureFieldsExtension(collection, appLayerFactory));
		extensions.menuItemActionsExtensions.add(new CoreMenuItemActionsExtension(collection, appLayerFactory));
	}

	private static void configureBaseModelLayerExtensions(AppLayerFactory appLayerFactory, String collection) {
		ModelLayerFactory modelFactory = appLayerFactory.getModelLayerFactory();
		modelFactory.getExtensions().forCollection(collection)
				.schemaExtensions.add(new CoreSearchFieldExtension(collection, appLayerFactory));

		modelFactory.getExtensions().forCollection(collection).recordExtensions.add(new AppRecordExtension(appLayerFactory, collection));
		modelFactory.getExtensions().forCollection(collection)
				.recordExtensions.add(new CoreUserRecordExtension(collection, appLayerFactory.getModelLayerFactory()));
	}

	private static void configureBaseDataLayerExtensions(AppLayerFactory appLayerFactory) {
		DataLayerSystemExtensions dataLayerSystemExtensions = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getExtensions().getSystemWideExtensions();
		dataLayerSystemExtensions.supportedExtensionExtensions.add(new AppSupportedExtensionExtension(appLayerFactory));
	}
}
