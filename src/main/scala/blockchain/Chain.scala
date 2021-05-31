package blockchain

import java.security.InvalidParameterException
import utils.JsonSupport._
import spray.json._
import crypto.Crypto

// since every chain is unique, use sealed trait
sealed trait Chain {

  val index: Int
  val hash: String
  val values: List[Transaction]
  val proof: Long
  val timestamp: Long

  def ::(link: Chain): Chain = link match {
    case l:ChainLink => ChainLink(l.index, l.proof, l.values, this.hash, l.timestamp, this)
    case _ => throw new InvalidParameterException("Cannot add invalid link to chain")
  }
}

// Static enough
object Chain {
  def apply[T](b: Chain*): Chain = {
    if (b.isEmpty) EmptyChain
    else {
      val link = b.head.asInstanceOf[ChainLink]
      ChainLink(link.index, link.proof, link.values, link.previousHash, link.timestamp, apply(b.tail: _*))
    }
  }
}

// case if we want multiple imports for 1 export
case class ChainLink(index: Int, proof: Long, values: List[Transaction], previousHash: String = "", timestamp: Long = System.currentTimeMillis(), tail: Chain = EmptyChain) extends Chain {
  val hash = Crypto.sha256Hash(this.toJson.toString)
}

case object EmptyChain extends Chain {
  val index = 0
  val hash = "1"
  val values = Nil
  val proof = 100L
  val timestamp = 0L
}
