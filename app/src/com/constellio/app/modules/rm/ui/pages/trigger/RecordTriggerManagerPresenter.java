package com.constellio.app.modules.rm.ui.pages.trigger;


import com.constellio.app.modules.rm.data.TriggerDataProvider;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter.TriggerManagerBreadcrumbItem;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderPresenter;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RecordTriggerManagerPresenter extends BasePresenter<RecordTriggerManagerView> {
	private Record targetedRecord;
	private RecordServices recordServices;
	private MetadataSchemasManager metadataSchemasManager;
	private Map<String, String> params;

	public RecordTriggerManagerPresenter(RecordTriggerManagerView view) {
		super(view);

		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	public void forParams(String parameters) {
		if (StringUtils.isNotBlank(parameters)) {
			this.params = ParamUtils.getParamsMap(parameters);

			String folderId = this.params.get("id");
			if (StringUtils.isBlank(folderId)) {
				throw new IllegalStateException("this page require a record");
			}

			targetedRecord = recordServices.getRecordSummaryById(view.getCollection(), folderId);
			String schemaLabel = metadataSchemasManager.getSchemaOf(targetedRecord).getSchemaType().getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale()));
			view.setRecordTitle(schemaLabel.toLowerCase() + " " + targetedRecord.getTitle());

		} else {
			throw new IllegalStateException("this page require parameters");
		}
	}

	public Map<String, String> getParams() {
		return params;
	}

	public Record getTargetedRecord() {
		return targetedRecord;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasWriteAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return Arrays.asList(targetedRecord.getId());
	}

	public RecordVODataProvider getDataProvider() {
		return new TriggerDataProvider(targetedRecord.getId(), appLayerFactory, view.getSessionContext());
	}

	public void addRecordTriggerClicked() {
		view.navigate().to(RMViews.class).addEditTriggerToRecord(params);
	}

	public BaseBreadcrumbTrail getBuildBreadcrumbTrail() {
		BaseBreadcrumbTrail baseBreadcrumbTrail = DisplayFolderPresenter.getBreadCrumbTrail(params, view, targetedRecord.getId(), null, true);
		baseBreadcrumbTrail.addItem(new TriggerManagerBreadcrumbItem(getParams(), view.getTitle()) {
			@Override
			public boolean isEnabled() {
				return false;
			}
		});


		return baseBreadcrumbTrail;
	}
}
