package org.constellation.gui.service;

import juzu.Mapped;

/**
 * POJO {@link org.constellation.configuration.Instance} mirror
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@Mapped
public class InstanceSummary {

    /**
     * service name
     */
    private String name;

    /**
     * service type
     */
    private String type;

    /**
     * service status
     */
    private String status;

    /**
     * service abstract
     */
    private String _abstract;

    /**
     * service layer number
     */
    private int layersNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public int getLayersNumber() {
        return layersNumber;
    }

    public void setLayersNumber(int layersNumber) {
        this.layersNumber = layersNumber;
    }
}