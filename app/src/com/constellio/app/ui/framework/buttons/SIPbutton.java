package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.ConstellioSIP;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.data.intelligid.IntelliGIDSIPObjectsProvider;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.exceptions.SIPMaxReachedException;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.apache.commons.io.IOUtils;
import org.jdom2.JDOMException;
import org.joda.time.LocalDateTime;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class SIPbutton extends WindowButton implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver {


    private List<RecordVO> objectList = new ArrayList<>();
    private CheckBox deleteCheckBox, limitSizeCheckbox;
    private BaseView view;
    private IOServices ioServices;
    private File bagInfoFile;
    private AppLayerFactory factory;
    private String collection;
    private BaseUploadField upload;
    private RMSchemasRecordsServices rm;

    public SIPbutton(String caption, String windowCaption, BaseView view) {
        super(caption, windowCaption);
        this.view = view;
        if (this.view != null) {
            this.rm = new RMSchemasRecordsServices(view.getCollection(), view.getConstellioFactories().getAppLayerFactory());
            this.factory = this.view.getConstellioFactories().getAppLayerFactory();
            this.collection = this.view.getCollection();
            ioServices = view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getIOServicesFactory().newIOServices();
            User user = this.view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection(this.view.getSessionContext().getCurrentUser().getUsername(), this.view.getCollection());
            if (!user.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally()) {
                super.setVisible(false);
            }
        }
    }

    @Override
    protected Component buildWindowContent() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(buildCheckboxComponent());
        mainLayout.addComponent(buildUploadComponent());
        mainLayout.addComponent(buildButtonComponent());
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        mainLayout.setSpacing(true);
        return mainLayout;
    }

    private VerticalLayout buildCheckboxComponent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(buildDeleteItemCheckbox());
        layout.addComponent(buildLimitSizeComponent());
        return layout;
    }

    public void addAllObject(RecordVO... objects) {
        objectList.addAll(asList(objects));
    }

    private HorizontalLayout buildDeleteItemCheckbox() {
        HorizontalLayout layout = new HorizontalLayout();
        deleteCheckBox = new CheckBox($("SIPButton.deleteFilesLabel"));
        layout.addComponents(deleteCheckBox);
        layout.setWidth("100%");
        return layout;
    }

    private HorizontalLayout buildLimitSizeComponent() {
        HorizontalLayout layout = new HorizontalLayout();
        limitSizeCheckbox = new CheckBox($("SIPButton.limitSize"));
        layout.addComponents(limitSizeCheckbox);
        layout.setWidth("100%");
        return layout;
    }

    private HorizontalLayout buildUploadComponent() {
        HorizontalLayout layout = new HorizontalLayout();
        upload = new BaseUploadField(true, false);
        layout.addComponents(upload);
        layout.setWidth("100%");
        return layout;
    }

    private HorizontalLayout buildButtonComponent() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button cancelButton = new BaseButton($("cancel")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                getWindow().close();
            }
        };
        Button continueButton = new BaseButton($("ok")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                try {
                    continueButtonClicked();
                } catch (IOException | JDOMException | SIPMaxReachedException | RecordServicesException e) {
                    view.showErrorMessage(e.getMessage());
                }
            }
        };
        buttonLayout.addComponents(cancelButton, continueButton);
        buttonLayout.setWidth("100%");
        return buttonLayout;
    }

    private List<String> getDocumentIDListFromObjectList() {
        List<String> documents = new ArrayList<>();
        for (RecordVO recordVO : this.objectList) {
            if (recordVO.getSchema().getTypeCode().equals(Document.SCHEMA_TYPE)) {
                documents.add(recordVO.getId());
            }
        }
        return documents;
    }

    private List<String> getFolderIDListFromObjectList() {
        List<String> folders = new ArrayList<>();
        for (RecordVO recordVO : this.objectList) {
            if (recordVO.getSchema().getTypeCode().equals(Folder.SCHEMA_TYPE)) {
                folders.add(recordVO.getId());
            }
        }
        return folders;
    }

    private void continueButtonClicked() throws IOException, SIPMaxReachedException, JDOMException, RecordServicesException {
        File outFolder = ioServices.newTemporaryFolder("SIPArchives");
        String nomSipDossier = "sip-" + new LocalDateTime().toString("Y-M-d") + ".zip";
        final File outFile = new File(outFolder, nomSipDossier);
        InputStream bagInfoIn = new FileInputStream(((TempFileUpload) upload.getValue()).getTempFile());
        List<String> packageInfoLines = IOUtils.readLines(bagInfoIn);
        bagInfoIn.close();
        List<String> documentList = getDocumentIDListFromObjectList();
        List<String> folderList = getFolderIDListFromObjectList();
        SIPFilter filter = new SIPFilter(view.getCollection(), view.getConstellioFactories().getAppLayerFactory())
                .withIncludeDocumentIds(documentList)
                .withIncludeFolderIds(folderList);
        IntelliGIDSIPObjectsProvider metsObjectsProvider = new IntelliGIDSIPObjectsProvider(view.getCollection(), view.getConstellioFactories().getAppLayerFactory(), filter);
        if (!metsObjectsProvider.list().isEmpty()) {
            ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, packageInfoLines, limitSizeCheckbox.getValue());
            constellioSIP.build(outFile);
            SIParchive sipArchive = rm.newSIParchive();
            ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory();

            //Create SIParchive record
            ContentManager contentManager = modelLayerFactory.getContentManager();
            User currentUser = modelLayerFactory.newUserServices().getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), view.getCollection());
            ContentVersionDataSummary summary = contentManager.upload(outFile);
            sipArchive.setContent(contentManager.createMajor(currentUser, nomSipDossier, summary));
            User user = modelLayerFactory.newUserServices().getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), view.getCollection());
            sipArchive.setUser(user);
            sipArchive.setCreatedBy(user.getId());
            sipArchive.setCreationDate(new LocalDateTime());
            Transaction transaction = new Transaction();
            transaction.add(sipArchive);
            modelLayerFactory.newRecordServices().execute(transaction);

            //download ZIP
            FileResource res = new FileResource(outFile);
            Page.getCurrent().open(res, null, false);
        }
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {

    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        FileOutputStream fos = null; // Output stream to write to
        bagInfoFile = ioServices.newTemporaryFile(filename);
        try {
            // Open the file for writing.
            fos = new FileOutputStream(bagInfoFile);
        } catch (final java.io.FileNotFoundException e) {
            // Error while opening the file. Not reported here.
            e.printStackTrace();
            return null;
        }

        return fos; // Return the output stream to write to
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {

    }

    private StreamResource.StreamSource buildSource(final File file) {
        return new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                try{
                    return new FileInputStream(file);
                }catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}
