package org.jvoicexml.systemtest.testcase;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CaseLibraryIgnoreTest {

    String ignoreList = "config/ignores.xml";

    String manifest = "irtest/irtests/manifest.xml";

    IRTestCaseLibrary lib;

    @Before
    public void setUp() throws Exception {

        lib = new IRTestCaseLibrary();
        lib.setBaseURL("http:Localhost:8080/irtest");
        lib.setTestsuite("vxml21_0.0.5");

        Assert.assertEquals(607, lib.size());

    }

    @Test
    public void testTestCaseSetIgnore() {
        IRTestCase tc = new IRTestCase();
        Assert.assertNull(tc.getIgnoreReason());
        tc.setIgnoreReason("a test");
        Assert.assertNotNull(tc.getIgnoreReason());
    }

    @Test
    public void listFatchLegal1() throws Exception {

        IRTestCase tc;

        tc = lib.fetch(11);
        Assert.assertNull(tc.getIgnoreReason());
        tc = lib.fetch(1019);
        Assert.assertNull(tc.getIgnoreReason());
        tc = lib.fetch(41);
        Assert.assertNull(tc.getIgnoreReason());

        lib.setIgnoreList(ignoreList);

        tc = lib.fetch(1019);
        Assert.assertNotNull(tc.getIgnoreReason());
        tc = lib.fetch(41);
        Assert.assertNull(tc.getIgnoreReason());
    }

    @Test
    public void testTemporaryIgnoreWork() throws IOException {
        IRTestCase tc;

        tc = lib.fetch(1);
        Assert.assertNull(tc.getIgnoreReason());

        lib.setIgnores("1");
        tc = lib.fetch(1);
        Assert.assertNotNull(tc.getIgnoreReason());

    }
    
    @Test
    public void testManualWork() throws IOException {
        IRTestCase tc;

        tc = lib.fetch(1184);
        Assert.assertNotNull(tc.getIgnoreReason());

        lib.setIgnores("1");
        tc = lib.fetch(1);
        Assert.assertNotNull(tc.getIgnoreReason());

    }
}
