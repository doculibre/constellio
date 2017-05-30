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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CuratorAcceptanceTest {

    private CuratorFramework client;
    @Mock private TreeCacheListener treeCacheListener;
    @Mock private NodeCacheListener nodeCacheListener;
    @Mock private PathChildrenCacheListener pathChildrenListener;

    @Before
    public void setUp() {
        //FIXME
        File configFile = new FoldersLocator().getConstellioProperties();
        Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);
        String zhHost = new PropertiesDataLayerConfiguration(configs, null, null, null).getRecordsDaoCloudSolrServerZKHost();
        client = CuratorFrameworkFactory.newClient(zhHost, new ExponentialBackoffRetry(1000, 10));
        client.start();
    }

    @Test
    public void givenTreeCacheWhenChangeWithDelayThenNodeUpdatedNotified() throws Exception {
        TreeCache cache = new TreeCache(client, "/configs/records");
        cache.start();
        cache.getListenable().addListener(treeCacheListener);

        Thread.sleep(100);

        String contentAsString = "Hello World!" + System.currentTimeMillis();
        byte[] content = contentAsString.getBytes(StandardCharsets.UTF_8);
        String path = "/configs/records/test.txt";
        client.setData().forPath("/configs/records/test.txt", content);

        Thread.sleep(100);

        ArgumentCaptor<TreeCacheEvent> argument = ArgumentCaptor.forClass(TreeCacheEvent.class);
        verify(treeCacheListener, atLeastOnce()).childEvent(eq(client), argument.capture());

        List<TreeCacheEvent> events = argument.getAllValues();
        TreeCacheEvent lastEvent = events.get(events.size() - 1);
        assert(lastEvent.getType()).equals(TreeCacheEvent.Type.NODE_UPDATED);
        assert(lastEvent.getData().getPath()).equals(path);
        assert(new String(lastEvent.getData().getData(), "UTF-8")).equals(contentAsString);
    }

    @Test
    public void givenNodeCacheWhenChangeWithDelayThenNodeUpdatedNotified() throws Exception {
        NodeCache cache = new NodeCache(client, "/configs/records/test.txt");
        cache.start();
        cache.getListenable().addListener(nodeCacheListener);

        Thread.sleep(100);

        String contentAsString = "Hello World!" + System.currentTimeMillis();
        byte[] content = contentAsString.getBytes(StandardCharsets.UTF_8);
        String path = "/configs/records/test.txt";
        client.setData().forPath("/configs/records/test.txt", content);

        Thread.sleep(100);

        //Sent two events for one update !
        verify(nodeCacheListener, times(2)).nodeChanged();
    }

    @Test
    public void givenPathCacheWhenChangeWithDelayThenNodeUpdatedNotified() throws Exception {
        PathChildrenCache cache = new PathChildrenCache(client, "/configs/records/test.txt", true);
        cache.start();
        cache.getListenable().addListener(pathChildrenListener);

        Thread.sleep(100);

        String contentAsString = "Hello World!" + System.currentTimeMillis();
        byte[] content = contentAsString.getBytes(StandardCharsets.UTF_8);
        String path = "/configs/records/test.txt";
        client.setData().forPath("/configs/records/test.txt", content);

        Thread.sleep(100);

        verify(pathChildrenListener, never()).childEvent(eq(client), any(PathChildrenCacheEvent.class));
    }

    @Test
    public void givenTreeCacheWhenChangeThenNotified() throws Exception {
        TreeCache cache = new TreeCache(client, "/configs/records");
        cache.start();
        cache.getListenable().addListener(treeCacheListener);

        byte[] content = ("Hello World!" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
        String path = "/configs/records/test.txt";
        client.setData().forPath("/configs/records/test.txt", content);

        ArgumentCaptor<TreeCacheEvent> argument = ArgumentCaptor.forClass(TreeCacheEvent.class);
        verify(treeCacheListener, timeout(1000).atLeastOnce()).childEvent(eq(client), argument.capture());

        byte[] contentFound = null;

        List<TreeCacheEvent> events = argument.getAllValues();
        for (TreeCacheEvent event : events) {
            ChildData data = event.getData();
            if (data != null && data.getPath().equals(path)) {
                contentFound = event.getData().getData();
            }
        }

        String contentString = new String(content);
        String cacheString = new String(contentFound);

        assertThat(contentString).isEqualTo(cacheString);
    }

    @After
    public void cleanup() {
        CloseableUtils.closeQuietly(client);
    }
}
