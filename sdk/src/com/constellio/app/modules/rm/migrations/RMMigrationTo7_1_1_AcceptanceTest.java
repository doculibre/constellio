package com.constellio.app.modules.rm.migrations;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

/**
 * Created by Charles Blanchette on 2017-03-10.
 */
public class RMMigrationTo7_1_1_AcceptanceTest extends ConstellioTest {

    @Test
    public void whenUpdatingFrom7_1ThenMigrateBorrowing()
            throws OptimisticLockingConfiguration {

        givenDisabledAfterTestValidations();

    }
}
