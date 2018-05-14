package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMFolderSummaryColumnPresenter extends SingleSchemaBasePresenter<RMFolderSummaryColumnView>  {

    private static final String SCHEMA_CODE = "schemaCode";
    private String schemaCode;

    public RMFolderSummaryColumnPresenter(RMFolderSummaryColumnView view) {
        super(view);
    }

    public void forParams(String params) {
        Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
        this.schemaCode = paramsMap.get(SCHEMA_CODE);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }
}
