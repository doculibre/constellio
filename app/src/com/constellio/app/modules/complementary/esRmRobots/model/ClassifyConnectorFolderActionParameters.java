package com.constellio.app.modules.complementary.esRmRobots.model;

import org.joda.time.LocalDate;

import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.model.entities.records.Content;

public interface ClassifyConnectorFolderActionParameters {

	String getInTaxonomy();

	ActionAfterClassification getActionAfterClassification();

	String getDelimiter();

	Content getFolderMapping();

	Content getDocumentMapping();

	String getDefaultAdminUnit();

	String getDefaultParentFolder();

	String getDefaultCategory();

	LocalDate getDefaultOpenDate();

	String getDefaultRetentionRule();

	CopyType getDefaultCopyStatus();

	String getPathPrefix();
}
