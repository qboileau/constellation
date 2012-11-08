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
package org.constellation.configuration;

import java.awt.Dimension;
import java.awt.Font;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.geotoolkit.display2d.ext.BackgroundTemplate;
import org.geotoolkit.display2d.ext.legend.DefaultLegendTemplate;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LegendDecoration extends PositionableDecoration {
    
    @XmlElement(name="Gap")
    private Float gap;
    
    @XmlElement(name="MainFont")
    private String mainFont;
    
    @XmlElement(name="SecondFont")
    private String secondFont;
    
    @XmlElement(name="LayerVisible")
    private Boolean layerVisible;

    @XmlElement(name="GlyphWidth")
    private Integer glyphWidth;
    
    @XmlElement(name="GlyphHeight")
    private Integer glyphHeight;

    public LegendDecoration() {
        this(2.0f, "Arial-BOLD-14", "Arial-ITALIC-11", true, 20, 30, new Background(), 0, 0, PositionableDecoration.POSITION_EAST);
    }

    public LegendDecoration(final Float gap, final String mainFont, final String secondFont, final Boolean layerName, 
            final Integer glyphWidth, final Integer glyphHeight, final Background background, final Integer offsetX, 
            final Integer offsetY, final String position) {
        super(background, offsetX, offsetY, position);
        this.gap = gap;
        this.mainFont = mainFont;
        this.secondFont = secondFont;
        this.layerVisible = layerName;
        this.glyphWidth = glyphWidth;
        this.glyphHeight = glyphHeight;
    }

    public Float getGap() {
        return gap;
    }

    public void setGap(Float gap) {
        this.gap = gap;
    }

    public String getMainFont() {
        return mainFont;
    }

    public void setMainFont(String mainFont) {
        this.mainFont = mainFont;
    }

    public String getSecondFont() {
        return secondFont;
    }

    public void setSecondFont(String secondFont) {
        this.secondFont = secondFont;
    }

    public Boolean getLayerVisible() {
        return layerVisible;
    }

    public void setLayerVisible(Boolean layerName) {
        this.layerVisible = layerName;
    }

    public Integer getGlyphWidth() {
        return glyphWidth;
    }

    public void setGlyphWidth(Integer glyphWidth) {
        this.glyphWidth = glyphWidth;
    }

    public Integer getGlyphHeight() {
        return glyphHeight;
    }

    public void setGlyphHeight(Integer glyphHeight) {
        this.glyphHeight = glyphHeight;
    }
    
    /**
     * Convert to geotoolkit LegendTemplate.
     * @return org.geotoolkit.display2d.ext.legend.LegendTemplate
     */
    public org.geotoolkit.display2d.ext.legend.LegendTemplate toLegendTemplate() {
        
        final BackgroundTemplate dispBackground = getBackground().toBackgroundTemplate();
        final Font font1 = mainFont != null ? Font.decode(mainFont) : new Font("serial", Font.BOLD, 14);
        final Font font2 = secondFont != null ? Font.decode(secondFont) : new Font("serial", Font.BOLD, 14);
        
        final Dimension glyphSize;
        if(glyphWidth != null && glyphHeight != null){
            glyphSize = new Dimension( glyphWidth , glyphHeight ) ;
        }else{
            glyphSize = null;
        }
        
        return new DefaultLegendTemplate(dispBackground, gap != null ? gap : 2.0f, glyphSize, font1, layerVisible, font2);
    } 
}