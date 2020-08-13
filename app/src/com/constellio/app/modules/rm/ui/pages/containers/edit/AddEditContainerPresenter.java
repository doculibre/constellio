package com.constellio.app.modules.rm.ui.pages.containers.edit;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.jgoodies.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AddEditContainerPresenter extends SingleSchemaBasePresenter<AddEditContainerView> {

	private static Logger LOGGER = LoggerFactory.getLogger(AddEditContainerPresenter.class);

	protected RecordVO container;
	protected boolean editMode;
	protected boolean multipleMode;
	protected int numberOfContainer = 1;

	private String tabName;
	private String administrativeUnitId;
	private Map<String, String> params;

	public static final String STYLE_NAME = "window-button";
	public static final String WINDOW_STYLE_NAME = STYLE_NAME + "-window";
	public static final String WINDOW_CONTENT_STYLE_NAME = WINDOW_STYLE_NAME + "-content";

	private transient RMSchemasRecordsServices rmRecordServices;
	private transient DecommissioningService decommissioningService;

	public AddEditContainerPresenter(AddEditContainerView view) {
		super(view, ContainerRecord.DEFAULT_SCHEMA);
	}

	public AddEditContainerPresenter forParams(String parameters) {
		editMode = StringUtils.isNotBlank(parameters) && StringUtils.countMatches(parameters, "/") == 0;

		String recordId = parameters;
		if(parameters.contains(RMViews.FAV_GROUP_ID_KEY)) {
			this.params = ParamUtils.getParamsMap(parameters);
			recordId = params.get(RMViews.ID_KEY);
		} else if(parameters.startsWith("edit")) {
			editMode = true;
			String[] parts = parameters.split("/");
			recordId = parts[1];
			tabName = parts[2];
			administrativeUnitId = parts[3];
		}

		multipleMode = parameters.equals("m/t");
		Record container = editMode ? getRecord(recordId) : newContainerRecord();
		setSchemaCode(container.getSchemaCode());
		this.container = new RecordToVOBuilder().build(container, VIEW_MODE.FORM, view.getSessionContext());
		return this;
	}

	public RecordVO getContainerRecord() {
		return container;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void typeSelected(String type) {
		String newSchemaCode = getLinkedSchemaCodeOf(type);
		if (editMode) {
			view.setType(container.<String>get(ContainerRecord.TYPE));
			view.showErrorMessage($("AddEditContainerView.cannotChangeSchema"));
			return;
		}
		setSchemaCode(newSchemaCode);
		container = copyMetadataToSchema(view.getUpdatedContainer(), newSchemaCode);
		container.set(ContainerRecord.TYPE, type);
		view.reloadWithContainer(container);
	}

	public boolean canEditAdministrativeUnit() {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_CONTAINERS).onSomething();
	}

	public boolean canEditDecommissioningType() {
		return !editMode || container.get(ContainerRecord.DECOMMISSIONING_TYPE) == null || getCurrentUser().has(RMPermissionsTo.MANAGE_CONTAINERS).globally();
	}

	public void saveButtonClicked(RecordVO recordVO) {
		try {
			Record record = toRecord(recordVO);
			if (multipleMode) {
				if (numberOfContainer < 1 || numberOfContainer > 100) {
					view.showErrorMessage($("AddEditContainerView.invalidNumberOfContainer"));
					return;
				}
				try {
					createMultipleContainer(record, numberOfContainer);
					view.navigate().to(RMViews.class).archiveManagement();
				} catch (RecordServicesException.ValidationException e) {
					view.showMessage($(e.getErrors()));

				} catch (RecordServicesException e) {
					view.showMessage($(e));
				}
			} else {
				addOrUpdate(record);

				if (params != null && params.get(RMViews.FAV_GROUP_ID_KEY) != null) {
					view.navigate().to(RMViews.class).displayContainerFromFavorites(recordVO.getId(), params.get(RMViews.FAV_GROUP_ID_KEY));
				} else if (Strings.isNotBlank(tabName) && Strings.isNotBlank(administrativeUnitId)) {
					view.navigate().to(RMViews.class)
							.displayContainerFromContainerByAdministrativeUnit(recordVO.getId(), tabName, administrativeUnitId);
				} else {
					view.navigate().to(RMViews.class)
							.displayContainer(recordVO.getId());
				}
			}
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
	}

	public void cancelRequested() {
		view.navigate().to().previousView();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		DecommissioningSecurityService securityServices = new DecommissioningSecurityService(collection, appLayerFactory);
		if (StringUtils.countMatches(params, "/") > 0) {
			return securityServices.canCreateContainers(user) &&
				   (areContainersSequential() || Toggle.FORCE_MULTIPLE_CONTAINERS_VIEW_TO_DISPLAY.isEnabled());
		} else {
			return securityServices.canCreateContainers(user);
		}
	}

	protected Record newContainerRecord() {
		ContainerRecord containerRecord = rmRecordServices().newContainerRecord();

		User currentUser = getCurrentUser();
		SearchServices searchServices = searchServices();
		MetadataSchemaTypes types = types();
		MetadataSchemaType administrativeUnitSchemaType = types.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
		LogicalSearchQuery visibleAdministrativeUnitsQuery = new LogicalSearchQuery();
		visibleAdministrativeUnitsQuery.filteredWithUserWrite(currentUser);
		LogicalSearchCondition visibleAdministrativeUnitsCondition = from(administrativeUnitSchemaType).returnAll();
		visibleAdministrativeUnitsQuery.setCondition(visibleAdministrativeUnitsCondition);
		String defaultAdministrativeUnit = currentUser.get(RMUser.DEFAULT_ADMINISTRATIVE_UNIT);
		RMConfigs rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		if (rmConfigs.isFolderAdministrativeUnitEnteredAutomatically()) {
			if (StringUtils.isNotBlank(defaultAdministrativeUnit)) {
				try {
					Record defaultAdministrativeUnitRecord = recordServices().getDocumentById(defaultAdministrativeUnit);
					if (currentUser.has(RMPermissionsTo.MANAGE_CONTAINERS).on(defaultAdministrativeUnitRecord)) {
						containerRecord.setAdministrativeUnit(defaultAdministrativeUnitRecord);
					} else {
						LOGGER.error("User " + currentUser.getUsername()
									 + " has no longer write access to default administrative unit " + defaultAdministrativeUnit);
					}
				} catch (Exception e) {
					LOGGER.error("Default administrative unit for user " + currentUser.getUsername() + " is invalid: "
								 + defaultAdministrativeUnit);
				}
			} else {
				if (searchServices().getResultsCount(visibleAdministrativeUnitsQuery) > 0) {
					Record defaultAdministrativeUnitRecord = searchServices.search(visibleAdministrativeUnitsQuery).get(0);
					containerRecord.setAdministrativeUnit(defaultAdministrativeUnitRecord);
				}
			}
		}
		return containerRecord.getWrappedRecord();
	}

	private RecordVO copyMetadataToSchema(RecordVO record, String schemaCode) {
		MetadataSchema schema = schema(schemaCode);
		Record container = recordServices().newRecordWithSchema(schema, record.getId());
		boolean hasOverriddenAMetadata = false;
		for (MetadataVO metadataVO : record.getMetadatas()) {
			String localCode = metadataVO.getLocalCode();
			try {
				Metadata metadata = schema.getMetadata(localCode);
				if (metadata.getDataEntry().getType() == DataEntryType.MANUAL && !metadata.isSystemReserved()) {
					Object defaultValue = metadata.getDefaultValue();
					if (!(defaultValue == null ||
						  (defaultValue instanceof List && ((List) defaultValue).isEmpty()))) {
						container.set(metadata, defaultValue);
						hasOverriddenAMetadata = hasOverriddenAMetadata || record.get(metadataVO) != null;
					} else {
						container.set(metadata, record.get(metadataVO));
					}
				}
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				e.printStackTrace();
			}
		}
		if (hasOverriddenAMetadata) {
			view.showMessage($("AddEditContainerView.hasOverriddenAMetadata"));
		}
		return new RecordToVOBuilder().build(container, VIEW_MODE.FORM, view.getSessionContext());
	}

	private String getLinkedSchemaCodeOf(String id) {
		String linkedSchemaCode;
		ContainerRecordType type = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory).getContainerRecordType(id);
		if (type == null || StringUtils.isBlank(type.getLinkedSchema())) {
			linkedSchemaCode = ContainerRecord.DEFAULT_SCHEMA;
		} else {
			linkedSchemaCode = type.getLinkedSchema();
		}
		return linkedSchemaCode;
	}

	public boolean isAddView() {
		return !editMode;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public boolean isMultipleMode() {
		return multipleMode;
	}

	public void createMultipleContainer(Record record, Integer value)
			throws RecordServicesException {
		MetadataList modifiedMetadatas = record
				.getModifiedMetadatas(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
		Transaction transaction = new Transaction();
		transaction.add(record);

		for (int i = 0; i < value - 1; i++) {
			ContainerRecord container = rmRecordServices().newContainerRecord();
			container.changeSchemaTo(record.getSchemaCode());
			Iterator<Metadata> iterator = modifiedMetadatas.iterator();
			while (iterator.hasNext()) {
				Metadata metadata = iterator.next();
				container.set(metadata, record.get(metadata));
			}
			transaction.add(container);
		}
		recordServices().execute(transaction);

	}

	public void setNumberOfContainer(int i) {
		numberOfContainer = i;
	}

	private boolean areContainersSequential() {
		return isMetadataSequential(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.IDENTIFIER) ||
			   isMetadataSequential(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.TEMPORARY_IDENTIFIER);
	}

	private boolean isMetadataSequential(String metadataCode) {
		return DataEntryType.SEQUENCE.equals(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getMetadata(metadataCode).getDataEntry().getType());
	}

	public SessionContext getSessionContext() {
		return view.getSessionContext();
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public ConstellioFactories getConstellioFactories() {
		return view.getConstellioFactories();
	}

	public User getCurrentUser() {
		return presenterService().getCurrentUser(getSessionContext());
	}

	public boolean isContainerWithMultipleStorageSpaces() {
		return new RMConfigs(getModelLayerFactory().getSystemConfigurationsManager()).isContainerMultipleValue();
	}

	public void setStorageSpaceTo(String storageSpaceId) {
		container = view.getUpdatedContainer();
		view.reloadWithContainer(container);
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return rmRecordServices;
	}

	private DecommissioningService decommissioningService() {
		if (decommissioningService == null) {
			decommissioningService = new DecommissioningService(view.getCollection(), appLayerFactory);
		}
		return decommissioningService;
	}
}
