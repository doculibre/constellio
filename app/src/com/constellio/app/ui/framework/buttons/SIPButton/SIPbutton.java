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
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.SIPForm;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
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

    @PropertyId("deleteFile")
    private CheckBox deleteCheckBox;
    @PropertyId("limitSize")
    private CheckBox limitSizeCheckbox;

    @PropertyId("note")
    private TextArea noteTextArea;

    @PropertyId("descriptionSommaire")
    private TextArea descriptionSommaire;

    @PropertyId("identificationOrganismeVerseurOuDonateur")
    private TextField identificationOrganismeTextField;

    @PropertyId("IDOrganismeVerseurOuDonateur")
    private TextField IDOrganismeTextField;

    @PropertyId("address")
    private TextField adresseTextField;

    @PropertyId("regionAdministrative")
    private TextField regionAdministrativeTextField;

    @PropertyId("entiteResponsable")
    private TextField entiteResponsableTextField;

    @PropertyId("identificationEntiteResponsable")
    private TextField identificationEntiteResponsableTextField;

    @PropertyId("courrielResponsable")
    private TextField courrielResponsableTextField;

    @PropertyId("telephoneResponsable")
    private TextField telephoneResponsableTextField;

    @PropertyId("categoryDocument")
    private TextField categoryDocumentTextField;

    @PropertyId("methodTransfere")
    private TextField methodeTransfereTextField;

    @PropertyId("restrictionAccessibilite")
    private TextField restrictionAccessibiliteTextField;

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
        return build();
    }

    public ConstellioHeader getView() {
        return view;
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
                    $("BagInfoForm.encoding") + ": UTF-8");
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

    public BaseForm build(){
        bagInfoVO = new BagInfoVO();
        limitSizeCheckbox = new CheckBox($("SIPButton.limitSize"));
        limitSizeCheckbox.setId("limitSize");

        deleteCheckBox = new CheckBox($("SIPButton.deleteFilesLabel"));
        deleteCheckBox.setId("deleteFile");

        identificationOrganismeTextField = new TextField($("BagInfoForm.identificationOrganisme"));
        identificationOrganismeTextField.setId("identificationOrganismeVerseurOuDonateur");
        identificationOrganismeTextField.setNullRepresentation("");

        IDOrganismeTextField = new TextField($("BagInfoForm.IDOrganisme"));
        IDOrganismeTextField.setId("IDOrganismeVerseurOuDonateur");
        IDOrganismeTextField.setNullRepresentation("");

        adresseTextField = new TextField($("BagInfoForm.address"));
        adresseTextField.setId("address");
        adresseTextField.setNullRepresentation("");

        regionAdministrativeTextField = new TextField($("BagInfoForm.regionAdministrative"));
        regionAdministrativeTextField.setId("regionAdministrative");
        regionAdministrativeTextField.setNullRepresentation("");

        entiteResponsableTextField = new TextField($("BagInfoForm.entiteResponsable"));
        entiteResponsableTextField.setId("entiteResponsable");
        entiteResponsableTextField.setNullRepresentation("");

        identificationEntiteResponsableTextField = new TextField($("BagInfoForm.identificationEntiteResponsable"));
        identificationEntiteResponsableTextField.setId("identificationEntiteResponsable");
        identificationEntiteResponsableTextField.setNullRepresentation("");

        courrielResponsableTextField = new TextField($("BagInfoForm.courrielResponsable"));
        courrielResponsableTextField.setId("courrielResponsable");
        courrielResponsableTextField.setNullRepresentation("");

        telephoneResponsableTextField = new TextField($("BagInfoForm.telephoneResponsable"));
        telephoneResponsableTextField.setId("telephoneResponsable");
        telephoneResponsableTextField.setNullRepresentation("");

        categoryDocumentTextField = new TextField($("BagInfoForm.categoryDocument"));
        categoryDocumentTextField.setId("categoryDocument");
        categoryDocumentTextField.setNullRepresentation("");

        methodeTransfereTextField = new TextField($("BagInfoForm.methodeTransfere"));
        methodeTransfereTextField.setId("methodeTransfere");
        methodeTransfereTextField.setNullRepresentation("");

        restrictionAccessibiliteTextField = new TextField($("BagInfoForm.restrictionAccessibilite"));
        restrictionAccessibiliteTextField.setId("restrictionAccessibilite");
        restrictionAccessibiliteTextField.setNullRepresentation("");

        noteTextArea = new TextArea($("BagInfoForm.note"));
        noteTextArea.setId("note");
        noteTextArea.setNullRepresentation("");

        descriptionSommaire = new TextArea($("BagInfoForm.descriptionSommaire"));
        descriptionSommaire.setId("descriptionSommaire");
        descriptionSommaire.setNullRepresentation("");

        return new BagInfoForm(bagInfoVO, this.objectList, this,
                limitSizeCheckbox,
                deleteCheckBox,
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
                noteTextArea,
                descriptionSommaire);
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
                restrictionAccessibiliteTextField.getValue());
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
            LogicalSearchCondition condition = LogicalSearchQueryOperators.from(documentSchemaType).where(documentSchemaType.getDefaultSchema().get(Document.FOLDER)).isEqualTo(folderId);
            if(searchServices.getResultsCount(new LogicalSearchQuery(condition)) > 0) {
                return true;
            }
        }
        return false;
    }
}
