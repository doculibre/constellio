package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListExcelReportPresenter extends BasePresenter<ListExcelReportView>{

    public ListExcelReportPresenter(ListExcelReportView view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return user.has(CorePermissions.MANAGE_EXCEL_REPORT).globally();
    }

    private RecordVODataProvider getDataProviderForSchemaType(final String schemaType){
        final MetadataSchemaVO printableReportVO =  schemaVOBuilder.build(
                modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.SCHEMA_NAME),
                RecordVO.VIEW_MODE.TABLE,
                view.getSessionContext());
        return new RecordVODataProvider(printableReportVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {
                MetadataSchema metadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.SCHEMA_NAME);
                return schemaType == null ? null : new LogicalSearchQuery(from(metadataSchema).where(metadataSchema.getMetadata(PrintableReport.RECORD_TYPE)).isEqualTo(schemaType.toUpperCase()));
            }
        };
    }
}
