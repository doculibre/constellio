package com.constellio.app.modules.rm.ui.components.content;

import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentWindow;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
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


	public DocumentContentVersionWindowImpl(RecordVO recordVO, ContentVersionVO contentVersionVO, Map<String,String> params) {
		this.recordVO = recordVO;
		this.contentVersionVO = contentVersionVO;
		this.presenter = new DocumentContentVersionPresenter(this, params);

		setWidth("100%");
		addStyleName("document-window-content");
		addStyleName("no-scroll-vertical");

		readOnlyLabel = new Label(readOnlyMessage, ContentMode.HTML);
		readOnlyLabel.addStyleName("read-only-document-warning");
		readOnlyLabel.setVisible(readOnlyMessage != null);

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
			openOrDownloadLink = new DownloadContentVersionLink(contentVersionVO,
					$("DocumentContentVersionWindow.downloadLinkCaption"), recordVO.getId(), Document.CONTENT,
					presenter.hasWritePermission());
		}

		checkOutLink = new Button($("DocumentContentVersionWindow.checkOutLinkCaption"), new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.checkOutLinkClicked();
			}
		});
		checkOutLink.addStyleName(ValoTheme.BUTTON_LINK);
		checkOutLink.setVisible(checkOutLinkVisible);

		if (presenter.isNavigationStateDocumentView()) {
			addComponents(readOnlyLabel, openOrDownloadLink, checkOutLink);
		} else {
			displayDocumentLink = new Button($("DocumentContentVersionWindow.displayDocumentLinkCaption"), new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.displayDocumentLinkClicked();
				}
			});
			displayDocumentLink.addStyleName(ValoTheme.BUTTON_LINK);
			addComponents(readOnlyLabel, displayDocumentLink, openOrDownloadLink, checkOutLink);
		}
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

	public Window openWindow() {
		Window warningWindow = new BaseWindow($("DocumentContentVersionWindow.windowTitle"), this);
		if (ResponsiveUtils.isPhone()) {
			warningWindow.setWidth("90%");
		} else {
			warningWindow.setWidth("700px");
		}
		warningWindow.center();
		warningWindow.setModal(true);
		UI.getCurrent().addWindow(warningWindow);
		return warningWindow;
	}

	@Override
	public void closeWindow() {
		Window window = (Window) getParent();
		window.close();
	}

	@Override
	public void open(String url) {
		StringBuilder js = new StringBuilder();
		js.append("setTimeout(function(){ window.location.href=\"" + url + "\"; }, 100)");
		JavaScript.getCurrent().execute(js.toString());
		//		Page.getCurrent().open(url, null);
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

	@Override
	public Navigation navigate() {
		return ConstellioUI.getCurrent().navigate();
	}

	@Override
	public void displayInWindow() {
		closeWindow();
		Window window;
		try {
			window = new DisplayDocumentWindow(recordVO);
			ConstellioUI.getCurrent().addWindow(window);
		} catch (UserDoesNotHaveAccessException e) {
			log.error(e.getMessage(), e);
		}

	}
}
