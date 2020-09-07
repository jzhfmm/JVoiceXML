/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008-2019 JVoiceXML group - http://jvoicexml.sourceforge.net
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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.jvoicexml.ConnectionInformation;
import org.jvoicexml.DtmfRecognizerProperties;
import org.jvoicexml.SpeechRecognizerProperties;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.event.error.UnsupportedFormatError;
import org.jvoicexml.event.error.UnsupportedLanguageError;
import org.jvoicexml.event.plain.implementation.RecognitionStartedEvent;
import org.jvoicexml.event.plain.implementation.RecognitionStoppedEvent;
import org.jvoicexml.event.plain.implementation.UserInputEvent;
import org.jvoicexml.implementation.GrammarImplementation;
import org.jvoicexml.implementation.UserInputImplementation;
import org.jvoicexml.implementation.UserInputImplementationListener;
import org.jvoicexml.interpreter.datamodel.DataModel;
import org.jvoicexml.xml.srgs.GrammarType;
import org.jvoicexml.xml.srgs.ModeType;
import org.jvoicexml.xml.vxml.BargeInType;

/**
 * This class provides a dummy {@link UserInputImplementation} for testing
 * purposes.
 *
 * @author Dirk Schnelle-Walka
 * @since 0.6
 */
public final class MockUsetInputImplementation
    implements UserInputImplementation {
    /** Registered output listener. */
    private final Collection<UserInputImplementationListener> listener;

    /** Flag, if recognition is turned on. */
    private boolean recognizing;

    /**
     * Constructs a new object.
     */
    public MockUsetInputImplementation() {
        listener = new java.util.ArrayList<UserInputImplementationListener>();
    }

    /**
     * {@inheritDoc}
     */
    public void activateGrammars(
            final Collection<GrammarImplementation<?>> grammars)
            throws BadFetchError, UnsupportedLanguageError, NoresourceError {
    }

    /**
     * {@inheritDoc}
     */
    public void deactivateGrammars(
            final Collection<GrammarImplementation<?>> grammars)
            throws NoresourceError, BadFetchError {
    }

    /**
     * {@inheritDoc}
     */
    public Collection<BargeInType> getSupportedBargeInTypes() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<GrammarType> getSupportedGrammarTypes() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public URI getUriForNextSpokenInput() throws NoresourceError {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public GrammarImplementation<?> loadGrammar(
            final URI uri, final GrammarType type)
            throws NoresourceError, IOException, UnsupportedFormatError {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void activate() {
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return "dummy";
    }

    /**
     * {@inheritDoc}
     */
    public void open() throws NoresourceError {
    }

    /**
     * {@inheritDoc}
     */
    public void passivate() {
    }

    /**
     * {@inheritDoc}
     */
    public void connect(final ConnectionInformation client) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect(final ConnectionInformation client) {
    }

    /**
     * {@inheritDoc}
     */
    public void startRecognition(final DataModel model,
            final SpeechRecognizerProperties speech,
            final DtmfRecognizerProperties dtmf)
        throws NoresourceError, BadFetchError {
        recognizing = true;
        final UserInputEvent event =
            new RecognitionStartedEvent(this, null);
        fireInputEvent(event);
    }

    /**
     * {@inheritDoc}
     */
    public void stopRecognition() {
        recognizing = false;
        final UserInputEvent event =
            new RecognitionStoppedEvent(this, null);
        fireInputEvent(event);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBusy() {
       return recognizing;
    }

    /**
     * {@inheritDoc}
     */
    public void addListener(
            final UserInputImplementationListener inputListener) {
       synchronized (listener) {
           listener.add(inputListener);
       }
    }

    /**
     * {@inheritDoc}
     */
    public void removeListener(
            final UserInputImplementationListener inputListener) {
        synchronized (listener) {
            listener.remove(inputListener);
        }
    }

    /**
     * Notifies all registered listeners about the given event.
     * @param event the event.
     * @since 0.6
     */
    void fireInputEvent(final UserInputEvent event) {
        synchronized (listener) {
            final Collection<UserInputImplementationListener> copy =
                new java.util.ArrayList<UserInputImplementationListener>();
            copy.addAll(listener);
            for (UserInputImplementationListener current : copy) {
                current.inputStatusChanged(event);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModeType getModeType() {
        return ModeType.VOICE;
    }
}