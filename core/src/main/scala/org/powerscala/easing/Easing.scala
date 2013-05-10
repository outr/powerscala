package org.powerscala.easing

import org.powerscala.enum.{Enumerated, EnumEntry}

trait Easing extends EnumEntry {
  def apply(elapsed: Double, start: Double, target: Double, duration: Double): Double
}

sealed class EasingEnum(val f: Function4[Double, Double, Double, Double, Double]) extends Easing {
  def apply(elapsed: Double, start: Double, target: Double, duration: Double) = {
    f(elapsed, start, target - start, duration)
  }
}

object Easing extends Enumerated[Easing] {
  val BackIn = new EasingEnum(Back.easeIn)
  val BackOut = new EasingEnum(Back.easeOut)
  val BackInOut = new EasingEnum(Back.easeInOut)
  val BounceIn = new EasingEnum(Bounce.easeIn)
  val BounceOut = new EasingEnum(Bounce.easeOut)
  val BounceInOut = new EasingEnum(Bounce.easeInOut)
  val CircularIn = new EasingEnum(Circular.easeIn)
  val CircularOut = new EasingEnum(Circular.easeOut)
  val CircularInOut = new EasingEnum(Circular.easeInOut)
  val CubicIn = new EasingEnum(Cubic.easeIn)
  val CubicOut = new EasingEnum(Cubic.easeOut)
  val CubicInOut = new EasingEnum(Cubic.easeInOut)
  val ElasticIn = new EasingEnum(Elastic.easeIn)
  val ElasticOut = new EasingEnum(Elastic.easeOut)
  val ElasticInOut = new EasingEnum(Elastic.easeInOut)
  val ExponentialIn = new EasingEnum(Exponential.easeIn)
  val ExponentialOut = new EasingEnum(Exponential.easeOut)
  val ExponentialInOut = new EasingEnum(Exponential.easeInOut)
  val LinearIn = new EasingEnum(Linear.easeIn)
  val LinearOut = new EasingEnum(Linear.easeOut)
  val LinearInOut = new EasingEnum(Linear.easeInOut)
  val QuadraticIn = new EasingEnum(Quadratic.easeIn)
  val QuadraticOut = new EasingEnum(Quadratic.easeOut)
  val QuadraticInOut = new EasingEnum(Quadratic.easeInOut)
  val QuarticIn = new EasingEnum(Quartic.easeIn)
  val QuarticOut = new EasingEnum(Quartic.easeOut)
  val QuarticInOut = new EasingEnum(Quartic.easeInOut)
  val QuinticIn = new EasingEnum(Quintic.easeIn)
  val QuinticOut = new EasingEnum(Quintic.easeOut)
  val QuinticInOut = new EasingEnum(Quintic.easeInOut)
  val SineIn = new EasingEnum(Sine.easeIn)
  val SineOut = new EasingEnum(Sine.easeOut)
  val SineInOut = new EasingEnum(Sine.easeInOut)
}