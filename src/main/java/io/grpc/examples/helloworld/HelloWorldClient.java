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


import javax.inject.Inject;
import java.io.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldClient implements Runnable{
  private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  private String riddle = "";
  @Inject
  private int outputType = 0; // 0 = console, 1 = .txt, 2 = mttq server
  private boolean stop = true;
  static AtomicBoolean hasUserPressedKey = new AtomicBoolean(false);

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

  public void createFile(String filename, String input){
    File directory = new File("outputs");
    if (! directory.exists()){
      directory.mkdir();
    }
    String fileName = "outputs/"+ filename + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date()) + ".txt";

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

  public String requestSolution() {
    if (riddle.equals("")){
      return "no riddle found. Please use \"!requestRiddle\" before \"!requestSolution\".";
    }

    SolveRequest request = SolveRequest.newBuilder().setName(riddle).build();
    SolveReply response;
    try {
      response = blockingStub.solveRiddle(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return "RPC failed: {0}";
    }

    /*
    if (outputType == 0)
      System.out.println(response.getMessage());
    else */if (outputType == 1){
      createFile("output", response.getMessage());
    }

    return response.getMessage();
  }

  public void requestBrute() {
    if (riddle.equals("")){
      System.out.println("no riddle found. Please use \"!requestRiddle\" before \"!requestSolution\".");
      return;
    }

    BruteRequest request = BruteRequest.newBuilder().setName(riddle).build();
    BruteReply response;
    try {
      response = blockingStub.bruteRiddle(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }

    System.out.println(response.getMessage());

  }
  public void showCommands(){
    System.out.println("-----------------------------");
    System.out.println("Command List:      (output type: + " + outputType +" )");
    System.out.println("-----------------------------");
    System.out.println("!help");
    System.out.println("!setOutput x (for x: 0 = console, 1 = txt, 2 = mqtt)");
    System.out.println("!requestRiddle");
    System.out.println("!showRiddle");
    System.out.println("!solveRiddle");
    System.out.println("!stopSolve (only needed for txt output)");
    System.out.println("!bruteRiddle");
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
      Thread t = new Thread(client);
      System.out.println("Type !help to get the command list.");
      //client.greet(user);

      while (true){
        String input = reader.readLine();

          if (!client.stop && client.outputType == 0) {
            client.stop = true;
            System.out.println("stopped output.");
            continue;
          }

          if (input.equals("!help")) {
            client.showCommands();
          } else if (input.equals("!requestRiddle")) {
            client.requestRiddle();
          } else if (input.equals("!showRiddle")) {
            client.showRiddle();
          }
          else if (input.equals("!solveRiddle")) {
            client.stop = false;
            t = new Thread(client);
            t.start();
          }
          else if (input.equals("!stopSolve")) {
            client.stop = true;
          }
          else if (input.equals("!bruteRiddle")) {
            client.requestBrute();
          } else if (input.contains("!setOutput")) {
            String[] split = input.split(" ");
            if (split.length == 2) {
              if (isInteger(split[1])) {
                int x = Integer.parseInt(split[1]);
                client.outputType = x;
                System.out.println("output type has been set to " + x + ".");
              } else
                System.out.println("x in \"!setOutput x\" must be a number between 0 and 2");
            } else
              System.out.println("Something went wrong pleas make sure the command has the following structure \"!setOutput\".");
          } else {
            System.out.println("command not found. use \"!help\" to show all commands.");
            System.out.println("output type: console.");
          }

          hasUserPressedKey.set(false);
      }
    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
      // resources the channel should be shut down when it will no longer be used. If it may be used
      // again leave it running.
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
  public static boolean isInteger(String str) {
    try {
      Integer.parseInt(str);
      return true;
    } catch(NumberFormatException e){
      return false;
    }
  }

  @Override
  public void run() {
    while(!stop){
      try {
        Thread.sleep(1000);
        if (!stop)
          if (outputType == 0)
            System.out.println(requestSolution());
          else if (outputType == 1)
            requestSolution();

      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
