package com.floor.camelmicroservicea.routes.b;

import org.apache.camel.Body;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Handler;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

//@Component
public class MyFileRouter extends RouteBuilder {
    @Autowired
    private DeciderBean deciderBean;

    @Override
    public void configure() throws Exception {
        from("file:files/input")
                .routeId("Files-Input-Route")
                .transform().body(String.class)
                .choice()
                    .when(simple("${file:ext}  == 'xml'"))
                    .log("XML file")
//                .when(method(deciderBean))
                    .when(simple("${body} contains 'USD'"))
                    .log("NOT and XML file but contains USD")
                .otherwise()
                .log("Not and XML file")
                .end()

//                .to("direct://log-file-values")
                .to("file:files/output");

            from("direct:log-file-values")
                    .log("${body}")
                    .log("${messageHistory} ${headers.CamelFileAbsolute}")
                    .log("${file:absolute.path}")
        ;
    }
}

@Component

class DeciderBean {
    Logger logger = LoggerFactory.getLogger(DeciderBean.class);
    public boolean isThisConditionMet (@Body String body,
                                       @Headers Map<String,String> headers,
                                       @ExchangeProperties Map<String, String> exchange)
                                        {
//        logger.info("Decider Bean: {} {}, {}", body ,headers, headers.get("CamelFileAbsolutePath"));
        logger.info("{}", exchange);
        return true;
    }
}