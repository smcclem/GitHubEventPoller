package dev.appsody.starter;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.eclipse.egit.github.core.service.EventService;
import org.eclipse.egit.github.core.service.RepositoryService;

public class Test {

	public static void main(String[] args) {

		
		RepositoryService service = new RepositoryService();
		Repository repository = null;
		try {
			repository = service.getRepository("kabanero-io", "kabanero-pipelines");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EventService eventService = new EventService();
		PageIterator<Event> events = eventService.pageEvents(repository);
		
		for (Collection<Event> eventPage : events) {
			for(Event event : eventPage) {
				System.out.println();
				System.out.println("Type: " + event.getType());
				System.out.println("Date: " + event.getCreatedAt());
				if (event.getType().equals(Event.TYPE_PULL_REQUEST)) {
					PullRequestPayload prp = (PullRequestPayload) event.getPayload();
					System.out.println("prp1:" + prp.getAction());
					System.out.println("prp2:" + prp.getPullRequest());
				}
			}
		}
	}
}
