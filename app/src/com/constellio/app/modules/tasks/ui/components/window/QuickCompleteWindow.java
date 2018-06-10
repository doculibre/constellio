package com.constellio.app.modules.tasks.ui.components.window;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.components.TaskFieldFactory;
import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.modules.tasks.ui.components.fields.TaskDecisionField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class QuickCompleteWindow {
    Window floatingWindow;
    String collection;
    AppLayerFactory appLayerFactory;
    TasksSearchServices tasksSearchServices;
    TaskTable.TaskPresenter presenter;
    BaseView view;

    public QuickCompleteWindow(TaskTable.TaskPresenter presenter, AppLayerFactory appLayerFactory, RecordVO recordVO) {
        this.presenter = presenter;
        this.view = presenter.getView();
        this.collection = view.getCollection();
        this.appLayerFactory = appLayerFactory;
        TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
        this.tasksSearchServices = new TasksSearchServices(tasksSchemas);
        Task task = tasksSchemas.getTask(recordVO.getId());

        floatingWindow = new Window($("com.constellio.app.extensions.WorkflowPageExtension_confirmationTitle"));
        floatingWindow.setWidth("50%");
        floatingWindow.setHeight("50%");
        floatingWindow.center();
        floatingWindow.setContent(buildQuickCompletePopup(task));
    }

    public Component buildQuickCompletePopup(final Task task) {

        RecordVO recordVO = new RecordToVOBuilder().build(task.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, view.getSessionContext());
        TaskFieldFactory fieldFactory = new TaskFieldFactory(false);
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        final VerticalLayout fieldLayout = new VerticalLayout();
        fieldLayout.setSpacing(true);

        Field decisionField = buildDecisionField(task, recordVO, fieldFactory, fieldLayout);
        Field acceptedField = buildAcceptedField(task, recordVO, fieldFactory, fieldLayout);
        Field reasonField = buildReasonField(task, recordVO, fieldFactory, fieldLayout);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.addComponents(buildCancelButton(), buildSaveButton(task, fieldLayout, decisionField, acceptedField, reasonField));
        mainLayout.addComponents(fieldLayout, buttonLayout);
        return mainLayout;
    }

    private Field buildDecisionField(Task task, RecordVO recordVO, TaskFieldFactory fieldFactory, VerticalLayout fieldLayout) {
        Field decisionField = null;
        MapStringStringStructure decisions = task.get(Task.BETA_NEXT_TASKS_DECISIONS);
        if (task.getModelTask() != null && decisions != null) {
            MetadataVO decisionMetadata = recordVO.getMetadata(Task.DECISION);
            decisionField = fieldFactory.build(decisionMetadata);
            decisionField.setRequired(true);

            List<String> decisionCodes = new ArrayList<>();
            decisionCodes.addAll(decisions.keySet());
            Collections.sort(decisionCodes);
            for(String decision: decisionCodes) {
                ((TaskDecisionField) decisionField).addItem(decision);
                if (Task.isExpressionLanguage(decision)) {
                    decisionField.setRequired(true);
                    decisionField.setVisible(false);
                }
            }

            if(task.getDecision() != null) {
                decisionField.setValue(task.getDecision());
            }

            if(recordVO.getSchema().getMetadata(Task.QUESTION) != null && task.get(Task.QUESTION) != null) {
                String question = task.get(Task.QUESTION);
                if(question != null) {
                    Label questionField = new Label(question);
                    questionField.addStyleName(ValoTheme.LABEL_BOLD);
                    fieldLayout.addComponent(questionField);
                }
            }

            fieldLayout.addComponent(decisionField);
        }
        return decisionField;
    }

    private Field buildAcceptedField(Task task, RecordVO recordVO, TaskFieldFactory fieldFactory, VerticalLayout fieldLayout) {
        Field acceptedField = null;
        TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
        if(tasksSchemas.isRequestTask(task)) {
            acceptedField = fieldFactory.build(recordVO.getMetadata(RequestTask.ACCEPTED));
            acceptedField.setRequired(true);
            fieldLayout.addComponent(acceptedField);
        }
        return acceptedField;
    }

    private Field buildReasonField(Task task, RecordVO recordVO, TaskFieldFactory fieldFactory, VerticalLayout fieldLayout) {
        Field reasonField = null;
        TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
        if(tasksSchemas.isRequestTask(task)) {
            reasonField = fieldFactory.build(recordVO.getMetadata(RequestTask.REASON));
            fieldLayout.addComponent(reasonField);
        }
        return reasonField;
    }

    private Button buildCancelButton() {
        Button cancelButton = new Button($("cancel"));
        cancelButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                floatingWindow.close();
            }
        });
        return cancelButton;
    }

    private Button buildSaveButton(final Task task, final VerticalLayout fieldLayout, final Field decisionField, final Field acceptedField, final Field reasonField) {
        final Button saveButton = new Button($("save"));
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                List<String> errors = new ArrayList<>();
                for(int i = 0; i < fieldLayout.getComponentCount(); i++) {
                    if(fieldLayout.getComponent(i) instanceof Field) {
                        Field field = (Field) fieldLayout.getComponent(i);
                        try {
                            field.validate();
                        } catch (Validator.InvalidValueException invalidValueException) {
                            HashMap<String, Object> parameters = new HashMap<>();
                            parameters.put("metadataLabel", field.getCaption());
                            errors.add($("com.constellio.model.services.schemas.validators.MetadataValueTypeValidator_requiredValueForMetadata", parameters));
                        } catch (Exception e) {
                            view.showErrorMessage(e.getMessage());
                            return;
                        }
                    }
                }

                if(errors.isEmpty()) {
                    completeQuicklyButtonClicked(task,
                            decisionField == null? null: (String) decisionField.getValue(),
                            acceptedField == null? null: (Boolean) acceptedField.getValue(),
                            reasonField == null? null: (String) reasonField.getValue());
                    floatingWindow.close();
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    String prefix = "";
                    for(String error: errors) {
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

    private void completeQuicklyButtonClicked(Task task, String decision, Boolean accepted, String reason) {
        try {
            quickCompleteTask(appLayerFactory, task, decision, accepted, reason, view.getSessionContext().getCurrentUser().getId());
        } catch (RecordServicesException e) {
            e.printStackTrace();
            view.showErrorMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            view.showErrorMessage(e.getMessage());
            throw new RuntimeException();
        }

        presenter.reloadTaskModified(task);
    }

    static public void quickCompleteTask(AppLayerFactory appLayerFactory, Task task,
                                         String decision, Boolean accepted, String reason, String respondantId) throws RecordServicesException {
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

        task.setDecision(decision);
        appLayerFactory.getModelLayerFactory().newRecordServices().update(task);
    }

    public void show() {
        ConstellioUI.getCurrent().addWindow(floatingWindow);
    }
}
