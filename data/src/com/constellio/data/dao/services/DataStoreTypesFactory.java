package com.constellio.data.dao.services;

public interface DataStoreTypesFactory {

	String forString(boolean multivalue);

	String forText(boolean multivalue);

	String forDouble(boolean multivalue);

	String forDate(boolean multivalue);

	String forDateTime(boolean multivalue);

	String forBoolean(boolean multivalue);

}
