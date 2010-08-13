/*
 *  Copyright (C) 2008-2010 WaveMaker Software, Inc.
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
package com.wavemaker.tools.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.SecurityConfig;
import org.acegisecurity.intercept.web.FilterInvocationDefinitionSourceEditor;
import org.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.memory.UserMap;
import org.acegisecurity.userdetails.memory.UserMapEditor;

import com.wavemaker.common.util.CastUtils;
import com.wavemaker.common.util.SystemUtils;
import com.wavemaker.tools.common.ConfigurationException;
import com.wavemaker.tools.spring.beans.Bean;
import com.wavemaker.tools.spring.beans.Beans;
import com.wavemaker.tools.spring.beans.ConstructorArg;
import com.wavemaker.tools.spring.beans.Property;
import com.wavemaker.tools.spring.beans.Ref;
import com.wavemaker.tools.spring.beans.Value;

/**
 * @author ffu
 * @version $Rev$ - $Date$
 *
 */
public class SecuritySpringSupport {
    
    static final String ROLE_PREFIX = "ROLE_";
    
    private static final String DEFAULT_NO_ROLES_ROLE = "DEFAULT_NO_ROLES";

    private static final String FILTER_SECURITY_INTERCEPTOR_BEAN_ID = "filterSecurityInterceptor";

    private static final String OBJECT_DEFINITION_SOURCE_PROPERTY = "objectDefinitionSource";

    private static final String AUTHENTICATON_MANAGER_BEAN_ID = "authenticationManager";

    private static final String AUTH_PROVIDERS_PROPERTY = "providers";

    private static final String ANONYMOUS_AUTHENTICATION_PROVIDER_BEAN_ID = "anonymousAuthenticationProvider";

    private static final String DAO_AUTHENTICATION_PROVIDER_BEAN_ID = "daoAuthenticationProvider";

    static final String LDAP_AUTH_PROVIDER_BEAN_ID = "ldapAuthProvider";

    private static final String USER_DETAILS_SERVICE_PROPERTY = "userDetailsService";

    static final String IN_MEMORY_DAO_IMPL_BEAN_ID = "inMemoryDaoImpl";

    private static final String USER_MAP_PROPERTY = "userMap";

    static final String JDBC_DAO_IMPL_BEAN_ID = "jdbcDaoImpl";

    private static final String JDBC_DAO_IMPL_BEAN_CLASSNAME = "com.wavemaker.runtime.security.EnhancedJdbcDaoImpl";
    
    private static final String DATA_SOURCE_PROPERTY = "dataSource";

    private static final String DEFAULT_DATA_SOURCE_BEAN_ID = "jdbcDataSource";

    private static final String USERS_BY_USERNAME_QUERY_PROPERTY = "usersByUsernameQuery";

    private static final String AUTHORITIES_BY_USERNAME_QUERY_PROPERTY = "authoritiesByUsernameQuery";

    private static final String AUTHORITIES_BY_USERNAME_QUERY_PARAM_TYPE_PROPERTY = "authoritiesByUsernameQueryParamType";

    private static final String TABLE_MARKER = "table";

    private static final String UNAME_COLUMN_MARKER = "_unameColumn_";
    
    private static final String UID_COLUMN_MARKER = "_uidColumn_";

    private static final String PW_COLUMN_MARKER = "_pwColumn_";
    
    private static final String ROLE_COLUMN_MARKER = "_roleColumn_";

    private static final String USERS_BY_USERNAME_QUERY = "SELECT "
            + UID_COLUMN_MARKER + ", " + PW_COLUMN_MARKER + ", 1, " + UNAME_COLUMN_MARKER
            + " FROM " + TABLE_MARKER + " WHERE "
            + UNAME_COLUMN_MARKER + " = ?";

    private static final String AUTHORITIES_BY_USERNAME_QUERY_PREFIX = "SELECT "
            + UID_COLUMN_MARKER + ", ";

    private static final String AUTHORITIES_BY_USERNAME_QUERY_SUFFIX = " FROM "
            + TABLE_MARKER + " WHERE " + UID_COLUMN_MARKER + " = ?";

    private static final String AUTHORITIES_BY_USERNAME_QUERY = AUTHORITIES_BY_USERNAME_QUERY_PREFIX
            + ROLE_COLUMN_MARKER + AUTHORITIES_BY_USERNAME_QUERY_SUFFIX;

    private static final String LDAP_DIR_CONTEXT_FACTORY_BEAN_ID = "initialDirContextFactory";

    private static final String LDAP_BIND_AUTHENTICATOR_CLASSNAME = 
        "org.acegisecurity.providers.ldap.authenticator.BindAuthenticator";
    
    private static final String LDAP_AUTHORITIES_POPULATOR_CLASSNAME = 
        "com.wavemaker.runtime.security.LdapAuthoritiesPopulator";

    private static final String LDAP_MANAGER_DN_PROPERTY = "managerDn";

    private static final String LDAP_MANAGER_PASSWORD_PROPERTY = "managerPassword";

    private static final String LDAP_USERDN_PATTERNS_PROPERTY = "userDnPatterns";

    private static final String LDAP_GROUP_SEARCHING_DISABLED = "groupSearchDisabled";

    private static final String LDAP_GROUP_ROLE_ATTRIBUTE = "groupRoleAttribute";
    
    private static final String LDAP_GROUP_SEARCH_FILTER = "groupSearchFilter";
    
    private static final String SPACES_12 = "            ";
    
    private static final String SPACES_16 = "                ";
    
    private static final String OBJECT_DEFINITION_SOURCE_PREFIX =
        "\n" + 
        SPACES_16 + "CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON\n" + 
        SPACES_16 + "PATTERN_TYPE_APACHE_ANT\n";

    private static final String IS_AUTHENTICATED_ANONYMOUSLY = 
        "IS_AUTHENTICATED_ANONYMOUSLY";

    private static final String IS_AUTHENTICATED_FULLY = 
        "IS_AUTHENTICATED_FULLY";

    private static final String UNPROTECTED_OBJECT_DEFINITION_SOURCE_SUFFIX =
        "\n" + SPACES_12;

    private static final String SECURITY_SERVICE = "securityService";
    
    private static final String ROLES = "roles";

    private static List<String> getSecurityResourceAttrs(Beans beans, String url) {
        Bean bean = beans.getBeanById(FILTER_SECURITY_INTERCEPTOR_BEAN_ID);
        FilterInvocationDefinitionSourceEditor e = new FilterInvocationDefinitionSourceEditor();
        e.setAsText(getPropertyValueString(bean,
                OBJECT_DEFINITION_SOURCE_PROPERTY));
        PathBasedFilterInvocationDefinitionMap map = 
            (PathBasedFilterInvocationDefinitionMap) e.getValue();
        ConfigAttributeDefinition attrs = map.lookupAttributes(url);
        List<String> authzList = new ArrayList<String>();
        if (attrs != null) {
            Iterator<SecurityConfig> iter = CastUtils.cast(attrs
                    .getConfigAttributes());
            while (iter.hasNext()) {
                authzList.add(iter.next().getAttribute());
            }
        }
        return authzList;
    }

    static boolean isSecurityEnforced(Beans beans) {
        return !getSecurityResourceAttrs(beans, "/index.html").isEmpty();
    }

    static boolean isIndexHtmlEnforced(Beans beans) {
        return getSecurityResourceAttrs(beans, "/index.html").contains(
                IS_AUTHENTICATED_FULLY);
    }

    static void setSecurityResources(Beans beans, boolean enforceSecurity,
            boolean enforceIndexHtml) {
        Bean bean = beans.getBeanById(FILTER_SECURITY_INTERCEPTOR_BEAN_ID);
        Property property = bean.getProperty(OBJECT_DEFINITION_SOURCE_PROPERTY);
        List<String> newContent = new ArrayList<String>();
        String value = null;
        Map<String, List<String>> urlMap = new LinkedHashMap<String, List<String>>();
        if (enforceSecurity) {
            String indexHtmlAuthz = null;
            if (enforceIndexHtml) {
                indexHtmlAuthz = IS_AUTHENTICATED_FULLY;
            } else {
                indexHtmlAuthz = IS_AUTHENTICATED_ANONYMOUSLY;
            }
            urlMap.put("/index.html", Arrays.asList(
                    (new String[] { indexHtmlAuthz })));
            urlMap.put("/", Arrays.asList(
                    (new String[] { indexHtmlAuthz })));
            urlMap.put("/securityservice.json", Arrays.asList(
                    (new String[] { IS_AUTHENTICATED_ANONYMOUSLY })));
            urlMap.put("/*.json", Arrays.asList(
                    (new String[] { IS_AUTHENTICATED_FULLY })));
            urlMap.put("/*.download", Arrays.asList(
                    (new String[] { IS_AUTHENTICATED_FULLY })));
            urlMap.put("/*.upload", Arrays.asList(
                    (new String[] { IS_AUTHENTICATED_FULLY })));
        }
        value = generateObjectDefinitionSource(enforceSecurity, urlMap);
        newContent.add(value);
        property.getValueElement().setContent(newContent);
    }

    private static String generateObjectDefinitionSource(
            boolean securityEnabled, Map<String, List<String>> urlMap) {
        StringBuilder objectDefSource = new StringBuilder();
        objectDefSource.append(OBJECT_DEFINITION_SOURCE_PREFIX);
        if (securityEnabled) {
            for (String url : urlMap.keySet()) {
                objectDefSource.append(SPACES_16);
                objectDefSource.append(url);
                objectDefSource.append("=");
                List<String> authzList = urlMap.get(url);
                if (authzList.size() > 0) {
                    objectDefSource.append(authzList.get(0));
                    for (int i = 1; i < authzList.size(); i++) {
                        objectDefSource.append(",");
                        objectDefSource.append(authzList.get(i));
                    }
                }
                objectDefSource.append("\n");
            }
            objectDefSource.append(SPACES_12);
        } else {
            objectDefSource.append(UNPROTECTED_OBJECT_DEFINITION_SOURCE_SUFFIX);
        }
        return objectDefSource.toString();
    }

    private static String[] getAuthManagerProviderBeanIds(Beans beans) {
        Bean bean = beans.getBeanById(AUTHENTICATON_MANAGER_BEAN_ID);
        Property property = bean.getProperty(AUTH_PROVIDERS_PROPERTY);
        com.wavemaker.tools.spring.beans.List list = property.getList();
        List<Object> refElements = list.getRefElement();
        String[] pbids = new String[refElements.size()];
        for (int i = 0; i < refElements.size(); i++) {
            Ref ref = (Ref) refElements.get(i);
            pbids[i] = ref.getBean();
        }
        return pbids;
    }

    static void setAuthManagerProviderBeanId(Beans beans, String beanId) {
        Bean bean = beans.getBeanById(AUTHENTICATON_MANAGER_BEAN_ID);
        Property property = bean.getProperty(AUTH_PROVIDERS_PROPERTY);
        com.wavemaker.tools.spring.beans.List list = new com.wavemaker.tools.spring.beans.List();
        List<Object> refElements = new ArrayList<Object>();
        Ref ref = new Ref();
        ref.setBean(beanId);
        refElements.add(ref);
        
        // always add the Anonymous provider 
        Ref anonyRef = new Ref();
        anonyRef.setBean(ANONYMOUS_AUTHENTICATION_PROVIDER_BEAN_ID);
        refElements.add(anonyRef);
        
        list.setRefElement(refElements);
        property.setList(list);
    }

    static String getDataSourceType(Beans beans) {
        String[] authProviderBeanIds = getAuthManagerProviderBeanIds(beans);
        for (String authProviderBeanId : authProviderBeanIds) {
            if (authProviderBeanId.equals(DAO_AUTHENTICATION_PROVIDER_BEAN_ID)) {
                Bean bean = beans
                        .getBeanById(DAO_AUTHENTICATION_PROVIDER_BEAN_ID);
                Property property = bean
                        .getProperty(USER_DETAILS_SERVICE_PROPERTY);
                Ref refElement = property.getRefElement();
                String beanId = refElement.getBean();
                if (beanId.equals(IN_MEMORY_DAO_IMPL_BEAN_ID)) {
                    return GeneralOptions.DEMO_TYPE;
                } else if (beanId.equals(JDBC_DAO_IMPL_BEAN_ID)) {
                    return GeneralOptions.DATABASE_TYPE;
                } else {
                    throw new ConfigurationException("Unrecognized bean Id "
                            + beanId + " for userDetailsService property.");
                }
            } else if (authProviderBeanId.equals(LDAP_AUTH_PROVIDER_BEAN_ID)) {
                return GeneralOptions.LDAP_TYPE;
            }
        }
        throw new ConfigurationException("Unable to get data source type!");
    }

    static void updateAuthProviderUserDetailsService(Beans beans, String refId) {
        setAuthManagerProviderBeanId(beans, DAO_AUTHENTICATION_PROVIDER_BEAN_ID);
        Bean bean = beans.getBeanById(DAO_AUTHENTICATION_PROVIDER_BEAN_ID);
        Property property = bean.getProperty(USER_DETAILS_SERVICE_PROPERTY);
        Ref ref = new Ref();
        ref.setBean(refId);
        property.setRefElement(ref);
    }

    static List<DemoUser> getDemoUsers(Beans beans) {
        Bean bean = beans.getBeanById(IN_MEMORY_DAO_IMPL_BEAN_ID);
        Property property = bean.getProperty(USER_MAP_PROPERTY);
        Value valueElement = property.getValueElement();
        List<String> content = valueElement.getContent();
        List<DemoUser> demoUsers = new ArrayList<DemoUser>();
        if (content.size() == 1) {
            String value = content.get(0);
            value = value.trim();
            
            // user the property editor to parse the string value
            UserMapEditor e = new UserMapEditor();
            e.setAsText(value);
            UserMap userMap = (UserMap) e.getValue();
            
            String[] userStringArray = value.split("\n");
            for (String userString : userStringArray) {
                userString = userString.trim();
                int i = userString.indexOf('=');
                if (i > 0) {
                    String userid = userString.substring(0, i);
                    UserDetails user = userMap.getUser(userid);
                    DemoUser demoUser = new DemoUser();
                    demoUser.setUserid(userid);
                    demoUser.setPassword(user.getPassword());
                    GrantedAuthority[] authorities = user.getAuthorities();
                    List<String> userRoles = new ArrayList<String>();
                    for (GrantedAuthority authority : authorities) {
                        String role = authority.getAuthority();
                        if (role.startsWith(ROLE_PREFIX)) {
                            String realRole = role.substring(ROLE_PREFIX.length());
                            if (!realRole.equals(DEFAULT_NO_ROLES_ROLE)) {
                                userRoles.add(realRole);
                            }
                        } else {
                            SecurityToolsManager.logger.warn("Skipping Role "
                                    + role + ". It should be prefix with "
                                    + ROLE_PREFIX + ". Something is wrong!");
                        }
                    }
                    demoUser.setRoles(userRoles);
                    demoUsers.add(demoUser);
                }
            }
        }
        return demoUsers;
    }

    static void setDemoUsers(Beans beans, DemoUser[] demoUsers) {
        Bean bean = beans.getBeanById(IN_MEMORY_DAO_IMPL_BEAN_ID);
        StringBuilder sb = new StringBuilder();
        for (DemoUser demoUser : demoUsers) {
            sb.append("\n");
            sb.append(SPACES_16);
            sb.append(demoUser.getUserid());
            sb.append("=");
            sb.append( demoUser.getPassword());
            List<String> roles = demoUser.getRoles();
            if (roles.isEmpty()) {
                sb.append(",");
                sb.append(ROLE_PREFIX);
                sb.append(DEFAULT_NO_ROLES_ROLE);
            } else {
                for (String role : roles) {
                    sb.append(",");
                    sb.append(ROLE_PREFIX);
                    sb.append(role);
                }
            }
        }
        sb.append("\n");
        sb.append(SPACES_12);
        
        setPropertyValueString(bean, USER_MAP_PROPERTY, sb.toString());
    }

    static DatabaseOptions constructDatabaseOptions(Beans beans) {
        DatabaseOptions options = new DatabaseOptions();
        Bean jdbcDaoBean = beans.getBeanById(JDBC_DAO_IMPL_BEAN_ID);

        Property property = jdbcDaoBean.getProperty(DATA_SOURCE_PROPERTY);
        String dataSourceBeanId = property.getRefElement().getBean();
        String modelName = dataSourceBeanId.substring(0, 
                dataSourceBeanId.indexOf("DataSource"));
        options.setModelName(modelName);

        String value = getPropertyValueString(jdbcDaoBean, USERS_BY_USERNAME_QUERY_PROPERTY);
        value = value.substring(7).trim();
        String unameColumnName = value.substring(value.indexOf("WHERE") + 6,
                value.indexOf("= ?")).trim();
        String uidColumnName = value.substring(0, value.indexOf(','));
        String pwColumnName = value.substring(uidColumnName.length() + 1,
                value.lastIndexOf(',')).trim();
        String tableNameWithPrefix = value.substring(value.indexOf("FROM") + 4,
                value.indexOf("WHERE")).trim();
        String tableName = null;
        int tableNameIndex = tableNameWithPrefix.indexOf('.');
        if (tableNameIndex > -1) {
            tableName = tableNameWithPrefix.substring(tableNameIndex + 1);
        } else {
            tableName = tableNameWithPrefix;
        }
        options.setTableName(tableName);
        options.setUnameColumnName(unameColumnName);
        options.setUidColumnName(uidColumnName);
        options.setPwColumnName(pwColumnName);
        
        value = getPropertyValueString(jdbcDaoBean, AUTHORITIES_BY_USERNAME_QUERY_PROPERTY);
        options.setRolesByUsernameQuery(value);
        String authQueryPrefix = AUTHORITIES_BY_USERNAME_QUERY_PREFIX.replaceAll(UID_COLUMN_MARKER, uidColumnName);
        String authQuerySuffix = AUTHORITIES_BY_USERNAME_QUERY_SUFFIX.replaceAll(TABLE_MARKER, tableNameWithPrefix);
        authQuerySuffix = authQuerySuffix.replaceAll(UID_COLUMN_MARKER, uidColumnName);
        if (value.indexOf(authQueryPrefix) != -1 && value.indexOf(authQuerySuffix) != -1) {
            // the query most likely not a custom query
            String roleColumnName = value.substring(value.indexOf(',') + 1,
                    value.indexOf("FROM")).trim();
            options.setUseRolesQuery(false);
            if (!roleColumnName.equals("'" + DEFAULT_NO_ROLES_ROLE + "'")) {
                options.setRoleColumnName(roleColumnName);
            }
        } else {
            options.setUseRolesQuery(true);
        }
        
        return options;
    }

    static void updateJdbcDaoImpl(Beans beans, String modelName,
            String tableName, String unameColumnName, String uidColumnName,
            String uidColumnSqlType, String pwColumnName, String roleColumnName,
            String rolesByUsernameQuery) {
        Bean jdbcDaoBean = beans.getBeanById(JDBC_DAO_IMPL_BEAN_ID);
        String clazz = jdbcDaoBean.getClazz();
        if (clazz == null || !clazz.equals(JDBC_DAO_IMPL_BEAN_CLASSNAME)) {
            jdbcDaoBean.setClazz(JDBC_DAO_IMPL_BEAN_CLASSNAME);
        }
        Property property = jdbcDaoBean.getProperty(DATA_SOURCE_PROPERTY);
        if (property == null) {
            property = new Property();
            property.setName(DATA_SOURCE_PROPERTY);
        }
        Ref ref = new Ref();
        // TODO: should get this from DataService
        ref.setBean(modelName + "DataSource");
        property.setRefElement(ref);

        setPropertyValueString(jdbcDaoBean, USERS_BY_USERNAME_QUERY_PROPERTY,
                buildUsersByUsernameQuery(tableName, unameColumnName,
                        uidColumnName, pwColumnName));

        if (rolesByUsernameQuery != null && rolesByUsernameQuery.length() != 0) {
            setPropertyValueString(jdbcDaoBean,
                    AUTHORITIES_BY_USERNAME_QUERY_PROPERTY,
                    rolesByUsernameQuery);
        } else {
            setPropertyValueString(jdbcDaoBean,
                    AUTHORITIES_BY_USERNAME_QUERY_PROPERTY,
                    buildAuthoritiesByUsernameQuery(tableName, unameColumnName,
                            uidColumnName, roleColumnName));
        }

        setPropertyValueString(jdbcDaoBean, 
                AUTHORITIES_BY_USERNAME_QUERY_PARAM_TYPE_PROPERTY,
                uidColumnSqlType);
    }

    static void resetJdbcDaoImpl(Beans beans) {
        Bean jdbcDaoBean = beans.getBeanById(JDBC_DAO_IMPL_BEAN_ID);
        Property property = jdbcDaoBean.getProperty(DATA_SOURCE_PROPERTY);
        if (property == null) {
            property = new Property();
            property.setName(DATA_SOURCE_PROPERTY);
        }
        Ref ref = new Ref();
        ref.setBean(DEFAULT_DATA_SOURCE_BEAN_ID);
        property.setRefElement(ref);
    }

    private static String buildUsersByUsernameQuery(String tableName,
            String unameColumnName, String uidColumnName, String pwColumnName) {
        String queryString = USERS_BY_USERNAME_QUERY;
        queryString = queryString.replaceAll(TABLE_MARKER, tableName);
        queryString = queryString.replaceAll(UNAME_COLUMN_MARKER, unameColumnName);
        queryString = queryString.replaceAll(UID_COLUMN_MARKER, uidColumnName);
        queryString = queryString.replaceAll(PW_COLUMN_MARKER, pwColumnName);
        return queryString;
    }

    private static String buildAuthoritiesByUsernameQuery(String tableName,
            String unameColumnName, String uidColumnName, String roleColumnName) {
        String queryString = AUTHORITIES_BY_USERNAME_QUERY;
        queryString = queryString.replaceAll(TABLE_MARKER, tableName);
        queryString = queryString.replaceAll(UID_COLUMN_MARKER, uidColumnName);
        if (roleColumnName == null || roleColumnName.length() == 0) {
            roleColumnName = "'" + DEFAULT_NO_ROLES_ROLE + "'";
        }
        queryString = queryString.replaceAll(ROLE_COLUMN_MARKER, roleColumnName);
        return queryString;
    }

    static LDAPOptions constructLDAPOptions(Beans beans) {
        LDAPOptions options = new LDAPOptions();
        Bean ldapDirContextBean = beans
                .getBeanById(LDAP_DIR_CONTEXT_FACTORY_BEAN_ID);

        ConstructorArg arg = ldapDirContextBean.getConstructorArgs().get(0);
        String ldapUrl = arg.getValue();
        options.setLdapUrl(ldapUrl);

        options.setManagerDn(getPropertyValueString(ldapDirContextBean,
                LDAP_MANAGER_DN_PROPERTY));
        options.setManagerPassword(getPropertyValueString(ldapDirContextBean,
                LDAP_MANAGER_PASSWORD_PROPERTY));
        
        Bean ldapAuthProviderBean = beans
                .getBeanById(LDAP_AUTH_PROVIDER_BEAN_ID);
        List<ConstructorArg> constructorArgs = ldapAuthProviderBean
                .getConstructorArgs();
        for (ConstructorArg constructorArg : constructorArgs) {
            if (constructorArg.getBean().getClazz().equals(
                    LDAP_BIND_AUTHENTICATOR_CLASSNAME)) {
                Bean bindAuthBean = constructorArg.getBean();
                Property userDnPatternsProperty = bindAuthBean
                        .getProperty(LDAP_USERDN_PATTERNS_PROPERTY);
                Value v = (Value) userDnPatternsProperty.getList()
                        .getRefElement().get(0);
                String userDnPattern = v.getContent().get(0);
                options.setUserDnPattern(userDnPattern);
            } else if (constructorArg.getBean().getClazz().equals(
                    LDAP_AUTHORITIES_POPULATOR_CLASSNAME)) {
                Bean authzBean = constructorArg.getBean();
                boolean isGroupSearchDisabled = Boolean.parseBoolean(
                        getPropertyValueString(authzBean,
                                LDAP_GROUP_SEARCHING_DISABLED));
                options.setGroupSearchDisabled(isGroupSearchDisabled);
                if (!isGroupSearchDisabled) {
                    List<ConstructorArg> authzArgs = authzBean.getConstructorArgs();
                    if (authzArgs.size() > 1) {
                        Value valueElement = authzArgs.get(1).getValueElement();
                        if (valueElement != null
                                && valueElement.getContent() != null 
                                && !valueElement.getContent().isEmpty()) {
                            String groupSearchBase = valueElement.getContent().get(0);
                            options.setGroupSearchBase(groupSearchBase);
                        }
                    }
                    options.setGroupRoleAttribute(getPropertyValueString(
                            authzBean, LDAP_GROUP_ROLE_ATTRIBUTE));
                    options.setGroupSearchFilter(getPropertyValueString(
                            authzBean, LDAP_GROUP_SEARCH_FILTER));
                }
            }
        }

        return options;
    }

    static void updateLDAPDirContext(Beans beans, String ldapUrl,
            String managerDn, String managerPassword) {
        Bean ldapDirContextBean = beans
                .getBeanById(LDAP_DIR_CONTEXT_FACTORY_BEAN_ID);

        ldapDirContextBean.getConstructorArgs().get(0).setValue(ldapUrl);
        if (managerDn != null && managerDn.length() != 0) {
            setPropertyValueString(ldapDirContextBean,
                    LDAP_MANAGER_DN_PROPERTY, managerDn);
            setPropertyValueString(ldapDirContextBean,
                    LDAP_MANAGER_PASSWORD_PROPERTY, 
                    SystemUtils.encrypt(managerPassword));
        } else {
            List<Object> props = ldapDirContextBean
                    .getMetasAndConstructorArgsAndProperties();
            Property prop = ldapDirContextBean
                    .getProperty(LDAP_MANAGER_DN_PROPERTY);
            if (prop != null) {
                props.remove(prop);
            }
            prop = ldapDirContextBean
                    .getProperty(LDAP_MANAGER_PASSWORD_PROPERTY);
            if (prop != null) {
                props.remove(prop);
            }
        }
    }

    static void updateLDAAuthProvider(Beans beans, String userDnPattern,
            boolean groupSearchDisabled, String groupSearchBase,
            String groupRoleAttribute, String groupSearchFilter) {
        Bean ldapAuthProviderBean = beans
                .getBeanById(LDAP_AUTH_PROVIDER_BEAN_ID);
        List<ConstructorArg> constructorArgs = ldapAuthProviderBean
                .getConstructorArgs();
        for (ConstructorArg constructorArg : constructorArgs) {
            if (constructorArg.getBean().getClazz().equals(
                    LDAP_BIND_AUTHENTICATOR_CLASSNAME)) {
                Bean bindAuthBean = constructorArg.getBean();
                Property userDnPatternsProperty = bindAuthBean
                        .getProperty(LDAP_USERDN_PATTERNS_PROPERTY);
                com.wavemaker.tools.spring.beans.List list = userDnPatternsProperty
                        .getList();
                List<Object> refElements = new ArrayList<Object>();
                Value v = new Value();
                List<String> content = new ArrayList<String>();
                content.add(userDnPattern);
                v.setContent(content);
                refElements.add(v);
                list.setRefElement(refElements);
            } else if (constructorArg.getBean().getClazz().equals(
                    LDAP_AUTHORITIES_POPULATOR_CLASSNAME)) {
                Bean authzBean = constructorArg.getBean();
                List<ConstructorArg> authzArgs = authzBean.getConstructorArgs();
                if (authzArgs.size() > 1) {
                    Value valueElement = authzArgs.get(1).getValueElement();
                    if (valueElement != null) {
                        List<String> content = new ArrayList<String>();
                        content.add(groupSearchBase);
                        valueElement.setContent(content);
                    }
                }
                setPropertyValueString(authzBean,
                        LDAP_GROUP_SEARCHING_DISABLED, 
                        Boolean.toString(groupSearchDisabled));
                setPropertyValueString(authzBean, LDAP_GROUP_ROLE_ATTRIBUTE,
                        groupRoleAttribute);
                setPropertyValueString(authzBean, LDAP_GROUP_SEARCH_FILTER,
                        groupSearchFilter);
            }
        }
    }

    static List<String> getRoles(Beans beans) {
        Bean securityServiceBean = beans.getBeanById(SECURITY_SERVICE);
        Property rolesProperty = securityServiceBean.getProperty(ROLES);
        if (rolesProperty == null || rolesProperty.getList() == null) {
            return Collections.emptyList();
        }
        List<Object> refElements = rolesProperty.getList().getRefElement();
        List<String> roles = new ArrayList<String>(refElements.size());
        for (Object o : refElements) {
            roles.add(((Value)o).getContent().get(0));
        }
        return roles;
    }

    static void setRoles(Beans beans, List<String> roles) {
        Bean securityServiceBean = beans.getBeanById(SECURITY_SERVICE);
        Property rolesProperty = securityServiceBean.getProperty(ROLES);
        com.wavemaker.tools.spring.beans.List list = rolesProperty.getList();
        List<Object> refElements = new ArrayList<Object>();
        for (String role : roles) {
            Value v = new Value();
            List<String> content = new ArrayList<String>();
            content.add(role);
            v.setContent(content);
            refElements.add(v);
        }
        list.setRefElement(refElements);
    }
    
    private static String getPropertyValueString(Bean bean, String propertyName) {
        Property property = bean.getProperty(propertyName);
        if (property == null) {
            return null;
        }
        Value valueElement = property.getValueElement();
        return valueElement.getContent().get(0);
    }
    
    private static void setPropertyValueString(Bean bean, String propertyName,
            String value) {
        Property property = bean.getProperty(propertyName);
        if (property == null) {
            property = new Property();
            property.setName(propertyName);
            bean.addProperty(property);
        }
        Value valueElement = new Value();
        List<String> content = new ArrayList<String>();
        content.add(value);
        valueElement.setContent(content);
        property.setValueElement(valueElement);
    }
}
