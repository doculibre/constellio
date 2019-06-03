package com.constellio.app.modules.tasks.ui.pages.tasks;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.tasks.extensions.action.Action;
import com.constellio.app.modules.tasks.extensions.param.PromptUserParam;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.components.TaskFieldFactory;
import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.fields.TaskDecisionField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveWorkflowInclusiveDecisionFieldImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.ErrorDisplayUtil;
import com.constellio.app.ui.framework.components.fields.comment.CommentField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.data.Validator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public abstract class TaskCompleteWindowButton extends WindowButton {

	private AppLayerFactory appLayerFactory;
	private Task task;
	private SessionContext sessionContext;
	private String collection;
	private BaseView view;
	private TaskTable.TaskPresenter presenter;
	private RMModuleExtensions rmModuleExtensions;

	public TaskCompleteWindowButton(Task task, String caption, AppLayerFactory appLayerFactory,
									TaskTable.TaskPresenter presenter) {
		super(caption, caption, WindowConfiguration.modalDialog("1000px", "800px"));

		this.task = task;
		this.appLayerFactory = appLayerFactory;
		this.presenter = presenter;
		this.view = presenter.getView();
		this.sessionContext = view.getSessionContext();
		this.collection = sessionContext.getCurrentCollection();
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	@Override
	protected Component buildWindowContent() {
		reloadTask();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		Label label = new Label(getConfirmDialogMessage());
		mainLayout.addComponent(label);
		RecordVO recordVO = new RecordToVOBuilder().build(task.getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
		TaskFieldFactory taskFieldFactory = new TaskFieldFactory(false);

		Map.Entry<MetadataVO, Field> decisionField = buildDecisionField(recordVO, taskFieldFactory, mainLayout);
		Field acceptedField = buildAcceptedField(recordVO, taskFieldFactory, mainLayout);
		Field reasonField = buildReasonField(recordVO, taskFieldFactory, mainLayout);
		Field commentField = buildCommentField(decisionField, recordVO, taskFieldFactory, mainLayout);

		Map<MetadataVO, Field> uncompletedRequiredFields = buildUncompletedRequiredFields(recordVO, taskFieldFactory, mainLayout);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.addComponents(buildCancelButton(), buildSlowCompleteButton(),
				buildQuickCompleteButton(mainLayout, decisionField, acceptedField, reasonField, commentField, uncompletedRequiredFields));

		mainLayout.addComponent(buttonsLayout);
		mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

		setWindowHeight(decisionField, acceptedField, reasonField, commentField, uncompletedRequiredFields);

		return mainLayout;
	}

	private void setWindowHeight(Entry<MetadataVO, Field> decisionField, Field acceptedField, Field reasonField,
								 Field commentField, Map<MetadataVO, Field> uncompletedRequiredFields) {
		double height = 200;

		if (uncompletedRequiredFields != null) {
			height += uncompletedRequiredFields.size() * 80;
		}

		if (decisionField != null) {
			height += 80;
		}
		if (acceptedField != null) {
			height += 60;
		}
		if (reasonField != null) {
			height += 150;
		}
		if (commentField != null) {
			height += 200;
		}
		getWindow().setHeight(height + "px");
	}

	private Button buildSlowCompleteButton() {
		Button slowCompleteBtn = new Button(getSlowCompleteCaption());
		slowCompleteBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
				view.navigate().to().editTask(task.getId());
			}
		});

		return slowCompleteBtn;
	}

	private Button buildCancelButton() {
		Button cancelButton = new Button(getCancelCaption());
		cancelButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				getWindow().close();
			}
		});
		return cancelButton;
	}

	private Button buildQuickCompleteButton(final VerticalLayout fieldLayout,
											final Map.Entry<MetadataVO, Field> decisionMetadataAndField,
											final Field decisionField, final Field reasonField,
											final Field commentField,
											final Map<MetadataVO, Field> uncompletedRequiredFields) {
		final Button saveButton = new Button(getQuickCaption());
		saveButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				List<String> errors = new ArrayList<>();
				for (int i = 0; i < fieldLayout.getComponentCount(); i++) {
					if (fieldLayout.getComponent(i) instanceof Field) {
						Field field = (Field) fieldLayout.getComponent(i);
						try {
							field.validate();
						} catch (Validator.InvalidValueException invalidValueException) {
							HashMap<String, Object> parameters = new HashMap<>();
							parameters.put("metadataLabel", field.getCaption());
							errors.add(
									$("com.constellio.model.services.schemas.validators.MetadataValueTypeValidator_requiredValueForMetadata",
											parameters));
						} catch (Exception e) {
							view.showErrorMessage(e.getMessage());
							return;
						}
					}
				}

				if (errors.isEmpty()) {
					updateUncompletedRequiredField(uncompletedRequiredFields);
					boolean completed = completeQuicklyButtonClicked(
							decisionMetadataAndField == null ? null : decisionMetadataAndField.getValue().getValue(),
							decisionMetadataAndField == null ? null : decisionMetadataAndField.getKey().getLocalCode(),
							decisionField == null ? null : (Boolean) decisionField.getValue(),
							reasonField == null ? null : (String) reasonField.getValue(),
							commentField == null ? null : (Comment) commentField.getValue());
					if (completed) {
						getWindow().close();
					}

				} else {
					StringBuilder stringBuilder = new StringBuilder();
					String prefix = "";
					for (String error : errors) {
						stringBuilder.append(prefix);
						stringBuilder.append(error);
						prefix = "<br>";
					}
					view.showErrorMessage(stringBuilder.toString());
				}
			}
		});
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		return saveButton;
	}

	private boolean completeQuicklyButtonClicked(final Object decision, final String decisionCode,
												 final Boolean accepted,
												 final String reason, final Comment comment) {
		boolean userPromted = false;
		boolean exception = false;

		try {
			userPromted = rmModuleExtensions.isPromptUser(new PromptUserParam(task, new Action() {
				@Override
				public void doAction() {
					quickCompleteTask(task, decision, decisionCode, accepted, reason, comment);
				}
			}));
		} catch (ValidationException e) {
			exception = true;
			ErrorDisplayUtil.showBackendValidationException(e.getValidationErrors());
		}

		if (!userPromted) {
			quickCompleteTask(task, decision, decisionCode, accepted, reason, comment);
		}


		return !exception;
	}

	private boolean quickCompleteTask(Task task, Object decision, String decisionCode, Boolean accepted,
									  String reason, Comment comment) {
		boolean validationException = false;
		try {
			presenter.beforeCompletionActions(task);
			quickCompleteTask(appLayerFactory, task, decision, decisionCode, accepted, reason, comment,
					view.getSessionContext().getCurrentUser().getId());
		} catch (RecordServicesException.ValidationException e) {
			ErrorDisplayUtil.showBackendValidationException(e.getErrors());
			validationException = true;
		} catch (Exception e) {
			e.printStackTrace();
			view.showErrorMessage(e.getMessage());
			throw new RuntimeException();
		}
		presenter.afterCompletionActions();
		presenter.reloadTaskModified(task);
		return !validationException;
	}

	static public void quickCompleteTask(AppLayerFactory appLayerFactory, Task task,
										 Object decision, String decisionCode, Boolean accepted, String reason,
										 Comment comment, String respondantId)
			throws RecordServicesException {
		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(task.getCollection(), appLayerFactory);
		TasksSearchServices tasksSearchServices = new TasksSearchServices(tasksSchemas);
		TaskStatus finishedStatus = tasksSearchServices
				.getFirstFinishedStatus();
		if (finishedStatus != null) {
			task.setStatus(finishedStatus.getId());
		}
		if (tasksSchemas.isRequestTask(task)) {
			task.set(RequestTask.RESPONDANT, respondantId);
			task.set(RequestTask.ACCEPTED, accepted);
			task.set(RequestTask.REASON, reason);
		}
		if (decisionCode != null) {
			task.set(decisionCode, decision);
		}
		if (comment != null) {
			List<Comment> comments = new ArrayList<>(task.getComments());

			StringBuilder sb = new StringBuilder();
			sb.append($("DisplayTaskView.decisionCommentPrefix", decisionCode, task.getTitle()));
			sb.append("\n");
			sb.append(comment.getMessage());
			comment.setMessage(sb.toString());
			comments.add(comment);
			task.setComments(comments);
		}

		appLayerFactory.getModelLayerFactory().newRecordServices().update(task);
	}

	protected abstract String getConfirmDialogMessage();

	protected String getQuickCaption() {
		return $("DisplayTaskView.quickComplete");
	}

	protected String getCancelCaption() {
		return $("cancel");
	}

	protected String title() {
		return $("ConfirmDialog.title");
	}

	protected String getSlowCompleteCaption() {
		return $("DisplayTaskView.slowComplete");
	}

	private void reloadTask() {
		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		task = tasksSchemas.getTask(task.getId());
	}

	private void updateUncompletedRequiredField(Map<MetadataVO, Field> fields) {
		MetadataSchema taskSchema = task.getSchema();
		Record record = task.getWrappedRecord();

		for (Map.Entry<MetadataVO, Field> entry : MapUtils.emptyIfNull(fields).entrySet()) {
			MetadataVO m = entry.getKey();
			Field field = entry.getValue();

			Object value = field.getValue();
			if (value instanceof Date) {
				value = LocalDate.fromDateFields((Date) value);
			}
			record.set(taskSchema.getMetadata(m.getCode()), value);
		}
	}

	private Map.Entry<MetadataVO, Field> buildDecisionField(RecordVO recordVO, TaskFieldFactory fieldFactory,
															VerticalLayout fieldLayout) {
		Field decisionField = null;
		MapStringStringStructure decisions = task.get(Task.BETA_NEXT_TASKS_DECISIONS);
		Map.Entry<MetadataVO, Field> decisionMetadataAndField = null;

		if (task.getModelTask() != null && decisions != null) {
			MetadataVO decisionMetadata = recordVO.getMetadata(Task.DECISION);
			boolean isInclusiveDecision = Boolean.TRUE.equals(task.get(AddEditTaskPresenter.IS_INCLUSIVE_DECISION));
			if (isInclusiveDecision) {
				decisionMetadata = recordVO.getMetadata(AddEditTaskPresenter.INCLUSIVE_DECISION);
			}
			decisionField = fieldFactory.build(decisionMetadata);
			decisionField.setRequired(true);

			List<String> decisionCodes = new ArrayList<>();
			decisionCodes.addAll(decisions.keySet());
			Collections.sort(decisionCodes);
			for (String decision : decisionCodes) {
				if (decisionField instanceof TaskDecisionField) {
					((TaskDecisionField) decisionField).addItem(decision);
				} else {
					((ListAddRemoveWorkflowInclusiveDecisionFieldImpl) decisionField).addItem(decision);
				}

				if (Task.isExpressionLanguage(decision)) {
					decisionField.setRequired(true);
					decisionField.setVisible(false);
				}
			}

			Object object = task.get(decisionMetadata.getLocalCode());
			if (object != null) {
				decisionField.setValue(object);
			}

			if (recordVO.getSchema().getMetadata(Task.QUESTION) != null && task.get(Task.QUESTION) != null) {
				String question = task.get(Task.QUESTION);
				if (question != null) {
					Label questionField = new Label(question);
					questionField.addStyleName(ValoTheme.LABEL_BOLD);
					fieldLayout.addComponent(questionField);
				}
			}

			fieldLayout.addComponent(decisionField);

			decisionMetadataAndField = new AbstractMap.SimpleEntry<>(decisionMetadata, decisionField);
		}

		return decisionMetadataAndField;
	}

	private Field buildAcceptedField(RecordVO recordVO, TaskFieldFactory fieldFactory,
									 VerticalLayout fieldLayout) {
		Field acceptedField = null;
		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		if (tasksSchemas.isRequestTask(task)) {
			acceptedField = fieldFactory.build(recordVO.getMetadata(RequestTask.ACCEPTED));
			acceptedField.setRequired(true);
			fieldLayout.addComponent(acceptedField);
		}
		return acceptedField;
	}

	private Field buildReasonField(RecordVO recordVO, TaskFieldFactory fieldFactory,
								   VerticalLayout fieldLayout) {
		Field reasonField = null;
		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		if (tasksSchemas.isRequestTask(task)) {
			reasonField = fieldFactory.build(recordVO.getMetadata(RequestTask.REASON));
			fieldLayout.addComponent(reasonField);
		}
		return reasonField;
	}

	protected Field buildCommentField(final Map.Entry<MetadataVO, Field> decisionField, final RecordVO recordVO, final TaskFieldFactory fieldFactory,
									final VerticalLayout fieldLayout) {
		Field commentField;
		MapStringStringStructure decisions = task.get(Task.BETA_NEXT_TASKS_DECISIONS);
		if (task.getModelTask() != null && decisions != null) {
			commentField = new CommentField();
			commentField.setCaption(recordVO.getMetadata(Task.COMMENTS).getLabel());
			addCommentField(recordVO, commentField, fieldLayout);
		} else {
			commentField = null;
		}
		return commentField;
	}

	protected void addCommentField(RecordVO recordVO, Field commentField, VerticalLayout fieldLayout) {
		fieldLayout.addComponent(commentField);
	}

	private Map<MetadataVO, Field> buildUncompletedRequiredFields(RecordVO recordVO, TaskFieldFactory fieldFactory,
																  VerticalLayout fieldLayout) {

		Map<MetadataVO, Field> fields = new HashMap<>();
		List<MetadataValueVO> formMetadataValues = recordVO.getFormMetadataValues();
		for (MetadataValueVO metadataValueVO : CollectionUtils.emptyIfNull(formMetadataValues)) {
			MetadataVO m = metadataValueVO.getMetadata();
			if (m.isRequired() && m.isEnabled() && m.getDefaultValue() == null && metadataValueVO.getValue() == null) {
				Field<?> field = fieldFactory.build(m);
				field.setWidth("100%");
				fieldLayout.addComponent(field);
				fields.put(m, field);
			}
		}
		return fields;
	}
}
