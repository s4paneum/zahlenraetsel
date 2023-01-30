package io.grpc.examples.helloworld;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelClient extends org.apache.camel.builder.RouteBuilder {

    public CamelClient(){

    }

    public static void main(String[] args) {
        CamelClient client = new CamelClient();
        CamelContext context = new DefaultCamelContext();

        try {
            context.addRoutes(client);
            context.start();
            Thread.sleep(5 * 60 * 1000);
            context.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configure() throws Exception {
        from("file://outputs?move=./done")
                .process(new LogProcessor())
                .bean(new Transformer(), "transformContent")
                .to("file://camelOutputs");
    }

    public class Transformer {
        public String transformContent(String body){
            return body + " camel approved";
        }
    }

    public class LogProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println("Processing: " + exchange.getIn().getBody(String.class));
        }
    }

}
