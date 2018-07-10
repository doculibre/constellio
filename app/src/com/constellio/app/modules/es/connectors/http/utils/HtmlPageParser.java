package com.constellio.app.modules.es.connectors.http.utils;

import static org.apache.tika.io.IOUtils.toByteArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.james.mime4j.io.LimitedInputStream;

import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_CannotDownloadDocument;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_CannotParseDocument;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpDocumentFetchException.ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent;
import com.constellio.app.modules.es.connectors.http.fetcher.UrlAcceptor;
import com.constellio.app.modules.es.connectors.http.fetcher.config.BasicUrlNormalizer;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.jetbrains.annotations.NotNull;

public class HtmlPageParser {

    private static final long MAX_CONTENT_LENGTH = 20 * 1024 * 1024;

    HashingService hashingService;
    FileParser fileParser;
    BasicUrlNormalizer urlNormalizer;
    UrlAcceptor urlAcceptor;

    public HtmlPageParser(UrlAcceptor urlAcceptor, FileParser fileParser, HashingService hashingService) {
        this.urlAcceptor = urlAcceptor;
        this.urlNormalizer = new BasicUrlNormalizer();
        this.hashingService = hashingService;
        this.fileParser = fileParser;
    }

    public HtmlPageParserResults parse(String url, HtmlPage page)
            throws ConnectorHttpDocumentFetchException {

        if (!isNoIndexContent(page)) {
            ParsedContent parsedContent;
            Set<String> uniqueAnchors = getUniqueAnchors(page);
            byte[] content;
            try {
                content = getContent(page);

            } catch (IOException e) {
                throw new ConnectorHttpDocumentFetchException_CannotDownloadDocument(url, e);
            }

            try {
                parsedContent = fileParser.parse(new ByteArrayInputStream(content), true);
            } catch (FileParserException e) {
                throw new ConnectorHttpDocumentFetchException_CannotParseDocument(url, e);
            }

            return finalizeHtmlPageParserResults(url, page, parsedContent, uniqueAnchors);
        } else {
            return createNoIndexHtmlPageParserResults();
        }
    }

    private boolean isNoIndexContent(HtmlPage page) {
        DomNodeList<DomElement> metas = page.getElementsByTagName("meta");
        if(metas != null) {
            ListIterator<DomElement> listIterator = metas.listIterator();
            while (listIterator.hasNext()) {
                DomElement element = listIterator.next();
                String name = element.getAttribute("name");
                if("robots".equalsIgnoreCase(name)) {
                    String content = element.getAttribute("content");
                    if(StringUtils.containsIgnoreCase(content, "noindex")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private HtmlPageParserResults createNoIndexHtmlPageParserResults() {
        try {
            String content = "NOINDEX";
            String hash = hashingService.getHashFromString(content);
            return new HtmlPageParserResults(hash, content, content, SetUtils.<String>emptySet(),
                    "text/html", "fr", null);
        } catch (HashingServiceException e) {
            throw new ImpossibleRuntimeException(e);
        }
    }

    @NotNull
    private HtmlPageParserResults finalizeHtmlPageParserResults(String url, HtmlPage page, ParsedContent parsedContent, Set<String> uniqueAnchors)
            throws ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent {
        String title;
        String parsedContentText;
        String digest;
        title = (String) parsedContent.getNormalizedProperty("title");
        parsedContentText = parsedContent.getParsedContent();

        String description = parsedContent.getDescription();
        if (StringUtils.isEmpty(description)) {
            List<HtmlElement> h1 = ListUtils.emptyIfNull(page.getDocumentElement().getHtmlElementsByTagName("h1"));
            if (!h1.isEmpty()) {
                List<HtmlElement> scripts = ListUtils.emptyIfNull(page.getDocumentElement().getHtmlElementsByTagName("script"));

                DomElement h1Element = h1.get(0);
                StringBuilder builder = new StringBuilder();
                for (DomElement nextElement = h1Element; nextElement != null; nextElement = nextElement.getNextElementSibling()) {
                    String textContent = getTextContent(nextElement);
                    if (StringUtils.isNotBlank(textContent)) {
                        for (HtmlElement script : scripts) {
                            textContent = StringUtils.remove(textContent, getTextContent(script));
                        }

                        builder.append(textContent);
                        if (builder.length() > 200) {
                            break;
                        }
                    }
                }

                parsedContent.setDescription(StringUtils.left(builder.toString(), 200));
            }
        }

        String language = parsedContent.getLanguage();
        HtmlElement docElement = page.getDocumentElement();
        if (docElement != null) {
            String lang = docElement.getLangAttribute();
            try {
                Locale langLocale = new Locale(lang);
                language = langLocale.getLanguage();
            } catch (Exception e) {}
        }

        if (parsedContentText.isEmpty()) {
            throw new ConnectorHttpDocumentFetchException_DocumentHasNoParsedContent(url);
        }

        try {
            digest = hashingService.getHashFromString(parsedContentText);
        } catch (HashingServiceException e) {
            throw new ImpossibleRuntimeException(e);
        }
        return new HtmlPageParserResults(digest, parsedContentText, title, uniqueAnchors,
                parsedContent.getMimetypeWithoutCharset(), language, parsedContent.getDescription());
    }

    public String getTextContent(DomNode node) {
        switch (node.getNodeType()) {
            case DomNode.ELEMENT_NODE:
            case DomNode.ATTRIBUTE_NODE:
            case DomNode.ENTITY_NODE:
            case DomNode.ENTITY_REFERENCE_NODE:
            case DomNode.DOCUMENT_FRAGMENT_NODE:
                final StringBuilder builder = new StringBuilder();
                for (final DomNode child : node.getChildren()) {
                    final short childType = child.getNodeType();
                    if (childType != DomNode.COMMENT_NODE && childType != DomNode.PROCESSING_INSTRUCTION_NODE) {
                        if (builder.length() > 0) {
                            builder.append(" ");
                        }
                        builder.append(getTextContent(child));
                    }
                }
                return builder.toString();

            case DomNode.TEXT_NODE:
            case DomNode.CDATA_SECTION_NODE:
            case DomNode.COMMENT_NODE:
            case DomNode.PROCESSING_INSTRUCTION_NODE:
                return node.getNodeValue();

            default:
                return null;
        }
    }

    private byte[] getContent(HtmlPage page)
            throws IOException {
        WebResponse webResponse = page.getWebResponse();
        final String contentLenghtString = webResponse.getResponseHeaderValue("Content-Length");
        //		if (StringUtils.isNotBlank(contentLenghtString)) {
        //			final long contentLength = Long.parseLong(contentLenghtString);
        //			if (contentLength > MAX_CONTENT_LENGTH) {
        //				throw new IOException("Max content length exceeded: " + contentLength);
        //			}
        //		}
        InputStream contentStream = null;
        try {
            contentStream = new LimitedInputStream(webResponse.getContentAsStream(), MAX_CONTENT_LENGTH);
            return toByteArray(contentStream);
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
    }

    private String getTitle(HtmlPage page) {
        return null;
    }

    private Set<String> getUniqueAnchors(HtmlPage page) {
        Set<String> uniqueAnchorUrls = new HashSet<>();

        for (HtmlAnchor anchor : page.getAnchors()) {
            String anchorUrl = null;
            try {
                final String unNormalizedAnchorUrl = HtmlAnchorUtils.getUrl(anchor);
                anchorUrl = urlNormalizer.normalize(unNormalizedAnchorUrl);
            } catch (Exception e) {
                //Normal, just skipping this url
            }
            if (StringUtils.isNotBlank(anchorUrl) && !anchorUrl.equals(page.getUrl().toString()) && urlAcceptor
                    .isAccepted(anchorUrl)) {
                uniqueAnchorUrls.add(anchorUrl);
            }
        }

        return uniqueAnchorUrls;
    }

    public static class HtmlPageParserResults {

        private String parsedContent;

        private String title;

        private Set<String> linkedUrls;

        private String language;

        private String digest;

        private String mimetype;

        private String description;

        public HtmlPageParserResults(String digest, String parsedContent, String title, Set<String> linkedUrls, String mimetype,
                                     String language, String description) {
            this.digest = digest;
            this.parsedContent = parsedContent;
            this.title = title;
            this.linkedUrls = linkedUrls;
            this.mimetype = mimetype;
            this.language = language;
            this.description = description;
        }

        public String getDigest() {
            return digest;
        }

        public String getParsedContent() {
            return parsedContent;
        }

        public String getMimetype() {
            return mimetype;
        }

        public Set<String> getLinkedUrls() {
            return linkedUrls;
        }

        public String getTitle() {
            return title;
        }

        public String getLanguage() {
            return language;
        }

        public String getDescription() {
            return description;
        }
    }
}
