package com.constellio.app.ui.pages.trash;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.trash.TrashServices;
import com.vaadin.server.Page;
import com.vaadin.ui.Table;

public class TrashPresenter extends BasePresenter<TrashView> {
	Set<String> selectedRecords = new HashSet<>();

	private transient TrashServices trashServices;

	public TrashPresenter(TrashView trashView) {
		super(trashView);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_TRASH).globally();
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
		MetadataSchema currentDefaultSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getDefaultSchema(view.getSelectedType());
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(currentDefaultSchema, VIEW_MODE.TABLE, asList(Schemas.LOGICALLY_DELETED_ON.getLocalCode()),
						view.getSessionContext(), true);
		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return trashServices().getTrashRecordsQueryForType(view.getSelectedType(), getCurrentUser());
			}
		};
	}

	private TrashServices trashServices() {
		if (trashServices == null) {
			trashServices = new TrashServices(appLayerFactory.getModelLayerFactory(), collection);
		}
		return trashServices;
	}

	public void clearSelectedRecords() {
		this.selectedRecords.clear();
	}

	public SchemaTypeVODataProvider getSchemaTypes() {
		final List<String> codes = new ArrayList<>(
				trashServices().getTypesWithLogicallyDeletedRecords(collection, getCurrentUser()));
		return new SchemaTypeVODataProvider(new MetadataSchemaTypeToVOBuilder(), appLayerFactory, collection) {
			@Override
			protected boolean isAccepted(MetadataSchemaType type) {
				return codes.contains(type.getCode());
			}
		};
	}

	public List<String> restoreSelection() {
		if (StringUtils.isBlank(view.getSelectedType()) || this.selectedRecords.isEmpty()) {
			return asList();
		}
		List<String> returnSet = trashServices().restoreSelection(this.selectedRecords, getCurrentUser());
		this.selectedRecords.clear();
		return returnSet;
	}

	public Set<String> deleteSelection() {
		if (StringUtils.isBlank(view.getSelectedType()) || this.selectedRecords.isEmpty()) {
			return new HashSet<>();
		}
		Set<String> notDeleted = trashServices().deleteSelection(this.selectedRecords, getCurrentUser());
		this.selectedRecords.clear();
		return notDeleted;
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
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		List<RecordNavigationExtension> recordNavigationExtensions = extensions.recordNavigationExtensions.getExtensions();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(entity.getSchema().getCode());
		for (RecordNavigationExtension extension : recordNavigationExtensions) {
			if (extension.isViewForSchemaTypeCode(schemaTypeCode)) {
				NavigationParams navigationParams = new NavigationParams(ConstellioUI.getCurrent().navigate(), entity,
						schemaTypeCode, Page.getCurrent(),
						//FIXME
						new Table());
				extension.navigateToView(navigationParams);
			}
		}
		//FIXME
		view.navigate().to().displaySchemaRecord(entity.getId());
	}

	public String getRelatedRecordsMessage(RecordVO recordVO) {
		List<String> relatedRecordsIds = trashServices().getRelatedRecords(recordVO.getId(), getCurrentUser());
		if (relatedRecordsIds.isEmpty()) {
			return $("TrashView.noRelatedRecord");
		} else {
			return $("TrashView.relatedRecordIds") + "\n" + StringUtils.join(relatedRecordsIds, ",");
		}
	}
}
