package com.constellio.model.services.contents.icap;

import com.constellio.data.threads.ConstellioJob;
import com.constellio.data.threads.ConstellioJobManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class IcapClientService {

    public final static class IcapPreviewLengthReaderJob extends ConstellioJob {

        private static Runnable action;

        @Override
        protected String name() {
            return IcapPreviewLengthReaderJob.class.getSimpleName();
        }

        @Override
        protected Runnable action() {
            return action;
        }

        @Override
        protected boolean unscheduleOnException() {
            return false;
        }

        @Override
        protected Set<Integer> intervals() {
            return new TreeSet<>(Arrays.asList(new Integer[]{DateTimeConstants.SECONDS_PER_HOUR}));
        }

        @Override
        protected Set<String> cronExpressions() {
            return null;
        }

        @Override
        protected Date startTime() {
            return DateTime.now().toDate();
        }

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
        IcapPreviewLengthReaderJob.action = new Runnable() {
            @Override
            public void run() {
                readPreviewLengthFromIcapServer();
            }
        };

        //
        constellioJobManager.addJob(new IcapPreviewLengthReaderJob(), true);
    }

    void readPreviewLengthFromIcapServer() {
        icapServerUrl = modelLayerFactory.getSystemConfigs().getIcapServerUrl();

        if (StringUtils.isNotBlank(icapServerUrl)) {
            Integer icapPreviewLengthNewValue = null;

            try {
                icapPreviewLengthNewValue = new IcapClient(URI.create(icapServerUrl), null).getIcapConfigurationsFromServer().getPreviewLength();
            } catch (final IOException e) {
                LOGGER.warn("communication error while reading preview length from ICAP server");
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

                if (icapResponse.isScanTimedout()) {
                    final IcapClientException.IcapScanTimedout exception_icapScanTimedout = new IcapClientException.IcapScanTimedout();
                    LOGGER.info(exception_icapScanTimedout.getMessage());
                    throw exception_icapScanTimedout;
                }

                if (icapResponse.isNoThreatFound()) {
                    return;
                }

                final IcapClientException.IcapScanThreatFound exception_icapScanThreatFound = new IcapClientException.IcapScanThreatFound(icapResponse.getThreatDescription());
                LOGGER.info(exception_icapScanThreatFound.getMessage() + " - " + exception_icapScanThreatFound.getThreatName());
                throw exception_icapScanThreatFound;
            } catch (final IOException e) {
                final IcapClientException.IcapCommunicationFailure exception_icapCommunicationFailure = new IcapClientException.IcapCommunicationFailure(e);
                LOGGER.warn(exception_icapCommunicationFailure.getMessage(), exception_icapCommunicationFailure);
                throw exception_icapCommunicationFailure;
            }
        }
    }

}
