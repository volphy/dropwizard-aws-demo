package com.krzysztofwilk.dropwizard.resource;

import com.codahale.metrics.annotation.Timed;
import com.krzysztofwilk.dropwizard.api.Saying;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;

    public HelloWorldResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Saying sayHello(@Nullable @QueryParam("name") String name) {
        final String value = String.format(template, (name != null) ? name : defaultName);
        return new Saying(counter.incrementAndGet(), value);
    }
}