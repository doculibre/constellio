package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;


import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Marco on 2017-01-19.
 */
public class AddEditLabelPresenter extends SingleSchemaBasePresenter<AddEditLabelView> {
    private MetadataSchemaToVOBuilder schemaVOBuilder;
    private transient RMConfigs rmConfigs;
    private transient RMSchemasRecordsServices rmSchemasRecordsServices;
    private transient BorrowingServices borrowingServices;
    private transient MetadataSchemasManager metadataSchemasManager;
    private transient RecordServices recordServices;
    private transient ModelLayerCollectionExtensions extensions;
    protected RecordVO container;


    public AddEditLabelPresenter(AddEditLabelView view) {
        super(view);
        initTransientObjects();
    }

    public RecordVO getRecordVO(String id, RecordVO.VIEW_MODE mode) {
        LogicalSearchCondition condition = from(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableLabel.SCHEMA_NAME)).where(Schemas.IDENTIFIER).isEqualTo(id);
        Record r = searchServices().searchSingleResult(condition);
        RecordToVOBuilder voBuilder = new RecordToVOBuilder();
        return voBuilder.build(r, mode, view.getSessionContext());
    }

    private void initTransientObjects() {
        schemaVOBuilder = new MetadataSchemaToVOBuilder();
        rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
        borrowingServices = new BorrowingServices(collection, modelLayerFactory);
        metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
        recordServices = modelLayerFactory.newRecordServices();
        extensions = modelLayerFactory.getExtensions().forCollection(collection);
        rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public RecordVODataProvider getLabelFolderDataProvider() {
        final MetadataSchemaVO labelSchemaVo = schemaVOBuilder
                .build(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableLabel.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        return new RecordVODataProvider(labelSchemaVo, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {
                MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableLabel.SCHEMA_NAME);
                return new LogicalSearchQuery(
                        from(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableLabel.SCHEMA_NAME))
                                .where(schema.getMetadata(PrintableLabel.TYPE_LABEL)).isEqualTo(Folder.SCHEMA_TYPE));
            }
        };
    }

    public RecordVODataProvider getLabelContainerDataProvider() {
        final MetadataSchemaVO labelSchemaVo = schemaVOBuilder
                .build(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableLabel.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        return new RecordVODataProvider(labelSchemaVo, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {
                MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableLabel.SCHEMA_NAME);
                return new LogicalSearchQuery(
                        from(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableLabel.SCHEMA_NAME))
                                .where(schema.getMetadata(PrintableLabel.TYPE_LABEL)).isEqualTo(ContainerRecord.SCHEMA_TYPE));
            }
        };
    }

    public void addLabelButtonClicked() {
        view.navigate().to().addLabel();
    }

    public void saveButtonClicked(RecordVO rvo) throws Exception {
        RecordServices rs = appLayerFactory.getModelLayerFactory().newRecordServices();
        SchemaPresenterUtils utils = new SchemaPresenterUtils(PrintableLabel.SCHEMA_NAME, view.getConstellioFactories(), view.getSessionContext());
        Record record = utils.toRecord(rvo);
        record.set(metadataSchemasManager.getSchemaTypes(collection).getMetadata(PrintableLabel.SCHEMA_NAME + "_" + PrintableLabel.ISDELETABLE), true);
        Transaction trans = new Transaction();
        trans.update(record);
        rs.execute(trans);
        view.navigate().to().previousView();
    }

    public void cancelButtonClicked() {
        view.navigate().to().previousView();
    }

    public void customFieldValueChanged(CustomDocumentField<?> field) {

    }

    @Override
    protected Record newRecord() {
        super.setSchemaCode(PrintableLabel.SCHEMA_NAME);
        Record record = super.newRecord();
        PrintableLabel report = rmSchemasRecordsServices.wrapRMReport(record);
        return record;
    }

    public SchemaTypeVODataProvider getDataProvider() {
        return new SchemaTypeVODataProvider(new MetadataSchemaTypeToVOBuilder(), appLayerFactory, collection);
    }

    public void editButtonClicked(RecordVO record, String schema) {
        view.navigate().to().editLabel(record.getId());
    }

    public RecordVO getRecordsWithIndex(String schema, String itemId) {
        RecordVODataProvider dataProvider = schema.equals(Folder.SCHEMA_TYPE) ? this.getLabelFolderDataProvider() : this.getLabelContainerDataProvider();
        RecordVO records = dataProvider.getRecordVO(Integer.parseInt(itemId));
        return records;
    }

    public void displayButtonClicked(RecordVO record, String schema) {
        view.navigate().to().viewLabel(record.getId());
    }

    public void typeSelected(String type) {
        String newSchemaCode = getLinkedSchemaCodeOf(type);
        if (container.getSchema().getCode().equals(newSchemaCode)) {
            return;
        }
//        setSchemaCode(newSchemaCode);
//        container = copyMetadataToSchema(view.getUpdatedContainer(), newSchemaCode);
//        container.set(ContainerRecord.TYPE, type);
//        view.reloadWithContainer(container);
    }

    private String getLinkedSchemaCodeOf(String id) {
        String linkedSchemaCode;
        ContainerRecordType type = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory).getContainerRecordType(id);
        if (type == null || StringUtils.isBlank(type.getLinkedSchema())) {
            linkedSchemaCode = ContainerRecord.DEFAULT_SCHEMA;
        } else {
            linkedSchemaCode = type.getLinkedSchema();
        }
        return linkedSchemaCode;
    }

    public void removeRecord(String itemId, String schema) {
        SchemaPresenterUtils utils = new SchemaPresenterUtils(PrintableLabel.SCHEMA_NAME, view.getConstellioFactories(), view.getSessionContext());
        Record record = utils.toRecord(this.getRecordsWithIndex(schema, itemId));
        delete(record);
        view.navigate().to().manageLabels();
    }

    public void backButtonClicked() {
        view.navigate().to().previousView();
    }
}
