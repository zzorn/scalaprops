package org.scalaprops

sealed trait ValidationResult

case object Success extends ValidationResult

case class Failure(error: String) extends ValidationResult
