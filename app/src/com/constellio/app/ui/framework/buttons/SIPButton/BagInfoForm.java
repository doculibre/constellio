package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import org.joda.time.LocalDateTime;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class BagInfoForm extends BaseForm<BagInfoVO> {
    private List<RecordVO> objectList = new ArrayList<>();
    private BaseViewImpl button;
    public BagInfoForm(BagInfoVO bagInfoVO, List<RecordVO> objectList, BaseViewImpl button, Field<?>... fields){
        super(bagInfoVO, button, fields);
        this.objectList = objectList;
        this.button = button;
    }

    @Override
    protected void saveButtonClick(BagInfoVO viewObject) throws ValidationException {
        if(validateBagInfoLine()) {
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
                    $("BagInfoForm.restrictionAccessibilite") + ":" + viewObject.getRestrictionAccessibilite());
// TODO check if necessary                   $("BagInfoForm.encoding") + ":" + encodingTextField.getValue());
            List<String> documentList = getDocumentIDListFromObjectList();
            List<String> folderList = getFolderIDListFromObjectList();
            SIPBuildAsyncTask task = new SIPBuildAsyncTask(nomSipDossier, packageInfoLines, documentList, folderList, viewObject.isLimitSize(), button.getSessionContext().getCurrentUser().getUsername(), viewObject.isDeleteFile(), button.getConstellioFactories().getAppLayerFactory().newApplicationService().getWarVersion());
            button.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(new AsyncTaskCreationRequest(task, button.getCollection(), "SIPArchives"));
            button.showMessage($("SIPButton.SIPArchivesAddedToBatchProcess"));
        } else {
            showErrorMessage($("SIPButton.atLeastOneBagInfoLineMustBeThere"));
        }
    }

    @Override
    protected void cancelButtonClick(BagInfoVO viewObject) {

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

    private boolean validateBagInfoLine() {
        for(Field<?> field : getFields()) {
            if(field.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
