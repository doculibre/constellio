package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskUser;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.ui.builders.TaskFollowerFromVOBuilder;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.components.fields.TaskFollowerFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.pages.profile.ModifyProfileView;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class TaskUserProfileFieldsExtension extends PagesComponentsExtension {
	String collection;
	AppLayerFactory appLayerFactory;

	@PropertyId("test")
	Field champ1;

	public TaskUserProfileFieldsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public List<AdditionnalRecordField> getAdditionnalFields(RecordFieldsExtensionParams params) {
		ArrayList<AdditionnalRecordField> additionnalFields = new ArrayList<>();
		if(params.getMainComponent() instanceof ModifyProfileView) {
			AdditionnalRecordField autoAssigningField = buildAutoAssigningField(params);
            AdditionnalRecordField taskFollowerField = buildTaskFollowerField(params);

			additionnalFields.addAll(asList(autoAssigningField, taskFollowerField));
		}
		return additionnalFields;
	}

	private AdditionnalRecordField buildTaskFollowerField(RecordFieldsExtensionParams params) {
        User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

        TaskToVOBuilder taskToVOBuilder = new TaskToVOBuilder();
        TaskFollower taskFollower = user.get(TaskUser.DEFAULT_FOLLOWER_WHEN_CREATING_TASK);
		TaskFollowerAdditionalFieldImpl taskFollowerField = new TaskFollowerAdditionalFieldImpl();
        taskFollowerField.setCaption(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(user.getWrappedRecord())
                .getMetadata(TaskUser.DEFAULT_FOLLOWER_WHEN_CREATING_TASK)
                .getLabel(Language.withLocale(params.getMainComponent().getSessionContext().getCurrentLocale())));

        if(taskFollower != null) {
            taskFollowerField.setTaskFollowerVO(taskToVOBuilder.toTaskFollowerVO(taskFollower));
        } else {
            taskFollowerField.setTaskFollowerVO(new TaskFollowerVO(user.getId(), false, false, false, false, false));
        }

        return taskFollowerField;
    }

    private AdditionnalRecordField buildAutoAssigningField(RecordFieldsExtensionParams params) {
        User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

        Boolean isAssigningTaskAutomatically = user.get(TaskUser.ASSIGN_TASK_AUTOMATICALLY);
        AutoAssigningTaskAdditionalFieldImpl autoAssigningField = new AutoAssigningTaskAdditionalFieldImpl();
        autoAssigningField.setImmediate(true);
		autoAssigningField.setCaption(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(user.getWrappedRecord())
				.getMetadata(TaskUser.ASSIGN_TASK_AUTOMATICALLY)
				.getLabel(Language.withLocale(params.getMainComponent().getSessionContext().getCurrentLocale())));

        if(Boolean.FALSE.equals(isAssigningTaskAutomatically)) {
            autoAssigningField.setValue(false);
        } else {
            autoAssigningField.setValue(true);
        }

        return autoAssigningField;
    }

	private class TaskFollowerAdditionalFieldImpl extends TaskFollowerFieldImpl implements AdditionnalRecordField<TaskFollowerVO> {

		@Override
		public String getMetadataLocalCode() {
			return TaskUser.DEFAULT_FOLLOWER_WHEN_CREATING_TASK;
		}

		@Override
		public TaskFollower getCommittableValue() {
			TaskFollowerVO value = getValue();
			if(value != null && isFollowingSomething(value)) {
				TaskFollowerFromVOBuilder builder = new TaskFollowerFromVOBuilder();
				return builder.build(value);
			}
			return null;
		}

		private boolean isFollowingSomething(TaskFollowerVO taskFollowerVO)  {
			return taskFollowerVO.isFollowSubTasksModified() || taskFollowerVO.isFollowTaskAssigneeModified() ||
					taskFollowerVO.isFollowTaskCompleted() || taskFollowerVO.isFollowTaskDeleted() ||
					taskFollowerVO.isFollowTaskStatusModified();
		}

		@Override
		protected boolean isFollowerIdFieldVisible() {
			return false;
		}

		@Override
		protected boolean isInvalidFieldValue() {
			return false;
		}
	}

	private class AutoAssigningTaskAdditionalFieldImpl extends CheckBox implements AdditionnalRecordField<Boolean>{

        @Override
        public String getMetadataLocalCode() {
            return TaskUser.ASSIGN_TASK_AUTOMATICALLY;
        }

        @Override
        public Boolean getCommittableValue() {
            return getValue();
        }
    }
}
