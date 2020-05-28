package com.constellio.app.modules.rm.services.actions;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;

public class StorageSpaceRecordActionsServices {

	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private String collection;
	private SearchServices searchServices;
	private RecordServices recordServices;

	public StorageSpaceRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		this.searchServices = modelLayerFactory.newSearchServices();
	}

	public boolean isGenerateReportActionPossible(Record record, User user) {
		return user.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally() &&
			   user.hasWriteAccess().on(record) &&
			   !record.isLogicallyDeleted() &&
			   rmModuleExtensions.isGenerateReportActionPossibleOnStorageSpace(rm.wrapStorageSpace(record), user);
	}

	public boolean isDisplayActionPossible(Record record, User user) {
		return user.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally() &&
			   user.hasReadAccess().on(record)
			   && rmModuleExtensions.isConsultActionPossibleOnStorageSpace(rm.wrapStorageSpace(record), user);
	}

	public boolean isEditActionPossible(Record record, User user) {
		return user.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally() &&
			   user.hasWriteAccess().on(record) &&
			   rmModuleExtensions.isEditActionPossibleOnStorageSpace(rm.wrapStorageSpace(record), user);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		return user.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally() &&
			   user.hasWriteAndDeleteAccess().on(record) &&
			   rmModuleExtensions.isDeleteActionPossibleOnStorageSpace(rm.wrapStorageSpace(record), user);
	}

	public boolean isConsultLinkActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record)
			   && rmModuleExtensions.isConsultLinkActionPossibleOnStorageSpace(rm.wrapStorageSpace(record), user);
	}
}
