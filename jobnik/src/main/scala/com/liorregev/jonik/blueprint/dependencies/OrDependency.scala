package com.liorregev.jonik.blueprint.dependencies

import com.liorregev.jonik.blueprint.jobs.Job

final case class OrDependency(job: Job, jobs: Job*) extends Dependency{
  override def satisfied: Boolean = job +: jobs exists(_.isCompleted)
}
