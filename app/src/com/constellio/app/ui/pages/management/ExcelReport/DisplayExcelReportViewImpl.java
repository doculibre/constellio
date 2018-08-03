package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayExcelReportViewImpl extends BaseViewImpl implements DisplayExcelReportView {
	private DisplayExcelReportPresenter presenter;

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		presenter = new DisplayExcelReportPresenter(this);
		presenter.setParametersMap(ParamUtils.getParamsMap(event.getParameters()));
		return new RecordDisplay(presenter.getReport());
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
