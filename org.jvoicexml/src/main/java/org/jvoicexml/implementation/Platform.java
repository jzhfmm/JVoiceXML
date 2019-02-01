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

package org.jvoicexml.implementation;

/**
 * Basic implementation of a {@link PlatformFactory}.
 *
 * @author Dirk Schnelle-Walka
 * @since 0.6
 */
public final class Platform implements PlatformFactory {
    /** The used spoken input. */
    private ResourceFactory<UserInputImplementation> spokenInputFactory;

    /** The used synthesized output. */
    private ResourceFactory<SystemOutputOutputImplementation> synthesizedOutputFactory;

    /** The used telephonyFactory implementation. */
    private ResourceFactory<CallControlImplementation> telephonyFactory;

    /**
     * {@inheritDoc}
     */
    public ResourceFactory<UserInputImplementation> getSpokeninput() {
        return spokenInputFactory;
    }

    /**
     * Sets the spoken input implementation.
     * @param input the spokenInputFactory to set
     */
    public void setSpokeninput(final ResourceFactory<UserInputImplementation> input) {
        spokenInputFactory = input;
    }

    /**
     * {@inheritDoc}
     */
    public ResourceFactory<SystemOutputOutputImplementation> getSynthesizedoutput() {
        return synthesizedOutputFactory;
    }

    /**
     * Sets the synthesized output implementation.
     * @param output the synthesizedOutputFactory to set
     */
    public void setSynthesizedoutput(
            final ResourceFactory<SystemOutputOutputImplementation> output) {
        synthesizedOutputFactory = output;
    }

    /**
     * {@inheritDoc}
     */
    public ResourceFactory<CallControlImplementation> getTelephony() {
        return telephonyFactory;
    }

    /**
     * Sets the telephonyFactory implementation.
     * @param tel the telephonyFactory to set
     */
    public void setTelephony(final ResourceFactory<CallControlImplementation> tel) {
        telephonyFactory = tel;
    }
}
