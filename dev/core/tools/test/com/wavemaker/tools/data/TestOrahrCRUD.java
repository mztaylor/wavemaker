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
package com.wavemaker.tools.data;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;

import com.wavemaker.common.util.ClassLoaderUtils;
import com.wavemaker.infra.DependentTestFailureException;
import com.wavemaker.infra.TestSpringContextTestCase;
import com.wavemaker.runtime.data.DataServiceRuntimeException;
import com.wavemaker.runtime.data.DataServiceTestConstants;
import com.wavemaker.runtime.data.QueryOptions;
import com.wavemaker.runtime.data.sample.orahr.Employees;
import com.wavemaker.runtime.data.sample.orahr.Jobs;
import com.wavemaker.runtime.data.sample.orahr.Orahr;
import com.wavemaker.runtime.data.util.DataServiceUtils;
import com.wavemaker.runtime.service.ServiceConstants;
import com.wavemaker.runtime.service.ServiceManager;
import com.wavemaker.runtime.service.reflect.ReflectServiceWire;

/**
 * @author stoens
 * @version $Rev$ - $Date$
 * 
 */
public class TestOrahrCRUD extends TestSpringContextTestCase {

    static class TestData {

        private String dependentTestName = null;

        private boolean skipRemaining = false;

        private Orahr orahr = null;

        private Employees newEmployee = null;

        void setService(Orahr orahr) {
            this.orahr = orahr;
        }

        Orahr getOrahr() {
            return orahr;
        }

        void setNewEmployee(Employees newEmployee) {
            this.newEmployee = newEmployee;
        }

        Employees getNewEmployee() {
            return newEmployee;
        }

        void setSkipRemaining(String dependentTestName) {
            this.skipRemaining = true;
            this.dependentTestName = dependentTestName;
        }

        boolean skipRemaining() {
            return skipRemaining;
        }

        String getDependentTestName() {
            return dependentTestName;
        }

    }

    public static Test suite() {

        TestData testData = new TestData();

        TestSuite testsToRun = new TestSuite();

        testsToRun.addTest(new TestOrahrCRUD("testCountEmployees", testData));
        testsToRun.addTest(new TestOrahrCRUD("testScrollableResultSet",
                testData));
        testsToRun.addTest(new TestOrahrCRUD("testAddNewEmployee", testData));
        testsToRun.addTest(new TestOrahrCRUD("testFindNewEmployee", testData));
        testsToRun
                .addTest(new TestOrahrCRUD("testDeleteNewEmployee", testData));
        testsToRun.addTest(new TestOrahrCRUD("testOracleConnection", testData));
        testsToRun
                .addTest(new TestOrahrCRUD("testOracleConnection2", testData));

        return testsToRun;
    }

    protected final TestData testData;

    public TestOrahrCRUD(String testMethod, TestData testData) {
        super(testMethod);
        this.testData = testData;
    }

    protected void checkShouldSkip() {
        if (testData.skipRemaining) {
            throw new DependentTestFailureException(testData
                    .getDependentTestName());
        }
    }

    public void testCountEmployees() {

        try {

            ApplicationContext ctx = getApplicationContext();

            ServiceManager serviceMgr = (ServiceManager) ctx
                    .getBean(ServiceConstants.SERVICE_MANAGER_NAME);

            Orahr orahr = (Orahr) ((ReflectServiceWire) serviceMgr
                    .getServiceWire(DataServiceTestConstants.ORACLE_HR_SERVICE_ID)).getServiceBean();

            assertTrue(orahr.getEmployeesCount(new Employees(),
                    new QueryOptions()) == (long) 107);

            testData.setService(orahr);

        } catch (RuntimeException ex) {
            testData.setSkipRemaining("testCountEmployees");
            throw ex;
        }
    }

    public void testScrollableResultSet() {

        checkShouldSkip();

        Orahr orahr = testData.getOrahr();

        orahr.getDataServiceManager().begin();

        ScrollableResults sr = null;

        try {

            Session session = orahr.getDataServiceManager().getSession();

            Query q = session
                    .createQuery("from Employees where lastName like 'A%'");

            sr = q.scroll();
            sr.last();

            assertEquals(4, sr.getRowNumber() + 1);

        } finally {
            try {
                sr.close();
            } catch (Exception ignore) {
            }
            orahr.getDataServiceManager().rollback();
        }
    }

    public void testAddNewEmployee() {

        checkShouldSkip();

        Jobs job = new Jobs();

        job.setJobId("AC_ACCOUNT");

        Employees mgr = new Employees();
        mgr.setEmployeeId(200);

        Employees e = new Employees();
        e.setEmployeeId(300);
        e.setLastName("NEW-EMP");
        e.setHireDate(new Date());
        e.setEmail("new");
        e.setJobs(job);
        e.setEmployees(mgr);

        try {
            testData.getOrahr().insertEmployees(e);
        } catch (RuntimeException ex) {
            testData.setSkipRemaining("testAddNewEmployee");
            throw ex;
        }
    }

    public void testFindNewEmployee() {

        checkShouldSkip();

        Employees qbe = new Employees();

        qbe.setLastName("NEW-EMP");
        qbe.setEmail("new");

        try {
            List<Employees> l = testData.getOrahr().getEmployeesList(qbe,
                    new QueryOptions());

            assertTrue(l.size() == 1);

            assertTrue(l.get(0).getLastName().equals("NEW-EMP"));

            testData.setNewEmployee(l.get(0));

        } catch (RuntimeException ex) {
            testData.setSkipRemaining("testFindNewEmployee");
            throw ex;
        }

    }

    public void testDeleteNewEmployee() {

        checkShouldSkip();

        testData.getOrahr().deleteEmployees(testData.getNewEmployee());

    }

    public void testOracleConnection() throws IOException {

        File props = ClassLoaderUtils
                .getClasspathFile("oracle_orahr.properties");

        Properties p = DataServiceUtils.loadDBProperties(props);

        TestDBConnection t = new TestDBConnection();
        t.setProperties(p);
        t.run();
    }

    public void testOracleConnection2() throws Exception {

        File props = ClassLoaderUtils
                .getClasspathFile("oracle_orahr.properties");

        Properties p = DataServiceUtils.loadDBProperties(props);

        TestDBConnection t = new TestDBConnection();
        t.setProperties(p);
        t.setUsername("___foo");
        try {
            t.run();
        } catch (DataServiceRuntimeException ex) {
            Throwable cause = ex.getCause();
            assertTrue(cause.getMessage().startsWith(
                    "ORA-01017: invalid username/password"));
            return;
        }

        fail("connection should have failed");
    }

}
