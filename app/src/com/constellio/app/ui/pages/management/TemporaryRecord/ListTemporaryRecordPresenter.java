package com.constellio.app.ui.pages.management.TemporaryRecord;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;

public class ListTemporaryRecordPresenter extends BasePresenter<ListTemporaryRecordView> {

    public ListTemporaryRecordPresenter(ListTemporaryRecordView view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    private RecordVODataProvider getDataProviderFromType(final String schema) {
        final MetadataSchemaType temporaryRecordSchemaType = types().getSchemaType(TemporaryRecord.SCHEMA_TYPE);
        MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(temporaryRecordSchemaType.getSchema(schema),
                RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
        return new RecordVODataProvider(
                schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {

                return new LogicalSearchQuery().setCondition(from(temporaryRecordSchemaType.getSchema(schema))
                        .returnAll())
                        .sortDesc(Schemas.CREATED_ON);
            }
        };
    }

    public RecordVODataProvider getExportDataProvider(){
        return getDataProviderFromType(TemporaryRecordType.EXPORT.getSchema());
    }

    public RecordVODataProvider getImportDataProvider(){
        return getDataProviderFromType(TemporaryRecordType.IMPORT.getSchema());
    }
}
