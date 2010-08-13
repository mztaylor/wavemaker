/*
 *  Copyright (C) 2009 WaveMaker Software, Inc.
 *
 *  This file is part of the WaveMaker Server Runtime.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.wavemaker.runtime.data.sample.adventure;
// Generated Aug 18, 2007 5:20:14 PM by Hibernate Tools 3.2.0.b9


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Address generated by hbm2java
 */
@SuppressWarnings({"serial", "unchecked"})
public class Address  implements java.io.Serializable {

     private int addressId;
     private String addressLine1;
     private String addressLine2;
     private String city;
     private String stateProvince;
     private String countryRegion;
     private String postalCode;
     private String rowguid;
     private Date modifiedDate;

     private Set salesOrderHeadersForShipToAddressId = new HashSet(0);
     private Set salesOrderHeadersForBillToAddressId = new HashSet(0);
     private Set customerAddresses = new HashSet(0);

    public Address() {
    }

	
    public Address(int addressId, String addressLine1, String city, String stateProvince, String countryRegion, String postalCode, String rowguid, Date modifiedDate) {
        this.addressId = addressId;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.stateProvince = stateProvince;
        this.countryRegion = countryRegion;
        this.postalCode = postalCode;
        this.rowguid = rowguid;
        this.modifiedDate = modifiedDate;
    }
    public Address(int addressId, String addressLine1, String addressLine2, String city, String stateProvince, String countryRegion, String postalCode, String rowguid, Date modifiedDate, Set salesOrderHeadersForShipToAddressId, Set salesOrderHeadersForBillToAddressId, Set customerAddresses) {
       this.addressId = addressId;
       this.addressLine1 = addressLine1;
       this.addressLine2 = addressLine2;
       this.city = city;
       this.stateProvince = stateProvince;
       this.countryRegion = countryRegion;
       this.postalCode = postalCode;
       this.rowguid = rowguid;
       this.modifiedDate = modifiedDate;
       this.salesOrderHeadersForShipToAddressId = salesOrderHeadersForShipToAddressId;
       this.salesOrderHeadersForBillToAddressId = salesOrderHeadersForBillToAddressId;
       this.customerAddresses = customerAddresses;
    }
   
    public int getAddressId() {
        return this.addressId;
    }
    
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }
    public String getAddressLine1() {
        return this.addressLine1;
    }
    
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }
    public String getAddressLine2() {
        return this.addressLine2;
    }
    
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }
    public String getCity() {
        return this.city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    public String getStateProvince() {
        return this.stateProvince;
    }
    
    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }
    public String getCountryRegion() {
        return this.countryRegion;
    }
    
    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }
    public String getPostalCode() {
        return this.postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    public String getRowguid() {
        return this.rowguid;
    }
    
    public void setRowguid(String rowguid) {
        this.rowguid = rowguid;
    }
    public Date getModifiedDate() {
        return this.modifiedDate;
    }
    
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    public Set getSalesOrderHeadersForShipToAddressId() {
        return this.salesOrderHeadersForShipToAddressId;
    }
    
    public void setSalesOrderHeadersForShipToAddressId(Set salesOrderHeadersForShipToAddressId) {
        this.salesOrderHeadersForShipToAddressId = salesOrderHeadersForShipToAddressId;
    }
    public Set getSalesOrderHeadersForBillToAddressId() {
        return this.salesOrderHeadersForBillToAddressId;
    }
    
    public void setSalesOrderHeadersForBillToAddressId(Set salesOrderHeadersForBillToAddressId) {
        this.salesOrderHeadersForBillToAddressId = salesOrderHeadersForBillToAddressId;
    }
    public Set getCustomerAddresses() {
        return this.customerAddresses;
    }
    
    public void setCustomerAddresses(Set customerAddresses) {
        this.customerAddresses = customerAddresses;
    }




}


