package com.constellio.app.modules.rm.reports.model.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.io.streamFactories.StreamFactory;

public class FoldersCertificateReportModel {

	private StreamFactory<InputStream> headerLogo;
	private String certificateCreationDate = "";
	private String destructionDate = "";
	private String hash = "";
	private boolean cellBorder = false;
	private List<FoldersCertificateReportModel_Folder> folders = new ArrayList<>();

	public StreamFactory<InputStream> getHeaderLogo() {
		return headerLogo;
	}

	public FoldersCertificateReportModel setHeaderLogo(StreamFactory<InputStream> headerLogo) {
		this.headerLogo = headerLogo;
		return this;
	}

	public String getTitle() {
		String title = "FoldersCertificateReport.Title";
		return $(title);
	}

	public List<FoldersCertificateReportModel_Folder> getFolders() {
		return folders;
	}

	public void setFolders(List<FoldersCertificateReportModel_Folder> folders) {
		this.folders = folders;
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

	public FoldersCertificateReportModel setDate(LocalDate date) {
		this.destructionDate = DateFormatUtils.format(date);
		this.certificateCreationDate = this.destructionDate;
		return this;
	}

	public static class FoldersCertificateReportModel_Folder {

		String id = "";

		String title = "";

		String folder = "";

		String retentionRuleCode = "";

		String principalCopyRetentionRule = "";

		public FoldersCertificateReportModel_Folder() {
		}

		public FoldersCertificateReportModel_Folder(String id, String title, String folder, String retentionRuleCode,
				String principalCopyRetentionRule) {
			this.id = id;
			this.title = title;
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
