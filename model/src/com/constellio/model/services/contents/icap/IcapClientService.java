package com.constellio.model.services.contents.icap;

import static com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_IcapCommunicationFailure;
import static com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_IcapScanTimedout;
import static com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_IcapScanThreatFound;

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
public class IcapClientService {

    public static class IcapPreviewLengthReaderJob extends ConstellioJob {
        private static final int PERIOD = DateTimeConstants.SECONDS_PER_HOUR;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioJob.class);

    private final ModelLayerFactory modelLayerFactory;

    private final ConstellioJobManager constellioJobManager;

    private String icapServerUrl;

    private Integer icapPreviewLength = 30;

    public IcapClientService(final ModelLayerFactory modelLayerFactory) {
        this.modelLayerFactory = modelLayerFactory;
        constellioJobManager = modelLayerFactory.getDataLayerFactory().getConstellioJobManager();
    }

    public void init() {
        //
        final ConstellioJob.Action action = new ConstellioJob.Action() {
            @Override
            public void run() {
                readPreviewLengthFromIcapServer();
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
        LOGGER.info("ICAP preview length reader job successfully scheduled");
    }

    void readPreviewLengthFromIcapServer() {
        icapServerUrl = modelLayerFactory.getSystemConfigs().getIcapServerUrl();

        if (StringUtils.isNotBlank(icapServerUrl)) {
            Integer icapPreviewLengthNewValue = null;

            try {
                icapPreviewLengthNewValue = new IcapClient(URI.create(icapServerUrl), null).getIcapConfigurationsFromServer().getPreviewLength();
            } catch (final IOException e) {
                LOGGER.warn("Communication error while reading preview length from ICAP server");
            }

            if (icapPreviewLengthNewValue == null) {
                LOGGER.warn("missing preview header in ICAP OPTIONS method response");
            } else {
                icapPreviewLength = icapPreviewLengthNewValue;
            }
        }
    }

    public void scan(final String filename, final InputStream fileContent) {
        icapServerUrl = modelLayerFactory.getSystemConfigs().getIcapServerUrl();

        if (StringUtils.isNotBlank(icapServerUrl)) {
            try {
                final String constellioServerHostname = URI.create(modelLayerFactory.getSystemConfigs().getConstellioUrl()).getHost();

                final IcapResponse icapResponse = new IcapClient(URI.create(icapServerUrl), null).scanFile(filename, fileContent, constellioServerHostname, icapPreviewLength);

                if (icapResponse.isClear()) {
                    return;
                }

                if (icapResponse.isTimedout()) {
                    throw new ContentManagerRuntimeException_IcapScanTimedout();
                }

                throw new ContentManagerRuntimeException_IcapScanThreatFound(icapResponse.getThreatDescription());
            } catch (final IOException e) {
                throw new ContentManagerRuntimeException_IcapCommunicationFailure(e);
            }
        }
    }

}
