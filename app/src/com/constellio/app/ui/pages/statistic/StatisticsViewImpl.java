package com.constellio.app.ui.pages.statistic;

import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.*;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import java.io.Serializable;
import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class StatisticsViewImpl extends BaseViewImpl implements StatisticsView, Serializable {

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

    private Table resultTable;
    private VerticalLayout tableLayout = new VerticalLayout();

    public StatisticsViewImpl() {
        presenter = new StatisticsPresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);

        verticalLayout.addComponent(buildSearchForm());
        //verticalLayout.addComponent(buildApplyFilterButton());
        verticalLayout.addComponent(buildResultTable());

        return verticalLayout;
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
                        linesField.getValue(),
                        filterField.getValue());

                if(Objects.equals(statisticType, newStatisticType)) {
                    resultTable.setContainerDataSource(getContainer(initColumnVisibility()));
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

        statisticTypeField = new ComboBox($("StatisticsView.statisticType"));
        statisticTypeField.setNullSelectionAllowed(false);
        statisticTypeField.setId("statisticType");

        CBItem item = new CBItem(null, $("StatisticsView.journalRequest"));
        statisticTypeField.addItem(item);

        statisticTypeField.addItem(new CBItem(StatisticsPresenter.FAMOUS_REQUEST, $("StatisticsView.famousRequest")));
        statisticTypeField.addItem(new CBItem(StatisticsPresenter.FAMOUS_REQUEST_WITH_RESULT, $("StatisticsView.famousRequestWithResult")));
        statisticTypeField.addItem(new CBItem(StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_RESULT, $("StatisticsView.famousRequestWithoutResult")));
        statisticTypeField.addItem(new CBItem(StatisticsPresenter.FAMOUS_REQUEST_WITH_CLICK, $("StatisticsView.famousRequestWithClick")));
        statisticTypeField.addItem(new CBItem(StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_CLICK, $("StatisticsView.famousRequestWithoutClick")));

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
        linesField.setValue(DEFAULT_LINE_NUMBER.toString());
        linesField.setConverter(Integer.class);
        linesField.setConversionError($("StatisticsView.lines.conversionError"));
        linesField.addValidator(new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator"), 0, 15000));

        filterField = new BaseTextField($("StatisticsView.filter"));
        filterField.setId("filter");

        BaseForm<FormBean> baseForm = new BaseForm<FormBean>(formBean, this,
                excludedRequestField, statisticTypeField, startDateField, endDateField, linesField, filterField) {
            private String statisticType;

            @Override
            protected String getSaveButtonCaption() {
                return $("StatisticsView.applyFilter");
            }

            @Override
            protected void saveButtonClick(FormBean viewObject) throws ValidationException {
                CBItem cbItem = (CBItem) statisticTypeField.getValue();
                String newStatisticType = null;
                if (cbItem != null) {
                    newStatisticType = cbItem.code;
                }

                presenter.applyFilter(excludedRequestField.getValue(),
                        newStatisticType,
                        startDateField.getValue(),
                        endDateField.getValue(),
                        linesField.getValue(),
                        filterField.getValue());

                if(Objects.equals(statisticType, newStatisticType)) {
                    resultTable.setContainerDataSource(getContainer(initColumnVisibility()));
                } else {
                    buildResultTable();
                }

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
                linesField.setVisible(isStatisticTypeChoice());
            }
        });
        statisticTypeField.select(item);

        return baseForm;
    }

    private Layout buildResultTable() {
        List<String> properties = initColumnVisibility();
        LazyQueryContainer container = getContainer(properties);

        resultTable = new BaseTable(getClass().getName(), "", container);
        resultTable.setWidth("100%");

        for (String property: properties) {
            resultTable.setColumnHeader(property, getColumnHeader(property));
        }

        if(properties.contains(SearchEventVOLazyContainer.PARAMS)) {
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

        tableLayout.removeAllComponents();

        tableLayout.addComponent(resultTable);
        tableLayout.setExpandRatio(resultTable, 1);

        return tableLayout;
    }

    @NotNull
    private LazyQueryContainer getContainer(List<String> properties) {
        if (isStatisticTypeChoice()) {
            return StatisticsLazyContainer.defaultInstance(presenter.getStatisticsFacetsDataProvider(), properties);
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
            case StatisticsLazyContainer.CLICK_COUNT:
            case SearchEventVOLazyContainer.CLICK_COUNT:
                return $("StatisticsView.clickCount");

            case SearchEventVOLazyContainer.ORIGINAL_QUERY:
                return $("StatisticsView.originalQuery");

            case SearchEventVOLazyContainer.PAGE_NAVIGATION_COUNT:
                return $("StatisticsView.pageNavigationCount");

            case SearchEventVOLazyContainer.PARAMS:
                return $("StatisticsView.params");

            case SearchEventVOLazyContainer.Q_TIME:
                return $("StatisticsView.qTime");

            case SearchEventVOLazyContainer.NUM_FOUND:
                return $("StatisticsView.numFound");

            case StatisticsLazyContainer.QUERY:
            case SearchEventVOLazyContainer.QUERY:
                return $("StatisticsView.query");

            case SearchEventVOLazyContainer.USER:
                return $("StatisticsView.user");

            case SearchEventVOLazyContainer.CREATION_DATE:
                return $("StatisticsView.creationDate");

            case StatisticsLazyContainer.FREQUENCY:
                return $("StatisticsView.frequency");

            default:
                return property;
        }
    }

    private List<String> initColumnVisibility() {
        List<String> properties;

        switch (StringUtils.trimToEmpty(getChoosenStatisticTypeCode())) {
            case StatisticsPresenter.FAMOUS_REQUEST:
                properties = Arrays.asList(StatisticsLazyContainer.QUERY, StatisticsLazyContainer.FREQUENCY, StatisticsLazyContainer.CLICK_COUNT);
                break;
            case StatisticsPresenter.FAMOUS_REQUEST_WITH_RESULT:
                properties = Arrays.asList(StatisticsLazyContainer.QUERY, StatisticsLazyContainer.FREQUENCY, StatisticsLazyContainer.CLICK_COUNT);
                break;
            case StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_RESULT:
                properties = Arrays.asList(StatisticsLazyContainer.QUERY, StatisticsLazyContainer.FREQUENCY);
                break;
            case StatisticsPresenter.FAMOUS_REQUEST_WITH_CLICK:
                properties = Arrays.asList(StatisticsLazyContainer.QUERY, StatisticsLazyContainer.FREQUENCY, StatisticsLazyContainer.CLICK_COUNT);
                break;
            case StatisticsPresenter.FAMOUS_REQUEST_WITHOUT_CLICK:
                properties = Arrays.asList(StatisticsLazyContainer.QUERY, StatisticsLazyContainer.FREQUENCY);
                break;
            default:
                properties = new ArrayList<>(SearchEventVOLazyContainer.PROPERTIES);
        }

        return properties;
    }

    @Nullable
    private String getChoosenStatisticTypeCode() {
        String code = null;

        CBItem cbItem = (CBItem) statisticTypeField.getValue();
        if(cbItem != null) {
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

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
    }
}
