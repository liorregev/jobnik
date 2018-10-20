package com.liorregev.jonik.blueprint.jobs

trait Job extends Product with Serializable{
  def isCompleted: Boolean
}
