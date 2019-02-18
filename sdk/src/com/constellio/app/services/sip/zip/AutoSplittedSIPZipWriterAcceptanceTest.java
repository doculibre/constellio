package com.constellio.app.services.sip.zip;

import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static java.util.Locale.CANADA_FRENCH;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

public class AutoSplittedSIPZipWriterAcceptanceTest extends ConstellioTest {

	@Test
	public void givenLotOfFilesThenSplittedByBatchOf250K() throws IOException {

		File outputFolder = new File("/Users/francisbaril/Downloads/test");
		FileUtils.deleteQuietly(outputFolder);
		outputFolder.mkdirs();

		SIPFileNameProvider fileNameProvider = new DefaultSIPFileNameProvider(outputFolder, "sip");
		SIPZipBagInfoFactory bagInfoFactory = new DefaultSIPZipBagInfoFactory(getAppLayerFactory(), CANADA_FRENCH);
		AutoSplittedSIPZipWriter writer = new AutoSplittedSIPZipWriter(getAppLayerFactory(), fileNameProvider, 1_000_000_000, bagInfoFactory);

		writer.addDivisionInfo(new MetsDivisionInfo("everything", null, "Everything", "data"));
		for (int i = 0; i < 1_000_000; i++) {
			if (i % 100 == 0) {
				System.out.println(i);
			}
			File tempFile = new File(newTempFolder(), "temp");
			FileUtils.writeStringToFile(tempFile, "" + i, "UTF-8");

			SIPZipWriterTransaction tx = writer.newInsertTransaction();

			tx.addContentFile("everything/" + i, readFileToByteArray(tempFile)).setTitle("Ze file " + i).setId("" + i).setDmdid("everything");

			writer.insertAll(tx);
			tempFile.delete();
		}
		writer.close();

	}
}
