package com.constellio.app.modules.rm.ui.components.retentionRule;

import static com.constellio.app.modules.rm.exports.RetentionRuleXMLExporter.forAllApprovedRulesInCollection;
import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.AutoCloseInputStream;

import com.constellio.app.modules.rm.exports.RetentionRuleXMLExporter;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.server.StreamResource;

public class ExportRetentionRulesLink extends DownloadLink {

	public ExportRetentionRulesLink(String caption) {
		super(new RetentionRuleVOsResource(), caption);
	}
	
	public static class RetentionRuleVOsResource extends StreamResource {

		private static final String STREAM_NAME = "ExportRetentionRulesLink.RetentionRuleVOsResource-InputStream";

		public RetentionRuleVOsResource() {
			super(new StreamSource() {
				@Override
				public InputStream getStream() {
					final File tempFile;
					try {
						tempFile = File.createTempFile(STREAM_NAME, "xml");
						tempFile.deleteOnExit();
						
						// TODO Use presenter
						String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
						ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
						ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
						RetentionRuleXMLExporter exporter = forAllApprovedRulesInCollection(collection, tempFile, modelLayerFactory);
						
						try {
							exporter.run();
							return new AutoCloseInputStream(new FileInputStream(tempFile)) {
								@Override
								public void close()
										throws IOException {
									try {
										super.close();
									} finally {
										tempFile.delete();
									}
								}
							};
						} catch (Throwable t) {	
							FileUtils.deleteQuietly(tempFile);
							if (t instanceof RuntimeException) {
								throw (RuntimeException) t;
							} else {
								throw new RuntimeException(t);
							}
						}	
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}, $("ExportRetentionRulesLink.fileName"));
		}

	}

}
