package com.constellio.app.modules.rm.reports.model.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.io.streamFactories.StreamFactory;

public class DocumentsCertificateReportModel {

	private StreamFactory<InputStream> headerLogo;
	private String certificateCreationDate = "";
	private String destructionDate = "";
	private String hash = "";
	private boolean cellBorder = false;
	private List<DocumentsCertificateReportModel_Document> documents = new ArrayList<>();

	public StreamFactory<InputStream> getHeaderLogo() {
		return headerLogo;
	}

	public DocumentsCertificateReportModel setHeaderLogo(StreamFactory<InputStream> headerLogo) {
		this.headerLogo = headerLogo;
		return this;
	}

	public String getTitle() {
		String title = "DocumentsCertificateReport.Title";
		return $(title);
	}

	public List<DocumentsCertificateReportModel_Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<DocumentsCertificateReportModel_Document> documents) {
		this.documents = documents;
	}

	public String getCertificateCreationDate() {
		return certificateCreationDate;
	}

	public void setCertificateCreationDate(String certificateCreationDate) {
		this.certificateCreationDate = certificateCreationDate;
	}

	public String getDestructionDate() {
		return destructionDate;
	}

	public void setDestructionDate(String destructionDate) {
		this.destructionDate = destructionDate;
	}

	public DocumentsCertificateReportModel setDate(LocalDate date) {
		this.destructionDate = DateFormatUtils.format(date);
		this.certificateCreationDate = this.destructionDate;
		return this;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public boolean hasCellBorder() {
		return cellBorder;
	}

	public void setCellBorder(boolean cellBorder) {
		this.cellBorder = cellBorder;
	}

	public static class DocumentsCertificateReportModel_Document {

		String id = "";

		String title = "";

		String md5 = "";

		String filename = "";

		String folder = "";

		String retentionRuleCode = "";

		String principalCopyRetentionRule = "";

		public DocumentsCertificateReportModel_Document() {
		}

		public DocumentsCertificateReportModel_Document(String id, String title, String md5, String filename, String folder,
				String retentionRuleCode,
				String principalCopyRetentionRule) {
			this.id = id;
			this.title = title;
			this.md5 = md5;
			this.filename = filename;
			this.folder = folder;
			this.retentionRuleCode = retentionRuleCode;
			this.principalCopyRetentionRule = principalCopyRetentionRule;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getMd5() {
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getFolder() {
			return folder;
		}

		public void setFolder(String folder) {
			this.folder = folder;
		}

		public String getRetentionRuleCode() {
			return retentionRuleCode;
		}

		public void setRetentionRuleCode(String retentionRuleCode) {
			this.retentionRuleCode = retentionRuleCode;
		}

		public String getPrincipalCopyRetentionRule() {
			return principalCopyRetentionRule;
		}

		public void setPrincipalCopyRetentionRule(String principalCopyRetentionRule) {
			this.principalCopyRetentionRule = principalCopyRetentionRule;
		}
	}
}
