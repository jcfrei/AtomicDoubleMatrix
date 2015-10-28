/**
 * Adapted from Google Guava's AtomicDoubleArray
 * Author: Jonathan Frei
 */
package com.jcfrei.utils;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;

import java.util.concurrent.atomic.AtomicLongArray;


public class AtomicDoubleMatrix  {

  // Making this non-final is the lesser evil according to Effective
  // Java 2nd Edition Item 76: Write readObject methods defensively.
  private transient AtomicLongArray longs;
  private transient int[] colNumbers; 
  private transient int noElements;

  /**
   * Creates a new AtomicDoubleMatrix with the specified
   * rows and columns
   * 
   * @param row
   * @param col
   */
  public AtomicDoubleMatrix(int row, int col) {
	  this(new double[row][col]);
  }
  
  /**
   * Creates a new {@code AtomicMatrixArray} with the same length
   * as, and all elements copied from, the given matrix. the two-
   * dimensional array can be jagged
   *
   * @param matrix the matrix to copy elements from
   * @throws NullPointerException if array is null
   */
  public AtomicDoubleMatrix(double[][] array) {
	colNumbers = new int[array.length];
	noElements = 0;
    for(int i = 0; i < array.length; i++){
    	colNumbers[i] = array[i].length;
    	for(int j = 0 ; j < array[i].length; j++){
    		noElements++;
    	}
    }
    long[] longArray = new long[noElements];
    int longArrayIndex = 0;
    for(int i = 0; i < array.length; i++){
    	for(int j = 0 ; j < array[i].length; j++){
    		longArray[longArrayIndex] = doubleToRawLongBits(array[i][j]);
    		longArrayIndex++;
    	}
    }
    this.longs = new AtomicLongArray(longArray);
  }
  
  private int rowColsToIndex(int row, int col){
	  int noElement = 0;
	  for(int i = 0; i < row ; i++){
		  noElement += colNumbers[i];
	  }
	  return (noElement+col);
  }
  

  /**
   * Returns the length of the array.
   *
   * @return the length of the array
   */
  public final int length() {
    return noElements;
  }
  
  public final int getNoRows(){
	  return colNumbers.length;
  }

  /**
   * Gets the current value at position {@code i}.
   *
   * @param i the index
   * @return the current value
   */
  public final double get(int row, int col) {
    return longBitsToDouble(rowColsToIndex(row, col));
  }
  
  /**
   * get a whole row
   * @param row
   * @return
   */
  public final double[] getRow(int row) {
	  double [] fullDoubleRow = new double[colNumbers[row]];
	  for(int i = 0; i < colNumbers[row]; i++){
		  fullDoubleRow[i] = longBitsToDouble(rowColsToIndex(row, i));
	  }
	    return fullDoubleRow;
  }
  
  public final double[][] get() {
	  int elementno = 0;
	  double [][] matrix = new double[colNumbers.length][];
	  for(int i = 0; i < colNumbers.length; i++){
		  matrix[i] = new double[colNumbers[i]];
		  for(int j = 0; j < colNumbers[i]; j++){
			  matrix[i][j] = longBitsToDouble(longs.get(elementno));
			  elementno++;
		  }
	  }
	    return matrix;
  }
  
   private final double getDirectly(int i) {
	    return longBitsToDouble(i);
   }

  /**
   * Sets the element at position {@code i} to the given value.
   *
   * @param i the index
   * @param newValue the new value
   */
  public final void set(int row, int col, double newValue) {
    long next = doubleToRawLongBits(newValue);
    longs.set(rowColsToIndex(row, col), next);
  }
  
  public final void set( double[][] newMatrix) {
	  int elementno = 0;
	  for(int i = 0; i < newMatrix.length; i++){
	    	for(int j = 0 ; j < newMatrix[i].length; j++){
	    		longs.set(elementno, doubleToRawLongBits(newMatrix[i][j]));
	    		elementno++;
	    	}
	    }
	  }
  
  private final void setDirectly(int i , double newValue) {
	  long next = doubleToRawLongBits(newValue);
	  longs.set(i, next);
 }

  /**
   * Eventually sets the element at position {@code i} to the given value.
   *
   * @param i the index
   * @param newValue the new value
   */
  public final void lazySet(int row, int col, double newValue) {
    set(row, col, newValue);
    // TODO(user): replace with code below when jdk5 support is dropped.
    // long next = doubleToRawLongBits(newValue);
    // longs.lazySet(i, next);
  }

  /**
   * Atomically sets the element at position {@code i} to the given value
   * and returns the old value.
   *
   * @param i the index
   * @param newValue the new value
   * @return the previous value
   */
  public final double getAndSet(int row, int col, double newValue) {
    long next = doubleToRawLongBits(newValue);
    return longBitsToDouble(longs.getAndSet(rowColsToIndex(row, col), next));
  }

  /**
   * Atomically sets the element at position {@code i} to the given
   * updated value
   * if the current value is <a href="#bitEquals">bitwise equal</a>
   * to the expected value.
   *
   * @param i the index
   * @param expect the expected value
   * @param update the new value
   * @return true if successful. False return indicates that
   * the actual value was not equal to the expected value.
   */
  public final boolean compareAndSet(int row, int col, double expect, double update) {
    return longs.compareAndSet(rowColsToIndex(row, col),
                               doubleToRawLongBits(expect),
                               doubleToRawLongBits(update));
  }

  /**
   * Atomically sets the element at position {@code i} to the given
   * updated value
   * if the current value is <a href="#bitEquals">bitwise equal</a>
   * to the expected value.
   *
   * <p>May <a
   * href="http://download.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/package-summary.html#Spurious">
   * fail spuriously</a>
   * and does not provide ordering guarantees, so is only rarely an
   * appropriate alternative to {@code compareAndSet}.
   *
   * @param i the index
   * @param expect the expected value
   * @param update the new value
   * @return true if successful
   */
  public final boolean weakCompareAndSet(int row, int col, double expect, double update) {
    return longs.weakCompareAndSet(rowColsToIndex(row, col),
                                   doubleToRawLongBits(expect),
                                   doubleToRawLongBits(update));
  }

  /**
   * Atomically adds the given value to the element at index {@code i}.
   *
   * @param i the index
   * @param delta the value to add
   * @return the previous value
   */
  public final double getAndAdd(int row, int col, double delta) {
    while (true) {
      int l = rowColsToIndex(row, col);
      long current = longs.get(l);
      double currentVal = longBitsToDouble(current);
      double nextVal = currentVal + delta;
      long next = doubleToRawLongBits(nextVal);
      if (longs.compareAndSet(l, current, next)) {
        return currentVal;
      }
    }
  }

  /**
   * Atomically adds the given value to the element at index {@code i}.
   *
   * @param i the index
   * @param delta the value to add
   * @return the updated value
   */
  public double addAndGet(int row, int col, double delta) {
    while (true) {
    	int l = rowColsToIndex(row, col);
      long current = longs.get(l);
      double currentVal = longBitsToDouble(current);
      double nextVal = currentVal + delta;
      long next = doubleToRawLongBits(nextVal);
      if (longs.compareAndSet(l, current, next)) {
        return nextVal;
      }
    }
  }

  /**
   * Returns the String representation of the current values of array.
   * @return the String representation of the current values of array
   */
  public String toString() {
	// not even gonna attempt to guess the initial size here
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i<colNumbers.length; i++) {
      sb.append('[');
      for(int j = 0; j<colNumbers[i]; j++){
    	  sb.append(longBitsToDouble(longs.get(i)));
    	  if(j == colNumbers[i]-1){
    		  sb.append(']');
    	  } else {
    		  sb.append(',').append(' ');
    	  }
      }
    }
    
    return sb.toString();
  }

}
