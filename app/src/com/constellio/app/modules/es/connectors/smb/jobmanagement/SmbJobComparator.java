package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class SmbJobComparator implements Comparator<ConnectorJob> {
	// Parent before child
	// New document before modified document
	// Modified document before non modified document
	// Lower levels firsts

	@Override
	public int compare(ConnectorJob o1, ConnectorJob o2) {
		SmbConnectorJob job1 = (SmbConnectorJob) o1;
		SmbConnectorJob job2 = (SmbConnectorJob) o2;

		String url1 = job1.getUrl();
		String url2 = job2.getUrl();

		int url1Level = StringUtils.countMatches(url1, "/");
		int url2Level = StringUtils.countMatches(url2, "/");

		// All considered at the same level
		// smb://ip/share/
		// smb://ip/share/afile.txt
		// smb://ip/share/file.txt
		if (url1Level == url2Level) {

			// For the same job type at the same level it is of equal priority
			// smb://ip/share/newfile1.txt
			// smb://ip/share/newfile2.txt
			if (job1.getType()
					.equals(job2.getType())) {

				if (StringUtils.equals(url1, url2)) {
					return 0;
				}

				String[] unsorted = { url1, url2 };
				Arrays.sort(unsorted);
				if (StringUtils.equals(unsorted[0], url1)) {
					return -1;
				} else {
					return 1;
				}
			} else {
				if (job1.getType()
						.getPriority() < job2.getType()
						.getPriority()) {
					return -1;
				} else {
					return 1;
				}
			}
		} else {
			// Less slashes in the path is lower level so first
			// smb://ip/share/
			// smb://ip/share/folder/
			if (url1Level < url2Level) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}