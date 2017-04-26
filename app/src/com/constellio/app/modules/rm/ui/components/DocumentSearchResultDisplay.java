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
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;

public class DocumentSearchResultDisplay extends SearchResultDisplay {

	public DocumentSearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory, AppLayerFactory appLayerFactory) {
		super(searchResultVO, componentFactory, appLayerFactory);
	}

	@Override
	protected Component newTitleComponent(SearchResultVO searchResultVO) {
		final RecordVO record = searchResultVO.getRecordVO();

		String schemaCode = record.getSchema().getCode();
		Component titleComponent;
		if (ConstellioAgentUtils.isAgentSupported() && SchemaUtils.getSchemaTypeCode(schemaCode)
				.equals(Document.SCHEMA_TYPE)) {
			ContentVersionVO contentVersionVO = record.get(Document.CONTENT);
			String agentURL = ConstellioAgentUtils.getAgentURL(record, contentVersionVO);
			if (agentURL != null) {
				titleComponent = new ConstellioAgentLink(agentURL, record, contentVersionVO, record.getTitle(), false);
			} else {
				titleComponent = super.newTitleComponent(searchResultVO);
			}
		} else {
			titleComponent = super.newTitleComponent(searchResultVO);
		}
		
		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		MenuBar menuBar = new RMRecordMenuBarHandler(constellioFactories).get(record);

		HorizontalLayout layout = new HorizontalLayout(titleComponent, menuBar);
		layout.setExpandRatio(titleComponent, 1);
		layout.setComponentAlignment(menuBar, Alignment.TOP_RIGHT);
		layout.setComponentAlignment(titleComponent, Alignment.BOTTOM_LEFT);
		layout.setWidth("100%");
		layout.setHeight("100%");
		layout.addStyleName("document-search-result-display");

		return layout;
	}
}
