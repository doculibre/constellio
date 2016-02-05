package com.constellio.app.modules.rm.reports.builders.decommissioning.builders;

import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel.DocumentsCertificateReportModel_Document;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.services.records.RecordServicesRuntimeException;

public class DocumentToDocumentCertificate {
	final RMSchemasRecordsServices rmSchemas;

	public DocumentToDocumentCertificate(RMSchemasRecordsServices rmSchemas) {
		this.rmSchemas = rmSchemas;
	}

	public DocumentsCertificateReportModel_Document toReportDocument(Document document) {
		String id, title, md5 = "", filename = "", folder = "", retentionRuleCode = "", principalCopyRetentionRule = "";
		id = document.getId();
		title = document.getTitle();
		Content content = document.getContent();
		if (content != null) {
			ContentVersion currentVersion = content.getCurrentVersion();
			md5 = currentVersion.getHash();
			filename = currentVersion.getFilename();
		}
		if (document.getFolder() != null) {
			Folder documentFolder = rmSchemas.getFolder(document.getFolder());
			folder = documentFolder.getTitle();
			principalCopyRetentionRule = documentFolder.getMainCopyRule().toString();
		}
		RetentionRule documentRetentionRule = rmSchemas
				.getRetentionRule(document.getRetentionRule());
		retentionRuleCode = documentRetentionRule.getCode();

		return new DocumentsCertificateReportModel_Document(id, title, md5, filename, folder, retentionRuleCode,
				principalCopyRetentionRule);
	}
}
