package com.constellio.app.ui.pages.collection;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.Map;

public class CollectionSecurityManagementPresenter extends BasePresenter<CollectionSecurityManagement> {


	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private User user;

	private boolean groupTabSelectedFirst = false;


	public CollectionSecurityManagementPresenter(CollectionSecurityManagement view) {
		super(view);
		initTransientObjects();

	}

	private void initTransientObjects() {
		user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	protected boolean canAddUser() {
		return user.has(CorePermissions.MANAGE_SYSTEM_USERS).globally();
	}

	protected boolean canAddGroup() {
		return user.has(CorePermissions.MANAGE_SYSTEM_GROUPS).globally();
	}

	public void viewAssembled() {
		selectInitialTabForUser();
	}

	public void forParams(String params) {
		Map<String, String> paramMap = ParamUtils.getParamsMap(params);

		if (paramMap != null) {
			groupTabSelectedFirst = paramMap.get("groupTabSelectedFirst") != null;
		}
	}

	public boolean isGroupTabSelectedFirst() {
		return groupTabSelectedFirst;
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
		view.navigate().to().addEditUserCredential(params, null);
	}
}
