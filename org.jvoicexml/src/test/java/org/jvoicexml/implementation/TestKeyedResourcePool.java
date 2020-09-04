/*
 * JVoiceXML - A free VoiceXML implementation.
 *
<<<<<<< HEAD
 * Copyright (C) 2007-2019 JVoiceXML group - http://jvoicexml.sourceforge.net
=======
 * Copyright (C) 2007-2020 JVoiceXML group - http://jvoicexml.sourceforge.net
>>>>>>> refs/heads/develop
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.jvoicexml.implementation;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.implementation.pool.KeyedResourcePool;
import org.jvoicexml.xml.srgs.ModeType;
import org.mockito.Mockito;

/**
 *Test cases for {@link KeyedResourcePool}.
 * @author Dirk Schnelle
 *
 */
public final class TestKeyedResourcePool {
    /** The object to test. */
    private KeyedResourcePool<SystemOutputImplementation> pool;

    /** The resource factory inside the pool. */
    private ResourceFactory<SystemOutputImplementation> factory;
    
    /**
     * Prepare the test setup.
     * @throws NoresourceError test failed
     * @throws Exception test failed
     * @since 0.7.9
     */
    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws NoresourceError, Exception {
        factory = Mockito.mock(ResourceFactory.class);
        Mockito.when(factory.getType()).thenReturn("dummy");
        final SystemOutputImplementation output = Mockito.mock(SystemOutputImplementation.class);
        Mockito.when(factory.createResource()).thenReturn(output);
        pool = new KeyedResourcePool<SystemOutputImplementation>();
    }

    /**
     * Test method for {@link org.jvoicexml.implementation.pool.KeyedResourcePool#borrowObject(java.lang.Object)}.
     * @throws Exception
     *         Test failed.
     * @throws NoresourceError
     *         Test failed.
     */
    @Test
    public void testBorrowObjectObject()  throws Exception, NoresourceError {
        final int instances = 500;
        Mockito.when(factory.getInstances()).thenReturn(instances);
        pool.addResourceFactory(factory);
        Assert.assertEquals(instances, pool.getNumIdle());
        final String key = factory.getType();
        final List<Map<ModeType, SystemOutputImplementation>> outputs = 
                new java.util.ArrayList<Map<ModeType, SystemOutputImplementation>>(instances);
        for (int i = 0; i < instances; i++) {
            final Map<ModeType, SystemOutputImplementation> current =
                    pool.borrowObjects(key);
            Assert.assertNotEquals(0, current.size());
            outputs.add(current);
        }
        Assert.assertEquals(instances, pool.getNumActive(key));

        for (int i = 0; i < instances; i++) {
            final Map<ModeType, SystemOutputImplementation> current =
                    outputs.get(i);
            pool.returnObjects(key, current);
        }
        Assert.assertEquals(0, pool.getNumActive(key));
    }

    /**
     * Test method for {@link org.jvoicexml.implementation.pool.KeyedResourcePool#borrowObject(java.lang.Object)}.
     * @throws Exception
     *         Test failed.
     * @throws NoresourceError
     *         Test succeeded
     */
    @Test(expected = NoresourceError.class)
    public void testBorrowObjectObjectExceed()
        throws Exception, NoresourceError {
        final int instances = 10;
        Mockito.when(factory.getInstances()).thenReturn(instances);
        pool.addResourceFactory(factory);
        pool = new KeyedResourcePool<SystemOutputImplementation>();
        pool.addResourceFactory(factory);
        Assert.assertEquals(instances, pool.getNumIdle());
        final String key = factory.getType();
        final List<Map<ModeType, SystemOutputImplementation>> outputs = 
                new java.util.ArrayList<Map<ModeType, SystemOutputImplementation>>(instances);
        for (int i = 0; i < instances; i++) {
            final Map<ModeType, SystemOutputImplementation> current =
                    pool.borrowObjects(key);
            Assert.assertNotEquals(0, current.size());
            outputs.add(current);
        }
        Assert.assertEquals(instances, pool.getNumActive(key));
        pool.borrowObjects(key);
    }

    /**
     * Test method for {@link org.jvoicexml.implementation.pool.KeyedResourcePool#borrowObject(java.lang.Object)}.
     * @throws Exception
     *         Test failed.
     * @throws NoresourceError
     *         Test failed
     */
    @Test
    public void testBorrowObjectObjectMultipleKey()
        throws Exception, NoresourceError {
        final int instancesKey1 = 3;
        @SuppressWarnings("unchecked")
        final ResourceFactory<SystemOutputImplementation> factory1 =
            Mockito.mock(ResourceFactory.class);
        Mockito.when(factory1.getInstances()).thenReturn(instancesKey1);
        Mockito.when(factory1.getType()).thenReturn("dummy1");
        final SystemOutputImplementation output1 = Mockito.mock(SystemOutputImplementation.class);
        Mockito.when(factory1.createResource()).thenReturn(output1);
        final int instancesKey2 = 5;
        @SuppressWarnings("unchecked")
        final ResourceFactory<SystemOutputImplementation> factory2 =
                Mockito.mock(ResourceFactory.class);
        Mockito.when(factory2.getInstances()).thenReturn(instancesKey2);
        Mockito.when(factory2.getType()).thenReturn("dummy2");
        final SystemOutputImplementation output2 = Mockito.mock(SystemOutputImplementation.class);
        Mockito.when(factory2.createResource()).thenReturn(output2);
        pool = new KeyedResourcePool<SystemOutputImplementation>();
        pool.addResourceFactory(factory1);
        pool.addResourceFactory(factory2);
        final String key1 = factory1.getType();
        final String key2 = factory2.getType();
        Assert.assertEquals(instancesKey1, pool.getNumIdle(key1));
        Assert.assertEquals(instancesKey2, pool.getNumIdle(key2));
        Assert.assertEquals(instancesKey1 + instancesKey2, pool.getNumIdle());
        final String[] keys = new String[]
                            {key2, key1, key2, key1, key2, key1, key2, key2};
        final List<Map<ModeType, SystemOutputImplementation>> outputs = 
                new java.util.ArrayList<Map<ModeType, SystemOutputImplementation>>(instancesKey1 + instancesKey2);
        for (int i = 0; i < instancesKey1 + instancesKey2; i++) {
            final String key = keys[i];
            final Map<ModeType, SystemOutputImplementation> current =
                    pool.borrowObjects(key);
            Assert.assertNotEquals(0, current.size());
            outputs.add(current);
        }
        Assert.assertEquals(instancesKey1, pool.getNumActive(key1));
        Assert.assertEquals(instancesKey2, pool.getNumActive(key2));
        for (int i = 0; i < instancesKey1 + instancesKey2; i++) {
            final String key = keys[i];
            final Map<ModeType, SystemOutputImplementation> current =
                    outputs.get(i);
            pool.returnObjects(key, current);
        }
        Assert.assertEquals(0, pool.getNumActive(key1));
        Assert.assertEquals(0, pool.getNumActive(key2));
    }
}
