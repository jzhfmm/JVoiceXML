/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008-2019 JVoiceXML group - http://jvoicexml.sourceforge.net
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.CallControl;
import org.jvoicexml.CallControlProperties;
import org.jvoicexml.SystemOutput;
import org.jvoicexml.UserInput;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.implementation.CallControlImplementation;
import org.jvoicexml.implementation.CallControlImplementationListener;
import org.jvoicexml.implementation.SystemOutputImplementationProvider;
import org.jvoicexml.implementation.SystemOutputImplementation;
import org.jvoicexml.implementation.UserInputImplementation;
import org.jvoicexml.implementation.UserInputImplementationProvider;
import org.jvoicexml.xml.srgs.ModeType;

/**
 * Basic wrapper for {@link CallControl}. Method calls are forwarded to
 * the {@link CallControlImplementation} implementation for their supported
 * {@link ModeType}s.
 *
 * @author Dirk Schnelle-Walka
 * @since 0.6
 */
public final class JVoiceXmlCallControl implements CallControl {
    /** Logger instance. */
    private static final Logger LOGGER =
        LogManager.getLogger(JVoiceXmlCallControl.class);

    /** The encapsulated telephony object. */
    private final Collection<CallControlImplementation> callcontrols;

    /**
     * Constructs a new object.
     * @param controls encapsulated call controls
     */
    JVoiceXmlCallControl(final Collection<CallControlImplementation> controls) {
        callcontrols = controls;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation expects that the given output implements
     * {@link SystemOutputImplementationProvider} to retrieve the
     * {@link SystemOutputImplementation} that is needed to trigger the
     * {@link CallControlImplementation} implementation.
     * </p>
     */
    @Override
    public void play(SystemOutput output,
            final CallControlProperties props)
            throws NoresourceError, IOException {
        for (CallControlImplementation call : callcontrols) {
            final Collection<SystemOutputImplementation> relevant =
                    getRelavantSystemOutputImplementations(call, output);
            call.play(relevant, props);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopPlay() throws NoresourceError {
        for (CallControlImplementation call : callcontrols) {
            call.stopPlay();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation expects that the given output implements
     * {@link UserInputImplementationProvider} to retrieve the
     * {@link UserInputImplementation} that is needed to trigger the
     * {@link CallControlImplementation} implementation.
     * </p>
     */
    @Override
    public void record(final UserInput input,
            final CallControlProperties props)
            throws NoresourceError, IOException {
        for (CallControlImplementation call : callcontrols) {
            final Collection<UserInputImplementation> relevant =
                    getRelavantUserInputImplementations(call, input);
            call.record(relevant, props);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AudioFormat getRecordingAudioFormat() {
        // TODO find a proper way to handle potentially multiple audio formats
        for (CallControlImplementation call : callcontrols) {
            return call.getRecordingAudioFormat();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation expects that the given output implements
     * {@link UserInputImplementationProvider} to retrieve the
     * {@link UserInputImplementation} that is needed to trigger the
     * {@link CallControlImplementation} implementation.
     * </p>
     */
    @Override
    public void startRecording(final UserInput input, final OutputStream stream,
            final CallControlProperties props)
            throws NoresourceError, IOException {
        // TODO find a proper way to handle potentially multiple streams
        for (CallControlImplementation call : callcontrols) {
            final Collection<UserInputImplementation> relevantInputs =
                    getRelavantUserInputImplementations(call, input);
            for (UserInputImplementation current : relevantInputs) {
                call.startRecording(current, stream, props);
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopRecord() throws NoresourceError {
        for (CallControlImplementation call : callcontrols) {
            call.stopRecording();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(final String dest) throws NoresourceError {
        // TODO find a better way to handle the transfer and select the correct one
        for (CallControlImplementation call : callcontrols) {
            call.transfer(dest);
        }
    }

    /**
     * Retrieves the encapsulated telephony object.
     * @return the encapsulated telephony object.
     */
    public Collection<CallControlImplementation> getCallControlImplementations() {
        return callcontrols;
    }

    /**
     * Checks if the corresponding telephony device is busy.
     * @return <code>true</code> if the telephony devices is busy.
     */
    public boolean isBusy() {
        for (CallControlImplementation call : callcontrols) {
            if (call.isBusy()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hangup() {
        LOGGER.info("terminating telephony");
        for (CallControlImplementation call : callcontrols) {
            if (call.isBusy()) {
                try {
                    call.stopPlay();
                    call.stopRecording();
                } catch (NoresourceError e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("error stopping telephony", e);
                    }
                }
            }
            
            call.hangup();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCallActive() {
        for (CallControlImplementation call : callcontrols) {
            if (call.isActive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the listener to all known {@link CallControlImplementation}s.
     * @param listener the listener to add
     */
    public void addListener(final CallControlImplementationListener listener) {
        for (CallControlImplementation call : callcontrols) {
            call.addListener(listener);
        }
    }

    /**
     * Removes the listener from all known {@link CallControlImplementation}s.
     * @param listener the listener to remove
     */
    public void removeListener(final CallControlImplementationListener listener) {
        for (CallControlImplementation call : callcontrols) {
            call.removeListener(listener);
        }
    }

    /**
     * Retrieves a list of relevant {@link SystemOutputImplementation}s
     * form the given {@link CallControlImplementation}.
     * @param call the call control to investigate
     * @param output the the system output provider
     * @param modes the modes to check for
     * @return matching implementations for the mode
     * @since 0.7.9
     */
    private Collection<SystemOutputImplementation> getRelavantSystemOutputImplementations(
            final CallControlImplementation call, final SystemOutput output) {
        // TODO add a caching to do this only once
        final Collection<SystemOutputImplementation> relevant =
                new java.util.ArrayList<SystemOutputImplementation>();
        if (output instanceof SystemOutputImplementationProvider) {
            final SystemOutputImplementationProvider provider =
                (SystemOutputImplementationProvider) output;
            final Collection<SystemOutputImplementation> outputs =
                    provider.getSystemOutputImplementations();            
            final Collection<ModeType> modes =
                    call.getSupportedInputModeTypes();
            for (SystemOutputImplementation current : outputs) {
                final ModeType currentMode = current.getModeType();
                if (modes.contains(currentMode)) {
                    relevant.add(current);
                }
            }
        } else {
            LOGGER.warn("unable to retrieve a system output from "
                    + output);
        }
        return relevant;
    }

    /**
     * Retrieves a list of relevant {@link SystemOutputImplementation}s
     * form the given provider for the specified mode.
     * @param call the call control to investigate
     * @param input the the system output provider
     * @return matching implementations for the mode
     * @since 0.7.9
     */
    private Collection<UserInputImplementation> getRelavantUserInputImplementations(
            final CallControlImplementation call, final UserInput input) {
        // TODO add a caching to do this only once
        final Collection<UserInputImplementation> relevant =
                new java.util.ArrayList<UserInputImplementation>();
        if (input instanceof UserInputImplementationProvider) {
            final UserInputImplementationProvider provider =
                (UserInputImplementationProvider) input;
            final Collection<UserInputImplementation> inputs =
                provider.getUserInputImplementations();
            final Collection<ModeType> modes =
                    call.getSupportedInputModeTypes();
            for (UserInputImplementation current : inputs) {
                final ModeType currentMode = current.getModeType();
                if (modes.contains(currentMode)) {
                    relevant.add(current);
                }
            }
        } else {
            LOGGER.warn("unable to retrieve a system output from "
                    + input);
        }
        return relevant;
    }
    
}
