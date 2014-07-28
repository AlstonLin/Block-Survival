package alston.minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Used as a generic data structure for any recipies, which requires an array of
 * type T extends Comparable, which maps to a single type T and the Class of U,
 * a type that represents nothing. When getting, the keys does not have to
 * directly match the keys given, but all non-U type keys must match.
 *
 * @author Alston
 * @version RTM
 */
public class RecipeMap<T extends Comparable> {

    //Fields
    private ArrayList<ArrayList<ArrayList<T>>> keys; //ArrayList with a 2D ArrayList of T as the Type Parameter
    private ArrayList<T> results; //The result of the keys; The index should corrospond the the index of Keys
    private final Class UClass; //Keeps track of U's class to compare for types

    /**
     * Creates a new RecipieMap by storing a refrence to the type U's class, and
     * initiating all other Objects.
     *
     * @param UClass The Class of type U which MUST be a subclass of T (Cannot
     * use assertion check due to generic type erasure)
     */
    public RecipeMap(Class UClass) {
        this.UClass = UClass;
        keys = new ArrayList();
        results = new ArrayList();
    }

    /**
     * Maps the given keys, removes all instances of U (that is not adjacent to
     * more than 1 instance of T) and the corrosponding result.
     *
     * @param key The key for the result
     * @param result The Object being mapped
     */
    public void put(T[][] key, T result) {
        ArrayList<ArrayList<T>> keysList = new ArrayList(); //New 2D ArrayList that the key will be converted to
        for (int i = 0; i < key.length; i++) { //Converts the 2D array to a 2D ArrayList
            keysList.add(new ArrayList(Arrays.asList(key[i])));
        }
        checkForClearing(keysList); //Checks for empty columns and rows before storing it
        if (key == null) { //If the parameter Key is null
            throw new NullPointerException("Cannot use null as key");
        }
        keys.add(keysList);
        results.add(result);
    }

    /**
     * Gets the Object that the given key maps to. If it does not map to
     * anything, it returns null.
     *
     * @param key The keys for the Object
     * @return The Object that the given keys map to; null if it does not map to
     * anything.
     */
    public T get(T[][] key) {
        int index;
        ArrayList<ArrayList<T>> keysList = new ArrayList(); //New 2D ArrayList that the key will be converted to
        for (int i = 0; i < key.length; i++) { //Converts the 2D array to a 2D ArrayList
            keysList.add(new ArrayList(Arrays.asList(key[i])));
        }
        checkForClearing(keysList); //Checks for empty columns and rows before getting it
        if (keysList.isEmpty()) { //Empty
            return null;
        }
        index = Collections.binarySearch(keys, keysList, Utility.ARRAY_LIST_COMPARATOR);
        return index <= -1 ? null : results.get(index); //Returns null if there was no result; otherwise returns the result
    }

    /**
     * Sorts the keys according to the Object's comparator (MUST be called
     * before searching).
     */
    public void sortKeys() {
        ArrayList<ArrayList<ArrayList<T>>> tempKeys = (ArrayList<ArrayList<ArrayList<T>>>)keys.clone(); //Keeps track of order before, to sort the results later
        ArrayList<T> newResults = new ArrayList<T> ();
        Collections.sort(keys, Utility.ARRAY_LIST_COMPARATOR);
        //Sorts the results according to the keys
        for (ArrayList<ArrayList<T>> key : keys){
            newResults.add(results.get(tempKeys.indexOf(key)));
        }
        results = newResults;
    }

    /**
     * Checks if there are any columns/rows containing only type U to remove.
     *
     * @param key The key to check
     */
    private void checkForClearing(ArrayList<ArrayList<T>> key) {
        for (int i = 0; i < key.size(); i++) { //Checks every row first
            boolean onlyU = true; //Keeps track if there was a instance that is not type U
            for (int j = 0; j < key.get(0).size(); j++) { //Every index (column) of the row
                if (!UClass.isInstance(key.get(i).get(j))) { //Not an instance of U (cannot use instanceof due to type erasure of generics)
                    onlyU = false;
                }
            }
            if (onlyU) { //There was only instances of U in the row; deletes the row
                key.remove(i);
                i--; //Accounts for the row that was deleted 
            }
        }
        if (key.isEmpty()) { //Key w/ all EmptyItems
            return;
        }
        for (int i = 0; i < key.get(0).size(); i++) { //Checks every column
            boolean onlyU = true; //Keeps track if there was a instance that is not type U
            for (int j = 0; j < key.size(); j++) { //Every index (row) of the column
                if (!UClass.isInstance(key.get(j).get(i))) { //The key is not an instance of U
                    onlyU = false;
                }
            }
            if (onlyU) { //There was only instances of U in the row; deletes the row
                for (int j = 0; j < key.size(); j++) { //Removes the 1 Object at the index in every row
                    key.get(j).remove(i);
                }
                i--; //Accounts for the column that was deleted
            }
        }
    }
}
