/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.examples.helloworld;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldClient {
  private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  private String riddle = "";

  /** Construct client for accessing HelloWorld server using the existing channel. */
  public HelloWorldClient(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  /** Say hello to server. */
  public void greet(String name) {
    logger.info("Will try to greet " + name + " ...");
    HelloRequest request = HelloRequest.newBuilder().setName(name).build();
    HelloReply response;

    try {
      response = blockingStub.sayHello(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());
  }

  public void requestRiddle() {
    RiddleRequest request = RiddleRequest.newBuilder().setName("").build();
    RiddleReply response;
    try {
      response = blockingStub.requestRiddle(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }

    riddle = response.getMessage();
    System.out.println(riddle);
  }

  public void requestSolution() {
    if (riddle.equals("")){
      System.out.println("no riddle found. Please use \"!requestRiddle\" before \"!requestSolution\".");
      return;
    }

    SolveRequest request = SolveRequest.newBuilder().setName(riddle).build();
    SolveReply response;
    try {
      response = blockingStub.solveRiddle(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }

    System.out.println(response.getMessage());

  }
  public void showCommands(){
    System.out.println("-----------------------------");
    System.out.println("Command List:");
    System.out.println("-----------------------------");
    System.out.println("!help");
    System.out.println("!requestRiddle");
    System.out.println("!showRiddle");
    System.out.println("!solveRiddle");
    System.out.println("-----------------------------");
  }

  public void showRiddle(){
    if (riddle.equals("")){
      System.out.println("no riddle found. Use \"!requestRiddle\" to request one.");
    }else {
      System.out.println(riddle);
    }
  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting. The second argument is the target server.
   */
  public static void main(String[] args) throws Exception {
    String user = "world";
    // Access a service running on the local machine on port 50051
    String target = "localhost:50051";

    // Create a communication channel to the server, known as a Channel. Channels are thread-safe
    // and reusable. It is common to create channels at the beginning of your application and reuse
    // them until the application shuts down.
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
        // needing certificates.
        .usePlaintext()
        .build();
    try {
      HelloWorldClient client = new HelloWorldClient(channel);
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("Type !help to get the command list.");
      //client.greet(user);

      while (true){
        String input = reader.readLine();

        if (input.equals("!help")){
          client.showCommands();
        }else if (input.equals("!requestRiddle")){
          client.requestRiddle();
        }else if (input.equals("!showRiddle")){
          client.showRiddle();
        }else if (input.equals("!solveRiddle")){
          client.requestSolution();
        }else{
          System.out.println("command not found. use \"!help\" to show all commands.");
        }
      }
    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
      // resources the channel should be shut down when it will no longer be used. If it may be used
      // again leave it running.
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
