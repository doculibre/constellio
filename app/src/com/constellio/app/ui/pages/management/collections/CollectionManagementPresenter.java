package com.constellio.app.ui.pages.management.collections;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.data.CollectionVODataProvider;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;

public class CollectionManagementPresenter extends BasePresenter<CollectionManagementView> {
	protected transient CollectionsManager collectionManager;
	protected transient RecordServices recordServices;

	public CollectionManagementPresenter(CollectionManagementView view) {
		super(view);
	}

	public CollectionVODataProvider getDataProvider() {
		return new CollectionVODataProvider(appLayerFactory);
	}

	public void editButtonClicked(CollectionVODataProvider dataProvider, Integer index) {
		CollectionVO collectionVO = dataProvider.getRecordVO(index);
		String parameters = getParameters(collectionVO);
		view.navigateTo().addEditCollection(parameters);
	}

	public void deleteButtonClicked(CollectionVODataProvider dataProvider, Integer index) {
		CollectionVO collectionVO = dataProvider.getRecordVO(index);
		dataProvider.delete(index);
		collectionManager().deleteCollection(collectionVO.getCode());
		view.showMessage($("CollectionManagementView.removalConfirmation"));
	}

	CollectionsManager collectionManager() {
		if (collectionManager == null) {
			collectionManager = appLayerFactory.getCollectionsManager();
		}
		return collectionManager;
	}

	public void addButtonClick() {
		view.navigateTo().addEditCollection(getParameters(null));
	}

	private String getParameters(CollectionVO entity) {
		Map<String, Object> params = new HashMap<>();
		if (entity != null) {
			params.put("collectionCode", entity.getCode());
		}
		return ParamUtils.addParams(NavigatorConfigurationService.COLLECTION_MANAGEMENT, params);
	}

	public void backButtonClick() {
		view.navigateTo().adminModule();
	}

	public boolean isDeletePossible(CollectionVODataProvider dataProvider, Integer index) {
		String collection = dataProvider.getRecordVO(index).getCode();

		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential userCredential = userServices.getUserCredential(getCurrentUser().getUsername());

		boolean hasDeleteAccess = userCredential.isSystemAdmin();
		if (!hasDeleteAccess && userCredential.getCollections().contains(collection)) {
			User user = userServices.getUserInCollection(userCredential.getUsername(), collection);
			hasDeleteAccess = user.has(CorePermissions.MANAGE_SYSTEM_COLLECTIONS).globally();
		}

		return hasDeleteAccess && !collection.equals(view.getCollection());
	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		return user.has(CorePermissions.MANAGE_SYSTEM_COLLECTIONS).globally();
	}
}
