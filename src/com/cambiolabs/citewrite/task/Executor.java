package com.cambiolabs.citewrite.task;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class Executor {
	
	private ThreadPoolTaskExecutor threadPoolTaskExecutor = null;
	
	static private Executor executor = null;

	public Executor() {
		executor = this;
	}

	public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
		return threadPoolTaskExecutor;
	}

	public void setThreadPoolTaskExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
	}

	public static Executor getIntance() {
		return executor;
	}
	
	public void addTask (Runnable task){
		threadPoolTaskExecutor.execute(task);
	}
	
}
