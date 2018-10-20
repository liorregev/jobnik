package com.liorregev.jonik.blueprint.dependencies

import com.liorregev.jonik.blueprint.jobs.TestJob
import org.scalatest.{Matchers, WordSpec}

class AndDependencyTest extends WordSpec with Matchers {
  "And Dependency" should {
    "is satisfied when all jobs are completed" in {
      val job1 = TestJob(true)
      val job2 = TestJob(true)
      val dep = AndDependency(job1, job2)
      dep.satisfied should be (true)
    }

    "is not satisfied when any jobs are not completed" in {
      val job1 = TestJob(true)
      val job2 = TestJob(false)
      val dep = AndDependency(job1, job2)
      dep.satisfied should be (false)
    }
  }
}
