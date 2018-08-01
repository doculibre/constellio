package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.builders.SearchBoostToVOBuilder;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
		List<SearchBoostVO> searchBoostVOs = new ArrayList<>();
		for (Metadata metadata : getSearcheableMetadatas()) {
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

	protected MetadataList getSearcheableMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataList list = new MetadataList();
		for (MetadataSchemaType metadataSchemaType : schemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			for (MetadataSchema metadataSchema : metadataSchemaType.getAllSchemas()) {
				MetadataList metadataList = metadataSchema.getMetadatas().onlySearchable();
				for (Metadata metadata : metadataList) {
					if (!list.containsMetadataWithLocalCode(metadata.getLocalCode())) {
						list.add(metadata);
					}
				}
			}
		}
		return list;
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
