/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.wps.ws;

import com.vividsolutions.jts.geom.Geometry;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;

import org.geotoolkit.process.ProcessException;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.geotoolkit.ows.xml.v110.AnyValue;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.DomainMetadataType;
import org.geotoolkit.ows.xml.v110.LanguageStringType;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.util.converter.ConverterRegistry;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.ObjectConverter;
import org.geotoolkit.wps.xml.v100.ComplexDataType;
import org.geotoolkit.wps.xml.v100.DescribeProcess;
import org.geotoolkit.wps.xml.v100.Execute;
import org.geotoolkit.wps.xml.v100.ExecuteResponse;
import org.geotoolkit.wps.xml.v100.GetCapabilities;
import org.geotoolkit.wps.xml.v100.InputDescriptionType;
import org.geotoolkit.wps.xml.v100.InputType;
import org.geotoolkit.wps.xml.v100.LiteralDataType;
import org.geotoolkit.wps.xml.v100.LiteralInputType;
import org.geotoolkit.wps.xml.v100.OutputDescriptionType;
import org.geotoolkit.wps.xml.v100.ProcessDescriptionType;
import org.geotoolkit.wps.xml.v100.ProcessDescriptions;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;
import org.geotoolkit.wps.xml.v100.ProcessOfferings;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.wps.xml.v100.ComplexDataCombinationType;
import org.geotoolkit.wps.xml.v100.ComplexDataCombinationsType;
import org.geotoolkit.wps.xml.v100.ComplexDataDescriptionType;
import org.geotoolkit.wps.xml.v100.DataType;
import org.geotoolkit.wps.xml.v100.OutputDataType;
import org.geotoolkit.wps.xml.v100.SupportedComplexDataInputType;
import org.geotoolkit.wps.xml.v100.SupportedComplexDataType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.wps.xml.v100.CRSsType;
import org.geotoolkit.wps.xml.v100.SupportedCRSsType;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.wps.xml.v100.DocumentOutputDefinitionType;
import org.geotoolkit.wps.xml.v100.OutputDefinitionType;
import org.geotoolkit.wps.xml.v100.OutputDefinitionsType;
import org.geotoolkit.wps.xml.v100.ResponseDocumentType;
import org.geotoolkit.wps.xml.v100.ResponseFormType;
import org.geotoolkit.wps.xml.v100.StatusType;
import org.geotoolkit.wps.xml.v100.ProcessStartedType;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.type.DefaultFeatureType;
import org.geotoolkit.feature.type.DefaultGeometryType;
import org.geotoolkit.feature.type.DefaultPropertyDescriptor;
import org.geotoolkit.geometry.isoonjts.GeometryUtils;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.wps.xml.WPSMarshallerPool;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.feature.Feature;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;
import org.opengis.feature.Property;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.constellation.ServiceDef;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.process.ProcessingRegistry;
import org.geotoolkit.wps.xml.v100.LiteralOutputType;
import org.geotoolkit.wps.xml.v100.SupportedUOMsType;
import org.geotoolkit.wps.xml.v100.UOMsType;

import static org.constellation.api.QueryConstants.*;
import org.constellation.wps.utils.WPSUtils;
import static org.constellation.wps.ws.WPSConstant.*;

/**
 * WPS worker.Compute response of getCapabilities, DescribeProcess and Execute requests.
 *
 * @author Quentin Boileau
 */
public class WPSWorker extends AbstractWorker {

    /**
     * List of literal converters. Used to convert a String to an Object like AffineTransform, Coodinate Reference System, ...
     */
//    public static final List LITERAL_CONVERTERS = UnmodifiableArrayList.wrap(
//            StringToFeatureCollectionConverter.getInstance(),
//            StringToUnitConverter.getInstance(),
//            StringToGeometryConverter.getInstance(),
//            StringToCRSConverter.getInstance(),
//            StringToAffineTransformConverter.getInstance(),
//            StringToFilterConverter.getInstance(),
//            StringToSortByConverter.getInstance(),
//            StringToNumberRangeConverter.getInstance());
//    /**
//     * List of reference converters.Used to extract an Object from a Reference like an URL. For example to a Feature or FeatureCollection,
//     * to a Geometry or a coverage.
//     */
//    public static final List REFERENCE_CONVERTERS = UnmodifiableArrayList.wrap(
//            ReferenceToFeatureCollectionConverter.getInstance(),
//            ReferenceToFeatureConverter.getInstance(),
//            ReferenceToFeatureTypeConverter.getInstance(),
//            ReferenceToFileConverter.getInstance(),
//            ReferenceToGeometryConverter.getInstance(),
//            ReferenceToGridCoverage2DConverter.getInstance(),
//            ReferenceToGridCoverageReaderConverter.getInstance());
//    /**
//     * List of complex converters
//     */
//    public static final List COMPLEX_CONVERTERS = UnmodifiableArrayList.wrap(
//            ComplexToFeatureCollectionConverter.getInstance(),
//            ComplexToFeatureCollectionArrayConverter.getInstance(),
//            ComplexToFeatureConverter.getInstance(),
//            ComplexToFeatureArrayConverter.getInstance(),
//            ComplexToFeatureTypeConverter.getInstance(),
//            ComplexToGeometryConverter.getInstance(),
//            ComplexToGeometryArrayConverter.getInstance());
//    /**
//     * List of output complex converters. They used to convert an output from a process into a complex WPS output like GML.
//     */
//    public static final List OUTPUT_COMPLEX_CONVERTERS = UnmodifiableArrayList.wrap(
//            GeometryToComplexConverter.getInstance(),
//            GeometryArrayToComplexConverter.getInstance(),
//            FeatureToComplexConverter.getInstance(),
//            FeatureCollectionToComplexConverter.getInstance());
//    /**
//     * List of supported <b>input</b> Class for a Complex <b>input</b>.
//     */
//    public static final List COMPLEX_INPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
//            Feature.class,
//            FeatureCollection.class,
//            Feature[].class,
//            FeatureCollection[].class,
//            FeatureType.class,
//            Geometry.class);
//    /**
//     * List of supported <b>output</b> Class for a Complex <b>output</b>.
//     */
//    public static final List COMPLEX_OUTPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
//            Feature.class,
//            FeatureCollection.class,
//            Geometry.class,
//            Geometry[].class);
//    /**
//     * List of supported <b>input</b> Class for a Literal <b>input</b>.
//     */
//    public static final List LITERAL_INPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
//            Number.class, Boolean.class, String.class,
//            Unit.class,
//            AffineTransform.class,
//            org.opengis.filter.Filter.class,
//            CoordinateReferenceSystem.class,
//            SortBy[].class,
//            NumberRange[].class);
//    /**
//     * List of supported <b>output</b> Class for a Literal <b>output</b>.
//     */
//    public static final List LITERAL_OUPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
//            Number.class, Boolean.class, String.class,
//            Unit.class,
//            AffineTransform.class,
//            CoordinateReferenceSystem.class);
//
//    /*
//     * List of supported <b>input</b> Class for a Reference <b>input</b>.
//     */
//    public static final List REFERENCE_INPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
//            Feature.class,
//            FeatureCollection.class,
//            Geometry.class,
//            File.class,
//            FeatureType.class,
//            FeatureExtend.class,
//            GridCoverage2D.class,
//            GridCoverageReader.class);
//    /*
//     * List of supported <b>output</b> Class for a Reference <b>output</b>.
//     */
//    public static final List REFERENCE_OUTPUT_TYPE_LIST = UnmodifiableArrayList.wrap();
    
    /**
     * Supported CRS.
     */
    private static final SupportedCRSsType WPS_SUPPORTED_CRS;
    static {
        WPS_SUPPORTED_CRS = new SupportedCRSsType();

        //Default CRS.
        final SupportedCRSsType.Default defaultCRS = new SupportedCRSsType.Default();
        defaultCRS.setCRS(DEFAULT_CRS);
        WPS_SUPPORTED_CRS.setDefault(defaultCRS);

        //Supported CRS.
        final CRSsType supportedCRS = new CRSsType();
        final Set<String> allAuth = CRS.getSupportedAuthorities(true);
        for (String auth : allAuth) {
            final Set<String> allCodes = CRS.getSupportedCodes(auth);
            for (String code : allCodes) {
                supportedCRS.getCRS().add(auth + ":" + code);
            }
        }
        WPS_SUPPORTED_CRS.setSupported(supportedCRS);
    }
    
    /**
     * Supported UOM
     */
    private static final SupportedUOMsType WPS_SUPPORTED_UOM;
    static{
        WPS_SUPPORTED_UOM = new SupportedUOMsType();
        final SupportedUOMsType.Default defaultUOM = new SupportedUOMsType.Default();
        final UOMsType supportedUOM = new UOMsType();

        defaultUOM.setUOM(new DomainMetadataType("m", null));

        supportedUOM.getUOM().add(new DomainMetadataType("m", null));
        supportedUOM.getUOM().add(new DomainMetadataType("km", null));
        supportedUOM.getUOM().add(new DomainMetadataType("cm", null));
        supportedUOM.getUOM().add(new DomainMetadataType("mm", null));

        WPS_SUPPORTED_UOM.setDefault(defaultUOM);
        WPS_SUPPORTED_UOM.setSupported(supportedUOM);
    }
    
    /**
     * List of process descriptor avaible.
     */
    private static final List<ProcessDescriptor> PROCESS_DESCRIPTOR_LIST;
    static {
        final List<ProcessDescriptor> descList = new ArrayList<ProcessDescriptor>();
        final Iterator<ProcessingRegistry> factoryIte = ProcessFinder.getProcessFactories();
        while (factoryIte.hasNext()) {
            final ProcessingRegistry factory = factoryIte.next();
            for (ProcessDescriptor descriptor : factory.getDescriptors()) {
                descList.add(descriptor);
            }
        }
        PROCESS_DESCRIPTOR_LIST = UnmodifiableArrayList.wrap(descList.toArray(new ProcessDescriptor[descList.size()]));
    }
    
    

    /**
     * Constructor.
     *
     * @param id
     * @param configurationDirectory
     */
    public WPSWorker(final String id, final File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.WPS);
        isStarted = true;
        if (isStarted) {
            LOGGER.log(Level.INFO, "WPS worker {0} running", id);
        }
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WPSMarshallerPool.getInstance();
    }

    @Override
    public void destroy() {
    }

    //////////////////////////////////////////////////////////////////////
    //                      GetCapabilities
    //////////////////////////////////////////////////////////////////////
    /**
     * GetCapabilities request
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    public WPSCapabilitiesType getCapabilities(final GetCapabilities request) throws CstlServiceException {
        isWorking();

        //check SERVICE=WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter "+ SERVICE_PARAMETER +" must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }
        
        //check LANGUAGE=en-EN
        if(request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)){
             throw new CstlServiceException("The specified "+ LANGUAGE_PARAMETER +" is not handled by the service. ",
                    INVALID_PARAMETER_VALUE, LANGUAGE_PARAMETER.toLowerCase());
        }

        List<String> versionsAccepted = null;
        if (request.getAcceptVersions() != null) {
            versionsAccepted = request.getAcceptVersions().getVersion();
        }

        boolean versionSupported = false;
        if (versionsAccepted != null) {

            if (versionsAccepted.contains(ServiceDef.WPS_1_0_0.version.toString())) {
                versionSupported = true;
            }
        }

        //if versionAccepted parameted is not define return the last getCapabilities
        if (versionsAccepted == null || versionSupported) {
            return getCapabilities100((org.geotoolkit.wps.xml.v100.GetCapabilities) request);
        } else {
           throw new CstlServiceException("The specified " + ACCEPT_VERSIONS_PARAMETER + " numbers are not handled by the service.",
                    VERSION_NEGOTIATION_FAILED, ACCEPT_VERSIONS_PARAMETER.toLowerCase());
        }

    }

    /**
     * GetCapabilities request for WPS 1.0.0.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private WPSCapabilitiesType getCapabilities100(final GetCapabilities request) throws CstlServiceException {

        // We unmarshall the static capabilities document.
        final WPSCapabilitiesType staticCapabilities;
        try {
            staticCapabilities = (WPSCapabilitiesType) getStaticCapabilitiesObject(ServiceDef.WPS_1_0_0.version.toString(), ServiceDef.Specification.WPS.toString());
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex);
        }

        staticCapabilities.getOperationsMetadata().updateURL(getServiceUrl());

        final ProcessOfferings offering = new ProcessOfferings();

        for (final ProcessDescriptor procDesc : PROCESS_DESCRIPTOR_LIST) {
            if (WPSUtils.isSupportedProcess(procDesc)) {
                offering.getProcess().add(WPSUtils.generateProcessBrief(procDesc));
            }
        }

        staticCapabilities.setProcessOfferings(offering);
        return staticCapabilities;
    }

    //////////////////////////////////////////////////////////////////////
    //                      DescibeProcess
    //////////////////////////////////////////////////////////////////////
    /**
     * Describe process request.
     *
     * @param request
     * @return ProcessDescriptions
     * @throws CstlServiceException
     *
     */
    public ProcessDescriptions describeProcess(final DescribeProcess request) throws CstlServiceException {
        isWorking();

        //check SERVICE=WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter "+ SERVICE_PARAMETER +" must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }
        
        //check LANGUAGE=en-EN
        if(request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)){
            throw new CstlServiceException("The specified "+ LANGUAGE_PARAMETER +" is not handled by the service. ",
                    INVALID_PARAMETER_VALUE, LANGUAGE_PARAMETER.toLowerCase());
        }

        //check mandatory version is not missing.
        if (request.getVersion() == null || request.getVersion().isEmpty()) {
            throw new CstlServiceException("The parameter " + VERSION_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
        }

        //check VERSION=1.0.0
        if (request.getVersion().equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return describeProcess100((org.geotoolkit.wps.xml.v100.DescribeProcess) request);
        } else {
             throw new CstlServiceException("The specified " + VERSION_PARAMETER + " number is not handled by the service.",
                    VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Describe a process in WPS v1.0.0.
     *
     * @param request
     * @return ProcessDescriptions
     * @throws CstlServiceException
     */
    private ProcessDescriptions describeProcess100(DescribeProcess request) throws CstlServiceException {

        //check mandatory IDENTIFIER is not missing.
        if (request.getIdentifier() == null || request.getIdentifier().isEmpty()) {
            throw new CstlServiceException("The parameter " + IDENTIFER_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }

        final ProcessDescriptions descriptions = new ProcessDescriptions();
        descriptions.setLang(WPS_LANG);
        descriptions.setService(WPS_SERVICE);
        descriptions.setVersion(WPS_1_0_0);

        for (CodeType identifier : request.getIdentifier()) {

            final ProcessDescriptionType descriptionType = new ProcessDescriptionType();
            descriptionType.setIdentifier(identifier);          //Process Identifier
            descriptionType.setProcessVersion(WPS_1_0_0);
            descriptionType.setWSDL(null);                      //TODO WSDL
            descriptionType.setStatusSupported(false);          //TODO support process status
            descriptionType.setStoreSupported(false);           //TODO support process storage
            

            // Find the process
            final ProcessDescriptor processDesc = WPSUtils.getProcessDescriptor(identifier.getValue());
            if (!WPSUtils.isSupportedProcess(processDesc)) {
                throw new CstlServiceException("Process "+ identifier.getValue() +" not supported by the service.",
                        NO_APPLICABLE_CODE);
            }

            descriptionType.setTitle(new LanguageStringType(processDesc.getIdentifier().getCode()));                //Process Title
            descriptionType.setAbstract(new LanguageStringType(processDesc.getProcedureDescription().toString()));  //Process abstract

            // Get process input and output descriptors
            final ParameterDescriptorGroup input = processDesc.getInputDescriptor();
            final ParameterDescriptorGroup output = processDesc.getOutputDescriptor();

            ///////////////////////////////
            //  Process Input parameters
            ///////////////////////////////
            final ProcessDescriptionType.DataInputs dataInputs = new ProcessDescriptionType.DataInputs();
            for (GeneralParameterDescriptor param : input.descriptors()) {
                
                // If the Parameter Descriptor isn't a GroupeParameterDescriptor
                if (param instanceof ParameterDescriptor) {
                    final InputDescriptionType in = new InputDescriptionType();
                    final ParameterDescriptor paramDesc = (ParameterDescriptor) param;

                    // Parameter informations
                    in.setIdentifier(new CodeType(WPSUtils.buildProcessIOIdentifiers(processDesc, paramDesc, WPSIO.IOType.INPUT)));
                    in.setTitle(new LanguageStringType(WPSUtils.capitalizeFirstLetter(paramDesc.getName().getCode())));
                    in.setAbstract(new LanguageStringType(paramDesc.getRemarks().toString()));

                    //set occurs
                    in.setMaxOccurs(BigInteger.valueOf(paramDesc.getMaximumOccurs()));
                    in.setMinOccurs(BigInteger.valueOf(paramDesc.getMinimumOccurs()));
                    // Input class
                    final Class clazz = paramDesc.getValueClass();

                    // BoundingBox type
                    if (clazz.equals(Envelope.class)) {
                        in.setBoundingBoxData(WPS_SUPPORTED_CRS);

                        //Complex type (XML, ...)     
                    } else if (WPSIO.isSupportedComplexInputClass(clazz)) {
                        in.setComplexData(WPSUtils.describeComplex(clazz, WPSIO.IOType.INPUT));

                        //Simple object (Integer, double, ...) and Object which need a conversion from String like affineTransform or WKT Geometry
                    } else if(WPSIO.isSupportedLiteralInputClass(clazz)){
                        final LiteralInputType literal = new LiteralInputType();

                        if (paramDesc.getDefaultValue() != null) {
                            literal.setDefaultValue(paramDesc.getDefaultValue().toString()); //default value if enable
                        }
                        literal.setAnyValue(new AnyValue());
                        literal.setDataType(WPSUtils.createDataType(clazz));
                        literal.setUOMs(WPS_SUPPORTED_UOM);

                        in.setLiteralData(literal);
                        
                    }else{
                        throw new CstlServiceException("Process output not supported.", OPERATION_NOT_SUPPORTED);
                    }
                    
                    dataInputs.getInput().add(in);
                } else {
                    throw new CstlServiceException("Process parameter invalid", OPERATION_NOT_SUPPORTED);
                }
            }
            descriptionType.setDataInputs(dataInputs);

             ///////////////////////////////
            //  Process Output parameters
            ///////////////////////////////
            final ProcessDescriptionType.ProcessOutputs dataOutput = new ProcessDescriptionType.ProcessOutputs();
            for (GeneralParameterDescriptor param : output.descriptors()) {
                final OutputDescriptionType out = new OutputDescriptionType();

                //simple paramater
                if (param instanceof ParameterDescriptor) {
                    final ParameterDescriptor paramDesc = (ParameterDescriptor) param;

                    //parameter informations
                    out.setIdentifier(new CodeType(WPSUtils.buildProcessIOIdentifiers(processDesc, paramDesc, WPSIO.IOType.OUTPUT)));
                    out.setTitle(new LanguageStringType(WPSUtils.capitalizeFirstLetter(paramDesc.getName().getCode())));
                    out.setAbstract(new LanguageStringType(paramDesc.getRemarks().toString()));

                    //input class
                    final Class clazz = paramDesc.getValueClass();

                    //BoundingBox type
                    if (clazz.equals(JTSEnvelope2D.class)) {
                        out.setBoundingBoxOutput(WPS_SUPPORTED_CRS);

                        //Complex type (XML, raster, ...)
                    } else if (WPSIO.isSupportedComplexOutputClass(clazz)) {
                        out.setComplexOutput((SupportedComplexDataType) WPSUtils.describeComplex(clazz, WPSIO.IOType.OUTPUT));

                        //Simple object (Integer, double) and Object which need a conversion from String like affineTransform or Geometry
                    } else if (WPSIO.isSupportedLiteralOutputClass(clazz)) {

                        final LiteralOutputType literal = new LiteralOutputType();
                        literal.setUOMs(WPS_SUPPORTED_UOM);
                        literal.setDataType(WPSUtils.createDataType(clazz));

                        out.setLiteralOutput(literal);
                    }else{
                        throw new CstlServiceException("Process output not supported.", OPERATION_NOT_SUPPORTED);
                    }

                } else {
                    throw new CstlServiceException("Process parameter invalid", OPERATION_NOT_SUPPORTED);
                }

                dataOutput.getOutput().add(out);
            }
            descriptionType.setProcessOutputs(dataOutput);
            descriptions.getProcessDescription().add(descriptionType);
        }

        return descriptions;
    }

    //////////////////////////////////////////////////////////////////////
    //                      Execute
    //////////////////////////////////////////////////////////////////////
    /**
     * Redirect execute requests from the WPS version requested.
     *
     * @param request
     * @return execute response (Raw data or Document response) depends of the ResponseFormType in execute request
     * @throws CstlServiceException
     */
    public Object execute(Execute request) throws CstlServiceException {
        isWorking();

        //if requested SERVICE param is different than WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter SERVICE must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }

        final String version = request.getVersion().toString();
        if (version.isEmpty()) {
            throw new CstlServiceException("The parameter VERSION must be specified.",
                    MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
        }

        if (version.equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return execute100((org.geotoolkit.wps.xml.v100.Execute) request);
        } else {
            throw new CstlServiceException("The version number specified for this discribeProcess request "
                    + "is not handled.", NO_APPLICABLE_CODE, VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Execute a process in wps v1.0
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private Object execute100(Execute request) throws CstlServiceException {
        if (request.getIdentifier() == null) {
            throw new CstlServiceException("The parameter Identifier must be specified.",
                    MISSING_PARAMETER_VALUE, "identifier");
        }
        final StatusType status = new StatusType();
        LOGGER.info("LOG -> Process : " + request.getIdentifier().getValue());
        //Find the process
        final ProcessDescriptor processDesc = WPSUtils.getProcessDescriptor(request.getIdentifier().getValue());

        if (!WPSUtils.isSupportedProcess(processDesc)) {
            throw new CstlServiceException("Process not supported by the service.",
                    OPERATION_NOT_SUPPORTED, request.getIdentifier().getValue());
        }

        //status.setProcessAccepted("Process "+request.getIdentifier().getValue()+" found.");

        boolean isOutputRaw = false; // the default output is a ResponseDocument

        /*
         * Get the requested output form
         */
        final ResponseFormType responseForm = request.getResponseForm();
        final OutputDefinitionType rawData = responseForm.getRawDataOutput();
        final ResponseDocumentType respDoc = responseForm.getResponseDocument();

        /*
         * Raw output data attributs
         */
        String rawOutputID = null;
        String rawOutputMime = null;
        String rawOutputEncoding = null;
        String rawOutputSchema = null;
        String rawOutputUom = null;

        /*
         * ResponseDocument attributs
         */
        boolean isLineage = false;
        boolean useStatus = false;
        boolean useStorage = false;
        ExecuteResponse response = null;
        List<DocumentOutputDefinitionType> wantedOutputs = null;

        /*
         * Raw Data
         */
        if (rawData != null) {
            isOutputRaw = true;
            rawOutputID = rawData.getIdentifier().getValue();
            rawOutputMime = rawData.getMimeType();
            rawOutputEncoding = rawData.getEncoding();
            rawOutputSchema = rawData.getSchema();
            rawOutputUom = rawData.getUom();

            /*
             * ResponseDocument
             */
        } else if (respDoc != null) {

            isLineage = respDoc.isLineage();

            // Status and storage desactivated for now
            useStatus = respDoc.isStatus();
            //useStorage = respDoc.isStoreExecuteResponse();

            wantedOutputs = respDoc.getOutput();

            response = new ExecuteResponse();
            response.setService(WPS_SERVICE);
            response.setVersion(WPS_1_0_0);
            response.setLang(WPS_LANG);
            response.setServiceInstance(null);      //TODO getCapabilities URL

            //Give a bief process description into the execute response
            response.setProcess(WPSUtils.generateProcessBrief(processDesc));

            LOGGER.info("LOG -> Lineage=" + isLineage);
            LOGGER.info("LOG -> Storage=" + useStorage);
            LOGGER.info("LOG -> Status=" + useStatus);

            if (isLineage) {
                //Inputs
                response.setDataInputs(request.getDataInputs());
                final OutputDefinitionsType outputsDef = new OutputDefinitionsType();
                outputsDef.getOutput().addAll(respDoc.getOutput());
                //Outputs
                response.setOutputDefinitions(outputsDef);
            }

            if (useStorage) {
                response.setStatusLocation(null); //Output data URL
            }

            if (useStatus) {
                response.setStatus(status);
            }
        } else {
        }
        //Input temporary files used by the process. In order to delete them at the end of the process.
        List<File> files = null;

        //Create Process and Inputs
        final ParameterValueGroup in = processDesc.getInputDescriptor().createValue();

        /**
         * ****************
         * Process INPUT
         *****************
         */
        final List<InputType> requestInputData = request.getDataInputs().getInput();
        final List<GeneralParameterDescriptor> processInputDesc = processDesc.getInputDescriptor().descriptors();

        /*
         * Check for a missing input parameter
         */
        if (requestInputData.size() != processInputDesc.size()) {
            for (GeneralParameterDescriptor generalParameterDescriptor : processInputDesc) {
                boolean inputFound = false;
                final String processInputID = generalParameterDescriptor.getName().getCode();
                for (InputType inputRequest : requestInputData) {
                    if (inputRequest.getIdentifier().getValue().equals(processInputID)) {
                        inputFound = true;
                    }
                }
                // if the parameter is not found and if it's a mandatory parameter
                if (!inputFound) {
                    if (generalParameterDescriptor.getMinimumOccurs() != 0) {
                        throw new CstlServiceException("Mandatory input parameter is"
                                + "missing.", MISSING_PARAMETER_VALUE, processInputID);
                    }
                }
            }
        }

        //Fill input process with there default values
        for (GeneralParameterDescriptor inputGeneDesc : processDesc.getInputDescriptor().descriptors()) {

            if (inputGeneDesc instanceof ParameterDescriptor) {
                final ParameterDescriptor inputDesc = (ParameterDescriptor) inputGeneDesc;

                if (inputDesc.getDefaultValue() != null) {
                    in.parameter(inputDesc.getName().getCode()).setValue(inputDesc.getDefaultValue());
                }
            } else {
                throw new CstlServiceException("Process parameter invalid", OPERATION_NOT_SUPPORTED);
            }
        }

        //Each input from the request
        for (InputType inputRequest : requestInputData) {

            if (inputRequest.getIdentifier() == null) {
                throw new CstlServiceException("Missing input Identifier.", INVALID_PARAMETER_VALUE);
            }

            final String inputIdentifier = inputRequest.getIdentifier().getValue();

            //Check if it's a valid input identifier
            final List<GeneralParameterDescriptor> processInputList = processDesc.getInputDescriptor().descriptors();
            boolean existInput = false;
            for (GeneralParameterDescriptor processInput : processInputList) {
                if (processInput.getName().getCode().equals(inputIdentifier)) {
                    existInput = true;
                }
            }
            if (!existInput) {
                throw new CstlServiceException("Unknow input Identifier.", INVALID_PARAMETER_VALUE, inputIdentifier);
            }

            final GeneralParameterDescriptor inputGeneralDescriptor = processDesc.getInputDescriptor().descriptor(inputIdentifier);
            ParameterDescriptor inputDescriptor;

            if (inputGeneralDescriptor instanceof ParameterDescriptor) {
                inputDescriptor = (ParameterDescriptor) inputGeneralDescriptor;
            } else {
                throw new CstlServiceException("The input Identifier is invalid.", INVALID_PARAMETER_VALUE, inputIdentifier);
            }

            /*
             * Get expected input Class from the process input
             */
            final Class expectedClass = inputDescriptor.getValueClass();


            Object dataValue = null;
            LOGGER.info("Expected Class = " + expectedClass.getCanonicalName());

            /*
             * A referenced input data
             */
            if (inputRequest.getReference() != null) {

                //Check if the expected class is supproted for literal using
                if (!WPSIO.isSupportedReferenceInputClass(expectedClass)) {
                    throw new CstlServiceException("Reference value expected", INVALID_PARAMETER_VALUE, inputIdentifier);
                }

                LOGGER.info("LOG -> Input -> Reference");
                final String href = inputRequest.getReference().getHref();
                final String method = inputRequest.getReference().getMethod();
                final String mime = inputRequest.getReference().getMimeType();
                final String encoding = inputRequest.getReference().getEncoding();
                final String schema = inputRequest.getReference().getSchema();

                dataValue = WPSUtils.reachReferencedData(href, method, mime, encoding, schema, expectedClass, inputIdentifier);
                if (dataValue instanceof FeatureCollection) {
                    dataValue = (FeatureCollection) dataValue;
                }
                if (dataValue instanceof File) {
                    if (files == null) {
                        files = new ArrayList<File>();
                    }
                    files.add((File) dataValue);
                }

                /*
                 * Encapsulated data into the Execute Request
                 */
            } else if (inputRequest.getData() != null) {

                /*
                 * BoundingBox data
                 */
                if (inputRequest.getData().getBoundingBoxData() != null) {
                    LOGGER.info("LOG -> Input -> Boundingbox");
                    final BoundingBoxType bBox = inputRequest.getData().getBoundingBoxData();
                    final List<Double> lower = bBox.getLowerCorner();
                    final List<Double> upper = bBox.getUpperCorner();
                    final String crs = bBox.getCrs();
                    final int dimension = bBox.getDimensions();

                    //Check if it's a 2D boundingbox
                    if (dimension != 2 || lower.size() != 2 || upper.size() != 2) {
                        throw new CstlServiceException("Invalid data input : Only 2 dimension boundingbox supported.", OPERATION_NOT_SUPPORTED, inputIdentifier);
                    }

                    CoordinateReferenceSystem crsDecode;
                    try {
                        crsDecode = CRS.decode(crs);
                    } catch (FactoryException ex) {
                        throw new CstlServiceException("Invalid data input : CRS not supported.",
                                ex, OPERATION_NOT_SUPPORTED, inputIdentifier);
                    }

                    final Envelope envelop = GeometryUtils.createCRSEnvelope(crsDecode, lower.get(0), lower.get(1), upper.get(0), upper.get(1));
                    dataValue = envelop;

                    /*
                     * Complex data (XML, raster, ...)
                     */
                } else if (inputRequest.getData().getComplexData() != null) {

                    //Check if the expected class is supproted for complex using
                    if (!WPSIO.isSupportedComplexInputClass(expectedClass)) {
                        throw new CstlServiceException("Complex value expected", INVALID_PARAMETER_VALUE, inputIdentifier);
                    }

                    LOGGER.info("LOG -> Input -> Complex");

                    final ComplexDataType complex = inputRequest.getData().getComplexData();
                    final String mime = complex.getMimeType();
                    final String encoding = complex.getEncoding();
                    final List<Object> content = complex.getContent();
                    final String schema = complex.getSchema();

                    if (content.size() <= 0) {
                        throw new CstlServiceException("Missing data input value.", INVALID_PARAMETER_VALUE, inputIdentifier);

                    } else {

                        final List<Object> inputObject = new ArrayList<Object>();
                        for (Object obj : content) {
                            if (obj != null) {
                                if (!(obj instanceof String)) {
                                    inputObject.add(obj);
                                }
                            }
                        }

                        if (inputObject == null) {
                            throw new CstlServiceException("Invalid data input value : Empty value.", INVALID_PARAMETER_VALUE, inputIdentifier);
                        }

                        /*
                         * Extract Data from inputObject array
                         */
                        dataValue = WPSUtils.extractComplexInput(expectedClass, inputObject, schema, mime, encoding, inputIdentifier);
                    }

                    /*
                     * Literal data
                     */
                } else if (inputRequest.getData().getLiteralData() != null) {
                    //Check if the expected class is supproted for literal using
                    if (!WPSIO.isSupportedLiteralInputClass(expectedClass)) {
                        throw new CstlServiceException("Literal value expected", INVALID_PARAMETER_VALUE, inputIdentifier);
                    }

                    LOGGER.info("LOG -> Input -> Literal");

                    final LiteralDataType literal = inputRequest.getData().getLiteralData();
                    final String data = literal.getValue();

                    //convert String into expected type
                    dataValue = WPSUtils.convertFromString(data, expectedClass);
                    LOGGER.info("DEBUG -> Input -> Literal -> Value=" + dataValue);

                } else {
                    throw new CstlServiceException("Invalid input data type.", INVALID_REQUEST, inputIdentifier);
                }
            } else {
                throw new CstlServiceException("Invalid input data format.", INVALID_REQUEST, inputIdentifier);
            }

            try {
                in.parameter(inputIdentifier).setValue(dataValue);
            } catch (InvalidParameterValueException ex) {
                throw new CstlServiceException("Invalid data input value.", ex, INVALID_PARAMETER_VALUE, inputIdentifier);
            }
        }

        //Give input parameter to the process
        final org.geotoolkit.process.Process proc = processDesc.createProcess(in);

        //Status
        final ProcessStartedType started = new ProcessStartedType();
        started.setValue("Process " + request.getIdentifier().getValue() + " is started");
        started.setPercentCompleted(0);
        //status.setProcessStarted(started);

        //Run the process
        final ParameterValueGroup result;
        try {
            result = proc.call();
        } catch (ProcessException ex) {
            throw new CstlServiceException("Process execution failed");
        }

        /**
         * ****************
         * Process OUTPUT *
         *****************
         */
        /*
         * Storage data
         */
        if (useStorage) {
            //TODO storage output
            throw new UnsupportedOperationException("Output storage not yet implemented");
            /*
             * No strorage
             */
        } else {
            /*
             * Raw Data returned
             */
            if (isOutputRaw) {
                LOGGER.info("LOG -> Output -> Raw");
                final Object outputValue = result.parameter(rawOutputID).getValue();
                LOGGER.info("DEBUG -> Output -> Raw -> Value=" + outputValue);

                if (outputValue instanceof Geometry) {
                    try {
                        final Geometry jtsGeom = (Geometry) outputValue;
                        final AbstractGeometryType gmlGeom = JTStoGeometry.toGML(jtsGeom);
                        return gmlGeom;
                    } catch (NoSuchAuthorityCodeException ex) {
                        throw new CstlServiceException(ex);
                    } catch (FactoryException ex) {
                        throw new CstlServiceException(ex);
                    }

                }

                if (outputValue instanceof Envelope) {
                    return new BoundingBoxType((Envelope) outputValue);
                }
                return outputValue;

                /*
                 * DocumentResponse returned
                 */
            } else {
                LOGGER.info("LOG -> Output -> Document");
                final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
                //Process Outputs
                for (GeneralParameterDescriptor outputDescriptor : processDesc.getOutputDescriptor().descriptors()) {

                    final OutputDataType outData = new OutputDataType();

                    //set Ouput informations
                    final String outputIdentifier = outputDescriptor.getName().getCode();
                    outData.setIdentifier(new CodeType(outputIdentifier));
                    outData.setTitle(new LanguageStringType(outputIdentifier));
                    outData.setAbstract(new LanguageStringType(outputDescriptor.getRemarks().toString()));

                    /*
                     * Output value from process
                     */
                    final Object outputValue = result.parameter(outputIdentifier).getValue();

                    final DataType data = new DataType();
                    if (outputDescriptor instanceof ParameterDescriptor) {

                        final ParameterDescriptor outParamDesc = (ParameterDescriptor) outputDescriptor;
                        /*
                         * Output Class
                         */
                        final Class outClass = outParamDesc.getValueClass();

                        /*
                         * Bounding Box
                         */
                        if (outClass.equals(Envelope.class)) {
                            LOGGER.info("LOG -> Output -> BoundingBox");
                            org.opengis.geometry.Envelope envelop = (org.opengis.geometry.Envelope) outputValue;

                            data.setBoundingBoxData(new BoundingBoxType(envelop));

                            /*
                             * Complex
                             */
                        } else if (WPSIO.isSupportedComplexOutputClass(outClass)) {
                            LOGGER.info("LOG -> Output -> Complex");
                            final ComplexDataType complex = new ComplexDataType();

                            for (DocumentOutputDefinitionType wO : wantedOutputs) {
                                if (wO.getIdentifier().getValue().equals(outputIdentifier)) {
                                    complex.setEncoding(wO.getEncoding());
                                    complex.setMimeType(wO.getMimeType());
                                    complex.setSchema(wO.getSchema());
                                }
                            }

                            final ObjectConverter converter = WPSIO.getConverter(outClass, WPSIO.IOType.OUTPUT, WPSIO.DataType.COMPLEX, complex.getMimeType());
                            
                            if (converter == null) {
                                throw new CstlServiceException("Input complex not supported, no converter found.",
                                        OPERATION_NOT_SUPPORTED, outputIdentifier);
                            }

                            try {
                                complex.getContent().addAll((Collection<Object>) converter.convert(outputValue));
                            } catch (NonconvertibleObjectException ex) {
                                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, outputIdentifier);
                            }

                            data.setComplexData(complex);

                            /*
                             * Literal
                             */
                        } else if (WPSIO.isSupportedLiteralOutputClass(outClass)) {
                            LOGGER.info("LOG -> Output -> Literal");
                            final LiteralDataType literal = new LiteralDataType();
                            literal.setDataType(outClass.getCanonicalName());
                            if (outputValue == null) {
                                literal.setValue(null);
                            } else {
                                literal.setValue(outputValue.toString());
                            }
                            data.setLiteralData(literal);

                        } else {
                            throw new CstlServiceException("Process output parameter invalid", OPERATION_NOT_SUPPORTED, outputIdentifier);
                        }
                    } else {
                        throw new CstlServiceException("Process output parameter invalid", OPERATION_NOT_SUPPORTED, outputIdentifier);
                    }

                    outData.setData(data);
                    outputs.getOutput().add(outData);
                }

                response.setProcessOutputs(outputs);

                if (useStatus) {
                    response.setStatus(new StatusType());
                }
                status.setProcessSucceeded("Process " + request.getIdentifier().getValue() + " finiched.");
                if (useStatus) {
                    response.setStatus(status);
                }

                //Delete input temporary files 
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }

                return response;
            }
        }
    }
}

