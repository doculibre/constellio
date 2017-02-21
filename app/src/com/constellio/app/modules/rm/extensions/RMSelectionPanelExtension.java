package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.SelectionPanelExtension;
import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.cart.CartEmlServiceRuntimeException;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.io.IOUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.apache.commons.lang.StringUtils.isNotBlank;

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
        setStyles(moveInFolderButton);
        moveInFolderButton.setEnabled(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
//        moveInFolderButton.setVisible(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
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
        setStyles(duplicateButton);
        duplicateButton.setEnabled(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
//        duplicateButton.setVisible(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
        ((VerticalLayout) param.getComponent()).addComponent(duplicateButton);
    }

    public void addClassifyButton(final AvailableActionsParam param) {
        WindowButton classifyButton = new WindowButton($("ConstellioHeader.selection.actions.classify"), $("ConstellioHeader.selection.actions.classify")
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
                        try {
                            classifyButtonClicked(parentId, param);
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
        setStyles(classifyButton);
        classifyButton.setEnabled(param.getSchemaTypeCodes().contains(UserDocument.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(UserFolder.SCHEMA_TYPE));
//        moveInFolderButton.setVisible(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE) || param.getSchemaTypeCodes().contains(Folder.SCHEMA_TYPE));
        ((VerticalLayout) param.getComponent()).addComponent(classifyButton);
    }

    public void addCheckInButton(final AvailableActionsParam param) {
        Button checkInButton = new Button($("ConstellioHeader.selection.actions.checkIn"));
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
        checkInButton.setEnabled(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE));
//        checkInButton.setVisible(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE));
        ((VerticalLayout) param.getComponent()).addComponent(checkInButton);
    }

    private void addSendEmailButton(final AvailableActionsParam param) {
        Button button = new Button($("ConstellioHeader.selection.actions.prepareEmail"));
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                emailPreparationRequested(param);
            }
        });
        setStyles(button);
        button.setEnabled(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE));
//        button.setVisible(param.getSchemaTypeCodes().contains(Document.SCHEMA_TYPE));
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

    public void classifyButtonClicked(String parentId, AvailableActionsParam param)
            throws RecordServicesException {

        List<String> recordIds = param.getIds();
        List<String> couldNotMove = new ArrayList<>();
        if (isNotBlank(parentId)) {
            RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
            RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
            for(String id: recordIds) {
                Record record = recordServices.getDocumentById(id);
                try {
                    switch (record.getTypeCode()) {
                        case UserFolder.SCHEMA_TYPE:
                            Folder newFolder = rmSchemas.newFolder();
                            decommissioningService(param).populateFolderFromUserFolder(newFolder, rmSchemas.wrapUserFolder(record), param.getUser());
                            newFolder.setParentFolder(parentId);
                            recordServices.add(newFolder);
                            decommissioningService(param).duplicateStructureAndDocuments(rmSchemas.wrapFolder(record), param.getUser(), true);
                            break;
                        case UserDocument.SCHEMA_TYPE:
                            Document newDocument = rmSchemas.newDocument();
                            decommissioningService(param).populateDocumentFromUserDocument(newDocument, rmSchemas.wrapUserDocument(record), param.getUser());
                            newDocument.setFolder(parentId);
                            recordServices.add(newDocument);
                            break;
                        default:
                            couldNotMove.add(record.getTitle());
                    }
                } catch (RecordServicesException e) {
                    couldNotMove.add(record.getTitle());
                } catch (IOException e) {
                    couldNotMove.add(record.getTitle());
                }
            }
        }

        if(couldNotMove.isEmpty()) {
            showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted"));
        } else {
            showErrorMessage($("ConstellioHeader.selection.actions.couldNotClassify"));
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
}
