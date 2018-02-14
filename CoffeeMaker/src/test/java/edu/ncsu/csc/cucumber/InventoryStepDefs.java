package edu.ncsu.csc.cucumber;

import java.util.List;

import org.junit.Assert;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.ncsu.csc.coffee_maker.models.CoffeeMaker;
import edu.ncsu.csc.coffee_maker.models.persistent.Inventory;
import edu.ncsu.csc.coffee_maker.models.persistent.Recipe;
import edu.ncsu.csc.test_utils.SharedInventoryData;

/**
 * StepDefs (Cucumber) test class for interacting with the Inventory model. This
 * performs a number of tests to ensure that the inventory is changed in the
 * expected manner
 *
 * @author Kai Presler-Marshall
 * @author Sarah Elder
 *
 */
public class InventoryStepDefs {
    private final CoffeeMaker         coffeeMaker;
    private final SharedInventoryData inventoryData;

    /**
     * Constructor
     *
     * @param cm
     *            CoffeeMaker to use
     * @param sid
     *            SharedInventoryData; basically a backup copy of the inventory
     *            to make sure that changes made to the "real" one are what is
     *            expected
     */
    public InventoryStepDefs ( final CoffeeMaker cm, final SharedInventoryData sid ) {
        this.coffeeMaker = cm;
        this.inventoryData = sid;

    }

    /**
     * The CoffeeMaker has no (direct) way to remove ingredients from the
     * Inventory it stores, this allows us to effectively do so by creating a
     * recipe with the amount of ingredients to remove, and then making coffee
     * with the Recipe just created
     *
     * @param removeCoffee
     *            Amount of Coffee to remove from the Inventory
     * @param removeMilk
     *            Amount of Milk to remove from the Inventory
     * @param removeSugar
     *            Amount of Sugar to remove from the Inventory
     * @param removeChocolate
     *            Amount of Chocolate to remove from the Inventory
     */
    public void removeInventoryHelper ( final int removeCoffee, final int removeMilk, final int removeSugar,
            final int removeChocolate ) {
        // add a recipe that uses the exact amounts needed to remove
        final Recipe tempRecipe = new Recipe();
        try {
            tempRecipe.setName( "tempRecipeRemoveInventory" );
            tempRecipe.setCoffee( removeCoffee );
            tempRecipe.setMilk( removeMilk );
            tempRecipe.setSugar( removeSugar );
            tempRecipe.setChocolate( removeChocolate );
            tempRecipe.setPrice( 0 );
            coffeeMaker.addRecipe( tempRecipe );
        }
        catch ( final Exception e ) {
            e.printStackTrace();
        }

        // purchase the amount of coffee necessary to deplete the inventory
        coffeeMaker.makeCoffee( tempRecipe, 0 );

        // remove the temporary recipe so that it doesn't impact future
        // tests/steps
        coffeeMaker.deleteRecipe( tempRecipe );
    }

    /**
     * This Cucumber "Given" step ensures that the Recipe has the amount of
     * ingredients specified. This is used to ensure that preconditions for the
     * tests are satisfied
     *
     * @param originalCoffee
     *            Amount of Coffee that the inventory will be set to have
     * @param originalMilk
     *            Amount of Milk that the inventory will be set to have
     * @param originalSugar
     *            Amount of Sugar that the inventory will be set to have
     * @param originalChocolate
     *            Amount of Chocolate that the inventory will be set to have
     */
    @Given ( "^there is (-?\\d+) coffee, (-?\\d+) milk, (-?\\d+) sugar, and (-?\\d+) chocolate in the CoffeeMaker$" )
    public void initialInventory ( final int originalCoffee, final int originalMilk, final int originalSugar,
            final int originalChocolate ) {
    	List<Recipe> all = coffeeMaker.getRecipes();
    	for(Recipe r:all) {
    		coffeeMaker.deleteRecipe(r);
    	}
    	
        inventoryData.originalCoffee = originalCoffee;
        inventoryData.originalMilk = originalMilk;
        inventoryData.originalSugar = originalSugar;
        inventoryData.originalChocolate = originalChocolate;
        final String unparsedInventory = coffeeMaker.checkInventory();
        final String[] inventoryComponents = unparsedInventory.split( "\n" );
        int coffee1 = 0;
        int milk1 = 0;
        int sugar1 = 0;
        int chocolate1 = 0;
        for ( int i = 0; i < inventoryComponents.length; i++ ) {
            final String c = inventoryComponents[i];
            if ( c.toLowerCase().contains( "coffee" ) ) {
                coffee1 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "milk" ) ) {
                milk1 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "sugar" ) ) {
                sugar1 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "chocolate" ) ) {
                chocolate1 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
        }
        int coffeeToAdd = originalCoffee - coffee1;
        int milkToAdd = originalMilk - milk1;
        int sugarToAdd = originalSugar - sugar1;
        int chocolateToAdd = originalChocolate - chocolate1;

        int subtractCoffee = 0, subtractMilk = 0, subtractSugar = 0, subtractChocolate = 0;
        boolean tooMuchInventory = false;
        if ( coffeeToAdd < 0 ) {
            subtractCoffee = 0 - coffeeToAdd;
            coffeeToAdd = 0;
            tooMuchInventory = true;
        }
        if ( milkToAdd < 0 ) {
            subtractMilk = 0 - milkToAdd;
            milkToAdd = 0;
            tooMuchInventory = true;
        }
        if ( sugarToAdd < 0 ) {
            subtractSugar = 0 - sugarToAdd;
            sugarToAdd = 0;
            tooMuchInventory = true;
        }
        if ( chocolateToAdd < 0 ) {
            subtractChocolate = 0 - chocolateToAdd;
            chocolateToAdd = 0;
            tooMuchInventory = true;
        }
        if ( tooMuchInventory ) {
            removeInventoryHelper( subtractCoffee, subtractMilk, subtractSugar, subtractChocolate );
        }

        try {
            coffeeMaker.addInventory( coffeeToAdd, milkToAdd, sugarToAdd, chocolateToAdd );
        }
        catch ( final Exception e ) {
            Assert.fail( "Inventory not added. InventoryException thrown" );
        }
    }

    /**
     * Add the specified amounts of ingredients to the CoffeeMaker's inventory.
     * This is required to pass.
     *
     * @param amtCoffee
     *            Amount of Coffee to add to the Inventory.
     * @param amtMilk
     *            Amount of Milk to add to the Inventory.
     * @param amtSugar
     *            Amount of Sugar to add to the Inventory.
     * @param amtChocolate
     *            Amount of Chocolate to add to the Inventory.
     */
    @When ( "^I add (-?\\d+) coffee, (-?\\d+) milk, (-?\\d+) sugar, and (-?\\d+) chocolate$" )
    public void addInventory ( final int amtCoffee, final int amtMilk, final int amtSugar, final int amtChocolate ) {
        inventoryData.newCoffee = amtCoffee;
        inventoryData.newMilk = amtMilk;
        inventoryData.newSugar = amtSugar;
        inventoryData.newChocolate = amtChocolate;
        try {
            coffeeMaker.addInventory( amtCoffee, amtMilk, amtSugar, amtChocolate );
        }
        catch ( final Exception e ) {
            Assert.fail( "Inventory not added. InventoryException thrown" );
        }
    }

    /**
     * Add the specified amount of ingredients to the CoffeeMaker's inventory.
     * This is for testing with invalid values and is expected to fail.
     *
     * @param amtCoffee
     *            Amount of Coffee to add to the Inventory.
     * @param amtMilk
     *            Amount of Milk to add to the Inventory.
     * @param amtSugar
     *            Amount of Sugar to add to the Inventory.
     * @param amtChocolate
     *            Amount of Chocolate to add to the Inventory.
     */
    @When ( "^I attempt to add (-?\\d+) coffee, (-?\\d+) milk, (-?\\d+) sugar, and (-?\\d+) chocolate$" )
    public void invalidAddInventory ( final int amtCoffee, final int amtMilk, final int amtSugar,
            final int amtChocolate ) {
        try {
            coffeeMaker.addInventory( amtCoffee, amtMilk, amtSugar, amtChocolate );
            Assert.fail( "Inventory added without throwing an error." );
        }
        catch ( final Exception e ) {
            inventoryData.errorMessage = e.getMessage();
            Assert.assertTrue( "Adding Inventory throws error", true );
        }
    }

    /**
     * Set the CoffeeMaker's Inventory to have the amount of ingredients
     * specified. This will completely replace the values that were already
     * stored.
     *
     * @param amtCoffee
     *            Amount of Coffee to set the inventory to contain.
     * @param amtMilk
     *            Amount of Milk to set the inventory to contain.
     * @param amtSugar
     *            Amount of Sugar to set the inventory to contain.
     * @param amtChocolate
     *            Amount of Chocolate to set the inventory to contain.
     */
    @When ( "^I update it to be (-?\\d+) coffee, (-?\\d+) milk, (-?\\d+) sugar, and (-?\\d+) chocolate$" )
    public void updateInventory ( final int amtCoffee, final int amtMilk, final int amtSugar, final int amtChocolate ) {
        inventoryData.newCoffee = amtCoffee;
        inventoryData.newMilk = amtMilk;
        inventoryData.newSugar = amtSugar;
        inventoryData.newChocolate = amtChocolate;

        try {
            final Inventory inventory = coffeeMaker.getInventory();
            inventory.setCoffee( amtCoffee );
            inventory.setMilk( amtMilk );
            inventory.setSugar( amtSugar );
            inventory.setChocolate( amtChocolate );
        }
        catch ( final Exception e ) {
            Assert.fail( "Inventory not added. InventoryException thrown" );
        }
    }

    /**
     * Verify that the CoffeeMaker's Inventory was not updated and contains the
     * same values that were already stored in the "backup" Shared Inventory
     * Data.
     */
    @Then ( "^the inventory of the CoffeeMaker is not updated$" )
    public void inventoryNotUpdated () {

        // Get the current inventory and break it into its components
        final String unparsedInventory = coffeeMaker.checkInventory();
        final String[] inventoryComponents = unparsedInventory.split( "\n" );

        int coffee2 = 0;
        int milk2 = 0;
        int sugar2 = 0;
        int chocolate2 = 0;
        for ( int i = 0; i < inventoryComponents.length; i++ ) {
            final String c = inventoryComponents[i];
            if ( c.toLowerCase().contains( "coffee" ) ) {
                coffee2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "milk" ) ) {
                milk2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "sugar" ) ) {
                sugar2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "chocolate" ) ) {
                chocolate2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
        }
        // Verify that the inventory is unchanged
        Assert.assertEquals( "Coffee not correct", inventoryData.originalCoffee, coffee2 );
        Assert.assertEquals( "Milk not correct", inventoryData.originalMilk, milk2 );
        Assert.assertEquals( "Sugar not correct", inventoryData.originalSugar, sugar2 );
        Assert.assertEquals( "Chocolate not correct", inventoryData.originalChocolate, chocolate2 );

    }

    /**
     * Verify that the CoffeeMaker's Inventory has been updated and that it
     * stores the values in the SharedInventoryData
     */
    @Then ( "^the inventory of the CoffeeMaker is successfully added$" )
    public void inventoryAdded () {
        // calculate what the inventory SHOULD be
        final int expectedCoffee = inventoryData.originalCoffee + inventoryData.newCoffee;
        final int expectedMilk = inventoryData.originalMilk + inventoryData.newMilk;
        final int expectedSugar = inventoryData.originalSugar + inventoryData.newSugar;
        final int expectedChocolate = inventoryData.originalChocolate + inventoryData.newChocolate;

        // Get the inventory of the coffeeMaker and break it into its individual
        // components
        final String unparsedInventory = coffeeMaker.checkInventory();
        final String[] inventoryComponents = unparsedInventory.split( "\n" );

        int coffee2 = 0;
        int milk2 = 0;
        int sugar2 = 0;
        int chocolate2 = 0;
        for ( int i = 0; i < inventoryComponents.length; i++ ) {
            final String c = inventoryComponents[i];
            if ( c.toLowerCase().contains( "coffee" ) ) {
                coffee2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "milk" ) ) {
                milk2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "sugar" ) ) {
                sugar2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "chocolate" ) ) {
                chocolate2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
        }

        // Verify that the inventory is correct
        Assert.assertEquals( "Coffee not added correctly", expectedCoffee, coffee2 );
        Assert.assertEquals( "Milk not added correctly", expectedMilk, milk2 );
        Assert.assertEquals( "Sugar not added correctly", expectedSugar, sugar2 );
        Assert.assertEquals( "Chocolate not added correctly", expectedChocolate, chocolate2 );

    }

    /**
     * Verify that the CoffeeMaker's Inventory has been updated and that it
     * stores the values in the SharedInventoryData
     */
    @Then ( "^the inventory of the CoffeeMaker is successfully updated$" )
    public void inventoryUpdated () {

        // Get the inventory of the coffeeMaker and break it into its individual
        // components
        final String unparsedInventory = coffeeMaker.checkInventory();
        final String[] inventoryComponents = unparsedInventory.split( "\n" );

        int coffee2 = 0;
        int milk2 = 0;
        int sugar2 = 0;
        int chocolate2 = 0;
        for ( int i = 0; i < inventoryComponents.length; i++ ) {
            final String c = inventoryComponents[i];
            if ( c.toLowerCase().contains( "coffee" ) ) {
                coffee2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "milk" ) ) {
                milk2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "sugar" ) ) {
                sugar2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
            if ( c.toLowerCase().contains( "chocolate" ) ) {
                chocolate2 = Integer.parseInt( c.replaceAll( "\\D+", "" ) );
            }
        }

        // Verify that the inventory is correct
        Assert.assertEquals( "Coffee not added correctly", inventoryData.newCoffee, coffee2 );
        Assert.assertEquals( "Milk not added correctly", inventoryData.newMilk, milk2 );
        Assert.assertEquals( "Sugar not added correctly", inventoryData.newSugar, sugar2 );
        Assert.assertEquals( "Chocolate not added correctly", inventoryData.newChocolate, chocolate2 );

    }

    /**
     * Ensure that an error was thrown while attempting to perform an action
     *
     * @param error
     *            The error message to check
     */
    @Then ( "^an error occurs for (.+)$" )
    public void errorThrown ( final String error ) {
        Assert.assertTrue( !error.isEmpty() );
    }

}
