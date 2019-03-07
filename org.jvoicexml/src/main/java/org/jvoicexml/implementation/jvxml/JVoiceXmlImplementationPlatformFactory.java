/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2005-2017 JVoiceXML group - http://jvoicexml.sourceforge.net
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

package org.jvoicexml.implementation.jvxml;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.Configuration;
import org.jvoicexml.ConfigurationException;
import org.jvoicexml.ConnectionInformation;
import org.jvoicexml.ImplementationPlatform;
import org.jvoicexml.ImplementationPlatformFactory;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.implementation.CallControlImplementation;
import org.jvoicexml.implementation.ExternalResource;
import org.jvoicexml.implementation.PlatformFactory;
import org.jvoicexml.implementation.ResourceFactory;
import org.jvoicexml.implementation.SystemOutputImplementation;
import org.jvoicexml.implementation.UserInputImplementation;
import org.jvoicexml.implementation.dtmf.BufferedDtmfInput;
import org.jvoicexml.implementation.pool.KeyedResourcePool;

/**
 * Basic implementation of an {@link ImplementationPlatformFactory}.
 * 
 * <p>
 * This implementation manages a pool of resource factories which are delivered
 * to each created {@link ImplementationPlatform}.
 * </p>
 * 
 * <p>
 * In {@link #init(Configuration)} the resources are acquired as
 * {@link PlatformFactory}s and {@link ResourceFactory}s.
 * </p>
 * 
 * @author Dirk Schnelle-Walka
 */
public final class JVoiceXmlImplementationPlatformFactory
        implements ImplementationPlatformFactory {
    /** Logger for this class. */
    private static final Logger LOGGER = LogManager
            .getLogger(JVoiceXmlImplementationPlatformFactory.class);

    /** Pool of system output resource factories. */
    private final KeyedResourcePool<SystemOutputImplementation> sysystemOutputPool;

    /** Pool of user input resource factories. */
    private final KeyedResourcePool<UserInputImplementation> userInputPool;

    /** Pool of user calling resource factories. */
    private final KeyedResourcePool<CallControlImplementation> callControlPool;

    /** The JVoiceXML configuration. */
    private Configuration configuration;

    /**
     * Constructs a new object.
     * 
     * <p>
     * This method should not be called by any application. This resource is
     * controlled by the <code>JvoiceXml</code> object.
     * </p>
     * 
     * @see org.jvoicexml.JVoiceXml
     */
    public JVoiceXmlImplementationPlatformFactory() {
        sysystemOutputPool = new KeyedResourcePool<SystemOutputImplementation>();
        userInputPool = new KeyedResourcePool<UserInputImplementation>();
        callControlPool = new KeyedResourcePool<CallControlImplementation>();
    }

    /**
     * {@inheritDoc} This implementation loads all {@link PlatformFactory}s and
     * {@link ResourceFactory}s. They can also be set manually by
     * {@link #addPlatform(PlatformFactory)},
     * {@link #addUserInputFactory(ResourceFactory)},
     * {@link #addSynthesizedOutputFactory(ResourceFactory)} and
     * {@link #addCallControlFactory(ResourceFactory)}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void init(final Configuration config) throws ConfigurationException {
        final Collection<PlatformFactory> factories = config.loadObjects(
                PlatformFactory.class, "implementation");
        for (PlatformFactory factory : factories) {
            try {
                addPlatform(factory);
            } catch (Exception e) {
                throw new ConfigurationException(e.getMessage(), e);
            }
        }
        final Collection<ResourceFactory> resourceFactories = config
                .loadObjects(ResourceFactory.class, "implementation");
        try {
            for (ResourceFactory resourceFactory : resourceFactories) {
                final Class<ExternalResource> clazz = resourceFactory
                        .getResourceType();
                if (clazz.equals(UserInputImplementation.class)) {
                    addUserInputFactory(resourceFactory);
                } else if (clazz.equals(SystemOutputImplementation.class)) {
                    addSynthesizedOutputFactory(resourceFactory);
                } else if (clazz.equals(CallControlImplementation.class)) {
                    addCallControlFactory(resourceFactory);
                }
            }
        } catch (Exception | UnsatisfiedLinkError e) {
            throw new ConfigurationException(e.getMessage(), e);
        }

        // Give a short summary of what is available
        reportPlatforms();

        // Keep a reference to the configuration
        configuration = config;
    }

    /**
     * Log a report of currently available platforms.
     * 
     * @since 0.7.4
     */
    private void reportPlatforms() {
        final Collection<String> synthesizers = sysystemOutputPool.getKeys();
        if (synthesizers.isEmpty()) {
            LOGGER.warn("no synthesizers available");
        } else {
            LOGGER.info("available user inputs:");
            for (String key : synthesizers) {
                sysystemOutputPool.reportPool(key);
            }
        }
        final Collection<String> recognizers = userInputPool.getKeys();
        if (recognizers.isEmpty()) {
            LOGGER.warn("no recognizers available");
        } else {
            LOGGER.info("available recognizers:");
            for (String key : recognizers) {
                userInputPool.reportPool(key);
            }
        }
        final Collection<String> telephones = callControlPool.getKeys();
        if (telephones.isEmpty()) {
            LOGGER.warn("no telephones available");
        } else {
            LOGGER.info("available telephones:");
            for (String key : telephones) {
                callControlPool.reportPool(key);
            }
        }
    }

    /**
     * Adds the given platform factory to the list of known factories.
     * 
     * @param platform
     *            the platform factory to add.
     * @exception Exception
     *                error adding the platform
     * @since 0.7
     */
    public void addPlatform(final PlatformFactory platform) throws Exception {
        final ResourceFactory<SystemOutputImplementation> synthesizedOutputFactory =
                platform.getSystemoutputImplementation();
        if (synthesizedOutputFactory != null) {
            addSynthesizedOutputFactory(synthesizedOutputFactory);
        }
        final ResourceFactory<UserInputImplementation> spokenInputFactory = platform
                .getUserinputImplemetation();
        if (spokenInputFactory != null) {
            addUserInputFactory(spokenInputFactory);
        }
        final ResourceFactory<CallControlImplementation> telephonyFactory = platform
                .getCallControlImplementation();
        if (telephonyFactory != null) {
            addCallControlFactory(telephonyFactory);
        }
    }

    /**
     * Adds the given {@link ResourceFactory} for {@link SystemOutputImplementation} to
     * the list of know factories.
     * 
     * @param factory
     *            the factory to add.
     * @exception Exception
     *                error creating the pool
     * @since 0.6
     */
    private void addSynthesizedOutputFactory(
            final ResourceFactory<SystemOutputImplementation> factory) 
                    throws Exception {
        final String type = factory.getType();
        sysystemOutputPool.addResourceFactory(factory);

        LOGGER.info("added synthesized output factory " + factory.getClass()
                + " for type '" + type + "'");
    }

    /**
     * Adds the given {@link ResourceFactory} for {@link UserInputImplementation} to the
     * list of know factories.
     * 
     * @param factory
     *            the factory to add.
     * @exception Exception
     *                error adding the factory
     * @since 0.6
     */
    public void addUserInputFactory(
            final ResourceFactory<UserInputImplementation> factory)
            throws Exception {
        final String type = factory.getType();
        userInputPool.addResourceFactory(factory);

        LOGGER.info("added user input factory " + factory.getClass()
                + " for type '" + type + "'");
    }

    /**
     * Adds the given {@link ResourceFactory} for
     * {@link CallControlImplementation} to the list of know factories.
     * 
     * @param factory
     *            the factory to add.
     * @exception Exception
     *                error adding the factory
     * @since 0.6
     */
    public void addCallControlFactory(
            final ResourceFactory<CallControlImplementation> factory)
            throws Exception {
        final String type = factory.getType();
        callControlPool.addResourceFactory(factory);
        LOGGER.info("added telephony factory " + factory.getClass()
                + " for type '" + type + "'");
    }

    /**
     * {@inheritDoc}
     */
    public synchronized ImplementationPlatform getImplementationPlatform(
            final ConnectionInformation info) throws NoresourceError {
        if (info == null) {
            throw new NoresourceError("No connection information given!");
        }

        try {
            final BufferedDtmfInput input = configuration
                    .loadObject(BufferedDtmfInput.class);
            final JVoiceXmlImplementationPlatform platform =
                    new JVoiceXmlImplementationPlatform(
                    callControlPool, sysystemOutputPool, userInputPool, input,
                    info);
            platform.init(configuration);
            return platform;
        } catch (ConfigurationException e) {
            throw new NoresourceError(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("closing implementation platforms...");
        }

        // Start a brute force termination in case the shutdown does not
        // terminate.
        final TerminationThread termination = new TerminationThread();
        termination.start();

        /** @todo Wait until all objects are returned to the pool. */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("synthesizer pool has "
                    + sysystemOutputPool.getNumActive() + " active/"
                    + sysystemOutputPool.getNumIdle() + " idle objects");
        }
        try {
            sysystemOutputPool.close();
        } catch (Exception ex) {
            LOGGER.error("error closing synthesizer output pool", ex);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("spoken input pool has "
                    + userInputPool.getNumActive() + " active/"
                    + userInputPool.getNumIdle() + " idle objects");
        }
        try {
            userInputPool.close();
        } catch (Exception ex) {
            LOGGER.error("error closing spoken input output pool", ex);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("telephony pool has " + callControlPool.getNumActive()
                    + " active/" + callControlPool.getNumIdle()
                    + " idle objects");
        }
        try {
            callControlPool.close();
        } catch (Exception ex) {
            LOGGER.error("error closing call control pool", ex);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("...implementation platforms closed");
        }
    }
}
