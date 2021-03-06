/*
 * Copyright (C) 2008-2012 VMware, Inc. All rights reserved.
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

dojo.provide("wm.studio.pages.RestServiceBuilder.RestServiceBuilder");

dojo.declare("RestServiceBuilder", wm.Page, {
        i18n: true, 
	IMPORT_TYPE_URL: "URL",
	IMPORT_TYPE_FILE: "File",
	start: function() {
		this.updateSelect(this.methodInput, ["GET", "POST"]);
		this.methodInput.setValue("displayValue", "GET");
		this.contentTypeInput.setValue("displayValue", "text/xml");
		this.updateSelect(this.pathTypeInput, [this.IMPORT_TYPE_URL]);
		this.pathTypeInput.setValue("displayValue", this.IMPORT_TYPE_URL);
		this.schemaFileRadioInputChange();
	    //this.schemaTextRadioInputChange();
	},
	updateSelect: function(inSelect, inData) {
		var s = inSelect, o;
		if (inData == null) {
			o = null;
		} else {
			o = inData.join(",");
		}
		s.displayValue = "";
	    if (inSelect instanceof wm.SelectMenu) {
		s.setOptions(o);
	    } else {
		s.editor.setOptions(o);
	    }
	},
	saveButtonClick: function(inSender) {
		this.buildRestService(false);
	},
	buildRestService: function(inOverwrite) {
		var w = (inOverwrite == undefined || inOverwrite == null) ? false : inOverwrite;
		var sname = this.serviceNameInput.getValue("displayValue");
		var opname = this.serviceOpInput.getValue("displayValue");
		if (!sname || sname.length == 0 || !opname || opname.length == 0) {
		    app.alert(this.getDictionaryItem("ALERT_INPUT_NEEDED"));
			return;
		}
		if(this.inputFieldListVar.getCount() > 0 ) {
			var d = this.inputFieldListVar.getData();
			for (var i = 0; i < d.length; i++) {
			d[i].location = d[i].isHeader ? "header" : "url";
			delete d[i].isHeader;
			}
		}
		else{
			d =  [];
		}
		var url = this.urlInput.getValue("displayValue");
		if (!url || url.length == 0) {
		    app.alert(this.getDictionaryItem("ALERT_NO_URL"));
			return;
		}
		var m = this.methodInput.getValue("displayValue");
		var ct = this.contentTypeInput.getValue("displayValue");
		var output;
		if (this.outIsRawStringInput.getChecked()) {
			output = "string";
		} else {
			output = this.outTypeInput.getValue("displayValue");
//			if (!output || output.length == 0) {
//				app.alert("Missing Output Type!");
//				return;
//			}
		}
		if (!output || output == "(None)") output = null;
		var schemaPath = null, schemaText = null;
		if (output != "string") {
			if (this.isSchemaFileRadioSelected()) {
				if (this.pathTypeInput.getValue("displayValue") == this.IMPORT_TYPE_URL) {
					schemaPath = this.xmlSchemaUrlInput.getValue("displayValue");
				} else {
					schemaPath = this.xmlSchemaFileInput.fileNode.value;
				}
			} else {
				schemaText = this.xmlSchemaTextInput.getValue("displayValue");
			}
		}
	    studio.beginWait(this.getDictionaryItem("WAIT_IMPORT"));
		studio.webService.requestAsync("buildRestService", 
			[sname, opname, d, url, m, ct, output, schemaText, schemaPath, w], 
			dojo.hitch(this, "buildRestServiceSuccess"), 
			dojo.hitch(this, "buildRestServiceError"));
	},
	buildRestServiceSuccess: function(inResponse) {
		studio.endWait();
		if (inResponse == "$already_exists$") {
                    app.confirm(this.getDictionaryItem("CONFIRM_OVERWRITE"), false,
                                dojo.hitch(this, function() {
				    this.buildRestService(true);
                                }));
	        } else {
			this.owner.owner.importCompleted(inResponse);
		}
	},
	buildRestServiceError: function(inError) {
		studio.endWait();
	    app.alert(this.getDictionaryItem("ALERT_ERROR", {error: inError}));
	},
	clearAll: function(inSender) {
		this.serviceNameInput.clear();
		this.serviceOpInput.clear();
		this.methodInput.setValue("displayValue", "GET");
		this.contentTypeInput.setValue("displayValue", "text/xml");
	    //this.inParamsList.clear();
	    this.inputFieldListVar.clearData();
		this.urlInput.clear();
		this.updateSelect(this.outTypeInput, null);
		this.outTypeInput.clear();
		this.outIsRawStringInput.setChecked(false);
		this.xmlSchemaFileInput.fileNode.value = "";
		this.xmlSchemaUrlInput.clear();
		this.xmlSchemaTextInput.clear();
	},
	populateButtonClick: function(inSender) {
		this.owner.owner.layers.setLayer("restUrlLayer");
		this.owner.owner.importButton.setDisabled(true);
		this.owner.owner.restUrlPage.page.clearAll();
	},
	populate: function() {
		var url = this.owner.owner.restUrlPage.page.url;
		var method = this.owner.owner.restUrlPage.page.method;
		this.methodInput.setDataValue(method);
		var contentType = this.owner.owner.restUrlPage.page.contentType;
		this.contentTypeInput.setValue("displayValue", contentType);
		var postData = this.owner.owner.restUrlPage.page.postData;
		var basicAuth = this.owner.owner.restUrlPage.page.basicAuth;
		var userId = this.owner.owner.restUrlPage.page.userId;
		var password = this.owner.owner.restUrlPage.page.password;
		if (url) {
			studio.beginWait("Populating...");
			if (method == "GET") {
				studio.webService.requestAsync("generateRESTWsdlSettings", 
					[url, basicAuth, userId, password], 
					dojo.hitch(this, "generateRESTWsdlSettingsSuccess"), 
					dojo.hitch(this, "generateRESTWsdlSettingsError"));
			} else { //POST
				studio.webService.requestAsync("generateRESTWsdlSettings", 
					[url, method, contentType, postData, basicAuth, userId, password], 
					dojo.hitch(this, "generateRESTWsdlSettingsSuccess"), 
					dojo.hitch(this, "generateRESTWsdlSettingsError"));
			}
		}
	},
	generateRESTWsdlSettingsSuccess: function(inResponse) {
		studio.endWait();
		this.serviceNameInput.setValue("displayValue", inResponse.serviceName);
		this.serviceOpInput.setValue("displayValue", inResponse.operationName);
		var d = inResponse.inputs;
	    this.inputFieldListVar.setData(d);
	    //this.inParamsList.renderData(d);
		this.urlInput.setValue("displayValue", inResponse.parameterizedUrl);
		this.schemaTextRadioInput.components.editor.setChecked(true);
		if (inResponse.outputType && inResponse.outputType == "string") {
			this.outIsRawStringInput.setChecked(true);
		} else {
			this.outIsRawStringInput.setChecked(false);
			this.xmlSchemaTextInput.setValue("displayValue", inResponse.xmlSchemaText);
			this.getSchemaElementTypes(inResponse.outputType);
		}
	},
	generateRESTWsdlSettingsError: function(inError) {
		studio.endWait();
	},
	addInParamButtonClick: function(inSender) {
		var name = dojo.trim(this.inParamNameInput.getValue("displayValue"));
		var type = dojo.trim(this.inParamTypeInput.getValue("displayValue"));
		if (name && type) {
		    var count = this.inputFieldListVar.getCount();
		    for (var i = 0; i < count; i++) {
			var item = this.inputFieldListVar.getItem(i);
			if (item.getValue("name") == name) {
			    app.alert(this.getDictionaryItem("ALERT_PARAMETER_EXISTS"));
			    return;
			}
		    }
		    this.inputFieldListVar.addItem({isHeader: false, name: name, type: type});
		    this.updateParameters();
		    this.inParamNameInput.focus();
/*
			var d = this.inParamsList._data;
			if (d == null) {
				d = [];
			}
			for (var i = 0; i < d.length; i++) {
				if (d[i] && d[i].name == name) {
				    app.alert(this.getDictionaryItem("ALERT_PARAMETER_EXISTS"));
					return;
				}
			}
			d.push({name: name, type: type});

			this.inParamsList.renderData(d);
			*/
			this.inParamNameInput.clear();
			this.inParamTypeInput.clear();
		}
	},
	removeInParamButtonClick: function(inSender) {
	    var data = this.inParamsGrid.selectedItem.getData();
	    if (!data) return;
	    var count = this.inputFieldListVar.getCount();
	    for (var i = 0; i < count; i++) {
		var item = this.inputFieldListVar.getItem(i);
		if (item.getValue("name") == data.name) {
		    this.inputFieldListVar.removeItem(i);
		    this.updateParameters();
		    return;
		}
	    }
	},
    onHeaderCheckboxChange: function(inSender, inValue, rowId, fieldId) {
      try {
          var data = inSender.getRow(rowId);
	  var count = this.inputFieldListVar.getCount();
	  for (var i = 0; i < count; i++) {
	      var item = this.inputFieldListVar.getItem(i);
	      if (item.getValue("name") == data.name) {
		  item.beginUpdate();
		  item.setValue("isHeader", data.isHeader);
		  item.endUpdate();
		  this.updateParameters();
		  return;
	      }
	  }
	  
          
      } catch(e) {
          console.error('ERROR IN dojoGrid1CellEdited: ' + e); 
      } 
    },
    updateParameters: function(inSender) {
		var url = this.urlInput.getValue("displayValue");
		if (url) {
		    url = url.replace(/\?.*$/,"");
		    var data = this.inputFieldListVar.getData();
		    var d = [];
		    for (var i = 0; i < data.length; i++) {
			if (!data[i].isHeader) d.push(data[i]);
		    }
		    var queryString = "";
		    for (var i = 0; i < d.length; i++) {
			queryString = queryString + d[i].name + "={" + d[i].name + "}" + (i+1 == d.length ? "" : "&");
		    }
		    this.urlInput.setValue("displayValue", (url + "?" + queryString));
		}
        },
	outIsRawStringInputChange: function(inSender, inDisplayValue, inDataValue) {
		var b = inSender.getChecked();
		this.outTypeInput.editor.setDisabled(b);
		this.xmlSchemaLabel.setDisabled(b);
		this.schemaFileRadioInput.setDisabled(b);
		this.schemaTextRadioInput.setDisabled(b);
		this.importXmlSchemaButton.setDisabled(b);
		if (b) {
			this.pathTypeInput.setDisabled(b);
			this.xmlSchemaFileInput.fileNode.disabled = b;
			this.xmlSchemaUrlInput.setDisabled(b);
			this.xmlSchemaTextInput.setDisabled(b);
			this.xml2SchemaButton.setDisabled(b);
		} else {
			this.schemaFileRadioInputChange();
		    //this.schemaTextRadioInputChange();
		}
	},
	isSchemaFileRadioSelected: function() {
		//return (this.schemaFileRadioInput.displayValue == this.schemaFileRadioInput.dataValue);
		return (this.schemaFileRadioInput.getGroupValue() == 1);
	},
	schemaFileRadioInputChange: function(inSender, inDisplayValue, inDataValue) {
		var b = this.isSchemaFileRadioSelected();
		this.pathTypeInput.setDisabled(!b);
		this.xmlSchemaFileInput.fileNode.disabled = !b;
		this.xmlSchemaUrlInput.setDisabled(!b);
		this.xmlSchemaTextInput.setDisabled(b);
		this.xml2SchemaButton.setDisabled(b);
	},
/*
	schemaTextRadioInputChange: function(inSender, inDisplayValue, inDataValue) {
		var b = this.isSchemaFileRadioSelected();
		this.pathTypeInput.setDisabled(!b);
		this.xmlSchemaFileInput.fileNode.disabled = !b;
		this.xmlSchemaUrlInput.setDisabled(!b);
		this.xmlSchemaTextInput.setDisabled(b);
		this.xml2SchemaButton.setDisabled(b);
	},
	*/
	importXmlSchemaButtonClick: function(inSender) {
		this.getSchemaElementTypes();
	},
	getSchemaElementTypes: function(inType) {
		var schemaPath = null, schemaText = null;
		if (this.isSchemaFileRadioSelected()) {
			if (this.pathTypeInput.getValue("displayValue") == this.IMPORT_TYPE_URL) {
				schemaPath = this.xmlSchemaUrlInput.getValue("displayValue");
			} else {
				schemaPath = this.xmlSchemaFileInput.fileNode.value;
			}
		} else {
			schemaText = this.xmlSchemaTextInput.getValue("displayValue");
		}
		studio.webService.requestAsync("getSchemaElementTypes", 
			[schemaPath, schemaText], 
			dojo.hitch(this, "getSchemaElementTypesSuccess", inType), 
			dojo.hitch(this, "getSchemaElementTypesError"));
	},
	getSchemaElementTypesSuccess: function(inType, inResponse) {
		var r = inResponse;
		if (r) {
			var o = (this.inParamTypeInput.options.split(",")).concat(r);
			this.updateSelect(this.inParamTypeInput, o);
			r.push("(None)");
		}
		this.updateSelect(this.outTypeInput, r);
		if (inType) {
			this.outTypeInput.setValue("displayValue", inType);
		} else if (r && r.length > 0) {
			this.outTypeInput.setValue("displayValue", r[0]);
		}
	},
	getSchemaElementTypesError: function(inError) {
		
	},
	xml2SchemaButtonClick: function(inSender) {
		var xml = this.xmlSchemaTextInput.getValue("displayValue");
	    if (xml) {
                app.confirm(this.getDictionaryItem("CONFIRM_GENERATE_SCHEMA"), false,
                            dojo.hitch(this, function() {
			        studio.beginWait(this.getDictionaryItem("WAIT_GENERATE_SCHEMA"));
			        studio.webService.requestAsync("convertXmlToSchema", 
				                               [xml], 
				                               dojo.hitch(this, "convertXmlToSchemaSuccess"), 
				                               dojo.hitch(this, "convertXmlToSchemaError"));
                            }));
		}
	},
	convertXmlToSchemaSuccess: function(inResponse) {
		studio.endWait();
		this.xmlSchemaTextInput.setValue("displayValue", inResponse);
		this.getSchemaElementTypes();
	},
	convertXmlToSchemaError: function(inError) {
		studio.endWait();
	    app.alert(this.getDictionaryItem("ALERT_GENERATE_SCHEMA_ERROR", {error: inError}));
	},
	pathTypeInputChange: function(inSender, inValue) {
		var b = (inValue == this.IMPORT_TYPE_URL);
		this.xmlSchemaFileInput.setShowing(!b);
		this.xmlSchemaUrlInput.setShowing(b);
	},
	_end: 0
});