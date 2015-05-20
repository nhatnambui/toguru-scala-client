package featurebee.impl

import java.math.BigInteger
import java.util.{UUID, Locale}

import featurebee.ClientInfo
import featurebee.ClientInfo.Browser.Browser
import featurebee.impl.LocaleSupport._
import UuidDistributionCondition._

sealed trait Condition {
  def applies(clientInfo: ClientInfo): Boolean
}

case object AlwaysOnCondition extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = true
}

case object AlwaysOffCondition extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = false
}

case class BrowserCondition(browsers: Set[Browser]) extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = clientInfo.browser.exists(browsers.contains)
}

case class CultureCondition(cultures: Set[Locale]) extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = {
    cultures.exists {
      activatingLocale => activatingLocale.lang match {
        case None =>
          clientInfo.culture.exists(clientLocale => activatingLocale.country == clientLocale.country)
        case _ => clientInfo.culture.contains(activatingLocale)
      }
    }
  }
}

/**
 * A condition that takes the uuid from client info, applies f() to it and checks if it is inside the given range.
 *
 * @param f a function that projects an UUID to an Int in the range between 1 and 100
 * @param range range from 1 to 100 (inclusive) that f(uuid) has to land in so that this condition will be true for the client uuid
 */
case class UuidDistributionCondition(range: Range, f: UUID => Int = defaultUuidToIntProjection) extends Condition {

  if(range.head < 0 || range.last > 100) throw new IllegalArgumentException("Range should describe a range between 0 and 100 inclusive")

  override def applies(clientInfo: ClientInfo): Boolean = {
    clientInfo.uuid.exists {
      uuid => range.contains(f(uuid))
    }
  }
}

object UuidDistributionCondition {
  val defaultUuidToIntProjection: UUID => Int = {
    (uuid) =>
      val hibits = uuid.getMostSignificantBits
      val  lobits = uuid.getLeastSignificantBits
      val barrayHi = BigInteger.valueOf(hibits).toByteArray
      val barrayLo = BigInteger.valueOf(lobits).toByteArray
      val comb = barrayLo ++ barrayHi
      Math.abs(new BigInteger(comb).mod(BigInteger.valueOf(100l)).intValue()) + 1
  }
}
