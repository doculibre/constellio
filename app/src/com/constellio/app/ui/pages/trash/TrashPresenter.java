package com.constellio.app.ui.pages.trash;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.trash.TrashServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Property;

public class TrashPresenter extends BasePresenter<TrashView> {
	Set<String> selectedRecords = new HashSet<>();

	private transient TrashServices trashServices;

	public TrashPresenter(TrashView trashView) {
		super(trashView);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public boolean isRecordSelected(RecordVO recordVO) {
		return selectedRecords.contains(recordVO.getId());
	}

	public void recordToggled(RecordVO record) {
		String recordId = record.getId();
		if (selectedRecords.contains(recordId)) {
			selectedRecords.remove(recordId);
		} else {
			selectedRecords.add(recordId);
		}
		view.enableOrDisableActionButtons();
	}

	public boolean atLeastOneRecordSelected() {
		return selectedRecords.size() != 0;
	}

	public RecordVODataProvider getTrashRecords() {
		MetadataSchema currentDefaultSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getDefaultSchema(view.getSelectedType());
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(currentDefaultSchema, VIEW_MODE.TABLE, asList(Schemas.LOGICALLY_DELETED_ON.getLocalCode()), view.getSessionContext());
		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return trashServices().getTrashRecordsQueryForType(view.getSelectedType(), getCurrentUser());
			}
		};
	}

	private TrashServices trashServices() {
		if(trashServices == null){
			trashServices = new TrashServices(appLayerFactory, collection);
		}
		return trashServices;
	}

	public void clearSelectedRecords() {
		this.selectedRecords.clear();
	}

	public SchemaTypeVODataProvider getSchemaTypes() {
		return new SchemaTypeVODataProvider(new MetadataSchemaTypeToVOBuilder(), appLayerFactory, collection);
	}

	public void deleteAll() {
		trashServices().deleteAll(view.getSelectedType(), getCurrentUser());
	}

	public void restoreSelection() {
		if(StringUtils.isBlank(view.getSelectedType()) || this.selectedRecords.isEmpty()){
			return;
		}
		trashServices().restoreSelection(this.selectedRecords, getCurrentUser());
	}

	public void deleteSelection() {
		if(StringUtils.isBlank(view.getSelectedType()) || this.selectedRecords.isEmpty()){
			return;
		}
		trashServices().deleteSelection(this.selectedRecords, getCurrentUser());
	}

	public long getLogicallyDeletedRecordsCount() {
		return searchServices().getResultsCount(trashServices().getTrashRecordsQueryForCollection(collection, getCurrentUser()));
	}

	public Boolean physicalDeleteFailed(RecordVO recordVO) {
		Record record = recordServices().getDocumentById(recordVO.getId());
		Boolean errorOnPhysicalDelete = record.get(Schemas.ERROR_ON_PHYSICAL_DELETION);
		return errorOnPhysicalDelete != null && errorOnPhysicalDelete;
	}

	public void displayButtonClicked(RecordVO entity) {
		//TODO
	}

	public String getRelatedRecordsMessage(RecordVO recordVO) {
		//TODO
		return new String("lol");
	}
}
