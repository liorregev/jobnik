package com.liorregev.jonik.blueprint.jobs

import java.time.Instant

import play.api.libs.json.JsObject

sealed trait DispatchResult extends Product with Serializable
final case class DispatchSuccess(time: Instant) extends DispatchResult
final case class DispatchFailure(time: Instant) extends DispatchResult

final case class JobProgress()

final case class SimpleJob(config: JsObject, dispatchResult: Option[DispatchResult]) extends Job {

}
