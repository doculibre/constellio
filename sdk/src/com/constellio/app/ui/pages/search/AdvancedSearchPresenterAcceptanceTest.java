package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentPresenter;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentView;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Constelio on 2016-11-04.
 */
public class AdvancedSearchPresenterAcceptanceTest extends ConstellioTest {

    Users users = new Users();
    @Mock
    AdvancedSearchView advancedSearchView;
    @Mock
    CoreViews navigator;
    RMTestRecords rmRecords = new RMTestRecords(zeCollection);
    RMSchemasRecordsServices schemasRecordsServices;
    AdvancedSearchPresenter presenter;
    SessionContext sessionContext;
    @Mock
    UIContext uiContext;
    RecordServices recordServices;
    LocalDateTime now = new LocalDateTime();
    LocalDateTime shishOClock = new LocalDateTime().plusDays(1);

    MetadataSchemasManager metadataSchemasManager;
    SearchServices searchServices;

    @Before
    public void setUp()
            throws Exception {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
                        .withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
        );
        inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);

        schemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
        metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
        searchServices = getModelLayerFactory().newSearchServices();

        inCollection(zeCollection).setCollectionTitleTo("Collection de test");

        sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);

        when(advancedSearchView.getSessionContext()).thenReturn(sessionContext);
        when(advancedSearchView.getCollection()).thenReturn(zeCollection);
        when(advancedSearchView.getConstellioFactories()).thenReturn(getConstellioFactories());
        when(advancedSearchView.getUIContext()).thenReturn(uiContext);

        presenter = new AdvancedSearchPresenter(advancedSearchView);
    }

    @Test
    public void givenViewIsEnteredThenAddToCartButtonOnlyShowsWhenUserHasPermission() {
        String roleCode = users.aliceIn(zeCollection).getUserRoles().get(0);
        RolesManager rolesManager = getAppLayerFactory().getModelLayerFactory().getRolesManager();

        Role role = rolesManager.getRole(zeCollection, roleCode);
        Role editedRole = role.withPermissions(new ArrayList<String>());
        rolesManager.updateRole(editedRole);

        connectWithAlice();
        assertThat(presenter.hasCurrentUserPermissionToUseCart()).isFalse();

        Role editedRole2 = editedRole.withPermissions(asList(RMPermissionsTo.USE_CART));
        rolesManager.updateRole(editedRole2);

        connectWithAlice();
        assertThat(presenter.hasCurrentUserPermissionToUseCart()).isTrue();
    }

    private void connectWithAlice() {
        sessionContext = FakeSessionContext.aliceInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);
        when(advancedSearchView.getSessionContext()).thenReturn(sessionContext);
        presenter = new AdvancedSearchPresenter(advancedSearchView);
    }

    //
    private MetadataSchemaTypes getSchemaTypes() {
        return getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
    }

}
