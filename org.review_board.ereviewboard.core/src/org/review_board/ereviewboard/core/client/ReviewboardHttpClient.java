/*******************************************************************************
 * Copyright (c) 2004 - 2009 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers, Atlassian, Sven Krzyzak
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2009 Markus Knittig
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Markus Knittig - adapted Trac, Redmine & Atlassian implementations for
 *                      Review Board
 *******************************************************************************/
package org.review_board.ereviewboard.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.review_board.ereviewboard.core.exception.ReviewboardException;
import org.review_board.ereviewboard.core.util.IOUtil;

/**
 * HTTP Client for calling the Review Board API. Handles {@link HttpClient}
 * setup, authentication and GET/POST requests.
 * 
 * @author Markus Knittig
 */
public class ReviewboardHttpClient {

    private final AbstractWebLocation location;

    private final HttpClient httpClient;

    private String sessionCookie;

    public ReviewboardHttpClient(AbstractWebLocation location, String characterEncoding,
            boolean selfSignedSSL) {
        this.location = location;
        this.httpClient = createAndInitHttpClient(characterEncoding, selfSignedSSL);
    }

    private HttpClient createAndInitHttpClient(String characterEncoding, boolean selfSignedSSL) {
        if (selfSignedSSL) {
            Protocol.registerProtocol("https",
                    new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
        }
        HttpClient httpClient = new HttpClient();
        WebUtil.configureHttpClient(httpClient, "Mylyn");
        httpClient.getParams().setContentCharset(characterEncoding);
        return httpClient;
    }

    public boolean apiEntryPointExist(IProgressMonitor monitor) {

        GetMethod getMethod = new GetMethod(location.getUrl() + "/api/");
        
        return executeRequest(getMethod, monitor) == HttpStatus.SC_OK;
    }

    public String login(String username, String password, IProgressMonitor monitor) throws ReviewboardException {

        GetMethod loginRequest = new GetMethod(location.getUrl() + "/api/info");
        Credentials credentials = new UsernamePasswordCredentials(username, password);

        monitor = Policy.monitorFor(monitor);
        
        String foundSessionCookie = null;

        try {
            monitor.beginTask("Logging in", IProgressMonitor.UNKNOWN);

            // TODO: this will probably affect existing requests, might have ill side-effects
            httpClient.getState().clearCookies();
            
            // perform authentication
            String authHeader = new BasicScheme().authenticate(credentials, loginRequest);
            loginRequest.addRequestHeader("Authorization", authHeader);
            
            // execute and validate call
            int requestStatus = executeRequest(loginRequest, monitor);
            
            switch (requestStatus) {

            case HttpStatus.SC_OK:
                break;
            case HttpStatus.SC_UNAUTHORIZED:
                throw new ReviewboardException(
                            "Authentication failed, please check your username and password");
            default:
                throw new ReviewboardException("Request returned unacceptable status code "
                            + requestStatus);
            }
                
            // look for session cookie
            for (Cookie cookie : httpClient.getState().getCookies())
                if (cookie.getName().equals("rbsessionid"))
                    foundSessionCookie = cookie.getValue();

            if ( foundSessionCookie == null )
                throw new ReviewboardException("Did not find session cookie in response");
            
            return foundSessionCookie;

        } catch (AuthenticationException e) {
            throw new ReviewboardException(e.getMessage(), e);
        } finally {
            loginRequest.releaseConnection();
            monitor.done();
        }
    }

    private void ensureIsLoggedIn(IProgressMonitor monitor) throws ReviewboardException {
        
        if ( sessionCookie != null )
            return;
        
        AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
        sessionCookie = login(credentials.getUserName(), credentials.getPassword(), monitor);
    }

    private String stripSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.lastIndexOf("/"));
        }
        return url;
    }

    public String executeGet(String url, IProgressMonitor monitor) throws ReviewboardException {
        GetMethod getRequest = new GetMethod(stripSlash(location.getUrl()) + url);
        getRequest.getParams().setParameter("Accept", "application/json");

        return executeMethod(getRequest, monitor);
    }

    public byte[] executeGetForBytes(String url, String acceptHeaderValue, IProgressMonitor monitor)
            throws ReviewboardException {

        GetMethod getRequest = new GetMethod(stripSlash(location.getUrl()) + url);
        getRequest.addRequestHeader("Accept", acceptHeaderValue);

        return executeMethodForBytes(getRequest, monitor);
    }

    public String executePost(String url, IProgressMonitor monitor) throws ReviewboardException {
        return executePost(url, new HashMap<String, String>(), monitor);
    }

    public String executePost(String url, Map<String, String> parameters,
            IProgressMonitor monitor) throws ReviewboardException {
        PostMethod postRequest = new PostMethod(stripSlash(location.getUrl()) + url);

        for (String key : parameters.keySet())
            postRequest.setParameter(key, parameters.get(key));

        return executeMethod(postRequest, monitor);
    }

    private String executeMethod(HttpMethodBase request, IProgressMonitor monitor) throws ReviewboardException {
        
        monitor = Policy.monitorFor(monitor);
        
        ensureIsLoggedIn(monitor);
        
        try {
            monitor.beginTask("Executing request", IProgressMonitor.UNKNOWN);

            executeRequest(request, monitor);
            return getResponseBodyAsString(request, monitor);
        } finally {
            request.releaseConnection();
            monitor.done();
        }
    }

    private byte[] executeMethodForBytes(HttpMethodBase request, IProgressMonitor monitor) throws ReviewboardException {
        
        monitor = Policy.monitorFor(monitor);
        
        ensureIsLoggedIn(monitor);
        
        try {
            monitor.beginTask("Executing request", IProgressMonitor.UNKNOWN);

            executeRequest(request, monitor);
            return getResponseBodyAsByteArray(request, monitor);
        } finally {
            request.releaseConnection();
            monitor.done();
        }
    }

    private int executeRequest(HttpMethodBase request, IProgressMonitor monitor) {
        HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location,
                monitor);
        try {
            return WebUtil.execute(httpClient, hostConfiguration, request, monitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getResponseBodyAsString(HttpMethodBase request, IProgressMonitor monitor) {

        InputStream stream = null;
        try {
            stream = WebUtil.getResponseBodyAsStream(request, monitor);
            return IOUtils.toString(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.closeSilently(stream);
        }
    }

    private byte[] getResponseBodyAsByteArray(HttpMethodBase request, IProgressMonitor monitor) {

        InputStream stream = null;
        try {
            stream = WebUtil.getResponseBodyAsStream(request, monitor);
            return IOUtils.toByteArray(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.closeSilently(stream);
        }
    }

}
