package com.constellio.app.modules.rm.ui.components.content;

import com.constellio.app.ui.application.ConstellioUI.Navigation;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContextProvider;

public interface DocumentContentVersionWindow extends SessionContextProvider {
	RecordVO getRecordVO();

	ContentVersionVO getContentVersionVO();

	void setReadOnlyMessage(String message);

	void setCheckOutLinkVisible(boolean visible);

	void closeWindow();

	void setAgentURL(String agentURL);

	void open(String url);

	@Deprecated
	CoreViews navigateTo();

	Navigation navigate();
}
