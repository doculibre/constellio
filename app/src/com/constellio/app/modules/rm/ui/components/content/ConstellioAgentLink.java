package com.constellio.app.modules.rm.ui.components.content;

import static com.constellio.app.ui.application.ConstellioUI.getCurrent;
import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.content.UpdatableContentVersionPresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ConstellioAgentLink extends HorizontalLayout {
	
	private AgentLink agentLink;
	
	private DownloadContentVersionLink downloadContentLink;

	public ConstellioAgentLink(String agentURL, RecordVO recordVO, ContentVersionVO contentVersionVO, String caption,
							   UpdatableContentVersionPresenter presenter) {
		this(agentURL, recordVO, contentVersionVO, caption, true, presenter);
	}

	public ConstellioAgentLink(String agentURL, ContentVersionVO contentVersionVO, String caption,
							   boolean downloadLink) {
		this(agentURL, null, contentVersionVO, caption, downloadLink, null);
	}

	public ConstellioAgentLink(final String agentURL, final RecordVO recordVO, final ContentVersionVO contentVersionVO,
							   String caption, boolean downloadLink, UpdatableContentVersionPresenter presenter) {
		addStyleName("agent-link");
		agentLink = new AgentLink(agentURL, contentVersionVO, caption);
		agentLink.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				new ConstellioAgentClickHandler().handleClick(agentURL, recordVO, contentVersionVO, null);
			}
		});
		SessionContext sessionContext = getCurrent().getSessionContext();
		if (recordVO != null && sessionContext.isVisited(recordVO.getId())) {
			agentLink.addStyleName("visited-link");
		}
		addComponent(agentLink);
		if (downloadLink) {
			if (presenter != null) {
				downloadContentLink = new DownloadContentVersionLink(recordVO, contentVersionVO, new ThemeResource("images/icons/actions/download.png"), presenter);
			} else {
				downloadContentLink = new DownloadContentVersionLink(contentVersionVO, new ThemeResource("images/icons/actions/download.png"));
			}
			downloadContentLink.setDescription($("download"));
			addComponent(downloadContentLink);
		}
	}

	public AgentLink getAgentLink() {
		return agentLink;
	}

	public DownloadContentVersionLink getDownloadContentLink() {
		return downloadContentLink;
	}
	
	public void addVisitedClickListener(final String id) {
		agentLink.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
				sessionContext.addVisited(id);
			}
		});
	}

	public static class AgentLink extends Button {

		public static final String STYLE_NAME = "agent-action-link";

		public AgentLink(String agentURL, ContentVersionVO contentVersionVO, String caption) {
			this(agentURL, contentVersionVO != null ? contentVersionVO.getFileName() : null, caption);
		}

		public AgentLink(String agentURL, String filename, String caption) {
			super(caption);
			addStyleName(STYLE_NAME);
			addStyleName(ValoTheme.BUTTON_LINK);

			if (filename == null) {
				filename = agentURL;
			}
			Resource icon = FileIconUtils.getIcon(filename);
			if (icon != null) {
				setIcon(icon);
			}
			setSizeFull();
		}
	}
}
