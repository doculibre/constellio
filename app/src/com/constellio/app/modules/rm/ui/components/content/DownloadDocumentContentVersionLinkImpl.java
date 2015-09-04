/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.components.content;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DownloadDocumentContentVersionLinkImpl extends WindowButton implements DownloadDocumentContentVersionLink {
	
	private RecordVO recordVO;
	
	private ContentVersionVO contentVersionVO;
	
	private VerticalLayout windowLayout;
	
	private String readOnlyMessage;
	
	private Label readOnlyLabel;

	private DownloadContentVersionLink downloadLink;
	
	private Button checkOutLink;
	
	private boolean checkOutLinkVisible;
	
	private DownloadDocumentContentVersionPresenter presenter;

	public DownloadDocumentContentVersionLinkImpl(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption) {
		super(caption, $("DownloadDocumentContentVersionLink.windowTitle"));
		addStyleName(ValoTheme.BUTTON_LINK);
		
		this.recordVO = recordVO;
		this.contentVersionVO = contentVersionVO;
		
		this.presenter = new DownloadDocumentContentVersionPresenter(this);
	}

	@Override
	protected Component buildWindowContent() {
		windowLayout = new VerticalLayout();
		windowLayout.setSpacing(true);
		windowLayout.setWidth("90%");
		
		readOnlyLabel = new Label(readOnlyMessage);
		readOnlyLabel.addStyleName(ValoTheme.LABEL_H2);
		readOnlyLabel.setVisible(readOnlyMessage != null);
		
		downloadLink = new DownloadContentVersionLink(contentVersionVO, $("DownloadDocumentContentVersionLink.downloadLinkCaption"));
		
		checkOutLink = new Button($("DownloadDocumentContentVersionLink.checkOutLinkCaption"), new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.checkOutLinkClicked();
			}
		});
		checkOutLink.addStyleName(ValoTheme.BUTTON_LINK);
		checkOutLink.setVisible(checkOutLinkVisible);
		
		windowLayout.addComponents(readOnlyLabel, downloadLink, checkOutLink);
		
		return windowLayout;
	}

	@Override
	public RecordVO getRecordVO() {
		return recordVO;
	}

	@Override
	public ContentVersionVO getContentVersionVO() {
		return contentVersionVO;
	}

	@Override
	public void setReadOnlyMessage(String message) {
		this.readOnlyMessage = message;
	}

	@Override
	public void setCheckOutLinkVisible(boolean visible) {
		this.checkOutLinkVisible = visible;
	}

	@Override
	public void closeWindow() {
		getWindow().close();
	}

	@Override
	public void open(String url) {
		Page.getCurrent().open(url, "_self");
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioUI.getCurrent().getConstellioFactories();
	}

	@Override
	public ConstellioNavigator navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

}
