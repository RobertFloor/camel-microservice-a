package com.floor.camelmicroservicea.routes.patterns;

import org.apache.camel.Body;
import org.apache.camel.DynamicRouter;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

//@Component
public class   EipPatternsRouter extends RouteBuilder {

    @Autowired
    SplitterComponent splitter;
    @Autowired
    DynamicRouterBean dynamicRouterBean;
    @Override
    public void configure() throws Exception {
//        from("timer:multicast-timer?period=10000")
//                .multicast()
//                .to("log:something1", "log:something2", "log:something3");

//        from("file:files/csv")
//                .unmarshal().csv()
//                .split(body())
////                .to("log:split-files");
//                .to("activemq:split-queue");

        // Message,message1,Messsage3
//        from("file:files/csv")
//                .convertBodyTo(String.class)
////                .split(body(), ",")
//                .split(method(splitter))
//                .log("${body}")
//                .to("activemq:split-queue");

        //Aggregate
        //Messages => Aggregate => Endpoint
        from("file:files/aggregate-json")
                .unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
                .aggregate(simple("${body.to}"), new ArrayListAggregationStrategy())
               .completionSize(3)
                .to("log:aggregate-json");
//                .completionTimeout(10)

        String routingSlip = "direct:endpoint1, direct:endpoint3";
        // Routing slip pattern
//                from("timer:routingSlip?period=10000")
//                        .transform().constant("My message is hardcoded")
//                        .routingSlip(simple(routingSlip));
//                .multicast()
//                .to("log:something1", "log:something2", "log:something3");

        // Dynamic Routing
       from("timer:dynamicRouting?period={{timePeriod}}")
       .transform().constant("My message is hardcoded")
       .dynamicRouter(method(dynamicRouterBean));
        // Step1, step2, Step3
        // Endpoint 1
        // Endpoint 2
        // Endpoint 3


                from("direct:endpoint1")
                        .wireTap("log:wire-tap")
                        .to("{{endpoint-for-logging}}");
        from("direct:endpoint2")
                .to("log:direct-endpoint2");
        from("direct:endpoint3")
                .to("log:direct-endpoint3");
    }

}

@Component
class SplitterComponent  {
    public List<String> splitInput (String body) {
        return List.of("ABC","DEF","GHI");
    }
}

@Component
class DynamicRouterBean {

    Logger logger = LoggerFactory.getLogger(DynamicRouterBean.class);

    int invocation;

    public String decideTheNextEndpoint (
            @ExchangeProperties Map<String, String> properties,
            @Headers Map<String, String> headers,
            @Body String body
    ) {
        logger.info(" {} {} {}", properties, headers, body);
        invocation++;

        if (invocation%3 == 0)
            return "direct:endpoint1";
        if (invocation%3 == 1)
            return "direct:endpoint2,direct:endpoint3";
        return null;
    }
}