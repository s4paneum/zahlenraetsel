package io.grpc.examples.helloworld;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelClient extends org.apache.camel.builder.RouteBuilder {

    public CamelClient(){

    }

    public static void main(String[] args) {
        // Testing purpose, can be deleted later
        /*
        Riddle riddle = new Riddle("t", 0);
        riddle.generateRiddle();
        riddle.brute_force_riddle(riddle.encodedMatrix);
        System.out.println(riddle.decodedRiddleToDataString());
         */

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
        // getting Riddle from MQTT Server

        String topic = "xxxx";
        String topic2 = "xxxx";
        String brokerUrl =  "xxxx";
        String userName = "xxxx";
        String password = "xxxx";

        from("paho:" + topic + "?" + "brokerUrl=" + brokerUrl +"&userName=" + userName + "&password=" + password)
                .process(new RiddleProcessor())
                .to("grpc://localhost:50051/io.grpc.examples.helloworld.Greeter?method=bruteRiddle&synchronous=true")
                .process(new FormatProcessor())
                .to("paho:" + topic2 + "?" + "brokerUrl=" + brokerUrl +"&userName=" + userName + "&password=" + password);
    }
    public  class FormatProcessor implements Processor{

        @Override
        public void process(Exchange exchange) throws Exception {
            String result = exchange.getMessage().getBody(String.class)
                    .replace("message: \"", "")
                    .replace("}\"", "}")
                    .replace("\\\"", "\"");
            System.out.println(result);
            exchange.getMessage().setBody(result, String.class);
        }
    }
    public class RiddleProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println("Received from MQTT: ");
            System.out.println( exchange.getIn().getBody(String.class));
            Riddle riddle = new Riddle("PatrickNeumann", 0);
            riddle.stringToRiddle(exchange.getIn().getBody(String.class));
            BruteRequest request = BruteRequest.newBuilder().setName(riddle.encodedRiddleToDataString()).build();
            exchange.getIn().setBody(request, BruteRequest.class);
            System.out.println("---------------");
        }
    }
}
