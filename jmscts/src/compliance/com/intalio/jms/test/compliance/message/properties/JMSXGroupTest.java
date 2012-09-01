/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact jima@intalio.com.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: JMSXGroupTest.java,v 1.6 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.message.properties;

import java.util.Enumeration;
import java.util.HashSet;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.ClassHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests that connections support JMSXGroupID and JMSXGroupSeq
 * properties
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>properties.group</li>
 * </ul>
 * NOTE: the specification is not clear on the behaviour of null values
 * for these properties.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractMessageTestCase
 * @see org.exolab.jmscts.core.MessageTestRunner
 */
public class JMSXGroupTest extends AbstractMessageTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testJMSXGroupID", "properties.group"},
        {"testJMSXGroupSeq", "properties.group"}};

    private static final String GROUP_ID = "JMSXGroupID";
    private static final String GROUP_SEQ = "JMSXGroupSeq";

    private static final Object[] INVALID_GROUP_ID_VALUES = {
        Boolean.TRUE, new Byte(Byte.MIN_VALUE),new Short(Short.MIN_VALUE), 
        new Character(Character.MIN_VALUE), new Integer(Integer.MIN_VALUE),
        new Float(Float.MIN_VALUE), new Double(Double.MIN_VALUE)};

    private static final Object[] INVALID_GROUP_SEQ_VALUES = {
        Boolean.TRUE, new Byte(Byte.MIN_VALUE),new Short(Short.MIN_VALUE), 
        new Character(Character.MIN_VALUE), new Float(Float.MIN_VALUE), 
        new Double(Double.MIN_VALUE), "abc"};

    /**
     * Create an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public JMSXGroupTest(String name) {
        super(name, REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner test = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(test);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by 
     * {@link JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(JMSXGroupTest.class);
    }

    /**
     * Test that the only allowed type for JMSXGroupID is a String.
     * This covers requirements:
     * <ul>
     *   <li>properties.group</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testJMSXGroupID() throws Exception {
        Message message = getContext().getMessage();
        for (int i = 0; i < INVALID_GROUP_ID_VALUES.length; ++i) {
            Object value = INVALID_GROUP_ID_VALUES[i];
            checkProperty(message, GROUP_ID, value);
        }
    }

    /**
     * Test that the only allowed type for JMSXGroupID is an int.
     * This covers requirements:
     * <ul>
     *   <li>properties.group</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testJMSGroupSeq() throws Exception {
        Message message = getContext().getMessage();
        for (int i = 0; i < INVALID_GROUP_SEQ_VALUES.length; ++i) {
            Object value = INVALID_GROUP_SEQ_VALUES[i];
            checkProperty(message, GROUP_SEQ, value);
        }
    }

    /**
     * Test that JMSXGroupSeq only handles ints > 0
     *
     * @throws Exception for any error
     */
    public void testJMSGroupSeqRange() throws Exception {
        Message message = getContext().getMessage();
        checkSequenceValue(message, -1, false);
        checkSequenceValue(message, 0, false);
        checkSequenceValue(message, 1, true);
        checkSequenceValue(message, Integer.MAX_VALUE, true);
    }

    private void checkProperty(Message message, String name, Object value) 
        throws Exception {

        // javax.jms.Message doesn't provide an interface for populating
        // char primitives. The setObjectProperty method should throw
        // MessageFormatException for Character instances.

        if (!(value instanceof Character)) {
            try {
                PropertyHelper.setPrimitiveProperty(message, name, value);
                fail("Managed to use invalid type=" + 
                     ClassHelper.getPrimitiveName(value.getClass()) + 
                     " for property=" + name);
            } catch (MessageFormatException ignore) {
            } catch (Exception exception) {
                fail("Expected MessageFormatException to be thrown when " +
                     "setting property=" + name + " with type=" +
                     ClassHelper.getPrimitiveName(value.getClass()) + 
                     " but got exception=" + exception.getClass().getName() +
                     ", message=" + exception.getMessage());
            }
        }

        try {
            message.setObjectProperty(name, value);
            fail("Managed to use invalid type=" + 
                 ClassHelper.getPrimitiveName(value.getClass()) + 
                 " for property=" + name);
        } catch (MessageFormatException ignore) {
        } catch (Exception exception) {
            fail("Expected MessageFormatException to be thrown when " +
                 "setting property=" + name + " with type=" +
                 ClassHelper.getPrimitiveName(value.getClass()) + 
                 " but got exception=" + exception.getClass().getName() +
                 ", message=" + exception.getMessage());
        }
    }

    private void checkSequenceValue(Message message, int value, boolean valid)
        throws Exception {

        checkSequenceValue(message, value, valid, true);
        checkSequenceValue(message, value, valid, false);
    }

    private void checkSequenceValue(Message message, int value, boolean valid,
                                    boolean primitive) throws Exception {
        try {
            if (primitive) {
                message.setIntProperty(GROUP_SEQ, value);
            } else {
                message.setObjectProperty(GROUP_SEQ, new Integer(value));
            }
            if (!valid) {
                fail("Managed to use invalid int=" + value +
                     " for property=" + GROUP_SEQ);
            }
        } catch (JMSException exception) {
            if (valid) {
                fail("Valid int value=" + value + " for property=" + 
                     GROUP_SEQ + " threw exception=" + exception.getClass() +
                     ", message=" + exception.getMessage());
            }
        } catch (Exception exception) {
            if (valid) {
                fail("Valid int value=" + value + " for property=" + 
                     GROUP_SEQ + " threw exception=" + exception.getClass() +
                     ", message=" + exception.getMessage());
            } else {
                fail("Expected JMSException to be thrown when " +
                     "setting property=" + GROUP_SEQ + " with value=" + value +
                     " but got exception=" + exception.getClass().getName() +
                     ", message=" + exception.getMessage());
            }
        }
    }
    
} //-- JMSXGroupTest