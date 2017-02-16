package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.SelectionPanelExtension;
import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class RMSelectionPanelExtension extends SelectionPanelExtension {
    AppLayerFactory appLayerFactory;
    String collection;
    IOServices ioServices;

    public RMSelectionPanelExtension(AppLayerFactory appLayerFactory, String collection) {
        this.appLayerFactory = appLayerFactory;
        this.collection = collection;
        this.ioServices = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().newIOServices();
    }

    @Override
    public void addAvailableActions(AvailableActionsParam param) {
        addMoveButton(param);
        addDuplicateButton(param);
        addCheckInButton(param);
    }

    public void addMoveButton(final AvailableActionsParam param) {
        WindowButton moveInFolderButton = new WindowButton($("DisplayFolderView.parentFolder"), $("DisplayFolderView.parentFolder")
                , WindowButton.WindowConfiguration.modalDialog("50%", "20%")) {
            @Override
            protected Component buildWindowContent() {
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setSpacing(true);
                final LookupFolderField field = new LookupFolderField();
                verticalLayout.addComponent(field);
                BaseButton saveButton = new BaseButton($("save")) {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        String parentId = field.getValue();
                        try {
                            parentFolderButtonClicked(parentId, param.getIds());
                        } catch (Throwable e) {
//                            LOGGER.warn("error when trying to modify folder parent to " + parentId, e);
//                            showErrorMessage("DisplayFolderView.parentFolderException");
                            e.printStackTrace();
                        }
                        getWindow().close();
                    }
                };
                saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout();
                hLayout.setSizeFull();
                hLayout.addComponent(saveButton);
                hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
                verticalLayout.addComponent(hLayout);
                return verticalLayout;
            }
        };
        moveInFolderButton.setEnabled(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
        moveInFolderButton.setVisible(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
        ((VerticalLayout) param.getComponent()).addComponent(moveInFolderButton);
    }

    public void addDuplicateButton(final AvailableActionsParam param) {
        WindowButton moveInFolderButton = new WindowButton($("DisplayFolderView.duplicate"), $("DisplayFolderView.duplicate")
                , WindowButton.WindowConfiguration.modalDialog("50%", "20%")) {
            @Override
            protected Component buildWindowContent() {
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setSpacing(true);
                final LookupFolderField field = new LookupFolderField();
                verticalLayout.addComponent(field);
                BaseButton saveButton = new BaseButton($("save")) {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        String parentId = field.getValue();
                        try {
                            duplicateButtonClicked(parentId, param.getIds());
                        } catch (Throwable e) {
//                            LOGGER.warn("error when trying to modify folder parent to " + parentId, e);
//                            showErrorMessage("DisplayFolderView.parentFolderException");
                            e.printStackTrace();
                        }
                        getWindow().close();
                    }
                };
                saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout();
                hLayout.setSizeFull();
                hLayout.addComponent(saveButton);
                hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
                verticalLayout.addComponent(hLayout);
                return verticalLayout;
            }
        };
        moveInFolderButton.setEnabled(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
        moveInFolderButton.setVisible(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
        ((VerticalLayout) param.getComponent()).addComponent(moveInFolderButton);
    }

    public void addCheckInButton(final AvailableActionsParam param) {
        Button button = new Button("RMSelectionPanelExtension.checkInButton");
        if(!param.getIds().isEmpty()) {
            RecordVO documentVO = new RecordToVOBuilder().build(appLayerFactory.getModelLayerFactory().newRecordServices()
                    .getDocumentById(param.getIds().get(0)), RecordVO.VIEW_MODE.TABLE, getSessionContext());
            final UpdateContentVersionWindowImpl uploadWindow = new UpdateContentVersionWindowImpl(documentVO, documentVO.getMetadata(Document.CONTENT)) {
                @Override
                public void close() {
                    super.close();
//                presenter.updateWindowClosed();
                }
            };

            button.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    uploadWindow.open(true);
                }
            });
        }

        button.setEnabled(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE));
        button.setVisible(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE));
        ((VerticalLayout) param.getComponent()).addComponent(button);
    }

    protected SessionContext getSessionContext() {
        return ConstellioUI.getCurrentSessionContext();
    }

    public void parentFolderButtonClicked(String parentId, List<String> recordIds)
            throws RecordServicesException {

        if (isNotBlank(parentId)) {
            RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
            RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
            for(String id: recordIds) {
                try {
                    Record record = recordServices.getDocumentById(id);
                    switch (record.getTypeCode()) {
                        case Folder.SCHEMA_TYPE:
                            recordServices.update(rmSchemas.getFolder(id).setParentFolder(parentId));
                            break;
                        case Document.SCHEMA_TYPE:
                            recordServices.update(rmSchemas.getDocument(id).setFolder(parentId));
                    }
                } catch (RecordServicesException.ValidationException e) {
                    e.printStackTrace();
//                    view.showErrorMessage($(e.getErrors()));
                }
            }
        }
    }

    public void duplicateButtonClicked(String parentId, List<String> recordIds)
            throws RecordServicesException {

        if (isNotBlank(parentId)) {
            RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
            RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
            for(String id: recordIds) {
                try {
                    Record record = recordServices.getDocumentById(id);
                    switch (record.getTypeCode()) {
                        case Folder.SCHEMA_TYPE:
                            Folder newFolder = rmSchemas.newFolder();
                            for(Metadata metadata: rmSchemas.wrapFolder(record).getSchema().getMetadatas().onlyNonSystemReserved().onlyManuals().onlyDuplicable()) {
                                newFolder.set(metadata, record.get(metadata));
                            }
                            newFolder.setParentFolder(parentId);
                            recordServices.add(newFolder);
                            break;
                        case Document.SCHEMA_TYPE:
                            Document newDocument = rmSchemas.newDocument();
                            for(Metadata metadata: rmSchemas.wrapDocument(record).getSchema().getMetadatas().onlyNonSystemReserved().onlyManuals().onlyDuplicable()) {
                                newDocument.set(metadata, record.get(metadata));
                            }
                            newDocument.setFolder(parentId);
                            recordServices.add(newDocument);
                    }
                } catch (RecordServicesException.ValidationException e) {
                    e.printStackTrace();
//                    view.showErrorMessage($(e.getErrors()));
                }
            }
        }
    }

    protected boolean isCheckInPossible(AvailableActionsParam param, String id) {
        boolean email = isEmail(id);
        return !email && (getContent(id) != null && isCurrentUserBorrower(param, id));
    }

    private boolean isEmail(String id) {
        Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(id);
        return Email.SCHEMA.equals(record.getSchemaCode());
    }

    protected Content getContent(String id) {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(id);
        Document document = rm.wrapDocument(record);
        return document.getContent();
    }

    protected boolean isCurrentUserBorrower(AvailableActionsParam param, String id) {
        User currentUser = param.getUser();
        Content content = getContent(id);
        return content != null && currentUser.getId().equals(content.getCheckoutUserId());
    }
}
