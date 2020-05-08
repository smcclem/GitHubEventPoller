package dev.appsody.starter;

import java.io.IOException;
import java.util.Queue;
import io.kabanero.github.api.Event;

public class GitHubEventPoller implements Runnable {
	
	private Queue<Event> githubEventsQueue = null; 
	private GitHubRestAPIClient gitHubRestAPIClient = null;
	
	public GitHubEventPoller(Queue<Event> githubEventsQueue, GitHubRestAPIClient gitHubRestAPIClient) {
		this.githubEventsQueue = githubEventsQueue;
		this.gitHubRestAPIClient = gitHubRestAPIClient;
	}

	@Override
	public void run() {
		try {
			Event[] events = gitHubRestAPIClient.getEvents();
			for (Event event : events) {
				if(!!!githubEventsQueue.contains(event)) {
					System.out.println("---------------");
					System.out.println("New github event ");
					System.out.println("Id:  " + event.getId());
					System.out.println("Created at:  " + event.getCreatedAt());					
					System.out.println("Type:  " + event.getType());							
					System.out.println("---------------");				
					githubEventsQueue.add(event);
				} else {
					System.out.println(event.getId() + " already serviced");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
