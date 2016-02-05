package com.constellio.app.api.extensions;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Component;

public interface DownloadContentVersionLinkExtension {
	
	Component getDownloadLink(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption);

}
