package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.tasks.TaskConfigs;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class TaskEmailExtension {

	protected String collection;
	protected AppLayerFactory appLayerFactory;
	protected TasksSchemasRecordsServices taskServices;

	public TaskEmailExtension(String collection,
							  AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.taskServices = new TasksSchemasRecordsServices(collection, appLayerFactory);
	}

	protected boolean showComments() {
		return appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager().getValue(TaskConfigs.SHOW_COMMENTS);
	}

	public List<String> newParameters(Task task) {
		return Collections.emptyList();
	}

	protected String buildCommentsValue(List<Comment> comments, String label) {
		StringBuilder htmlComments = new StringBuilder();
		if (!comments.isEmpty()) {
			htmlComments.append(formatToParameter(label + " :" + "<br/>"));
			for (Iterator<Comment> it = comments.iterator(); it.hasNext(); ) {
				Comment comment = it.next();
				User user = taskServices.getUser(comment.getUserId());
				htmlComments.append(escapeHtml4(user.getTitle() + " : " + comment.getCreationDateTime().toString()) + "<br/>");
				htmlComments.append(escapeHtml4(comment.getMessage()).replace("\n", "<br/>") + "<br/>");

				if (it.hasNext()) {
					htmlComments.append("<br/>");
				}
			}
		}

		return formatToParameter(htmlComments.toString());
	}

	protected String formatToParameter(String parameter) {
		if (parameter == null) {
			return "";
		}
		return parameter;
	}
}
