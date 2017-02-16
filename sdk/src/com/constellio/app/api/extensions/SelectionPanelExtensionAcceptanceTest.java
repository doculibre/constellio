package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.ConstellioHeaderImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created by Constelio on 2016-10-19.
 */
public class SelectionPanelExtensionAcceptanceTest extends ConstellioTest {
    AppLayerFactory appLayerFactory;
    RMTestRecords records = new RMTestRecords(zeCollection);

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );
        appLayerFactory = getAppLayerFactory();
    }

    @Test
    public void givenSelectionPanelIsBuiltThenExtensionIsCalled() {
        SelectionPanelExtension selectionPanelExtension = spy(new SelectionPanelExtension());
        appLayerFactory.getExtensions().forCollection(zeCollection).selectionPanelExtensions.add(selectionPanelExtension);

        new ConstellioHeaderImpl() {
            @Override
            public SessionContext getSessionContext() {
                return FakeSessionContext.adminInCollection(zeCollection);
            }

            @Override
            protected Component buildAdvancedSearchUI() {
                return new VerticalLayout();
            }

            @Override
            protected MenuBar buildCollectionMenu() {
                return new MenuBar();
            }

            @Override
            public Navigator getNavigator() {
                return mock(Navigator.class);
            }
        };
        verify(selectionPanelExtension, times(1)).addAvailableActions(any(AvailableActionsParam.class));
    }
}
