/*
 * Copyright (C) 2008 WaveMaker Software, Inc.
 *
 * This file is part of WaveMaker Studio.
 *
 * WaveMaker Studio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License, only.
 *
 * WaveMaker Studio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with WaveMaker Studio.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wavemaker.studio.project.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.wavemaker.common.util.IOUtils;
import com.wavemaker.studio.infra.StudioTestCase;
import com.wavemaker.tools.project.upgrade.StudioUpgradeTask;
import com.wavemaker.tools.project.upgrade.UpgradeInfo;
import com.wavemaker.tools.project.upgrade.UpgradeManager;

/**
 * This class should only contain the single test method; any more than that and
 * the studioVersion key stuff can get wack.
 * 
 * @author small
 * @version $Rev$ - $Date$
 *
 */
public class TestUpgradeManager_StudioUpgrade extends StudioTestCase {

    private Double oldStudioVersion = null;
    
    @Override
    public void onSetUp() throws Exception {
        
        setDirty();
        
        oldStudioVersion = UpgradeManager.getStudioVersion();
        UpgradeManager.setStudioVersion(0.0d);
        
        super.onSetUp();
    }
    
    @Override
    public void onTearDown() throws Exception {

        UpgradeManager.setStudioVersion(oldStudioVersion);

        super.onTearDown();
    }
    
    @Override
    protected String[] getWebAppConfigLocations() {
        
        String[] supers = super.getWebAppConfigLocations();
        String[] more = new String[] {
                // beans tested here
                "com/wavemaker/studio/project/upgrade/test-upgrademanager.xml"
            };
        
        return (String[]) ArrayUtils.addAll(supers, more);
    }
    
    public void testStudioUpgrade() throws Exception {
        
        UpgradeManager um = (UpgradeManager) getApplicationContext().getBean("upgradeManager");
        assertFalse(um.getStudioUpgrades().isEmpty());
        assertTrue(UpgradeManager.getStudioVersion()>0.0d);
        assertTrue(um.getStudioUpgrades().containsKey(0.1d));
        
        List<StudioUpgradeTask> oneUpgrades = um.getStudioUpgrades().get(0.1d);
        assertEquals(1, oneUpgrades.size());
        StudioUpgradeTask sut = oneUpgrades.get(0);
        assertTrue(sut instanceof StudioUpgradeFileCreationUpgradeTask);
        
        StudioUpgradeFileCreationUpgradeTask sufcut = (StudioUpgradeFileCreationUpgradeTask) sut;
        assertNotNull(sufcut.tempFile);
        assertTrue(sufcut.tempFile.exists());
        sufcut.tempFile.delete();
    }
    
    public static class StudioUpgradeFileCreationUpgradeTask implements StudioUpgradeTask {

        public File tempFile = null;
        
        public void doUpgrade(UpgradeInfo upgradeInfo) {
            
            try {
                tempFile = File.createTempFile(
                        "StudioUpgradeFileCreationUpgradeTask", ".tmp");
                tempFile.deleteOnExit();
                IOUtils.touch(tempFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}