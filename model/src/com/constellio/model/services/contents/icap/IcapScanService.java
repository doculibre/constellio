package com.constellio.model.services.contents.icap;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.ConstellioJob;
import com.constellio.data.threads.ConstellioJobManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeConstants;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 *
 */
public class IcapScanService implements StatefulService {

    public static class IcapPreviewLengthReaderJob extends ConstellioJob {
        private static final int PERIOD = DateTimeConstants.SECONDS_PER_HOUR;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioJob.class);

    private final ModelLayerFactory modelLayerFactory;

    private final ConstellioJobManager constellioJobManager;

    private final String constellioServerHostname;

    private String icapServerUrl;

    private boolean activated;

    private Integer icapPreviewLength = 30;

    public IcapScanService(final ModelLayerFactory modelLayerFactory) {
        super();

        this.modelLayerFactory = modelLayerFactory;
        constellioJobManager = modelLayerFactory.getDataLayerFactory().getConstellioJobManager();
        constellioServerHostname = URI.create(modelLayerFactory.getSystemConfigs().getConstellioUrl()).getHost();
    }

    @Override
    public void initialize() {
        icapServerUrl = modelLayerFactory.getSystemConfigs().getIcapServerUrl();

        activated = StringUtils.isNotBlank(icapServerUrl);

        if (activated) {
            //
            final ConstellioJob.Action action = new ConstellioJob.Action() {
                @Override
                public void run() {
                    refreshPreviewLenghth();
                }
            };

            //
            try {
                constellioJobManager.addJob(
                        IcapPreviewLengthReaderJob.class,
                        IcapPreviewLengthReaderJob.class.getSimpleName(),
                        IcapPreviewLengthReaderJob.PERIOD,
                        action,
                        false,
                        true);
            } catch (final SchedulerException e) {
                LOGGER.error("ICAP preview length reader job can't be scheduled", e);
            }

            //
            LOGGER.error("ICAP preview length reader job successfully scheduled");
        }
    }

    public void refreshPreviewLenghth() {
        try {
            final Integer icapPreviewLengthNewValue = new IcapClient(URI.create(icapServerUrl), null).getIcapConfigurationsFromServer().getPreviewLength();

            if (icapPreviewLengthNewValue != null) {
                this.icapPreviewLength = icapPreviewLengthNewValue;
            }
        } catch (final IOException e) {
            // TODO
        }
    }

    @Override
    public void close() {
        // Do nothing
    }

    public void scan(final String filename, final InputStream fileContent, final String clientHostname) throws IcapScanException {
        try {
            final IcapResponse icapResponse = new IcapClient(URI.create(icapServerUrl), null).scanFile(filename, fileContent, constellioServerHostname, icapPreviewLength);

            if (icapResponse.isClear()) {
                return;
            }

            if (icapResponse.isTimedout()) {
                throw new IcapScanException("Document '" + filename + "' ICAP scanning has timed out");
            }

            throw new IcapScanException(icapResponse.getThreatDescription());
        } catch (final IOException e) {
            throw new IcapScanException("ICAP communication error while scanning document '" + filename + "'", e);
        }
    }

}
