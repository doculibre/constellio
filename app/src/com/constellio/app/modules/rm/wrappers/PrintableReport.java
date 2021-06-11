package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.enums.TemplateVersionType;
import com.constellio.app.modules.rm.services.reports.printable.PrintableExtension;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class PrintableReport extends Printable {
	public final static String SCHEMA_TYPE = "report";
	public final static String SCHEMA_NAME = Printable.SCHEMA_TYPE + "_" + SCHEMA_TYPE;

	public final static String RECORD_TYPE = "recordType";
	public final static String RECORD_SCHEMA = "recordSchema";
	public final static String JASPER_SUBREPORT_FILES = "jasperSubreportFiles";
	public final static String SUPPORTED_EXTENSIONS = "supportedExtensions";
	public final static String ADD_PARENTS = "addParents";
	public final static String ADD_CHILDREN = "addChildren";
	public final static String OPTIMIZED = "optimized";
	public final static String DEPTH = "depth";

	public PrintableReport(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_NAME);
	}

	public PrintableReport setReportType(String reportType) {
		set(RECORD_TYPE, reportType);
		return this;
	}

	public String getReportType() {
		return get(RECORD_TYPE);
	}

	public String getSchemaType() {
		return SCHEMA_TYPE;
	}

	public PrintableReport setReportSchema(String schema) {
		set(RECORD_SCHEMA, schema);
		return this;
	}

	public PrintableReport setTemplateVersion(TemplateVersionType templateVersion) {
		set(TEMPLATE_VERSION, templateVersion);
		return this;
	}

	public String getReportSchema() {
		return get(RECORD_SCHEMA);
	}

	public List<Content> getJasperSubreportFiles() {
		return get(JASPER_SUBREPORT_FILES);
	}

	public PrintableReport setJasperSubreportFiles(List<Content> files) {
		set(JASPER_SUBREPORT_FILES, files);
		return this;
	}

	public List<PrintableExtension> getSupportedExtensions() {
		return get(SUPPORTED_EXTENSIONS);
	}

	public PrintableReport setSupportedExtensions(List<PrintableExtension> supportedExtensions) {
		set(SUPPORTED_EXTENSIONS, supportedExtensions);
		return this;
	}

	public PrintableReport setAddParents(Boolean addParents) {
		set(ADD_PARENTS, addParents);
		return this;
	}

	public Boolean getAddParents() {
		return getBooleanWithDefaultValue(ADD_PARENTS, false);
	}

	public PrintableReport setAddChildren(Boolean addChildren) {
		set(ADD_CHILDREN, addChildren);
		return this;
	}

	public Boolean getAddChildren() {
		return getBooleanWithDefaultValue(ADD_CHILDREN, false);
	}

	public PrintableReport setOptimized(Boolean optimized) {
		set(OPTIMIZED, optimized);
		return this;
	}

	public boolean isOptimized() {
		return getBooleanWithDefaultValue(OPTIMIZED, false);
	}

	public PrintableReport setDepth(Integer depth) {
		set(DEPTH, depth);
		return this;
	}

	public Integer getDepth() {
		return getInteger(DEPTH);
	}
}
