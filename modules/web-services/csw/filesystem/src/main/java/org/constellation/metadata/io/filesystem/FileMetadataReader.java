/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.metadata.io.filesystem;

import org.apache.sis.xml.Namespaces;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.DomMetadataReader;
import org.constellation.metadata.io.ElementSetType;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataType;
import org.constellation.metadata.io.filesystem.sql.IdentifierIterator;
import org.constellation.metadata.io.filesystem.sql.MetadataDatasource;
import org.constellation.metadata.io.filesystem.sql.RecordIterator;
import org.constellation.metadata.io.filesystem.sql.Session;
import org.constellation.util.NodeUtilities;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.util.collection.CloseableIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import static org.constellation.metadata.CSWConstants.XML_EXT;
import static org.constellation.metadata.CSWQueryable.DUBLIN_CORE_QUERYABLE;
import static org.constellation.metadata.CSWQueryable.ISO_QUERYABLE;
import static org.geotoolkit.csw.xml.TypeNames.METADATA_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Creator_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Date_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Description_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Format_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Identifier_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Language_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Publisher_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Subject_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Title_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Type_QNAME;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory._Abstract_QNAME;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory._Modified_QNAME;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;
import static org.geotoolkit.ows.xml.v100.ObjectFactory._BoundingBox_QNAME;

// XML dependencies
// constellation dependencies
// Geotoolkit dependencies
// Apache SIS dependencies

/**
 * A CSW Metadata Reader. This reader does not require a database.
 * The CSW records are stored XML file in a directory .
 *
 * This reader can be used for test purpose or in case of small amount of record.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataReader extends DomMetadataReader implements CSWMetadataReader {

    /**
     * The directory containing the data XML files.
     */
    private final File dataDirectory;

    private final String serviceID;

    /**
     * Build a new CSW File Reader.
     *
     * @param configuration A generic configuration object containing a directory path
     * in the configuration.dataDirectory field.
     * @param serviceID
     *
     * @throws MetadataIoException If the configuration object does
     * not contains an existing directory path in the configuration.dataDirectory field.
     * If the creation of a MarshallerPool throw a JAXBException.
     */
    public FileMetadataReader(final Automatic configuration, final String serviceID) throws MetadataIoException {
        super(true, false);
        dataDirectory = configuration.getDataDirectory();
        this.serviceID = serviceID;
        if (dataDirectory == null) {
            throw new MetadataIoException("cause: unable to find the data directory", NO_APPLICABLE_CODE);
        } else if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }
        if (configuration.getEnableThread() != null && !configuration.getEnableThread().isEmpty()) {
            final boolean t = Boolean.parseBoolean(configuration.getEnableThread());
            if (t) {
                LOGGER.info("parrallele treatment enabled");
            }
            setIsThreadEnabled(t);
        }
        if (configuration.getEnableCache() != null && !configuration.getEnableCache().isEmpty()) {
            final boolean c = Boolean.parseBoolean(configuration.getEnableCache());
            if (!c) {
                LOGGER.info("cache system have been disabled");
            }
            setIsCacheEnabled(c);
        }
        analyzeFileSystem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getMetadata(final String identifier, final MetadataType mode) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, new ArrayList<QName>());
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public Node getMetadata(final String identifier, final MetadataType mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier);
        if (metadataFile != null) {
            final MetadataType metadataMode;
            try {
                metadataMode = getMetadataType(new FileInputStream(metadataFile), false);
            } catch (IOException | XMLStreamException ex) {
                throw new MetadataIoException(ex);
            }
            final Node metadataNode = getNodeFromFile(metadataFile);

            if (metadataMode ==  MetadataType.ISO_19115 && mode == MetadataType.DUBLINCORE) {
                return translateISOtoDCNode(metadataNode, type, elementName);
            } else if (mode == MetadataType.DUBLINCORE && metadataMode == MetadataType.DUBLINCORE) {
                return  applyElementSetNode(metadataNode, type, elementName);
            } else {
               return metadataNode;
            }
        }
        return null;
    }

    @Override
    public boolean existMetadata(final String identifier) throws MetadataIoException {
        Session session = null;
        try {
            session = MetadataDatasource.createSession(serviceID);
            return session.existRecord(identifier);
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while analyzing the file system", ex, NO_APPLICABLE_CODE);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    private Node translateISOtoDCNode(final Node metadata, final ElementSetType type, final List<QName> elementName) throws MetadataIoException  {
        if (metadata != null) {

            final DocumentBuilder docBuilder;
            try {
                docBuilder = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                throw new MetadataIoException(ex);
            }
            final Document document = docBuilder.newDocument();

            final Element root = document.createElementNS(Namespaces.CSW, "Record");

            /*
             * BRIEF part
             */
            final List<String> identifierValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Identifier"));
            final List<Node> identifiers = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "identifier", identifierValues, true);
            
            if (elementName != null && elementName.contains(_Identifier_QNAME)) {
                NodeUtilities.appendChilds(root, identifiers);
            }

            final List<String> titleValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Title"));
            final List<Node> titles = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "title", titleValues, true);

            if (elementName != null && elementName.contains(_Title_QNAME)) {
                NodeUtilities.appendChilds(root, titles);
            }
            
            final List<String> dataTypeValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Type"));
            final List<Node> dataTypes = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "type", dataTypeValues, false);
                
            if (elementName != null && elementName.contains(_Type_QNAME)) {
                NodeUtilities.appendChilds(root, dataTypes);
            }

            final List<String> westValues  = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("WestBoundLongitude"));
            final List<String> eastValues  = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("EastBoundLongitude"));
            final List<String> northValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("NorthBoundLatitude"));
            final List<String> southValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("SouthBoundLatitude"));

            final List<Node> bboxes = new ArrayList<>();
            if (westValues.size()  == eastValues.size()  &&
                eastValues.size()  == northValues.size() &&
                northValues.size() == southValues.size()) {

                for (int i = 0; i < westValues.size(); i++) {
                    final Node bboxNode = document.createElementNS("http://www.opengis.net/ows", "BoundingBox");
                    final Node crsAtt   = document.createAttribute("crs");
                    crsAtt.setTextContent("EPSG:4326");
                    bboxNode.getAttributes().setNamedItem(crsAtt);
                    final Node dimAtt   = document.createAttribute("dimensions");
                    dimAtt.setTextContent("2");
                    bboxNode.getAttributes().setNamedItem(dimAtt);
                    final Node lower    = document.createElementNS("http://www.opengis.net/ows", "LowerCorner");
                    lower.setTextContent(westValues.get(i) + " " + southValues.get(i));
                    bboxNode.appendChild(lower);
                    final Node upper    = document.createElementNS("http://www.opengis.net/ows", "UpperCorner");
                    upper.setTextContent(eastValues.get(i) + " " + northValues.get(i));
                    bboxNode.appendChild(upper);
                    bboxes.add(bboxNode);
                }
            } else {
                LOGGER.warning("incoherent bboxes coordinate");
            }

            if (ElementSetType.BRIEF.equals(type)) {
                final Element briefRoot = document.createElementNS(Namespaces.CSW, "BriefRecord");
                NodeUtilities.appendChilds(briefRoot, identifiers);
                NodeUtilities.appendChilds(briefRoot, titles);
                NodeUtilities.appendChilds(briefRoot, dataTypes);
                NodeUtilities.appendChilds(briefRoot, bboxes);
                return briefRoot;
            }

            /*
             *  SUMMARY part
             */
            final List<String> abstractValues = NodeUtilities.getValuesFromPath(metadata, "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString");
            final List<Node> abstracts = NodeUtilities.buildNodes(document, "http://purl.org/dc/terms/", "abstract", abstractValues, false);

            if (elementName != null && elementName.contains(_Abstract_QNAME)) {
                NodeUtilities.appendChilds(root, abstracts);
            }

            final List<String> kwValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Subject"));
            final List<Node> subjects = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "subject", kwValues, false);

            if (elementName != null && elementName.contains(_Subject_QNAME)) {
                NodeUtilities.appendChilds(root, subjects);
            }

            final List<String> formValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Format"));
            final List<Node> formats = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "format", formValues, false);

            if (elementName != null && elementName.contains(_Format_QNAME)) {
                 NodeUtilities.appendChilds(root, formats);
            }

            final List<String> modValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Modified"));
            final List<String> dateValues = new ArrayList<>();
            for (String modValue : modValues) {
                dateValues.add(formatDate(modValue));
            }
            final List<Node> modifieds = NodeUtilities.buildNodes(document, "http://purl.org/dc/terms/", "modified", dateValues, false);

            if (elementName != null && elementName.contains(_Modified_QNAME)) {
                NodeUtilities.appendChilds(root, modifieds);
            }

            if (ElementSetType.SUMMARY.equals(type)) {
                final Element sumRoot = document.createElementNS(Namespaces.CSW, "SummaryRecord");
                NodeUtilities.appendChilds(sumRoot, identifiers);
                NodeUtilities.appendChilds(sumRoot, titles);
                NodeUtilities.appendChilds(sumRoot, dataTypes);
                NodeUtilities.appendChilds(sumRoot, subjects);
                NodeUtilities.appendChilds(sumRoot, formats);
                NodeUtilities.appendChilds(sumRoot, modifieds);
                NodeUtilities.appendChilds(sumRoot, abstracts);
                NodeUtilities.appendChilds(sumRoot, bboxes);
                return sumRoot;
            }

            final List<Node> dates = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "date", dateValues, false);

            if (elementName != null && elementName.contains(_Date_QNAME)) {
                NodeUtilities.appendChilds(root, dates);
            }

            final List<String> creaValues = NodeUtilities.getValuesFromPaths(metadata, DUBLIN_CORE_QUERYABLE.get("creator"));
            final List<Node> creators = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "creator", creaValues, false);

            if (elementName != null && elementName.contains(_Creator_QNAME)) {
                NodeUtilities.appendChilds(root, creators);
            }

            final List<String> desValues = NodeUtilities.getValuesFromPath(metadata, "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gmx:FileName/@src");
            final List<Node> descriptions = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "description", desValues, false);

            if (!descriptions.isEmpty() && elementName != null && elementName.contains(_Description_QNAME)) {
                NodeUtilities.appendChilds(root, descriptions);
            }

            final List<String> paths = new ArrayList<>();
            paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
            paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
            paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
            paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
            final List<String> distValues = NodeUtilities.getValuesFromPaths(metadata, paths);
            final List<Node> distributors = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "publisher", distValues, false);

            if (elementName != null && elementName.contains(_Publisher_QNAME)) {
                NodeUtilities.appendChilds(root, distributors);
            }

            final List<String> langValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Language"));
            final List<Node> languages = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "language", langValues, false);
           
            if (elementName != null && elementName.contains(_Language_QNAME)) {
                NodeUtilities.appendChilds(root, languages);
            }

            if (elementName != null && elementName.contains(_BoundingBox_QNAME)) {
                NodeUtilities.appendChilds(root, bboxes);
            }
            
            /* TODO
            final SimpleLiteral spatial = null;
            final SimpleLiteral references = null;*/

            if (ElementSetType.FULL.equals(type)) {
                final Element recRoot = document.createElementNS(Namespaces.CSW, "Record");
                NodeUtilities.appendChilds(recRoot, identifiers);
                NodeUtilities.appendChilds(recRoot, titles);
                NodeUtilities.appendChilds(recRoot, dataTypes);
                NodeUtilities.appendChilds(recRoot, subjects);
                NodeUtilities.appendChilds(recRoot, formats);
                NodeUtilities.appendChilds(recRoot, languages);
                NodeUtilities.appendChilds(recRoot, creators);
                NodeUtilities.appendChilds(recRoot, modifieds);
                NodeUtilities.appendChilds(recRoot, dates);
                NodeUtilities.appendChilds(recRoot, abstracts);
                NodeUtilities.appendChilds(recRoot, distributors);
                NodeUtilities.appendChilds(recRoot, descriptions);
                NodeUtilities.appendChilds(recRoot, bboxes);
                //NodeUtilities.appendChilds(recRoot, spatials);
                //NodeUtilities.appendChilds(recRoot, references);
                return recRoot;
            }

            document.appendChild(root);
            return root;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DomainValues> getFieldDomainofValues(final String propertyNames) throws MetadataIoException {
        final List<DomainValues> responseList = new ArrayList<>();
        final StringTokenizer tokens          = new StringTokenizer(propertyNames, ",");

        while (tokens.hasMoreTokens()) {
            final String token       = tokens.nextToken().trim();
            final List<String> paths;
            if (ISO_QUERYABLE.get(token) != null) {
                paths = ISO_QUERYABLE.get(token);
            } else if (DUBLIN_CORE_QUERYABLE.get(token) != null) {
                paths = DUBLIN_CORE_QUERYABLE.get(token);
            } else {
                throw new MetadataIoException("The property " + token + " is not queryable",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }

            if (!paths.isEmpty()) {
                final List<String> values         = getAllValuesFromPaths(paths);
                final ListOfValuesType listValues = new ListOfValuesType(values);
                final DomainValuesType value      = new DomainValuesType(null, token, listValues, METADATA_QNAME);
                responseList.add(value);
            } else {
                throw new MetadataIoException("The property " + token + " is not queryable for now",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }
        }
        return responseList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        MetadataDatasource.close(serviceID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] executeEbrimSQLQuery(final String sqlQuery) throws MetadataIoException {
        throw new MetadataIoException("Ebrim query are not supported int the FILESYSTEM mode.", OPERATION_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Node> getAllEntries() throws MetadataIoException {
        final List<Node> results = new ArrayList<>();
        final CloseableIterator<String> iterator = getIdentifierIterator();
        while (iterator.hasNext()) {
            final File f = getFileFromIdentifier(iterator.next());
            results.add(getNodeFromFile(f));
        }
        iterator.close();
        return results;
    }

    @Override
    public CloseableIterator<String> getIdentifierIterator() throws MetadataIoException {
        try {
            final Session session = MetadataDatasource.createSession(serviceID);
            return new IdentifierIterator(session);
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while building identifier iterator", ex, NO_APPLICABLE_CODE);
        }
    }

    @Override
    public CloseableIterator<Node> getEntryIterator() throws MetadataIoException {
        try {
            final Session session = MetadataDatasource.createSession(serviceID);
            return new RecordIterator(session);
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while building identifier iterator", ex, NO_APPLICABLE_CODE);
        }
    }

    @Override
    public boolean useEntryIterator() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllIdentifiers() throws MetadataIoException {
        final List<String> results = new ArrayList<>();
        Session session = null;
        try {
            session = MetadataDatasource.createSession(serviceID);
            session.setReadOnly(true);
            results.addAll(session.getRecordList());
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while analyzing the file system", ex, NO_APPLICABLE_CODE);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetadataType> getSupportedDataTypes() {
        return Arrays.asList(MetadataType.ISO_19115, MetadataType.DUBLINCORE, MetadataType.EBRIM, MetadataType.ISO_19110);
    }

    /**
     * Return the list of Additional queryable element.
     */
    @Override
    public List<QName> getAdditionalQueryableQName() {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return new HashMap<>();
    }

    public final void analyzeFileSystem() throws MetadataIoException {
        if (dataDirectory != null) {
            Session session = null;
            try {
                session = MetadataDatasource.createSession(serviceID);
                if (session.needAnalyze()) {
                    LOGGER.info("Launching file system analyze");
                    session.setAutoCommit(false);
                    final long start = System.currentTimeMillis();
                    final Path dataPath = Paths.get(dataDirectory.getPath());
                    analyzeFileSystem(dataPath, session);
                    session.commit();
                    LOGGER.log(Level.INFO, "fileSystem analyze done in :{0} ms", (System.currentTimeMillis() - start));
                }
            } catch (SQLException ex) {
                throw new MetadataIoException("SQL Exception while analyzing the file system", ex, NO_APPLICABLE_CODE);
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }
    
    private void analyzeFileSystem(final Path directory, final Session session) throws SQLException, MetadataIoException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path f : stream) {
                final String fileName = f.getFileName().toString();
                if (fileName.endsWith(XML_EXT)) {
                    //final Node metadata = getNodeFromStream(Files.newInputStream(f));
                    final String identifier = getMetadataIdentifier(Files.newInputStream(f));
                    session.putRecord(identifier, f.toString());

                } else if (Files.isDirectory(f)) {
                    analyzeFileSystem(f, session);

                } else {
                    LOGGER.log(Level.FINER, "File {0} is not a valid", fileName);
                }
            }

        } catch (IOException | XMLStreamException e) {
            LOGGER.log(Level.WARNING, "Error while walking through file system", e);
        }
    }

    private File getFileFromIdentifier(final String identifier) throws MetadataIoException {
        Session session = null;
        try {
            session = MetadataDatasource.createSession(serviceID);
            final String path = session.getPathForRecord(identifier);
            if (path != null) {
                return new File(path);
            }
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while reading path for record", ex, NO_APPLICABLE_CODE);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }
    
    @Override
    public int getEntryCount() throws MetadataIoException {
        Session session = null;
        try {
            session = MetadataDatasource.createSession(serviceID);
            return session.getCount();
            
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while getting records count", ex, NO_APPLICABLE_CODE);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    protected String getMetadataIdentifier(final InputStream metadataStream) throws IOException, XMLStreamException {
        final List<String> identifierPaths = DUBLIN_CORE_QUERYABLE.get("identifier");
        final List<String[]> paths = new ArrayList<>();
        for (String identifierPath : identifierPaths) {
            identifierPath = identifierPath.substring(1); // remove the first '/'
            final String[] path = identifierPath.split("/");
            for (int i = 0; i < path.length; i++) {
                int sep = path[i].indexOf(':');
                if (sep != -1) {
                    path[i] = path[i].substring(sep + 1);
                }
            }
            paths.add(path);
        }
        XMLStreamReader xsr = null;
        try {
            xsr = xif.createXMLStreamReader(metadataStream);
            int i = 0;
            while (xsr.hasNext()) {
                xsr.next();
                if (xsr.isStartElement()) {
                    String nodeName = xsr.getLocalName();
                    final List<String[]> toRemove = new ArrayList<>();
                    for (String [] path : paths) {
                        String currentName = path[i];
                        if (i == path.length -2 && path[i + 1].startsWith("@")) {
                            final String value = xsr.getAttributeValue(null, path[i + 1].substring(1));
                            if (value != null) {
                                return value;
                            } else {
                                toRemove.add(path);
                            }
                        } else if (!currentName.equals("*") && !currentName.equals(nodeName)) {
                            toRemove.add(path);
                        } else if (i  == path.length -1) {
                            return xsr.getElementText();
                        }
                    }
                    paths.removeAll(toRemove);
                    i++;
                }
            }
        } finally {
            if (xsr != null) {
                xsr.close();
            }
            metadataStream.close();
        }
        return null;
    }
}

