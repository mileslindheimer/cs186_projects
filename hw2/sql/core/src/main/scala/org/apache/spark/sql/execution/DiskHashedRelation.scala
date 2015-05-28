package org.apache.spark.sql.execution

import java.io._
import java.nio.file.{Path, StandardOpenOption, Files}
import java.util.{ArrayList => JavaArrayList}

import org.apache.spark.SparkException
import org.apache.spark.sql.catalyst.expressions.{Projection, Row}
import org.apache.spark.sql.execution.CS186Utils._

import scala.collection.JavaConverters._

/**
 * This trait represents a regular relation that is hash partitioned and spilled to
 * disk.
 */
private[sql] sealed trait DiskHashedRelation {
  /**
   *
   * @return an iterator of the [[DiskPartition]]s that make up this relation.
   */
  def getIterator(): Iterator[DiskPartition]

  /**
   * Close all the partitions for this relation. This should involve deleting the files hashed into.
   */
  def closeAllPartitions()
}

/**
 * A general implementation of [[DiskHashedRelation]].
 *
 * @param partitions the disk partitions that we are going to spill to
 */
protected [sql] final class GeneralDiskHashedRelation(partitions: Array[DiskPartition])
    extends DiskHashedRelation with Serializable {

  override def getIterator() = {
    // IMPLEMENT ME
		var partitionList: JavaArrayList[DiskPartition] = new JavaArrayList[DiskPartition]()
		for (partition <- partitions) {
			partitionList.add(partition)
		}
		partitionList.iterator().asScala
  }

  override def closeAllPartitions() = {
    // IMPLEMENT ME
		var partitionIter: Iterator[DiskPartition] = this.getIterator()
		while (partitionIter.hasNext) {
			partitionIter.next().closePartition()
		}
  }
}

private[sql] class DiskPartition (
                                  filename: String,
                                  blockSize: Int) {
  private val path: Path = Files.createTempFile("", filename)
  private val data: JavaArrayList[Row] = new JavaArrayList[Row]
  private val outStream: OutputStream = Files.newOutputStream(path)
  private val inStream: InputStream = Files.newInputStream(path)
  private val chunkSizes: JavaArrayList[Int] = new JavaArrayList[Int]()
  private var writtenToDisk: Boolean = false
  private var inputClosed: Boolean = false

  /**
   * This method inserts a new row into this particular partition. If the size of the partition
   * exceeds the blockSize, the partition is spilled to disk.
   *
   * @param row the [[Row]] we are adding
   */
  def insert(row: Row) = {
    // IMPLEMENT ME
		if (inputClosed) {
      throw new SparkException("Should not be inserting into file after closing input.")
    }
		data.add(row)
		if (this.measurePartitionSize() > this.blockSize) {
			data.remove(row)
			this.spillPartitionToDisk()
			data.clear()
			data.add(row)
		}
  }

  /**
   * This method converts the data to a byte array and returns the size of the byte array
   * as an estimation of the size of the partition.
   *
   * @return the estimated size of the data
   */
  private[this] def measurePartitionSize(): Int = {
    CS186Utils.getBytesFromList(data).size
  }

  /**
   * Uses the [[Files]] API to write a byte array representing data to a file.
   */
  private[this] def spillPartitionToDisk() = {
    val bytes: Array[Byte] = getBytesFromList(data)

    // This array list stores the sizes of chunks written in order to read them back correctly.
    chunkSizes.add(bytes.size)

    Files.write(path, bytes, StandardOpenOption.APPEND)
    writtenToDisk = true
  }

  /**
   * If this partition has been closed, this method returns an Iterator of all the
   * data that was written to disk by this partition.
   *
   * @return the [[Iterator]] of the data
   */
  def getData(): Iterator[Row] = {
    if (!inputClosed) {
      throw new SparkException("Should not be reading from file before closing input. Bad things will happen!")
    }

    new Iterator[Row] {
      var currentIterator: Iterator[Row] = data.iterator.asScala
      val chunkSizeIterator: Iterator[Int] = chunkSizes.iterator().asScala
      var byteArray: Array[Byte] = null

      override def next() = {
        // IMPLEMENT ME
				currentIterator.next()	
      }

      override def hasNext() = {
        // IMPLEMENT ME
				var result: Boolean = false
				if (byteArray == null || !(currentIterator.hasNext)) {
					result = fetchNextChunk()
				} else if (currentIterator.hasNext) {
					result = true
				} 
			  result	
      }

      /**
       * Fetches the next chunk of the file and updates the iterator. Should return true
       * unless the iterator is empty.
       *
       * @return true unless the iterator is empty.
       */
      private[this] def fetchNextChunk(): Boolean = {
        // IMPLEMENT ME
				if (this.chunkSizeIterator.hasNext) {
					byteArray = CS186Utils.getNextChunkBytes(inStream, chunkSizeIterator.next(), byteArray) 
					data.clear()
					data.addAll(CS186Utils.getListFromBytes(byteArray))
					currentIterator = data.iterator.asScala
					if (data.isEmpty()) {
						false
					} else {
					true
					}
				} else {
					false
				}
      }
    }
  }

  /**
   * Closes this partition, implying that no more data will be written to this partition. If getData()
   * is called without closing the partition, an error will be thrown.
   *
   * If any data has not been written to disk yet, it should be written. The output stream should
   * also be closed.
   */
  def closeInput() = {
    // IMPLEMENT ME
		if (this.measurePartitionSize() > 0) {
			this.spillPartitionToDisk()
			data.clear()
		}
		inputClosed = true
		outStream.close()
  }


  /**
   * Closes this partition. This closes the input stream and deletes the file backing the partition.
   */
  private[sql] def closePartition() = {
    inStream.close()
    Files.deleteIfExists(path)
  }
}

private[sql] object DiskHashedRelation {

  /**
   * Given an input iterator, partitions each row into one of a number of [[DiskPartition]]s
   * and constructors a [[DiskHashedRelation]].
   *
   * This executes the first phase of external hashing -- using a course-grained hash function
   * to partition the tuples to disk.
   *
   * The block size is approximately set to 64k because that is a good estimate of the average
   * buffer page.
   *
   * @param input the input [[Iterator]] of [[Row]]s
   * @param keyGenerator a [[Projection]] that generates the keys for the input
   * @param size the number of [[DiskPartition]]s
   * @param blockSize the threshold at which each partition will spill
   * @return the constructed [[DiskHashedRelation]]
   */
  def apply (
                input: Iterator[Row],
                keyGenerator: Projection,
                size: Int = 64,
                blockSize: Int = 64000) = {
    // IMPLEMENT ME
		var diskPartitionArray: Array[DiskPartition] = new Array[DiskPartition](size)
    var index: Int = 0
    while (index < size) {
      var tempPartition: DiskPartition = new DiskPartition(index.toString, blockSize)
      diskPartitionArray(index) = tempPartition
      index = index + 1
		}
    var rawRow: Row = null
    var genRow: Row = null
    var hash: Int = -1
    while (input.hasNext){
      rawRow = input.next()
      genRow = keyGenerator(rawRow)
      hash = genRow.hashCode() % size
      diskPartitionArray(hash).insert(rawRow)
    }
		for (partition <- diskPartitionArray) {
			partition.closeInput()
		}
    var diskHashedRelation: DiskHashedRelation = new GeneralDiskHashedRelation(diskPartitionArray)
		diskHashedRelation
  }
}
