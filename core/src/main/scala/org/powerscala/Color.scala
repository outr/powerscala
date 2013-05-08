package org.powerscala

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Color extends EnumEntry[Color] {
  def red: Double
  def green: Double
  def blue: Double
  def alpha: Double

  object hex {
    def red = toHex(Color.this.red)
    def green = toHex(Color.this.green)
    def blue = toHex(Color.this.blue)
    def alpha = toHex(Color.this.alpha)

    def rgb = "#%s%s%s".format(red, green, blue)

    private def toHex(v: Double) = {
      java.lang.Long.toString((v * 255.0).toLong, 16) match {
        case s if (s.length == 1) => "0%s".format(s)
        case s => s
      }
    }
  }

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

  override def toString = "Color(red=%s, green=%s, blue=%s, alpha=%s)".format(red, green, blue, alpha)
}

object Color extends Enumerated[Color] {
  val AliceBlue = immutable(0xfff0f8ff)
  val AntiqueWhite = immutable(0xfffaebd7)
  val Aquamarine = immutable(0xff7fffd4)
  val Azure = immutable(0xfff0ffff)
  val Beige = immutable(0xfff5f5dc)
  val Bisque = immutable(0xffffe4c4)
  val Black = immutable(0xff000000)
  val BlanchedAlmond = immutable(0xffffebcd)
  val Blue = immutable(0xff0000ff)
  val BlueViolet = immutable(0xff8a2be2)
  val Brown = immutable(0xffa52a2a)
  val Burlywood = immutable(0xffdeb887)
  val CadetBlue = immutable(0xff5f9ea0)
  val Chartreuse = immutable(0xff7fff00)
  val Chocolate = immutable(0xffd2691e)
  val Coral = immutable(0xffff7f50)
  val CornflowerBlue = immutable(0xff6495ed)
  val Cornsilk = immutable(0xfffff8dc)
  val Cyan = immutable(0xff00cdcd)
  val DarkBlue = immutable(0xff00008b)
  val DarkGoldenrod = immutable(0xffb8860b)
  val DarkGray = immutable(0xff3f3f3f)
  val DarkGreen = immutable(0xff006400)
  val DarkKhaki = immutable(0xffbdb76b)
  val DarkOliveGreen = immutable(0xff556b2f)
  val DarkOrange = immutable(0xffff8c00)
  val DarkOrchid = immutable(0xff9932cc)
  val DarkRed = immutable(0xff8b0000)
  val DarkSalmon = immutable(0xffe9967a)
  val DarkSeaGreen = immutable(0xff8fbc8f)
  val DarkSlateBlue = immutable(0xff483d8b)
  val DarkSlateGray = immutable(0xff2f4f4f)
  val DarkTurquoise = immutable(0xff00ced1)
  val DarkViolet = immutable(0xff9400d3)
  val DeepPink = immutable(0xffff1493)
  val DeepSkyBlue = immutable(0xff00bfff)
  val DimGray = immutable(0xff696969)
  val DodgerBlue = immutable(0xff1e90ff)
  val Firebrick = immutable(0xffb22222)
  val FloralWhite = immutable(0xfffffaf0)
  val ForestGreen = immutable(0xff228b22)
  val Gainsboro = immutable(0xffdcdcdc)
  val GhostWhite = immutable(0xfff8f8ff)
  val Gold = immutable(0xffffd700)
  val Goldenrod = immutable(0xffdaa520)
  val Gray = immutable(0xffbebebe)
  val Green = immutable(0xff008000)
  val GreenYellow = immutable(0xffadff2f)
  val HaloBlue = immutable(0xff93a9b4)
  val HighlightBlue = immutable(0xffb2e1ff)
  val Honeydew = immutable(0xfff0fff0)
  val HotPink = immutable(0xffff69b4)
  val IndianRed = immutable(0xffcd5c5c)
  val Ivory = immutable(0xfffffff0)
  val Khaki = immutable(0xfff0e68c)
  val Lavender = immutable(0xffe6e6fa)
  val LavenderBlush = immutable(0xfffff0f5)
  val LawnGreen = immutable(0xff7cfc00)
  val LemonChiffon = immutable(0xfffffacd)
  val LightBlue = immutable(0xffadd8e6)
  val LightCoral = immutable(0xfff08080)
  val LightCyan = immutable(0xffe0ffff)
  val LightGoldenrod = immutable(0xffeedd82)
  val LightGoldenrodYellow = immutable(0xfffafad2)
  val LightGray = immutable(0xffd3d3d3)
  val LightPink = immutable(0xffffb6c1)
  val LightSalmon = immutable(0xffffa07a)
  val LightSeaGreen = immutable(0xff20b2aa)
  val LightSkyBlue = immutable(0xff87cefa)
  val LightSlateBlue = immutable(0xff8470ff)
  val LightSlateGray = immutable(0xff778899)
  val LightSteelBlue = immutable(0xffb0c4de)
  val LightYellow = immutable(0xffffffe0)
  val LimeGreen = immutable(0xff32cd32)
  val Linen = immutable(0xfffaf0e6)
  val Magenta = immutable(0xffff00ff)
  val Maroon = immutable(0xffb03060)
  val MediumAquamarine = immutable(0xff66cdaa)
  val MediumBlue = immutable(0xff0000cd)
  val MediumOrchid = immutable(0xffba55d3)
  val MediumPurple = immutable(0xff9370db)
  val MediumSeaGreen = immutable(0xff3cb371)
  val MediumSlateBlue = immutable(0xff7b68ee)
  val MediumSpringGreen = immutable(0xff00fa9a)
  val MediumTurquoise = immutable(0xff48d1cc)
  val MediumVioletRed = immutable(0xffc71585)
  val MidnightBlue = immutable(0xff191970)
  val MintCream = immutable(0xfff5fffa)
  val MistyRose = immutable(0xffffe4e1)
  val Moccasin = immutable(0xffffe4b5)
  val NavajoWhite = immutable(0xffffdead)
  val NavyBlue = immutable(0xff000080)
  val OldLace = immutable(0xfffdf5e6)
  val OliveDrab = immutable(0xff6b8e23)
  val Orange = immutable(0xffffa500)
  val OrangeRed = immutable(0xffff4500)
  val Orchid = immutable(0xffda70d6)
  val PaleGoldenrod = immutable(0xffeee8aa)
  val PaleGreen = immutable(0xff98fb98)
  val PaleTurquoise = immutable(0xffafeeee)
  val PaleVioletRed = immutable(0xffdb7093)
  val PapayaWhip = immutable(0xffffefd5)
  val PeachPuff = immutable(0xffffdab9)
  val Peru = immutable(0xffcd853f)
  val Pink = immutable(0xffffc0cb)
  val Plum = immutable(0xffdda0dd)
  val PowderBlue = immutable(0xffb0e0e6)
  val Purple = immutable(0xff800080)
  val Red = immutable(0xffff0000)
  val RosyBrown = immutable(0xffbc8f8f)
  val RoyalBlue = immutable(0xff4169e1)
  val SaddleBrown = immutable(0xff8b4513)
  val Salmon = immutable(0xfffa8072)
  val SandyBrown = immutable(0xfff4a460)
  val SeaGreen = immutable(0xff2e8b57)
  val Seashell = immutable(0xfffff5ee)
  val SelectBlue = immutable(0xff4394ff)
  val Sienna = immutable(0xffa0522d)
  val SkyBlue = immutable(0xff87ceeb)
  val SlateBlue = immutable(0xff6a5acd)
  val SlateGray = immutable(0xff708090)
  val Snow = immutable(0xfffffafa)
  val SpringGreen = immutable(0xff00ff7f)
  val SteelBlue = immutable(0xff4682b4)
  val Tan = immutable(0xffd2b48c)
  val Thistle = immutable(0xffd8bfd8)
  val Tomato = immutable(0xffff6347)
  val Turquoise = immutable(0xff40e0d0)
  val UmmGold = immutable(0xffffcc33)
  val UmmMaroon = immutable(0xff660000)
  val Violet = immutable(0xffee82ee)
  val VioletRed = immutable(0xffd02090)
  val Wheat = immutable(0xfff5deb3)
  val White = immutable(0xffffffff)
  val WhiteSmoke = immutable(0xfff5f5f5)
  val Yellow = immutable(0xffffff00)
  val YellowGreen = immutable(0xff9acd32)

  val Clear = immutable(0x00ffffff)

  def immutable(value: Long): ImmutableColor = {
    val alpha = (value >> 24 & 0xff) / 255.0
    val red = (value >> 16 & 0xff) / 255.0
    val green = (value >> 8 & 0xff) / 255.0
    val blue = (value >> 0 & 0xff) / 255.0
    immutable(red, green, blue, alpha)
  }

  def immutable(red: Int, green: Int, blue: Int, alpha: Int): ImmutableColor = {
    immutable(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0)
  }

  def immutable(hex: String): ImmutableColor = {
    val (red, green, blue, alpha) = convertHex(hex)
    new ImmutableColor(red, green, blue, alpha)
  }

  def immutable(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0, alpha: Double = 1.0): ImmutableColor = {
    ImmutableColor(red, green, blue, alpha)
  }

  def mutable(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0, alpha: Double = 1.0): MutableColor = {
    MutableColor(red, green, blue, alpha)
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
    MutableColor(red, green, blue, alpha)
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

case class ImmutableColor(red: Double, green: Double, blue: Double, alpha: Double) extends Color {
  def apply(red: Double = red, green: Double = green, blue: Double = blue, alpha: Double = alpha) = {
    copy(red, green, blue, alpha)
  }

  def mutable = MutableColor(red, green, blue, alpha)
}

case class MutableColor(var red: Double, var green: Double, var blue: Double, var alpha: Double) extends Color {
  def apply(red: Double = red, green: Double = green, blue: Double = blue, alpha: Double = alpha) = {
    copy(red, green, blue, alpha)
  }

  def immutable = ImmutableColor(red, green, blue, alpha)
}