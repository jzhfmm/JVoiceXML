/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2007-2019 JVoiceXML group - http://jvoicexml.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.jvoicexml.implementation.jvxml;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.DocumentServer;
import org.jvoicexml.Session;
import org.jvoicexml.SessionIdentifier;
import org.jvoicexml.SpeakableText;
import org.jvoicexml.SystemOutput;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.implementation.SystemOutputImplementationListener;
import org.jvoicexml.implementation.SystemOutputImplementationProvider;
import org.jvoicexml.implementation.SystemOutputImplementation;
import org.jvoicexml.xml.srgs.ModeType;
import org.jvoicexml.xml.vxml.BargeInType;

/**
 * Basic wrapper for {@link SystemOutput}.
 *
 * <p>
 * The {@link JVoiceXmlSystemOutput} encapsulates all external resources of
 * that are employed of a same type. 
 * </p>
 *
 * @author Dirk Schnelle-Walka
 * @since 0.6
 */
final class JVoiceXmlSystemOutput
    implements SystemOutput, SystemOutputImplementationProvider {
    /** Logger for this class. */
    private static final Logger LOGGER =
        LogManager.getLogger(JVoiceXmlSystemOutput.class);

    /** The system output devices. */
    private final Collection<SystemOutputImplementation> outputs;

    /**
     * Constructs a new object.
     * @param synthesizer the synthesizer output device.
     * @param currentSession the current session.
     */
    JVoiceXmlSystemOutput(
            final Map<ModeType, SystemOutputImplementation> synthesizers,
            final Session currentSession) {
        outputs = synthesizers.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SystemOutputImplementation> getSystemOutputImplementations() {
        return outputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queueSpeakable(final SpeakableText speakable,
            final SessionIdentifier sessionId, final DocumentServer documentServer)
        throws NoresourceError, BadFetchError {
        for (SystemOutputImplementation output : outputs) {
            output.queueSpeakable(speakable, sessionId, documentServer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOutput(final BargeInType type) throws NoresourceError {
        boolean supportsBargeIn = false;
        for (SystemOutputImplementation output : outputs) {
            if (output.supportsBargeIn()) {
                supportsBargeIn = true; 
                output.cancelOutput(type);
            }
        }
        if (!supportsBargeIn) {
            LOGGER.warn("no implementation platform supports barge-in of type '"
                    + type + "'");
        }
    }

    /**
     * Adds the given listener for output events.
     * @param listener the listener to add
     */
    public void addListener(final SystemOutputImplementationListener listener) {
        for (SystemOutputImplementation output : outputs) {
            output.addListener(listener);
        }
    }

    /**
     * Removes the given listener for output events.
     * @param listener the listener to remove
     */
    public void removeListener(
            final SystemOutputImplementationListener listener) {
        for (SystemOutputImplementation output : outputs) {
            output.removeListener(listener);
        }
    }

    /**
     * Checks if the corresponding output device is busy.
     * @return {@code true} if the output devices is busy.
     */
    public boolean isBusy() {
        for (SystemOutputImplementation output : outputs) {
            if (output.isBusy()) {
                return true;
            }
        }
        return false;
    }
}
