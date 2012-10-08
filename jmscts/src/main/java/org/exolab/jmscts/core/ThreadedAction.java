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
 *    please contact tma@netspace.net.au.
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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: ThreadedAction.java,v 1.6 2005/06/16 06:25:35 tanderson Exp $
 */
package org.exolab.jmscts.core;

import org.apache.log4j.Logger;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;


/**
 * Helper class to run an action in a separate thread, and catch any
 * exception that the action generates.
 *
 * @version     $Revision: 1.6 $ $Date: 2005/06/16 06:25:35 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public abstract class ThreadedAction extends Thread {

    /**
     * The completion listener. May be <code>null</code>.
     */
    private CompletionListener _listener = null;

    /**
     * Semaphore to enable synchronization on completion
     */
    private Semaphore _completedLock = new Semaphore(0);

    /**
     * The exception generated by the action, or <code>null</code>, if no
     * exception was thrown
     */
    private volatile Exception _exception = null;

    /**
     * The time the action started
     */
    private volatile long _start;

    /**
     * The time the action ended
     */
    private volatile long _end;

    /**
     * The logger
     */
    private static final Logger _log
            = Logger.getLogger(ThreadedAction.class);


    /**
     * Construct a new <code>ThreadedAction</code>
     */
    public ThreadedAction() {
    }

    /**
     * Construct a new <code>ThreadedAction</code>, with a listener to notify
     * on completion
     *
     * @param listener the listener to notify on completion
     */
    public ThreadedAction(CompletionListener listener) {
        _listener = listener;
    }

    /**
     * Run the action. This delegates to {@link #runProtected}.<br/>
     * If a {@link CompletionListener} was supplied, it will be notified
     * on completion of the action.
     */
    @Override
    public void run() {
        try {
            _start = System.currentTimeMillis();
            runProtected();
        } catch (Exception exception) {
            _log.debug("ThreadAction caught exception", exception);
            setException(exception);
        } finally {
            _end = System.currentTimeMillis();
        }
        _completedLock.release();
        if (_listener != null) {
            _listener.completed();
        }
    }

    /**
     * Run the action
     *
     * @throws Exception for any error
     */
    public abstract void runProtected() throws Exception;

    /**
     * Returns the elapsed time of the action. This is the time
     * taken to run {@link #runProtected}
     *
     * @return the elapsed time of the action, in milliseconds
     */
    public long getElapsedTime() {
        return _end - _start;
    }

    /**
     * Returns any exception thrown by {@link #runProtected}, or
     * <code>null</code>, if no exception was thrown
     *
     * @return any exception thrown, or <code>null</code>, if none was
     * thrown
     */
    public Exception getException() {
        return _exception;
    }

    /**
     * Wait for the action to complete
     *
     * @throws InterruptedException if interrupted
     */
    public void waitForCompletion() throws InterruptedException {
        _completedLock.acquire();
        _completedLock.release();
    }

    /**
     * Wait for the action to complete
     *
     * @param timeout the number of milleseconds to wait. An argument less
     * than or equal to zero means not to wait at all
     * @throws InterruptedException if interrupted while waiting
     * @return <code>true</code> if the action completed in the given time
     * frame
     */
    public boolean waitForCompletion(long timeout)
        throws InterruptedException {
        boolean completed = _completedLock.attempt(timeout);
        if (completed) {
            _completedLock.release();
        }
        return completed;
    }

    /**
     * Set the exception
     *
     * @param exception the exception
     */
    protected void setException(Exception exception) {
        _exception = exception;
    }

}
