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

package org.jvoicexml.implementation.jvxml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.CallControlProperties;
import org.jvoicexml.ConnectionInformation;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.implementation.AudioSource;
import org.jvoicexml.implementation.CallControlImplementation;
import org.jvoicexml.implementation.CallControlImplementationEvent;
import org.jvoicexml.implementation.CallControlImplementationListener;
import org.jvoicexml.implementation.SystemOutputImplementation;
import org.jvoicexml.implementation.UserInputImplementation;
import org.jvoicexml.xml.srgs.ModeType;

/**
 * Implementation of a {@link CallControlImplementation} resource to be used in desktop/like
 * environments.
 *
 * <p>
 * This implementation of a {@link CallControlImplementation} resource can be used, if there is
 * no telephony support or if the {@link UserInputImplementation} and
 * {@link SystemOutputImplementation} implementations are able to produced communicate
 * directly with the user.
 * </p>
 *
 * @author Dirk Schnelle-Walka
 *
 * @since 0.5.5
 */
public final class DesktopTelephonySupport implements CallControlImplementation {
    /** Logger for this class. */
    private static final Logger LOGGER = LogManager
            .getLogger(DesktopTelephonySupport.class);

    /** Audio format to use for recording. */
    private final AudioFormat recordingAudioFormat;

    /** Registered output listener. */
    private final Collection<CallControlImplementationListener> listener;

    /** Flag if this device is busy. */
    private boolean busy;

    /** Flag if this device is active. */
    private boolean active;

    /** Asynchronous recording of audio. */
    private RecordingThread recording;

    /** Th associated speaker output stream. */
    private SpeakerOutputStream out;

    /**
     * Constructs a new object.
     * 
     * @param format
     *            the audio format to use for recording
     */
    public DesktopTelephonySupport(final AudioFormat format) {
        listener = new java.util.ArrayList<CallControlImplementationListener>();
        recordingAudioFormat = format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activate() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("activated telephony");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "desktop";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() throws NoresourceError {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void passivate() {
        if (recording != null) {
            recording.stopRecording();
            recording = null;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("passivated telephony");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(final ConnectionInformation client) throws IOException {
        synchronized (listener) {
            final Collection<CallControlImplementationListener> copy =
                    new java.util.ArrayList<CallControlImplementationListener>();
            copy.addAll(listener);
            final CallControlImplementationEvent event = new CallControlImplementationEvent(this,
                    CallControlImplementationEvent.ANSWERED);
            for (CallControlImplementationListener current : copy) {
                current.telephonyCallAnswered(event);
            }
        }
        active = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect(final ConnectionInformation client) {
        active = false;
        synchronized (listener) {
            final Collection<CallControlImplementationListener> copy =
                    new java.util.ArrayList<CallControlImplementationListener>();
            copy.addAll(listener);
            final CallControlImplementationEvent event = new CallControlImplementationEvent(this,
                    CallControlImplementationEvent.HUNGUP);
            for (CallControlImplementationListener current : copy) {
                current.telephonyCallHungup(event);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void play(Collection<SystemOutputImplementation> outputs,
            CallControlProperties props) throws NoresourceError, IOException {
        if (!active) {
            throw new NoresourceError("desktop telephony is no longer active");
        }
        if (outputs.isEmpty()) {
            throw new NoresourceError("no outputs provided");
        }
        final SystemOutputImplementation output = outputs.iterator().next();
        if (output instanceof AudioSource) {
            final AudioSource source = (AudioSource) output;
            try {
                play(source);
            } catch (LineUnavailableException e) {
                throw new NoresourceError(e.getMessage(), e);
            }
        }
        busy = true;
        synchronized (listener) {
            final Collection<CallControlImplementationListener> copy =
                    new java.util.ArrayList<CallControlImplementationListener>();
            copy.addAll(listener);
            final CallControlImplementationEvent event = new CallControlImplementationEvent(this,
                    CallControlImplementationEvent.PLAY_STARTED);
            for (CallControlImplementationListener current : copy) {
                current.telephonyMediaEvent(event);
            }
        }
    }

    /**
     * Play backs audio from the given audio source.
     * 
     * @param source
     *            the current audio source
     * @throws LineUnavailableException
     *             error opening the speaker
     * @throws IOException
     *             error starting the speaker line
     * @since 0.7.8
     */
    private void play(final AudioSource source)
            throws LineUnavailableException, IOException {
        final AudioFormat format = source.getAudioFormat();
        out = new SpeakerOutputStream(format);
        out.open();
        source.setOutputStream(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopPlay() throws NoresourceError {
        if (!busy) {
            return;
        }
        busy = false;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new NoresourceError(e.getMessage(), e);
            } finally {
                out = null;
            }
        }
        synchronized (listener) {
            final Collection<CallControlImplementationListener> copy =
                    new java.util.ArrayList<CallControlImplementationListener>();
            copy.addAll(listener);
            final CallControlImplementationEvent event = new CallControlImplementationEvent(this,
                    CallControlImplementationEvent.PLAY_STOPPED);
            for (CallControlImplementationListener current : copy) {
                current.telephonyMediaEvent(event);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void record(Collection<UserInputImplementation> inputs,
            CallControlProperties props) throws NoresourceError, IOException {
        if (!active) {
            throw new NoresourceError("desktop telephony is no longer active");
        }
        if (inputs.isEmpty()) {
            throw new NoresourceError("no inpus provided");
        }
        busy = true;
        synchronized (listener) {
            final Collection<CallControlImplementationListener> copy =
                    new java.util.ArrayList<CallControlImplementationListener>();
            copy.addAll(listener);
            final CallControlImplementationEvent event = new CallControlImplementationEvent(this,
                    CallControlImplementationEvent.RECORD_STARTED);
            for (CallControlImplementationListener current : copy) {
                current.telephonyMediaEvent(event);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AudioFormat getRecordingAudioFormat() {
        return recordingAudioFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startRecording(final UserInputImplementation input,
            final OutputStream stream, final CallControlProperties props)
            throws IOException, NoresourceError {
        if (!active) {
            throw new NoresourceError("desktop telephony is no longer active");
        }
        busy = true;
        try {
            recording = new RecordingThread(stream, recordingAudioFormat);
            recording.start();
        } catch (LineUnavailableException e) {
            throw new IOException(e.getMessage(), e);
        }
        synchronized (listener) {
            final Collection<CallControlImplementationListener> copy =
                    new java.util.ArrayList<CallControlImplementationListener>();
            copy.addAll(listener);
            final CallControlImplementationEvent event = new CallControlImplementationEvent(this,
                    CallControlImplementationEvent.RECORD_STARTED);
            for (CallControlImplementationListener current : copy) {
                current.telephonyMediaEvent(event);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopRecording() throws NoresourceError {
        if (!busy) {
            return;
        }
        busy = false;
        if (recording != null) {
            recording.stopRecording();
            recording = null;
        }
        synchronized (listener) {
            final Collection<CallControlImplementationListener> copy =
                    new java.util.ArrayList<CallControlImplementationListener>();
            copy.addAll(listener);
            final CallControlImplementationEvent event = new CallControlImplementationEvent(this,
                    CallControlImplementationEvent.RECORD_STOPPED);
            for (CallControlImplementationListener current : copy) {
                current.telephonyMediaEvent(event);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(final String dest) throws NoresourceError {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hangup() {
        active = false;
        synchronized (listener) {
            final Collection<CallControlImplementationListener> copy =
                    new java.util.ArrayList<CallControlImplementationListener>();
            copy.addAll(listener);
            final CallControlImplementationEvent event = new CallControlImplementationEvent(this,
                    CallControlImplementationEvent.HUNGUP);
            for (CallControlImplementationListener current : copy) {
                current.telephonyCallHungup(event);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(final CallControlImplementationListener callListener) {
        synchronized (listener) {
            listener.add(callListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(final CallControlImplementationListener callListener) {
        synchronized (listener) {
            listener.add(callListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBusy() {
        return busy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModeType getModeType() {
        return ModeType.VOICE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ModeType> getSupportedOutputModeTypes() {
        // TODO add a cache for the modes
        final Collection<ModeType> modes =
                new java.util.ArrayList<ModeType>();
        modes.add(ModeType.DTMF);
        return modes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ModeType> getSupportedInputModeTypes() {
        // TODO add a cache for the modes
        final Collection<ModeType> modes =
                new java.util.ArrayList<ModeType>();
        modes.add(ModeType.VOICE);
        modes.add(ModeType.DTMF);
        return modes;
    }
}
