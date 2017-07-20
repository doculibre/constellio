package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.*;

import com.constellio.data.io.services.facades.IOServices;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class LabelViewer extends VerticalLayout {
    private ContentManager contentManager;

    public LabelViewer(File PDF, String filename, IOServices ioServices) {
        InputStream inputStream = null;
        try {
            ModelLayerFactory model = ConstellioFactories.getInstance().getAppLayerFactory().getModelLayerFactory();
            contentManager = model.getContentManager();
            inputStream = new FileInputStream(PDF);
            byte[] PDFbytes = IOUtils.toByteArray(inputStream);
            StreamSource source = buildSource(PDFbytes);
            BrowserFrame viewer = new BrowserFrame();
            viewer.setSource(new StreamResource(source, filename));

            viewer.setWidth("100%");
            viewer.setHeight("1024px");

            Link download = new Link($("ReportViewer.download", filename),
                    new DownloadStreamResource(source, filename, getMimeTypeFromFileName(filename)));

            addComponents(download, viewer);
            setWidth("100%");

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            ioServices.closeQuietly(inputStream);
            ioServices.deleteQuietly(PDF);
        }
    }

    static String getMimeTypeFromFileName(String filename) {
        if (StringUtils.isBlank(filename)) {
            return DownloadStreamResource.PDF_MIMETYPE;
        }
        String extension = StringUtils.substringAfterLast(filename, ".");
        if (StringUtils.isBlank(extension)) {
            return DownloadStreamResource.PDF_MIMETYPE;
        }
        extension = extension.toLowerCase();
        if (extension.equals("xls") || extension.equals("xlsx")) {
            return DownloadStreamResource.EXCEL_MIMETYPE;
        }
        if (extension.equals("zip")) {
            return DownloadStreamResource.ZIP_MIMETYPE;
        }
        return DownloadStreamResource.PDF_MIMETYPE;
    }

    private StreamSource buildSource(final byte[] PDF) {
        return new StreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    return new ByteArrayInputStream(PDF);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public static class DownloadStreamResource extends StreamResource {
        public static String PDF_MIMETYPE = "application/pdf";
        public static String ZIP_MIMETYPE = "application/zip";
        public static String EXCEL_MIMETYPE = "application/vnd.ms-excel";

        public DownloadStreamResource(StreamSource source, String filename) {
            this(source, filename, getMimeTypeFromFileName(filename));
        }

        public DownloadStreamResource(StreamSource source, String filename, String MIMEType) {
            super(source, filename);
            setMIMEType(MIMEType);
        }

        @Override
        public DownloadStream getStream() {
            DownloadStream stream = super.getStream();
            stream.setParameter("Content-Disposition", "attachment; filename=" + getFilename());
            return stream;
        }
    }
}
