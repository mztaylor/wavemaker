/*
 *  Copyright (C) 2012 VMware, Inc. All rights reserved.
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

package com.wavemaker.tools.io.zip;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.io.DynamicZipInputStream;
import org.cloudfoundry.client.lib.io.DynamicZipInputStream.Entry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.wavemaker.tools.io.File;
import com.wavemaker.tools.io.Folder;
import com.wavemaker.tools.io.Resource;
import com.wavemaker.tools.io.ResourcePath;

/**
 * Adapter class that can be used to convert a {@link Folder} into a zipped {@link InputStream}.
 * 
 * @author Phillip Webb
 */
public class ZippedFolderInputStream extends FilterInputStream {

    /**
     * Create a new {@link ZippedFolderInputStream} instance.
     * 
     * @param folder the folder to zip
     */
    public ZippedFolderInputStream(Folder folder) {
        this(folder, null);
    }

    /**
     * Create a new {@link ZippedFolderInputStream} instance.
     * 
     * @param folder the folder to zip
     * @param entryPrefix an optional entry prefix. This allows a entries to be nested within a folder if required
     */
    public ZippedFolderInputStream(Folder folder, String entryPrefix) {
        super(null);
        Assert.notNull(folder, "Folder must not be null");
        ResourcePath resourcePath = new ResourcePath();
        if (StringUtils.hasLength(entryPrefix)) {
            resourcePath = resourcePath.get(entryPrefix);
        }
        List<DynamicZipInputStream.Entry> entries = new ArrayList<DynamicZipInputStream.Entry>();
        for (Resource child : folder) {
            addToEntries(resourcePath, entries, child);
        }
        this.in = new DynamicZipInputStream(entries);
    }

    private void addToEntries(ResourcePath parent, List<Entry> entries, Resource resource) {
        entries.add(new ResourceEntry(parent, resource));
        if (resource instanceof Folder) {
            for (Resource child : (Folder) resource) {
                addToEntries(parent.get(resource.getName()), entries, child);
            }
        }
    }

    private static class ResourceEntry implements DynamicZipInputStream.Entry {

        private final ResourcePath parent;

        private final Resource resource;

        public ResourceEntry(ResourcePath resourcePath, Resource resource) {
            this.parent = resourcePath;
            this.resource = resource;
        }

        @Override
        public String getName() {
            return this.parent.get(this.resource.getName()).toString().substring(1) + (this.resource instanceof Folder ? "/" : "");
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (this.resource instanceof File) {
                return ((File) this.resource).getContent().asInputStream();
            }
            return null;
        }
    }
}
