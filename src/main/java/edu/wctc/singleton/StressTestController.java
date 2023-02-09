package edu.wctc.singleton;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class StressTestController {

    /**
     * StressTestController is a Spring bean, which means that when Spring
     * Boot starts, it creates exactly one StressTestController object.
     * (This is the "singleton" design pattern in action, by the way.)
     * When a request comes in for one of its defined endpoints, Spring
     * Boot will invoke the matching method of its ONE StressTestController
     * bean. Again, this means that ALL requests are being served by a
     * SINGLE object.
     *
     * This constructor exists so that we can see the moment at startup when
     * Spring creates the bean.
     */
    public StressTestController() {
        System.out.println("One StressTestController bean has been created!");
    }

    // Because StressTestController is a singleton bean, all requests will
    // be using this same list, which is an instance field of the object
    private List<String> sharedList = new ArrayList<>();

    /**
     * This version places letters in the shared list, concatenates, then
     * returns the contents of the list as one string. The first time you call
     * this handler, all is well: you get a 10-character string as expected.
     * But the second time, you get a 20-character string, then a 30-character
     * string, and so on.
     * @return A 10-letter string (the first time)
     */
    @GetMapping("/v1")
    @ResponseBody
    public String version1() {
        String letter = getRandomLetter();

        // Add 10 of that letter to the shared list
        for (int i = 0; i < 10; i++) {
            sharedList.add(letter);
        }

        // Join letters together and return the string
        String returnValue = String.join("", sharedList);

        // This hashcode of the controller is like its unique object
        // ID. We print it to demonstrate that the same controller
        // object is being used for each request.
        System.out.printf("%d: %s%n", this.hashCode(), returnValue);

        return returnValue;
    }

    /**
     * This version clears the shared list before adding its 10 letters.
     * This will probably give the expected result 99.999% of the time
     * because the method runs so fast that the odds of a concurrent thread
     * calling it at the same time are very low.
     * @return A 10-letter string (probably)
     */
    @GetMapping("/v2")
    @ResponseBody
    public String version2() {
        sharedList.clear();

        String letter = getRandomLetter();

        // Add 10 of that letter to the shared list
        for (int i = 0; i < 10; i++) {
            sharedList.add(letter);
        }

        // Join letters together and return the string
        String returnValue = String.join("", sharedList);

        // This hashcode of the controller is like its unique object
        // ID. We print it to demonstrate that the same controller
        // object is being used for each request.
        System.out.printf("%d: %s%n", this.hashCode(), returnValue);

        return returnValue;
    }

    /**
     * This version also clears the shared list, but it introduces an
     * artificial delay to simulate a slower request. Now the success
     * drops to around 98%, with a few noticeable anomalies when
     * serving simultaneous requests. Sometimes there will be strings
     * of different lengths. Sometimes there will be exceptions thrown
     * when one thread is reading the list while another is modifying it.
     *
     * You can get rid of the exceptions by using a thread-safe list
     * (e.g. Collections.synchronizedList(new ArrayList<>())) but you'll
     * still see mixed-letter strings popping up.
     *
     * @return A 10-letter string (most of the time)
     */
    @GetMapping("/v3")
    @ResponseBody
    public String version3() {
        sharedList.clear();

        String letter = getRandomLetter();

        // Add 10 of that letter to the shared list
        for (int i = 0; i < 10; i++) {
            sharedList.add(letter);
        }

        // Add a tiny delay (0.05 seconds) before creating the return value
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Join letters together and return the string
        String returnValue = String.join("", sharedList);

        // This hashcode of the controller is like its unique object
        // ID. We print it to demonstrate that the same controller
        // object is being used for each request.
        System.out.printf("%d: %s%n", this.hashCode(), returnValue);

        return returnValue;
    }

    /**
     * Ahh, finally, the good version! This version will never have a
     * concurrency problem because it uses a list that has been declared
     * inside the method, versus 'sharedList', which belongs to the
     * controller object. Local variables exist in the stack frame of
     * the method that created them, so each call stack (e.g. each thread)
     * will have its own list.
     *
     * In case you were wondering, this is why
     * the previous methods could generate different random letters --
     * because the String variable 'letter' is declared in the method. If
     * 'letter' had been a class field, it would have had the same problem
     * as 'sharedList': being overwritten by other threads mid-call.
     *
     * What is the takeaway here? Never, ever store stateful information
     * as an instance field in a bean! Constants are okay (final Strings
     * and ints, for example). Other beans are okay, too, provided they
     * abide by the same rules.
     *
     * @return A 10-letter string, always!!
     */
    @GetMapping("/v4")
    @ResponseBody
    public String version4() {
        List<String> nonSharedList = new ArrayList<>();

        String letter = getRandomLetter();

        // Add 10 of that letter to the shared list
        for (int i = 0; i < 10; i++) {
            nonSharedList.add(letter);
        }

        // Add a giant delay (0.5 seconds) before creating the return value
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Join letters together and return the string
        String returnValue = String.join("", nonSharedList);

        // This hashcode of the controller is like its unique object
        // ID. We print it to demonstrate that the same controller
        // object is being used for each request.
        System.out.printf("%d: %s%n", this.hashCode(), returnValue);

        return returnValue;
    }

    /**
     * A silly helper method, simply to illustrate the call stack.
     * @return A randomly generated capital letter
     */
    private String getRandomLetter() {
        // Pick a random letter of the alphabet (ASCII char 65 - 91)
        Random random = new Random();
        int number = random.nextInt(26) + 65;
        return convertToLetter(number);
    }

    /**
     * Another silly method to illustrate the call stack.
     * @param num An integer between 65 and 91 (inclusive)
     * @return The ASCII character with the given code
     */
    private String convertToLetter(int num) {
        Character c = (char) num;
        return c.toString();
    }
}
