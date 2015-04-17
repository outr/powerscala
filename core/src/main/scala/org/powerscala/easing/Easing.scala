package org.powerscala.easing

import org.powerscala.enum.{Enumerated, EnumEntry}

trait Easing extends EnumEntry {
  def apply(elapsed: Double, start: Double, target: Double, duration: Double): Double
}

sealed abstract class EasingEnum(val f: Function4[Double, Double, Double, Double, Double]) extends Easing {
  def apply(elapsed: Double, start: Double, target: Double, duration: Double) = {
    f(elapsed, start, target - start, duration)
  }
}

object Easing extends Enumerated[EasingEnum] {
  case object BackIn extends EasingEnum(Back.easeIn)
  case object BackOut extends EasingEnum(Back.easeOut)
  case object BackInOut extends EasingEnum(Back.easeInOut)
  case object BounceIn extends EasingEnum(Bounce.easeIn)
  case object BounceOut extends EasingEnum(Bounce.easeOut)
  case object BounceInOut extends EasingEnum(Bounce.easeInOut)
  case object CircularIn extends EasingEnum(Circular.easeIn)
  case object CircularOut extends EasingEnum(Circular.easeOut)
  case object CircularInOut extends EasingEnum(Circular.easeInOut)
  case object CubicIn extends EasingEnum(Cubic.easeIn)
  case object CubicOut extends EasingEnum(Cubic.easeOut)
  case object CubicInOut extends EasingEnum(Cubic.easeInOut)
  case object ElasticIn extends EasingEnum(Elastic.easeIn)
  case object ElasticOut extends EasingEnum(Elastic.easeOut)
  case object ElasticInOut extends EasingEnum(Elastic.easeInOut)
  case object ExponentialIn extends EasingEnum(Exponential.easeIn)
  case object ExponentialOut extends EasingEnum(Exponential.easeOut)
  case object ExponentialInOut extends EasingEnum(Exponential.easeInOut)
  case object LinearIn extends EasingEnum(Linear.easeIn)
  case object LinearOut extends EasingEnum(Linear.easeOut)
  case object LinearInOut extends EasingEnum(Linear.easeInOut)
  case object QuadraticIn extends EasingEnum(Quadratic.easeIn)
  case object QuadraticOut extends EasingEnum(Quadratic.easeOut)
  case object QuadraticInOut extends EasingEnum(Quadratic.easeInOut)
  case object QuarticIn extends EasingEnum(Quartic.easeIn)
  case object QuarticOut extends EasingEnum(Quartic.easeOut)
  case object QuarticInOut extends EasingEnum(Quartic.easeInOut)
  case object QuinticIn extends EasingEnum(Quintic.easeIn)
  case object QuinticOut extends EasingEnum(Quintic.easeOut)
  case object QuinticInOut extends EasingEnum(Quintic.easeInOut)
  case object SineIn extends EasingEnum(Sine.easeIn)
  case object SineOut extends EasingEnum(Sine.easeOut)
  case object SineInOut extends EasingEnum(Sine.easeInOut)

  val values = findValues.toVector
}