/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.base;

import static com.constellio.data.frameworks.extensions.ExtensionUtils.getBooleanValue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.extensions.PageAccessExtension;
import com.constellio.app.extensions.AppLayerCollectionEventsListeners;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.BaseView.ViewEnterListener;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BehaviorCaller;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;

@SuppressWarnings("serial")
public abstract class BasePresenter<T extends BaseView> implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasePresenter.class);
	protected final T view;
	protected final String collection;
	protected transient ModelLayerFactory modelLayerFactory;
	protected transient AppLayerFactory appLayerFactory;
	private BasePresenterUtils presenterUtils;

	public BasePresenter(T view) {
		this(view, view.getConstellioFactories(), view.getSessionContext());
	}

	public BasePresenter(final T view, ConstellioFactories constellioFactories, SessionContext sessionContext) {
		this.view = view;
		view.addViewEnterListener(new ViewEnterListener() {
			@Override
			public void viewEntered(String params) {
			}

			@Override
			public void afterInit(String params) {
				if (!isViewVisibleToCurrentUser(params)) {
					view.navigateTo().home();
				}
			}
		});
		this.collection = sessionContext.getCurrentCollection();
		this.presenterUtils = new BasePresenterUtils(constellioFactories, sessionContext);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		modelLayerFactory = presenterUtils.modelLayerFactory();
		appLayerFactory = presenterUtils.appLayerFactory();
	}

	private boolean isViewVisibleToCurrentUser(String params) {
		User user = getCurrentUser();

		List<String> restrictedRecordIds = getRestrictedRecordIds(params);
		List<Record> restrictedRecords = new ArrayList<>();

		if (restrictedRecordIds != null) {
			for (String recordId : restrictedRecordIds) {
				restrictedRecords.add(recordServices().getDocumentById(recordId));
			}
		}

		String restrictedRecordsCollection = new RecordUtils().getRecordsCollection(restrictedRecords);

		if (restrictedRecordsCollection != null && !restrictedRecordsCollection.equals(user.getCollection())) {
			UserServices userServices = modelLayerFactory.newUserServices();
			try {
				User userInOtherCollection = userServices.getUserInCollection(user.getUsername(), restrictedRecordsCollection);

				boolean access = hasPageAccess(params, userInOtherCollection, restrictedRecords);

				if (access) {
					//Switching to other collection
					UserToVOBuilder voBuilder = new UserToVOBuilder();
					SessionContext sessionContext = presenterUtils.sessionContext;
					UserVO newUserVO = voBuilder.build(userInOtherCollection.getWrappedRecord(), VIEW_MODE.DISPLAY);
					sessionContext.setCurrentCollection(restrictedRecordsCollection);
					sessionContext.setCurrentUser(newUserVO);
					view.updateUI();
					return true;
				} else {
					return false;
				}

			} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
				return false;
			}
		} else {

			return hasPageAccess(params, user, restrictedRecords);
		}
	}

	private boolean hasPageAccess(final String params, final User user, List<Record> restrictedRecords) {
		AppLayerCollectionEventsListeners listeners = appLayerFactory.getExtensions()
				.getCollectionListeners(user.getCollection());

		boolean pageAccess = getBooleanValue(listeners.pageAccessExtensions, hasPageAccess(params, user),
				new BehaviorCaller<PageAccessExtension, ExtensionBooleanResult>() {
					@Override
					public ExtensionBooleanResult call(PageAccessExtension behavior) {
						return behavior.hasPageAccess(BasePresenter.this.getClass(), params, user);
					}
				});

		boolean restrictedRecordsAccess = true;
		if (!restrictedRecords.isEmpty()) {
			for (final Record restrictedRecord : restrictedRecords) {

				boolean defaultValue = hasRestrictedRecordAccess(params, user, restrictedRecord);
				boolean hasAccessToRestrictedRecord = getBooleanValue(listeners.pageAccessExtensions, defaultValue,
						new BehaviorCaller<PageAccessExtension, ExtensionBooleanResult>() {
							@Override
							public ExtensionBooleanResult call(PageAccessExtension behavior) {
								return behavior.hasRestrictedRecordAccess(BasePresenter.this.getClass(), params, user,
										restrictedRecord);
							}
						});

				if (!hasAccessToRestrictedRecord) {
					restrictedRecordsAccess = false;
				}
			}
		}
		return restrictedRecordsAccess && pageAccess;
	}

	protected abstract boolean hasPageAccess(String params, User user);

	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return true;
	}

	protected List<String> getRestrictedRecordIds(String params) {
		return null;
	}

	protected SchemasRecordsServices coreSchemas() {
		return new SchemasRecordsServices(collection, modelLayerFactory);
	}

	protected MetadataSchemaTypes types() {
		return presenterUtils.types();
	}

	protected MetadataSchemaType schemaType(String code) {
		return presenterUtils.schemaType(code);
	}

	protected MetadataSchema schema(String code) {
		return presenterUtils.schema(code);
	}

	protected User getCurrentUser() {
		return presenterUtils.getCurrentUser();
	}

	protected Locale getCurrentLocale() {
		return presenterUtils.getCurrentLocale();
	}

	protected UserServices userServices() {
		return modelLayerFactory.newUserServices();
	}

	protected final RecordServices recordServices() {
		return presenterUtils.recordServices();
	}

	protected final PresenterService presenterService() {
		return presenterUtils.presenterService();
	}

	protected final SearchServices searchServices() {
		return presenterUtils.searchServices();
	}

	protected final SchemasDisplayManager schemasDisplayManager() {
		return presenterUtils.schemasDisplayManager();
	}

	public String getTitlesStringFromIds(List<String> ids) {
		return presenterUtils.getTitlesStringFromIds(ids);
	}

	public String buildString(List<String> list) {
		return presenterUtils.buildString(list);
	}

	public final List<String> getAllRecordIds(String schemaCode) {
		return presenterUtils.getAllRecordIds(schemaCode);
	}
}
