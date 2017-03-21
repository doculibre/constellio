package com.constellio.app.modules.rm.extensions;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;

import com.constellio.app.api.extensions.SelectionPanelExtension;
import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmlServiceRuntimeException;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.ui.pages.userDocuments.ListUserDocumentsView;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class RMSelectionPanelExtension extends SelectionPanelExtension {
    AppLayerFactory appLayerFactory;
    String collection;
    IOServices ioServices;

    public RMSelectionPanelExtension(AppLayerFactory appLayerFactory, String collection) {
        this.appLayerFactory = appLayerFactory;
        this.collection = collection;
        this.ioServices = this.appLayerFactory.getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().newIOServices();
    }

    @Override
    public void addAvailableActions(AvailableActionsParam param) {
        addMoveButton(param);
        addDuplicateButton(param);
        addClassifyButton(param);
        addCheckInButton(param);
        addSendEmailButton(param);
    }

    public void addMoveButton(final AvailableActionsParam param) {
        WindowButton moveInFolderButton = new WindowButton($("ConstellioHeader.selection.actions.moveInFolder"), $("ConstellioHeader.selection.actions.moveInFolder")
                , WindowButton.WindowConfiguration.modalDialog("50%", "220px")) {
            @Override
            protected Component buildWindowContent() {
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.addStyleName("no-scroll");
                verticalLayout.setSpacing(true);
                final LookupFolderField field = new LookupFolderField();
                field.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
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

                    @Override
                    public boolean isVisible() {
                        return containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE));
                    }

                    @Override
                    public boolean isEnabled() {
                        return isVisible();
                    }
                };
                saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout();
                hLayout.setSpacing(true);
                hLayout.setSizeFull();
                hLayout.addComponent(saveButton);
                hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
                verticalLayout.addComponent(hLayout);
                return verticalLayout;
            }
        };
        setStyles(moveInFolderButton);
        moveInFolderButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
        moveInFolderButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
        ((VerticalLayout) param.getComponent()).addComponent(moveInFolderButton);
    }


    public void addDuplicateButton(final AvailableActionsParam param) {
        WindowButton duplicateButton = new WindowButton($("ConstellioHeader.selection.actions.duplicate"), $("ConstellioHeader.selection.actions.duplicate")
                , WindowButton.WindowConfiguration.modalDialog("50%", "20%")) {
            @Override
            protected Component buildWindowContent() {
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setSpacing(true);
                final LookupFolderField field = new LookupFolderField();
                field.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
                verticalLayout.addComponent(field);
                BaseButton saveButton = new BaseButton($("save")) {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        String parentId = field.getValue();
                        duplicateButtonClicked(parentId, param);
                        getWindow().close();
                    }

                    @Override
                    public boolean isVisible() {
                        return containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE));
                    }

                    @Override
                    public boolean isEnabled() {
                        return isVisible();
                    }
                };
                saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout();
                hLayout.setSizeFull();
                hLayout.addComponent(saveButton);
                hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_CENTER);
                verticalLayout.addComponent(hLayout);
                return verticalLayout;
            }
        };
        setStyles(duplicateButton);
        duplicateButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
        duplicateButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)));
        ((VerticalLayout) param.getComponent()).addComponent(duplicateButton);
    }

    public void addClassifyButton(final AvailableActionsParam param) {
        WindowButton classifyButton = new WindowButton($("ConstellioHeader.selection.actions.classify"), $("ConstellioHeader.selection.actions.classify")
                , WindowButton.WindowConfiguration.modalDialog("50%", "220px")) {
            @Override
            protected Component buildWindowContent() {
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.addStyleName("no-scroll");
                verticalLayout.setSpacing(true);
                final LookupFolderField folderField = new LookupFolderField();
                folderField.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
                folderField.setVisible(true);
                final FolderCategoryFieldImpl categoryField = new FolderCategoryFieldImpl();
                categoryField.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
                categoryField.setVisible(false);
                final ListOptionGroup classificationOption = new ListOptionGroup($("ConstellioHeader.selection.actions.classificationChoice"), asList(true, false));
                classificationOption.addStyleName("horizontal");
                classificationOption.setNullSelectionAllowed(false);
                classificationOption.setItemCaption(true, $("ConstellioHeader.selection.actions.classifyInClassificationPlan"));
                classificationOption.setItemCaption(false, $("ConstellioHeader.selection.actions.classifyInFolder"));
                classificationOption.setValue(false);
                classificationOption.addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent event) {
                        folderField.setVisible(!Boolean.TRUE.equals(event.getProperty().getValue()));
                        categoryField.setVisible(Boolean.TRUE.equals(event.getProperty().getValue()));
                    }
                });
                classificationOption.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(UserFolder.SCHEMA_TYPE)));


                verticalLayout.addComponents(classificationOption, folderField, categoryField);
                BaseButton saveButton = new BaseButton($("save")) {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        String parentId = folderField.getValue();
                        String categoryId = categoryField.getValue();
                        boolean isClassifiedInFolder = !Boolean.TRUE.equals(classificationOption.getValue());
                        try {
                            classifyButtonClicked(parentId, categoryId, isClassifiedInFolder, param);
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
                hLayout.setSpacing(true);
                hLayout.setSizeFull();
                hLayout.addComponent(saveButton);
                hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_CENTER);
                verticalLayout.addComponent(hLayout);
                return verticalLayout;
            }

            @Override
            public boolean isVisible() {
                return containsOnly(param.getSchemaTypeCodes(), asList(UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE));
            }

            @Override
            public boolean isEnabled() {
                return isVisible();
            }
        };
        setStyles(classifyButton);
        classifyButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE)));
        classifyButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE)));
        ((VerticalLayout) param.getComponent()).addComponent(classifyButton);
    }

    public void addCheckInButton(final AvailableActionsParam param) {
        Button checkInButton = new Button($("ConstellioHeader.selection.actions.checkIn")) {
            @Override
            public boolean isVisible() {
                return containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE));
            }

            @Override
            public boolean isEnabled() {
                return isVisible();
            }
        };
        checkInButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(!param.getIds().isEmpty()) {
                    RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
                    Map<RecordVO, MetadataVO> records = new HashMap<>();
                    RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
                    for(String id: param.getIds()) {
                        Record record = recordServices.getDocumentById(id);
                        if(record.isOfSchemaType(Document.SCHEMA_TYPE)) {
                            if(isCheckInPossible(param, id)) {
                                RecordVO documentVo = recordToVOBuilder.build(appLayerFactory.getModelLayerFactory().newRecordServices()
                                        .getDocumentById(id), RecordVO.VIEW_MODE.TABLE, getSessionContext());
                                records.put(documentVo, documentVo.getMetadata(Document.CONTENT));
                            }
                        }
                    }
                    final int numberOfRecords = records.size();
                    if(numberOfRecords > 0) {
                        final UpdateContentVersionWindowImpl uploadWindow = new UpdateContentVersionWindowImpl(records) {
                            @Override
                            public void close() {
                                super.close();
                                showErrorMessage($("ConstellioHeader.selection.actions.noApplicableRecords"));
                            }
                        };
                        uploadWindow.open(true);
                    } else {
                        showErrorMessage($("ConstellioHeader.selection.actions.noApplicableRecords"));
                    }
                } else {
                    showErrorMessage($("ConstellioHeader.selection.actions.noApplicableRecords"));
                }
            }
        });

        setStyles(checkInButton);
        checkInButton.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
        checkInButton.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
        ((VerticalLayout) param.getComponent()).addComponent(checkInButton);
    }

    private void addSendEmailButton(final AvailableActionsParam param) {
        Button button = new Button($("ConstellioHeader.selection.actions.prepareEmail")) {
            @Override
            public boolean isVisible() {
                return containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE));
            }

            @Override
            public boolean isEnabled() {
                return isVisible();
            }
        };
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                emailPreparationRequested(param);
            }
        });
        setStyles(button);
        button.setEnabled(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
        button.setVisible(containsOnly(param.getSchemaTypeCodes(), asList(Document.SCHEMA_TYPE)));
        ((VerticalLayout) param.getComponent()).addComponent(button);
    }

    protected SessionContext getSessionContext() {
        return ConstellioUI.getCurrentSessionContext();
    }

    private DecommissioningService decommissioningService(AvailableActionsParam param) {
        return new DecommissioningService(param.getUser().getCollection(), appLayerFactory);
    }

    public void parentFolderButtonClicked(String parentId, List<String> recordIds)
            throws RecordServicesException {

        List<String> couldNotMove = new ArrayList<>();
        if (isNotBlank(parentId)) {
            RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
            RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
            for(String id: recordIds) {
                Record record = recordServices.getDocumentById(id);
                try {
                    switch (record.getTypeCode()) {
                        case Folder.SCHEMA_TYPE:
                            recordServices.update(rmSchemas.getFolder(id).setParentFolder(parentId));
                            break;
                        case Document.SCHEMA_TYPE:
                            recordServices.update(rmSchemas.getDocument(id).setFolder(parentId));
                            break;
                        default:
                            couldNotMove.add(record.getTitle());
                    }
                } catch (RecordServicesException.ValidationException e) {
                    couldNotMove.add(record.getTitle());
                }
            }
        }

        if(couldNotMove.isEmpty()) {
            showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted"));
        } else {
            showErrorMessage($("ConstellioHeader.selection.actions.couldNotMove"));
        }
    }

    public void duplicateButtonClicked(String parentId, AvailableActionsParam param) {
        List<String> recordIds = param.getIds();
        List<String> couldNotDuplicate = new ArrayList<>();
        if (isNotBlank(parentId)) {
            RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
            RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
            for(String id: recordIds) {
                Record record = recordServices.getDocumentById(id);
                try {
                    switch (record.getTypeCode()) {
                        case Folder.SCHEMA_TYPE:
                            Folder newFolder = decommissioningService(param).duplicateStructureAndDocuments(rmSchemas.wrapFolder(record), param.getUser(), false);
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
                            break;
                        default:
                            couldNotDuplicate.add(record.getTitle());
                    }
                } catch (RecordServicesException e) {
                    couldNotDuplicate.add(record.getTitle());
                }
            }
        }

        if(couldNotDuplicate.isEmpty()) {
            showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted"));
        } else {
            showErrorMessage($("ConstellioHeader.selection.actions.couldNotDuplicate"));
        }
    }

    public void classifyButtonClicked(String parentId, String categoryId, boolean isClassifiedInFolder, AvailableActionsParam param)
            throws RecordServicesException {

        List<String> recordIds = param.getIds();
        List<String> couldNotMove = new ArrayList<>();
        if ((isClassifiedInFolder && isNotBlank(parentId)) || (!isClassifiedInFolder && isNotBlank(categoryId))) {
            RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
            RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
            for (String id: recordIds) {
            	Record record = null;
                try {
                    record = recordServices.getDocumentById(id);
                    switch (record.getTypeCode()) {
                        case UserFolder.SCHEMA_TYPE:
                            Folder newFolder = rmSchemas.newFolder();
                            RMUserFolder userFolder = rmSchemas.wrapUserFolder(record);
                            if(!isClassifiedInFolder) {
                                classifyUserFolderInCategory(param, categoryId, userFolder);
                            }
                            decommissioningService(param).populateFolderFromUserFolder(newFolder, userFolder, param.getUser());
                            if(isClassifiedInFolder) {
                                newFolder.setParentFolder(parentId);
                            }
                            recordServices.add(newFolder);
                            decommissioningService(param).duplicateSubStructureAndSave(newFolder, userFolder, param.getUser());
                            deleteUserFolder(param, userFolder, param.getUser());
                            break;
                        case UserDocument.SCHEMA_TYPE:
                            Document newDocument = rmSchemas.newDocument();
                            UserDocument userDocument = rmSchemas.wrapUserDocument(record);
                            decommissioningService(param).populateDocumentFromUserDocument(newDocument, userDocument, param.getUser());
                            newDocument.setFolder(parentId);
                            recordServices.add(newDocument);
                            deleteUserDocument(param, rmSchemas.wrapUserDocument(record), param.getUser());
                            break;
                        default:
                            couldNotMove.add(record.getTitle());
                    }
                } catch (RecordServicesException e) {
                	if (record != null) {
                        couldNotMove.add(record.getTitle());
                	}
                    e.printStackTrace();
                } catch (IOException e) {
                	if (record != null) {
                        couldNotMove.add(record.getTitle());
                	}
                    e.printStackTrace();
                }
            }
        }

        if(couldNotMove.isEmpty()) {
            showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted"));
        } else {
            showErrorMessage($("ConstellioHeader.selection.actions.couldNotClassify"));
        }
    }

    protected void deleteUserFolder(AvailableActionsParam param, RMUserFolder rmUserFolder, User user) {
        decommissioningService(param).deleteUserFolder(rmUserFolder, user);
        refreshSelectionTables(param, rmUserFolder);
    }

    protected void deleteUserDocument(AvailableActionsParam param, UserDocument userDocument, User user) {
        decommissioningService(param).deleteUserDocument(userDocument, user);
        refreshSelectionTables(param, userDocument);
    }
    
    private void refreshSelectionTables(AvailableActionsParam param, RecordWrapper recordWrapper) {
    	String recordId = recordWrapper.getId();
        Collection<Window> windows = UI.getCurrent().getWindows();
        for (Window window : windows) {
            SelectionTableAdapter selectionTableAdapter = ComponentTreeUtils.getFirstChild(window, SelectionTableAdapter.class);
            if (selectionTableAdapter != null) {
            	try {
                    selectionTableAdapter.getTable().removeItem(recordId);
            	} catch (Throwable t) {
                	selectionTableAdapter.refresh();
            	}
            }
		}
        View currentView = ConstellioUI.getCurrent().getCurrentView();
        if (currentView instanceof ListUserDocumentsView) {
        	((ListUserDocumentsView) currentView).refresh();
        }
    }

    private void emailPreparationRequested(AvailableActionsParam param) {
        InputStream stream = createEml(param);
        startDownload(stream);
    }

    private InputStream createEml(AvailableActionsParam param) {
        File newTempFolder = null;
        try {
            newTempFolder = ioServices.newTemporaryFile("CartEmlService-emlFile");
            return createEml(param, newTempFolder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ioServices.deleteQuietly(newTempFolder);
        }
    }

    private InputStream createEml(AvailableActionsParam param, File emlFile) {
        try {
            OutputStream outputStream = new FileOutputStream(emlFile);
            User user = param.getUser();
            String signature = getSignature(user);
            String subject = "";
            String from = user.getEmail();
            List<EmailServices.MessageAttachment> attachments = getAttachments(param);
            if(attachments == null || attachments.isEmpty()) {
                showErrorMessage($("ConstellioHeader.selection.actions.noApplicableRecords"));
                return null;
            }
            Message message = new EmailServices().createMessage(from, subject, signature, attachments);
            message.addHeader("X-Unsent", "1");
            message.writeTo(outputStream);
            IOUtils.closeQuietly(outputStream);
            closeAllInputStreams(attachments);
            return new FileInputStream(emlFile);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSignature(User user) {
        return user.getSignature() != null ? user.getSignature() : user.getTitle();
    }

    private List<EmailServices.MessageAttachment> getAttachments(AvailableActionsParam param)
            throws IOException {
        //FIXME current version get only cart documents attachments
        List<EmailServices.MessageAttachment> returnList = new ArrayList<>();
        returnList.addAll(getDocumentsAttachments(param.getIds()));
        return returnList;
    }

    private List<EmailServices.MessageAttachment> getDocumentsAttachments(List<String> recordIds)
            throws IOException {
        List<EmailServices.MessageAttachment> returnList = new ArrayList<>();
        RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
        RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
        for (String currentDocumentId : recordIds) {
            Record record = recordServices.getDocumentById(currentDocumentId);
            if(record.isOfSchemaType(Document.SCHEMA_TYPE)) {
                try {
                    Document document = rmSchemasRecordsServices.wrapDocument(record);
                    if (document.getContent() != null) {
                        EmailServices.MessageAttachment contentFile = createAttachment(document.getContent());
                        returnList.add(contentFile);
                    }
                } catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
                    throw new CartEmlServiceRuntimeException.CartEmlServiceRuntimeException_InvalidRecordId(e);
                }
            }
        }
        return returnList;
    }

    private EmailServices.MessageAttachment createAttachment(Content content)
            throws IOException {
        String hash = content.getCurrentVersion().getHash();
        ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
        InputStream inputStream = contentManager.getContentInputStream(hash, content.getCurrentVersion().getFilename());
        String mimeType = content.getCurrentVersion().getMimetype();
        String attachmentName = content.getCurrentVersion().getFilename();
        return new EmailServices.MessageAttachment().setMimeType(mimeType).setAttachmentName(attachmentName).setInputStream(inputStream);
    }

    private void closeAllInputStreams(List<EmailServices.MessageAttachment> attachments) {
        for (EmailServices.MessageAttachment attachment : attachments) {
            ioServices.closeQuietly(attachment.getInputStream());
            IOUtils.closeQuietly(attachment.getInputStream());
        }
    }

    @SuppressWarnings("deprecation")
	private void startDownload(final InputStream stream) {
        Resource resource = new ReportViewer.DownloadStreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return stream;
            }
        }, "cart.eml");
        Page.getCurrent().open(resource, null, false);
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

    public void showErrorMessage(String errorMessage) {
        Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Notification.Type.WARNING_MESSAGE);
        notification.setHtmlContentAllowed(true);
        notification.show(Page.getCurrent());
    }

    public boolean containsOnly(List<String> list, List<String> values) {
        for(String value: list) {
            if(!values.contains(value)) {
                return false;
            }
        }
        return true && list.size() > 0;
    }

    public void classifyUserFolderInCategory(AvailableActionsParam param, String categoryId, RMUserFolder userFolder) {
        User currentUser = param.getUser();
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        Category category = rm.getCategory(categoryId);
        userFolder.setCategory(category);
        List<String> retentionRules = category.getRententionRules();
        if (!retentionRules.isEmpty()) {
            userFolder.setRetentionRule(retentionRules.get(0));
        }
        AdministrativeUnit administrativeUnit = getDefaultAdministrativeUnit(currentUser);
        userFolder.setAdministrativeUnit(administrativeUnit);
    }

    private AdministrativeUnit getDefaultAdministrativeUnit(User user) {
        String collection = user.getCollection();
        AdministrativeUnit defaultAdministrativeUnit;
        ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
        AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
        ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

        SearchServices searchServices = modelLayerFactory.newSearchServices();
        MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
        MetadataSchemaType administrativeUnitSchemaType = types.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
        LogicalSearchQuery visibleAdministrativeUnitsQuery = new LogicalSearchQuery();
        visibleAdministrativeUnitsQuery.filteredWithUserWrite(user);
        LogicalSearchCondition visibleAdministrativeUnitsCondition = from(administrativeUnitSchemaType).returnAll();
        visibleAdministrativeUnitsQuery.setCondition(visibleAdministrativeUnitsCondition);
        if (searchServices.getResultsCount(visibleAdministrativeUnitsQuery) > 0) {
            Record defaultAdministrativeUnitRecord = searchServices.search(visibleAdministrativeUnitsQuery).get(0);
            defaultAdministrativeUnit = rm.wrapAdministrativeUnit(defaultAdministrativeUnitRecord);
        } else {
            defaultAdministrativeUnit = null;
        }
        return defaultAdministrativeUnit;
    }
}
