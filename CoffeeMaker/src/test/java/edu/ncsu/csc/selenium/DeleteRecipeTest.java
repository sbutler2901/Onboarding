package edu.ncsu.csc.selenium;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Tests Delete Recipe functionality.
 *
 * @author Kai Presler-Marshall (kpresle@ncsu.edu)
 * @author Elizabeth Gilbert (evgilber@ncsu.edu)
 */

public class DeleteRecipeTest extends SeleniumTest {

    /** The URL for CoffeeMaker - change as needed */
    private WebDriver          driver;
    private String             baseUrl;
    private final StringBuffer verificationErrors = new StringBuffer();

    private String             recipeName;

    @Override
    @Before
    protected void setUp () throws Exception {
        super.setUp();

        recipeName = "CoffeeRecipe";
        driver = new HtmlUnitDriver( true );
        baseUrl = "http://localhost:8080";
        driver.manage().timeouts().implicitlyWait( 10, TimeUnit.SECONDS );

    }

    /**
     * Test to delete an existing, valid recipe. Expect to get a valid success
     * message stating the recipe was deleted.
     *
     * @throws Exception
     */
    @Test
    public void testDeleteRecipe1 () throws Exception {
        add();
        delete();
    }

    /**
     * Tests deleting a recipe.
     *
     * @throws Exception
     */
    public void delete () throws Exception {
        driver.get( baseUrl + "" );
        driver.findElement( By.linkText( "Delete Recipe" ) ).click();

        // Select the recipe to delete and delete it.
        driver.findElement( By.cssSelector( "input[type=\"radio\"]" ) ).click();
        driver.findElement( By.cssSelector( "input[type=\"submit\"]" ) ).click();

        driver.get( baseUrl + "" );
        driver.findElement( By.linkText( "Delete Recipe" ) ).click();
        assertTrue( !driver.getPageSource().contains( recipeName ) );

        driver.findElement( By.linkText( "Home" ) ).click();
    }

    /**
     * Tests adding a recipe to delete.
     *
     * @throws Exception
     */
    public void add () throws Exception {
        driver.get( baseUrl + "" );
        driver.findElement( By.linkText( "Add a Recipe" ) ).click();

        // Enter the recipe information
        driver.findElement( By.name( "name" ) ).clear();
        driver.findElement( By.name( "name" ) ).sendKeys( recipeName );
        driver.findElement( By.name( "price" ) ).clear();
        driver.findElement( By.name( "price" ) ).sendKeys( "50" );
        driver.findElement( By.name( "coffee" ) ).clear();
        driver.findElement( By.name( "coffee" ) ).sendKeys( "2" );
        driver.findElement( By.name( "milk" ) ).clear();
        driver.findElement( By.name( "milk" ) ).sendKeys( "1" );
        driver.findElement( By.name( "sugar" ) ).clear();
        driver.findElement( By.name( "sugar" ) ).sendKeys( "1" );
        driver.findElement( By.name( "chocolate" ) ).clear();
        driver.findElement( By.name( "chocolate" ) ).sendKeys( "0" );
        // Submit the recipe.
        driver.findElement( By.cssSelector( "input[type=\"submit\"]" ) ).click();

        // Make sure the proper message was displayed.
        assertTrue( driver.getPageSource().contains( "Recipe Created" ) );
    }

    @Override
    @After
    public void tearDown () throws Exception {
        driver.quit();
        final String verificationErrorString = verificationErrors.toString();
        if ( !"".equals( verificationErrorString ) ) {
            fail( verificationErrorString );
        }
    }

}