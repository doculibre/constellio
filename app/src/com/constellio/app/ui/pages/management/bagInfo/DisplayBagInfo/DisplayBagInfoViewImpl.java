package com.constellio.app.ui.pages.management.bagInfo.DisplayBagInfo;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayBagInfoViewImpl extends BaseViewImpl implements DisplayBagInfoView {
	public RecordVO recordVO;
	public DisplayBagInfoPresenter presenter;

	@Override
	protected String getTitle() {
		return $("DisplayBagInfoViewImpl.title");
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter = new DisplayBagInfoPresenter(this);
		if (StringUtils.isNotEmpty(event.getParameters())) {
			Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
			recordVO = presenter.getRecordVO(paramsMap.get("id"));
		} else {
			showErrorMessage($("DisplayBagInfoView.idMustNotBeNull"));
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
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		return new RecordDisplay(this.recordVO);
	}
}
