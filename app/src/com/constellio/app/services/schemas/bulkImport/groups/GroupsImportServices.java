package com.constellio.app.services.schemas.bulkImport.groups;

import com.constellio.app.services.schemas.bulkImport.BulkImportResults;
import com.constellio.app.services.schemas.bulkImport.ImportError;
import com.constellio.app.services.schemas.bulkImport.groups.ImportedGroupValidatorRuntimeException.ImportedGroupValidatorRuntimeException_GroupCodeIsMissing;
import com.constellio.app.services.schemas.bulkImport.groups.ImportedGroupValidatorRuntimeException.ImportedGroupValidatorRuntimeException_GroupTitleIsMissing;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupsImportServices {
	private static final Logger LOGGER = LogManager.getLogger(GroupsImportServices.class);
	public static final String CODE_MISSING = "codeMissing";
	private static final String TITLE_MISSING = "titleMissing";

	public BulkImportResults bulkImport(File file, List<String> collections, ModelLayerFactory modelLayerFactory) {
		BulkImportResults result = new BulkImportResults();
		if (file != null) {
			Document document = getDocumentFromFile(file);
			bulkImport(document, result, collections, modelLayerFactory);
		}
		return result;
	}

	private void bulkImport(Document document, BulkImportResults bulkImportResults, List<String> collections,
							ModelLayerFactory modelLayerFactory) {
		try {
			List<ImportedGroup> allImportedGroups = new ImportedGroupReader(
					document).readAll();
			List<ImportedGroup> validGroups = addInvalidGroupsToErrorsAndGetValidGroups(
					allImportedGroups, bulkImportResults);
			addUpdateOrDeleteGroups(validGroups, collections, modelLayerFactory, bulkImportResults);
		} catch (Exception e) {
			bulkImportResults.add(new ImportError("", e.getMessage()));
		}

	}

	private void addUpdateOrDeleteGroups(List<ImportedGroup> importedGroups, List<String> collections,
										 ModelLayerFactory modelLayerFactory, BulkImportResults bulkImportResults) {
		if (!importedGroups.isEmpty()) {
			UserServices userServices = modelLayerFactory.newUserServices();
			for (ImportedGroup importedGroup : importedGroups) {
				try {
					addUpdateOrDeleteGroup(importedGroup, collections, userServices);
					bulkImportResults.inc();
				} catch (Exception e) {
					bulkImportResults.add(new ImportError(importedGroup.getCode(), e.getMessage()));
				}
			}
			modelLayerFactory.getBatchProcessesManager().waitUntilAllFinished();
		}
	}

	void addUpdateOrDeleteGroup(ImportedGroup importedGroup, List<String> collections,
								UserServices userServices) {
		GroupAddUpdateRequest globalGroup = userServices.createGlobalGroup(importedGroup.getCode(), importedGroup.getTitle(), collections,
				importedGroup.getParent(), GlobalGroupStatus.ACTIVE, true);
		userServices.execute(globalGroup);
	}

	//TODO tester à mort
	List<ImportedGroup> addInvalidGroupsToErrorsAndGetValidGroups(
			List<ImportedGroup> allImportedGroups,
			BulkImportResults bulkImportResults) {
		List<String> addedGroupsCodes = new ArrayList<>();
		List<ImportedGroup> validGroups = new ArrayList<>();
		ImportedGroupValidator importedGroupValidator = new ImportedGroupValidator();

		for (ImportedGroup importedGroup : allImportedGroups) {
			try {
				importedGroupValidator.validate(importedGroup);
				validGroups.add(importedGroup);
			} catch (ImportedGroupValidatorRuntimeException e) {
				bulkImportResults.add(new ImportError(importedGroup.getCode(), getErrorCode(e)));
			}
		}
		return validGroups;
	}

	private String getErrorCode(ImportedGroupValidatorRuntimeException e) {
		if (e instanceof ImportedGroupValidatorRuntimeException_GroupCodeIsMissing) {
			return CODE_MISSING;
		} else if (e instanceof ImportedGroupValidatorRuntimeException_GroupTitleIsMissing) {
			return TITLE_MISSING;
		} else {
			throw new RuntimeException("Unsupported exception");
		}
	}

	private Document getDocumentFromFile(File file) {
		SAXBuilder builder = new SAXBuilder();
		try {
			return builder.build(file);
		} catch (JDOMException e) {
			throw new RuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new RuntimeException("build Document JDOM2 from file", e);
		}
	}

}
