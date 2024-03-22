/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.media.internal

import com.adobe.marketing.mobile.edge.media.Media
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.Properties

class MediaExtensionVersionTest {
    private val gradlePropertiesPath = "../gradle.properties"
    private val propertyModuleVersion = "moduleVersion"

    @Test
    fun extensionVersion_verifyModuleVersionInPropertiesFile_asEqual() {
        val properties: Properties = loadProperties(gradlePropertiesPath)
        assertNotNull(Media.extensionVersion())
        assertFalse(Media.extensionVersion().isEmpty())
        val moduleVersion: String = properties.getProperty(propertyModuleVersion)
        assertNotNull(moduleVersion)
        assertFalse(moduleVersion.isEmpty())
        assertEquals(
            java.lang.String.format(
                "Expected version to match in gradle.properties (%s) and extensionVersion API (%s)",
                moduleVersion,
                Media.extensionVersion()
            ),
            moduleVersion,
            Media.extensionVersion()
        )
    }

    private fun loadProperties(filepath: String): Properties {
        val properties = Properties()
        var input: InputStream? = null
        try {
            input = FileInputStream(filepath)
            properties.load(input)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return properties
    }
}
