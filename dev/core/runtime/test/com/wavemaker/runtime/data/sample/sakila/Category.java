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
package com.wavemaker.runtime.data.sample.sakila;
// Generated Jul 5, 2007 6:21:54 PM by Hibernate Tools 3.2.0.b9


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Category generated by hbm2java
 */
@SuppressWarnings({"serial", "unchecked"})
public class Category  implements java.io.Serializable {


     private Byte categoryId;
     private String name;
     private Date lastUpdate;
     private Set filmCategories = new HashSet(0);

    public Category() {
    }

	
    public Category(String name, Date lastUpdate) {
        this.name = name;
        this.lastUpdate = lastUpdate;
    }
    public Category(String name, Date lastUpdate, Set filmCategories) {
       this.name = name;
       this.lastUpdate = lastUpdate;
       this.filmCategories = filmCategories;
    }
   
    public Byte getCategoryId() {
        return this.categoryId;
    }
    
    public void setCategoryId(Byte categoryId) {
        this.categoryId = categoryId;
    }
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public Date getLastUpdate() {
        return this.lastUpdate;
    }
    
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    public Set getFilmCategories() {
        return this.filmCategories;
    }
    
    public void setFilmCategories(Set filmCategories) {
        this.filmCategories = filmCategories;
    }




}


