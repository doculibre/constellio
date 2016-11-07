package com.constellio.model.services.contents.icap;

import static com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_IcapCommunicationFailure;
import static com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_IcapScanTimedout;
import static com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_IcapScanThreatFound;

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
                    throw new ContentManagerRuntimeException_IcapScanTimedout();
                }

                if (icapResponse.isNoThreatFound()) {
                    return;
                }

                throw new ContentManagerRuntimeException_IcapScanThreatFound(icapResponse.getThreatDescription());
            } catch (final IOException e) {
                throw new ContentManagerRuntimeException_IcapCommunicationFailure(e);
            }
        }
    }

}
