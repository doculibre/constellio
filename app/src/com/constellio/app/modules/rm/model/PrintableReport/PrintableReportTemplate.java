package com.constellio.app.modules.rm.model.PrintableReport;

import com.constellio.model.entities.records.Content;

import java.io.Serializable;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PrintableReportTemplate that = (PrintableReportTemplate) o;

		if (title != null ? !title.equals(that.title) : that.title != null) {
			return false;
		}
		if (id != null ? !id.equals(that.id) : that.id != null) {
			return false;
		}
		return jasperFile != null ? jasperFile.equals(that.jasperFile) : that.jasperFile == null;
	}

	@Override
	public int hashCode() {
		int result = title != null ? title.hashCode() : 0;
		result = 31 * result + (id != null ? id.hashCode() : 0);
		result = 31 * result + (jasperFile != null ? jasperFile.hashCode() : 0);
		return result;
	}
}
