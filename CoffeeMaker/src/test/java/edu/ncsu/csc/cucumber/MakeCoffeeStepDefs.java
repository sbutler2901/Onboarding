package edu.ncsu.csc.cucumber;

import org.junit.Assert;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.ncsu.csc.coffee_maker.models.CoffeeMaker;
import edu.ncsu.csc.coffee_maker.models.persistent.Recipe;
import edu.ncsu.csc.test_utils.SharedCoffeeMakerData;

/**
 * StepDefs (Cucumber) for Making Coffee.
 *
 * @author Kai Presler-Marshall
 * @author Sarah Elder
 *
 */
public class MakeCoffeeStepDefs {
    private final CoffeeMaker           coffeeMaker;
    private final SharedCoffeeMakerData makerData;

    /**
     * Constructor for the StepDefs
     *
     * @param coffeemaker
     *            The CoffeeMaker object used by the system
     * @param md
     *            The CoffeeMakerData that is used to ensure that actions taken
     *            resulted in the correct result
     */
    public MakeCoffeeStepDefs ( final CoffeeMaker coffeemaker, final SharedCoffeeMakerData md ) {
        this.coffeeMaker = coffeemaker;
        this.makerData = md;
    }

    /**
     * Sets the CoffeeMaker's Inventory to contain the ingredients specified
     * here.
     *
     * @param originalCoffee
     *            The amount of Coffee to set the Inventory to.
     * @param originalMilk
     *            The amount of Milk to set the Inventory to.
     * @param originalSugar
     *            The amount of Sugar to set the Inventory to.
     * @param originalChocolate
     *            The amount of Chocolate to set the Inventory to.
     */
    @Given ( "^(\\d+) coffee, (\\d+) milk, (\\d+) sugar, and (\\d+) chocolate currently in the CoffeeMaker$" )
    public void initialInventory ( final int originalCoffee, final int originalMilk, final int originalSugar,
            final int originalChocolate ) {
        makerData.originalCoffee = originalCoffee;
        makerData.originalMilk = originalMilk;
        makerData.originalSugar = originalSugar;
        makerData.originalChocolate = originalChocolate;

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
     * Deletes the existing batch of recipies in the CoffeeMaker and then
     * populates it with a new batch of Recipies.
     *
     * @param numRecipes
     *            The number of recipies that should be in the RecipeBook
     * @throws Exception
     *             If the number of recipies to add is greater than the max
     *             allowed
     */
    @Given ( "^the CoffeeMaker has (\\d+) Recipes$" )
    public void existingRecipes ( final int numRecipes ) throws Exception {
        // Check current number of recipes in Recipe Book
        final int total = this.coffeeMaker.getRecipes().size();
        if ( total != 0 ) {
            int count = 0;
            while ( count < total ) {
                this.coffeeMaker.deleteRecipe( 0 );
                count++;
            }
            Assert.assertEquals( 0, this.coffeeMaker.getRecipes().size() );
        }

        if ( numRecipes == 0 ) {
            return; // no need to execute the rest of the code
        }
        if ( numRecipes > 3 ) {
            throw new IllegalArgumentException( "Number of Recipes cannot be greater than 3" );
        }
        else {
            for ( int i = 1; i <= numRecipes; i++ ) {
                try {
                    final Recipe testR = new Recipe();
                    try {
                        testR.setName( "Recipe" + i );
                        final Integer pr = new Integer( i * 10 );
                        testR.setPrice( pr );
                        testR.setCoffee( new Integer( i ) );
                        testR.setMilk( 1 );
                        testR.setSugar( 1 );
                        testR.setChocolate( 1 );
                    }
                    catch ( final Exception e ) {
                        Assert.fail( "Error in creating recipes" );
                    }
                    this.coffeeMaker.addRecipe( testR );

                }
                catch ( final Exception e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Creates a new Recipe for the COffeeMaker using the parameters specified.
     *
     * @param name
     *            Name of the new Recipe
     * @param cost
     *            Cost of the new Recipe
     * @param coffeeAmt
     *            Amount of Coffee to be used by the new Recipe
     * @param milkAmt
     *            Amount of Milk to be used by the new Recipe
     * @param sugarAmt
     *            Amount of Sugar to be used by the new Recipe
     * @param chocolateAmt
     *            Amount of Chocolate to be used by the new Recipe
     */
    @When ( "^I submit values for name: (.+); cost: (\\d+); and ingredients: (\\d+) coffee, (\\d+) milk, (\\d+) sugar, (\\d+) chocolate$" )
    public void addSpecificRecipe ( final String name, final int cost, final int coffeeAmt, final int milkAmt,
            final int sugarAmt, final int chocolateAmt ) {
        makerData.errorMessage = "";
        final Recipe newR = new Recipe();
        try {

            newR.setName( name );
            newR.setPrice( cost );
            newR.setCoffee( coffeeAmt );
            newR.setMilk( milkAmt );
            newR.setSugar( sugarAmt );
            newR.setChocolate( chocolateAmt );
        }
        catch ( final Exception e ) {
            makerData.errorMessage = e.getMessage();
            Assert.fail( "Unable to create new Recipe" );
        }
        makerData.currentRecipe = newR;
        coffeeMaker.addRecipe( newR );
    }

    /**
     * Execute the CoffeeMaker's MakeCoffee action with the name of the recipe
     * specified and the amount of money paid.
     *
     * @param recipeName
     *            Name of the recipe to use for making Coffee
     * @param sMoney
     *            Amount of money that is paid
     */
    @When ( "^I make coffee with the recipe for (.+) and input (.+) money$" )
    public void validMakeCoffee ( final String recipeName, final String sMoney ) {
        try {
            try {
                final int money = Integer.parseInt( sMoney );
                makerData.moneyGiven = money;
                makerData.change = coffeeMaker.makeCoffee( makerData.currentRecipe, money );
            }
            catch ( final NumberFormatException nfe ) {
                throw new NumberFormatException( "Money must be a positive integer" );
            }
        }
        catch ( final Exception e ) {
            makerData.errorMessage = e.getMessage();
        }
    }

    /**
     * Ensure that the coffee was successfully made and the change given was the
     * correct amount for the recipe price and the amount paid
     */
    @Then ( "^the coffee is successfully made with correct change$" )
    public void validMakeCoffeeResult () {
        Assert.assertEquals( "", makerData.errorMessage );

        final int expectedChange = makerData.moneyGiven - makerData.currentRecipe.getPrice();
        Assert.assertEquals( expectedChange, makerData.change );
    }

    /**
     * Ensure that if the CoffeeMaker failed to make coffee properly that all of
     * the money provided was returned as change
     */
    @Then ( "^the coffee maker returns your money$" )
    public void invalidMakeCoffeeResult () {
        Assert.assertEquals( makerData.moneyGiven, makerData.change );
    }

    /**
     * Ensure that updating the inventory or making coffee results in the
     * expected inventory
     */
    @Then ( "^the inventory is updated correctly$" )
    public void validInventoryUpdate () {
        final int expectedCoffee = makerData.originalCoffee - makerData.currentRecipe.getCoffee();
        final int expectedMilk = makerData.originalMilk - makerData.currentRecipe.getMilk();
        final int expectedSugar = makerData.originalSugar - makerData.currentRecipe.getSugar();
        final int expectedChocolate = makerData.originalChocolate - makerData.currentRecipe.getChocolate();

        Assert.assertEquals( expectedCoffee, coffeeMaker.getInventory().getCoffee() );
        Assert.assertEquals( expectedMilk, coffeeMaker.getInventory().getMilk() );
        Assert.assertEquals( expectedSugar, coffeeMaker.getInventory().getSugar() );
        Assert.assertEquals( expectedChocolate, coffeeMaker.getInventory().getChocolate() );
    }

    /**
     * Ensure that an error occurring results in the inventory _not_ being
     * changed
     */
    @Then ( "^the inventory is not changed$" )
    public void inventoryNotChanged () {
        Assert.assertEquals( makerData.originalCoffee, coffeeMaker.getInventory().getCoffee() );
        Assert.assertEquals( makerData.originalMilk, coffeeMaker.getInventory().getMilk() );
        Assert.assertEquals( makerData.originalSugar, coffeeMaker.getInventory().getSugar() );
        Assert.assertEquals( makerData.originalChocolate, coffeeMaker.getInventory().getChocolate() );
    }

}
