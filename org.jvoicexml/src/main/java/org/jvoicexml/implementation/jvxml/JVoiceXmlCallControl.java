/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008-2017 JVoiceXML group - http://jvoicexml.sourceforge.net
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

import javax.sound.sampled.AudioFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.CallControl;
import org.jvoicexml.CallControlProperties;
import org.jvoicexml.SystemOutput;
import org.jvoicexml.UserInput;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.implementation.CallControlImplementation;
import org.jvoicexml.implementation.SystemOutputImplementationProvider;
import org.jvoicexml.implementation.SystemOutputOutputImplementation;
import org.jvoicexml.implementation.UserInputImplementation;
import org.jvoicexml.implementation.UserInputImplementationProvider;
import org.jvoicexml.xml.srgs.ModeType;

/**
 * Basic wrapper for {@link CallControl}. Method calls are forwarded to
 * the {@link CallControlImplementation} implementation.
 *
 * @author Dirk Schnelle-Walka
 * @since 0.6
 */
final class JVoiceXmlCallControl implements CallControl {
    /** Logger instance. */
    private static final Logger LOGGER =
        LogManager.getLogger(JVoiceXmlCallControl.class);

    /** The encapsulated telephony object. */
    private final CallControlImplementation telephony;

    /**
     * Constructs a new object.
     * @param tel encapsulated telephony object.
     */
    JVoiceXmlCallControl(final CallControlImplementation tel) {
        telephony = tel;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation expects that the given output implements
     * {@link SystemOutputImplementationProvider} to retrieve the
     * {@link SystemOutputOutputImplementation} that is needed to trigger the
     * {@link CallControlImplementation} implementation.
     * </p>
     */
    @Override
    public void play(final SystemOutput output,
            final CallControlProperties props)
            throws NoresourceError, IOException {
        if (output instanceof SystemOutputImplementationProvider) {
            final SystemOutputImplementationProvider provider =
                (SystemOutputImplementationProvider) output;
            final SystemOutputOutputImplementation synthesizer =
                provider.getSynthesizedOutput();
            telephony.play(synthesizer, props);
        } else {
            LOGGER.warn("unable to retrieve a synthesized output from "
                    + output);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopPlay() throws NoresourceError {
        telephony.stopPlay();
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
        if (input instanceof UserInputImplementationProvider) {
            final UserInputImplementationProvider provider =
                (UserInputImplementationProvider) input;
            final UserInputImplementation recognizer =
                    provider.getUserInputImplemenation(ModeType.VOICE);
            telephony.record(recognizer, props);
        } else {
            LOGGER.warn("unable to retrieve a recognizer output from "
                    + input);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AudioFormat getRecordingAudioFormat() {
        return telephony.getRecordingAudioFormat();
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
        if (input instanceof UserInputImplementationProvider) {
            final UserInputImplementationProvider provider =
                (UserInputImplementationProvider) input;
            final UserInputImplementation recognizer = provider.getUserInputImplemenation(ModeType.VOICE);
            telephony.startRecording(recognizer, stream, props);
        } else {
            LOGGER.warn("unable to retrieve a recognizer output from "
                    + input);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopRecord() throws NoresourceError {
        telephony.stopRecording();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(final String dest) throws NoresourceError {
        telephony.transfer(dest);
    }

    /**
     * Retrieves the encapsulated telephony object.
     * @return the encapsulated telephony object.
     */
    public CallControlImplementation getTelephony() {
        return telephony;
    }

    /**
     * Checks if the corresponding telephony device is busy.
     * @return <code>true</code> if the telephony devices is busy.
     */
    public boolean isBusy() {
        return telephony.isBusy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hangup() {
        LOGGER.info("terminating telephony");
        if (telephony.isBusy()) {
            try {
                telephony.stopPlay();
                telephony.stopRecording();
            } catch (NoresourceError e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("error stopping telephony", e);
                }
            }
        }

        telephony.hangup();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCallActive() {
        return telephony.isActive();
    }
}
