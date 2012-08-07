
package org.constellation.util;

import org.geotoolkit.feature.DefaultName;
import org.opengis.feature.type.Name;

/**
 * Reference to a provider or service layer relative to dcns server.
 * The reference pattern depend of the type of input data.
 *
 * If DataReference is from a provider layer, the pattern will be like :
 * <code>${providerLayerType|providerId|layerId}</code> for example <code>${providerLayerType|shapeFileProvider|CountiesLayer}</code>
 *
 * If DataReference is from a provider Style, the pattern will be like :
 * <code>${providerStyleType|providerId|layerId}</code> for example <code>${providerStyleType|sldProvider|flashyStyle}</code>
 *
 * If DataReference is from a Service, the pattern will be like :
 * <code>${serviceType|serviceURL|serviceSpec|serviceId|layerId}</code> for example <code>${serviceType|http://localhost:8080/cstl/WS/wms/defaultInstance|WMS|defaultInstance|CountiesLayer}</code>
 *
 * @author Johann Sorel (Geomatys)
 * @author Quentin Boileau (Geomatys).
 */
public class DataReference implements CharSequence{

    private static final String SEPARATOR = "|";
    /*
     * Data types
     */
    public static String PROVIDER_LAYER_TYPE = "providerLayerType";
    public static String PROVIDER_STYLE_TYPE = "providerStyleType";
    public static String SERVICE_TYPE        = "serviceType";

    private String reference;
    private String type;
    private String providerId;
    private String serviceURL;
    private String serviceSpec;
    private String serviceId;
    private String layerId;

    public DataReference(final String str) {
        this.reference = str;
        computeReferenceParts();
    }

    public DataReference (final String dataType, final String providerId, final String serviceURL, final String serviceSpec, final String serviceId, final String layerId) {
        if (dataType != null && (dataType.equals(PROVIDER_LAYER_TYPE) || dataType.equals(PROVIDER_STYLE_TYPE) || dataType.equals(SERVICE_TYPE))) {
            this.type       = dataType;
        }
        this.serviceURL     = serviceURL;
        this.providerId     = providerId;
        this.serviceSpec    = serviceSpec;
        this.serviceId      = serviceId;
        this.layerId        = layerId;
        this.reference      = buildRefrenceString();
    }


    /**
     * Create a DataReference from a provider layer or style.
     * @param providerType like providerLayerType or providerStyleType
     * @param providerId provider identifier
     * @param layerId layer identifier
     * @return DataReference
     */
    public static DataReference createProviderDataReference(final String providerType, final String providerId, final String layerId) {
        if (providerType != null && (providerType.equals(PROVIDER_LAYER_TYPE) || providerType.equals(PROVIDER_STYLE_TYPE))) {
            return new DataReference(providerType, providerId, null, null, null, layerId);
        }
        throw new IllegalArgumentException("Reference should match pattern ${providerLayerType|providerId|layerId} or ${providerStyleType|providerId|layerId} or ${serviceType|serviceURL|serviceSpec|serviceId|layerId}.");
    }

    /**
     * Create a DataReference from a service.
     * @param serviceSpec like WMS, WFS, ...
     * @param serviceId instance identifier of the service
     * @param layerId layer identifier
     * @return DataReference
     */
    public static DataReference createServiceDataReference(final String serviceURL, final String serviceSpec, final String serviceId, final String layerId) {
        return new DataReference(SERVICE_TYPE, null,serviceURL, serviceSpec, serviceId, layerId);
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
        computeReferenceParts();
    }

    /**
     * Split reference string.
     */
    private void computeReferenceParts () {
        if (reference != null && reference.startsWith("${") && reference.endsWith("}")) {
            final String datas = reference.substring(2,reference.length()-1);

            final String[] dataSplit = datas.split("\\"+SEPARATOR);
            final int groupCount = dataSplit.length;

            final String datatype = dataSplit[0];
            //get data type
            if (!datatype.isEmpty() && (datatype.equals(PROVIDER_LAYER_TYPE) || datatype.equals(PROVIDER_STYLE_TYPE) ) && groupCount == 3 ) {
                type = datatype;
            } else if (!datatype.isEmpty() && datatype.equals(SERVICE_TYPE) && groupCount == 5) {
                type = datatype;
            } else {
                throw new IllegalArgumentException("Reference data should be type of providerLayerType or providerStyleType or serviceType.");
            }

            if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {

                this.serviceSpec = null;
                this.serviceId = null;
                this.providerId = dataSplit[1];     //providerID
                this.layerId = dataSplit[2];        //layerID

            } else if (type.equals(SERVICE_TYPE)) {

                this.providerId = null;
                this.serviceURL = dataSplit[1];     //http://localhost:8080/cstl/WS/wms/serviceID
                this.serviceSpec = dataSplit[2];    //WMS
                this.serviceId = dataSplit[3];      //serviceID
                this.layerId = dataSplit[4];        //layerID

            }
        } else {
            throw new IllegalArgumentException("Reference should match pattern ${providerLayerType|providerId|layerId} or ${providerStyleType|providerId|layerId} or ${serviceType|serviceURL|serviceSpec|serviceId|layerId}.");
        }
    }

    /**
     * Make the reference string from parts.
     */
    private String buildRefrenceString() {
        final StringBuffer buffer = new StringBuffer("${");
        buffer.append(getDataType()).append(SEPARATOR);

        if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {

            buffer.append(providerId).append(SEPARATOR);
            buffer.append(layerId);

        } else if (type.equals(SERVICE_TYPE)) {

            buffer.append(serviceURL).append(SEPARATOR);
            buffer.append(serviceSpec).append(SEPARATOR);
            buffer.append(serviceId).append(SEPARATOR);
            buffer.append(layerId);
        }

        buffer.append("}");
        return buffer.toString();
    }

    /**
     * Return the type of dataRefrence, <code>providerLayerType</code> or <code>providerStyleType</code> or <code>serviceType</code>.
     * @return String or null if type is undefined.
     */
    public String getDataType() {
        return type;
    }

    /**
     * The service specification part of the data.
     * @return String
     */
    public String getServiceSpec(){
        return serviceSpec;
    }

    /**
     * The service server URL part of the data.
     * @return String
     */
    public String getServiceURL(){
        return serviceURL;
    }

    /**
     * Read the service/provider id part of the data.
     * @return String
     */
    public String getServiceId(){
        if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {
            return providerId;
        } else if (type.equals(SERVICE_TYPE)) {
            return serviceId;
        }
        return null;
    }

    /**
     * Read the layer id part of the data.
     * @return Name
     */
    public Name getLayerId(){
        return DefaultName.valueOf(layerId);
    }

    @Override
    public int length() {
        return reference.length();
    }

    @Override
    public char charAt(int index) {
        return reference.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return reference.subSequence(start, end);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.reference != null ? this.reference.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataReference other = (DataReference) obj;
        if ((this.reference == null) ? (other.reference != null) : !this.reference.equals(other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[DataReference]");
        sb.append("reference:\n").append(reference).append('\n');
        sb.append("type:\n").append(type).append('\n');
        sb.append("providerId:\n").append(providerId).append('\n');
        sb.append("serviceURL:\n").append(serviceURL).append('\n');
        sb.append("serviceSpec:\n").append(serviceSpec).append('\n');
        sb.append("serviceId:\n").append(serviceId).append('\n');
        sb.append("layerId:\n").append(layerId).append('\n');
        return sb.toString();
    }

}
