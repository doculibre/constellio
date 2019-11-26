package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

import java.util.List;

public interface RecordCommentsDisplay {

	void setCaption(String caption);

	void setVisible(boolean visible);

	void setReadOnly(boolean readOnly);

	void setComments(List<Comment> comments);

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();
}
