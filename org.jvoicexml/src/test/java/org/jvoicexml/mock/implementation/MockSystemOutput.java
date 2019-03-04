/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008-2015 JVoiceXML group - http://jvoicexml.sourceforge.net
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
package org.jvoicexml.mock.implementation;

import java.util.Collection;

import org.jvoicexml.DocumentServer;
import org.jvoicexml.SpeakableText;
import org.jvoicexml.SystemOutput;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.event.plain.implementation.OutputEndedEvent;
import org.jvoicexml.event.plain.implementation.OutputStartedEvent;
import org.jvoicexml.event.plain.implementation.QueueEmptyEvent;
import org.jvoicexml.event.plain.implementation.SystemOutputEvent;
import org.jvoicexml.implementation.SystemOutputImplementation;
import org.jvoicexml.implementation.SystemOutputImplementationListener;
import org.jvoicexml.xml.vxml.BargeInType;

/**
 * This class provides a dummy implementation of a {@link SystemOutput} for
 * testing purposes.
 *
 * @author Dirk Schnelle-Walka
 * @since 0.6
 */
public final class MockSystemOutput implements SystemOutput {
    /** Registered output listener. */
    private final Collection<SystemOutputImplementationListener> listener;

    /** The current speakable. */
    private SpeakableText speakable;

    /** The encapsulated synthesized output. */
    private final SystemOutputImplementation output;

    /** the session id. */
    private String sessionId;

    /**
     * Constructs a new object.
     */
    public MockSystemOutput() {
        this(null);
    }

    /**
     * Constructs a new object.
     * @param synthesizedOutput the encapsulated synthesized output.
     */
    public MockSystemOutput(final SystemOutputImplementation synthesizedOutput) {
        listener = new java.util.ArrayList<SystemOutputImplementationListener>();
        output = synthesizedOutput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOutput(final BargeInType type) throws NoresourceError {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queueSpeakable(final SpeakableText speakableText,
            final String id, final DocumentServer documentServer)
        throws NoresourceError, BadFetchError {
        speakable = speakableText;
        sessionId = id;
        final SystemOutputEvent event =
            new OutputStartedEvent(null, sessionId, speakable);
        fireOutputEvent(event);
    }

    /**
     * Simulates the end of an output.
     */
    public void outputEnded() {
        final SystemOutputEvent endedEvent =
            new OutputEndedEvent(null, sessionId, speakable);
        fireOutputEvent(endedEvent);
        speakable = null;
        final SystemOutputEvent emptyEvent =
            new QueueEmptyEvent(null, sessionId);
        fireOutputEvent(emptyEvent);
    }

    /**
     * {@inheritDoc}
     */
    public void addListener(
            final SystemOutputImplementationListener outputListener) {
        if (outputListener == null) {
            return;
        }
        synchronized (listener) {
            listener.add(outputListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeListener(
            final SystemOutputImplementationListener outputListener) {
        synchronized (listener) {
            listener.remove(outputListener);
        }
    }

    /**
     * Notifies all registered listeners about the given event.
     * @param event the event.
     * @since 0.6
     */
    private void fireOutputEvent(final SystemOutputEvent event) {
        synchronized (listener) {
            final Collection<SystemOutputImplementationListener> copy =
                new java.util.ArrayList<SystemOutputImplementationListener>();
            copy.addAll(listener);
            for (SystemOutputImplementationListener current : copy) {
                current.outputStatusChanged(event);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public SystemOutputImplementation getSynthesizedOutput() throws NoresourceError {
        return output;
    }
}
