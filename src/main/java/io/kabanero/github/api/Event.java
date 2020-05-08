
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
    "id",
    "type",
    "actor",
    "repo",
    "payload",
    "public",
    "created_at",
    "org"
})
public class Event {

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("actor")
    private Actor actor;
    @JsonProperty("repo")
    private Repo repo;
    @JsonProperty("payload")
    private Payload payload;
    @JsonProperty("public")
    private Boolean _public;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("org")
    private Org org;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("actor")
    public Actor getActor() {
        return actor;
    }

    @JsonProperty("actor")
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @JsonProperty("repo")
    public Repo getRepo() {
        return repo;
    }

    @JsonProperty("repo")
    public void setRepo(Repo repo) {
        this.repo = repo;
    }

    @JsonProperty("payload")
    public Payload getPayload() {
        return payload;
    }

    @JsonProperty("payload")
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    @JsonProperty("public")
    public Boolean getPublic() {
        return _public;
    }

    @JsonProperty("public")
    public void setPublic(Boolean _public) {
        this._public = _public;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("org")
    public Org getOrg() {
        return org;
    }

    @JsonProperty("org")
    public void setOrg(Org org) {
        this.org = org;
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
        return new HashCodeBuilder().append(actor).append(createdAt).append(payload).append(_public).append(org).append(repo).append(id).append(additionalProperties).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Event) == false) {
            return false;
        }
        Event rhs = ((Event) other);
        return new EqualsBuilder().append(actor, rhs.actor).append(createdAt, rhs.createdAt).append(payload, rhs.payload).append(_public, rhs._public).append(org, rhs.org).append(repo, rhs.repo).append(id, rhs.id).append(additionalProperties, rhs.additionalProperties).append(type, rhs.type).isEquals();
    }
    
    public static String getEvents(Event event) {
        try {
            return new ObjectMapper().writeValueAsString(event);
        } catch (Exception err) {
            // TODO log
            return null;
        }
    }

    public static Event[] getEvents(String jsonString) {
        Event[] event = null;
        try {
            event = new ObjectMapper().readValue(jsonString.getBytes(), Event[].class);
        } catch (Exception e) {
            // TODO: Log
        }
        return event;
    }

}
