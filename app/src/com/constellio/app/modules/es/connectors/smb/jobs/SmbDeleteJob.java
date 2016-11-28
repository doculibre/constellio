package com.constellio.app.modules.es.connectors.smb.jobs;

import static com.constellio.model.services.records.RecordLogicalDeleteOptions.LogicallyDeleteTaxonomyRecordsBehavior.LOGICALLY_DELETE_THEM;
import static com.constellio.model.services.records.RecordPhysicalDeleteOptions.PhysicalDeleteTaxonomyRecordsBehavior.PHYSICALLY_DELETE_THEM;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.crawler.DeleteEventOptions;

public class SmbDeleteJob extends ConnectorJob implements SmbConnectorJob {
	private static final String jobName = SmbDeleteJob.class.getSimpleName();
	private final String url;
	private final ConnectorEventObserver eventObserver;
	private final ConnectorSmbUtils smbUtils;
	private final ConnectorSmbInstance connectorInstance;
	private final SmbService smbService;

	private final SmbRecordService smbRecordService;

	public SmbDeleteJob(Connector connector, String url, ConnectorEventObserver eventObserver, SmbRecordService smbRecordService,
			ConnectorSmbInstance connectorInstance, SmbService smbService) {
		this(connector, url, eventObserver, smbRecordService, connectorInstance, smbService, new ConnectorSmbUtils());
	}

	public SmbDeleteJob(Connector connector, String url, ConnectorEventObserver eventObserver, SmbRecordService smbRecordService,
			ConnectorSmbInstance connectorInstance, SmbService smbService, ConnectorSmbUtils smbUtils) {
		super(connector, jobName);
		this.url = url;
		this.eventObserver = eventObserver;
		this.smbRecordService = smbRecordService;
		this.smbUtils = smbUtils;
		this.connectorInstance = connectorInstance;
		this.smbService = smbService;
	}

	@Override
	public void execute(Connector connector) {
		connector.getLogger()
				.debug("Executing " + toString(), "", new LinkedHashMap<String, String>());

		if (smbUtils.isAccepted(url, connectorInstance)) {
			SmbFileDTO smbFileDTO = smbService.getSmbFileDTO(url, false);
			SmbFileDTOStatus status = smbFileDTO.getStatus();
			switch (status) {
			case DELETE_DTO:
				deleteRecords();
				break;
			case FAILED_DTO:
				// Do nothing
				break;
			case FULL_DTO:
				// Do nothing
				break;
			default:
				connector.getLogger()
						.error("Unexpected DTO status when deleting : " + url, "", new LinkedHashMap<String, String>());
				break;
			}
		} else {
			deleteRecords();
		}
	}

	private void deleteRecords() {
		if (smbUtils.isFolder(url)) {
			ConnectorSmbFolder folderToDelete = smbRecordService.getFolder(url);
			if (folderToDelete != null) {
				DeleteEventOptions options = new DeleteEventOptions();
				options.getPhysicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(PHYSICALLY_DELETE_THEM);
				options.getLogicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(LOGICALLY_DELETE_THEM);
				eventObserver.deleteEvents(options, folderToDelete);
			}
		} else {
			ConnectorSmbDocument documentToDelete = smbRecordService.getDocument(url);
			if (documentToDelete != null) {
				eventObserver.deleteEvents(documentToDelete);
			}
		}
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - " + url;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.DELETE_JOB;
	}
}