/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2007-2019 JVoiceXML group - http://jvoicexml.sourceforge.net
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
import org.junit.Test;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.implementation.pool.KeyedResourcePool;
import org.jvoicexml.mock.implementation.MockSynthesizedOutputFactory;
import org.jvoicexml.xml.srgs.ModeType;

/**
 *Test cases for {@link KeyedResourcePool}.
 * @author Dirk Schnelle
 *
 */
public final class TestKeyedResourcePool {
    /** The object to test. */
    private KeyedResourcePool<SystemOutputImplementation> pool;

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
        final ResourceFactory<SystemOutputImplementation> factory =
            new MockSynthesizedOutputFactory();
        ((MockSynthesizedOutputFactory) factory).setInstances(instances);
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
        final ResourceFactory<SystemOutputImplementation> factory =
            new MockSynthesizedOutputFactory();
        ((MockSynthesizedOutputFactory) factory).setInstances(instances);
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
        final ResourceFactory<SystemOutputImplementation> factory1 =
            new MockSynthesizedOutputFactory();
        ((MockSynthesizedOutputFactory) factory1).setInstances(instancesKey1);
        final int instancesKey2 = 5;
        final ResourceFactory<SystemOutputImplementation> factory2 =
            new MockSynthesizedOutputFactory("alt");
        ((MockSynthesizedOutputFactory) factory2).setInstances(instancesKey2);
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
