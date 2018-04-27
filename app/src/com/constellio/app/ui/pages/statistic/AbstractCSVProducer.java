package com.constellio.app.ui.pages.statistic;

import au.com.bytecode.opencsv.CSVWriter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractCSVProducer implements Iterator<List<String[]>> {
    public final static int OFFSET = 1000;

    private int currentPage;
    private final Table table;

    public AbstractCSVProducer(Table table) {
        this.table = table;
        this.currentPage = 0;
    }

    public File produceCSVFile() throws IOException {
        File temp = File.createTempFile("csv", ".csv");

        List<String[]> csvDatas = new ArrayList<>();
        csvDatas.add(getHeaderRow());

        writeToFile(csvDatas, temp);

        while (hasNext()) {
            writeToFile(next(), temp);
        }

        return temp;
    }

    private void writeToFile(List<String[]> csvDatas, File temp) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(temp, true))) {
            writer.writeAll(csvDatas);
            writer.flush();
        }
    }

    protected String[] getHeaderRow() {
        List<String> headerRow = new ArrayList<>();

        Object[] visibleColumns = table.getVisibleColumns();
        for (int i = 0; i < visibleColumns.length; i++) {
            headerRow.add(table.getColumnHeader(visibleColumns[i]));
        }

        return headerRow.toArray(new String[0]);
    }

    public Table getTable() {
        return table;
    }

    public int getTotalPage() {
        double res = ((double) getRowCount())/((double) OFFSET);
        return (int) Math.ceil(res);
    }

    @Override
    public boolean hasNext() {
        return currentPage < getTotalPage();
    }

    @Override
    public List<String[]> next() {
        List<String[]> rows = new ArrayList<>();

        List<Item> items = loadItems((currentPage++) * OFFSET, OFFSET);
        for (final Item item : items) {
            rows.add(getDataRow(item));
        }

        return rows;
    }

    private String[] getDataRow(Item item) {
        List<String> row = new ArrayList<>();

        Object[] visibleColumns = getTable().getVisibleColumns();
        for (int i = 0; i < visibleColumns.length; i++) {
            Property prop = item.getItemProperty(visibleColumns[i]);

            String value = "";
            if(prop != null && prop.getValue() != null) {
                value = getValue(prop);
            }

            row.add(value);
        }

        return row.toArray(new String[0]);
    }

    private String getValue(Property prop) {
        Object value = prop.getValue();

        if(Number.class.isAssignableFrom(value.getClass())) {
            return String.valueOf(((Number) value).longValue());
        }

        return value.toString();
    }

    protected abstract long getRowCount();

    protected abstract List<Item> loadItems(int startIndex, int numberOfItems);

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
