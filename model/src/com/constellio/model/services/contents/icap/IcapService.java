package com.constellio.model.services.contents.icap;

import com.constellio.data.threads.ConstellioJob;
import com.constellio.data.threads.ConstellioJobManager;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
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
public class IcapService {

	public final static class IcapPreviewLengthReaderJob extends ConstellioJob {

		private static Runnable action;

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

	public IcapService(final ModelLayerFactory modelLayerFactory) {
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
		if (modelLayerFactory.getSystemConfigs().getIcapScanActivated()) {
			icapServerUrl = modelLayerFactory.getSystemConfigs().getIcapServerUrl();

			if (StringUtils.isBlank(icapServerUrl)) {
				LOGGER.error("ICAP is activated while its server URL is missing");
			} else {
				final int icapResponseTimeout = modelLayerFactory.getSystemConfigs().getIcapResponseTimeout();
				Integer icapPreviewLengthNewValue = null;

				try {
					icapPreviewLengthNewValue = new IcapClient(URI.create(icapServerUrl), icapResponseTimeout)
							.getIcapConfigurationsFromServer().getPreviewLength();
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
	}

	public void scan(final String filename, final InputStream fileContent) {
		if (modelLayerFactory.getSystemConfigs().getIcapScanActivated()
			&& new FoldersLocator().getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			icapServerUrl = modelLayerFactory.getSystemConfigs().getIcapServerUrl();

			if (StringUtils.isBlank(icapServerUrl)) {
				LOGGER.error("ICAP is activated while its server URL is missing");
			} else {
				try {
					IcapResponse icapResponse = tryScan(filename, fileContent);

					if (icapResponse.isScanTimedout()) {
						final IcapException.TimeoutException timeoutException = new IcapException.TimeoutException(filename);
						LOGGER.info(timeoutException.getMessage() + " - " + filename);
						throw timeoutException;
					}

					if (icapResponse.isNoThreatFound()) {
						return;
					}

					String threatDescription = icapResponse.getThreatDescription();
					if (StringUtils.isEmpty(threatDescription)) {
						return;
					}

					final IcapException.ThreatFoundException threatFoundException = new IcapException.ThreatFoundException(
							icapResponse.getThreatDescription(), filename);
					LOGGER.info(
							threatFoundException.getMessage() + " " + threatFoundException.getThreatName() + " - " + filename);
					throw threatFoundException;
				} catch (final IOException e) {
					final IcapException.CommunicationFailure communicationFailureException = new IcapException.CommunicationFailure(
							e, filename);
					LOGGER.warn(communicationFailureException.getMessage() + " - " + filename, communicationFailureException);
					throw communicationFailureException;
				}
			}
		}
	}

	public IcapResponse tryScan(String filename, InputStream fileContent)
			throws IOException {
		final String constellioServerHostname = URI.create(modelLayerFactory.getSystemConfigs().getConstellioUrl()).getHost();
		final int icapResponseTimeout = modelLayerFactory.getSystemConfigs().getIcapResponseTimeout();

		return new IcapClient(URI.create(icapServerUrl), icapResponseTimeout)
				.scanFile(filename, fileContent, constellioServerHostname, icapPreviewLength);

	}

}
