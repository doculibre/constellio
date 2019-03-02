import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.RMSelectedFoldersAndDocumentsSIPBuilder;
import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;
import com.constellio.app.services.sip.record.CollectionInfosSIPWriter;
import com.constellio.app.services.sip.zip.FileSIPZipWriter;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.data.dao.services.idGenerator.InMemorySequentialGenerator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class CollectionInfosSIPWriterAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RMSchemasRecordsServices rm;
	IOServices ioServices;
	RMSelectedFoldersAndDocumentsSIPBuilder constellioSIP;

	@Before
	public void setUp() throws Exception {
		records.copyBuilder = new CopyRetentionRuleBuilder(new InMemorySequentialGenerator());
		givenTimeIs(new LocalDateTime(2018, 1, 2, 3, 4, 5));
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList());
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenRMModuleInstalledWhenThenAlsoExportRMTools() throws IOException {

		File sipFile = new File(newTempFolder(), "archive.zip");
		SIPZipBagInfoFactory bagInfoFactory = new DefaultSIPZipBagInfoFactory(getAppLayerFactory(), Locale.FRENCH);
		SIPZipWriter zipWriter = new FileSIPZipWriter(getAppLayerFactory(), sipFile, "test", bagInfoFactory);
		CollectionInfosSIPWriter writer = new CollectionInfosSIPWriter(zeCollection, getAppLayerFactory(), zipWriter, Locale.FRENCH, new ProgressInfo());

		writer.exportCollectionConfigs();
		writer.close();

		unzipInDownloadFolder(sipFile, "testSIP");

		//assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip2.zip")));
	}

	private void unzipInDownloadFolder(File sipFile, String name) {
		File folder = new File("/Users/francisbaril/Downloads/" + name);
		try {
			FileUtils.deleteDirectory(folder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		folder.mkdirs();

		try {
			getIOLayerFactory().newZipService().unzip(sipFile, folder);
		} catch (ZipServiceException e) {
			throw new RuntimeException(e);
		}
	}
}
