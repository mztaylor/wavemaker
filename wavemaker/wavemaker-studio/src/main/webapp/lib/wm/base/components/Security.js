/*
 *  Copyright (C) 2008-2012 VMware, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

dojo.provide("wm.base.components.Security");
dojo.require("dojo.cookie");

wm.disableUserPrincipalCookie = false;

wm.login = function(args, loginSuccessCallback, loginFailedCallback, properties, projectName) {
	if (properties === undefined || properties == null) {
		properties = {
			j_username : args[0],
			j_password : args[1]
		};
	}

	properties.acegiAjaxLogin = 'true';

	var def= dojo.xhrPost({
		url: (projectName ? '/' + projectName + '/' : '') + 'j_acegi_security_check',
		content : properties,
		handleAs: "json",
	    error: function(response) {
				if (loginFailedCallback) {
				    loginFailedCallback(response.toString());
				} else {
				    app.alert(response.toString());
				}
	    },
		load: function(response, ioArgs) {
			if (response.url) {
                            var pathname = location.protocol + "//" + location.host + location.pathname + location.search; // sometimes using search helps and sometimes it breaks this test; still working out what is going on
				if (dojo.cookie.isSupported() && !wm.disableUserPrincipalCookie) {
					var p = {username: properties.j_username, roles: wm.getUserRoles(true)};
					wm.setUserPrincipal(p);
				} else {
				    wm.roles = wm.getUserRoles(true);
				}
			    dojo.publish("wmRbacUpdate");
				if (loginSuccessCallback) {
					loginSuccessCallback(response.url);
				} else {
				    if (window.studio) {
					// studio.application is set to null on projectClose; studio.project seems to retain value
					if (studio.application && studio.page && studio.project) {
					    if (studio.studioService.requestSync("openProject", [studio.project.projectName]));
					    studio.navGotoDesignerClick();
					} else 
					    studio.startLayer.activate();
                                        // Typically this tests to see if we're on login.html and being directed to index.html
				    } else if (pathname != response.url) {
					location.href = response.url;

                                        // If the page name is login, but app.main is not login, then we're on the real project,
                                        // not the special project used for logging in.  If we're on the real project, a wm page nav is all that is needed
				    } else if (app._page.name == "login" && app.main != "login") {
                                        app.loadPage(app.main);
                                    }
				}
			} else if (response.error) {

				if (loginFailedCallback) {
					loginFailedCallback(response.error);
				} else {
					console.error(response.error);
				}
			}
		}
	});
}

wm.getUserPrincipal = function() {
	return wm.disableUserPrincipalCookie ? {} : 
		dojo.fromJson(dojo.cookie("wmUserPrincipal")) || {};
}

wm.setUserPrincipal = function(userPrincipal) {
	dojo.cookie("wmUserPrincipal", dojo.toJson(userPrincipal));
}

wm.clearUserPrincipal = function() {
	dojo.cookie("wmUserPrincipal", null, {expires: -1});
}

wm.getUserRoles = function(force) {
    if (!force) {
	if (!wm.disableUserPrincipalCookie) {
	    if (wm.getUserPrincipal().roles) {
		return wm.getUserPrincipal().roles;
	    }
	} else if (wm.roles) {
	    return wm.roles;
	}
    }
	var s = wm.securityService || (wm.securityService = 
		new wm.JsonRpcService({name: "securityService", sync: true}));
	try {
		if (s.ready) {
			s.request("getUserRoles", null);
			if (s.result) {
				return s.result;
			}
		}
	} catch(x) {}
}

wm.logoutSuccess = function() {
    if (dojo.cookie.isSupported() && !wm.disableUserPrincipalCookie) {
	wm.clearUserPrincipal();
    } else {
	wm.roles = [];
    }
    dojo.publish("wmRbacUpdate");    
}

wm.logout = function() {
	var s = wm.securityService || (wm.securityService = 
		new wm.JsonRpcService({name: "securityService", sync: true, errorLevel: 2}));
	try {
		if (s.ready) {
			s.request("logout", null);
			window.location.reload();
		}
	} catch(x) {}
}

