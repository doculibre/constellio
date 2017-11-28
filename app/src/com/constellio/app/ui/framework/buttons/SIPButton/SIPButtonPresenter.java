package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class SIPButtonPresenter {
    private SIPbutton button;
    private AppLayerFactory factory;
    private String collection;
    private List<RecordVO> objectList;

    public SIPButtonPresenter(SIPbutton button, List<RecordVO> objectList) {
        this.button = button;
        this.objectList = objectList;
        factory = button.getView().getConstellioFactories().getAppLayerFactory();
        collection = button.getView().getCollection();
    }

    protected List<String> getDocumentIDListFromObjectList() {
        List<String> documents = new ArrayList<>();
        for (RecordVO recordVO : this.objectList) {
            if (recordVO.getSchema().getTypeCode().equals(Document.SCHEMA_TYPE)) {
                documents.add(recordVO.getId());
            }
        }
        return documents;
    }

    protected List<String> getFolderIDListFromObjectList() {
        List<String> folders = new ArrayList<>();
        for (RecordVO recordVO : this.objectList) {
            if (recordVO.getSchema().getTypeCode().equals(Folder.SCHEMA_TYPE)) {
                folders.add(recordVO.getId());
            }
        }
        return folders;
    }

    protected boolean validateFolderHasDocument() {
        SearchServices searchServices = factory.getModelLayerFactory().newSearchServices();
        MetadataSchemaType documentSchemaType = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(Document.SCHEMA_TYPE);
        for (String folderId : getFolderIDListFromObjectList()) {
            LogicalSearchCondition condition = LogicalSearchQueryOperators.from(documentSchemaType).where(documentSchemaType.getDefaultSchema().get(Document.FOLDER)).isEqualTo(folderId);
            if (searchServices.getResultsCount(new LogicalSearchQuery(condition)) > 0) {
                return true;
            }
        }
        return false;
    }

    protected boolean validateBagInfoLine(BagInfoVO object) {
        for (MetadataVO field : object.getFormMetadatas()) {
            if (!"".equals(object.get(field)) && object.get(field) != null) {
                return true;
            }
        }
        return false;
    }

    protected void saveButtonClick(BagInfoVO viewObject) {
        if (validateBagInfoLine(viewObject) && validateFolderHasDocument()) {
            String nomSipDossier = viewObject.getArchiveTitle() + ".zip";
            List<String> packageInfoLines = new ArrayList<>();
            for(MetadataVO metadatavo : viewObject.getFormMetadatas()) {
                Object value = viewObject.get(metadatavo);
                packageInfoLines.add(metadatavo.getLabel(this.button.getView().getSessionContext().getCurrentLocale()) + ":" +(value != null ? value : ""));
            }
            List<String> documentList = getDocumentIDListFromObjectList();
            List<String> folderList = getFolderIDListFromObjectList();
            SIPBuildAsyncTask task = new SIPBuildAsyncTask(nomSipDossier, packageInfoLines, documentList, folderList, viewObject.isLimitSize(), this.button.getView().getSessionContext().getCurrentUser().getUsername(), viewObject.isDeleteFile(), this.button.getView().getConstellioFactories().getAppLayerFactory().newApplicationService().getWarVersion());
            AsyncTaskBatchProcess asyncTaskBatchProcess = this.button.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(new AsyncTaskCreationRequest(task, this.button.getView().getCollection(), "SIPArchives"));
            this.button.showMessage($("SIPButton.SIPArchivesAddedToBatchProcess"));
            this.button.closeAllWindows();
            this.button.navigate().to().listTemporaryRecord();
        } else {
            this.button.showErrorMessage($("SIPButton.atLeastOneBagInfoLineMustBeThere"));
        }
    }
}
