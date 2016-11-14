package com.constellio.model.services.contents.icap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;

public class IcapClient {

    private static final String ICAP_SCHEME = "icap";

    private static final int CHUNK_BUFFER_MAX_SIZE = 1024 * 4;

    private static final int DEFAUT_SOCKET_TIMEOUT = 5000;

    private static final String CR_LF = "\r\n";

    private final URI icapServerUrl;

    private final int socketTimeOut;

    private Socket socket;

    private DataOutputStream socketOutputStream;

    private byte[] chunkBuffer;

    public IcapClient(final URI icapServerUrl, final Integer socketTimeout) {
        Validate.notNull(icapServerUrl);
        Validate.isTrue(ICAP_SCHEME.equals(icapServerUrl.getScheme().toLowerCase()));

        this.icapServerUrl = icapServerUrl;
        socketTimeOut = (Integer) ObjectUtils.defaultIfNull(socketTimeout, DEFAUT_SOCKET_TIMEOUT);
    }

    public IcapResponse getIcapConfigurationsFromServer() throws IOException {
        try {
            connect();

            sendOptionsMethodRequest();

            return parseResponse();
        } finally {
            disconnect();
        }
    }

    private void connect() throws IOException {
        if (socket == null) {
            socket = new Socket();
            socket.connect(
                    new InetSocketAddress(
                            icapServerUrl.getHost(),
                            icapServerUrl.getPort()),
                    socketTimeOut);
            socket.setSoTimeout(socketTimeOut);

            socketOutputStream = new DataOutputStream(socket.getOutputStream());

            chunkBuffer = new byte[CHUNK_BUFFER_MAX_SIZE];
        }
    }

    private void sendOptionsMethodRequest() throws IOException {
        socketOutputStream.writeBytes(buildOptionsMethodRequest());

        socketOutputStream.flush();
    }

    private String buildOptionsMethodRequest() {
        return new StringBuilder().
                append("OPTIONS ").append(icapServerUrl).append(" ICAP/1.0").
                append(CR_LF).
                append("Host: ").append(icapServerUrl.getHost()).
                append(CR_LF).
                append(CR_LF).
                toString();
    }

    private IcapResponse parseResponse() throws IOException {
        return IcapResponse.parse(socket.getInputStream());
    }

    private void disconnect() {
        IOUtils.closeQuietly(socketOutputStream);
        IOUtils.closeQuietly(socket);

        socket = null;
        chunkBuffer = null;
    }

    public IcapResponse scanFile(final String filename, final InputStream fileContent, final String clientHostname, final Integer previewLength) throws IOException {
        Validate.notNull(filename);
        Validate.notNull(fileContent);

        IcapResponse response = null;

        if (fileContent.available() > 0) {
            try {
                connect();

                sendRespmodMethodRequest(filename, clientHostname, previewLength);

                response = sendContentPreview(fileContent, previewLength);

                if (response.isMoreThanPreviewScanNeeded()) {
                    response = sendPreviewRemaingingContent(fileContent);
                }
            } finally {
                disconnect();
            }
        } else {
            try (final InputStream emptyInputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            }) {
                response = IcapResponse.parse(emptyInputStream);
            }
        }

        return response;
    }

    private void sendRespmodMethodRequest(final String filename, final String clientHostname, final Integer previewLength) throws IOException {
        socketOutputStream.writeBytes(buildRespmodMethodRequest(filename, clientHostname, previewLength));

        socketOutputStream.flush();
    }

    private String buildRespmodMethodRequest(final String filename, final String clientHostname, final Integer previewLength) throws IOException {
        final String encapsulatedFileGetRequest = buildEncapsulatedFileGetRequest(filename, clientHostname);
        final String encapsulatedFileGetResponse = buildEncapsulatedFileGetResponse();

        return buildRespmodMethodRequest(encapsulatedFileGetRequest, encapsulatedFileGetResponse, clientHostname, previewLength);
    }

    private String buildEncapsulatedFileGetRequest(final String filename, final String clientHostname) throws IOException {
        return new StringBuilder().
                append("GET http://").append(clientHostname).append("/").append(URLEncoder.encode(filename, "utf-8")).append(" ").append("HTTP/1.1").
                append(CR_LF).
                append("Host: ").append(clientHostname).
                append(CR_LF).
                append(CR_LF).
                toString();
    }

    private String buildEncapsulatedFileGetResponse() {
        return new StringBuilder().
                append("HTTP/1.1 200 OK").
                append(CR_LF).
                append("Transfer-Encoding: chunked").
                append(CR_LF).
                append(CR_LF).
                toString();
    }

    private String buildRespmodMethodRequest(final String encapsulatedFileGetRequest, final String encapsulatedFileGetResponse, final String clientHostname, final Integer previewLength) {
        final int encapsulatedFileGetRequestOffset = encapsulatedFileGetRequest.length();
        final int encapsulatedFileGetResponseOffset = encapsulatedFileGetRequestOffset + encapsulatedFileGetResponse.length();

        final StringBuilder request = new StringBuilder().
                append("RESPMOD ").append(icapServerUrl).append(" ICAP/1.0").
                append(CR_LF).
                append("Host: ").append(icapServerUrl.getHost()).
                append(CR_LF).
                append("Allow: 204").
                append(CR_LF).
                append("Encapsulated: req-hdr=0 res-hdr=").append(encapsulatedFileGetRequestOffset).append(" res-body=").append(encapsulatedFileGetResponseOffset).
                append(CR_LF).
                append("Preview: ").append(previewLength).
                append(CR_LF).
                append("User-Agent: Java").
                append(CR_LF);

        if (StringUtils.isNotBlank(clientHostname)) {
            request.append("X-Client-IP: ").append(clientHostname)
                    .append(CR_LF);
        }

        request.append(CR_LF);

        request.append(encapsulatedFileGetRequest);

        request.append(encapsulatedFileGetResponse);

        return request.toString();
    }

    private IcapResponse sendContentPreview(final InputStream fileContent, final Integer previewLength) throws IOException {
        writeChunk(fileContent, previewLength);

        sendBodyEnd();

        return parseResponse();
    }

    private int writeChunk(final InputStream fileContent, final int size) throws IOException {
        final int chunkSize = IOUtils.read(fileContent, chunkBuffer, 0, size);

        writeChunk(chunkBuffer, 0, chunkSize);

        return chunkSize;
    }

    private void writeChunk(final byte[] chunkBuffer, final int offset, final int chunkSize) throws IOException {
        if (chunkSize > 0) {
            socketOutputStream.writeBytes(Long.toHexString(chunkSize) + CR_LF);
            socketOutputStream.write(chunkBuffer, offset, chunkSize);
            socketOutputStream.writeBytes(CR_LF);
        }
    }

    private void sendBodyEnd() throws IOException {
        socketOutputStream.writeBytes("0");
        socketOutputStream.writeBytes(CR_LF);
        socketOutputStream.writeBytes(CR_LF);

        socketOutputStream.flush();
    }

    private IcapResponse sendPreviewRemaingingContent(final InputStream fileContent) throws IOException {
        int lastSentChunkSize;

        do {
            lastSentChunkSize = writeChunk(fileContent, CHUNK_BUFFER_MAX_SIZE);

            socketOutputStream.flush();
        } while (lastSentChunkSize > 0);

        sendBodyEnd();

        return parseResponse();
    }

}
