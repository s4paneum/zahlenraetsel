package io.grpc.examples.helloworld;

public class MqttClient {

    public MqttClient(){
        String broker = "tcp://broker.emqx.io:1883";
        // TLS/SSL
        // String broker = "ssl://broker.emqx.io:8883";
        String username = "emqx";
        String password = "public";
        String clientid = "publish_client";
    }

    public static void main(String[] args) throws Exception {

    }
}
