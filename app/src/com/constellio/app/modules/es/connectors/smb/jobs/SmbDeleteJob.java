package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.DocumentSmbConnectorUrlCalculator;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SmbDeleteJob extends SmbConnectorJob {
    private static final String jobName = SmbDeleteJob.class.getSimpleName();
    private final JobParams jobParams;

    public SmbDeleteJob(JobParams jobParams) {
        super(jobParams.getConnector(), jobName);
        this.jobParams = jobParams;
    }

    @Override
    public void execute(Connector connector) {
        String url = jobParams.getUrl();
        boolean forceDelete = false;
        if (this.jobParams.getConnector().getDuplicateUrls().contains(url)) {
            forceDelete = true;
        } else if (!this.jobParams.getSmbUtils().isAccepted(url, this.jobParams.getConnectorInstance())) {
            forceDelete = true;
        }
        try {
            if (forceDelete) {
                deleteByUrl(url, false);
            } else {
                SmbFileDTO smbFileDTO = jobParams.getSmbShareService().getSmbFileDTO(url, false);
                if (smbFileDTO.getStatus() == SmbFileDTO.SmbFileDTOStatus.DELETE_DTO) {
                    deleteByUrl(url, true);
                }
            }
        } catch (Exception e) {
            this.connector.getLogger().errorUnexpected(e);
        }
    }

    private void deleteByUrl(String url, boolean withChildren) {
        ModelLayerFactory modelLayerFactory = jobParams.getEventObserver().getModelLayerFactory();
        RecordDao recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();
        TransactionDTO transaction = new TransactionDTO(RecordsFlushing.LATER());
        ConnectorSmbInstance connectorInstance = jobParams.getConnectorInstance();
        String connectorUrl = DocumentSmbConnectorUrlCalculator.calculate(url, connectorInstance.getId());
        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams();
        connectorUrl = ClientUtils.escapeQueryChars(connectorUrl);
        if (StringUtils.endsWith(url,"/") && withChildren) {
            modifiableSolrParams.set("q", "connectorUrl_s:" + connectorUrl + "*");
        } else {
            modifiableSolrParams.set("q", "connectorUrl_s:" + connectorUrl);
        }
        modifiableSolrParams.add("fq", "id:*ZZ");
        modifiableSolrParams.add("fq", "collection_s:"+connectorInstance.getCollection());
        transaction = transaction.withDeletedByQueries(modifiableSolrParams);
        try {
            recordDao.execute(transaction);
        } catch (RecordDaoException.OptimisticLocking optimisticLocking) {
            optimisticLocking.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return jobName + '@' + Integer.toHexString(hashCode()) + " - " + jobParams.getUrl();
    }

    @Override
    public String getUrl() {
        return jobParams.getUrl();
    }
}