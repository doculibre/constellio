package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;

import java.util.List;

public abstract class SingleSchemaBasePresenter<T extends BaseView> extends BasePresenter<T> {

	private SchemaPresenterUtils schemaPresenterUtils;

	public SingleSchemaBasePresenter(T view) {
		this(view, "");
	}

	public SingleSchemaBasePresenter(T view, String schemaCode) {
		this(view, schemaCode, view.getConstellioFactories(), view.getSessionContext());
	}

	public SingleSchemaBasePresenter(T view, String schemaCode, ConstellioFactories constellioFactories,
									 SessionContext sessionContext) {
		super(view, constellioFactories, sessionContext);
		this.schemaPresenterUtils = new SchemaPresenterUtils(schemaCode, constellioFactories, sessionContext);
	}

	public final String getSchemaCode() {
		return schemaPresenterUtils.schemaCode;
	}

	public final String getLabel() {
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		return schema().getLabel(language);
	}

	protected boolean isEnabledInAtLeastOneSchema(Metadata metadata, MetadataSchemaType schemaType) {
		if(metadata.isEnabled()) {
			return true;
		} else {
			List<MetadataSchema> allSchemas = schemaType.getAllSchemas();
			for(MetadataSchema schema: allSchemas) {
				if(schema.hasMetadataWithCode(metadata.getLocalCode()) && schema.getMetadata(metadata.getLocalCode()).isEnabled()) {
					return true;
				}
			}
		}
		return false;
	}

	public final void setSchemaCode(String schemaCode) {
		this.schemaPresenterUtils.setSchemaCode(schemaCode);
	}

	protected Record newRecord() {
		return schemaPresenterUtils.newRecord();
	}

	protected Record getRecord(String id) {
		try {
			return schemaPresenterUtils.getRecord(id);
		} catch (Exception e) {
			recordServices().flush();
			return schemaPresenterUtils.getRecord(id);
		}
	}

	public final Metadata getMetadata(String code) {
		return schemaPresenterUtils.getMetadata(code);
	}

	protected List<BatchProcess> addOrUpdate(Record record, RecordsFlushing recordsFlushing) {
		return schemaPresenterUtils.addOrUpdate(record, getCurrentUser(), recordsFlushing);
	}

	protected List<BatchProcess> addOrUpdate(Record record) {
		return schemaPresenterUtils.addOrUpdate(record);
	}

	protected List<BatchProcess> addOrUpdate(Record record, RecordUpdateOptions updateOptions) {
		return schemaPresenterUtils.addOrUpdate(record, updateOptions);
	}

	protected List<BatchProcess> addOrUpdateWithoutUser(Record record) {
		return schemaPresenterUtils.addOrUpdateWithoutUser(record);
	}

	protected final void delete(Record record) {
		delete(record, null, true);
	}

	protected final void delete(Record record, String reason) {
		delete(record, reason, true);
	}

	protected final void delete(Record record, boolean physically) {
		delete(record, null, physically);
	}

	protected final void delete(Record record, String reason, boolean physically) {
		delete(record, reason, physically, false);
	}

	protected final void delete(Record record, String reason, boolean physically, boolean throwException) {
		try {
			schemaPresenterUtils.delete(record, reason, physically);
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
			if (throwException) {
				throw exception;
			}
		}
	}

	protected final boolean delete(Record record, String reason, boolean physically, int waitSeconds) {
		boolean isDeletetionSuccessful = false;
		try {
			schemaPresenterUtils.delete(record, reason, physically, waitSeconds);
			isDeletetionSuccessful = true;
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
		} catch (RecordDeleteServicesRuntimeException exception) {
			view.showErrorMessage(i18n.$("deletionFailed") + "\n" + MessageUtils.toMessage(exception));
		}

		return isDeletetionSuccessful;
	}

	protected Record toRecord(RecordVO recordVO) {
		return schemaPresenterUtils.toRecord(recordVO);
	}

	protected Record toRecord(RecordVO recordVO, boolean newMinorEmpty) {
		return schemaPresenterUtils.toRecord(recordVO, newMinorEmpty);
	}

	protected Record toNewRecord(RecordVO recordVO) {
		return schemaPresenterUtils.toNewRecord(recordVO);
	}

	protected Record toNewRecord(RecordVO recordVO, boolean newMinorEmpty) {
		return schemaPresenterUtils.toNewRecord(recordVO, newMinorEmpty);
	}

	protected Content toContent(RecordVO recordVO, MetadataVO metadataVO, ContentVersionVO contentVersionVO) {
		return schemaPresenterUtils.toContent(recordVO, metadataVO, contentVersionVO);
	}

	protected Content toContent(RecordVO recordVO, MetadataVO metadataVO, ContentVersionVO contentVersionVO,
								boolean newMinorEmpty) {
		return schemaPresenterUtils.toContent(recordVO, metadataVO, contentVersionVO, newMinorEmpty);
	}

	@Deprecated
	//Use schema(schemaCode) instead
	protected MetadataSchema schema() {
		return schemaPresenterUtils.schema();
	}

	protected MetadataSchema schema(String schemaCode) {
		return schemaPresenterUtils.schema(schemaCode);
	}

	protected MetadataSchema defaultSchema() {
		return schemaPresenterUtils.defaultSchema();
	}

	protected boolean isSchemaExisting(String schemaCode) {
		return schemaPresenterUtils.hasSchema(schemaCode);
	}
}
