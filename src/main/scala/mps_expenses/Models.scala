package mps_expenses

object Models {
  final case class MPRequest(name : String, source : String)
  final case class MP(name : String, interests : String)
}
