package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.SIPForm;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.jdom2.JDOMException;
import org.joda.time.LocalDateTime;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class SIPbutton extends WindowButton implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver, Component {


    private List<RecordVO> objectList = new ArrayList<>();
    private CheckBox deleteCheckBox, limitSizeCheckbox;
    private ConstellioHeader view;
    private IOServices ioServices;
    private File bagInfoFile;
    private AppLayerFactory factory;
    private String collection;
    private BaseUploadField upload;
    private RMSchemasRecordsServices rm;
    private BeanFieldGroup<BagInfoVO> binder;

    private BagInfoVO bagInfoVO;

    private FormLayout formLayout;

    private TextArea noteTextArea, descriptionSommaire;

    private TextField
            identificationOrganismeTextField,
            IDOrganismeTextField,
            adresseTextField,
            regionAdministrativeTextField,
            entiteResponsableTextField,
            identificationEntiteResponsableTextField,
            courrielResponsableTextField,
            telephoneResponsableTextField,
            categoryDocumentTextField,
            methodeTransfereTextField,
            restrictionAccessibiliteTextField,
            encodingTextField;

    public SIPbutton(String caption, String windowCaption, ConstellioHeader view) {
        super(caption, windowCaption, new WindowConfiguration(true, true, "75%", "75%"));
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
        SIPForm form = new SIPForm(build());
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
                } catch (IOException | JDOMException | RecordServicesException e) {
                    view.getCurrentView().showErrorMessage(e.getMessage());
                }
            }
        };
        form.getFooter().addComponents(cancelButton, continueButton);
        form.getFooter().setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);
        form.getFooter().setComponentAlignment(continueButton, Alignment.MIDDLE_RIGHT);
        mainLayout.addComponent(form);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        return mainLayout;
    }

    public void addAllObject(RecordVO... objects) {
        objectList.addAll(asList(objects));
    }

    public void setAllObject(RecordVO... objects) {
        objectList = new ArrayList<>();
        objectList.addAll(asList(objects));
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

    private void continueButtonClicked() throws IOException, JDOMException, RecordServicesException {
        String nomSipDossier = "sip-" + new LocalDateTime().toString("Y-M-d") + ".zip";
        if(validateBagInfoLines()){
            List<String> packageInfoLines = asList(
                    $("BagInfoForm.note") + ":" + noteTextArea.getValue(),
                    $("BagInfoForm.identificationOrganisme") + ":" + identificationOrganismeTextField.getValue(),
                    $("BagInfoForm.IDOrganisme") + ":" + IDOrganismeTextField.getValue(),
                    $("BagInfoForm.address") + ":" + adresseTextField.getValue(),
                    $("BagInfoForm.regionAdministrative") + ":" + regionAdministrativeTextField.getValue(),
                    $("BagInfoForm.entiteResponsable") + ":" + entiteResponsableTextField.getValue(),
                    $("BagInfoForm.identificationEntiteResponsable") + ":" + identificationEntiteResponsableTextField.getValue(),
                    $("BagInfoForm.courrielResponsable") + ":" + courrielResponsableTextField.getValue(),
                    $("BagInfoForm.telephoneResponsable") + ":" + telephoneResponsableTextField.getValue(),
                    $("BagInfoForm.descriptionSommaire") + ":" + descriptionSommaire.getValue(),
                    $("BagInfoForm.categoryDocument") + ":" + categoryDocumentTextField.getValue(),
                    $("BagInfoForm.methodeTransfere") + ":" + methodeTransfereTextField.getValue(),
                    $("BagInfoForm.restrictionAccessibilite") + ":" + restrictionAccessibiliteTextField.getValue(),
                    $("BagInfoForm.encoding") + ":" + encodingTextField.getValue());
            List<String> documentList = getDocumentIDListFromObjectList();
            List<String> folderList = getFolderIDListFromObjectList();
            if(!documentList.isEmpty() || !folderList.isEmpty()) {
                if(!documentList.isEmpty() || validateFolderHasDocument()){
                    SIPBuildAsyncTask task = new SIPBuildAsyncTask(nomSipDossier, packageInfoLines, documentList, folderList, this.limitSizeCheckbox.getValue(), view.getSessionContext().getCurrentUser().getUsername(), this.deleteCheckBox.getValue(), view.getConstellioFactories().getAppLayerFactory().newApplicationService().getWarVersion());
                    view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(new AsyncTaskCreationRequest(task, view.getCollection(), "SIPArchives"));
                    view.getCurrentView().showMessage($("SIPButton.SIPArchivesAddedToBatchProcess"));
                    getWindow().close();
                } else {
                    view.getCurrentView().showErrorMessage($("SIPButton.cannotCreateSIPArchivesFromEmptyFolder"));
                }
            } else {
                view.getCurrentView().showErrorMessage($("SIPButton.thereMustBeAtleastOneElement"));
            }
        } else {
            view.getCurrentView().showErrorMessage($("SIPButton.atLeastOneBagInfoLineMustBeThere"));
        }
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {}

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
    public void uploadSucceeded(Upload.SucceededEvent event) {}

    public FormLayout build(){
        bagInfoVO = new BagInfoVO();
        formLayout = new FormLayout();
        binder = new BeanFieldGroup<>(BagInfoVO.class);
        binder.setItemDataSource(bagInfoVO);

        deleteCheckBox = new CheckBox($("SIPButton.deleteFilesLabel"));
        limitSizeCheckbox = new CheckBox($("SIPButton.limitSize"));
        deleteCheckBox.setWidth("100%");
        limitSizeCheckbox.setWidth("100%");
        formLayout.addComponent(deleteCheckBox);
        formLayout.addComponent(limitSizeCheckbox);

        noteTextArea = new TextArea($("BagInfoForm.note"));
        formLayout.addComponent(noteTextArea);

        identificationOrganismeTextField = new TextField($("BagInfoForm.identificationOrganisme"));
        formLayout.addComponent(identificationOrganismeTextField);

        IDOrganismeTextField = new TextField($("BagInfoForm.IDOrganisme"));
        formLayout.addComponent(IDOrganismeTextField);

        adresseTextField = new TextField($("BagInfoForm.address"));
        formLayout.addComponent(adresseTextField);

        regionAdministrativeTextField = new TextField($("BagInfoForm.regionAdministrative"));
        formLayout.addComponent(regionAdministrativeTextField);

        entiteResponsableTextField = new TextField($("BagInfoForm.entiteResponsable"));
        formLayout.addComponent(entiteResponsableTextField);

        identificationEntiteResponsableTextField = new TextField($("BagInfoForm.identificationEntiteResponsable"));
        formLayout.addComponent(identificationEntiteResponsableTextField);

        courrielResponsableTextField = new TextField($("BagInfoForm.courrielResponsable"));
        formLayout.addComponent(courrielResponsableTextField);

        telephoneResponsableTextField = new TextField($("BagInfoForm.telephoneResponsable"));
        formLayout.addComponent(telephoneResponsableTextField);

        descriptionSommaire = new TextArea($("BagInfoForm.descriptionSommaire"));
        formLayout.addComponent(descriptionSommaire);

        categoryDocumentTextField = new TextField($("BagInfoForm.categoryDocument"));
        formLayout.addComponent(categoryDocumentTextField);

        methodeTransfereTextField = new TextField($("BagInfoForm.methodeTransfere"));
        formLayout.addComponent(methodeTransfereTextField);

        restrictionAccessibiliteTextField = new TextField($("BagInfoForm.restrictionAccessibilite"));
        formLayout.addComponent(restrictionAccessibiliteTextField);

        encodingTextField = new TextField($("BagInfoForm.encoding"));
        formLayout.addComponent(encodingTextField);
        return formLayout;
    }

    private boolean validateBagInfoLines(){
        List<String> lines = asList(noteTextArea.getValue(),
                identificationOrganismeTextField.getValue(),
                IDOrganismeTextField.getValue(),
                adresseTextField.getValue(),
                regionAdministrativeTextField.getValue(),
                entiteResponsableTextField.getValue(),
                identificationEntiteResponsableTextField.getValue(),
                courrielResponsableTextField.getValue(),
                telephoneResponsableTextField.getValue(),
                descriptionSommaire.getValue(),
                categoryDocumentTextField.getValue(),
                methodeTransfereTextField.getValue(),
                restrictionAccessibiliteTextField.getValue(),
                encodingTextField.getValue());
        for(String line : lines) {
            if(!line.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean validateFolderHasDocument(){
        SearchServices searchServices = factory.getModelLayerFactory().newSearchServices();
        MetadataSchemaType documentSchemaType = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(Document.SCHEMA_TYPE);
        for(String folderId : getFolderIDListFromObjectList()) {
            Folder folder = rm.getFolder(folderId);
            LogicalSearchCondition condition = LogicalSearchQueryOperators.from(documentSchemaType).where(Schemas.PRINCIPAL_PATH).isStartingWithText(folder.<String>get(Schemas.PRINCIPAL_PATH));
            if(searchServices.getResultsCount(new LogicalSearchQuery(condition)) > 0) {
                return true;
            }
        }
        return false;
    }
}
