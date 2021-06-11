package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

public interface TaskTable {
	void addCompleteWindowCommentField(RecordVO taskVO, Field commentField, VerticalLayout fieldLayout);

	void displayTask(Object itemId, RecordVO taskVO);

	void editTask(RecordVO taskVO);
}
