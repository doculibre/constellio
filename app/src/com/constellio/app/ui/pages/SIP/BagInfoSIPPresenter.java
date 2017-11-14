package com.constellio.app.ui.pages.SIP;

import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.BagInfoToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

public class BagInfoSIPPresenter extends BasePresenter<BagInfoSIPForm> {

    SearchServices searchServices;
    SchemaPresenterUtils presenterUtils;
    BagInfoToVOBuilder builder;
    private List<BagInfoVO> bagInfoVOS;

    public BagInfoSIPPresenter(BagInfoSIPForm view) {
        super(view);
        searchServices = searchServices();
        builder = new BagInfoToVOBuilder();
        presenterUtils = new SchemaPresenterUtils(BagInfo.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
    }

    protected List<BagInfoVO> getAllBagInfo() {
        if(null == bagInfoVOS) {
            bagInfoVOS = new ArrayList<>();
            BagInfoToVOBuilder builder = new BagInfoToVOBuilder();
            MetadataSchema bagInfoDefaultSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(BagInfo.SCHEMA_TYPE).getDefaultSchema();
            LogicalSearchCondition condition = LogicalSearchQueryOperators.from(bagInfoDefaultSchema).where(LogicalSearchQueryOperators.returnAll());
            List<Record> records = searchServices.cachedSearch(new LogicalSearchQuery(condition));
            for(Record record : records){
                bagInfoVOS.add(builder.build(record, RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext()));
            }
        }
        return bagInfoVOS;
    }
    protected BagInfoVO newRecord() {
        return builder.build(presenterUtils.newRecord(), RecordVO.VIEW_MODE.FORM, view.getSessionContext());
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }
}
