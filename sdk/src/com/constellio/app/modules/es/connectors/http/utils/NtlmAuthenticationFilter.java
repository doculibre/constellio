package com.constellio.app.modules.es.connectors.http.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcifs.ntlmssp.Type3Message;

import org.apache.commons.codec.binary.Base64;

/**
 * Ref http://stackoverflow.com/questions/24066008/ntlm-authentification-java
 *
 * @author Nicolas Bélisle
 */
@WebServlet("/Authentication")
public class NtlmAuthenticationFilter implements Filter {

	public NtlmAuthenticationFilter() {
		System.out.println("Starting NtlmAuthenticationFilter");
	}

	public static final String USER = "admin";
	public static final String DOMAIN = "domain";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			System.out.println(headerName + "=" + httpRequest.getHeader(headerName));
		}
		String auth = httpRequest.getHeader("Authorization");
		if (auth != null && auth.startsWith("NTLM ")) {
			byte[] msg = Base64.decodeBase64(auth.substring(5));
			if (msg[8] == 1) {
				byte z = 0;
				byte[] msg1 = { (byte) 'N', (byte) 'T', (byte) 'L', (byte) 'M', (byte) 'S', (byte) 'S', (byte) 'P', z, (byte) 2,
						z, z, z, z, z, z, z, (byte) 40, z, z, z, (byte) 1,
						(byte) 130, z, z, z, (byte) 2, (byte) 2, (byte) 2, z, z, z, z, z, z, z, z, z, z, z, z };
				httpResponse.setHeader("WWW-Authenticate", "NTLM " + new sun.misc.BASE64Encoder().encodeBuffer(msg1));
				httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				httpResponse.setContentLength(0);
				httpResponse.flushBuffer();
			} else if (msg[8] == 3) {
				// Did Authentication Succeed?
				Type3Message type3 = new Type3Message(msg);
				String user = type3.getUser();
				String remoteHost = type3.getWorkstation();
				String domain = type3.getDomain();

				System.out.println("Login user:" + user + " remoteHost:" + remoteHost + " domain:" + type3.getDomain());
				try (PrintWriter out = httpResponse.getWriter()) {
					//TODO Checking for password in NTLM is not simple...
					if (USER.equals(user) && DOMAIN.equalsIgnoreCase(domain)) {
						chain.doFilter(request, response);
					} else {
						httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
						return;
					}
				}
			}
		} else {
			// The Type 2 message is sent by the server to the client in response to the client's Type 1 message.
			httpResponse.setHeader("WWW-Authenticate", "NTLM");
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			httpResponse.setContentLength(0);
			httpResponse.flushBuffer();
		}
	}

	@Override
	public void init(FilterConfig filterConfig)
			throws ServletException {
	}

	@Override
	public void destroy() {
	}
}
