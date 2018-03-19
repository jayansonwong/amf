package amf.client.convert

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

trait CoreBaseClientConverter extends CoreBaseConverter {

  override type ClientList[E] = js.Array[E]
  override type ClientMap[V]  = js.Dictionary[V]

  override private[convert] def asClientList[A, B](from: Seq[A], matcher: InternalClientMatcher[A, B]): js.Array[B] =
    from.map(matcher.asClient).toJSArray

  override private[convert] def asClientMap[Internal, Client](from: mutable.Map[String, Internal],
                                                              matcher: InternalClientMatcher[Internal, Client]) = {
    from.map { case (k, v) => k -> matcher.asClient(v) }.toJSDictionary
  }

  override private[convert] def asInternalSeq[Client, Internal](from: js.Array[Client],
                                                                matcher: ClientInternalMatcher[Client, Internal]) =
    from.toSeq.map(matcher.asInternal)
}