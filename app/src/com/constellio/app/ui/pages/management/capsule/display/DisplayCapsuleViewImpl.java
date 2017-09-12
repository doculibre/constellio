package com.constellio.app.ui.pages.management.capsule.display;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class DisplayCapsuleViewImpl extends BaseViewImpl implements DisplayViewCapsule {

    private RecordVO recordVO;
    private DisplayCapsulePresenter presenter;

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        if (StringUtils.isNotEmpty(event.getParameters())) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            recordVO = presenter.getRecordVO(paramsMap.get("id"));
        }
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        return new RecordDisplay(recordVO);
    }
}
