package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.Report;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayExcelReportViewImpl extends BaseViewImpl implements DisplayExcelReportView {
	private DisplayExcelReportPresenter presenter;
	private RecordVO report;

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		presenter = new DisplayExcelReportPresenter(this);
		presenter.setParametersMap(ParamUtils.getParamsMap(event.getParameters()));
		report = presenter.getReport();
		return new RecordDisplay(report);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter = new DisplayExcelReportPresenter(this);
		presenter.setParametersMap(ParamUtils.getParamsMap(event.getParameters()));
	}

	@Override
	protected Button.ClickListener getBackButtonClickListener() {
		return new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	private Button newEditButton() {
		Button modifyButton = new BaseButton($("DisplayExcelReportViewImpl.edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				Map<String, String> paramsMap = new HashMap<>();
				paramsMap.put("schemaTypeCode", report.get(Report.SCHEMA_TYPE_CODE));
				paramsMap.put("id", report.getId());
				String params = ParamUtils.addParams(NavigatorConfigurationService.REPORT_DISPLAY_FORM, paramsMap);
				navigate().to(RMViews.class).reportDisplayForm(params);
			}
		};

		return modifyButton;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		return Arrays.asList(newEditButton());
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return Collections.singletonList(new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("ViewGroup.PrintableViewGroup");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to().viewReport();
					}
				});
			}
		};
	}

	@Override
	protected String getTitle() {
		return $("DisplayExcelReport.title") + " : " + presenter.getReport().getTitle();
	}
}
