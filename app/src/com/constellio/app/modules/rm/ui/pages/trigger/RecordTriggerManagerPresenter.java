package com.constellio.app.modules.rm.ui.pages.trigger;


import com.constellio.app.modules.rm.data.TriggerDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;

public class RecordTriggerManagerPresenter extends BasePresenter<RecordTriggerManagerView> {
	private Record currentRecord;
	private RecordServices recordServices;
	private MetadataSchemasManager metadataSchemasManager;

	public RecordTriggerManagerPresenter(RecordTriggerManagerView view) {
		super(view);

		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	public void forParams(String parameters) {
		if (StringUtils.isNotBlank(parameters)) {
			currentRecord = recordServices.getRecordSummaryById(view.getCollection(), parameters);
			String schemaLabel = metadataSchemasManager.getSchemaOf(currentRecord).getSchemaType().getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale()));
			view.setRecordTitle(schemaLabel.toLowerCase() + " " + currentRecord.getTitle());
		} else {
			throw new IllegalStateException("this page require a recordid");
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasWriteAccess().on(restrictedRecord);
	}

	public RecordVODataProvider getDataProvider() {
		return new TriggerDataProvider(currentRecord.getId(), appLayerFactory, view.getSessionContext());
	}
}
