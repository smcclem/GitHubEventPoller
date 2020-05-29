package dev.appsody.starter;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;

import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import io.kabanero.github.api.Event;

@Singleton
@Startup
public class GitHubEventPollerService {
	
	// A service to convert a GitHub event obtained through the Rest API
	// to an event as as GitHub webhook call.
	
	// Fixed size, circular fifo queue to record serviced github events
	private Queue<Event> githubEventsQueue = new CircularFifoQueue<Event>(200);
	private GitHubRestAPIClient gitHubRestAPIClient = new GitHubRestAPIClient();
	private GitHubWebhookInternalClient gitHubWebhookInternalClient = new GitHubWebhookInternalClient();
	
    @Resource
    public ManagedScheduledExecutorService executor;

    @PostConstruct
    public void startup() {
    	executor.scheduleAtFixedRate(new GitHubEventPoller(githubEventsQueue, gitHubRestAPIClient, gitHubWebhookInternalClient), 0, 1, TimeUnit.MINUTES);
    }


}


