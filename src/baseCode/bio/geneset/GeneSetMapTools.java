package baseCode.bio.geneset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import baseCode.util.StatusViewer;

/**
 * Methods to 'clean' a set of geneSets - to remove redundancies, for example.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetMapTools {

   public static void removeBySize( GeneAnnotations ga, StatusViewer messenger,
         int minClassSize, int maxClassSize ) {

      Map geneSetToGeneMap = ga.getClassToGeneMap();

      Set removeUs = new HashSet();
      for ( Iterator iter = geneSetToGeneMap.keySet().iterator(); iter
            .hasNext(); ) {
         String geneSet = ( String ) iter.next();

         Set element;
         if ( geneSetToGeneMap.containsKey( geneSet ) ) {
            element = ( Set ) geneSetToGeneMap.get( geneSet );
            if ( element.size() < minClassSize || element.size() > maxClassSize ) {
               removeUs.add( geneSet );
            }
         }
      }

      for ( Iterator iter = removeUs.iterator(); iter.hasNext(); ) {
         String geneSet = ( String ) iter.next();
         ga.removeClassFromMaps( geneSet );
      }

      ga.resetSelectedSets();
      ga.sortGeneSets();

      if ( messenger != null ) {
         messenger.setStatus( "There are now " + ga.numClasses()
               + " sets remaining after removing sets with excluded sizes." );
      }

   }

   /**
    * <p>
    * Remove classes which are too similar to some other class. Classes which have fractionSameThreshold of a larger
    * class will be ignored. This doesn't know which classes are relevant to the data, so it does not work perfectly.
    * The algorithm is: for each class, compare it to all other classes, starting with small classes. If any class
    * encountered is nearly the same as the query class (with tolerance fractionSameThreshold), the smaller of the two
    * classes is deleted and the query continues with the class that is left.
    * </p>
    * 
    * @param fractionSameThreshold A value between 0 and 1, indicating how similar a class must be before it gets
    *        ditched.
    * @param ga
    * @param messenger For updating a log.
    * @param maxClassSize Large class considered. (that doesn't mean they are removed)
    * @param minClassSize Smallest class considered. (that doesn't mean they are removed)
    */
   public static void ignoreSimilar( double fractionSameThreshold,
         GeneAnnotations ga, StatusViewer messenger, int maxClassSize,
         int minClassSize ) {

      Map classesToSimilarMap = new LinkedHashMap();
      Set seenit = new HashSet();
      Set deleteUs = new HashSet();

      if ( messenger != null ) {
         messenger.setStatus( "...Highly (" + fractionSameThreshold * 100
               + "%)  similar classes are being removed..." + ga.numClasses()
               + " to start..." );
      }

      // iterate over all the classes, starting from the smallest one.

      List sortedList = ga.sortGeneSetsBySize();

      // OUTER
      for ( Iterator iter = sortedList.iterator(); iter.hasNext(); ) {
         String queryClassId = ( String ) iter.next();
         Set queryClass = ( Set ) ga.getClassToGeneMap().get( queryClassId );

         int querySize = queryClass.size();

         if ( querySize > maxClassSize || querySize < minClassSize ) {
            continue;
         }

         // INNER
         for ( Iterator iterb = sortedList.iterator(); iterb.hasNext(); ) {
            String targetClassId = ( String ) iterb.next();

            /// skip self comparisons and also symmetric comparisons.
            if ( targetClassId.equals( queryClassId )
                  || seenit.contains( targetClassId ) ) {
               continue;
            }
            seenit.add( targetClassId );

            Set targetClass = ( Set ) ga.getClassToGeneMap()
                  .get( targetClassId );

            int targetSize = targetClass.size();
            if ( targetSize > maxClassSize || targetSize < minClassSize ) {
               continue;
            }

            if ( targetSize < querySize ) {
               if ( areSimilarClasses( queryClass, targetClass,
                     fractionSameThreshold ) ) {
                  deleteUs.add( targetClassId );

                  storeSimilarSets( classesToSimilarMap, targetClassId,
                        queryClassId );
                  //     at this point there is no reason to keep looking at the target - it was removed.
               }
            } else {

               if ( areSimilarClasses( targetClass, queryClass,
                     fractionSameThreshold ) ) {
                  deleteUs.add( queryClassId );
                  storeSimilarSets( classesToSimilarMap, queryClassId,
                        targetClassId );
                  queryClassId = targetClassId; // swap. The query class is being deleted, so we should skip it. Move on
                                                // to the target.
                  queryClass = targetClass;
                  break; // if we do the swap, target and query are equal.
               }
            }
         } /* inner while */
         seenit.add( queryClassId );
      }
      /* end while ... */

      /* remove the ones we don't want to keep */
      Iterator itrd = deleteUs.iterator();
      while ( itrd.hasNext() ) {
         String deleteMe = ( String ) itrd.next();
         ga.removeClassFromMaps( deleteMe );
      }

      ga.resetSelectedSets();
      ga.sortGeneSets();

      if ( messenger != null ) {
         messenger.setStatus( "There are now " + ga.numClasses()
               + " classes represented on the chip (" + deleteUs.size()
               + " were ignored)" );
      }
   }

   /* ignoreSimilar */

   /**
    * @param classesToSimilarMap
    * @param queryClassId
    * @param targetClassId
    */
   private static void storeSimilarSets( Map classesToSimilarMap,
         String queryClassId, String targetClassId ) {
      if ( !classesToSimilarMap.containsKey( targetClassId ) ) {
         classesToSimilarMap.put( targetClassId, new HashSet() );
      }
      if ( !classesToSimilarMap.containsKey( queryClassId ) ) {
         classesToSimilarMap.put( queryClassId, new HashSet() );

      }
      ( ( HashSet ) classesToSimilarMap.get( queryClassId ) )
            .add( targetClassId );
      ( ( HashSet ) classesToSimilarMap.get( targetClassId ) )
            .add( queryClassId );
   }

   /**
    * Helper function for ignoreSimilar
    */
   private static boolean areSimilarClasses( Set biggerClass, Set smallerClass,
         double fractionSameThreshold ) {

      if ( biggerClass.size() < smallerClass.size() ) {
         throw new IllegalArgumentException( "Invalid sizes" );
      }

      int notInThresh = ( int ) Math.ceil( fractionSameThreshold
            * smallerClass.size() );

      int notin = 0;
      for ( Iterator iter = smallerClass.iterator(); iter.hasNext(); ) {

         String probe = ( String ) iter.next();
         if ( !biggerClass.contains( probe ) ) {
            notin++;
         }
         if ( notin > notInThresh ) {
            return false;
         }
      }

      /* return true is the count is high enough */
      return true;

   }

   /**
    * Identify classes which are absoluely identical to others. This isn't superfast, because it doesn't know which
    * classes are actually relevant in the data.
    */
   public static void collapseClasses( GeneAnnotations geneData,
         StatusViewer messenger ) {
      Map setToGeneMap = geneData.getClassToGeneMap();
      Map classesToRedundantMap = geneData.getClassesToRedundantMap();
      LinkedHashMap seenClasses = new LinkedHashMap();
      LinkedHashMap sigs = new LinkedHashMap();

      HashMap seenit = new HashMap();

      if ( messenger != null ) {
         messenger
               .setStatus( "There are "
                     + geneData.numClasses()
                     + " classes represented on the chip (of any size). Redundant classes are being removed..." );
      }

      // sort each arraylist in for each go and create a string that is a signature for this class.
      int ignored = 0;
      for ( Iterator iter = setToGeneMap.keySet().iterator(); iter.hasNext(); ) {
         String classId = ( String ) iter.next();
         Set classMembers = ( Set ) setToGeneMap.get( classId );

         // todo - hack : Skip classes that are huge. It's too slow
         // otherwise. This is a total heuristic. Note that this
         // doesn't mean the class won't get analyzed, it just
         // means we don't bother looking for redundancies. Big
         // classes are less likely to be identical to others,
         // anyway. In tests, the range shown below has no effect
         // on the results, but it _could_ matter.
         if ( classMembers.size() > 250 || classMembers.size() < 2 ) {
            continue;
         }

         Vector cls = new Vector( classMembers );
         Collections.sort( cls );
         String signature = "";
         seenit.clear();
         Iterator classit = cls.iterator();
         while ( classit.hasNext() ) {
            String probeid = ( String ) classit.next();
            if ( !seenit.containsKey( probeid ) ) {
               signature = signature + "__" + probeid;
               seenit.put( probeid, new Boolean( true ) );
            }
         }
         sigs.put( classId, signature );
      }

      // look at the signatures for repeats.
      for ( Iterator iter = sigs.keySet().iterator(); iter.hasNext(); ) {
         String classId = ( String ) iter.next();
         String signature = ( String ) sigs.get( classId );

         // if the signature has already been seen, add it to the redundant
         // list, and remove this class from the classToProbeMap.
         if ( seenClasses.containsKey( signature ) ) {
            if ( !classesToRedundantMap.containsKey( seenClasses
                  .get( signature ) ) ) {
               classesToRedundantMap.put( seenClasses.get( signature ),
                     new ArrayList() );

            }
            ( ( ArrayList ) classesToRedundantMap.get( seenClasses
                  .get( signature ) ) ).add( classId );
            ignored++;
            geneData.removeClassFromMaps( classId );
            //		System.err.println(classId + " is the same as an existing class, " + seenClasses.get(signature));
         } else {
            // add string to hash
            seenClasses.put( signature, classId );
         }
      }

      geneData.resetSelectedSets();
      geneData.sortGeneSets();

      if ( messenger != null ) {
         messenger.setStatus( "There are now " + geneData.numClasses()
               + " classes represented on the chip (" + ignored
               + " were removed)" );
      }
   }

   /**
    * @param classId
    * @param classesToRedundantMap
    * @return
    */
   public static ArrayList getRedundancies( String classId,
         Map classesToRedundantMap ) {
      if ( classesToRedundantMap != null
            && classesToRedundantMap.containsKey( classId ) ) {
         return ( ArrayList ) classesToRedundantMap.get( classId );
      }
      return null;

   }

   /**
    * @param classId
    * @param classesToSimilarMap
    * @return
    */
   public static ArrayList getSimilarities( String classId,
         Map classesToSimilarMap ) {
      if ( classesToSimilarMap != null
            && classesToSimilarMap.containsKey( classId ) ) {
         return ( ArrayList ) classesToSimilarMap.get( classId );
      }
      return null;
   }

   /**
    * @param classId
    * @param classesToRedundantMap
    * @return
    */
   public String getRedundanciesString( String classId,
         Map classesToRedundantMap ) {
      if ( classesToRedundantMap != null
            && classesToRedundantMap.containsKey( classId ) ) {
         ArrayList redundant = ( ArrayList ) classesToRedundantMap
               .get( classId );
         Iterator it = redundant.iterator();
         String returnValue = "";
         while ( it.hasNext() ) {
            returnValue = returnValue + ", " + it.next();
         }
         return returnValue;
      }
      return "";
   }

} // end of class
