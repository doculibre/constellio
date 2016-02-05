package com.constellio.model.conf.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.services.emails.SmtpServerTestConfig;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class EmailConfigurationsManagerAcceptanceTest extends ConstellioTest {

    CollectionsManager collectionsManager;
    private EmailServerConfiguration validConfig1;
    private EmailServerConfiguration validConfig2;
    private EmailConfigurationsManager manager;
    private String anotherCollection = "anotherCollection";

    Users users = new Users();
    private SmtpServerTestConfig validConfig3;

    @Before
    public void setUp()
            throws Exception {

        prepareSystem(
                withZeCollection().withAllTest(users),
                withCollection(anotherCollection).withAllTestUsers()
        );

        collectionsManager = getAppLayerFactory().getCollectionsManager();

        validConfig1 = new SmtpServerTestConfig();
        validConfig2 =  new SmtpServerTestConfig(){
            @Override
            public String getUsername() {
                return "username2";
            }

            @Override
            public String getDefaultSenderEmail() {
                return null;
            }
        };

        validConfig3 =  new SmtpServerTestConfig(){
            @Override
            public String getUsername() {
                return "username3";
            }

            @Override
            public String getDefaultSenderEmail() {
                return SDKPasswords.testSMTPUsername();
            }
        };

        manager = getModelLayerFactory().getEmailConfigurationsManager();
    }

    @Test
    public void givenEmailServerConfigsInTwoCollectionsThenSavedCorrectly()
            throws Exception {

        givenCollection("collection1");
        givenCollection("collection2");

        manager.addEmailServerConfiguration(validConfig1, "collection1");
        manager.addEmailServerConfiguration(validConfig2, "collection2");

        EmailServerConfiguration loadedConfig1 = manager.getEmailConfiguration("collection1", true);
        assertThat(loadedConfig1.getPassword()).isEqualTo(validConfig1.getPassword());
        assertThat(loadedConfig1.getUsername()).isEqualTo(validConfig1.getUsername());
        assertThat(loadedConfig1.getProperties().size()).isEqualTo(validConfig1.getProperties().size());
        for(String key : validConfig1.getProperties().keySet()){
            assertThat(loadedConfig1.getProperties().get(key)).isEqualTo(validConfig1.getProperties().get(key));
        }
        EmailServerConfiguration collection2Config = manager.getEmailConfiguration("collection2", true);
        assertThat(collection2Config.getUsername()).isEqualTo(validConfig2.getUsername());
        assertThat(collection2Config.getPassword()).isEqualTo(validConfig2.getPassword());
    }

    @Test
    public void whenDeleteCollectionConfigThenDeletedCorrectly()
            throws Exception {

        givenCollection("collection1");
        givenCollection("collection2");

        manager.addEmailServerConfiguration(validConfig1, "collection1");
        manager.addEmailServerConfiguration(validConfig2, "collection2");

        manager.deleteEmailServerConfiguration("collection1");

        assertThat(manager.getEmailConfiguration("collection1", false)).isNull();
        assertThat(manager.getEmailConfiguration("collection2", true).getPassword()).isEqualTo(validConfig2.getPassword());
    }

    @Test
    public void whenUpdateCollectionConfigThenUpdatedCorrectly()
            throws Exception {

        givenCollection("collection1");

        manager.addEmailServerConfiguration(validConfig1, "collection1");
        manager.updateEmailServerConfiguration(validConfig2, "collection1", true);

        EmailServerConfiguration loadedConfig2 = manager.getEmailConfiguration("collection1", true);
        assertThat(loadedConfig2.getPassword()).isEqualTo(validConfig2.getPassword());
        assertThat(loadedConfig2.getUsername()).isEqualTo(validConfig2.getUsername());
        assertThat(loadedConfig2.getDefaultSenderEmail()).isEmpty();
        assertThat(loadedConfig2.getProperties().size()).isEqualTo(validConfig2.getProperties().size());
        for(String key : validConfig2.getProperties().keySet()){
            assertThat(loadedConfig2.getProperties().get(key)).isEqualTo(validConfig2.getProperties().get(key));
        }
    }

    @Test
    public void whenUpdateCollectionConfigWithDefaultSenderEmailThenUpdatedCorrectly()
            throws Exception {

        givenCollection("collection1");

        manager.addEmailServerConfiguration(validConfig1, "collection1");
        manager.updateEmailServerConfiguration(validConfig3, "collection1", true);

        EmailServerConfiguration loadedConfig2 = manager.getEmailConfiguration("collection1", true);
        assertThat(loadedConfig2.getPassword()).isEqualTo(validConfig3.getPassword());
        assertThat(loadedConfig2.getUsername()).isEqualTo(validConfig3.getUsername());
        assertThat(loadedConfig2.getDefaultSenderEmail()).isEqualTo(validConfig3.getDefaultSenderEmail());
        assertThat(loadedConfig2.getProperties().size()).isEqualTo(validConfig3.getProperties().size());
        for(String key : validConfig2.getProperties().keySet()){
            assertThat(loadedConfig2.getProperties().get(key)).isEqualTo(validConfig3.getProperties().get(key));
        }
    }

}
