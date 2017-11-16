package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.SIPForm;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.pages.SIP.BagInfoSIPForm;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.frameworks.validation.ValidationException;
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

public class SIPbutton extends WindowButton {


    private List<RecordVO> objectList = new ArrayList<>();

    private ConstellioHeader view;
    private AppLayerFactory factory;
    private String collection;

    public SIPbutton(String caption, String windowCaption, ConstellioHeader view) {
        super(caption, windowCaption, new WindowConfiguration(true, true, "75%", "75%"));
        this.view = view;
        if (this.view != null) {
            this.factory = this.view.getConstellioFactories().getAppLayerFactory();
            this.collection = this.view.getCollection();
            User user = this.view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection(this.view.getSessionContext().getCurrentUser().getUsername(), this.view.getCollection());
            if (!user.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally()) {
                super.setVisible(false);
            }
        }
    }

    @Override
    protected Component buildWindowContent() {
        return new BagInfoSIPForm(){
            @Override
            protected void saveButtonClick(BagInfoVO viewObject) throws ValidationException {
                if(validateBagInfoLine(viewObject) && validateFolderHasDocument()) {
                    String nomSipDossier = "sip-" + new LocalDateTime().toString("Y-M-d") + ".zip";
                    List<String> packageInfoLines = asList(
                            $("BagInfoForm.note") + ":" + viewObject.getNote(),
                            $("BagInfoForm.identificationOrganisme") + ":" + viewObject.getIdentificationOrganismeVerseurOuDonateur(),
                            $("BagInfoForm.IDOrganisme") + ":" + viewObject.getIDOrganismeVerseurOuDonateur(),
                            $("BagInfoForm.address") + ":" + viewObject.getAddress(),
                            $("BagInfoForm.regionAdministrative") + ":" + viewObject.getRegionAdministrative(),
                            $("BagInfoForm.entiteResponsable") + ":" + viewObject.getEntiteResponsable(),
                            $("BagInfoForm.identificationEntiteResponsable") + ":" + viewObject.getIdentificationEntiteResponsable(),
                            $("BagInfoForm.courrielResponsable") + ":" + viewObject.getCourrielResponsable(),
                            $("BagInfoForm.telephoneResponsable") + ":" + viewObject.getTelephoneResponsable(),
                            $("BagInfoForm.descriptionSommaire") + ":" + viewObject.getDescriptionSommaire(),
                            $("BagInfoForm.categoryDocument") + ":" + viewObject.getCategoryDocument(),
                            $("BagInfoForm.methodeTransfere") + ":" + viewObject.getMethodeTransfere(),
                            $("BagInfoForm.restrictionAccessibilite") + ":" + viewObject.getRestrictionAccessibilite(),
                            $("BagInfoForm.encoding") + ":" + viewObject.getEncoding());
                    List<String> documentList = getDocumentIDListFromObjectList();
                    List<String> folderList = getFolderIDListFromObjectList();
                    SIPBuildAsyncTask task = new SIPBuildAsyncTask(nomSipDossier, packageInfoLines, documentList, folderList, viewObject.isLimitSize(), view.getSessionContext().getCurrentUser().getUsername(), viewObject.isDeleteFile(), view.getConstellioFactories().getAppLayerFactory().newApplicationService().getWarVersion());
                    AsyncTaskBatchProcess asyncTaskBatchProcess = view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(new AsyncTaskCreationRequest(task, view.getCollection(), "SIPArchives"));
                    showMessage($("SIPButton.SIPArchivesAddedToBatchProcess"));
                    closeAllWindows();
                    navigate().to().displaySIPProgression(asyncTaskBatchProcess.getId());
                } else {
                    showErrorMessage($("SIPButton.atLeastOneBagInfoLineMustBeThere"));
                }
            }
        };
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

    private boolean validateBagInfoLine(BagInfoVO object) {
        for(MetadataVO field : object.getFormMetadatas()) {
            if(!"".equals(object.get(field))) {
                return true;
            }
        }
        return false;
    }
}
