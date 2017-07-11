package com.constellio.app.modules.rm.model.PrintableReport;

import com.constellio.model.entities.records.Content;

import java.io.Serializable;

/**
 * Created by Marco on 2017-07-10.
 */
public class PrintableReportTemplate implements Serializable {
    private String title;
    private String id;
    private Content jasperFile;
    public PrintableReportTemplate(String id, String title, Content jasperFile) {
        this.title = title;
        this.id = id;
        this.jasperFile = jasperFile;
    }

    public String getTitle() {
        return title;
    }

    public PrintableReportTemplate setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public PrintableReportTemplate setId(String id) {
        this.id = id;
        return this;
    }

    public Content getJasperFile() {
        return this.jasperFile;
    }
}
