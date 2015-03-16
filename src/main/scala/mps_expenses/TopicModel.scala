package mps_expenses

import scala.collection.JavaConversions._
import java.util.TreeSet
import cc.mallet.topics.ParallelTopicModel
import cc.mallet.types.{IDSorter, InstanceList}
import cc.mallet.pipe._
import cc.mallet.pipe.iterator.ArrayIterator

object TopicModel extends App {
  import Models._
  import Persistence._
  val n = 5
  val texts = getInterests
  val instances = textsToInstances(texts)
  val model = createModel(n, instances)
  printTopics(model)

  private def textsToInstances(texts : Seq[String]) = {
    val pipes = Seq(
      new CharSequence2TokenSequence(),
      new TokenSequenceLowercase(),
      new TokenSequenceRemoveStopwords(),
      new TokenSequence2FeatureSequence()
    )
    val instances = new InstanceList(new SerialPipes(pipes))
    instances.addThruPipe(new ArrayIterator(texts))
    instances
  }

  private def createModel(n : Int, instances : InstanceList) = {
    val model = new ParallelTopicModel(n)
    model.addInstances(instances)
    model.estimate()
    model
  }

  private def printTopics(model : ParallelTopicModel) = {
    val alphabet = model.getAlphabet()
    model.getSortedWords().toSet.foreach { (words : TreeSet[IDSorter]) =>
      println("TOPIC: %s" format words.toSeq.map { (word : IDSorter) =>
        alphabet.lookupObject(word.getID)
      }.take(10).mkString(", "))
    }
  }
}
