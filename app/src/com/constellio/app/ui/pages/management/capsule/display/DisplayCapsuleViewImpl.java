package com.constellio.app.ui.pages.management.capsule.display;

import static com.constellio.app.ui.i18n.i18n.$;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class DisplayCapsuleViewImpl extends BaseViewImpl implements DisplayCapsuleView {

    private RecordVO recordVO;
    private DisplayCapsulePresenter presenter;

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new DisplayCapsulePresenter(this);
        if (StringUtils.isNotEmpty(event.getParameters())) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            recordVO = presenter.getRecordVO(paramsMap.get("id"));
        }
    }

    @Override
    protected Button.ClickListener getBackButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
               navigateTo().previousView();
            }
        };
    }

    @Override
    protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
        return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        return new RecordDisplay(recordVO);
    }

	@Override
	protected String getTitle() {
		return $("DisplayCapsuleView.viewTitle");
	}
}
