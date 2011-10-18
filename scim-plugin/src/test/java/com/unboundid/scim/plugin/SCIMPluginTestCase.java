/*
 * Copyright 2011 UnboundID Corp.
 * All Rights Reserved.
 */

package com.unboundid.scim.plugin;

import com.unboundid.directory.tests.standalone.ExternalInstance;
import com.unboundid.directory.tests.standalone.ExternalInstanceId;
import com.unboundid.directory.tests.standalone.ExternalInstanceManager;
import com.unboundid.scim.sdk.Version;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;



/**
 * Test coverage for the SCIM plugin.
 */
public class SCIMPluginTestCase extends ServerExtensionTestCase
{
  /**
   * Tests that the plugin can be installed and enabled.
   *
   * @throws Exception  If the test fails.
   */
  @Test
  public void enablePlugin()
      throws Exception
  {
    final ExternalInstanceManager m = ExternalInstanceManager.singleton();
    final ExternalInstance instance;
    try
    {
      instance =
        m.getExternalInstance(ExternalInstanceId.BasicServer);
    }
    catch(RuntimeException e)
    {
      // This could occur if there are no external instance ZIPs found. In which
      // case, we can just let the test pass as this was warned before.
      if(e.getMessage().equals("ExternalInstance must be provided with the " +
          "path to a DS zip file. This can be done by setting the dsZipPath " +
          "environment variable to the full path of the zip.  Or by setting " +
          "the JVM property dsZipPath to the full path of the zip."))
      {
        return;
      }
      else
      {
        throw e;
      }
    }

    final File pluginZipFile = new File(System.getProperty("pluginZipFile"));
    installExtension(instance, pluginZipFile);

    instance.startInstance();
    configurePlugin(instance, "scim-plugin", 8181);

    assertEquals(getMonitorAsString(instance, "Version"), Version.VERSION);
    instance.stopInstance();
  }
}