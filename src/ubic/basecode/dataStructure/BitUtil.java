/*

This code taken from: Derby - Class org.apache.derbyTesting.unitTests.util.BitUtil

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package ubic.basecode.dataStructure;

/**
 * This class provides utility methods for converting byte arrays to hexidecimal Strings and manipulating BIT/BIT
 * VARYING values as a packed vector of booleans.
 * <P>
 * The BIT/BIT VARYING methods are modeled after some methods in the <I>java.util.BitSet</I> class. An alternative to
 * using a SQL BIT (VARYING) column in conjunction with the methods provided herein to provide bit manipulation would be
 * to use a serialized <I>java.util.BitSet</I> column instead.
 * <p>
 * This class contains the following static methods:
 * <UL>
 * <LI> void <B>set</B>(byte[] bytes, int position) to set a bit</LI>
 * <LI> void <B>clear</B>(byte[] bytes, int position) to clear a bit</LI>
 * <LI> boolean <B>get</B>(byte[] bytes, int position) to get the bit status </LI>
 * </UL>
 */
public class BitUtil {
    /**
     * Set the bit at the specified position
     * 
     * @param bytes the byte array
     * @param position the bit to set, starting from zero
     * @return the byte array with the set bit
     * @exception IndexOutOfBoundsException on bad position
     */
    public static byte[] set( byte[] bytes, int position ) {
        if ( position >= 0 ) {
            int bytepos = position >> 3;
            if ( bytepos < bytes.length ) {
                int bitpos = 7 - ( position % 8 );

                bytes[bytepos] |= ( 1 << bitpos );
                return bytes;
            }
        }
        throw new IndexOutOfBoundsException( Integer.toString( position ) );
    }

    /**
     * Clear the bit at the specified position
     * 
     * @param bytes the byte array
     * @param position the bit to clear, starting from zero
     * @return the byte array with the cleared bit
     * @exception IndexOutOfBoundsException on bad position
     */
    public static byte[] clear( byte[] bytes, int position ) {
        if ( position >= 0 ) {
            int bytepos = position >> 3;
            if ( bytepos < bytes.length ) {
                int bitpos = 7 - ( position % 8 );
                bytes[bytepos] &= ~( 1 << bitpos );
                return bytes;
            }
        }

        throw new IndexOutOfBoundsException( Integer.toString( position ) );
    }

    /**
     * Check to see if the specified bit is set
     * 
     * @param bytes the byte array
     * @param position the bit to check, starting from zero
     * @return true/false
     * @exception IndexOutOfBoundsException on bad position
     */
    public static boolean get( byte[] bytes, int position ) {
        if ( position >= 0 ) {
            int bytepos = position >> 3;
            if ( bytepos < bytes.length ) {
                int bitpos = 7 - ( position % 8 );
                return ( ( bytes[bytepos] & ( 1 << bitpos ) ) != 0 );
            }
        }
        throw new IndexOutOfBoundsException( Integer.toString( position ) );
    }

}