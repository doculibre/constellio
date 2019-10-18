package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayPrintableReportViewImpl extends BaseViewImpl implements DisplayPrintableReportView {
	private DisplayPrintableReportPresenter presenter;
	private RecordVO recordVO;

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
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		return Arrays.asList(newEditButton());
	}


	private Button newEditButton() {
		Button modifyButton = new BaseButton($("DisplayPrintableReportViewImpl.edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				navigate().to(RMViews.class).editPrintableReport(recordVO.getId());
			}
		};

		return modifyButton;
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);

		layout.addComponent(new RecordDisplay(recordVO, new MetadataDisplayFactory() {
			@Override
			public Component build(RecordVO recordVO, MetadataValueVO metadataValue) {
				if (metadataValue.getMetadata().codeMatches(PrintableReport.RECORD_TYPE)) {
					metadataValue.setValue(presenter.getRecordTypeValue(recordVO));
				} else if (metadataValue.getMetadata().codeMatches(PrintableReport.RECORD_SCHEMA)) {
					metadataValue.setValue(presenter.getRecordSchemaValue(recordVO));
				}
				return super.build(recordVO, metadataValue);
			}
		}));
		return layout;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter = new DisplayPrintableReportPresenter(this);
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
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected String getTitle() {
		return $("DisplayPrintableReportViewImpl.title") + " : " + recordVO.getTitle();
	}
}
