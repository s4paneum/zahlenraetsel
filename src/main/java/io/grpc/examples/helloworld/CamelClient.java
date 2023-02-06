package io.grpc.examples.helloworld;

import com.google.protobuf.Type;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
  /*      from("file://outputs?move=./done")
                .process(new LogProcessor())
                .bean(new Transformer(), "transformContent")
                .to("file://camelOutputs");
  */
        // getting Riddle from MQTT Server

        String topic = "";
        String brokerUrl =  "";
        String userName = "";
        String password = "";

        from("paho:" + topic + "?" + brokerUrl +"&userName=" + userName + "&password=" + password)
                .process(new LogProcessor())
                .to("file://outputs");

        from("file://outputs?move=./done").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                Riddle riddle = new Riddle("s4paneum", 0);
                riddle.stringToRiddle(exchange.getIn().getBody(String.class));
                BruteRequest request = BruteRequest.newBuilder().setName(riddle.encodedRiddleToDataString()).build();
                exchange.getIn().setBody(request, BruteRequest.class);
            }
        }).to("grpc://localhost:50051/io.grpc.examples.helloworld.Greeter?method=bruteRiddle&synchronous=true")
                .bean(new Transformer(), "transformContent");


    }

    public class Transformer {
        public String transformContent(String body){
            JSONObject obj = new JSONObject(body);
            int id = obj.getInt("raetsel_id");
            String server_id = obj.getString("server_id");
            saveAsFile(server_id + "_" + id, body);
            System.out.println(body);
            return body;
        }
    }

    public class LogProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println("Received from MQTT: ");
            System.out.println( exchange.getIn().getBody(String.class));
            System.out.println("---------------");
        }
    }

    public void saveAsFile(String filename, String input){
        File directory = new File("camelOutputs");
        if (! directory.exists()){
            directory.mkdir();
        }
        String fileName = "camelOutputs/"+ filename + ".txt";

        File f = new File(fileName);
        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(fileName);
            myWriter.write(input);
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
