package com.constellio.app.modules.rm.ui.components;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentLink;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class DocumentSearchResultDisplay extends SearchResultDisplay {

	public DocumentSearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		super(searchResultVO, componentFactory);
	}

	@Override
	protected Component newTitleComponent(SearchResultVO searchResultVO) {
		final RecordVO record = searchResultVO.getRecordVO();

		String schemaCode = record.getSchema().getCode();
		Component titleComponent;
		if (ConstellioAgentUtils.isAgentSupported() && new SchemaUtils().getSchemaTypeCode(schemaCode)
				.equals(Document.SCHEMA_TYPE)) {
			ContentVersionVO contentVersionVO = record.get(Document.CONTENT);
			String agentURL = ConstellioAgentUtils.getAgentURL(record, contentVersionVO);
			if (agentURL != null) {
				titleComponent = new ConstellioAgentLink(agentURL, contentVersionVO, record.getTitle(), false);
			} else {
				titleComponent = super.newTitleComponent(searchResultVO);
			}
		} else {
			titleComponent = super.newTitleComponent(searchResultVO);
		}

		Button edit = new EditButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				ConstellioUI.getCurrent().navigate().to(RMViews.class).editDocument(record.getId());
			}
		};

		Button download = new IconButton(new ThemeResource("images/icons/actions/save.png"),
				$("DisplayFolderView.download")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				ContentVersionVO version = record.get(Document.CONTENT);
				ContentVersionVOResource resource = new ContentVersionVOResource(version);
				Resource downloadedResource = DownloadLink.wrapForDownload(resource);
				Page.getCurrent().open(downloadedResource, null, false);
			}
		};
		download.setEnabled(record.get(Document.CONTENT) != null);

		Button open = new DisplayButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				ConstellioUI.getCurrent().navigate().to(RMViews.class).displayDocument(record.getId());
			}
		};

		HorizontalLayout layout = new HorizontalLayout(titleComponent, edit, download, open);
		layout.setExpandRatio(titleComponent, 1);
		layout.setComponentAlignment(edit, Alignment.TOP_RIGHT);
		layout.setComponentAlignment(download, Alignment.TOP_RIGHT);
		layout.setComponentAlignment(open, Alignment.TOP_RIGHT);
		layout.setWidth("100%");

		return layout;
	}
}
