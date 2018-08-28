package com.constellio.app.ui.pages.statistic;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.AbstractCSVProducer;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.FacetsLazyContainer;
import com.constellio.app.ui.framework.containers.SearchEventVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.constellio.app.ui.framework.containers.SearchEventVOLazyContainer.getProperties;
import static com.constellio.app.ui.framework.containers.SearchEventVOLazyContainer.getPropertiesWithParams;
import static com.constellio.app.ui.i18n.i18n.$;

public class StatisticsViewImpl extends BaseViewImpl implements StatisticsView, Serializable {
	private static Logger LOGGER = LoggerFactory.getLogger(StatisticsViewImpl.class);

	public static final Integer DEFAULT_LINE_NUMBER = 15;

	private final StatisticsPresenter presenter;

	private FormBean formBean = new FormBean();

	@PropertyId("excludedRequest")
	private TextArea excludedRequestField;
	@PropertyId("statisticType")
	private ComboBox statisticTypeField;
	@PropertyId("startDate")
	private DateField startDateField;
	@PropertyId("endDate")
	private DateField endDateField;
	@PropertyId("lines")
	private TextField linesField;
	@PropertyId("filter")
	private TextField filterField;
	@PropertyId("showParams")
	private CheckBox showParamsField;
	@PropertyId("capsuleId")
	private ComboBox capsuleIdField;

	private Table resultTable;
	private VerticalLayout tableLayout;

	public StatisticsViewImpl() {
		presenter = new StatisticsPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();

		tableLayout = new VerticalLayout();
		tableLayout.setHeight("100%");
		tableLayout.setWidth("95%");
		tableLayout.addStyleName("stats-table-layout");

		verticalLayout.addComponent(buildSearchForm());
		//verticalLayout.addComponent(buildApplyFilterButton());
		verticalLayout.addComponent(buildResultTable());

		return verticalLayout;
	}

	@Override
	protected String getTitle() {
		return $("StatisticsView.viewTitle");
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@NotNull
	private Layout buildApplyFilterButton() {
		Button button = new Button($("StatisticsView.applyFilter"));
		button.addClickListener(new ClickListener() {
			private String statisticType;

			@Override
			public void buttonClick(ClickEvent event) {
				String newStatisticType = ((CBItem) statisticTypeField.getValue()).code;

				presenter.applyFilter(excludedRequestField.getValue(),
						newStatisticType,
						startDateField.getValue(),
						endDateField.getValue(),
						filterField.getValue(),
						(String) capsuleIdField.getValue());

				if (Objects.equals(statisticType, newStatisticType)) {
					resultTable.setContainerDataSource(getContainer(initColumnsHeader(getChoosenStatisticTypeCode())));
				} else {
					buildResultTable();
				}
			}
		});

		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.addComponent(button);
		hl.setComponentAlignment(button, Alignment.MIDDLE_CENTER);

		return hl;
	}

	private Component buildSearchForm() {
		excludedRequestField = new BaseTextArea($("StatisticsView.excludedRequest"));
		excludedRequestField.setId("excludedRequest");

		statisticTypeField = new BaseComboBox($("StatisticsView.statisticType"));
		statisticTypeField.setNullSelectionAllowed(false);
		statisticTypeField.setId("statisticType");

		CBItem item = new CBItem(null, $("StatisticsView.journalRequest"));
		statisticTypeField.addItem(item);

		statisticTypeField.addItem(new CBItem(StatisticsPresenter.FAMOUS_REQUEST, $("StatisticsView.famousRequest")));
		statisticTypeField
				.addItem(new CBItem(StatisticsPresenter.FAMOUS_REQUEST_WITH_RESULT, $("StatisticsView.famousRequestWithResult")));
		statisticTypeField.addItem(
				new CBItem(StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_RESULT, $("StatisticsView.famousRequestWithoutResult")));
		statisticTypeField
				.addItem(new CBItem(StatisticsPresenter.FAMOUS_REQUEST_WITH_CLICK, $("StatisticsView.famousRequestWithClick")));
		statisticTypeField.addItem(
				new CBItem(StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_CLICK, $("StatisticsView.famousRequestWithoutClick")));

		startDateField = new DateField($("StatisticsView.startDate"));
		startDateField.setId("startDateField");
		startDateField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				endDateField.setRangeStart(startDateField.getValue());
			}
		});

		endDateField = new DateField($("StatisticsView.endDate"));
		endDateField.setId("endDateField");
		endDateField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				startDateField.setRangeEnd(endDateField.getValue());
			}
		});

		linesField = new BaseTextField($("StatisticsView.lines"));
		linesField.setId("lines");
		linesField.setConverter(Long.class);
		linesField.setConversionError($("StatisticsView.lines.conversionError"));
		linesField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				buildResultTable();
			}
		});

		filterField = new BaseTextField($("StatisticsView.filter"));
		filterField.setId("filter");

		showParamsField = new CheckBox($("StatisticsView.showParams"));
		showParamsField.setId("showParams");
		showParamsField.setValue(false);
		showParamsField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				buildResultTable();
			}
		});

		capsuleIdField = new RecordComboBox(Capsule.DEFAULT_SCHEMA);
		capsuleIdField.setCaption($("StatisticsView.capsuleField"));

		List<Field<?>> formFields = new ArrayList<>();
		formFields.addAll(Arrays
				.asList(excludedRequestField, statisticTypeField, startDateField, endDateField, filterField, showParamsField,
						linesField));
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			formFields.add(5, capsuleIdField);
		}

		BaseForm<FormBean> baseForm = new BaseForm<FormBean>(formBean, this,
				formFields.toArray(new Field[0])) {
			private String statisticType;

			@Override
			protected String getSaveButtonCaption() {
				return $("StatisticsView.applyFilter");
			}

			@Override
			protected void saveButtonClick(FormBean viewObject)
					throws ValidationException {
				CBItem cbItem = (CBItem) statisticTypeField.getValue();
				String newStatisticType = null;
				if (cbItem != null) {
					newStatisticType = cbItem.code;
				}

				presenter.applyFilter(excludedRequestField.getValue(),
						newStatisticType,
						startDateField.getValue(),
						endDateField.getValue(),
						filterField.getValue(),
						(String) capsuleIdField.getValue());

				buildResultTable();

				statisticType = newStatisticType;
			}

			@Override
			protected void cancelButtonClick(FormBean viewObject) {
			}

			@Override
			protected boolean isCancelButtonVisible() {
				return false;
			}
		};

		statisticTypeField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				showParamsField.setVisible(!isStatisticTypeChoice());
			}
		});
		statisticTypeField.select(item);

		return baseForm;
	}

	private Layout buildResultTable() {
		List<String> columnsHeader = initColumnsHeader(getChoosenStatisticTypeCode());
		LazyQueryContainer container = getContainer(columnsHeader);

		resultTable = new BaseTable(getClass().getName() + System.currentTimeMillis(), "", container);
		resultTable.setWidth("100%");
		resultTable.setPageLength(Math.min(container.size(), DEFAULT_LINE_NUMBER));

		for (String property : columnsHeader) {
			resultTable.setColumnHeader(property, getColumnHeader(property));
		}

		if (columnsHeader.contains(SearchEventVOLazyContainer.PARAMS)) {
			resultTable.addGeneratedColumn(SearchEventVOLazyContainer.PARAMS, new Table.ColumnGenerator() {
				@Override
				public Object generateCell(Table source, Object itemId, Object columnId) {
					Property property = source.getContainerProperty(itemId, SearchEventVOLazyContainer.PARAMS);

					Label label = new Label(property.getValue().toString());
					label.setContentMode(ContentMode.HTML);

					return label;
				}
			});
		}

		List<String> visibleColumns = initVisibleColumns();
		resultTable.setVisibleColumns(visibleColumns.toArray(new String[0]));

		tableLayout.removeAllComponents();

		tableLayout.addComponent(createOtherComponents(container));
		tableLayout.addComponent(resultTable);
		tableLayout.setExpandRatio(resultTable, 1);

		return tableLayout;
	}

	protected Layout createOtherComponents(LazyQueryContainer container) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		//        layout.setSpacing(true);

		Label label = new Label($("StatisticsView.numberOfResults") + " : " + container.size());
		label.setContentMode(ContentMode.HTML);

		layout.addComponent(label);
		layout.setExpandRatio(label, 1);

		DownloadLink downloadLink = new DownloadLink(getCsvDocumentResource(), $("StatisticsView.downloadCsv"));

		layout.addComponent(downloadLink);
		layout.setComponentAlignment(downloadLink, Alignment.TOP_RIGHT);

		downloadLink.setVisible(container.size() > 0);

		return layout;
	}

	private Resource getCsvDocumentResource() {
		StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				try {
					return new FileInputStream(getCSVProducer().produceCSVFile());
				} catch (IOException e) {
					LOGGER.error("Error during CSV generation", e);
					return null;
				}
			}
		}, composeCsvName());
		resource.setCacheTime(0);

		return resource;
	}

	protected String composeCsvName() {
		StringBuilder sb = new StringBuilder(
				StringUtils.replaceChars(((CBItem) statisticTypeField.getValue()).toString(), ' ', '_'));
		sb.append("_" + getCollection());

		String filter = filterField.getValue();
		if (StringUtils.isNotBlank(filter)) {
			sb.append("_" + StringUtils.replaceChars(filter, ' ', '_'));
		}

		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
		Date startDate = startDateField.getValue();
		if (startDate != null) {
			sb.append("_" + sdf.format(startDate));
		}

		Date endDate = endDateField.getValue();
		if (endDate != null) {
			sb.append("_");
			if (startDate != null) {
				sb.append("au_");
			} else {
				sb.append("jusqu_au_");
			}
			sb.append(sdf.format(endDate));
		}

		return sb.toString() + ".csv";
	}

	@NotNull
	private AbstractCSVProducer getCSVProducer() {
		AbstractCSVProducer csvProducer;
		if (isStatisticTypeChoice()) {
			csvProducer = new FacetsCSVProducer(resultTable, (Long) linesField.getConvertedValue(),
					presenter.getStatisticsFacetsDataProvider(), initColumnsHeader(getChoosenStatisticTypeCode()));
		} else {
			csvProducer = new SearchEventCSVProducer(resultTable, (Long) linesField.getConvertedValue(),
					presenter.getStatisticsDataProvider());
		}

		return csvProducer;
	}

	@NotNull
	private LazyQueryContainer getContainer(List<String> properties) {
		if (isStatisticTypeChoice()) {
			return FacetsLazyContainer.defaultInstance(presenter.getStatisticsFacetsDataProvider(), properties);
		} else {
			return SearchEventVOLazyContainer.defaultInstance(presenter.getStatisticsDataProvider(), properties);
		}
	}

	private boolean isStatisticTypeChoice() {
		switch (StringUtils.trimToEmpty(getChoosenStatisticTypeCode())) {
			case StatisticsPresenter.FAMOUS_REQUEST:
			case StatisticsPresenter.FAMOUS_REQUEST_WITH_RESULT:
			case StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_RESULT:
			case StatisticsPresenter.FAMOUS_REQUEST_WITH_CLICK:
			case StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_CLICK:
				return true;
			default:
				return false;
		}
	}

	private String getColumnHeader(String property) {
		switch (property) {
			case FacetsLazyContainer.CLICK_COUNT:
			case SearchEventVOLazyContainer.CLICK_COUNT:
				return $("StatisticsView.clickCount");

			case FacetsLazyContainer.ORIGINAL_QUERY:
			case SearchEventVOLazyContainer.ORIGINAL_QUERY:
				return $("StatisticsView.originalQuery");

			case SearchEventVOLazyContainer.LAST_PAGE_NAVIGATION:
				return $("StatisticsView.lastPageNavigation");

			case SearchEventVOLazyContainer.PARAMS:
				return $("StatisticsView.params");

			case SearchEventVOLazyContainer.Q_TIME:
				return $("StatisticsView.qTime");

			case SearchEventVOLazyContainer.NUM_FOUND:
				return $("StatisticsView.numFound");

			case SearchEventVOLazyContainer.QUERY:
				return $("StatisticsView.query");

			case SearchEventVOLazyContainer.USER:
				return $("StatisticsView.user");

			case SearchEventVOLazyContainer.CREATION_DATE:
				return $("StatisticsView.creationDate");

			case FacetsLazyContainer.FREQUENCY:
				return $("StatisticsView.frequency");

			case SearchEventVOLazyContainer.NUM_PAGE:
				return $("StatisticsView.numPage");

			case SearchEventVOLazyContainer.SOUS_COLLECTION:
				return $("StatisticsView.sousCollection");

			case SearchEventVOLazyContainer.LANGUE:
				return $("StatisticsView.langue");

			case SearchEventVOLazyContainer.TYPE_RECHERCHE:
				return $("StatisticsView.typeRecherche");

			case SearchEventVOLazyContainer.CAPSULE:
				return $("StatisticsView.capsule");

			case FacetsLazyContainer.CLICKS:
			case SearchEventVOLazyContainer.CLICKS:
				return $("StatisticsView.clicks");

			case SearchEventVOLazyContainer.ID:
				return $("StatisticsView.id");

			default:
				return property;
		}
	}

	public List<String> initColumnsHeader(String statisticType) {
		switch (StringUtils.trimToEmpty(statisticType)) {
			case StatisticsPresenter.FAMOUS_REQUEST:
			case StatisticsPresenter.FAMOUS_REQUEST_WITH_RESULT:
			case StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_RESULT:
			case StatisticsPresenter.FAMOUS_REQUEST_WITH_CLICK:
			case StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_CLICK:
				return initStatisticsColumnsHeader(statisticType);
			default:
				MetadataSchemaVO schema = presenter.getStatisticsDataProvider().getSchema();
				return new ArrayList<>(isParamsShowed() ? getPropertiesWithParams(schema) : getProperties(schema));
		}
	}

	public static List<String> initStatisticsColumnsHeader(String statisticType) {
		List<String> properties;

		switch (StringUtils.trimToEmpty(statisticType)) {
			case StatisticsPresenter.FAMOUS_REQUEST:
				properties = Arrays
						.asList(FacetsLazyContainer.ORIGINAL_QUERY, FacetsLazyContainer.FREQUENCY, FacetsLazyContainer.CLICK_COUNT,
								FacetsLazyContainer.CLICKS);
				break;
			case StatisticsPresenter.FAMOUS_REQUEST_WITH_RESULT:
				properties = Arrays
						.asList(FacetsLazyContainer.ORIGINAL_QUERY, FacetsLazyContainer.FREQUENCY, FacetsLazyContainer.CLICK_COUNT,
								FacetsLazyContainer.CLICKS);
				break;
			case StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_RESULT:
				properties = Arrays.asList(FacetsLazyContainer.ORIGINAL_QUERY, FacetsLazyContainer.FREQUENCY);
				break;
			case StatisticsPresenter.FAMOUS_REQUEST_WITH_CLICK:
				properties = Arrays
						.asList(FacetsLazyContainer.ORIGINAL_QUERY, FacetsLazyContainer.FREQUENCY, FacetsLazyContainer.CLICK_COUNT,
								FacetsLazyContainer.CLICKS);
				break;
			case StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_CLICK:
				properties = Arrays.asList(FacetsLazyContainer.ORIGINAL_QUERY, FacetsLazyContainer.FREQUENCY);
				break;
			default:
				properties = new ArrayList<>();
		}

		return properties;
	}

	private List<String> initVisibleColumns() {
		if (isStatisticTypeChoice()) {
			return initColumnsHeader(getChoosenStatisticTypeCode());
		} else {
			MetadataSchemaVO schema = presenter.getStatisticsDataProvider().getSchema();
			return new ArrayList<>(isParamsShowed() ? getPropertiesWithParams(schema) : getProperties(schema));
		}
	}

	private boolean isParamsShowed() {
		if (showParamsField == null) {
			return false;
		} else {
			return BooleanUtils.isTrue(showParamsField.getValue());
		}
	}

	@Nullable
	private String getChoosenStatisticTypeCode() {
		String code = null;

		CBItem cbItem = (CBItem) statisticTypeField.getValue();
		if (cbItem != null) {
			code = cbItem.code;
		}
		return code;
	}

	private class CBItem {
		private final String code;
		private final String label;

		public CBItem(String code, String label) {
			this.code = code;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			CBItem cbItem = (CBItem) o;

			return code == cbItem.code;
		}
	}

	public class FormBean {
		private String excludedRequest;
		private CBItem statisticType;
		private Date startDate;
		private Date endDate;
		private String filter;
		private String capsuleId;

		public String getExcludedRequest() {
			return excludedRequest;
		}

		public void setExcludedRequest(String excludedRequest) {
			this.excludedRequest = excludedRequest;
		}

		public CBItem getStatisticType() {
			return statisticType;
		}

		public void setStatisticType(CBItem statisticType) {
			this.statisticType = statisticType;
		}

		public Date getStartDate() {
			return startDate;
		}

		public void setStartDate(Date startDate) {
			this.startDate = startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}

		public String getFilter() {
			return filter;
		}

		public void setFilter(String filter) {
			this.filter = filter;
		}

		public String getCapsuleId() {
			return capsuleId;
		}

		public void setCapsuleId(String capsuleId) {
			this.capsuleId = capsuleId;
		}
	}
}
