package edu.ncsu.csc.coffee_maker.models;

import java.util.List;

import javax.validation.Valid;

import edu.ncsu.csc.coffee_maker.models.persistent.Inventory;
import edu.ncsu.csc.coffee_maker.models.persistent.Recipe;

/**
 * Represents the CoffeeMaker.
 *
 * @author Sarah Heckman
 * @author Kai Presler-Marshall
 * @author Elizabeth Gilbert
 */
public class CoffeeMaker {
    /** Array of recipes in coffee maker */
    private final RecipeBook recipeBook;
    /** Inventory of the coffee maker */
    private final Inventory  inventory;

    /**
     * Constructor for the coffee maker
     *
     */
    public CoffeeMaker () {
        recipeBook = new RecipeBook();

        inventory = new Inventory();
        inventory.pullFromDB();
    }

    /**
     * Returns the inventory.
     *
     * @return the inventory
     */
    public Inventory getInventory () {
        return inventory;
    }

    /**
     * Returns the RecipeBook.
     *
     * @return the recipe book
     */
    public RecipeBook getRecipeBook () {
        return recipeBook;
    }

    /**
     * Returns true if the recipe is added to the list of recipes in the
     * CoffeeMaker and false otherwise.
     *
     * @param r
     *            recipe to add
     * @return true if added
     */
    public boolean addRecipe ( @Valid final Recipe r ) {
        return recipeBook.addRecipe( r );
    }

    /**
     * Returns the name of the successfully deleted recipe or null if the recipe
     * cannot be deleted.
     *
     * @param recipeToDelete
     *            id of recipe to delete
     * @return name of deleted recipe
     */
    public String deleteRecipe ( final int recipeToDelete ) {
        return recipeBook.deleteRecipe( recipeToDelete );
    }

    /**
     * Returns the name of the successfully deleted recipe or null if the recipe
     * cannot be deleted.
     *
     * @param recipeToDelete
     *            Recipe object to delete
     * @return name of delete recipe
     */
    public String deleteRecipe ( final Recipe recipeToDelete ) {
        return recipeBook.deleteRecipe( recipeToDelete );
    }

    /**
     * Returns the name of the successfully edited recipe or null if the recipe
     * cannot be edited.
     *
     * @param recipeToEdit
     *            id of recipe to edit
     * @param r
     *            Recipe containing edits
     * @return name of recipe edited
     */
    public String editRecipe ( final int recipeToEdit, final Recipe r ) {
        return recipeBook.editRecipe( recipeToEdit, r );
    }

    /**
     * Returns true if inventory was successfully added
     *
     * @param amtCoffee
     *            amount coffee
     * @param amtMilk
     *            amount milk
     * @param amtSugar
     *            amount sugar
     * @param amtChocolate
     *            amount chocolate
     */
    public synchronized void addInventory ( final int amtCoffee, final int amtMilk, final int amtSugar,
            final int amtChocolate ) throws IllegalArgumentException {
        inventory.addIngredients( amtCoffee, amtMilk, amtSugar, amtChocolate );
    }

    /**
     * Returns the inventory of the coffee maker
     *
     * @return Inventory
     */
    public synchronized String checkInventory () {
        return inventory.toString();
    }

    /**
     * Returns the change of a user's beverage purchase, or the user's money if
     * the beverage cannot be made
     *
     * @param recipeToPurchase
     *            recipe to purchase
     * @param amtPaid
     *            amount paid for beverage
     * @return change from purchase
     */
    public synchronized int makeCoffee ( final int recipeToPurchase, final int amtPaid ) {
        final Recipe toPurchase = getRecipes().get( recipeToPurchase );
        return makeCoffee( toPurchase, amtPaid );
    }

    /**
     * Makes the requested coffee and removes the needed ingredients from the
     * inventory.
     *
     * @param toPurchase
     *            recipe to purchase
     * @param amtPaid
     *            amount paid for beverage
     * @return change from purchase
     */
    public synchronized int makeCoffee ( final Recipe toPurchase, final int amtPaid ) {
        int change = amtPaid;
        inventory.pullFromDB();

        if ( toPurchase == null ) {
            throw new IllegalArgumentException( "Recipe not found" );
        }
        else if ( toPurchase.getPrice() <= amtPaid ) {
            if ( inventory.useIngredients( toPurchase ) ) {
                change = amtPaid - toPurchase.getPrice();
            }
            else {
                // Not enough inventory
                return change;
            }
        }

        // Not enough money paid
        return change;
    }

    /**
     * Returns the list of Recipes in the RecipeBook.
     *
     * @return Recipe []
     */
    public synchronized List<Recipe> getRecipes () {
        return recipeBook.getRecipes();
    }
}
