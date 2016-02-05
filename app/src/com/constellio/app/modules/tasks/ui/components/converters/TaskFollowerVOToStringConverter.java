package com.constellio.app.modules.tasks.ui.components.converters;

import static com.constellio.app.ui.i18n.i18n.$;
import java.util.Locale;

import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.vaadin.data.util.converter.Converter;

public class TaskFollowerVOToStringConverter implements Converter<String, TaskFollowerVO> {
	
	private RecordIdToCaptionConverter followerIdConverter = new RecordIdToCaptionConverter();
	
	@Override
	public TaskFollowerVO convertToModel(String value, Class<? extends TaskFollowerVO> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return null;
	}

	@Override
	public String convertToPresentation(TaskFollowerVO value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String presentation;
		if (value != null) {
			String followerId = value.getFollowerId();
			boolean followTaskStatusModified = value.isFollowTaskStatusModified();
			boolean followTaskAssigneeModified = value.isFollowTaskAssigneeModified();
			boolean followTaskCompleted = value.isFollowTaskCompleted();
			boolean followTaskDeleted = value.isFollowTaskDeleted();
			boolean followSubTasksModified = value.isFollowSubTasksModified();
			
			StringBuffer sb = new StringBuffer();
			String followerIdCaption = followerIdConverter.convertToPresentation(followerId, targetType, locale);
			sb.append(followerIdCaption);
			if (followTaskStatusModified) {
				sb.append(", ");
				sb.append($("TaskFollower.display.followTaskStatusModified"));
			}
			if (followTaskAssigneeModified) {
				sb.append(", ");
				sb.append($("TaskFollower.display.followTaskAssigneeModified"));
			}
			if (followTaskCompleted) {
				sb.append(", ");
				sb.append($("TaskFollower.display.followTaskCompleted"));
			}
			if (followTaskDeleted) {
				sb.append(", ");
				sb.append($("TaskFollower.display.followTaskDeleted"));
			}
			if (followSubTasksModified) {
				sb.append(", ");
				sb.append($("TaskFollower.display.followSubTasksModified"));
			}
			presentation = sb.toString();
		} else {
			presentation = null;
		}
		return presentation;
	}

	@Override
	public Class<TaskFollowerVO> getModelType() {
		return TaskFollowerVO.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}
}
