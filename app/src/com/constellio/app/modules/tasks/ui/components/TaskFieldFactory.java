package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.api.extensions.params.MetadataFieldExtensionParams;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAcceptedFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationEnumField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationListRecordLookupField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskDecisionFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskProgressPercentageFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskQuestionFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskReasonFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskRelativeDueDateFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskReminderFrequencyFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskTypeFieldComboBoxImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskTypeFieldLookupImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskTypeFieldOptionGroupImpl;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveCollaboratorsField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveCollaboratorsGroupsField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveTaskFollowerField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveTaskReminderField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveWorkflowInclusiveDecisionFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.list.TaskListAddRemoveLinkedDocumentsLookupField;
import com.constellio.app.modules.tasks.ui.components.fields.list.TaskListAddRemoveLinkedFoldersLookupField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.fields.lookup.GroupTextInputDataProvider;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.rm.wrappers.Document.TYPE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.DECISION;
import static com.constellio.app.modules.tasks.model.wrappers.Task.LINKED_DOCUMENTS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.LINKED_FOLDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.PROGRESS_PERCENTAGE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.QUESTION;
import static com.constellio.app.modules.tasks.model.wrappers.Task.RELATIVE_DUE_DATE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDERS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.REMINDER_FREQUENCY;
import static com.constellio.app.modules.tasks.model.wrappers.Task.STATUS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_COLLABORATORS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_COLLABORATORS_GROUPS;
import static com.constellio.app.modules.tasks.model.wrappers.Task.TASK_FOLLOWERS;
import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

public class TaskFieldFactory extends MetadataFieldFactory {

	public static final String INCLUSIVE_DECISION = "inclusiveDecision";
	public static final String ASSIGNEE_GROUPS_CANDIDATES = "assigneeGroupsCandidates";
	public static final String ASSIGNEE_USERS_CANDIDATES = "assigneeUsersCandidates";
	public static final String ASSIGNATION_MODES = "assignationModes";
	public static final String INSTANCE_WORKFLOW = "linkedWorkflowExecution";
	public static final String ASSIGNER = "assigner";
	public static final String SCRIPT = "script";

	private List<String> unavailablesTaskTypes;
	private RecordVO recordVO;
	private BaseView baseView;

	public TaskFieldFactory(boolean isViewOnly) {
		super(isViewOnly);
	}

	public TaskFieldFactory(boolean isViewOnly, List<String> unavailablesTaskTypes, RecordVO recordVO, BaseView taskForm) {
		super(isViewOnly);
		this.unavailablesTaskTypes = unavailablesTaskTypes;
		this.recordVO = recordVO;
		this.baseView = taskForm;
	}

	@Override
	public Field<?> build(MetadataVO metadata, Locale locale) {
		Field<?> field;
		MetadataInputType inputType = metadata.getMetadataInputType();
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		String currentCollection = metadata.getCollection();
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
				field = new TaskAssignationListRecordLookupField(metadata.getSchemaTypeCode(),
						new GroupTextInputDataProvider(getInstance(), getCurrentSessionContext()));
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
			case TASK_COLLABORATORS:
				field = new ListAddRemoveCollaboratorsField(recordVO);
				postBuild(field, metadata);
				break;
			case TASK_COLLABORATORS_GROUPS:
				field = new ListAddRemoveCollaboratorsGroupsField(recordVO, new GroupTextInputDataProvider(getInstance(), getCurrentSessionContext()));
				postBuild(field, metadata);
				break;
			case ASSIGNEE:
				field = super.build(metadata, locale);
				if(field instanceof LookupField) {
					((LookupField<Serializable>) field).setReadOnlyMessageI18NKey("TaskAssignee.readOnlyMessage");
				}
				break;
			case ASSIGNATION_MODES:
				field = new TaskAssignationEnumField(metadata.getEnumClass());
				postBuild(field, metadata);
				break;
			case STATUS:
				field = appLayerFactory.getExtensions().forCollection(currentCollection)
						.getMetadataField(new MetadataFieldExtensionParams(metadata, recordVO, baseView));
				postBuild(field, metadata);
				break;
		default:
			field = appLayerFactory.getExtensions().forCollection(currentCollection)
					.getMetadataField(new MetadataFieldExtensionParams(metadata, recordVO, baseView));
			if (field != null) {
				postBuild(field, metadata);
			} else {
				field = super.build(metadata, locale);
			}

		}
		if (field instanceof CustomTaskField) {
			postBuild(field, metadata);
		}
		return field;
	}

}
