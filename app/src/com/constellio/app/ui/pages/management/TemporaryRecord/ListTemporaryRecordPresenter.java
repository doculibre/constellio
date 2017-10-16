package com.constellio.app.ui.pages.management.TemporaryRecord;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;

public class ListTemporaryRecordPresenter extends BasePresenter<ListTemporaryRecordView> {

    public ListTemporaryRecordPresenter(ListTemporaryRecordView view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return user.hasAny(CorePermissions.ACCESS_TEMPORARY_RECORD, CorePermissions.SEE_ALL_TEMPORARY_RECORD).globally();
    }

    public RecordVODataProvider getDataProviderFromType(final String schema) {
        final MetadataSchemaType temporaryRecordSchemaType = types().getSchemaType(TemporaryRecord.SCHEMA_TYPE);
        MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(temporaryRecordSchemaType.getSchema(schema),
                RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
        return new RecordVODataProvider(
                schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {
                User user = view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection( view.getSessionContext().getCurrentUser().getUsername(), view.getCollection());
                OngoingLogicalSearchCondition FromCondition = from(temporaryRecordSchemaType.getSchema(schema));
                LogicalSearchCondition condition = user.has(CorePermissions.SEE_ALL_TEMPORARY_RECORD).globally() ? FromCondition.where(returnAll()) : FromCondition.where(Schemas.CREATED_BY).isEqualTo(user);
                return new LogicalSearchQuery().setCondition(condition).sortDesc(Schemas.CREATED_ON);
            }
        };
    }
}
