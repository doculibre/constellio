import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.pages.management.schemas.metadata.AddEditMetadataPresenter;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.*;
import static org.mockito.Mockito.when;

/**
 * Created by Constelio on 2016-10-31.
 */
public class AdministrativeUnitCleanerAcceptanceTest extends ConstellioTest{
    RMTestRecords records = new RMTestRecords(zeCollection);

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );
        inCollection(zeCollection).setCollectionTitleTo("Collection de test");
    }

    @Test
    @InDevelopmentTest
    public void whenCleaningAdministrativeUnitsThenFoldersNoLongerExist() {
        AdministrativeUnitCleaner.clean(zeCollection, getAppLayerFactory());
        newWebDriver();
        waitUntilICloseTheBrowsers();
    }

    @Test
    public void whenCleaningAdministrativeUnitsThenContainersNoLongerExist() {

    }
}
