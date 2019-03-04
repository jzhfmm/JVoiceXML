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
 * A platform factory provides access to all resources of a custom
 * {@link org.jvoicexml.ImplementationPlatform}.
 *
 * <p>
 * Implementations provide a means of grouping a custom set of
 * {@link ResourceFactory}s.
 * </p>
 *
 * @author Dirk Schnelle
 * @since 0.6
 */
public interface PlatformFactory {

    /**
     * Retrieves the spoken input implementation.
     * @return the spokenInputFactory
     */
    ResourceFactory<UserInputImplementation> getSpokeninput();

    /**
     * Retrieves the synthesized output implementation.
     * @return the synthesizedOutputFactory
     */
    ResourceFactory<SystemOutputImplementation> getSynthesizedoutput();

    /**
     * Retrieves the telephonyFactory implementation.
     * @return the telephonyFactory
     */
    ResourceFactory<CallControlImplementation> getTelephony();
}
