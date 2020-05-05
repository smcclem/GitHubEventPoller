package dev.appsody.starter;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Singleton
@Startup
public class GitHubEventPollerService {
	
	// A service to convert a GitHub event obtained through the Rest API
	// to an event as as GitHub webhook call.
	
    @Resource
    public ManagedScheduledExecutorService executor;

    @PostConstruct
    public void startup() {
    	executor.scheduleAtFixedRate(new GitHubEventPoller(), 0, 1, TimeUnit.MINUTES);
    }


}


