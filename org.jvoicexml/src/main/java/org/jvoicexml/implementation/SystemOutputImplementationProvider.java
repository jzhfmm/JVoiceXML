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

package org.jvoicexml.implementation;

import java.util.Collection;

import org.jvoicexml.event.error.NoresourceError;

/**
 * Objects that implement this class have an association to a
 * {@link SystemOutputOutputImplementation} device which can be retrieved by the corresponding
 * method.
 * *
 * @author Dirk Schnelle-Walka
 * @since 0.6
 */
public interface SystemOutputImplementationProvider {
    /**
     * Retrieves the currently employed
     * {@link SystemOutputOutputImplementation}s.
     * @return used system outputs.
     * @throws NoresourceError
     *         Error obtaining the system output.
     */
    Collection<SystemOutputOutputImplementation> getSystemOutputImplementations()
            throws NoresourceError;
}
