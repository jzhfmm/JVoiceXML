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
    private ResourceFactory<UserInputImplementation> userInputFactory;

    /** The used synthesized output. */
    private ResourceFactory<SystemOutputImplementation> systemOutputFactory;

    /** The used telephonyFactory implementation. */
    private ResourceFactory<CallControlImplementation> callControlFactory;

    /**
     * {@inheritDoc}
     */
    public ResourceFactory<UserInputImplementation> getUserinputImplemetation() {
        return userInputFactory;
    }

    /**
     * Sets the user input implementation.
     * @param input the spokenInputFactory to set
     */
    public void setUserinputImplementation(final ResourceFactory<UserInputImplementation> input) {
        userInputFactory = input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceFactory<SystemOutputImplementation> getSystemoutputImplementation() {
        return systemOutputFactory;
    }

    /**
     * Sets the system output implementation.
     * @param output the synthesizedOutputFactory to set
     */
    public void setSystemOutputImplementation(
            final ResourceFactory<SystemOutputImplementation> output) {
        systemOutputFactory = output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceFactory<CallControlImplementation> getCallControlImplementation() {
        return callControlFactory;
    }

    /**
     * Sets the call control implementation.
     * @param tel the telephonyFactory to set
     */
    public void setCallControlImplemetnation(final ResourceFactory<CallControlImplementation> tel) {
        callControlFactory = tel;
    }
}
