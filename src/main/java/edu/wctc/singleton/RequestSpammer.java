package edu.wctc.singleton;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RequestSpammer {
    public static void main(String[] args) throws InterruptedException {
        Scanner keyboard = new Scanner(System.in);

        while (true) {
            System.out.print("\nWhich version are you testing? (1-4, or 0 to quit): ");
            int version = Integer.parseInt(keyboard.nextLine());

            if (version == 0)
                break;

            System.out.print("How many requests would you like to send?: ");
            int requests = Integer.parseInt(keyboard.nextLine());

            ExecutorService es = Executors.newCachedThreadPool();

            // Start up X threads that will all hit Spring Boot together
            for (int i = 0; i < requests; i++) {
                es.execute(() -> {
                    try {
                        String command = "curl -X GET http://localhost:8080/v" + version;
                        Process process = Runtime.getRuntime().exec(command);
                        System.out.println(new String(process.getInputStream().readAllBytes()));
                        process.getInputStream().close();
                        process.destroy();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
            }
            es.shutdown();

            // Make the main thread wait for all X new threads to complete before
            // proceeding with another iteration of the while loop
            es.awaitTermination(1, TimeUnit.MINUTES);
        }

        System.out.println("DONE");
    }
}
