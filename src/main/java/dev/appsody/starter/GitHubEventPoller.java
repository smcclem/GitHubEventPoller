package dev.appsody.starter;

import java.io.IOException;
import java.util.Queue;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;

import io.kabanero.github.api.Event;
import io.kabanero.github.api.Payload;

public class GitHubEventPoller implements Runnable {

	private Queue<Event> githubEventsQueue = null; 
	private GitHubRestAPIClient gitHubRestAPIClient = null;
	private GitHubWebhookInternalClient gitHubWebhookInternalClient = null;

	public GitHubEventPoller(Queue<Event> githubEventsQueue, GitHubRestAPIClient gitHubRestAPIClient, GitHubWebhookInternalClient gitHubWebhookInternalClient) {
		this.githubEventsQueue = githubEventsQueue;
		this.gitHubRestAPIClient = gitHubRestAPIClient;
		this.gitHubWebhookInternalClient = gitHubWebhookInternalClient;
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
					if (event.getType().contentEquals("PullRequestEvent")) {
						System.out.println(Payload.getPayload(event.getPayload()));
						Header[] headers = new Header[] { 
								new BasicHeader("User-Agent", "GitHub-Hookshot/3211ebf"),
								new BasicHeader("X-GitHub-Delivery", "8d68eb30-8d8d-11ea-91cd-a63d03231536"),
								new BasicHeader("X-GitHub-Enterprise-Host", "github.ibm.com"),
								new BasicHeader("X-GitHub-Enterprise-Version", "2.19.8"),
								new BasicHeader("X-GitHub-Event", "pull_request"),
								// new BasicHeader("X-Hub-Signature", "sha1=eaa52510f15ea22ece6bd512ad262d40feba0ec8")
								};
						gitHubWebhookInternalClient.triggerInternalWebhook("/webhook", Payload.getPayload(event.getPayload()), headers);
						githubEventsQueue.add(event);
					}

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
