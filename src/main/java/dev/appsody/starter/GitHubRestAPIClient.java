package dev.appsody.starter;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

import io.kabanero.github.api.Event;
import io.kabanero.github.api.Payload;


public class GitHubRestAPIClient {

	private String authToken = null;
	private Date authTokenCreated;
	private String tenantId = null;
	private String camVersion = null;
	private HttpClientBuilder clientBuilder = null;
	private String ipAddress = "api.github.com";
	private String userName;
	private String password;

	private static final int CONNECTTIMEOUT_RETRIES = 2;
	private static final int AUTO_RENEW_AUTH_TOKEN = 4; // Hours

	// Reuse the same context in a thread to reuse cookies
	private ThreadLocal<HttpContext> context;

	public GitHubRestAPIClient() throws RuntimeException {

		this.context = new ThreadLocal<HttpContext>() {
			@Override
			protected HttpContext initialValue() {
				return new BasicHttpContext();
			}
		};

		try {

			clientBuilder = HttpClients.custom();


			TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {
						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						@Override
						public void checkClientTrusted(
								java.security.cert.X509Certificate[] certs, String authType) {}

						@Override
						public void checkServerTrusted(
								java.security.cert.X509Certificate[] certs, String authType) {}
					}
			};

			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(null, trustAllCerts, new java.security.SecureRandom());

			clientBuilder.setSSLSocketFactory(new SSLConnectionSocketFactory(context, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));


		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}



	private JSONObject post(String postPath, HashMap<String, String> parametersMap, Header[] headers) throws IOException {
		HttpPost postMethod = new HttpPost("https://" + ipAddress + postPath);
		StringEntity params = new StringEntity(getDataString(parametersMap));
		postMethod.setEntity(params);
		StringBuilder output = new StringBuilder();
		for (Header header : headers) {
			postMethod.addHeader(header);
		}
		// Make request and handle errors
		StatusLine status = executeWithRetries(postMethod, output, new Integer[] { 200 }, new Integer[] { 400, 401, 403, 404 });
		if (status.getStatusCode() != 202 && status.getStatusCode() != 201 && status.getStatusCode() != 200) {
			// TODO : LOGGER.log(Level.SEVERE, "Failed: HTTP error code : " + status + ", response : " + output + "performing post with path" + postPath);
			// TODO throw new StatusCodeException(status, "body: " + output.toString());
		}
		JSONObject obj = JSONObject.parse(output.toString());
		return obj;
	}

	private String post(String postPath) throws IOException {
		HttpPost postMethod = new HttpPost("https://" + ipAddress + postPath);
		postMethod.addHeader("accept", "application/json");
		// TODO : postMethod.addHeader("Authorization", "bearer " + getAuthToken());
		StringBuilder output = new StringBuilder();
		// Make request and handle errors
		StatusLine status = executeWithRetries(postMethod, output, new Integer[] { 200 }, new Integer[] { 400, 401, 403, 404 });
		if (status.getStatusCode() != 202 && status.getStatusCode() != 201 && status.getStatusCode() != 200) {
			// TODO  LOGGER.log(Level.SEVERE, "Failed: HTTP error code : " + status + ", response : " + output + "performing post with path" + postPath);
			// TODO throw new StatusCodeException(status, "body: " + output.toString());
		}
		return output.toString();
	}


	private String get(String path) throws IOException {
		HttpGet getMethod = new HttpGet("https://" + ipAddress + path);
		getMethod.addHeader("accept", "application/json");
		// TODO: getMethod.addHeader("Authorization", "bearer " + getAuthToken());
		StringBuilder output = new StringBuilder();
		// Make request and handle errors
		executeWithRetries(getMethod, output, new Integer[] { 200 }, new Integer[] { 401, 403, 404 });
		return output.toString();
	}

	private StatusLine executeWithRetries(HttpUriRequest request, StringBuilder output, Integer[] expectedCodes, Integer[] failureCodes) throws IOException {
		HashSet<Integer> expectedSet = new HashSet<>(Arrays.asList(expectedCodes));
		HashSet<Integer> failureSet = new HashSet<>(Arrays.asList(failureCodes));
		if (output == null) {
			output = new StringBuilder();
		}
		StatusLine statusLine = null;
		for (int retries = 0; retries < CONNECTTIMEOUT_RETRIES; retries++) {
			try {
				statusLine = execute(request, output);
				int status = statusLine.getStatusCode();
				if (expectedSet.contains(status)) {
					if (retries > 0) {
						// TODO:LOGGER.log(Level.INFO, "TerraFormRestClient " + request.getMethod() + " " + request.getURI() + ": Reached CAM after retrying " + retries + " times(s)");
					}
					return statusLine;
				}
				if (failureSet.contains(status)) {
					if (status == 401) {
						// TODO: request.setHeader("Authorization", "bearer " + getAuthToken(true));
					} else {
						break;
					}
				}
			} catch (ConnectTimeoutException ex) {
				// do nothing, absorb exception and try again
			}
			if (pause()) {
				break;
			}
		}
		if (statusLine == null) {
			throw new RuntimeException("TerraFormRestClient " + request.getMethod() + " " + request.getURI() + " Exceeded retry limit of " + CONNECTTIMEOUT_RETRIES
					+ " trying to reach Pure");
		} else {
			// TODO: LOGGER.log(Level.SEVERE, "Failed TerraFormRestClient " + request.getMethod() + " " + request.getURI() + ": HTTP error code : " + statusLine.getStatusCode() + ", response : " + output);
			// TODO : throw new StatusCodeException(statusLine, "body: " + output.toString());
			return null; // TODO
		}
	}


    private StatusLine execute(HttpUriRequest request, StringBuilder output) throws IOException {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = clientBuilder.setRedirectStrategy(new LaxRedirectStrategy()).build();
            //TODO : LOGGER.log(Level.FINE, "Executing HTTP Request >>> " + request.toString());
            response = client.execute(request, context.get());
            // TODO: LOGGER.log(Level.FINE, "Received HTTP Response <<< " + response.toString());

            if (output != null) {
                output.setLength(0);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    output.append(EntityUtils.toString(entity));
                }
            }
            return response.getStatusLine();
        } finally {
            close(response);
            close(client);
        }
    }


    protected boolean pause() {
        // Sleep for 5 seconds before next try
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Something woke us up, most probably process is exiting.
            // Just break out of the loop to report the last DB exception.
            return true;
        }
        return false;
    }    
    
    private void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ioe) {
            }
        }
    }
    
    private String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }    

    public Event[] getEvents() throws IOException {

    	
	    // return Event.getEvents(get("/repos/kabanero-io/kabanero-pipelines/events"));
	    return Event.getEvents(get("/repos/smcclem/GitHubEventPoller/events"));	    

    }
    
	public static void main(String[] args) throws IOException {

		GitHubRestAPIClient test2 = new GitHubRestAPIClient();
		Event[] events = Event.getEvents(test2.get("/repos/kabanero-io/kabanero-pipelines/events"));
		for (Event event : events) {
			System.out.println("Id: " + event.getId());
			System.out.println("Type: " + event.getType());
			System.out.println("Created at: " + event.getCreatedAt());	
			if (event.getType().equals("PullRequestEvent")) {
				Payload payload = event.getPayload();
				System.out.println(payload.getNumber());
			}
		}
	}
}
