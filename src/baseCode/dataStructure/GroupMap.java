package baseCode.dataStructure;

import java.util.*;

import baseCode.dataStructure.reader.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Institution:: Columbia University</p>
 * @author Paul Pavlidis
 * @version $Id$
 */

public class GroupMap {

   private int uniqueItems;
   private Map duplicateMap;

   public int getUniqueItems() { return uniqueItems; }

    /**
     *
     * @param k String
     * @return boolean
     */
    public boolean hasDuplicates(String k) {
      return numDuplicates(k) > 0;
   }

    /**
     *
     * @param k String
     * @return int
     */
    public int numDuplicates(String k) {
       return ((HashSet)duplicateMap.get(k)).size();
   }


    /**
     *
     * @param filename Duplicate map file name to be read.
     * @param dataMatrix Data file this the map refers to.
     * @return Map
     */
    public Map read(String filename, DenseDoubleMatrix2DNamed dataMatrix) {

     if (filename == null || dataMatrix == null) {
        throw new IllegalArgumentException("You must give a valid file name and data matrix.");
     }

     MapReader m = new MapReader();
     Map initialMap = m.read(filename);
     Map insideOutMap = new HashMap();

     // first we turn the map inside-out so we have gene --> probeA, probeB, probeC.
     Set keys = initialMap.keySet();
     for (Iterator it = keys.iterator(); it.hasNext(); ) {
        String p = (String) it.next();

        if (!dataMatrix.hasRow(p)) {
           continue;
        }

        String v = (String) initialMap.get(p);

        if (insideOutMap.get(v) == null) {
           insideOutMap.put(v, new ArrayList());
        }
        ( (ArrayList) insideOutMap.get(v)).add(p);
     }

     uniqueItems = insideOutMap.size();
     System.err.println(uniqueItems + " unique items read from duplicate map");

     // turn that map into a map of probeA --> probe1, probe2, where probes1 and probes2 are in the same 'group' as probeA
     this.duplicateMap = new HashMap();
     keys = insideOutMap.keySet();
     for (Iterator it = keys.iterator(); it.hasNext(); ) {
        String g = (String) it.next();
        ArrayList v = (ArrayList) insideOutMap.get(g);

        for (Iterator vit = v.iterator(); vit.hasNext(); ) {
           String p = (String) vit.next();
           if (duplicateMap.get(p) == null) {
              duplicateMap.put(p, new HashSet());
           }

           for (Iterator kit = v.iterator(); kit.hasNext(); ) {
              String pp = (String) kit.next();
              if (p.equals(pp)) {
                 continue;
              }

              ( (Set) duplicateMap.get(p)).add(pp);
           }
        }
     }

     // now a sanity check. Make sure every item in our data is also in the duplicate map.
     for (Iterator it = dataMatrix.getRowNameMapIterator(); it.hasNext(); ) {
        boolean foundProblem = false;
        String rowName = (String) it.next();
        if (!duplicateMap.containsKey(rowName)) {
           duplicateMap.put(rowName, new HashSet());
           foundProblem = true;
        }
        if (foundProblem) {
           //  throw new RuntimeException("The data has item(s) that aren't in the duplicate map.");
        }
     }

     return this.duplicateMap;

  }

}
