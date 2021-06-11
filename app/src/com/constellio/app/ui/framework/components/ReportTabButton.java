package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.reports.model.search.UnsupportedReportException;
import com.constellio.app.modules.rm.services.reports.printable.PrintableExtension;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.utils.ReportGeneratorUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class ReportTabButton extends WindowButton {
	private VerticalLayout mainLayout, PDFTabLayout, wordTabLayout, xlsxTabLayout, htmlTabLayout;
	private TabSheet tabSheet;
	private BaseView view;
	private ComboBox pdfReportComboBox = new BaseComboBox(), pdfCustomElementSelected = new BaseComboBox(), pdfDefaultElementSelected = new BaseComboBox();
	private ComboBox wordReportComboBox = new BaseComboBox(), wordCustomElementSelected = new BaseComboBox(), wordDefaultElementSelected = new BaseComboBox();
	private ComboBox xlsxReportComboBox = new BaseComboBox(), xlsxCustomElementSelected = new BaseComboBox(), xlsxDefaultElementSelected = new BaseComboBox();
	private ComboBox htmlReportComboBox = new BaseComboBox(), htmlCustomElementSelected = new BaseComboBox(), htmlDefaultElementSelected = new BaseComboBox();
	private PrintableReportListPossibleType selectedReporType;
	private MetadataSchemaVO selectedSchema;
	private boolean noExcelButton = false, noPDFButton = false, noWordButton = false, noXlsxButton = false, noHtmlButton = false;
	private AppLayerFactory factory;
	private String collection;
	private TabSheet.Tab excelTab, pdfTab, wordTab, xlsxTab, htmlTab, errorTab;
	private NewReportPresenter viewPresenter;
	private ReportTabButtonPresenter buttonPresenter;
	private SessionContext sessionContext;

	public ReportTabButton(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection,
						   NewReportPresenter presenter, SessionContext sessionContext) {
		this(caption, windowCaption, appLayerFactory, collection, false, false, false, false, false,
				presenter, sessionContext);
	}

	public ReportTabButton(String caption, String windowCaption, BaseView view) {
		this(caption, windowCaption, view.getConstellioFactories().getAppLayerFactory(), view.getCollection(),
				false, false, false, false, false, null,
				view.getSessionContext());
		this.view = view;
	}

	public ReportTabButton(String caption, String windowCaption, BaseView view, boolean noExcelButton,
						   boolean noPDFButton, boolean noWordButton, boolean noXlsxButton, boolean noHtmlButton) {
		this(caption, windowCaption, view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), noExcelButton,
				noPDFButton, noWordButton, noXlsxButton, noHtmlButton, null, view.getSessionContext());
		this.view = view;
	}

	public ReportTabButton(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection,
						   boolean noExcelButton, boolean noPDFButton, boolean noWordButton, boolean noXlsxButton,
						   boolean noHtmlButton, NewReportPresenter presenter, SessionContext sessionContext) {

		super(caption, windowCaption, new WindowConfiguration(true, true, "50%", "50%"));
		this.viewPresenter = presenter;
		this.factory = appLayerFactory;
		this.collection = collection;
		this.noExcelButton = noExcelButton;
		this.noPDFButton = noPDFButton;
		this.noWordButton = noWordButton;
		this.noXlsxButton = noXlsxButton;
		this.noHtmlButton = noHtmlButton;
		this.sessionContext = sessionContext;
		this.buttonPresenter = new ReportTabButtonPresenter(this);
	}

	public ReportTabButton setRecordVoList(RecordVO... recordVOS) {
		buttonPresenter.setRecordVoList(recordVOS);
		return this;
	}

	public ReportTabButton addRecordToVoList(RecordVO recordVO) {
		buttonPresenter.addRecordToVoList(recordVO);
		return this;
	}

	public String getCollection() {
		return collection;
	}

	public AppLayerFactory getFactory() {
		return this.factory;
	}

	public SessionContext getSessionContext() {
		return this.sessionContext;
	}

	@Override
	public void afterOpenModal() {
		if (pdfTab != null && (buttonPresenter.isNeedToRemovePDFTab() ||
							   (pdfReportComboBox == null || pdfReportComboBox.getContainerDataSource().size() == 0))) {
			pdfTab.setVisible(false);
		}
		if (wordTab != null && (buttonPresenter.isNeedToRemoveWordTab() ||
								(wordReportComboBox == null || wordReportComboBox.getContainerDataSource().size() == 0))) {
			wordTab.setVisible(false);
		}
		if (xlsxTab != null && (buttonPresenter.isNeedToRemoveXlsxTab() ||
								(xlsxReportComboBox == null || xlsxReportComboBox.getContainerDataSource().size() == 0))) {
			xlsxTab.setVisible(false);
		}
		if (htmlTab != null && (buttonPresenter.isNeedToRemoveHtmlTab() ||
								(htmlReportComboBox == null || htmlReportComboBox.getContainerDataSource().size() == 0))) {
			htmlTab.setVisible(false);
		}

		if (excelTab != null && buttonPresenter.isNeedToRemoveExcelTab()) {
			excelTab.setVisible(false);
		}

		if (errorTab != null && (pdfTab == null || !pdfTab.isVisible()) &&
			(excelTab == null || !excelTab.isVisible()) && (wordTab == null || !wordTab.isVisible()) &&
			(xlsxTab == null || !xlsxTab.isVisible()) && (htmlTab == null || !htmlTab.isVisible())) {
			errorTab.setVisible(true);
		} else {
			if (errorTab != null) {
				errorTab.setVisible(false);
			}
		}
	}

	@Override
	protected Component buildWindowContent() {
		mainLayout = new VerticalLayout();
		tabSheet = new TabSheet();

		if (!this.noExcelButton) {
			excelTab = tabSheet.addTab(createExcelTab(), $("ReportTabButton.ExcelReport"));
		}
		if (!this.noPDFButton) {
			pdfTab = tabSheet.addTab(createPDFTab(), $("ReportTabButton.PDFReport"));
		}
		if (!this.noWordButton) {
			wordTab = tabSheet.addTab(createWordTab(), $("ReportTabButton.WordReport"));
		}
		if (!this.noXlsxButton) {
			xlsxTab = tabSheet.addTab(createXlsxTab(), $("ReportTabButton.XlsxReport"));
		}
		if (!this.noHtmlButton) {
			htmlTab = tabSheet.addTab(createHtmlTab(), $("ReportTabButton.HtmlReport"));
		}

		errorTab = tabSheet.addTab(createErrorTab(), $("ReportTabButton.ShowError"));

		mainLayout.addComponent(tabSheet);
		return mainLayout;
	}

	private VerticalLayout createErrorTab() {
		VerticalLayout verticalLayout = new VerticalLayout();

		Label label = new Label($("ReportTabButton.noReportTemplateForCondition"));

		verticalLayout.addComponent(label);

		return verticalLayout;
	}

	private VerticalLayout createExcelTab() {
		VerticalLayout verticalLayout = new VerticalLayout();
		try {
			NewReportPresenter newReportPresenter;
			if (viewPresenter == null) {
				//                AdvancedSearchPresenter Advancedpresenter = new AdvancedSearchPresenter((AdvancedSearchView) view);
				//                Advancedpresenter.setSchemaType(((AdvancedSearchView) view).getSchemaType());
				//                newReportPresenter = Advancedpresenter;
				newReportPresenter = ((AdvancedSearchView) view).getPresenter();
			} else {
				newReportPresenter = this.viewPresenter;
			}
			if (newReportPresenter.getSupportedReports().isEmpty()) {
				buttonPresenter.setNeedToRemoveExcelTab(true);
			} else {
				buttonPresenter.setNeedToRemoveExcelTab(false);
			}
			verticalLayout.addComponent(new ReportSelector(newReportPresenter));
		} catch (UnsupportedReportException unsupportedReport) {
			showErrorMessage($("ReportTabButton.noExcelReport"));
		}
		return verticalLayout;
	}

	private VerticalLayout createPDFTab() {
		PDFTabLayout = new VerticalLayout();
		PDFTabLayout.addComponent(createDefaultSelectComboBox(pdfDefaultElementSelected, pdfCustomElementSelected));
		PDFTabLayout.addComponent(createCustomSelectComboBox(pdfCustomElementSelected, pdfReportComboBox, TabType.PDF));
		PDFTabLayout.addComponent(createReportSelectorComboBox(pdfReportComboBox, TabType.PDF));
		PDFTabLayout.addComponent(createButtonLayout(pdfReportComboBox, TabType.PDF));
		PDFTabLayout.setSpacing(true);
		return PDFTabLayout;
	}

	private VerticalLayout createWordTab() {
		wordTabLayout = new VerticalLayout();
		wordTabLayout.addComponent(createDefaultSelectComboBox(wordDefaultElementSelected, wordCustomElementSelected));
		wordTabLayout.addComponent(createCustomSelectComboBox(wordCustomElementSelected, wordReportComboBox, TabType.WORD));
		wordTabLayout.addComponent(createReportSelectorComboBox(wordReportComboBox, TabType.WORD));
		wordTabLayout.addComponent(createButtonLayout(wordReportComboBox, TabType.WORD));
		wordTabLayout.setSpacing(true);
		return wordTabLayout;
	}

	private VerticalLayout createXlsxTab() {
		xlsxTabLayout = new VerticalLayout();
		xlsxTabLayout.addComponent(createDefaultSelectComboBox(xlsxDefaultElementSelected, xlsxCustomElementSelected));
		xlsxTabLayout.addComponent(createCustomSelectComboBox(xlsxCustomElementSelected, xlsxReportComboBox, TabType.XLSX));
		xlsxTabLayout.addComponent(createReportSelectorComboBox(xlsxReportComboBox, TabType.XLSX));
		xlsxTabLayout.addComponent(createButtonLayout(xlsxReportComboBox, TabType.XLSX));
		xlsxTabLayout.setSpacing(true);
		return xlsxTabLayout;
	}

	private VerticalLayout createHtmlTab() {
		htmlTabLayout = new VerticalLayout();
		htmlTabLayout.addComponent(createDefaultSelectComboBox(htmlDefaultElementSelected, htmlCustomElementSelected));
		htmlTabLayout.addComponent(createCustomSelectComboBox(htmlCustomElementSelected, htmlReportComboBox, TabType.HTML));
		htmlTabLayout.addComponent(createReportSelectorComboBox(htmlReportComboBox, TabType.HTML));
		htmlTabLayout.addComponent(createButtonLayout(htmlReportComboBox, TabType.HTML));
		htmlTabLayout.setSpacing(true);
		return htmlTabLayout;
	}

	private Component createDefaultSelectComboBox(ComboBox defaultElementSelected, ComboBox customElementSelected) {
		List<PrintableReportListPossibleType> values = buttonPresenter.getAllGeneralSchema();
		if (values.size() == 1) {
			selectedReporType = values.get(0);
			return new HorizontalLayout();
		}

		for (PrintableReportListPossibleType printableReportListPossibleType : values) {
			defaultElementSelected.addItem(printableReportListPossibleType);
			defaultElementSelected.setItemCaption(printableReportListPossibleType,
					buttonPresenter.getLabelForSchemaType(printableReportListPossibleType.getSchemaType()));
			if (defaultElementSelected.getValue() == null) {
				defaultElementSelected.setValue(printableReportListPossibleType);
				selectedReporType = printableReportListPossibleType;
			}
		}

		defaultElementSelected.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (value == null) {
					throw new InvalidValueException($("ReportTabButton.invalidReportType"));
				}
			}
		});
		defaultElementSelected.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				selectedReporType = (PrintableReportListPossibleType) defaultElementSelected.getValue();
				updateCustomSchemaValues(customElementSelected);
			}
		});
		defaultElementSelected.setNullSelectionAllowed(false);
		defaultElementSelected.setCaption($("ReportTabButton.selectDefaultReportType"));
		defaultElementSelected.setWidth("100%");
		return defaultElementSelected;
	}

	private Component createCustomSelectComboBox(ComboBox customElementSelected, ComboBox reportComboBox,
												 TabType tabType) {
		customElementSelected.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (value == null) {
					throw new InvalidValueException($("ReportTabButton.invalidRecordSchema"));
				}
			}
		});
		customElementSelected.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				selectedSchema = (MetadataSchemaVO) customElementSelected.getValue();
				updateAvailableReportForCurrentCustomSchema(reportComboBox, tabType);
			}
		});
		customElementSelected.setCaption($("ReportTabButton.selectCustomReportSchema"));
		customElementSelected.setWidth("100%");
		customElementSelected.setNullSelectionAllowed(false);
		updateCustomSchemaValues(customElementSelected);
		return customElementSelected;
	}

	private Component createReportSelectorComboBox(ComboBox reportComboBox, TabType tabType) {
		reportComboBox.setCaption($("ReportTabButton.selectTemplate"));
		reportComboBox.setWidth("100%");
		reportComboBox.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (value == null) {
					throw new InvalidValueException($("ReportTabButton.invalidChoosenReport"));
				}
			}
		});
		updateAvailableReportForCurrentCustomSchema(reportComboBox, tabType);
		reportComboBox.setNullSelectionAllowed(false);
		return reportComboBox;
	}

	private Button createButtonLayout(ComboBox reportComboBox, TabType tabType) {
		final Button button = new Button($("LabelsButton.generate"));
		button.addStyleName(WindowButton.STYLE_NAME);
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				RecordVO recordVO = (RecordVO) reportComboBox.getValue();
				PrintableExtension printableExtension = getPrintableExtension(tabType);

				PrintableReportTemplate template = new PrintableReportTemplate(recordVO.getId(), recordVO.getTitle(),
						buttonPresenter.getReportContent(recordVO));
				getWindow().setContent(ReportGeneratorUtils
						.saveButtonClick(factory, collection, selectedSchema.getTypeCode(), template, 1,
								buttonPresenter.getRecordVOIdFilteredList(selectedSchema),
								getLogicalSearchQuery(selectedSchema.getCode()), sessionContext.getCurrentLocale(),
								sessionContext.getCurrentUser(), printableExtension));
			}
		});
		return button;
	}

	private void updateCustomSchemaValues(ComboBox customElementSelected) {
		if (customElementSelected != null) {
			customElementSelected.removeAllItems();
			customElementSelected.setVisible(true);
			List<MetadataSchemaVO> allCustomSchemaForCurrentGeneralSchema = buttonPresenter.getAllCustomSchema(selectedReporType);
			if (allCustomSchemaForCurrentGeneralSchema.size() == 1) {
				customElementSelected.setVisible(false);
				selectedSchema = allCustomSchemaForCurrentGeneralSchema.get(0);
			}
			for (MetadataSchemaVO metadataSchemaVO : allCustomSchemaForCurrentGeneralSchema) {
				customElementSelected.addItem(metadataSchemaVO);
				customElementSelected.setItemCaption(metadataSchemaVO, metadataSchemaVO.getLabel());
			}

			for (MetadataSchemaVO metadataSchemaVO : allCustomSchemaForCurrentGeneralSchema) {
				if (!buttonPresenter.getAllAvailableReport(metadataSchemaVO).isEmpty()) {
					customElementSelected.setValue(metadataSchemaVO);
					break;
				}
			}
		}
	}

	private void updateAvailableReportForCurrentCustomSchema(ComboBox reportComboBox, TabType tabType) {
		if (reportComboBox != null && selectedSchema != null) {
			reportComboBox.removeAllItems();
			List<RecordVO> currentAvailableReports = buttonPresenter.getAllAvailableReport(selectedSchema).stream()
					.filter(currentAvailableReport -> {
						List<PrintableExtension> extensions = currentAvailableReport.get(PrintableReport.SUPPORTED_EXTENSIONS);
						return extensions.contains(getPrintableExtension(tabType));
					}).collect(Collectors.toList());
			reportComboBox.setEnabled(true);
			currentAvailableReports.forEach(currentAvailableReport -> {
				reportComboBox.addItem(currentAvailableReport);
				reportComboBox.setItemCaption(currentAvailableReport, currentAvailableReport.getTitle());
			});

			if (!currentAvailableReports.isEmpty()) {
				reportComboBox.setValue(currentAvailableReports.get(0));
			}
			if (currentAvailableReports.size() == 1) {
				reportComboBox.setEnabled(false);
			}
		}
	}

	protected LogicalSearchQuery getLogicalSearchQuery(String selectedSchemaFilter) {
		return null;
	}

	public void showErrorMessage(String errorMessage) {
		Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Notification.Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	private PrintableExtension getPrintableExtension(TabType tabType) {
		switch (tabType) {
			case PDF:
				return PrintableExtension.PDF;
			case WORD:
				return PrintableExtension.DOCX;
			case XLSX:
				return PrintableExtension.XLSX;
			case HTML:
				return PrintableExtension.HTML;
			default:
				return null;
		}
	}

	private enum TabType {
		PDF, WORD, XLSX, HTML, EXCEL
	}
}
