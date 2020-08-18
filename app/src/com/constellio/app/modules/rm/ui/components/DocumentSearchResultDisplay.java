package com.constellio.app.modules.rm.ui.components;

import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentLink;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.BaseUpdatableContentVersionPresenter;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.ui.Component;

public class DocumentSearchResultDisplay extends SearchResultDisplay {

	public DocumentSearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory,
									   AppLayerFactory appLayerFactory, String query, boolean noLinks) {
		super(searchResultVO, componentFactory, appLayerFactory, query, noLinks);
		addStyleName("document-search-result-display");
	}

	@Override
	protected Component newTitleLink(SearchResultVO searchResultVO) {
		Component titleLink;
		RecordVO recordVO = searchResultVO.getRecordVO();
		String schemaCode = recordVO.getSchema().getCode();
		if (ConstellioAgentUtils.isAgentSupported() && SchemaUtils.getSchemaTypeCode(schemaCode).equals(Document.SCHEMA_TYPE)) {
			ContentVersionVO contentVersionVO = recordVO.get(Document.CONTENT);
			String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
			if (agentURL != null) {
				titleLink = new ConstellioAgentLink(agentURL, recordVO, contentVersionVO, recordVO.getTitle(), false, new BaseUpdatableContentVersionPresenter(getAppLayerFactory()), Document.CONTENT);
				addVisitedStyleNameIfNecessary(titleLink, recordVO.getId());
				((ConstellioAgentLink) titleLink).addVisitedClickListener(recordVO.getId());
			} else {
				titleLink = super.newTitleLink(searchResultVO);
			}
		} else {
			titleLink = super.newTitleLink(searchResultVO);
		}
		titleLink.addStyleName("document-search-result-title");
		return titleLink;
	}
}
