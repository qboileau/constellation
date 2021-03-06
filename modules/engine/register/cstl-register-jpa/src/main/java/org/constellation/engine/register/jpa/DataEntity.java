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
package org.constellation.engine.register.jpa;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.User;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.Set;

@Entity
@Table(schema = "`admin`", name = "`data`")
public class DataEntity implements Data {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;

    @Column(name = "`name`")
    private String name;
    @Column(name = "`namespace`")
    private String namespace;

    @ManyToOne(targetEntity=ProviderEntity.class)
    @JoinColumn(name = "`provider`")
    private Provider provider;

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable( schema="`admin`", name = "`crs`", joinColumns = { @JoinColumn(name = "`dataid`", referencedColumnName = "`id`")})
    @Column(name="`crscode`")
    private Set<Integer> crs;


    @Column(name = "`type`")
    private String type;

    @Column(name = "`date`")
    private long date;

    @Column(name = "`title`")
    private int title;

    @Column(name = "`description`")
    private int description;
    
    @ManyToOne(targetEntity=UserEntity.class)
    @JoinColumn(name = "`owner`")
    private User owner;

    @ManyToMany(mappedBy="datas", targetEntity=StyleEntity.class)
    private List<Style> styles;

    @OneToMany(mappedBy="data", targetEntity=LayerEntity.class)
    private List<Layer> layers;
    
    @Column(name = "`metadata`")
    private String metadata;

    
    @ManyToMany(mappedBy="datas", targetEntity=DomainEntity.class)
    private Set<Domain> domains;

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

//    public Provider getParent() {
//        return parent;
//    }
//
//    public void setParent(Provider parent) {
//        this.parent = parent;
//    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getDescription() {
        return description;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "Data [id=" + id + ", name=" + name + ", namespace=" + namespace + ", provider=" + provider /* + ", parent=" + parent */ +", type="
                + type + ", date=" + date + ", title=" + title + ", description=" + description + ", owner=" + owner
                + ", styles=" + styles + ", metadata="+metadata+ " crs=" +crs + "]";
    }
}
