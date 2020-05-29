
package io.kabanero.github.api;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "action",
    "number",
    "pull_request"
})
public class Payload {

    @JsonProperty("action")
    private String action;
    @JsonProperty("number")
    private Integer number;
    @JsonProperty("pull_request")
    private PullRequest pullRequest;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    @JsonProperty("number")
    public Integer getNumber() {
        return number;
    }

    @JsonProperty("number")
    public void setNumber(Integer number) {
        this.number = number;
    }

    @JsonProperty("pull_request")
    public PullRequest getPullRequest() {
        return pullRequest;
    }

    @JsonProperty("pull_request")
    public void setPullRequest(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(action).append(number).append(additionalProperties).append(pullRequest).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Payload) == false) {
            return false;
        }
        Payload rhs = ((Payload) other);
        return new EqualsBuilder().append(action, rhs.action).append(number, rhs.number).append(additionalProperties, rhs.additionalProperties).append(pullRequest, rhs.pullRequest).isEquals();
    }

    public static String getPayload(Payload payload) {
        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (Exception err) {
            // TODO log
            return null;
        }
    }

    public static Payload getPayload(String jsonString) {
    	Payload payload = null;
        try {
        	payload = new ObjectMapper().readValue(jsonString.getBytes(), Payload.class);
        } catch (Exception e) {
            // TODO: Log
        }
        return payload;
    }
    
}
