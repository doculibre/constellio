package com.constellio.app.modules.rm.ui.pages.containers.edit;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataList;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditContainerPresenter extends SingleSchemaBasePresenter<AddEditContainerView> {
	protected RecordVO container;
	protected boolean editMode;
	protected boolean multipleMode;
	protected int numberOfContainer = 1;

	public static final String STYLE_NAME = "window-button";
	public static final String WINDOW_STYLE_NAME = STYLE_NAME + "-window";
	public static final String WINDOW_CONTENT_STYLE_NAME = WINDOW_STYLE_NAME + "-content";

	public AddEditContainerPresenter(AddEditContainerView view) {
		super(view, ContainerRecord.DEFAULT_SCHEMA);
	}

	public AddEditContainerPresenter forParams(String parameters) {
		StringUtils.countMatches(parameters, "/");
		editMode = StringUtils.isNotBlank(parameters) && StringUtils.countMatches(parameters, "/") == 0;
		multipleMode = StringUtils.countMatches(parameters, "/") > 0;
		Record container = editMode ? getRecord(parameters) : newContainerRecord();
		this.container = new RecordToVOBuilder().build(container, VIEW_MODE.FORM, view.getSessionContext());
		return this;
	}

	public RecordVO getContainerRecord() {
		return container;
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
		return !editMode;
	}

	public boolean canEditDecommissioningType() {
		return !editMode;
	}

	public void saveButtonClicked(RecordVO record) {
		if (multipleMode) {
			if (numberOfContainer < 1) {
				view.showErrorMessage($("AddEditContainerView.invalidNumberOfContainer"));
				return;
			}
			try {
				createMultipleContainer(toRecord(record), numberOfContainer);
				view.navigate().to(RMViews.class).archiveManagement();
			} catch (RecordServicesException.ValidationException e) {
				view.showMessage($(e.getErrors()));

			} catch (RecordServicesException e) {
				view.showMessage($(e));
			}
		} else {
			addOrUpdate(toRecord(record));
			view.navigate().to(RMViews.class).displayContainer(record.getId());
		}

	}

	public void cancelRequested() {
		view.navigate().to().previousView();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		DecommissioningSecurityService securityServices = new DecommissioningSecurityService(collection, appLayerFactory);
		if(StringUtils.countMatches(params, "/") > 0) {
			return securityServices.canCreateContainers(user) && isSequenceActivated();
		} else {
			return securityServices.canCreateContainers(user);
		}
	}

	protected Record newContainerRecord() {
		return recordServices().newRecordWithSchema(schema(ContainerRecord.DEFAULT_SCHEMA));
	}

	private RecordVO copyMetadataToSchema(RecordVO record, String schemaCode) {
		MetadataSchema schema = schema(schemaCode);
		Record container = recordServices().newRecordWithSchema(schema, record.getId());
		for (MetadataVO metadataVO : record.getMetadatas()) {
			String localCode = metadataVO.getLocalCode();
			try {
				Metadata metadata = schema.getMetadata(localCode);
				if (metadata.getDataEntry().getType() == DataEntryType.MANUAL && !metadata.isSystemReserved()) {
					container.set(metadata, record.get(metadataVO));
				}
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				e.printStackTrace();
			}
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
			Record container = newContainerRecord();
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

	public boolean isSequenceActivated() {
		return DataEntryType.SEQUENCE.equals(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.IDENTIFIER).getDataEntry().getType());
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
}
