package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.ui.builders.TaskFollowerFromVOBuilder;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.components.fields.TaskFollowerFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.pages.profile.ModifyProfileView;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.ui.Field;

import java.util.ArrayList;
import java.util.List;

public class TaskUserFieldsExtension extends PagesComponentsExtension {
	String collection;
	AppLayerFactory appLayerFactory;

	public TaskUserFieldsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public List<AdditionnalRecordField> getAdditionnalFields(RecordFieldsExtensionParams params) {
		ArrayList<AdditionnalRecordField> additionnalFields = new ArrayList<>();
		if(params.getMainComponent() instanceof ModifyProfileView) {
			TaskToVOBuilder taskToVOBuilder = new TaskToVOBuilder();
			User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());
			TaskFollower taskFollower = user.get(User.DEFAULT_FOLLOWER_WHEN_CREATING_TASK);
			AdditionalTaskFollowerFieldImpl field = new AdditionalTaskFollowerFieldImpl();

			if(taskFollower != null) {
				field.setTaskFollowerVO(taskToVOBuilder.toTaskFollowerVO(taskFollower));
			}
			additionnalFields.add(field);
		}
		return additionnalFields;
	}

	private class AdditionalTaskFollowerFieldImpl extends TaskFollowerFieldImpl implements AdditionnalRecordField<TaskFollowerVO> {

		@Override
		public String getMetadataLocalCode() {
			return User.DEFAULT_FOLLOWER_WHEN_CREATING_TASK;
		}

		@Override
		public TaskFollower getCommittableValue() {
			TaskFollowerVO value = getValue();
			if(value != null) {
				TaskFollowerFromVOBuilder builder = new TaskFollowerFromVOBuilder();
				return builder.build(value);
			}
			return null;
		}
	}
}
