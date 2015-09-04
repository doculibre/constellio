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

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.ValoTheme;

public class ConstellioAgentLink extends Link {
	
	public static final String STYLE_NAME = "download-content-version-link";

	public ConstellioAgentLink(String agentURL, ContentVersionVO contentVersionVO) {
		this(agentURL, contentVersionVO, contentVersionVO.toString());
	}

	public ConstellioAgentLink(String agentURL, ContentVersionVO contentVersionVO, String caption) {
		this(agentURL, contentVersionVO.getFileName(), caption);
	}

	public ConstellioAgentLink(String agentURL, String fileName, String caption) {
		super(caption, new ExternalResource(agentURL));
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_LINK);

		Resource icon = FileIconUtils.getIcon(fileName);
		if (icon != null) {
			setIcon(icon);
		}
		setSizeFull();
	}

	public ConstellioAgentLink(String agentURL, RecordVO recordVO, MetadataVO metadataVO) {
		this(agentURL, (ContentVersionVO) recordVO.get(metadataVO));
	}
	
}
