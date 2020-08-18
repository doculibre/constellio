package com.constellio.app.services.extensions.plugins.utils;

import com.constellio.app.services.extensions.plugins.utils.PluginManagementUtils.NewPluginsInNewWar;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.constellio.app.services.extensions.plugins.JSPFPluginServices.NEW_JAR_EXTENSION;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginManagementUtilsAcceptanceTest extends ConstellioTest {
	File pluginsFolder, libFolder1, libFolder2, pluginsToMove1FileInexisting, pluginsToMove2FileWithPlugins;
	File plugin1, plugin2, plugin2Updated, newPlugin4, notAPlugin;
	private PluginManagementUtils utils1WithoutPluginsToMoveFile, utils2WithPluginsToMoveFile;
	private List<String> pluginsList;

	@Before
	public void setup()
			throws IOException {
		addDummyPlugins();
		libFolder1 = newTempFolder();
		libFolder2 = newTempFolder();
		pluginsToMove1FileInexisting = new File(newTempFolder(), "inexistingFile");
		pluginsToMove2FileWithPlugins = newTempFileWithContent("existingFile", "plugin1\n");

		utils1WithoutPluginsToMoveFile = new PluginManagementUtils(pluginsFolder, libFolder1,
				pluginsToMove1FileInexisting);
		utils2WithPluginsToMoveFile = new PluginManagementUtils(pluginsFolder, libFolder2,
				pluginsToMove2FileWithPlugins);
	}

	private void addDummyPlugins()
			throws IOException {
		pluginsFolder = newTempFolder();
		plugin1 = new File(pluginsFolder, "plugin1.jar");
		plugin2 = new File(pluginsFolder, "PLUGIN2.jar");
		plugin2Updated = new File(pluginsFolder, "PLUGIN2." + NEW_JAR_EXTENSION);
		newPlugin4 = new File(pluginsFolder, "plugin4." + NEW_JAR_EXTENSION);
		notAPlugin = new File(pluginsFolder, "plugin3.zip");
		FileUtils.write(plugin1, "A plugin");
		FileUtils.write(plugin2, "Another plugin");
		FileUtils.write(plugin2, "Another plugin updated");
		FileUtils.write(newPlugin4, "plugin 4");
		FileUtils.write(notAPlugin, "I am not a plugin - Mouhahahaha");

		pluginsList = Arrays.asList("plugin1", "PLUGIN2", "plugin4");
	}

	@Test
	public void whenAddPluginToMoveThenOk()
			throws Exception {
		utils2WithPluginsToMoveFile.addPluginToMove("plugin2");
		assertThat(FileUtils.readLines(pluginsToMove2FileWithPlugins)).containsOnly("plugin1", "plugin2");

		utils1WithoutPluginsToMoveFile.addPluginToMove("plugin2");
		assertThat(FileUtils.readLines(pluginsToMove1FileInexisting)).containsOnly("plugin1", "PLUGIN2", "plugin4");
	}

	@Test
	public void whenFillFileWithAllPluginsThenOk()
			throws Exception {
		utils2WithPluginsToMoveFile.fillFileWithAllPlugins();
		assertThat(FileUtils.readLines(pluginsToMove2FileWithPlugins)).containsOnly("plugin1", "PLUGIN2", "plugin4");

		utils1WithoutPluginsToMoveFile.fillFileWithAllPlugins();
		assertThat(FileUtils.readLines(pluginsToMove1FileInexisting)).containsOnly("plugin1", "PLUGIN2", "plugin4");
	}

	@Test
	public void whenMovePluginsAndSetNoPluginToMoveThenOk()
			throws Exception {
		utils1WithoutPluginsToMoveFile.movePlugins(pluginsList);
		assertThat(FileUtils.readLines(pluginsToMove1FileInexisting)).isEmpty();

		File plugin1FromLib1 = new File(libFolder1, "plugin1.jar");
		assertThat(plugin1FromLib1).hasContent("A plugin");
		File plugin2FromLib1 = new File(libFolder1, "PLUGIN2.jar");
		assertThat(plugin2FromLib1).hasContent("Another plugin updated");
		File plugin4FromLib1 = new File(libFolder1, "plugin4.jar");
		assertThat(plugin4FromLib1).hasContent("plugin 4");
		assertThat(FileUtils.listFiles(libFolder1, new String[]{"jar"}, false)).containsOnly(
				plugin1FromLib1, plugin2FromLib1, plugin4FromLib1
		);

		utils2WithPluginsToMoveFile.movePlugins(pluginsList);
		assertThat(FileUtils.readLines(pluginsToMove2FileWithPlugins)).isEmpty();

		File plugin1FromLib2 = new File(libFolder2, "plugin1.jar");
		assertThat(plugin1FromLib2).hasContent("A plugin");
		File plugin2FromLib2 = new File(libFolder2, "PLUGIN2.jar");
		assertThat(plugin2FromLib2).hasContent("Another plugin updated");
		File plugin4FromLib2 = new File(libFolder2, "plugin4.jar");
		assertThat(plugin4FromLib2).hasContent("plugin 4");
		assertThat(FileUtils.listFiles(libFolder2, new String[]{"jar"}, false)).containsOnly(
				plugin1FromLib2, plugin2FromLib2, plugin4FromLib2
		);
	}

	@Test
	public void whenCopyPluginFromPluginsFolderToLibFolderThenOk()
			throws Exception {
		utils1WithoutPluginsToMoveFile.copyPluginFromPluginsFolderToLibFolder("PLUGIN2");
		assertThat(pluginsToMove1FileInexisting.exists()).isFalse();

		File plugin2FromLib1 = new File(libFolder1, "PLUGIN2.jar");
		assertThat(plugin2FromLib1).hasContent("Another plugin updated");
		assertThat(FileUtils.listFiles(libFolder1, new String[]{"jar"}, false)).containsOnly(plugin2FromLib1);

		utils2WithPluginsToMoveFile.copyPluginFromPluginsFolderToLibFolder("PLUGIN2");
		assertThat(FileUtils.readLines(pluginsToMove2FileWithPlugins)).containsOnly("plugin1");

		File plugin2FromLib2 = new File(libFolder2, "PLUGIN2.jar");
		assertThat(plugin2FromLib2).hasContent("Another plugin updated");
		assertThat(FileUtils.listFiles(libFolder2, new String[]{"jar"}, false)).containsOnly(plugin2FromLib2);
	}

	@Test
	public void whenSetNoPluginToUpdateThenOk()
			throws Exception {
		utils1WithoutPluginsToMoveFile.setNoPluginToMove();
		assertThat(FileUtils.readLines(pluginsToMove1FileInexisting)).isEmpty();

		utils2WithPluginsToMoveFile.setNoPluginToMove();
		assertThat(FileUtils.readLines(pluginsToMove2FileWithPlugins)).isEmpty();
	}

	@Test
	public void whenGetPluginsToUpdateThenOk()
			throws Exception {
		Set<String> pluginsToUpdate = utils1WithoutPluginsToMoveFile.getPluginsToMove();
		assertThat(pluginsToUpdate).containsAll(pluginsList);

		Set<String> pluginsToUpdate2 = utils2WithPluginsToMoveFile.getPluginsToMove();
		assertThat(pluginsToUpdate2).containsOnly("plugin1");
	}

	@Test
	public void whenMarkPluginsToInstallThenMarked()
			throws Exception {

		File tempFolder = newTempFolder();
		File otherTempFolder = newTempFolder();

		PluginManagementUtils.markNewPluginsInNewWar(tempFolder, "plugin1.jar", null);
		PluginManagementUtils.markNewPluginsInNewWar(tempFolder, "plugin2.jar", null);
		PluginManagementUtils.markNewPluginsInNewWar(otherTempFolder, "plugin3.jar", null);

		assertThat(PluginManagementUtils.getNewPluginsInNewWar(tempFolder)).usingFieldByFieldElementComparator().containsOnly(
				new NewPluginsInNewWar("plugin1.jar", null), new NewPluginsInNewWar("plugin2.jar", null));

		PluginManagementUtils.clearNewPluginsInNewWar(tempFolder);
		assertThat(PluginManagementUtils.getNewPluginsInNewWar(tempFolder)).isEmpty();

	}

	@Test
	public void whenMarkPluginsToInstallForTenantThenMarked()
			throws Exception {

		File tempFolder = newTempFolder();
		File otherTempFolder = newTempFolder();

		PluginManagementUtils.markNewPluginsInNewWar(tempFolder, "plugin1.jar", "1");
		PluginManagementUtils.markNewPluginsInNewWar(tempFolder, "plugin2.jar", "1");
		PluginManagementUtils.markNewPluginsInNewWar(tempFolder, "plugin2.jar", "2");
		PluginManagementUtils.markNewPluginsInNewWar(tempFolder, "plugin3.jar", "3");

		assertThat(PluginManagementUtils.getNewPluginsInNewWar(tempFolder)).usingFieldByFieldElementComparator().containsOnly(
				new NewPluginsInNewWar("plugin1.jar", "1"),
				new NewPluginsInNewWar("plugin2.jar", "1"),
				new NewPluginsInNewWar("plugin2.jar", "2"),
				new NewPluginsInNewWar("plugin3.jar", "3"));

		PluginManagementUtils.clearNewPluginsInNewWar(tempFolder);
		assertThat(PluginManagementUtils.getNewPluginsInNewWar(tempFolder)).isEmpty();

	}
}
