package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;

import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListExcelReportPresenter extends BasePresenter<ListExcelReportView> {

    private MetadataSchema reportSchema;
    private MetadataSchemaToVOBuilder schemaVOBuilder;

    public ListExcelReportPresenter(ListExcelReportView view) {
        super(view);
        reportSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(view.getCollection()).getSchema(Report.DEFAULT_SCHEMA);
        initTransientObjects();
    }

    private void initTransientObjects() {
        schemaVOBuilder = new MetadataSchemaToVOBuilder();
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return user.has(CorePermissions.MANAGE_EXCEL_REPORT).globally();
    }

    protected Map<String, String> initPossibleTab(){
        Map<String, String> map = new HashMap<>();
        MetadataSchemaToVOBuilder builder = new MetadataSchemaToVOBuilder();

        //container
        MetadataSchemaVO containerSchemaVO = builder.build(schemaType(ContainerRecord.SCHEMA_TYPE).getDefaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        map.put(containerSchemaVO.getLabel(), containerSchemaVO.getTypeCode());

        //Document
        MetadataSchemaVO documentSchemaVO = builder.build(schemaType(Document.SCHEMA_TYPE).getDefaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        map.put(documentSchemaVO.getLabel(), documentSchemaVO.getTypeCode());

        //internetDocument
        MetadataSchemaVO httpDocumentSchemaVO = builder.build(schemaType(ConnectorHttpDocument.SCHEMA_TYPE).getDefaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        map.put(httpDocumentSchemaVO.getLabel(), httpDocumentSchemaVO.getTypeCode());

        //smbConnector
        MetadataSchemaVO SmbDocumentSchemaVO = builder.build(schemaType(ConnectorSmbDocument.SCHEMA_TYPE).getDefaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        map.put(SmbDocumentSchemaVO.getLabel(), SmbDocumentSchemaVO.getTypeCode());

        //folder
        MetadataSchemaVO folderSchemaVO = builder.build(schemaType(Folder.SCHEMA_TYPE).getDefaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        map.put(folderSchemaVO.getLabel(), folderSchemaVO.getTypeCode());

        //StorageSpace
        MetadataSchemaVO storageSpaceSchemaVO = builder.build(schemaType(StorageSpace.SCHEMA_TYPE).getDefaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        map.put(storageSpaceSchemaVO.getLabel(), storageSpaceSchemaVO.getTypeCode());

        //Task
        MetadataSchemaVO taskSchemaVO = builder.build(schemaType(Task.SCHEMA_TYPE).getDefaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        map.put(taskSchemaVO.getLabel(), taskSchemaVO.getTypeCode());

        //User
        MetadataSchemaVO userSchemaVO = builder.build(schemaType(User.SCHEMA_TYPE).getDefaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        map.put(userSchemaVO.getLabel(), userSchemaVO.getTypeCode());
        map = sortByValue(map);
        return map;
    }

    public RecordVODataProvider getDataProviderForSchemaType(final String schemaType) {
        final MetadataSchemaVO reportVo = schemaVOBuilder.build(
                reportSchema,
                RecordVO.VIEW_MODE.TABLE,
                view.getSessionContext());
        return new RecordVODataProvider(reportVo, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {
                Metadata schemaMetadata = reportSchema.getMetadata(Report.SCHEMA_TYPE_CODE);
                LogicalSearchQuery query = new LogicalSearchQuery();
                query.setCondition(from(reportSchema)
                        .where(schemaMetadata).isEqualTo(schemaType));

                return query;
            }
        };
    }

    protected void editButtonClicked(String item, String schema){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("schemaTypeCode", schema);
        paramsMap.put("id", item);
        String params = ParamUtils.addParams(NavigatorConfigurationService.REPORT_DISPLAY_FORM, paramsMap);
        view.navigate().to().reportDisplayForm(params);
    }

    protected void removeRecord(String item, String schema){
        ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
        UserServices userServices = modelLayerFactory.newUserServices();
        reportServices.deleteReport(userServices.getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection), reportServices.getReportById(item));
        view.navigate().to().manageExcelReport();
    }

    protected void displayButtonClicked(String item, String schema){
        view.navigate().to().displayExcelReport(item);
    }

    public RecordVO getRecordsWithIndex(String schema, String itemIndex) {
        RecordVODataProvider dataProvider = this.getDataProviderForSchemaType(schema);
        return itemIndex == null ?  null : dataProvider.getRecordVO(Integer.parseInt(itemIndex));
    }

    //copy paste from stack overflow. credit: https://stackoverflow.com/a/2581754/5784924
    public static <K extends String, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return ( o1.getKey() ).compareTo( o2.getKey() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}
