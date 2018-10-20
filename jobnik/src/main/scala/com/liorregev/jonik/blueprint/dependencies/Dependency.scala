package com.liorregev.jonik.blueprint.dependencies

trait Dependency extends Product with Serializable {
  def satisfied: Boolean
}