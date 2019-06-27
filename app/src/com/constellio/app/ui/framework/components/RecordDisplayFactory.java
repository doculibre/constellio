package com.constellio.app.ui.framework.components;

import java.io.Serializable;
import java.util.Map;

import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.vaadin.ui.Button.ClickListener;

@SuppressWarnings("serial")
public class RecordDisplayFactory implements Serializable {
	private UserVO currentUser;
	private MetadataDisplayFactory componentFactory;
	private Map<String, String> extraParameters;

	public RecordDisplayFactory(UserVO currentUser, MetadataDisplayFactory componentFactory, Map<String,String> extraParameters) {
		this.currentUser = currentUser;
		this.componentFactory = componentFactory;
		this.extraParameters = extraParameters;
	}

	public RecordDisplayFactory(UserVO currentUser, Map<String,String> extraParameters) {
		this(currentUser, new MetadataDisplayFactory(),extraParameters);
	}

	public RecordDisplay build(RecordVO recordVO) {
		return new RecordDisplay(recordVO, componentFactory);
	}


	public SearchResultDisplay build(SearchResultVO searchResultVO, String query, ClickListener clickListener,
									 ClickListener elevationClickListener, ClickListener exclusionClickListener) {
		SearchResultDisplay result;
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		GetCustomResultDisplayParam param = new GetCustomResultDisplayParam(searchResultVO, componentFactory, query);

		SearchResultDisplay searchResultDisplay = appLayerFactory.getExtensions()
				.forCollection(currentUser.getSchema().getCollection()).getCustomResultDisplayFor(param);
		
		ConstellioEIMConfigs configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());

		if (searchResultDisplay == null) {
			result = new SearchResultDisplay(searchResultVO, componentFactory, appLayerFactory, query, extraParameters, configs.isNoLinksInSearchResults());
		} else {
			result = searchResultDisplay;
		}
		result.setExtraParam(extraParameters);

		if (clickListener != null) {
			result.addClickListener(clickListener);
		}
		if (elevationClickListener != null) {
			result.addElevationClickListener(elevationClickListener);
		}
		if (exclusionClickListener != null) {
			result.addExclusionClickListener(exclusionClickListener);
		}
		return result;
	}
}
