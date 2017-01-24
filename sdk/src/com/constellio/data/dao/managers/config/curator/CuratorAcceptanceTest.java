package com.constellio.data.dao.managers.config.curator;

import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CuratorAcceptanceTest {

    private CuratorFramework client;
    @Mock private TreeCacheListener listener;

    @Before
    public void setUp() {
        //FIXME
        File configFile = new FoldersLocator().getConstellioProperties();
        Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);
        String zhHost = new PropertiesDataLayerConfiguration(configs, null, null, null).getRecordsDaoCloudSolrServerZKHost();
        client = CuratorFrameworkFactory.newClient(zhHost, new ExponentialBackoffRetry(1000, 10));
        client.start();
    }

    @After
    public void cleanup() {
        CloseableUtils.closeQuietly(client);
    }

    @Test
    public void givenTreeCacheWhenChangeThenNotified() throws Exception {
        TreeCache cache = new TreeCache(client, "/configs/records");
        cache.start();
        cache.getListenable().addListener(listener);

        client.setData().forPath("/configs/records", "Hello World!".getBytes(StandardCharsets.UTF_8));

        ArgumentCaptor<TreeCacheEvent> argument = ArgumentCaptor.forClass(TreeCacheEvent.class);
        verify(listener, atLeastOnce()).childEvent(eq(client), argument.capture());
    }
}
