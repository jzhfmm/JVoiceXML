/*
 * File:    $HeadURL$
 * Version: $LastChangedRevision$
 * Date:    $Date$
 * Author:  $LastChangedBy$
 *
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2006-2014 JVoiceXML group - http://jvoicexml.sourceforge.net
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

package org.jvoicexml.xml.srgs;

import java.util.ServiceLoader;

/**
 * The mode of a grammar indicates the type of input that the user agent should
 * be detecting.
 * 
 * @author Dirk Schnelle-Walka
 * @version $Revision$
 * @since 0.6
 */
public final class ModeType {
    /**
     * Voice input.
     */
    public static ModeType VOICE = new ModeType("voice");

    /**
     * DTMF input.
     */
    public static ModeType DTMF = new ModeType("dtmf");

    /**
     * External input.
     */
    public static ModeType EXTERNAL = new ModeType("external");

    /** Name of the mode. */
    private final String mode;

    /**
     * Do not create from outside.
     * 
     * @param name
     *            name of the mode.
     */
    private ModeType(final String name) {
        mode = name;
    }

    /**
     * Retrieves the name of this barge-in type.
     * 
     * @return Name of this type.
     */
    public String getMode() {
        return mode;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return mode;
    }

    /**
     * Converts the given value of the attribute into a
     * {@code ModeType} object. If the attribute can not be
     * resolved, an {@link IllegalArgumentException} is thrown.
     *
     * @param attribute Value of the attribute as it is specified in
     *        a {@link Grammar} type.
     * @return corresponding {@code ModeType} object.
     * @since 0.6
     */
    public static final ModeType valueOfAttribute(final String attribute) {
        // First, check if there is an externally defined grammar
        final ServiceLoader<ModeTypeFactory> factories =
            ServiceLoader.load(ModeTypeFactory.class);
        for (ModeTypeFactory factory : factories) {
            final ModeType type = factory.getGrammarType(attribute);
            if (type != null) {
                return type;
            }
        }
        
        // If there is none, try it with internal modes
        final JVoiceXmlModeTypeFactory factory =
            new JVoiceXmlModeTypeFactory();
        final ModeType type = factory.getGrammarType(attribute);
        if (type != null) {
            return type;
        }
        throw new IllegalArgumentException("Unable to determine the mode"
                + " type for '" + attribute + "'");
    }   
}
