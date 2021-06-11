package com.constellio.app.ui.pages.trash;

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
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.trash.TrashServices;
import com.constellio.model.services.trash.TrashServices.RecordsIdsAndTitles;
import com.vaadin.server.Page;
import com.vaadin.ui.Table;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class TrashPresenter extends BasePresenter<TrashView> {

	Boolean allItemsSelected = false;

	Boolean allItemsDeselected = false;

	Set<String> selectedRecordIds = new HashSet<>();

	private RecordVODataProvider dataProvider;

	private transient TrashServices trashServices;
	private List<String> fetchedRecordIds;

	public TrashPresenter(TrashView trashView) {
		super(trashView);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_TRASH).globally();
	}

	public boolean isRecordSelected(RecordVO recordVO) {
		return !allItemsDeselected && (allItemsSelected || selectedRecordIds.contains(recordVO.getId()));
	}

	public void recordSelectionChanged(RecordVO record, boolean selected) {
		boolean allItemsSelectedBefore = allItemsSelected;
		boolean allItemsDeselectedBefore = allItemsDeselected;
		allItemsSelected = false;
		allItemsDeselected = false;

		String recordId = record.getId();
		if (!selected && selectedRecordIds.contains(recordId)) {
			selectedRecordIds.remove(recordId);
			if (selectedRecordIds.isEmpty()) {
				allItemsDeselected = true;
			}
		} else if (!selected && !selectedRecordIds.contains(recordId)) {
			selectedRecordIds.addAll(fetchRecordIds());
			selectedRecordIds.remove(recordId);
			allItemsSelected = false;
		} else if (selected && !selectedRecordIds.contains(recordId)) {
			selectedRecordIds.add(recordId);
			if (CollectionUtils.isNotEmpty(fetchRecordIds()) && fetchRecordIds().size() == selectedRecordIds.size()) {
				allItemsSelected = true;
			}
		}

		if (allItemsSelectedBefore != allItemsSelected || allItemsDeselectedBefore != allItemsDeselected) {
			view.updateSelectDeselectAllToggle(allItemsSelected);
		}
		view.enableOrDisableActionButtons();
	}

	public boolean atLeastOneRecordSelected() {
		return !allItemsDeselected && (allItemsSelected || selectedRecordIds.size() != 0);
	}

	public RecordVODataProvider getTrashRecords() {
		MetadataSchema currentDefaultSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getDefaultSchema(view.getSelectedType());
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(currentDefaultSchema, VIEW_MODE.TABLE, asList(Schemas.LOGICALLY_DELETED_ON.getLocalCode()),
						view.getSessionContext(), true);
		return dataProvider = new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return TrashPresenter.this.getQuery();
			}
		};
	}

	protected LogicalSearchQuery getQuery() {
		LogicalSearchQuery trashRecordsQueryForType = trashServices().getTrashRecordsQueryForType(view.getSelectedType(), getCurrentUser());
		if (searchServices().getResultsCount(trashRecordsQueryForType) >= 1000) {
			trashRecordsQueryForType.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
		}
		return trashRecordsQueryForType;
	}

	private TrashServices trashServices() {
		if (trashServices == null) {
			trashServices = new TrashServices(appLayerFactory.getModelLayerFactory(), collection);
		}
		return trashServices;
	}

	public void clearSelectedRecords() {
		allItemsSelected = false;
		allItemsDeselected = false;
		this.selectedRecordIds.clear();
	}

	public SchemaTypeVODataProvider getSchemaTypes() {
		final List<String> codes = new ArrayList<>(
				trashServices().getTypesWithLogicallyDeletedRecords(collection, getCurrentUser()));
		return new SchemaTypeVODataProvider(new MetadataSchemaTypeToVOBuilder(), appLayerFactory, collection) {
			@Override
			protected boolean isAccepted(MetadataSchemaType type) {
				return codes.contains(type.getCode());
			}

			@Override
			protected boolean isExcludedIfNotConfigurable() {
				return false;
			}
		};
	}

	public Map<String, String> restoreSelection() {
		Set<String> selection = computeSelection();
		if (StringUtils.isBlank(view.getSelectedType()) || selection.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> returnMap = trashServices().restoreSelection(selection, getCurrentUser());
		this.selectedRecordIds.clear();
		return returnMap;
	}

	public Map<String, String> deleteSelection() {
		Set<String> selection = computeSelection();
		if (StringUtils.isBlank(view.getSelectedType()) || selection.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> notDeleted = trashServices().deleteSelection(selection, getCurrentUser());
		this.selectedRecordIds.clear();
		return notDeleted;
	}

	public long getLogicallyDeletedRecordsCount() {
		return trashServices().getLogicallyDeletedRecordsCount(collection, getCurrentUser());
	}

	public Boolean physicalDeleteFailed(RecordVO recordVO) {
		Record record = recordServices().getDocumentById(recordVO.getId());
		Boolean errorOnPhysicalDelete = record.get(Schemas.ERROR_ON_PHYSICAL_DELETION);
		return errorOnPhysicalDelete != null && errorOnPhysicalDelete;
	}

	public void displayButtonClicked(RecordVO entity) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		List<RecordNavigationExtension> recordNavigationExtensions = extensions.recordNavigationExtensions.getExtensions();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(entity.getSchema().getCode());
		for (RecordNavigationExtension extension : recordNavigationExtensions) {
			if (extension.isViewForSchemaTypeCode(schemaTypeCode)) {
				NavigationParams navigationParams = new NavigationParams(ConstellioUI.getCurrent().navigate(), entity,
						schemaTypeCode, Page.getCurrent(),
						//FIXME
						new Table());
				extension.navigateToView(navigationParams);
				break;
			}
		}
	}

	public String getRelatedRecordsMessage(RecordVO recordVO) {
		RecordsIdsAndTitles relatedRecordsIdsAndTitles = trashServices().getRelatedRecords(recordVO.getId(), getCurrentUser());
		Set<String> relatedRecordIds = relatedRecordsIdsAndTitles.getRecordsIds();
		Set<String> relatedRecordTitles = relatedRecordsIdsAndTitles.getRecordsTitles();
		if (relatedRecordIds.isEmpty()) {
			return $("TrashView.noRelatedRecord");
		} else {
			return $("TrashView.relatedRecordIds") + "<br>" + StringUtils.join(relatedRecordIds, "<br>")
				   + "<br>" + $("TrashView.relatedRecordTitles") + "<br>" + StringUtils.join(relatedRecordTitles, "<br>");
		}
	}

	Set<String> computeSelection() {
		Set<String> selection = new HashSet<>();
		if (allItemsSelected) {
			selection.addAll(fetchRecordIds());
		} else if (!allItemsDeselected) {
			selection.addAll(selectedRecordIds);
		}
		return selection;
	}

	List<String> fetchRecordIds() {
		if (CollectionUtils.isEmpty(fetchedRecordIds)) {
			LogicalSearchQuery query = getQuery();
			SearchServices searchServices = searchServices();
			fetchedRecordIds = searchServices.searchRecordIds(query);
		}

		return fetchedRecordIds;
	}

	boolean isAllItemsSelected() {
		return allItemsSelected;
	}

	boolean isAllItemsDeselected() {
		return allItemsDeselected;
	}

	void selectAllClicked() {
		allItemsSelected = true;
		allItemsDeselected = false;
		selectedRecordIds.clear();
		view.enableOrDisableActionButtons();
	}

	void deselectAllClicked() {
		allItemsSelected = false;
		allItemsDeselected = true;
		selectedRecordIds.clear();
		view.enableOrDisableActionButtons();
	}

	public void backButtonClicked() {
		view.navigate().to().previousView();
	}
}
