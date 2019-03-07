/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2006-2017 JVoiceXML group - http://jvoicexml.sourceforge.net
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

package org.jvoicexml.implementation.pool;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.implementation.ExternalResource;
import org.jvoicexml.implementation.ResourceFactory;
import org.jvoicexml.xml.srgs.ModeType;

/**
 * Pool to hold all instantiated resources of type <code>T</code>.
 *
 * <p>
 * The <code>KeyedResourcePool</code> uses a {@link ResourceFactory} to create
 * new objects for the pool until the number of instances is exceeded that is
 * set by the factory.
 * </p>
 *
 * @param <T> Type of {@link ExternalResource} to produce in this factory.
 *      This is usually mapped to one of the resource types known to the
 *      {@link org.jvoicexml.ImplementationPlatform}:
 *      <ul>
 *      <li>{@link org.jvoicexml.implementation.SystemOutputImplementation}</li>
 *      <li>{@link org.jvoicexml.implementation.UserInputImplementation}</li>
 *      <li>{@link org.jvoicexml.implementation.CallControlImplementation}</li>
 *      </ul>
 *
 * @author Dirk Schnelle-Walka
 *
 * @since 0.5.1
 */
public final class KeyedResourcePool<T extends ExternalResource> {
    /** Logger for this class. */
    private static final Logger LOGGER =
        LogManager.getLogger(KeyedResourcePool.class);

    /** Known pools. */
    private final Map<String, Map<ModeType, ObjectPool<T>>> pools;

    /**
     * Constructs a new object.
     */
    public KeyedResourcePool() {
        super();
        pools = new java.util.HashMap<String, Map<ModeType, ObjectPool<T>>>();
    }

    /**
     * Adds the given resource factory.
     * @param resourceFactory The {@link ResourceFactory} to add.
     * @exception Exception error populating the pool
     */
    public void addResourceFactory(
            final ResourceFactory<T> resourceFactory) throws Exception {
        final PoolableObjectFactory<T> factory =
            new PoolableResourceFactory<T>(resourceFactory);
        final GenericObjectPool<T> pool = new GenericObjectPool<T>(factory);
        final int instances = resourceFactory.getInstances();
        pool.setMinIdle(instances);
        pool.setMaxActive(instances);
        pool.setMaxIdle(instances);
        pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
        final String type = resourceFactory.getType();
        Map<ModeType, ObjectPool<T>> modePools = pools.get(type);
        if (modePools == null) {
            modePools = new java.util.HashMap<ModeType, ObjectPool<T>>();
            pools.put(type, modePools);
        }
        final ModeType mode = resourceFactory.getModeType();
        modePools.put(mode, pool);
        LOGGER.info("loading " + instances + " instance(s) of type '" + type
                + "' for mode '" + mode + "'");
        for (int i = 0; i < instances; i++) {
            pool.addObject();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("...resources loaded.");
        }
    }

    /**
     * Retrieves the known pools for the given key 
     * @param key the key to look for
     * @return known pools, never {@code null}
     * @throws NoresourceError
     *          if there are no pools for the given key
     * @since 0.7.9
     */
    private Map<ModeType, ObjectPool<T>> getPools(final Object key)
            throws NoresourceError {
        final Map<ModeType, ObjectPool<T>> modePools = pools.get(key);
        if (modePools == null) {
            throw new NoresourceError("Pool of type '" + key + "' is unknown!");
        }
        return modePools;
    }

    /**
     * Type safe return of the object to borrow from the pool.
     * @param key the type of the object to borrow from the pool
     * @return borrowed object
     * @exception NoresourceError
     *            the object could not be borrowed
     */
    public synchronized Map<ModeType, T> borrowObjects(final Object key)
        throws NoresourceError {
        final Map<ModeType, ObjectPool<T>> modePools = getPools(key);
        final Map<ModeType, T> resources = new java.util.HashMap<ModeType, T>();
        for (ModeType mode : modePools.keySet()) {
            T resource;
            final ObjectPool<T> pool = modePools.get(mode);
            try {
                resource = pool.borrowObject();
                resources.put(mode, resource);
            } catch (Exception e) {
                returnObjects(key.toString(), resources);
                throw new NoresourceError(e.getMessage(), e);
            }
            LOGGER.info("borrowed object of type '" + key + "' ("
                    + resource.getClass().getCanonicalName() + ")");
            if (LOGGER.isDebugEnabled()) {
                final int active = pool.getNumActive();
                final int idle = pool.getNumIdle();
                LOGGER.debug("pool has now " + active
                             + " active/" + idle + " idle for key '" + key
                             + "', mode '" + mode 
                             + "' (" + resource.getClass().getCanonicalName()
                             + ") after borrow");
            }
        }

        return resources;
    }

    /**
     * Returns a previously borrowed resource to the pool.
     * @param key resource type.
     * @param resource resource to return.
     * @throws NoresourceError
     *         Error returning the object to the pool.
     * @since 0.6
     */
    public synchronized void returnObjects(final String key,
            final Map<ModeType, T> resources) throws NoresourceError {
        if (resources.isEmpty()) {
            LOGGER.warn("no resources to retrun for key '" + key + "'");
            return;
        }
        final Map<ModeType, ObjectPool<T>> modePools = getPools(key);
        for (ModeType mode : modePools.keySet()) {
            final ObjectPool<T> pool = modePools.get(mode);
            final T resource = resources.get(mode);
            try {
                pool.returnObject(resource);
            } catch (Exception e) {
                throw new NoresourceError(e.getMessage(), e);
            }
            LOGGER.info("returned object of type '" + key + "' ("
                    + resource.getClass().getCanonicalName() + ")");
    
            if (LOGGER.isDebugEnabled()) {
                final int active = pool.getNumActive();
                final int idle = pool.getNumIdle();
                LOGGER.debug("pool has now " + active
                             + " active/" + idle + " idle for key '" + key
                             + "', mode '" + mode 
                             + "' (" + resource.getClass().getCanonicalName()
                             + ") after return");
            }
        }
    }

    /**
     * Retrieves the number of active resources in all pools.
     * @return number of active resources
     * @since 0.7.3
     */
    public synchronized int getNumActive() {
        int active = 0;
        for (Map<ModeType, ObjectPool<T>> modePools : pools.values()) {
            final Collection<ObjectPool<T>> col = modePools.values();
            for (ObjectPool<T> pool : col) {
                active += pool.getNumActive();
            }
        }
        return active;
    }

    /**
     * Retrieves the number of active resources for all modes in the pool for
     * the given key.
     * @param key the key
     * @return number of active resources
     * @since 0.7.3
     */
    public synchronized int getNumActive(final String key) {
        final Map<ModeType, ObjectPool<T>> modePools = pools.get(key);
        final Collection<ObjectPool<T>> col = modePools.values();
        int active = 0;
        for (ObjectPool<T> pool : col) {
            active += pool.getNumActive();
        }
        return active;
    }

    /**
     * Retrieves the number of idle resources in all pools.
     * @return number of idle resources
     * @since 0.7.3
     */
    public synchronized int getNumIdle() {
        int idle = 0;
        for (Map<ModeType, ObjectPool<T>> modePools : pools.values()) {
            final Collection<ObjectPool<T>> col = modePools.values();
            for (ObjectPool<T> pool : col) {
                idle += pool.getNumIdle();
            }
        }
        return idle;
    }

    /**
     * Retrieves the number of idle resources in the pool for the given key.
     * @param key the key
     * @return number of idle resources, <code>-1</code> if there is no resource
     *         with that key
     * @since 0.7.3
     */
    public synchronized int getNumIdle(final String key) {
        final Map<ModeType, ObjectPool<T>> modePools = pools.get(key);
        final Collection<ObjectPool<T>> col = modePools.values();
        int idle = 0;
        for (ObjectPool<T> pool : col) {
            idle += pool.getNumIdle();
        }
        return idle;
    }

    /**
     * Retrieves the available keys of this pool.
     * @return available keys.
     * @since 0.7.4
     */
    public Collection<String> getKeys() {
        return pools.keySet();
    }

    /**
     * Closes all pools for all keys.
     * @throws Exception
     *         error closing a pool
     * @since 0.7.3
     */
    public synchronized void close() throws Exception {
        for (Map<ModeType, ObjectPool<T>> modePools : pools.values()) {
            final Collection<ObjectPool<T>> col = modePools.values();
            for (ObjectPool<T> pool : col) {
                pool.close();
            }
        }
    }
    
    /**
     * Dumps the contents of the pools to the log.
     * @param key the key to look for
     * @since 0.7.9
     */
    public void reportPool(final String key) {
        final Map<ModeType, ObjectPool<T>> modePools = pools.get(key);
        for (ModeType mode : modePools.keySet()) {
            final ObjectPool<T> current = modePools.get(mode);
            final int avail = getNumIdle(key);
            LOGGER.info("key: '" + key + "' mode: '" + mode + "' available: " + avail);
            final int active = getNumActive(key);
            LOGGER.info("key: '" + key + "' mode: '" + mode + "' active:    " + active);
        }
    }
}
