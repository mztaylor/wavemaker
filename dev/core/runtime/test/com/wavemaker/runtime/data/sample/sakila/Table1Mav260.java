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
// Generated Jan 9, 2008 4:21:59 PM by Hibernate Tools 3.2.0.CR1


import java.util.HashSet;
import java.util.Set;

/**
 * Table1Mav260 generated by hbm2java
 */
@SuppressWarnings({"serial", "unchecked"})
public class Table1Mav260  implements java.io.Serializable {


     private Table1Mav260Id id;
     private Set table2Mav260sForFk2 = new HashSet(0);
     private Set table2Mav260sForFk = new HashSet(0);

    public Table1Mav260() {
    }

	
    public Table1Mav260(Table1Mav260Id id) {
        this.id = id;
    }
    public Table1Mav260(Table1Mav260Id id, Set table2Mav260sForFk2, Set table2Mav260sForFk) {
       this.id = id;
       this.table2Mav260sForFk2 = table2Mav260sForFk2;
       this.table2Mav260sForFk = table2Mav260sForFk;
    }
   
    public Table1Mav260Id getId() {
        return this.id;
    }
    
    public void setId(Table1Mav260Id id) {
        this.id = id;
    }
    public Set getTable2Mav260sForFk2() {
        return this.table2Mav260sForFk2;
    }
    
    public void setTable2Mav260sForFk2(Set table2Mav260sForFk2) {
        this.table2Mav260sForFk2 = table2Mav260sForFk2;
    }
    public Set getTable2Mav260sForFk() {
        return this.table2Mav260sForFk;
    }
    
    public void setTable2Mav260sForFk(Set table2Mav260sForFk) {
        this.table2Mav260sForFk = table2Mav260sForFk;
    }




}


