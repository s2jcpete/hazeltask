package com.hazeltask.executor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.hazelcast.logging.LoggingService;
import com.hazeltask.executor.task.HazeltaskTask;

public class ResponseExecutorListenerTest {
    
    private String workId;
    private IExecutorTopologyService mockedSvc;
    private LoggingService mockedLogging;
    private ResponseExecutorListener listener;
    
    @Before
    public void setupData() {
        workId = "item-1";
        mockedSvc = mock(IExecutorTopologyService.class);
        mockedLogging = mock(LoggingService.class);
        listener = new ResponseExecutorListener(mockedSvc, mockedLogging);
    }
    
    @Test
    public void testSuccessfulExecution() {        
        HazeltaskTask<String,String> work = new HazeltaskTask<String,String>("default", workId, "group-1", new SuccessCallable());
        work.run();
        listener.afterExecute(work, null);
        verify(mockedSvc).broadcastTaskCompletion(eq(workId), (Serializable) any());
    }
    
    @Test
    public void testFailedExecution() {
        HazeltaskTask<String,String> work = new HazeltaskTask<String,String>("default", workId, "group-1", new SuccessCallable());
        work.run();
        TestException e = new TestException("Darn!");
        listener.afterExecute(work, e);
        verify(mockedSvc).broadcastTaskError(eq(workId), eq(e));
    }
    
    @Test
    public void testFailedExecutionWorkFail() {
        TestException e = new TestException("Bah!");
        HazeltaskTask<String,String> work = new HazeltaskTask<String,String>("default", workId, "group-1", new ExceptionCallable(e));
        work.run();
        listener.afterExecute(work, null);
        verify(mockedSvc).broadcastTaskError(eq(workId), eq(e));
    }
    
    @Test
    public void testFailedExecutionAllFail() {
        TestException e1 = new TestException("Bah!");
        TestException e2 = new TestException("Humbug!");
        HazeltaskTask<String,String> work = new HazeltaskTask<String,String>("default", workId, "group-1", new ExceptionCallable(e1));
        work.run();
        listener.afterExecute(work, e2);
        verify(mockedSvc).broadcastTaskError(eq(workId), eq(e1));
    }
    
    private static class TestException extends RuntimeException {
        public TestException(String msg) {
            super(msg);
        }
    }
    
    private static class SuccessCallable implements Callable<String> {
        public String call() throws Exception {
            return "Yay!";
        }
    }
    
    private static class ExceptionCallable implements Callable<String> {
        private Exception toThrow;
        ExceptionCallable(Exception toThrow) {
            this.toThrow = toThrow;
        }
        
        public String call() throws Exception {
            throw toThrow;
        }
    }
}
