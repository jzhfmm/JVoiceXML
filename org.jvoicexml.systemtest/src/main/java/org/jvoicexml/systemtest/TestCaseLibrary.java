/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008-2018 JVoiceXML group - http://jvoicexml.sourceforge.net
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

package org.jvoicexml.systemtest;

import java.io.IOException;
import java.util.Set;


/**
 * Test case library..
 * @author lancer
 *
 */
public interface TestCaseLibrary {

    /**
     * Retrieves the total number of tests in this library.
     * @return total number of test case in library.
     * @throws IOException
     *         error loading the test cases
     */
    int size();

    /**
     * Fetch test case by expression.
     * @param testcases expression of test cases;
     * @return test case set
     * @throws IOException
     *         error loading the test cases
     */
    Set<TestCase> fetch(final String testcases) throws IOException;

}
