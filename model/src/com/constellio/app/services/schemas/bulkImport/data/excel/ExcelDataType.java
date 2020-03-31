package com.constellio.app.services.schemas.bulkImport.data.excel;

public class ExcelDataType {

	private String typeName;
	private String separator;
	private String dateType;
	private String datePattern;
	private String dataPattern;
	private String structure;
	private int item;
	private boolean multiline = false;
	private boolean filenameHashImport;

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public boolean isFilenameHashImport() {
		return filenameHashImport;
	}

	public ExcelDataType setFilenameHashImport(boolean filenameHashImport) {
		this.filenameHashImport = filenameHashImport;
		return this;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public void setDataPattern(String dataPattern) {
		this.dataPattern = dataPattern;
	}

	public String getDataPattern() {
		return dataPattern;
	}

	public String getDateType() {
		return dateType;
	}

	public void setDateType(String dateType) {
		this.dateType = dateType;
	}

	public String getStructure() {
		return structure;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	public int getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = Integer.valueOf(item);
	}

	public boolean isMultiline() {
		return multiline;
	}

	public void setMultiline(boolean multiline) {
		this.multiline = multiline;
	}
}
