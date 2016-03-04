package com.constellio.app.modules.rm.ui.components.content;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class DocumentContentVersionWindowImpl extends VerticalLayout implements DocumentContentVersionWindow {

	private RecordVO recordVO;

	private ContentVersionVO contentVersionVO;

	private String readOnlyMessage;

	private String agentURL;

	private Label readOnlyLabel;

	private Button displayDocumentLink;

	private Component openOrDownloadLink;

	private Button checkOutLink;

	private boolean checkOutLinkVisible;

	private DocumentContentVersionPresenter presenter;

	public DocumentContentVersionWindowImpl(RecordVO recordVO, ContentVersionVO contentVersionVO) {
		this.recordVO = recordVO;
		this.contentVersionVO = contentVersionVO;

		this.presenter = new DocumentContentVersionPresenter(this);

		setSpacing(true);
		setWidth("90%");
		addStyleName("document-window-content");

		readOnlyLabel = new Label(readOnlyMessage);
		readOnlyLabel.addStyleName(ValoTheme.LABEL_H2);
		readOnlyLabel.setVisible(readOnlyMessage != null);

		displayDocumentLink = new Button($("DocumentContentVersionWindow.displayDocumentLinkCaption"), new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.displayDocumentLinkClicked();
			}
		});
		displayDocumentLink.addStyleName(ValoTheme.BUTTON_LINK);

		if (agentURL != null) {
			Resource icon = FileIconUtils.getIcon(recordVO);
			openOrDownloadLink = new Button($("DocumentContentVersionWindow.openLinkCaption"), new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.openWithAgentLinkClicked();
				}
			});
			openOrDownloadLink.setIcon(icon);
			openOrDownloadLink.addStyleName(ValoTheme.BUTTON_LINK);
		} else {
			openOrDownloadLink = new DownloadContentVersionLink(contentVersionVO, $("DocumentContentVersionWindow.downloadLinkCaption"));
		}

		checkOutLink = new Button($("DocumentContentVersionWindow.checkOutLinkCaption"), new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.checkOutLinkClicked();
			}
		});
		checkOutLink.addStyleName(ValoTheme.BUTTON_LINK);
		checkOutLink.setVisible(checkOutLinkVisible);

		addComponents(readOnlyLabel, displayDocumentLink, openOrDownloadLink, checkOutLink);
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
	public void setAgentURL(String agentURL) {
		this.agentURL = agentURL;
	}

	@Override
	public void closeWindow() {
		Window window = (Window) getParent();
		window.close();
	}

	@Override
	public void open(String url) {
		Page.getCurrent().open(url, "_top");
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
	public CoreViews navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

}
