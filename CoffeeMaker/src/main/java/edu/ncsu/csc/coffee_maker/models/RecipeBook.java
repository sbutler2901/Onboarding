package edu.ncsu.csc.coffee_maker.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.hibernate.Session;

import edu.ncsu.csc.coffee_maker.models.persistent.Recipe;
import edu.ncsu.csc.coffee_maker.util.HibernateUtil;

/**
 * Contains the list of recipse
 *
 * @author Sarah Heckman
 * @author Kai Presler-Marshall
 * @author Elizabeth Gilbert
 */
public class RecipeBook {

    /** Array of recipes in coffee maker */
    private List<Recipe> recipesList;

    /**
     * Default constructor for a RecipeBook.
     */
    public RecipeBook () {
        // Get all recipes from the DB
        updateRecipes();
    }

    /**
     * Update the class's Recipe list to be persistent with DB
     */
    @SuppressWarnings ( "unchecked" )
    public void updateRecipes () {
        // Get all recipes from the DB
        final Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        recipesList = new Vector<Recipe>( 3 );

        final ArrayList<Recipe> result = (ArrayList<Recipe>) session.createQuery( "from Recipe" ).list();
        for ( final Recipe recipe : (List<Recipe>) result ) {
            this.recipesList.add( recipe );
            System.out.println( "Recipe: " + recipe.getName() );
        }

        session.getTransaction().commit();
        session.close();
    }

    /**
     * Returns recipes as a list.
     *
     * @return list of recipes
     */
    public synchronized List<Recipe> getRecipes () {
        return recipesList;
    }

    /**
     * Returns the Recipe associated with the given name. If there is no recipe
     * with the name, null is returned.
     *
     * @param name
     *            name to search for
     * @return Recipe with the name
     */
    public synchronized Recipe findRecipe ( final String name ) {
        for ( final Recipe r : recipesList ) {
            if ( r.getName().equalsIgnoreCase( name ) ) {
                return r;
            }
        }
        return null;
    }

    /**
     * Returns true if a recipe was added to the RecipeBook.
     *
     * @param r
     *            recipe to add
     * @return true if added
     */
    public synchronized boolean addRecipe ( final Recipe r ) {
        if ( recipesList.contains( r ) || recipesList.size() >= 3 ) {
            return false; // New recipe was not added
        }

        // Add to DB
        final Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save( r );
        session.getTransaction().commit();
        session.close();

        // Update list
        updateRecipes();

        // New recipe was added
        return true;
    }

    /**
     * Returns the name of the recipe deleted at the position specified and null
     * if the recipe does not exist.
     *
     * @param recipeToDelete
     *            recipe id to delete
     * @return name of deleted recipe
     */
    public synchronized String deleteRecipe ( final int recipeToDelete ) {
        return deleteRecipe( recipesList.get( recipeToDelete ) );
    }

    /**
     * eturns the name of the recipe deleted at the position specified and null
     * if the recipe does not exist.
     *
     * @param recipeToDelete
     *            recipe to delete
     * @return name of deleted recipe
     * @throws IllegalArgumentException
     */
    public synchronized String deleteRecipe ( final Recipe recipeToDelete ) throws IllegalArgumentException {
        final int index = recipesList.indexOf( recipeToDelete );
        if ( index < 0 ) {
            throw new IllegalArgumentException( "Recipe does not exist" );
        }

        final Recipe r = recipesList.get( index );

        if ( r != null ) {
            // Update the DB
            final Session session = HibernateUtil.getSessionFactory().openSession();
            final Recipe obj = session.load( Recipe.class, r.getId() );
            session.delete( obj );

            // This makes the pending delete to be done
            session.flush();
            session.close();

            // Update list
            updateRecipes();
        }

        return null;
    }

    /**
     * Returns the name of the recipe edited at the position specified and null
     * if the recipe does not exist.
     *
     * @param recipeToEdit
     *            recipe id to edit
     * @param newRecipe
     *            edited recipe
     * @return name of edited recipe
     */
    public synchronized String editRecipe ( final int recipeToEdit, final Recipe newRecipe ) {
        final Recipe r = recipesList.get( recipeToEdit );

        if ( r != null ) {

            // Update `r` to reflect the changes in `newRecipe`
            r.updateRecipe( newRecipe );

            // Use updated `r` to update the DB
            final Session session = HibernateUtil.getSessionFactory().openSession();
            session.update( r );

            // This makes the pending delete to be done
            session.flush();
            session.close();

            // Update the list
            updateRecipes();

            return r.getName();
        }
        else {
            return null;
        }
    }

}
