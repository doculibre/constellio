package com.constellio.app.ui.pages.search.savedSearch;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SavedSearchPresenter extends SingleSchemaBasePresenter<SavedSearchView> {

	public SavedSearchPresenter(SavedSearchView view) {
		super(view, SavedSearch.DEFAULT_SCHEMA);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public RecordVODataProvider getUserSearchesDataProvider() {
		return new RecordVODataProvider(savedSearchSchemaVO(), new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {

			@Override
			protected LogicalSearchQuery getQuery() {
				MetadataSchema schema = schema(SavedSearch.DEFAULT_SCHEMA);
				return new LogicalSearchQuery(from(schema)
						.where(schema.getMetadata(SavedSearch.USER)).isEqualTo(getCurrentUser())
						.andWhere(schema.getMetadata(SavedSearch.TEMPORARY)).isFalseOrNull())
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVODataProvider getPublicSearchesDataProvider() {
		return new RecordVODataProvider(savedSearchSchemaVO(), new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {

			@Override
			protected LogicalSearchQuery getQuery() {
				MetadataSchema schema = schema(SavedSearch.DEFAULT_SCHEMA);
				return new LogicalSearchQuery(from(schema)
						.where(schema.getMetadata(SavedSearch.PUBLIC)).isTrue()
						.andWhere(schema.getMetadata(SavedSearch.USER)).isNotEqual(getCurrentUser())
						.andWhere(schema.getMetadata(SavedSearch.TEMPORARY)).isFalseOrNull())
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	private MetadataSchemaVO savedSearchSchemaVO() {
		return new MetadataSchemaToVOBuilder()
				.build(schema(SavedSearch.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
	}

	public void searchModificationRequested(RecordVO recordVO) {
		SavedSearch savedSearch = new SavedSearch(getRecord(recordVO.getId()), types());
		savedSearch.setTitle(recordVO.getTitle());
		savedSearch.setPublic((boolean) recordVO.get(SavedSearch.PUBLIC));
		addOrUpdate(savedSearch.getWrappedRecord());
		view.navigateTo().listSavedSearches();
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		delete(getRecord(recordVO.getId()));
		view.navigateTo().listSavedSearches();
	}

	public void searchButtonClicked(RecordVO recordVO) {
		switch (recordVO.<String>get(SavedSearch.SEARCH_TYPE)) {
		case SimpleSearchView.SEARCH_TYPE:
			view.navigateTo().simpleSearchReplay(recordVO.getId());
			break;
		case AdvancedSearchView.SEARCH_TYPE:
			view.navigateTo().advancedSearchReplay(recordVO.getId());
			break;
		}
	}
}
