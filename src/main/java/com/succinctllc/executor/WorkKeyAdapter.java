package com.succinctllc.executor;

import java.util.concurrent.Callable;

public interface WorkKeyAdapter {
	public WorkReference getWorkKey(Runnable work);
	public WorkReference getWorkKey(Callable<?> work);
}
