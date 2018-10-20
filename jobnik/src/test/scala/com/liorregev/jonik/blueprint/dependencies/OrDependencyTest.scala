package com.liorregev.jonik.blueprint.dependencies

import com.liorregev.jonik.blueprint.jobs.TestJob
import org.scalatest.{Matchers, WordSpec}

class OrDependencyTest extends WordSpec with Matchers {
  "Or Dependency" should {
    "is satisfied when any jobs are completed" in {
      val job1 = TestJob(true)
      val job2 = TestJob(false)
      val dep = OrDependency(job1, job2)
      dep.satisfied should be (true)
    }

    "is satisfied when all jobs are completed" in {
      val job1 = TestJob(true)
      val job2 = TestJob(true)
      val dep = OrDependency(job1, job2)
      dep.satisfied should be (true)
    }

    "is not satisfied when all jobs are not completed" in {
      val job1 = TestJob(false)
      val job2 = TestJob(false)
      val dep = OrDependency(job1, job2)
      dep.satisfied should be (false)
    }
  }
}
