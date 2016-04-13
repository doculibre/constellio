package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.builders.SearchBoostToVOBuilder;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataListFilter;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public class SearchBoostByMetadataPresenter extends SearchBoostPresenter {

	private final String TYPE = "metadata";

	public SearchBoostByMetadataPresenter(SearchBoostView view) {
		super(view);
	}

	public SearchBoostDataProvider newDataProvider() {
		return new SearchBoostDataProvider(TYPE, collection, new SearchBoostToVOBuilder(), modelLayerFactory);
	}

	boolean validate(SearchBoostVO searchBoostVO, String value) {
		if (searchBoostVO == null) {
			showErrorMessageView($("SearchBoostByMetadataView.invalidMetadata"));
			return false;
		}
		try {
			Double.valueOf(value);
		} catch (NumberFormatException e) {
			showErrorMessageView($("SearchBoostByQueryView.invalidValue"));
			return false;
		}
		return true;
	}

	@Override
	String getSearchBoostType() {
		return TYPE;
	}

	public List<SearchBoostVO> getMetadatasSearchBoostVO() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataListFilter filterSearchable = new MetadataListFilter() {
			@Override
			public boolean isReturned(Metadata metadata) {
				return metadata.isSearchable();
			}
		};

		MetadataList list = schemasManager.getSchemaTypes(collection).getAllMetadatas().only(filterSearchable);

		List<SearchBoostVO> searchBoostVOs = new ArrayList<>();
		for (Metadata metadata : list) {
			SearchBoostVO searchBoostVO = new SearchBoostVO();
			searchBoostVO.setType(TYPE);
			searchBoostVO
					.setLabel(metadata.getLabel(Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage())));
			String analyzedField = metadata.getAnalyzedField(view.getSessionContext().getCurrentLocale().getLanguage())
					.getDataStoreCode();
			searchBoostVO.setKey(analyzedField);
			searchBoostVOs.add(searchBoostVO);
		}
		sort(searchBoostVOs);
		return searchBoostVOs;
	}

	private void sort(List<SearchBoostVO> searchBoostVOs) {
		Collections.sort(searchBoostVOs, new Comparator<SearchBoostVO>() {
			@Override
			public int compare(SearchBoostVO o1, SearchBoostVO o2) {
				return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
			}
		});
	}
}
