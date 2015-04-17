package org.powerscala

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
abstract class Color {
  def red: Double
  def green: Double
  def blue: Double
  def alpha: Double

  class HexColor {
    def red = toHex(Color.this.red)
    def green = toHex(Color.this.green)
    def blue = toHex(Color.this.blue)
    def alpha = toHex(Color.this.alpha)

    def rgb = "#%02x%02x%02x".format(normalize(Color.this.red), normalize(Color.this.green), normalize(Color.this.blue))

    private def toHex(v: Double) = {
      "%02x".format(normalize(v))
    }

    private def normalize(v: Double) = {
      (v * 255.0).toInt
    }
  }

  class IntColor {
    def red = toInt(Color.this.red)
    def green = toInt(Color.this.green)
    def blue = toInt(Color.this.blue)
    def alpha = toInt(Color.this.alpha)

    def rgb = s"$red, $green, $blue"
    def rgba = s"$red, $green, $blue, $alpha"

    private def toInt(v: Double) = {
      math.round(v * 255.0).toInt
    }
  }
  lazy val hex = new HexColor
  lazy val int = new IntColor

  def apply(index: Int) = index match {
    case 0 => red
    case 1 => green
    case 2 => blue
    case 3 => alpha
    case _ => throw new IndexOutOfBoundsException("Index %s is greater than Color bounds (3)".format(index))
  }

  def apply(color: Color): Color = apply(color.red, color.green, color.blue, color.alpha)

  def apply(red: Double = red, green: Double = green, blue: Double = blue, alpha: Double = alpha): Color

  def add(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0, alpha: Double = 0.0) = {
    apply(this.red + red, this.green + green, this.blue + blue, this.alpha + alpha)
  }

  def subtract(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0, alpha: Double = 0.0) = {
    apply(this.red - red, this.green - green, this.blue - blue, this.alpha - alpha)
  }

  def multiply(red: Double = 1.0, green: Double = 1.0, blue: Double = 1.0, alpha: Double = 1.0) = {
    apply(this.red * red, this.green * green, this.blue * blue, this.alpha * alpha)
  }

  def divide(red: Double = 1.0, green: Double = 1.0, blue: Double = 1.0, alpha: Double = 1.0) = {
    apply(this.red / red, this.green / green, this.blue / blue, this.alpha / alpha)
  }

  override def equals(that: Any) = that match {
    case color: Color => color.red == red && color.green == green && color.blue == blue && color.alpha == alpha
    case _ => false
  }

  def mutable: MutableColor
  def immutable: ImmutableColor

  override def toString = "Color(red=%s, green=%s, blue=%s, alpha=%s)".format(red, green, blue, alpha)

  def toCSS = if (alpha < 1.0) {
    s"rgba(${int.rgb}, $alpha)"
  } else {
    hex.rgb
  }
}

object Color extends Enumerated[EnumColor] {
  case object AliceBlue extends EnumColor(0xfff0f8ff)
  case object AntiqueWhite extends EnumColor(0xfffaebd7)
  case object Aquamarine extends EnumColor(0xff7fffd4)
  case object Azure extends EnumColor(0xfff0ffff)
  case object Beige extends EnumColor(0xfff5f5dc)
  case object Bisque extends EnumColor(0xffffe4c4)
  case object Black extends EnumColor(0xff000000)
  case object BlanchedAlmond extends EnumColor(0xffffebcd)
  case object Blue extends EnumColor(0xff0000ff)
  case object BlueViolet extends EnumColor(0xff8a2be2)
  case object Brown extends EnumColor(0xffa52a2a)
  case object Burlywood extends EnumColor(0xffdeb887)
  case object CadetBlue extends EnumColor(0xff5f9ea0)
  case object Chartreuse extends EnumColor(0xff7fff00)
  case object Chocolate extends EnumColor(0xffd2691e)
  case object Coral extends EnumColor(0xffff7f50)
  case object CornflowerBlue extends EnumColor(0xff6495ed)
  case object Cornsilk extends EnumColor(0xfffff8dc)
  case object Cyan extends EnumColor(0xff00cdcd)
  case object DarkBlue extends EnumColor(0xff00008b)
  case object DarkGoldenrod extends EnumColor(0xffb8860b)
  case object DarkGray extends EnumColor(0xff3f3f3f)
  case object DarkGreen extends EnumColor(0xff006400)
  case object DarkKhaki extends EnumColor(0xffbdb76b)
  case object DarkOliveGreen extends EnumColor(0xff556b2f)
  case object DarkOrange extends EnumColor(0xffff8c00)
  case object DarkOrchid extends EnumColor(0xff9932cc)
  case object DarkRed extends EnumColor(0xff8b0000)
  case object DarkSalmon extends EnumColor(0xffe9967a)
  case object DarkSeaGreen extends EnumColor(0xff8fbc8f)
  case object DarkSlateBlue extends EnumColor(0xff483d8b)
  case object DarkSlateGray extends EnumColor(0xff2f4f4f)
  case object DarkTurquoise extends EnumColor(0xff00ced1)
  case object DarkViolet extends EnumColor(0xff9400d3)
  case object DeepPink extends EnumColor(0xffff1493)
  case object DeepSkyBlue extends EnumColor(0xff00bfff)
  case object DimGray extends EnumColor(0xff696969)
  case object DodgerBlue extends EnumColor(0xff1e90ff)
  case object Firebrick extends EnumColor(0xffb22222)
  case object FloralWhite extends EnumColor(0xfffffaf0)
  case object ForestGreen extends EnumColor(0xff228b22)
  case object Gainsboro extends EnumColor(0xffdcdcdc)
  case object GhostWhite extends EnumColor(0xfff8f8ff)
  case object Gold extends EnumColor(0xffffd700)
  case object Goldenrod extends EnumColor(0xffdaa520)
  case object Gray extends EnumColor(0xffbebebe)
  case object Green extends EnumColor(0xff008000)
  case object GreenYellow extends EnumColor(0xffadff2f)
  case object HaloBlue extends EnumColor(0xff93a9b4)
  case object HighlightBlue extends EnumColor(0xffb2e1ff)
  case object Honeydew extends EnumColor(0xfff0fff0)
  case object HotPink extends EnumColor(0xffff69b4)
  case object IndianRed extends EnumColor(0xffcd5c5c)
  case object Ivory extends EnumColor(0xfffffff0)
  case object Khaki extends EnumColor(0xfff0e68c)
  case object Lavender extends EnumColor(0xffe6e6fa)
  case object LavenderBlush extends EnumColor(0xfffff0f5)
  case object LawnGreen extends EnumColor(0xff7cfc00)
  case object LemonChiffon extends EnumColor(0xfffffacd)
  case object LightBlue extends EnumColor(0xffadd8e6)
  case object LightCoral extends EnumColor(0xfff08080)
  case object LightCyan extends EnumColor(0xffe0ffff)
  case object LightGoldenrod extends EnumColor(0xffeedd82)
  case object LightGoldenrodYellow extends EnumColor(0xfffafad2)
  case object LightGray extends EnumColor(0xffd3d3d3)
  case object LightPink extends EnumColor(0xffffb6c1)
  case object LightSalmon extends EnumColor(0xffffa07a)
  case object LightSeaGreen extends EnumColor(0xff20b2aa)
  case object LightSkyBlue extends EnumColor(0xff87cefa)
  case object LightSlateBlue extends EnumColor(0xff8470ff)
  case object LightSlateGray extends EnumColor(0xff778899)
  case object LightSteelBlue extends EnumColor(0xffb0c4de)
  case object LightYellow extends EnumColor(0xffffffe0)
  case object LimeGreen extends EnumColor(0xff32cd32)
  case object Linen extends EnumColor(0xfffaf0e6)
  case object Magenta extends EnumColor(0xffff00ff)
  case object Maroon extends EnumColor(0xffb03060)
  case object MediumAquamarine extends EnumColor(0xff66cdaa)
  case object MediumBlue extends EnumColor(0xff0000cd)
  case object MediumOrchid extends EnumColor(0xffba55d3)
  case object MediumPurple extends EnumColor(0xff9370db)
  case object MediumSeaGreen extends EnumColor(0xff3cb371)
  case object MediumSlateBlue extends EnumColor(0xff7b68ee)
  case object MediumSpringGreen extends EnumColor(0xff00fa9a)
  case object MediumTurquoise extends EnumColor(0xff48d1cc)
  case object MediumVioletRed extends EnumColor(0xffc71585)
  case object MidnightBlue extends EnumColor(0xff191970)
  case object MintCream extends EnumColor(0xfff5fffa)
  case object MistyRose extends EnumColor(0xffffe4e1)
  case object Moccasin extends EnumColor(0xffffe4b5)
  case object NavajoWhite extends EnumColor(0xffffdead)
  case object NavyBlue extends EnumColor(0xff000080)
  case object OldLace extends EnumColor(0xfffdf5e6)
  case object OliveDrab extends EnumColor(0xff6b8e23)
  case object Orange extends EnumColor(0xffffa500)
  case object OrangeRed extends EnumColor(0xffff4500)
  case object Orchid extends EnumColor(0xffda70d6)
  case object PaleGoldenrod extends EnumColor(0xffeee8aa)
  case object PaleGreen extends EnumColor(0xff98fb98)
  case object PaleTurquoise extends EnumColor(0xffafeeee)
  case object PaleVioletRed extends EnumColor(0xffdb7093)
  case object PapayaWhip extends EnumColor(0xffffefd5)
  case object PeachPuff extends EnumColor(0xffffdab9)
  case object Peru extends EnumColor(0xffcd853f)
  case object Pink extends EnumColor(0xffffc0cb)
  case object Plum extends EnumColor(0xffdda0dd)
  case object PowderBlue extends EnumColor(0xffb0e0e6)
  case object Purple extends EnumColor(0xff800080)
  case object Red extends EnumColor(0xffff0000)
  case object RosyBrown extends EnumColor(0xffbc8f8f)
  case object RoyalBlue extends EnumColor(0xff4169e1)
  case object SaddleBrown extends EnumColor(0xff8b4513)
  case object Salmon extends EnumColor(0xfffa8072)
  case object SandyBrown extends EnumColor(0xfff4a460)
  case object SeaGreen extends EnumColor(0xff2e8b57)
  case object Seashell extends EnumColor(0xfffff5ee)
  case object SelectBlue extends EnumColor(0xff4394ff)
  case object Sienna extends EnumColor(0xffa0522d)
  case object SkyBlue extends EnumColor(0xff87ceeb)
  case object SlateBlue extends EnumColor(0xff6a5acd)
  case object SlateGray extends EnumColor(0xff708090)
  case object Snow extends EnumColor(0xfffffafa)
  case object SpringGreen extends EnumColor(0xff00ff7f)
  case object SteelBlue extends EnumColor(0xff4682b4)
  case object Tan extends EnumColor(0xffd2b48c)
  case object Thistle extends EnumColor(0xffd8bfd8)
  case object Tomato extends EnumColor(0xffff6347)
  case object Turquoise extends EnumColor(0xff40e0d0)
  case object UmmGold extends EnumColor(0xffffcc33)
  case object UmmMaroon extends EnumColor(0xff660000)
  case object Violet extends EnumColor(0xffee82ee)
  case object VioletRed extends EnumColor(0xffd02090)
  case object Wheat extends EnumColor(0xfff5deb3)
  case object White extends EnumColor(0xffffffff)
  case object WhiteSmoke extends EnumColor(0xfff5f5f5)
  case object Yellow extends EnumColor(0xffffff00)
  case object YellowGreen extends EnumColor(0xff9acd32)

  case object Clear extends EnumColor(0x00ffffff)

  private val RGBIntRegex = """rgb\((\d*), (\d*), (\d*)\)""".r
  private val RGBAIntRegex = """rgba\((\d*), (\d*), (\d*), (\d.*)\)""".r
  private val HSVIntRegex = """hsv\((\d*), (\d*)%?, (\d*)%?\)""".r

  val values = findValues.toVector

  def byName(name: String) = get(name, caseSensitive = false) match {
    case None => name match {
      case null => null
      case RGBIntRegex(red, green, blue) => Some(immutable(red.toInt, green.toInt, blue.toInt, 255))                                       // RGB
      case RGBAIntRegex(red, green, blue, alpha) => Some(immutable(red.toInt, green.toInt, blue.toInt, (alpha.toDouble * 255.0).toInt))    // RGBA
      case HSVIntRegex(hue, saturation, value) => Some(hsv(hue.toDouble, saturation.toDouble / 100.0, value.toDouble / 100.0))             // HSV
      case _ => try {
        Some(immutable(name))
      } catch {
        case t: Throwable => None
      }
    }
    case Some(color) => Some(color)
  }

  /**
   * Creates an immutable Color based on the HSV
   *
   * @param hue the hue (value from 0.0 to 360.0)
   * @param saturation the saturation (value from 0.0 to 1.0)
   * @param value the value (value from 0.0 to 1.0)
   */
  def hsv(hue: Double, saturation: Double, value: Double): Color = {
    val c  = saturation * value
    val h1 = hue / 60.0
    val x  = c*(1.0 - ((h1 % 2) - 1.0).abs)
    val (r, g, b) = if (h1 < 1.0) (c, x, 0.0)
    else if (h1 < 2.0) (x, c, 0.0)
    else if (h1 < 3.0) (0.0, c, x)
    else if (h1 < 4.0) (0.0, x, c)
    else if (h1 < 5.0) (x, 0.0, c)
    else  /*h1 < 6.0*/ (c, 0.0, x)
    val m = value - c
    immutable(r + m, g + m, b + m)
  }

  def immutable(value: Long): Color = {
    val alpha = (value >> 24 & 0xff) / 255.0
    val red = (value >> 16 & 0xff) / 255.0
    val green = (value >> 8 & 0xff) / 255.0
    val blue = (value >> 0 & 0xff) / 255.0
    immutable(red, green, blue, alpha)
  }

  def immutable(red: Int, green: Int, blue: Int, alpha: Double): Color = {
    immutable(red / 255.0, green / 255.0, blue / 255.0, alpha)
  }

  def immutable(hex: String): Color = {
    val (red, green, blue, alpha) = convertHex(hex)
    new ImmutableColor(red, green, blue, alpha)
  }

  def immutable(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0, alpha: Double = 1.0): Color = {
    new ImmutableColor(red, green, blue, alpha)
  }

  def mutable(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0, alpha: Double = 1.0): MutableColor = {
    new MutableColor(red, green, blue, alpha)
  }

  def mutable(value: Long): MutableColor = {
    val alpha = (value >> 24 & 0xff) / 255.0
    val red = (value >> 16 & 0xff) / 255.0
    val green = (value >> 8 & 0xff) / 255.0
    val blue = (value >> 0 & 0xff) / 255.0
    mutable(red, green, blue, alpha)
  }

  def mutable(red: Int, green: Int, blue: Int, alpha: Int): MutableColor = {
    mutable(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0)
  }

  def mutable(hex: String): MutableColor = {
    val (red, green, blue, alpha) = convertHex(hex)
    new MutableColor(red, green, blue, alpha)
  }

  private def convertHex(hex: String): (Double, Double, Double, Double) = {
    if (hex.startsWith("#")) {
      convertHex(hex.substring(1))
    } else if ((hex.length == 3) || (hex.length == 4)) {		// Convert 3-digit / 4-digit to 6-digit / 8-digit
      val b = new StringBuilder()
      for (i <- 0 until hex.length) {
        b.append(hex.charAt(i))
        b.append(hex.charAt(i))
      }
      convertHex(b.toString())
    } else if (hex.length >= 6) {
      val red = (fromHex(hex.charAt(0)) * 16) + fromHex(hex.charAt(1))
      val green = (fromHex(hex.charAt(2)) * 16) + fromHex(hex.charAt(3))
      val blue = (fromHex(hex.charAt(4)) * 16) + fromHex(hex.charAt(5))
      var alpha = 255
      if (hex.length == 8) {
        alpha = (fromHex(hex.charAt(6)) * 16) + fromHex(hex.charAt(7))
      }
      (red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0)
    } else {
      throw new RuntimeException("Unable to parse " + hex + " to Color")
    }
  }

  private def fromHex(c: Char) = if (c.isDigit) {
    c.toString.toInt
  } else {
    c.toLower match {
      case 'a' => 10
      case 'b' => 11
      case 'c' => 12
      case 'd' => 13
      case 'e' => 14
      case 'f' => 15
      case _ => throw new RuntimeException("Unable to parse character to hex: " + c)
    }
  }
}

class ImmutableColor(val red: Double, val green: Double, val blue: Double, val alpha: Double) extends Color {
  def this(value: Long) = {
    this((value >> 16 & 0xff) / 255.0, (value >> 8 & 0xff) / 255.0, (value >> 0 & 0xff) / 255.0, (value >> 24 & 0xff) / 255.0)
  }

  def apply(red: Double = red, green: Double = green, blue: Double = blue, alpha: Double = alpha) = {
    new ImmutableColor(red, green, blue, alpha)
  }

  def mutable = new MutableColor(red, green, blue, alpha)
  def immutable = this
}

sealed abstract class EnumColor(red: Double, green: Double, blue: Double, alpha: Double) extends ImmutableColor(red, green, blue, alpha) with EnumEntry {
  def this(value: Long) = {
    this((value >> 16 & 0xff) / 255.0, (value >> 8 & 0xff) / 255.0, (value >> 0 & 0xff) / 255.0, (value >> 24 & 0xff) / 255.0)
  }

  override def toString = name
}

class MutableColor(var red: Double, var green: Double, var blue: Double, var alpha: Double) extends Color {
  def apply(red: Double = red, green: Double = green, blue: Double = blue, alpha: Double = alpha) = {
    new MutableColor(red, green, blue, alpha)
  }

  def mutable = this
  def immutable = new ImmutableColor(red, green, blue, alpha)
}
