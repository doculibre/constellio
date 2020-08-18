package com.constellio.app.services.action;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.sequence.SequenceServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.records.RecordServices;

public class SchemaRecordActionsServices {
	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;
	private transient ModelLayerCollectionExtensions modelLayerCollectionextensions;
	private transient AppLayerCollectionExtensions appLayerCollectionExtensions;
	private RecordServices recordServices;

	public SchemaRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		this.modelLayerCollectionextensions = appLayerFactory.getModelLayerFactory().getExtensions().forCollection(collection);
		this.appLayerCollectionExtensions = appLayerFactory.getExtensions().forCollection(collection);

		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	public boolean isEditActionPossible(Record record, User user) {
		return appLayerCollectionExtensions.isEditActionPossibleOnSchemaRecord(record, user) && !modelLayerCollectionextensions.isModifyBlocked(record, user)
			   && modelLayerCollectionextensions.isRecordModifiableBy(record, user);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		return appLayerCollectionExtensions.isDeleteActionPossibleOnSchemaRecord(record, user)
			   && modelLayerCollectionextensions.validateDeleteAuthorized(record, user).isEmpty() && recordServices.validateLogicallyDeletable(record, user).isEmpty();
	}

	public boolean isSequencesActionPossible(Record record, User user, BaseView view) {
		SessionContext sessionContext = view.getSessionContext();
		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SequenceServices sequenceServices = new SequenceServices(constellioFactories, sessionContext);

		return appLayerCollectionExtensions.isSequencesActionPossibleOnSchemaRecord(record, user) &&
			   !sequenceServices.getAvailableSequences(record.getId()).isEmpty();
	}
}
