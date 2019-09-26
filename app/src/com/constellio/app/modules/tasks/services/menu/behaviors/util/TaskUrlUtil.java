package com.constellio.app.modules.tasks.services.menu.behaviors.util;

import com.constellio.app.modules.tasks.navigation.TasksNavigationConfiguration;

public class TaskUrlUtil {
	public static String getPathToConsultLinkForTask(String id) {
		return "#!" + TasksNavigationConfiguration.DISPLAY_TASK + "/" + id;
	}
}
