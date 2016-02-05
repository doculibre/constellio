package com.constellio.app.modules.rm.ui.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.ValoTheme;

public class ConstellioAgentLink extends HorizontalLayout {

	public ConstellioAgentLink(String agentURL, ContentVersionVO contentVersionVO) {
		this(agentURL, contentVersionVO, contentVersionVO.toString());
	}

	public ConstellioAgentLink(String agentURL, ContentVersionVO contentVersionVO, String caption) {
		this(agentURL, contentVersionVO, caption, true);
	}

	public ConstellioAgentLink(String agentURL, ContentVersionVO contentVersionVO, String caption, boolean downloadLink) {
		addComponent(new AgentLink(agentURL, contentVersionVO, caption));
		if (downloadLink) {
			addComponent(new DownloadContentVersionLink(contentVersionVO, new ThemeResource("images/icons/down.gif")));
		}
	}

	public ConstellioAgentLink(String agentURL, RecordVO recordVO, MetadataVO metadataVO) {
		this(agentURL, (ContentVersionVO) recordVO.get(metadataVO));
	}

	public static class AgentLink extends Link {
		public static final String STYLE_NAME = "download-content-version-link";

		public AgentLink(String agentURL, ContentVersionVO contentVersionVO, String caption) {
			this(agentURL, contentVersionVO.getFileName(), caption);
		}

		public AgentLink(String agentURL, String fileName, String caption) {
			super(caption, new ExternalResource(agentURL));
			addStyleName(STYLE_NAME);
			addStyleName(ValoTheme.BUTTON_LINK);

			Resource icon = FileIconUtils.getIcon(fileName);
			if (icon != null) {
				setIcon(icon);
			}
			setSizeFull();
		}
	}
}
