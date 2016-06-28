package com.constellio.app.ui.pages.base;

import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;

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

	public final void setSchemaCode(String schemaCode) {
		this.schemaPresenterUtils.setSchemaCode(schemaCode);
	}

	protected Record newRecord() {
		return schemaPresenterUtils.newRecord();
	}

	protected Record getRecord(String id) {
		return schemaPresenterUtils.getRecord(id);
	}

	public final Metadata getMetadata(String code) {
		return schemaPresenterUtils.getMetadata(code);
	}

	protected List<BatchProcess> addOrUpdate(Record record) {
		return schemaPresenterUtils.addOrUpdate(record);
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
		try {
			schemaPresenterUtils.delete(record, reason, physically);
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
		}
	}

	protected Record toRecord(RecordVO recordVO) {
		return schemaPresenterUtils.toRecord(recordVO);
	}

	protected Record toRecord(RecordVO recordVO, boolean newMinorEmpty) {
		return schemaPresenterUtils.toRecord(recordVO, newMinorEmpty);
	}

	protected Content toContent(ContentVersionVO contentVersionVO) {
		return schemaPresenterUtils.toContent(contentVersionVO);
	}

	protected Content toContent(ContentVersionVO contentVersionVO, boolean newMinorEmpty) {
		return schemaPresenterUtils.toContent(contentVersionVO, newMinorEmpty);
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
}
