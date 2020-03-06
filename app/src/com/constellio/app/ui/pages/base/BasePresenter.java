package com.constellio.app.ui.pages.base;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.BaseView.ViewEnterListener;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;

@SuppressWarnings("serial")
public abstract class BasePresenter<T extends BaseView> extends Observable implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasePresenter.class);
	protected final T view;
	protected final String collection;
	protected transient ModelLayerFactory modelLayerFactory;
	protected transient AppLayerFactory appLayerFactory;

	private BasePresenterUtils presenterUtils;

	protected transient AppLayerCollectionExtensions appCollectionExtentions;
	protected transient AppLayerSystemExtensions appSystemExtentions;
	private NavigationConfig config;

	public BasePresenter(T view) {
		this(view, view.getConstellioFactories(), view.getSessionContext());
	}

	public BasePresenter(final T view, ConstellioFactories constellioFactories, SessionContext sessionContext) {
		this.view = view;
		view.addViewEnterListener(new ViewEnterListener() {
			private String viewEnteredParams;

			@Override
			public void viewEntered(String params) {
				viewEnteredParams = params;
			}

			@Override
			public void afterInit(String params) {
				if (!isViewVisibleToCurrentUser(params)) {
					LOGGER.warn("Error does not have access to the page");
					view.navigate().to().home();
				}
			}


			@Override
			public boolean exception(Exception e) {
				boolean exceptionHandled;
				if (e instanceof RecordWrapperRuntimeException.WrappedRecordAndTypesCollectionMustBeTheSame) {
					RecordWrapperRuntimeException.WrappedRecordAndTypesCollectionMustBeTheSame wrongCollectionException =
							(RecordWrapperRuntimeException.WrappedRecordAndTypesCollectionMustBeTheSame) e;
					String recordCollection = wrongCollectionException.getRecordCollection();
					String sessionContextCollection = view.getSessionContext().getCurrentCollection();

					if (sessionContextCollection.equals(recordCollection) ||
						viewEnteredParams != null && isViewVisibleToCurrentUser(viewEnteredParams)) {
						exceptionHandled = true;
						view.navigate().to().currentView();
					} else {
						exceptionHandled = false;
					}
				} else {
					exceptionHandled = false;
				}
				return exceptionHandled;
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
		appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
		appSystemExtentions = appLayerFactory.getExtensions().getSystemWideExtensions();
	}

	protected void clearRequestCache() {
		ConstellioFactories constellioFactories = presenterUtils.getConstellioFactories();
		constellioFactories.onRequestEnded();
		constellioFactories.onRequestStarted();
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
					UserVO newUserVO = voBuilder
							.build(userInOtherCollection.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
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
		boolean result;
		if (user != null) {
			AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(user);

			boolean pageAccessDefaultValue = hasPageAccess(params, user);
			boolean pageAccess = extensions.hasPageAccess(pageAccessDefaultValue, getClass(), params, user);

			boolean restrictedRecordsAccess = true;
			if (!restrictedRecords.isEmpty()) {
				for (final Record restrictedRecord : restrictedRecords) {

					boolean restrictedRecordAccessDefaultValue = hasRestrictedRecordAccess(params, user, restrictedRecord);
					boolean restrictedRecordAccess = extensions.hasRestrictedRecordAccess(restrictedRecordAccessDefaultValue,
							getClass(), params, user, restrictedRecord);

					if (!restrictedRecordAccess) {
						restrictedRecordsAccess = false;
					}
				}
			}
			result = restrictedRecordsAccess && pageAccess;
		} else {
			result = false;
		}
		return result;
	}

	public ComponentState getStateFor(NavigationItem item) {
		return item.getStateFor(getCurrentUser(), appLayerFactory);
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

	protected SchemasRecordsServices coreSchemas(String collection) {
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

	public final void clearCurrentUserCache() {
		presenterUtils.clearCurrentUserCache();
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

	protected final CollectionsManager collectionsManager() {
		return presenterUtils.getCollectionManager();
	}

	public String buildString(List<String> list) {
		return presenterUtils.buildString(list);
	}

	public final List<String> getAllRecordIds(String schemaCode) {
		return presenterUtils.getAllRecordIds(schemaCode);
	}

	public final User wrapUser(Record record) {
		return presenterUtils.wrapUser(record);
	}

	public Roles getCollectionRoles() {
		return presenterUtils.getCollectionRoles();
	}

	public ValidationErrors validateDeletable(RecordVO entity) {
		Record record = presenterService().getRecord(entity.getId());
		User user = getCurrentUser();
		return recordServices().validateLogicallyThenPhysicallyDeletable(record, user);
	}

	protected NavigationConfig navigationConfig() {
		if (config == null) {
			ConstellioModulesManagerImpl manager = (ConstellioModulesManagerImpl) appLayerFactory.getModulesManager();
			config = manager.getNavigationConfig(view.getCollection());
		}
		return config;
	}

	public RMReportBuilderFactories getRmReportBuilderFactories() {
		final AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		final RMModuleExtensions rmModuleExtensions = extensions.forModule(ConstellioRMModule.ID);
		return rmModuleExtensions.getReportBuilderFactories();
	}

	public ContentManager.ContentVersionDataSummaryResponse uploadContent(final InputStream inputStream,
																		  UploadOptions options) {
		return presenterUtils.uploadContent(inputStream, options);
	}

	public List<String> getConceptsWithPermissionsForCurrentUser(String... permissions) {
		return presenterUtils.getConceptsWithPermissionsForCurrentUser(permissions);
	}



	/*public String getGuideUrl() {
		GuideManager manager = new GuideManager(appLayerFactory.getModelLayerFactory().getDataLayerFactory());
		String language = ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage();
		String field = "guide." + getClass().getSimpleName();
		return manager.getPropertyValue(language, field);
		//return $("guide." + getClass().getSimpleName());
	}*/

}
