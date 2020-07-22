package com.constellio.app.services.menu.behavior;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.ListSequencesButton;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import static com.constellio.app.ui.i18n.i18n.$;

public class SchemaRecordMenuItemActionBehaviors {

	private AppLayerFactory appLayerFactory;
	private String collection;
	private RecordServices recordServices;

	public SchemaRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	public void edit(Record record, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editSchemaRecord(record.getSchemaCode(), record.getId());
	}

	public void delete(Record record, MenuItemActionBehaviorParams params) {
		if (tryDelete(record, params)) {
			String parentMetadataCode = HierarchicalValueListItem.PARENT;
			if (isHierarchical(record) && params.getRecordVO().get(parentMetadataCode) != null) {
				String parentRecordId = params.getRecordVO().get(parentMetadataCode);
				params.getView().navigate().to().displaySchemaRecord(parentRecordId);
			} else {
				params.getView().navigate().to().listSchemaRecords(record.getSchemaCode());
			}
		}
	}

	private boolean isHierarchical(Record record) {
		MetadataSchema schema = schema(record.getSchemaCode());
		return schema.hasMetadataWithCode(HierarchicalValueListItem.PARENT);
	}

	protected MetadataSchema schema(String schemaCode) {
		MetadataSchemasManager metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);
	}

	private boolean tryDelete(Record record, MenuItemActionBehaviorParams params) {
		boolean success;
		ValidationErrors validationErrors = recordServices.validateLogicallyDeletable(record, params.getUser());
		if (validationErrors.isEmpty()) {
			try {
				deleteRecord(record, null, false, params, true);
				success = true;
			} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
				success = false;
			}
		} else {
			success = false;
		}
		return success;
	}

	private final void deleteRecord(Record record, String reason, boolean physically,
									MenuItemActionBehaviorParams params, boolean throwException) {
		try {
			SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(record.getSchemaCode(),
					params.getView().getConstellioFactories(), params.getView().getSessionContext());
			presenterUtils.delete(record, reason, physically);
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			params.getView().showErrorMessage(MessageUtils.toMessage(exception));
			if (throwException) {
				throw exception;
			}
		}
	}

	public void sequences(Record record, MenuItemActionBehaviorParams params) {
		new ListSequencesButton(record.getId(), $("DisplaySchemaRecordView.sequences")).click();
	}
}
