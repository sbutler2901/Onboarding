package edu.ncsu.csc.coffee_maker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.ncsu.csc.coffee_maker.models.CoffeeMaker;

/**
 * Application main.
 *
 * @author Sarah Heckman
 * @author Kai Presler-Marshall
 * @author Elizabeth Gilbert
 */
@SpringBootApplication
public class Application {

    private static CoffeeMaker cm = new CoffeeMaker();

    /**
     * Returns the CoffeeMaker.
     *
     * @return the CoffeeMaker
     */
    public static CoffeeMaker getCoffeeMaker () {
        return cm;
    }

    /**
     * Starts the program.
     *
     * @param args
     *            command line args
     */
    public static void main ( final String[] args ) {
        SpringApplication.run( Application.class, args );
    }
}
