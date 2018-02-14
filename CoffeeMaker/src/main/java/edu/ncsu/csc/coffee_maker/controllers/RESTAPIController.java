package edu.ncsu.csc.coffee_maker.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc.coffee_maker.Application;
import edu.ncsu.csc.coffee_maker.models.persistent.Inventory;
import edu.ncsu.csc.coffee_maker.models.persistent.Recipe;
import edu.ncsu.csc.coffee_maker.services.InventoryService;

/**
 * This is the single controller in the CoffeeMaker application that handles
 * REST API endpoints In a larger application, we would want multiple REST API
 * controllers, one per model.
 *
 * Spring will automatically convert all of the ResponseEntity and List results
 * to JSON
 *
 * @author Kai Presler-Marshall
 *
 */
@SuppressWarnings ( { "unchecked", "rawtypes" } )
@RestController
public class RESTAPIController {

    /**
     * The InventoryService is a Spring Service used to provide access to the
     * Inventory for API requests
     */
    @Autowired
    InventoryService              inventoryService;

    /**
     * This is the base path for the APIs. All API routes are relative to this
     * path. By keeping it in a single variable, it removes redundant
     * information and makes it easier to change if the API is
     * modified/re-versioned
     */
    static final protected String BASE_PATH = "/api/v1/";

    /**
     * REST API method to make coffee by completing a POST request with the ID
     * of the recipe as the path variable and the amount that has been paid as
     * the body of the response
     *
     * @param id
     *            recipe id
     * @param amtPaid
     *            amount paid
     * @return The change the customer is due if successful
     */
    @PostMapping ( BASE_PATH + "/makecoffee/{id}" )
    public ResponseEntity makeCoffee ( @PathVariable ( "id" ) final String id, @RequestBody final int amtPaid ) {
        final Recipe recipe = Application.getCoffeeMaker().getRecipeBook().findRecipe( id );

        try {
            System.out.println( "recipe: " + recipe.getName() + "    amt: " + amtPaid );
            final int change = Application.getCoffeeMaker().makeCoffee( recipe, amtPaid );
            System.out.println( "change: " + change );

            return new ResponseEntity<String>( "{\"result\":\"success\", \"change\":" + change + "}", HttpStatus.OK );
        }
        catch ( final IllegalArgumentException e ) {
            System.out.println( e.getMessage() );
            return new ResponseEntity( e.getMessage(), HttpStatus.NOT_FOUND );
        }
        catch ( final IllegalStateException e ) {
            System.out.println( e.getMessage() );
            return new ResponseEntity( e.getMessage(), HttpStatus.CONFLICT );
        }

    }

    /**
     * REST API method to provide GET access to all recipes in the system
     *
     * @return JSON representation of all recipies
     */
    @GetMapping ( BASE_PATH + "/recipes" )
    public List<Recipe> getRecipes () {
        return Application.getCoffeeMaker().getRecipes();
    }

    /**
     * REST API method to provide GET access to a specific recipe, as indicated
     * by the path variable provided (the name of the recipe desired)
     *
     * @param id
     *            recipe id
     * @return response to the request
     */
    @GetMapping ( BASE_PATH + "/recipes/{id}" )
    public ResponseEntity getRecipe ( @PathVariable ( "id" ) final String id ) {
        final Recipe recipe = Application.getCoffeeMaker().getRecipeBook().findRecipe( id );
        return null == recipe ? new ResponseEntity( "No recipe found for id " + id, HttpStatus.NOT_FOUND )
                : new ResponseEntity( recipe, HttpStatus.OK );
    }

    /**
     * REST API method to provide POST access to the Recipe model. This is used
     * to create a new Recipe by automatically converting the JSON RequestBody
     * provided to a Recipe object. Invalid JSON will fail.
     *
     * @param recipe
     *            The valid Recipe to be saved.
     * @return ResponseEntity indicating success if the Recipe could be saved to
     *         the inventory, or an error if it could not be
     */
    @PostMapping ( BASE_PATH + "/recipes" )
    public ResponseEntity createRecipe ( @RequestBody final Recipe recipe ) {
        if ( null != Application.getCoffeeMaker().getRecipeBook().findRecipe( recipe.getName() ) ) {
            return new ResponseEntity( "Recipe with the name " + recipe.getName() + " already exists",
                    HttpStatus.CONFLICT );
        }
        try {
            Application.getCoffeeMaker().getRecipeBook().addRecipe( recipe );
            return new ResponseEntity<String>( "{\"result\":\"success\"}", HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity( "Insufficient space in recipe book for recipe " + recipe.getName(),
                    HttpStatus.INSUFFICIENT_STORAGE );
        }

    }

    /**
     * REST API method to allow deleting a Recipe from the CoffeeMaker's
     * Inventory, by making a DELETE request to the API endpoint and indicating
     * the recipe to delete (as a path variable)
     *
     * @param id
     *            The name of the Recipe to delete
     * @return Success if the recipe could be deleted; an error if the recipe
     *         does not exist
     */
    @DeleteMapping ( BASE_PATH + "/recipes/{id}" )
    public ResponseEntity deleteRecipe ( @PathVariable final String id ) {
        final Recipe recipe = Application.getCoffeeMaker().getRecipeBook().findRecipe( id );
        if ( null == recipe ) {
            return new ResponseEntity( "No recipe found for name " + id, HttpStatus.NOT_FOUND );
        }
        Application.getCoffeeMaker().getRecipeBook().deleteRecipe( recipe );

        return new ResponseEntity<String>( "{\"result\":\"success\"}", HttpStatus.OK );
    }

    /**
     * REST API endpoint to provide GET access to the CoffeeMaker's singleton
     * Inventory. This will convert the Inventory to JSON.
     *
     * @return response to the request
     */
    @GetMapping ( BASE_PATH + "/inventory" )
    public ResponseEntity getInventory () {
        final Inventory inventory = inventoryService.getInventory();
        return new ResponseEntity( inventory, HttpStatus.OK );
    }

    /**
     * REST API endpoint to provide update access to CoffeeMaker's singleton
     * Inventory. This will update the Inventory of the CoffeeMaker by adding
     * amounts from the Inventory provided to the CoffeeMaker's stored inventory
     *
     * @param inventory
     *            amounts to add to inventory
     * @return response to the request
     */
    @PutMapping ( BASE_PATH + "/inventory" )
    public ResponseEntity updateInventory ( @RequestBody @Valid final Inventory inventory ) {
        inventoryService.addInventory( inventory );

        return new ResponseEntity( inventoryService.getInventory(), HttpStatus.OK );
    }
}
