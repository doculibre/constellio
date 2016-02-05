package com.constellio.app.ui.framework.components;

import java.io.Serializable;

import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.entities.UserVO;

@SuppressWarnings("serial")
public class RecordDisplayFactory implements Serializable {
	private UserVO currentUser;
	private MetadataDisplayFactory componentFactory;

	public RecordDisplayFactory(UserVO currentUser, MetadataDisplayFactory componentFactory) {
		this.currentUser = currentUser;
		this.componentFactory = componentFactory;
	}

	public RecordDisplayFactory(UserVO currentUser) {
		this(currentUser, new MetadataDisplayFactory());
	}

	public RecordDisplay build(RecordVO recordVO) {
		return new RecordDisplay(recordVO, componentFactory);
	}

	public SearchResultDisplay build(SearchResultVO searchResultVO) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		GetCustomResultDisplayParam param = new GetCustomResultDisplayParam(searchResultVO, componentFactory);

		SearchResultDisplay searchResultDisplay = appLayerFactory.getExtensions()
				.forCollection(currentUser.getSchema().getCollection()).getCustomResultDisplayFor(param);

		if (searchResultDisplay == null) {
			return new SearchResultDisplay(searchResultVO, componentFactory);
		} else {
			return searchResultDisplay;
		}
	}
}
