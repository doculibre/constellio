package com.constellio.app.modules.tasks.ui.components.display;

import com.constellio.app.modules.tasks.ui.components.converters.TaskFollowerVOToStringConverter;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.vaadin.ui.Label;

public class TaskFollowerDisplay extends Label {
	
	private TaskFollowerVOToStringConverter converter = new TaskFollowerVOToStringConverter();

	public TaskFollowerDisplay(TaskFollowerVO taskFollowerVO) {
		String label = converter.convertToPresentation(taskFollowerVO, String.class, getLocale());
		setValue(label);
	}

}
