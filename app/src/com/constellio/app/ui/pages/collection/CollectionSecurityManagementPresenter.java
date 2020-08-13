package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.Map;

public class CollectionSecurityManagementPresenter extends BasePresenter<CollectionSecurityManagement> {


	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private User user;

	private Map<String, String> params = null;

	private transient ConstellioEIMConfigs eimConfigs;
	private transient RMConfigs rmConfigs;
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient BorrowingServices borrowingServices;
	private transient MetadataSchemasManager metadataSchemasManager;
	private transient RecordServices recordServices;
	private transient ModelLayerCollectionExtensions extensions;
	private transient RMModuleExtensions rmModuleExtensions;


	public CollectionSecurityManagementPresenter(CollectionSecurityManagement view) {
		super(view);
		initTransientObjects();

	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		recordServices = modelLayerFactory.newRecordServices();
		extensions = modelLayerFactory.getExtensions().forCollection(collection);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection);

	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public void viewAssembled() {

		selectInitialTabForUser();
	}

	public void forParams(String params) {

	}

	void userTabSelected() {
		view.selectUserTab();
	}

	void groupTabSelected() {
		view.selectGroupTab();
	}

	public int getAutocompleteBufferSize() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		return modelLayerFactory.getSystemConfigs().getAutocompleteSize();
	}

	public void selectInitialTabForUser() {
		view.selectUserTab();
	}

	private Navigation navigate() {
		return view.navigate();
	}

	public void addGroupButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.COLLECTION_USER_LIST, null);
		view.navigate().to().addGlobalGroup(params);
	}

	public void addUserButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.COLLECTION_USER_LIST, null);
		view.navigate().to().addEditUserCredential(params);

	}
}
