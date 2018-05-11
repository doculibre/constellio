package com.constellio.app.ui.framework.components;

import au.com.bytecode.opencsv.CSVWriter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractCSVProducer implements Iterator<List<String[]>> {
    public final static int OFFSET = 1000;

    private Long maxRow;
    private int currentPage;
    private final Table table;

    public AbstractCSVProducer(Table table) {
        this(table, null);
    }

    public AbstractCSVProducer(Table table, Long maxRow) {
        this.table = table;
        this.currentPage = 0;
        this.maxRow = maxRow;
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
        try (CSVWriter writer = new CSVWriter(new FileWriterWithEncoding(temp, StandardCharsets.ISO_8859_1, true))) {
            writer.writeAll(csvDatas);
            writer.flush();
        }
    }

    protected String[] getHeaderRow() {
        List<String> headerRow = new ArrayList<>();

        Object[] visibleColumns = getTable().getVisibleColumns();
        for (int i = 0; i < visibleColumns.length; i++) {
            headerRow.add(getTable().getColumnHeader(visibleColumns[i]));
        }

        return headerRow.toArray(new String[0]);
    }

    public Table getTable() {
        return table;
    }

    public int getTotalPage() {
        double res = ((double) getCalculatedRowCount())/((double) OFFSET);
        return (int) Math.ceil(res);
    }

    @Override
    public boolean hasNext() {
        return currentPage < getTotalPage();
    }

    @Override
    public List<String[]> next() {
        List<String[]> rows = new ArrayList<>();
        int startIndex = (currentPage++) * OFFSET;
        int nbLigne = OFFSET;

        if(maxRow != null && maxRow > 0) {
            nbLigne = (currentPage < getTotalPage())?OFFSET:Math.min(OFFSET, maxRow.intValue());
        }

        List<Item> items = loadItems(startIndex, nbLigne);
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

    protected String getValue(Property prop) {
        Object value = prop.getValue();

        if(Number.class.isAssignableFrom(value.getClass())) {
            return String.valueOf(((Number) value).longValue());
        }

        return value.toString();
    }

    private long getCalculatedRowCount() {
        long currentRowCount = getRowCount();

        if(maxRow != null && maxRow > 0) {
            return Math.min(maxRow, currentRowCount);
        }

        return currentRowCount;
    }

    protected abstract long getRowCount();

    protected abstract List<Item> loadItems(int startIndex, int numberOfItems);

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
