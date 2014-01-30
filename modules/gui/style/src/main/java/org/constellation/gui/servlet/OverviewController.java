/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.gui.servlet;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.PortrayalContext;
import org.constellation.gui.admin.conf.CstlConfig;
import org.constellation.gui.binding.Style;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.util.StyleUtilities;
import org.geotoolkit.sld.xml.Specification;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Controller
@RequestMapping("/overview")
public final class OverviewController  {
    @Autowired
    private CstlConfig cstlConfig;

    /**
     * Use for debugging purpose.
     */
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * {@inheritDoc}
     */
    @RequestMapping(method = RequestMethod.POST)
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        render(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @RequestMapping(method = RequestMethod.GET, produces = "image/png")
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        render(req, resp);
    }

    public void render(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        // Get original request parameters.
        final String request = req.getParameter("REQUEST");
        final String layers = req.getParameter("LAYERS");
        final String crs = req.getParameter("CRS");
        final String bbox = req.getParameter("BBOX");
        final String width = req.getParameter("WIDTH");
        final String height = req.getParameter("HEIGHT");
        final String format = req.getParameter("FORMAT");
        final String sldBody = req.getParameter("SLD_BODY");
        final String sldVersion = req.getParameter("SLD_VERSION");

        // Perform a portrayal.
        if ("Portray".equalsIgnoreCase(request)) {
            final String method = cstlConfig.getUrl() + req.getParameter("METHOD");
            final String providerId = req.getParameter("PROVIDER");
            final String[] coords = bbox.split(",");

            // Handle style JSON body.
            String styleXml = null;
            if (sldBody != null && !sldBody.isEmpty()) {
                try {
                    final Style style = StyleUtilities.readJson(sldBody, Style.class);
                    final StringWriter writer = new StringWriter();
                    if ("1.1.0".equals(sldVersion)) {
                        new StyleXmlIO().writeStyle(writer, style.toType(), Specification.StyledLayerDescriptor.V_1_1_0);
                    } else {
                        new StyleXmlIO().writeStyle(writer, style.toType(), Specification.StyledLayerDescriptor.V_1_0_0);
                    }
                    styleXml = writer.toString();
                } catch (IOException ex) {
                    LOGGER.warn("Invalid style JSON body.", ex);
                } catch (JAXBException ex) {
                    LOGGER.warn("The style marshalling has failed.", ex);
                }
            }

            // Prepare portrayal context.
            final PortrayalContext context = new PortrayalContext();
            context.setProviderId(providerId);
            context.setDataName(layers);
            context.setProjection(crs);
            context.setFormat("image/png");
            context.setStyleBody(styleXml);
            context.setSldVersion(sldVersion);
            context.setWidth(Integer.parseInt(width));
            context.setHeight(Integer.parseInt(height));
            context.setWest(Double.parseDouble(coords[0]));
            context.setSouth(Double.parseDouble(coords[1]));
            context.setEast(Double.parseDouble(coords[2]));
            context.setNorth(Double.parseDouble(coords[3]));
            context.setLonFirstOutput(true);



            // set authentication on header
            final String toEncode = cstlConfig.getLogin()+":"+cstlConfig.getPassword();
            byte[] binaryPassword = toEncode.getBytes();
            final String encodedAuthentication = Base64.encodeBase64String(binaryPassword);

            // Prepare request.
            final HttpPost httpPost = new HttpPost(method);
            final byte[] entity = StyleUtilities.writeJson(context).getBytes("UTF-8");
            httpPost.setEntity(new ByteArrayEntity(entity));
            httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            httpPost.addHeader(HttpHeaders.CONTENT_ENCODING, "UTF-8");
            httpPost.addHeader("Authorization", "Basic "+ encodedAuthentication);

            resp.setContentType("image/png");
            resp.addHeader(HttpHeaders.PRAGMA, "no-cache");
            resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache,no-store");
            resp.addHeader(HttpHeaders.EXPIRES, "0");

            // Perform request.
            execute(httpPost, resp.getOutputStream());
        }
    }

    public static void execute(final HttpPost post, final OutputStream out) throws IOException {
        // Prepare execution.
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpParams httpParams = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        // Request execution.
        final HttpResponse response = httpClient.execute(post);
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                out.write(EntityUtils.toByteArray(entity));
            } finally {
                out.flush();
                out.close();
            }
            HttpClientUtils.closeQuietly(httpClient);
        }
    }
}