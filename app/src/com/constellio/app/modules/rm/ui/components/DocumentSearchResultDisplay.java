package com.constellio.app.modules.rm.ui.components;

import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentLink;
import com.constellio.app.modules.rm.ui.menuBar.RMRecordMenuBarHandler;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.BaseUpdatableContentVersionPresenter;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;

public class DocumentSearchResultDisplay extends SearchResultDisplay {

	public DocumentSearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory,
									   AppLayerFactory appLayerFactory, String query, boolean noLinks) {
		super(searchResultVO, componentFactory, appLayerFactory, query, noLinks);
	}

	@Override
	protected Component newTitleComponent(SearchResultVO searchResultVO) {
		RecordVO recordVO = searchResultVO.getRecordVO();

		Component titleComponent = super.newTitleComponent(searchResultVO);

		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		MenuBar menuBar = new RMRecordMenuBarHandler(constellioFactories).get(recordVO);

		I18NHorizontalLayout layout = new I18NHorizontalLayout(titleComponent, menuBar);

		layout.setExpandRatio(titleComponent, 1);

		layout.setComponentAlignment(menuBar, Alignment.TOP_RIGHT);
		layout.setComponentAlignment(titleComponent, Alignment.BOTTOM_LEFT);
		layout.setWidth("100%");
		layout.setHeight("100%");
		layout.addStyleName("document-search-result-display");

		return layout;
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
				titleLink = new ConstellioAgentLink(agentURL, recordVO, contentVersionVO, recordVO.getTitle(), false, new BaseUpdatableContentVersionPresenter(getAppLayerFactory()));
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
