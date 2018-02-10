package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class ScheduleComponent extends HorizontalLayout implements Property.ValueChangeListener {

    private static final String DELETE = "delete";

    private static final String ADD = "add";

    private static final Resource DELETE_ICON_RESOURCE = new ThemeResource("images/commun/supprimer.gif");

    private static final String HOUR_ROUNDED_SUFFIX = ":00";

    private static final String HALF_HOUR_ROUNDED_SUFFIX = ":30";

    private static final List<String> options = Arrays.asList(new String[] {"ldap.syncConfiguration.schedule.time", "ldap.syncConfiguration.durationBetweenExecution"});

    private BeanItemContainer<TimeBean> timeBeanContainer = new BeanItemContainer<>(TimeBean.class);

    private TextField daysComponent;

    private TextField hoursComponent;

    private TextField minComponent;

    private Layout scheduleTimeLayout;

    private Layout periodLayout;

    public ScheduleComponent(final List<String> timeList, final Duration period) {
        setSizeFull();
        addStyleName(ValoTheme.PANEL_BORDERLESS);

        final OptionGroup scheduleOptions = buildOptionsComponent();

        scheduleTimeLayout = buildTimeListTableComponent(timeList);
        addComponent(scheduleTimeLayout);

        periodLayout = buildPeriodComponent(period);
        addComponent(periodLayout);

        if (CollectionUtils.isEmpty(timeList)) {
            scheduleOptions.unselect(options.get(0));
            scheduleTimeLayout.setVisible(false);
        } else {
            scheduleOptions.select(options.get(0));
        }

        if (period == null) {
            scheduleOptions.unselect(options.get(1));
            periodLayout .setVisible(false);
        } else {
            scheduleOptions.select(options.get(1));
        }
    }

    private OptionGroup buildOptionsComponent() {
        final OptionGroup scheduleOptions = new OptionGroup($("ldap.syncConfiguration.schedule"), options);

        for (final String option : options) {
            scheduleOptions.setItemCaption(option, $(option));
        }

        scheduleOptions.setNullSelectionAllowed(true);
        scheduleOptions.setImmediate(true);
        scheduleOptions.addValueChangeListener(this);

        addComponent(scheduleOptions);

        return scheduleOptions;
    }

    private Layout buildTimeListTableComponent(final List<String> timeList) {
        final Layout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setSizeFull();

        final Table table = new BaseTable(getClass().getName());

        timeBeanContainer = new BeanItemContainer<>(TimeBean.class);

        setTimeList(timeList);

        table.setContainerDataSource(timeBeanContainer);

        table.setSizeFull();
        table.setPageLength(table.size());
        table.setEditable(true);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);

        table.addGeneratedColumn(TimeBean.TIME_PROPERTY_NAME, new Table.ColumnGenerator() {

            public Object generateCell(Table source, Object itemId, Object columnId) {
                final ComboBox comboBox = new ComboBox();

                for (int scheduleTime = 0; scheduleTime < 24; scheduleTime++) {
                    comboBox.addItem(scheduleTime + HOUR_ROUNDED_SUFFIX);
                    comboBox.addItem(scheduleTime + HALF_HOUR_ROUNDED_SUFFIX);
                }

                comboBox.setPropertyDataSource(new NestedMethodProperty<String>(itemId, TimeBean.TIME_PROPERTY_NAME));

                return comboBox;
            }
        });

        table.addGeneratedColumn(DELETE, new Table.ColumnGenerator() {

            public Object generateCell(final Table source, final Object itemId, Object columnId) {
                final Button removeButton = new Button();

                removeButton.setIcon(DELETE_ICON_RESOURCE);
                removeButton.setEnabled(source.size() > 1);

                removeButton.addClickListener(new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent event) {
                        timeBeanContainer.removeItem(itemId);
                        source.setPageLength(source.size());
                    }
                });

                return removeButton;
            }
        });

        horizontalLayout.addComponent(table);

        horizontalLayout.addComponent(new AddButton($(ADD)) {
            @Override
            protected void buttonClick(ClickEvent event) {
                timeBeanContainer.addItem(new TimeBean());
                table.setPageLength(table.size());
            }
        });

        return horizontalLayout;
    }

    private void setTimeList(final List<String> timeList) {
        timeBeanContainer.removeAllItems();

        if (CollectionUtils.isEmpty(timeList)) {
            timeBeanContainer.addItem(new TimeBean());
        } else {
            for (final String time : timeList) {
                timeBeanContainer.addItem(new TimeBean(time));
            }
        }
    }

    private Layout buildPeriodComponent(final Duration peroid) {
        final Layout horizontalLayout = new HorizontalLayout();

        daysComponent = new TextField($("days"));
        //daysComponent.addValidator(new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator_withMinValue"), 0, 100));
        horizontalLayout.addComponent(daysComponent);

        hoursComponent = new TextField($("hours"));
        //hoursComponent.addValidator(new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator"), 0, 23));
        horizontalLayout.addComponent(hoursComponent);

        minComponent = new TextField($("mns"));
        //minComponent.addValidator(new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator"), 0, 59));
        horizontalLayout.addComponent(minComponent);

        setPeriod(peroid);

        return horizontalLayout;
    }

    public void setPeriod(Duration period) {
        long days = 0, hours = 0, mns = 0;

        if (period != null) {
            days = period.getStandardDays();
            hours = period.getStandardHours() % 24;
            mns = period.getStandardMinutes() % 60;
        }

        daysComponent.setValue(days + "");
        hoursComponent.setValue(hours + "");
        minComponent.setValue(mns + "");
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        if (options.get(0).equals(event.getProperty().getValue())) {
            scheduleTimeLayout.setVisible(true);

            periodLayout.setVisible(false);
            setPeriod(null);
        } else {
            scheduleTimeLayout.setVisible(false);
            setTimeList(null);

            periodLayout.setVisible(true);
        }
    }

    public List<String> getTimeList() {
        final List<String> timeList = new ArrayList<>(timeBeanContainer.size());

        for (final TimeBean timeBean : timeBeanContainer.getItemIds()) {
            if ((timeBean == null) || (timeBean.getTime() == null) || (timeBean.getTime().isEmpty()) || (timeList.contains(timeBean.getTime()))) {
                continue;
            }

            timeList.add(timeBean.getTime());
        }

        Collections.sort(timeList);

        return timeList;
    }

    public Duration getPeriod() {
        Integer days = 0, hours = 0, mns = 0;
        if (StringUtils.isNotBlank(daysComponent.getValue())) {
            days = Integer.valueOf(daysComponent.getValue());
        }
        if (StringUtils.isNotBlank(hoursComponent.getValue())) {
            hours = Integer.valueOf(hoursComponent.getValue());
        }
        if (StringUtils.isNotBlank(minComponent.getValue())) {
            mns = Integer.valueOf(minComponent.getValue());
        }
        long durationInMilliSeconds = ((((days * 24) + hours) * 60) + mns) * 60 * 1000;
        return new Duration(durationInMilliSeconds);
    }

    protected static class TimeBean {

        public static final String TIME_PROPERTY_NAME = "time";

        private String time;

        public TimeBean() {
            time = "";
        }

        public TimeBean(String time) {
            this.time = time;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        @Override
        public String toString() {
            return time;
        }
    }
}
