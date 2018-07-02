package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.ui.components.fields.*;
import com.constellio.app.modules.tasks.ui.components.fields.list.*;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Field;

import java.util.List;

import static com.constellio.app.modules.rm.wrappers.Document.TYPE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.*;
import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class TaskFieldFactory extends MetadataFieldFactory {

	public static final String INCLUSIVE_DECISION = "inclusiveDecision";
	public static final String ASSIGNEE_GROUPS_CANDIDATES = "assigneeGroupsCandidates";
	public static final String ASSIGNEE_USERS_CANDIDATES = "assigneeUsersCandidates";
	public static final String ASSIGNATION_MODES = "assignationModes";
	public static final String INSTANCE_WORKFLOW = "linkedWorkflowExecution";
	public static final String ASSIGNER = "assigner";

	private List<String> unavailablesTaskTypes;

	public TaskFieldFactory(boolean isViewOnly) {
		super(isViewOnly);
	}

	public TaskFieldFactory(boolean isViewOnly, List<String> unavailablesTaskTypes) {
		super(isViewOnly);
		this.unavailablesTaskTypes = unavailablesTaskTypes;
	}
//assigneeGroupsCandidates assigneeUsersCandidates escalationAssignee assignationModes
	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		MetadataInputType inputType = metadata.getMetadataInputType();
		switch (metadata.getLocalCode()) {
		case TYPE:
			if (MetadataInputType.LOOKUP.equals(inputType)) {
				field = new TaskTypeFieldLookupImpl(unavailablesTaskTypes);
			} else if (MetadataInputType.RADIO_BUTTONS.equals(inputType)) {
				field = new TaskTypeFieldOptionGroupImpl(unavailablesTaskTypes);
			} else {
				field = new TaskTypeFieldComboBoxImpl(unavailablesTaskTypes);
			}
			break;
		case TASK_FOLLOWERS:
			field = new ListAddRemoveTaskFollowerField();
			break;
		case REMINDERS:
			field = new ListAddRemoveTaskReminderField();
			break;
		case PROGRESS_PERCENTAGE:
			field = new TaskProgressPercentageFieldImpl();
			break;
		case RELATIVE_DUE_DATE:
			field = new TaskRelativeDueDateFieldImpl();
			break;
		case DECISION:
			field = new TaskDecisionFieldImpl();
			break;
		case QUESTION:
			field = new TaskQuestionFieldImpl();
			field.setReadOnly(true);
			break;
		case BorrowRequest.ACCEPTED:
			field = new TaskAcceptedFieldImpl();
			break;
		case BorrowRequest.REASON:
			field = new TaskReasonFieldImpl();
			break;
		case LINKED_FOLDERS:
			field = new TaskListAddRemoveLinkedFoldersLookupField();
			postBuild(field, metadata);
			break;
        case LINKED_DOCUMENTS:
            field = new TaskListAddRemoveLinkedDocumentsLookupField();
			postBuild(field, metadata);
            break;
        case INCLUSIVE_DECISION:
        	field = new ListAddRemoveWorkflowInclusiveDecisionFieldImpl();
        	break;
		case REMINDER_FREQUENCY:
			field = new TaskReminderFrequencyFieldImpl();
			break;
		case ASSIGNEE_GROUPS_CANDIDATES:
			field = new TaskAssignationListRecordLookupField(metadata.getSchemaTypeCode());
			postBuild(field, metadata);
			break;
		case ASSIGNEE_USERS_CANDIDATES:
			field = new TaskAssignationListRecordLookupField(metadata.getSchemaTypeCode());
			postBuild(field, metadata);
			break;
		case ASSIGNER:
			field = new LookupRecordField(User.SCHEMA_TYPE);
			postBuild(field, metadata);
			break;
		case ASSIGNATION_MODES:
			field = new TaskAssignationEnumField(metadata.getEnumClass());
			postBuild(field, metadata);
			break;
		case INSTANCE_WORKFLOW:
			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			String currentCollection = metadata.getCollection();
			field = appLayerFactory.getExtensions().forCollection(currentCollection).getFieldForMetadata(metadata);
			postBuild(field, metadata);
			break;
		default:
			field = super.build(metadata);
		}
		if (field instanceof CustomTaskField) {
			postBuild(field, metadata);
		}
		return field;
	}
}
