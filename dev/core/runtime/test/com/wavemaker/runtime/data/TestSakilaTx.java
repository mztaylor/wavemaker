/*
 *  Copyright (C) 2007-2009 WaveMaker Software, Inc.
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
package com.wavemaker.runtime.data;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.SessionException;
import org.springframework.context.ApplicationContext;

import com.wavemaker.infra.DependentTestFailureException;
import com.wavemaker.runtime.data.sample.sakila.Actor;
import com.wavemaker.runtime.data.sample.sakila.Sakila;
import com.wavemaker.runtime.service.ServiceConstants;
import com.wavemaker.runtime.service.ServiceManager;
import com.wavemaker.runtime.service.reflect.ReflectServiceWire;

/**
 * @author Simon Toens
 * @version $Rev$ - $Date$
 * 
 */
public class TestSakilaTx extends RuntimeDataSpringContextTestCase {

    static class TestData {

        Sakila sakila;

        boolean skipRemaining;

        String dependentTestName;

        void setSkipRemaining(String dependentTestName) {
            this.skipRemaining = true;
            this.dependentTestName = dependentTestName;
        }
    }

    public static Test suite() {

        TestData testData = new TestData();

        TestSuite testsToRun = new TestSuite();

        testsToRun.addTest(new TestSakilaTx("testInitService", testData));
        testsToRun.addTest(new TestSakilaTx("testCommitNoTx", testData));
        testsToRun.addTest(new TestSakilaTx("testGetSessionTx", testData));
        testsToRun.addTest(new TestSakilaTx("testGetSessionNoTx", testData));

        return testsToRun;
    }

    private final TestData testData;

    public TestSakilaTx(String testMethod, TestData testData) {
        super(testMethod);
        this.testData = testData;
    }

    protected void checkShouldSkip() {
        if (testData.skipRemaining) {
            throw new DependentTestFailureException(testData.dependentTestName);
        }
    }

    public void testInitService() {

        try {

            ApplicationContext ctx = getApplicationContext();

            ServiceManager serviceMgr = (ServiceManager) ctx
                    .getBean(ServiceConstants.SERVICE_MANAGER_NAME);

            Sakila sakila = (Sakila) ((ReflectServiceWire) serviceMgr
                    .getServiceWire(DataServiceTestConstants.SAKILA_SERVICE_SPRING_ID_2)).getServiceBean();

            sakila.getActorById(Short.valueOf("1")); // test connection

            testData.sakila = sakila;

        } catch (RuntimeException ex) {
            testData.setSkipRemaining("testInitService");
            throw ex;
        }
    }

    public void testCommitNoTx() {

        checkShouldSkip();

        testData.sakila.getDataServiceManager().commit();
    }

    public void testGetSessionTx() {

        checkShouldSkip();

        testData.sakila.begin();
        testData.sakila.getDataServiceManager().getSession().get(Actor.class,
                Short.valueOf("1"));
        testData.sakila.rollback();
    }

    public void testGetSessionNoTx() {

        checkShouldSkip();

        try {
        testData.sakila.getDataServiceManager().getSession().get(Actor.class,
                Short.valueOf("1"));
        } catch (SessionException ex) {
            return;
        }
        fail("Expected SessionException to be thrown");
    }
}
