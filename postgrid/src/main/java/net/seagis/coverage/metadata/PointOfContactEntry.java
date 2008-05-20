/*
 * Ecocast - NASA Ames Research Center
 * (C) 2008, Ecocast
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.metadata;


import net.seagis.catalog.Entry;


/**
 * Implementation of a {@linkplain PointOfContact layer metadata entry}.
 *
 * @author Sam Hiatt
 * @version $Id: PointOfContactEntry.java  $
 */
final class PointOfContactEntry extends Entry implements PointOfContact {

    private String pocId;
    private String lastName;
    private String firstName;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String zip;
    private String phone;
    private String email;
    private String org;
    private String orgAddress1;
    private String orgAddress2;
    private String orgCity;
    private String orgState;
    private String orgZip;
    private String orgCountry;
    private String orgContact;

    protected PointOfContactEntry(
        final String pocId,
        final String lastName,
        final String firstName,
        final String address1,
        final String address2,
        final String city,
        final String state,
        final String country,
        final String zip,
        final String phone,
        final String email,
        final String org,
        final String orgAddress1,
        final String orgAddress2,
        final String orgCity,
        final String orgState,
        final String orgZip,
        final String orgCountry,
        final String orgContact                                                                                                                                )

    {
        super(pocId);
        this.pocId = pocId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zip = zip;
        this.phone = phone;
        this.email = email;
        this.org = org;
        this.orgAddress1 = orgAddress1;
        this.orgAddress2 = orgAddress2;
        this.orgCity = orgCity;
        this.orgState = orgState;
        this.orgZip = orgZip;
        this.orgCountry = orgCountry;
        this.orgContact = orgContact;
    }    

    public String getMetadata() {
        String res = 
                "poc id:            " + pocId + "\n" +
                "last name:         " + lastName + "\n" +
                "first name:        " + firstName + "\n" +
                "address1:          " + address1 + "\n" +
                "address2:          " + address2 + "\n" +
                "city:              " + city + "\n" +
                "state:             " + state + "\n" +
                "country:           " + country + "\n" +
                "zip:               " + zip + "\n" +
                "phone:             " + phone + "\n" +
                "email:             " + email + "\n" +
                "org:               " + org + "\n" +
                "org address1:      " + orgAddress1 + "\n" +
                "org address2:      " + orgAddress2 + "\n" +
                "org city:          " + orgCity + "\n" +
                "org state:         " + orgState + "\n" +
                "org country:       " + orgCountry + "\n" +
                "org zip:           " + orgZip + "\n" +
                "org contact:       " + orgContact + "\n" ;
                
        return res;
    }
}
