/*
 *  Copyright (C) 2008-2009 WaveMaker Software, Inc.
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
package com.wavemaker.runtime.data.sample;

import java.util.List;
import java.util.ArrayList;
import junit.framework.TestCase;

import com.wavemaker.runtime.data.sample.Product;
import com.wavemaker.runtime.data.sample.ProductManager;

public class TestProductManager extends TestCase {

    private ProductManager pm;

    public void setUp() {
        pm = new ProductManager();
        Product p = new Product();
        p.setDescription("Chair");
        p.setPrice(new Double("20.50"));
        ArrayList<Product> al = new ArrayList<Product>();
        al.add(p);
        p = new Product();
        p.setDescription("Table");
        p.setPrice(new Double("150.10"));
        al.add(p);
        pm.setProducts(al);
    }

    public void testGetProducs() {
        List<Product> l = pm.getProducts();
        Product p1 = (Product) l.get(0);
        assertEquals("Chair", p1.getDescription());
        Product p2 = (Product) l.get(1);
        assertEquals("Table", p2.getDescription());
    }

    public void testIncreasePrice() {
        pm.increasePrice(10);
        List<Product> l = pm.getProducts();
        Product p = (Product) l.get(0);
        assertEquals(new Double("22.55"), p.getPrice());
        p = (Product) l.get(1);
        assertEquals(new Double("165.11"), p.getPrice());
    }

}