/*
 *  Copyright (C) 2007-2010 WaveMaker Software, Inc.
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
package com.wavemaker.runtime.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.wavemaker.common.Resource;
import com.wavemaker.common.WMRuntimeException;
import com.wavemaker.common.util.CastUtils;
import com.wavemaker.common.util.SpringUtils;
import com.wavemaker.runtime.RuntimeAccess;

/**
 * The ServiceManager provides access to the system's current services.  As this
 * is a Spring-managed bean, access should be through Spring bean properties or
 * {@link RuntimeAccess}.
 * 
 * @author Matt Small
 * @version $Rev$ - $Date$
 */
public class ServiceManager implements ApplicationContextAware {

    /** Logger for this class and subclasses */
    protected final Logger logger = Logger.getLogger(getClass());
    
    private ApplicationContext applicationContext = null;
    private Set<ServiceType> serviceTypes = new HashSet<ServiceType>();
    private Set<ServiceWire> serviceWires = new HashSet<ServiceWire>();

    /**
     * Return the service bean for the given service id. Note that if the id
     * refers to a ServiceWire without an associated bean, this may return null,
     * or an unrelated bean with the same name.
     * 
     * @param serviceId
     *            The unique service id of the requested service class (this
     *            much match the bean id, as well)
     * 
     * @return the service, or null if the service does not exist
     * @deprecated Use {@link #getServiceWire(String)} instead. This method will
     *             be removed in two releases.
     */
    public Object getService(String serviceId) {

        if (serviceWires == null) {
            SpringUtils.throwSpringNotInitializedError(this.getClass());
        }
        
        if (!getServices().contains(serviceId)) {
            throw new WMRuntimeException(Resource.UNKNOWN_SERVICE, serviceId);
        }

        return applicationContext.getBean(serviceId);
    }

    /**
     * Return the service bean for the given service type. Note that if the id
     * refers to a ServiceWire without an associated bean, this may return null,
     * or an unrelated bean with the same name.
     * 
     * @param serviceType
     *            The class of the service for which to retrieve the service
     *            instance.
     * 
     * @return the service, or null if the service does not exist
     * @deprecated Use {@link #getServiceWire(String)} instead. This method will
     *             be removed in two releases.
     */
    public Object getService(Class<?> serviceType) {

        if (serviceWires == null) {
            SpringUtils.throwSpringNotInitializedError(this.getClass());
        }

        Map<String, Object> m = CastUtils.cast(applicationContext
                .getBeansOfType(serviceType));
        
        if (m.isEmpty()) {
            throw new WMRuntimeException(Resource.UNKNOWN_SERVICE_TYPE,
                    serviceType.getName());
        }
        
        Map<String, Object> foundServices = new HashMap<String, Object>();
        for (Entry<String, Object> entry: m.entrySet()) {
            if (getServices().contains(entry.getKey())) {
                foundServices.put(entry.getKey(), entry.getValue());
            }
        }
        
        if (foundServices.isEmpty() || 0==foundServices.size()) {
            throw new WMRuntimeException(Resource.UNKNOWN_SERVICE_TYPE,
                    serviceType.getName());
        } else if (foundServices.size() > 1) {
            throw new WMRuntimeException(Resource.MULTIPLE_SERVICE_BEANS,
                    serviceType.getName());
        }
        
        Entry<String, Object> entry = foundServices.entrySet().iterator().next();
        return entry.getValue();
    }
    
    public ServiceWire getServiceWire(String serviceId) {
        if (serviceWires == null) {
            SpringUtils.throwSpringNotInitializedError(this.getClass());
        }
        
        ServiceWire ret = null;
        for (ServiceWire sw: serviceWires) {
            if (sw.getServiceId().equals(serviceId)) {
                if (null == ret) {
                    ret = sw;
                } else {
                    throw new WMRuntimeException(Resource.SERVICEWIRE_ID_DUP,
                            serviceId);
                }
            }
        }
        
        return ret;
    }

    public Set<String> getServices() {

        if (serviceWires == null) {
            SpringUtils.throwSpringNotInitializedError(this.getClass());
        }
        
        Set<String> ret = new HashSet<String>();
        
        for (ServiceWire wire: serviceWires) {
            ret.add(wire.getServiceId());
        }
        
        return ret;
    }

    /**
     * Add the new ServiceType only if that service type doesn't already exist.
     * 
     * @param serviceType
     *            The ServiceType to add.
     */
    public void addServiceType(ServiceType serviceType) {

        for (ServiceType st: serviceTypes) {
            if (st.getTypeName().equals(serviceType.getTypeName())) {
                logger.warn("redefining ServiceType "+serviceType.getTypeName());
                return;
            }
        }
        
        logger.info("Adding ServiceType "+serviceType.getTypeName());
        serviceTypes.add(serviceType);
    }
    
    public void addServiceWire(ServiceWire serviceWire) {
        
        if (null==getServiceWire(serviceWire.getServiceId())) {
            logger.info("Adding ServiceWire "+serviceWire);
            serviceWires.add(serviceWire);
        } else {
            logger.warn("Attempt to re-add ServiceWire "+serviceWire);
            serviceWires.add(serviceWire);
        }
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    protected Set<ServiceWire> getServiceWires() {
        return serviceWires;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}