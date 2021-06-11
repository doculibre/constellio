package com.constellio.app.modules.rm.services.reports.xml.legacy.parameters;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.base.Strings;

import java.util.List;

// Use XMLDataSourceGeneratorFactory instead
@Deprecated
public abstract class AbstractXmlGeneratorParameters {

	private Record[] recordsElements;

	private List<String> ids;
	private String schemaCode;
	private LogicalSearchQuery query;

	private boolean isForTest = false;

	/**
	 * Instanciate a new instance of xml parameters, can be used with no parameters.
	 *
	 * @param recordsElements
	 */
	public AbstractXmlGeneratorParameters(Record... recordsElements) {
		this.setRecordsElements(recordsElements);
	}

	public AbstractXmlGeneratorParameters(LogicalSearchQuery query) {
		this.setQuery(query);
	}

	/**
	 * Set the records element
	 *
	 * @param recordsElements
	 * @return
	 */
	public AbstractXmlGeneratorParameters setRecordsElements(Record... recordsElements) {
		this.recordsElements = recordsElements;
		return this;
	}

	/**
	 * Set the record with ids and schema code
	 *
	 * @param schemaCode
	 * @param ids
	 */
	public void setElementWithIds(String schemaCode, List<String> ids) {
		this.schemaCode = schemaCode;
		this.ids = ids;
	}

	/**
	 * Returns the ids of the elements if it's sets.
	 *
	 * @return list of ids.
	 */
	public List<String> getIdsOfElement() {
		return this.ids;
	}

	/**
	 * Returns the schema code of the ids.
	 *
	 * @return String of schema code.
	 */
	public String getSchemaCode() {
		return this.schemaCode;
	}

	/**
	 * Returns the record elements
	 *
	 * @return array of Record
	 */
	public Record[] getRecordsElements() {
		return this.recordsElements;
	}

	public boolean isParametersUsingIds() {
		return recordsElements.length == 0 && ids.size() > 0 && !Strings.isNullOrEmpty(this.schemaCode);
	}

	/**
	 * Set the test flag to true.
	 * Used when generating a test XML, to fill the empty tags.
	 *
	 * @return this instance.
	 */
	public AbstractXmlGeneratorParameters markAsTestXml() {
		isForTest = true;
		return this;
	}

	/**
	 * Set the test flag to false
	 * Used when generating a test XML, to fill then empty tags
	 * the flag is false by default.
	 *
	 * @return this instance.
	 */
	public AbstractXmlGeneratorParameters markNotAsTestXml() {
		isForTest = false;
		return this;
	}

	/**
	 * Returns whether or not the xml is flag as test.
	 *
	 * @return boolean. is for test.
	 */
	public boolean isForTest() {
		return isForTest;
	}

	/**
	 * Method used to validate if the input are correct.
	 * Usually throws exception.
	 * Widely unused.
	 */
	public abstract void validateInputs();

	public LogicalSearchQuery getQuery() {
		return query;
	}

	public AbstractXmlGeneratorParameters setQuery(LogicalSearchQuery query) {
		this.query = query;
		return this;
	}
}
