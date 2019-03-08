/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2006-2017 JVoiceXML group - http://jvoicexml.sourceforge.net
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
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.DtmfInput;
import org.jvoicexml.DtmfRecognizerProperties;
import org.jvoicexml.GrammarDocument;
import org.jvoicexml.SpeechRecognizerProperties;
import org.jvoicexml.UserInput;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.event.error.UnsupportedFormatError;
import org.jvoicexml.event.error.UnsupportedLanguageError;
import org.jvoicexml.implementation.GrammarImplementation;
import org.jvoicexml.implementation.UserInputImplementation;
import org.jvoicexml.implementation.UserInputImplementationListener;
import org.jvoicexml.implementation.UserInputImplementationProvider;
import org.jvoicexml.implementation.grammar.GrammarCache;
import org.jvoicexml.implementation.grammar.LoadedGrammar;
import org.jvoicexml.interpreter.datamodel.DataModel;
import org.jvoicexml.xml.srgs.GrammarType;
import org.jvoicexml.xml.srgs.ModeType;
import org.jvoicexml.xml.vxml.BargeInType;

/**
 * Basic wrapper for {@link UserInput}.
 * 
 * <p>
 * The {@link UserInput} encapsulates two external resources. A basic
 * implementation for the {@link DtmfInput} is provided by the interpreter. The
 * unknown resource is the spoken input, which must be obtained from a resource
 * pool. This class combines these two as the {@link UserInput} which is been
 * used by the rest of the interpreter.
 * </p>
 * 
 * @author Dirk Schnelle-Walka
 * @since 0.5
 */
final class JVoiceXmlUserInput implements UserInput, UserInputImplementationProvider {
    /** Logger for this class. */
    private static final Logger LOGGER = LogManager
            .getLogger(JVoiceXmlUserInput.class);

    /** The known user inputs */
    private final Map<ModeType, UserInputImplementation> inputs;

    /** The cache of already processed grammars. */
    private final GrammarCache cache;

    /**
     * Constructs a new object.
     * 
     * @param inputs
     *            the spoken input implementation.s
     * @param dtmf
     *            the buffered character input.
     */
    JVoiceXmlUserInput(final Map<ModeType, UserInputImplementation> userInputs) {
        inputs = userInputs;
        cache = new GrammarCache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInputImplementation getUserInputImplemenation(ModeType mode) {
        return inputs.get(mode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<UserInputImplementation> getUserInputImplementations() {
        return inputs.values();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int activateGrammars(final Collection<GrammarDocument> grammars)
            throws BadFetchError, UnsupportedLanguageError, NoresourceError,
            UnsupportedFormatError {
        int loadedGrammars = 0;
        for (ModeType mode : inputs.keySet()) {
            final Collection<GrammarImplementation<?>> modeGramamrs =
                    new java.util.ArrayList<GrammarImplementation<?>>();
            final UserInputImplementation input = inputs.get(mode);
            for (GrammarDocument grammar : grammars) {
                final GrammarImplementation<?> grammarImplementation = loadGrammar(
                        grammar);
                modeGramamrs.add(grammarImplementation);
            }
            input.activateGrammars(modeGramamrs);
            loadedGrammars += modeGramamrs.size();
        }

        return loadedGrammars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deactivateGrammars(final Collection<GrammarDocument> grammars)
            throws NoresourceError, BadFetchError {
        int unloadedGrammars = 0;
        for (ModeType mode : inputs.keySet()) {
            final Collection<GrammarImplementation<?>> modeGramamrs =
                    new java.util.ArrayList<GrammarImplementation<?>>();
            final UserInputImplementation input = inputs.get(mode);
            for (GrammarDocument grammar : grammars) {
                GrammarImplementation<?> grammarImplementation =
                        cache.getImplementation(grammar);
                if (grammarImplementation == null) {
                    LOGGER.warn("no implementation for grammar " + grammar);
                    continue;
                }
                modeGramamrs.add(grammarImplementation);
            }
            input.deactivateGrammars(modeGramamrs);
            unloadedGrammars += modeGramamrs.size();
        }

        return unloadedGrammars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<BargeInType> getSupportedBargeInTypes() {
        final Collection<BargeInType> types =
                new java.util.ArrayList<BargeInType>();
        for (UserInputImplementation input : inputs.values()) {
            final Collection<BargeInType> current = 
                    input.getSupportedBargeInTypes();
            if (current == null) {
                LOGGER.warn("input '" + input.getClass().getCanonicalName() + "' does not support any barge-in types");
            } else {
                types.addAll(current);
            }
        }
        return types;
    }

    /**
     * Creates a {@link GrammarImplementation} from the contents provided by the
     * reader. If the grammar contained in the reader already exists, it is
     * over-written.
     *
     * <p>
     * This method is mainly needed for non SRGS grammars, e.g. JSGF. Loading an
     * SRGS grammar is quite easy and can be implemented e.g. as
     * </p>
     * <p>
     * <code>
     * final InputSource inputSource = new InputSource(reader);<br>
     * SrgsXmlDocument doc = new SrgsXmlDocument(inputSource);<br>
     * &#47;&#47; Pass it to the recognizer<br>
     * return doc;
     * </code>
     * </p>
     *
     * @param document
     *            the grammar to read. The type is one of the supported types of
     *            the implementation, that has been requested via
     *            {@link #getSupportedGrammarTypes(ModeType)}.
     *
     * @return Read grammar.
     *
     * @since 0.3
     *
     * @exception NoresourceError
     *                The input resource is not available.
     * @exception BadFetchError
     *                Error reading the grammar.
     * @exception UnsupportedFormatError
     *                Invalid grammar format.
     */
    private GrammarImplementation<?> loadGrammar(final GrammarDocument document)
            throws NoresourceError, BadFetchError, UnsupportedFormatError {
        final URI uri = document.getURI();

        // Check if the grammar has already been loaded
        if (cache.contains(document)) {
            LOGGER.info("grammar from '" + uri + "' already loaded");
            return cache.getImplementation(document);
        }

        // Actually load and cache the grammar
        final GrammarType type = document.getMediaType();
        final ModeType mode = document.getModeType();
        final UserInputImplementation input = inputs.get(mode);
        if (input == null) {
            throw new NoresourceError("No input known for mode ' + mode + "
                    + "' to load grammar " + document.getURI());
        }
        try {
            LOGGER.info("loading '" + type + "' grammar from '" + uri + "'");
            final GrammarImplementation<?> implementation =
                    input.loadGrammar(uri, type);
            final LoadedGrammar loaded = new LoadedGrammar(document,
                    implementation);
            cache.add(loaded);
            return implementation;
        } catch (IOException e) {
            throw new BadFetchError(e.getMessage(), e);
        }
    }

    /**
     * Adds the listener to all known {@link UserInputImplementation}s.
     * @param listener the listener to add
     */
    public void addListener(final UserInputImplementationListener listener) {
        for (UserInputImplementation input : inputs.values()) {
            input.addListener(listener);
        }
    }

    /**
     * Removes the listener from all known {@link UserInputImplementation}s.
     * @param listener the listener to remove
     */
    public void removeListener(final UserInputImplementationListener listener) {
        for (UserInputImplementation input : inputs.values()) {
            input.removeListener(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startRecognition(final DataModel model,
            final Collection<ModeType> types,
            final SpeechRecognizerProperties speech,
            final DtmfRecognizerProperties dtmf)
            throws NoresourceError, BadFetchError {
        for (ModeType type : types) {
            final UserInputImplementation input = inputs.get(type);
            if (input != null) {
                input.startRecognition(model, speech, dtmf);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopRecognition(final Collection<ModeType> types) {
        for (ModeType type : types) {
            final UserInputImplementation input = inputs.get(type);
            if (input != null) {
                input.stopRecognition();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<GrammarType> getSupportedGrammarTypes(
            final ModeType mode) {
        final Collection<GrammarType> types =
                new java.util.ArrayList<GrammarType>();
        for (UserInputImplementation input : inputs.values()) {
            final Collection<GrammarType> current = 
                    input.getSupportedGrammarTypes();
            types.addAll(current);
        }
        return types;
    }

    /**
     * Checks if the corresponding input device is busy.
     * 
     * @return <code>true</code> if the input devices is busy.
     */
    public boolean isBusy() {
        for (UserInputImplementation input : inputs.values()) {
            if (input.isBusy()) {
                return true;
            }
        }
        return false;
    }
}
