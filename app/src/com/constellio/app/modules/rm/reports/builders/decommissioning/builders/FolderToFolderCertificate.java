package com.constellio.app.modules.rm.reports.builders.decommissioning.builders;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.reports.model.decommissioning.FoldersCertificateReportModel.FoldersCertificateReportModel_Folder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;

public class FolderToFolderCertificate {
	final RMSchemasRecordsServices rmSchemas;

	public FolderToFolderCertificate(RMSchemasRecordsServices rmSchemas) {
		this.rmSchemas = rmSchemas;
	}

	public FoldersCertificateReportModel_Folder toReportFolder(Folder folder) {
		String id, title, category = "", retentionRuleCode, principalCopyRetentionRule;
		id = folder.getId();
		title = folder.getTitle();
		String parentFolder = folder.getParentFolder();
		if (StringUtils.isNotBlank(folder.getCategory())) {
			Category folderCategory = rmSchemas.getCategory(folder.getCategory());
			category = folderCategory.getCode();
		}
		retentionRuleCode = rmSchemas.getRetentionRule(folder.getRetentionRule()).getCode();
		principalCopyRetentionRule = folder.getMainCopyRule().toString();
		return new FoldersCertificateReportModel_Folder(id, title, category, retentionRuleCode, principalCopyRetentionRule);
	}
}
