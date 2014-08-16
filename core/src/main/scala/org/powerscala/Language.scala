package org.powerscala

import java.io.{FileWriter, File}

import org.powerscala.enum.{Enumerated, EnumEntry}

import scala.io.Source

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed class Language extends EnumEntry {
  override def toString = name
}

object Language extends Enumerated[Language] {
  val English = new Language
  val Spanish = new Language
  val German = new Language
  val French = new Language
  val Acehnese = new Language
  val Afrikaans = new Language
  val Akan = new Language
  val Albanian = new Language
  val Amharic = new Language
  val Arabic = new Language
  val Armenian = new Language
  val Assamese = new Language
  val Awadhi = new Language
  val Azerbaijani = new Language
  val Bagheli = new Language
  val Balinese = new Language
  val Balochi = new Language
  val Bambara = new Language
  val Batak = new Language
  val Bavarian = new Language
  val Belarusian = new Language
  val Bemba = new Language
  val Bengali = new Language
  val Bhili = new Language
  val Bhojpuri = new Language
  val Bikol = new Language
  val Buginese = new Language
  val Bulgarian = new Language
  val Burmese = new Language
  val Catalan = new Language
  val Cebuano = new Language
  val CentralTibetan = new Language
  val Chewa = new Language
  val Chhattisgarhi = new Language
  val ChineseSignLanguage = new Language
  val Chittagonian = new Language
  val Czech = new Language
  val Danish = new Language
  val Deccan = new Language
  val Dholuo = new Language
  val Dogri = new Language
  val Dutch = new Language
  val Efik = new Language
  val Ewe = new Language
  val Finnish = new Language
  val Fula = new Language
  val Galician = new Language
  val Gan = new Language
  val Ganda = new Language
  val Georgian = new Language
  val Gikuyu = new Language
  val Greek = new Language
  val Guarani = new Language
  val Gujarati = new Language
  val HaitianCreole = new Language
  val Hakka = new Language
  val Haryanvi = new Language
  val Hausa = new Language
  val Hebrew = new Language
  val Hiligaynon = new Language
  val Hindustani = new Language
  val Hmong = new Language
  val Hungarian = new Language
  val Igbo = new Language
  val Ilokano = new Language
  val IndoPakistaniSignLanguage = new Language
  val Italian = new Language
  val JamaicanCreole = new Language
  val Japanese = new Language
  val Javanese = new Language
  val Kamba = new Language
  val Kanauji = new Language
  val Kannada = new Language
  val Kanuri = new Language
  val Kashmiri = new Language
  val Kazakh = new Language
  val Khmer = new Language
  val Kimbundu = new Language
  val Kituba = new Language
  val Kongo = new Language
  val Konkani = new Language
  val Korean = new Language
  val Kurdish = new Language
  val Kyrgyz = new Language
  val LaoIsan = new Language
  val Lingala = new Language
  val Lithuanian = new Language
  val Lombard = new Language
  val Luyia = new Language
  val Madurese = new Language
  val Magahi = new Language
  val Maithili = new Language
  val Makuwa = new Language
  val Malagasy = new Language
  val Malay = new Language
  val Malayalam = new Language
  val Mandarin = new Language
  val Mandingo = new Language
  val Marathi = new Language
  val Marwari = new Language
  val MazanderaniGilaki = new Language
  val MinBei = new Language
  val MinDong = new Language
  val Minangkabau = new Language
  val Mongolian = new Language
  val MossiDagomba = new Language
  val Neapolitan = new Language
  val Nepali = new Language
  val NorthernBerber = new Language
  val Norwegian = new Language
  val Oriya = new Language
  val Oromo = new Language
  val Pashto = new Language
  val Persian = new Language
  val Piemonteis = new Language
  val Polish = new Language
  val Portuguese = new Language
  val Punjabi = new Language
  val Rajasthani = new Language
  val Rangpuri = new Language
  val Romanian = new Language
  val Russian = new Language
  val RwandaRundi = new Language
  val Santali = new Language
  val SerboCroatian = new Language
  val Shan = new Language
  val Shona = new Language
  val Sicilian = new Language
  val Sindhi = new Language
  val Sinhalese = new Language
  val Slovak = new Language
  val Somali = new Language
  val SothoTswana = new Language
  val SouthernQuechua = new Language
  val SouthernThai = new Language
  val SukumaNyamwezi = new Language
  val Sundanese = new Language
  val Swahili = new Language
  val Swedish = new Language
  val Sylheti = new Language
  val Tagalog = new Language
  val Taiwanese = new Language
  val Tamil = new Language
  val TatarBashkir = new Language
  val Telugu = new Language
  val Thai = new Language
  val Tigrinya = new Language
  val Tshiluba = new Language
  val Tsonga = new Language
  val Turkish = new Language
  val Turkmen = new Language
  val Ukrainian = new Language
  val Umbundu = new Language
  val Uyghur = new Language
  val Uzbek = new Language
  val Venetian = new Language
  val Vietnamese = new Language
  val Wolof = new Language
  val Wu = new Language
  val Xhosa = new Language
  val Xiang = new Language
  val Yi = new Language
  val Yoruba = new Language
  val Yue = new Language
  val Zhuang = new Language
  val Zulu = new Language

  def main(args: Array[String]): Unit = {
    values.foreach {
      case l => NewLanguage.get(l.name) match {
        case Some(nl) => // Found
        case None => System.err.println(s"Language: ${l.name} not found!")
      }
    }
  }

  def generate() = {
    val CodeRegex = """(\S{3})""".r
    val AlternativeCodesRegex = """(\S{3}) [(]B[)] (\S{3}) [(]T[)]""".r
    val source = Source.fromFile(new File("../iso639-2.csv"))
    try {
      val writer = new FileWriter(new File("../NewLanguage.scala"))
      try {
        val items = source.getLines().map {
          case LanguageResult(iso6392, iso6391, english, french, german) => {
            //          println(s"LanguageResult($iso6392 - $iso6391 - $english - $french - $german) - ${if (iso6391.nonEmpty) iso6391.charAt(0).isWhitespace}")
            val (bibliographic, terminology) = iso6392 match {
              case CodeRegex(code) => code -> code
              case AlternativeCodesRegex(b, t) => b -> t
            }
            val englishLanguages = english.split(";").toList
            val frenchLanguages = french.split(";").map(s => '"' + s.trim + '"').toList.mkString(", ")
            val germanLanguages = german.split(";").map(s => '"' + s.trim + '"').toList.mkString(", ")
            val code2Option = iso6391 match {
              case "" => "None"
              case _ => s"""Some("$iso6391")"""
            }
            val name = englishLanguages.head.split("[- ,']").map(s => s.capitalize).mkString("").replaceAll("[(].*[)]", "")
            s"""val $name = new NewLanguage("$bibliographic", "$terminology", $code2Option, List(${englishLanguages.map(s => '"' + s.trim + '"').mkString(", ")}), List($frenchLanguages), List($germanLanguages))"""
          }
        }.toList
        items.distinct.sorted.foreach {
          case line => writer.write(s"$line\r\n")
        }
      } finally {
        writer.flush()
        writer.close()
      }
    } finally {
      source.close()
    }
  }
}

object LanguageResult {
  import StringUtil._
  def unapply(s: String): Option[(String, String, String, String, String)] = {
    var quoted = false
    var start = 0
    var entries = List.empty[String]
    def extractEntry(index: Int) = {
      var entry = s.substring(start, index).trim
      if (entry.nonEmpty && entry.charAt(0) == '"') {
        entry = entry.substring(1)
      }
      if (entry.nonEmpty && entry.charAt(entry.length - 1) == '"') {
        entry = entry.substring(0, entry.length - 1)
      }
      entry
    }
    s.zipWithIndex.foreach {
      case (c, index) => if (c == '"' && quoted) {
        quoted = false
      } else if (c == '"') {
        quoted = true
      } else if (c == ',' && !quoted) {
        quoted = false
        entries = extractEntry(index) :: entries
        start = index + 1
      }
    }
    entries = extractEntry(s.length) :: entries
    val items = entries.reverse.toVector
    if (items.size != 5) {
      throw new RuntimeException(s"Unable to parse language. Invalid number of entries: ${items.size} but expected 5. Values: ${items.mkString(" - ")}")
    }
    Some(trim(items(0)), trim(items(1)), trim(items(2)), trim(items(3)), trim(items(4)))
  }
}

class NewLanguage(val bibliographic: String,
                  val terminology: String,
                  val iso639_1: Option[String],
                  val englishNames: List[String],
                  val frenchNames: List[String],
                  val germanNames: List[String]) extends EnumEntry {
  override def isMatch(s: String) = s.equalsIgnoreCase(bibliographic) ||
                                    s.equalsIgnoreCase(terminology) ||
                                    (iso639_1.nonEmpty && s.equalsIgnoreCase(iso639_1.get)) ||
                                    englishNames.find(n => s.equalsIgnoreCase(n)).nonEmpty ||
                                    frenchNames.find(n => s.equalsIgnoreCase(n)).nonEmpty ||
                                    germanNames.find(n => s.equalsIgnoreCase(n)).nonEmpty
}

object NewLanguage extends Enumerated[NewLanguage] {
  val Abkhazian = new NewLanguage("abk", "abk", Some("ab"), List("Abkhazian"), List("abkhaze"), List("Abchasisch"))
  val Achinese = new NewLanguage("ace", "ace", None, List("Achinese"), List("aceh"), List("Aceh-Sprache"))
  val Acoli = new NewLanguage("ach", "ach", None, List("Acoli"), List("acoli"), List("Acholi-Sprache"))
  val Adangme = new NewLanguage("ada", "ada", None, List("Adangme"), List("adangme"), List("Adangme-Sprache"))
  val Adyghe = new NewLanguage("ady", "ady", None, List("Adyghe", "Adygei"), List("adyghé"), List("Adygisch"))
  val Afar = new NewLanguage("aar", "aar", Some("aa"), List("Afar"), List("afar"), List("Danakil-Sprache"))
  val Afrihili = new NewLanguage("afh", "afh", None, List("Afrihili"), List("afrihili"), List("Afrihili"))
  val Afrikaans = new NewLanguage("afr", "afr", Some("af"), List("Afrikaans"), List("afrikaans"), List("Afrikaans"))
  val AfroAsiaticLanguages = new NewLanguage("afa", "afa", None, List("Afro-Asiatic languages"), List("afro-asiatiques, langues"), List("Hamitosemitische Sprachen (Andere)"))
  val Ainu = new NewLanguage("ain", "ain", None, List("Ainu"), List("aïnou"), List("Ainu-Sprache"))
  val Akan = new NewLanguage("aka", "aka", Some("ak"), List("Akan"), List("akan"), List("Akan-Sprache"))
  val Akkadian = new NewLanguage("akk", "akk", None, List("Akkadian"), List("akkadien"), List("Akkadisch"))
  val Albanian = new NewLanguage("alb", "sqi", Some("sq"), List("Albanian"), List("albanais"), List("Albanisch"))
  val Aleut = new NewLanguage("ale", "ale", None, List("Aleut"), List("aléoute"), List("Aleutisch"))
  val AlgonquianLanguages = new NewLanguage("alg", "alg", None, List("Algonquian languages"), List("algonquines, langues"), List("Algonkin-Sprachen (Andere)"))
  val AltaicLanguages = new NewLanguage("tut", "tut", None, List("Altaic languages"), List("altaïques, langues"), List("Altaische Sprachen (Andere)"))
  val Amharic = new NewLanguage("amh", "amh", Some("am"), List("Amharic"), List("amharique"), List("Amharisch"))
  val Angika = new NewLanguage("anp", "anp", None, List("Angika"), List("angika"), List("Anga-Sprache"))
  val ApacheLanguages = new NewLanguage("apa", "apa", None, List("Apache languages"), List("apaches, langues"), List("Apachen-Sprachen"))
  val Arabic = new NewLanguage("ara", "ara", Some("ar"), List("Arabic"), List("arabe"), List("Arabisch"))
  val Aragonese = new NewLanguage("arg", "arg", Some("an"), List("Aragonese"), List("aragonais"), List("Aragonesisch"))
  val Arapaho = new NewLanguage("arp", "arp", None, List("Arapaho"), List("arapaho"), List("Arapaho-Sprache"))
  val Arawak = new NewLanguage("arw", "arw", None, List("Arawak"), List("arawak"), List("Arawak-Sprachen"))
  val Armenian = new NewLanguage("arm", "hye", Some("hy"), List("Armenian"), List("arménien"), List("Armenisch"))
  val Aromanian = new NewLanguage("rup", "rup", None, List("Aromanian", "Arumanian", "Macedo-Romanian"), List("aroumain", "macédo-roumain"), List("Aromunisch"))
  val ArtificialLanguages = new NewLanguage("art", "art", None, List("Artificial languages"), List("artificielles, langues"), List("Kunstsprachen (Andere)"))
  val Assamese = new NewLanguage("asm", "asm", Some("as"), List("Assamese"), List("assamais"), List("Assamesisch"))
  val Asturian = new NewLanguage("ast", "ast", None, List("Asturian", "Bable", "Leonese", "Asturleonese"), List("asturien", "bable", "léonais", "asturoléonais"), List("Asturisch"))
  val AthapascanLanguages = new NewLanguage("ath", "ath", None, List("Athapascan languages"), List("athapascanes, langues"), List("Athapaskische Sprachen (Andere)"))
  val AustralianLanguages = new NewLanguage("aus", "aus", None, List("Australian languages"), List("australiennes, langues"), List("Australische Sprachen"))
  val AustronesianLanguages = new NewLanguage("map", "map", None, List("Austronesian languages"), List("austronésiennes, langues"), List("Austronesische Sprachen (Andere)"))
  val Avaric = new NewLanguage("ava", "ava", Some("av"), List("Avaric"), List("avar"), List("Awarisch"))
  val Avestan = new NewLanguage("ave", "ave", Some("ae"), List("Avestan"), List("avestique"), List("Avestisch"))
  val Awadhi = new NewLanguage("awa", "awa", None, List("Awadhi"), List("awadhi"), List("Awadhi"))
  val Aymara = new NewLanguage("aym", "aym", Some("ay"), List("Aymara"), List("aymara"), List("Aymará-Sprache"))
  val Azerbaijani = new NewLanguage("aze", "aze", Some("az"), List("Azerbaijani"), List("azéri"), List("Aserbeidschanisch"))
  val Balinese = new NewLanguage("ban", "ban", None, List("Balinese"), List("balinais"), List("Balinesisch"))
  val BalticLanguages = new NewLanguage("bat", "bat", None, List("Baltic languages"), List("baltes, langues"), List("Baltische Sprachen (Andere)"))
  val Baluchi = new NewLanguage("bal", "bal", None, List("Baluchi"), List("baloutchi"), List("Belutschisch"))
  val Bambara = new NewLanguage("bam", "bam", Some("bm"), List("Bambara"), List("bambara"), List("Bambara-Sprache"))
  val BamilekeLanguages = new NewLanguage("bai", "bai", None, List("Bamileke languages"), List("bamiléké, langues"), List("Bamileke-Sprachen"))
  val BandaLanguages = new NewLanguage("bad", "bad", None, List("Banda languages"), List("banda, langues"), List("Banda-Sprachen (Ubangi-Sprachen)"))
  val BantuLanguages = new NewLanguage("bnt", "bnt", None, List("Bantu languages"), List("bantou, langues"), List("Bantusprachen (Andere)"))
  val Basa = new NewLanguage("bas", "bas", None, List("Basa"), List("basa"), List("Basaa-Sprache"))
  val Bashkir = new NewLanguage("bak", "bak", Some("ba"), List("Bashkir"), List("bachkir"), List("Baschkirisch"))
  val Basque = new NewLanguage("baq", "eus", Some("eu"), List("Basque"), List("basque"), List("Baskisch"))
  val BatakLanguages = new NewLanguage("btk", "btk", None, List("Batak languages"), List("batak, langues"), List("Batak-Sprache"))
  val Beja = new NewLanguage("bej", "bej", None, List("Beja", "Bedawiyet"), List("bedja"), List("Bedauye"))
  val Belarusian = new NewLanguage("bel", "bel", Some("be"), List("Belarusian"), List("biélorusse"), List("Weißrussisch"))
  val Bemba = new NewLanguage("bem", "bem", None, List("Bemba"), List("bemba"), List("Bemba-Sprache"))
  val Bengali = new NewLanguage("ben", "ben", Some("bn"), List("Bengali"), List("bengali"), List("Bengali"))
  val BerberLanguages = new NewLanguage("ber", "ber", None, List("Berber languages"), List("berbères, langues"), List("Berbersprachen (Andere)"))
  val Bhojpuri = new NewLanguage("bho", "bho", None, List("Bhojpuri"), List("bhojpuri"), List("Bhojpuri"))
  val BihariLanguages = new NewLanguage("bih", "bih", Some("bh"), List("Bihari languages"), List("langues biharis"), List("Bihari (Andere)"))
  val Bikol = new NewLanguage("bik", "bik", None, List("Bikol"), List("bikol"), List("Bikol-Sprache"))
  val Bini = new NewLanguage("bin", "bin", None, List("Bini", "Edo"), List("bini", "edo"), List("Edo-Sprache"))
  val Bislama = new NewLanguage("bis", "bis", Some("bi"), List("Bislama"), List("bichlamar"), List("Beach-la-mar"))
  val Blin = new NewLanguage("byn", "byn", None, List("Blin", "Bilin"), List("blin", "bilen"), List("Bilin-Sprache"))
  val Blissymbols = new NewLanguage("zbl", "zbl", None, List("Blissymbols", "Blissymbolics", "Bliss"), List("symboles Bliss", "Bliss"), List("Bliss-Symbol"))
  val BokmålNorwegian = new NewLanguage("nob", "nob", Some("nb"), List("Bokmål, Norwegian", "Norwegian Bokmål"), List("norvégien bokmål"), List("Bokmål"))
  val Bosnian = new NewLanguage("bos", "bos", Some("bs"), List("Bosnian"), List("bosniaque"), List("Bosnisch"))
  val Braj = new NewLanguage("bra", "bra", None, List("Braj"), List("braj"), List("Braj-Bhakha"))
  val Breton = new NewLanguage("bre", "bre", Some("br"), List("Breton"), List("breton"), List("Bretonisch"))
  val Buginese = new NewLanguage("bug", "bug", None, List("Buginese"), List("bugi"), List("Bugi-Sprache"))
  val Bulgarian = new NewLanguage("bul", "bul", Some("bg"), List("Bulgarian"), List("bulgare"), List("Bulgarisch"))
  val Buriat = new NewLanguage("bua", "bua", None, List("Buriat"), List("bouriate"), List("Burjatisch"))
  val Burmese = new NewLanguage("bur", "mya", Some("my"), List("Burmese"), List("birman"), List("Birmanisch"))
  val Caddo = new NewLanguage("cad", "cad", None, List("Caddo"), List("caddo"), List("Caddo-Sprachen"))
  val Catalan = new NewLanguage("cat", "cat", Some("ca"), List("Catalan", "Valencian"), List("catalan", "valencien"), List("Katalanisch"))
  val CaucasianLanguages = new NewLanguage("cau", "cau", None, List("Caucasian languages"), List("caucasiennes, langues"), List("Kaukasische Sprachen (Andere)"))
  val Cebuano = new NewLanguage("ceb", "ceb", None, List("Cebuano"), List("cebuano"), List("Cebuano"))
  val CelticLanguages = new NewLanguage("cel", "cel", None, List("Celtic languages"), List("celtiques, langues", "celtes, langues"), List("Keltische Sprachen (Andere)"))
  val CentralAmericanIndianLanguages = new NewLanguage("cai", "cai", None, List("Central American Indian languages"), List("amérindiennes de l'Amérique centrale, langues"), List("Indianersprachen, Zentralamerika (Andere)"))
  val CentralKhmer = new NewLanguage("khm", "khm", Some("km"), List("Central Khmer"), List("khmer central"), List("Kambodschanisch"))
  val Chagatai = new NewLanguage("chg", "chg", None, List("Chagatai"), List("djaghataï"), List("Tschagataisch"))
  val ChamicLanguages = new NewLanguage("cmc", "cmc", None, List("Chamic languages"), List("chames, langues"), List("Cham-Sprachen"))
  val Chamorro = new NewLanguage("cha", "cha", Some("ch"), List("Chamorro"), List("chamorro"), List("Chamorro-Sprache"))
  val Chechen = new NewLanguage("che", "che", Some("ce"), List("Chechen"), List("tchétchène"), List("Tschetschenisch"))
  val Cherokee = new NewLanguage("chr", "chr", None, List("Cherokee"), List("cherokee"), List("Cherokee-Sprache"))
  val Cheyenne = new NewLanguage("chy", "chy", None, List("Cheyenne"), List("cheyenne"), List("Cheyenne-Sprache"))
  val Chibcha = new NewLanguage("chb", "chb", None, List("Chibcha"), List("chibcha"), List("Chibcha-Sprachen"))
  val Chichewa = new NewLanguage("nya", "nya", Some("ny"), List("Chichewa", "Chewa", "Nyanja"), List("chichewa", "chewa", "nyanja"), List("Nyanja-Sprache"))
  val Chinese = new NewLanguage("chi", "zho", Some("zh"), List("Chinese"), List("chinois"), List("Chinesisch"))
  val ChinookJargon = new NewLanguage("chn", "chn", None, List("Chinook jargon"), List("chinook, jargon"), List("Chinook-Jargon"))
  val Chipewyan = new NewLanguage("chp", "chp", None, List("Chipewyan", "Dene Suline"), List("chipewyan"), List("Chipewyan-Sprache"))
  val Choctaw = new NewLanguage("cho", "cho", None, List("Choctaw"), List("choctaw"), List("Choctaw-Sprache"))
  val ChurchSlavic = new NewLanguage("chu", "chu", Some("cu"), List("Church Slavic", "Old Slavonic", "Church Slavonic", "Old Bulgarian", "Old Church Slavonic"), List("slavon d'église", "vieux slave", "slavon liturgique", "vieux bulgare"), List("Kirchenslawisch"))
  val Chuukese = new NewLanguage("chk", "chk", None, List("Chuukese"), List("chuuk"), List("Trukesisch"))
  val Chuvash = new NewLanguage("chv", "chv", Some("cv"), List("Chuvash"), List("tchouvache"), List("Tschuwaschisch"))
  val ClassicalNewari = new NewLanguage("nwc", "nwc", None, List("Classical Newari", "Old Newari", "Classical Nepal Bhasa"), List("newari classique"), List("Alt-Newari"))
  val ClassicalSyriac = new NewLanguage("syc", "syc", None, List("Classical Syriac"), List("syriaque classique"), List("Syrisch"))
  val Coptic = new NewLanguage("cop", "cop", None, List("Coptic"), List("copte"), List("Koptisch"))
  val Cornish = new NewLanguage("cor", "cor", Some("kw"), List("Cornish"), List("cornique"), List("Kornisch"))
  val Corsican = new NewLanguage("cos", "cos", Some("co"), List("Corsican"), List("corse"), List("Korsisch"))
  val Cree = new NewLanguage("cre", "cre", Some("cr"), List("Cree"), List("cree"), List("Cree-Sprache"))
  val Creek = new NewLanguage("mus", "mus", None, List("Creek"), List("muskogee"), List("Muskogisch"))
  val CreolesAndPidgins = new NewLanguage("crp", "crp", None, List("Creoles and pidgins"), List("créoles et pidgins"), List("Kreolische Sprachen", "Pidginsprachen (Andere)"))
  val CreolesAndPidginsEnglishBased = new NewLanguage("cpe", "cpe", None, List("Creoles and pidgins, English based"), List("créoles et pidgins basés sur l'anglais"), List("Kreolisch-Englisch (Andere)"))
  val CreolesAndPidginsFrenchBased = new NewLanguage("cpf", "cpf", None, List("Creoles and pidgins, French-based"), List("créoles et pidgins basés sur le français"), List("Kreolisch-Französisch (Andere)"))
  val CreolesAndPidginsPortugueseBased = new NewLanguage("cpp", "cpp", None, List("Creoles and pidgins, Portuguese-based"), List("créoles et pidgins basés sur le portugais"), List("Kreolisch-Portugiesisch (Andere)"))
  val CrimeanTatar = new NewLanguage("crh", "crh", None, List("Crimean Tatar", "Crimean Turkish"), List("tatar de Crimé"), List("Krimtatarisch"))
  val Croatian = new NewLanguage("hrv", "hrv", Some("hr"), List("Croatian"), List("croate"), List("Kroatisch"))
  val CushiticLanguages = new NewLanguage("cus", "cus", None, List("Cushitic languages"), List("couchitiques, langues"), List("Kuschitische Sprachen (Andere)"))
  val Czech = new NewLanguage("cze", "ces", Some("cs"), List("Czech"), List("tchèque"), List("Tschechisch"))
  val Dakota = new NewLanguage("dak", "dak", None, List("Dakota"), List("dakota"), List("Dakota-Sprache"))
  val Danish = new NewLanguage("dan", "dan", Some("da"), List("Danish"), List("danois"), List("Dänisch"))
  val Dargwa = new NewLanguage("dar", "dar", None, List("Dargwa"), List("dargwa"), List("Darginisch"))
  val Delaware = new NewLanguage("del", "del", None, List("Delaware"), List("delaware"), List("Delaware-Sprache"))
  val Dinka = new NewLanguage("din", "din", None, List("Dinka"), List("dinka"), List("Dinka-Sprache"))
  val Divehi = new NewLanguage("div", "div", Some("dv"), List("Divehi", "Dhivehi", "Maldivian"), List("maldivien"), List("Maledivisch"))
  val Dogri = new NewLanguage("doi", "doi", None, List("Dogri"), List("dogri"), List("Dogri"))
  val Dogrib = new NewLanguage("dgr", "dgr", None, List("Dogrib"), List("dogrib"), List("Dogrib-Sprache"))
  val DravidianLanguages = new NewLanguage("dra", "dra", None, List("Dravidian languages"), List("dravidiennes, langues"), List("Drawidische Sprachen (Andere)"))
  val Duala = new NewLanguage("dua", "dua", None, List("Duala"), List("douala"), List("Duala-Sprachen"))
  val Dutch = new NewLanguage("dut", "nld", Some("nl"), List("Dutch", "Flemish"), List("néerlandais", "flamand"), List("Niederländisch"))
  val DutchMiddle = new NewLanguage("dum", "dum", None, List("Dutch, Middle (ca.1050-1350)"), List("néerlandais moyen (ca. 1050-1350)"), List("Mittelniederländisch"))
  val Dyula = new NewLanguage("dyu", "dyu", None, List("Dyula"), List("dioula"), List("Dyula-Sprache"))
  val Dzongkha = new NewLanguage("dzo", "dzo", Some("dz"), List("Dzongkha"), List("dzongkha"), List("Dzongkha"))
  val EasternFrisian = new NewLanguage("frs", "frs", None, List("Eastern Frisian"), List("frison oriental"), List("Ostfriesisch"))
  val Efik = new NewLanguage("efi", "efi", None, List("Efik"), List("efik"), List("Efik"))
  val Egyptian = new NewLanguage("egy", "egy", None, List("Egyptian (Ancient)"), List("égyptien"), List("Ägyptisch"))
  val Ekajuk = new NewLanguage("eka", "eka", None, List("Ekajuk"), List("ekajuk"), List("Ekajuk"))
  val Elamite = new NewLanguage("elx", "elx", None, List("Elamite"), List("élamite"), List("Elamisch"))
  val English = new NewLanguage("eng", "eng", Some("en"), List("English"), List("anglais"), List("Englisch"))
  val EnglishMiddle = new NewLanguage("enm", "enm", None, List("English, Middle (1100-1500)"), List("anglais moyen (1100-1500)"), List("Mittelenglisch"))
  val EnglishOld = new NewLanguage("ang", "ang", None, List("English, Old (ca.450-1100)"), List("anglo-saxon (ca.450-1100)"), List("Altenglisch"))
  val Erzya = new NewLanguage("myv", "myv", None, List("Erzya"), List("erza"), List("Erza-Mordwinisch"))
  val Esperanto = new NewLanguage("epo", "epo", Some("eo"), List("Esperanto"), List("espéranto"), List("Esperanto"))
  val Estonian = new NewLanguage("est", "est", Some("et"), List("Estonian"), List("estonien"), List("Estnisch"))
  val Ewe = new NewLanguage("ewe", "ewe", Some("ee"), List("Ewe"), List("éwé"), List("Ewe-Sprache"))
  val Ewondo = new NewLanguage("ewo", "ewo", None, List("Ewondo"), List("éwondo"), List("Ewondo"))
  val Fang = new NewLanguage("fan", "fan", None, List("Fang"), List("fang"), List("Pangwe-Sprache"))
  val Fanti = new NewLanguage("fat", "fat", None, List("Fanti"), List("fanti"), List("Fante-Sprache"))
  val Faroese = new NewLanguage("fao", "fao", Some("fo"), List("Faroese"), List("féroïen"), List("Färöisch"))
  val Fijian = new NewLanguage("fij", "fij", Some("fj"), List("Fijian"), List("fidjien"), List("Fidschi-Sprache"))
  val Filipino = new NewLanguage("fil", "fil", None, List("Filipino", "Pilipino"), List("filipino", "pilipino"), List("Pilipino"))
  val Finnish = new NewLanguage("fin", "fin", Some("fi"), List("Finnish"), List("finnois"), List("Finnisch"))
  val FinnoUgrianLanguages = new NewLanguage("fiu", "fiu", None, List("Finno-Ugrian languages"), List("finno-ougriennes, langues"), List("Finnougrische Sprachen (Andere)"))
  val Fon = new NewLanguage("fon", "fon", None, List("Fon"), List("fon"), List("Fon-Sprache"))
  val French = new NewLanguage("fre", "fra", Some("fr"), List("French"), List("français"), List("Französisch"))
  val FrenchMiddle = new NewLanguage("frm", "frm", None, List("French, Middle (ca.1400-1600)"), List("français moyen (1400-1600)"), List("Mittelfranzösisch"))
  val FrenchOld = new NewLanguage("fro", "fro", None, List("French, Old (842-ca.1400)"), List("français ancien (842-ca.1400)"), List("Altfranzösisch"))
  val Friulian = new NewLanguage("fur", "fur", None, List("Friulian"), List("frioulan"), List("Friulisch"))
  val Fulah = new NewLanguage("ful", "ful", Some("ff"), List("Fulah"), List("peul"), List("Ful"))
  val Ga = new NewLanguage("gaa", "gaa", None, List("Ga"), List("ga"), List("Ga-Sprache"))
  val Gaelic = new NewLanguage("gla", "gla", Some("gd"), List("Gaelic", "Scottish Gaelic"), List("gaélique", "gaélique écossais"), List("Gälisch-Schottisch"))
  val GalibiCarib = new NewLanguage("car", "car", None, List("Galibi Carib"), List("karib", "galibi", "carib"), List("Karibische Sprachen"))
  val Galician = new NewLanguage("glg", "glg", Some("gl"), List("Galician"), List("galicien"), List("Galicisch"))
  val Ganda = new NewLanguage("lug", "lug", Some("lg"), List("Ganda"), List("ganda"), List("Ganda-Sprache"))
  val Gayo = new NewLanguage("gay", "gay", None, List("Gayo"), List("gayo"), List("Gayo-Sprache"))
  val Gbaya = new NewLanguage("gba", "gba", None, List("Gbaya"), List("gbaya"), List("Gbaya-Sprache"))
  val Geez = new NewLanguage("gez", "gez", None, List("Geez"), List("guèze"), List("Altäthiopisch"))
  val Georgian = new NewLanguage("geo", "kat", Some("ka"), List("Georgian"), List("géorgien"), List("Georgisch"))
  val German = new NewLanguage("ger", "deu", Some("de"), List("German"), List("allemand"), List("Deutsch"))
  val GermanMiddleHigh = new NewLanguage("gmh", "gmh", None, List("German, Middle High (ca.1050-1500)"), List("allemand, moyen haut (ca. 1050-1500)"), List("Mittelhochdeutsch"))
  val GermanOldHigh = new NewLanguage("goh", "goh", None, List("German, Old High (ca.750-1050)"), List("allemand, vieux haut (ca. 750-1050)"), List("Althochdeutsch"))
  val GermanicLanguages = new NewLanguage("gem", "gem", None, List("Germanic languages"), List("germaniques, langues"), List("Germanische Sprachen (Andere)"))
  val Gilbertese = new NewLanguage("gil", "gil", None, List("Gilbertese"), List("kiribati"), List("Gilbertesisch"))
  val Gondi = new NewLanguage("gon", "gon", None, List("Gondi"), List("gond"), List("Gondi-Sprache"))
  val Gorontalo = new NewLanguage("gor", "gor", None, List("Gorontalo"), List("gorontalo"), List("Gorontalesisch"))
  val Gothic = new NewLanguage("got", "got", None, List("Gothic"), List("gothique"), List("Gotisch"))
  val Grebo = new NewLanguage("grb", "grb", None, List("Grebo"), List("grebo"), List("Grebo-Sprache"))
  val GreekAncient = new NewLanguage("grc", "grc", None, List("Greek, Ancient (to 1453)"), List("grec ancien (jusqu'à 1453)"), List("Griechisch"))
  val GreekModern = new NewLanguage("gre", "ell", Some("el"), List("Greek", "Greek, Modern (1453-)"), List("grec moderne (après 1453)"), List("Neugriechisch"))
  val Guarani = new NewLanguage("grn", "grn", Some("gn"), List("Guarani"), List("guarani"), List("Guaraní-Sprache"))
  val Gujarati = new NewLanguage("guj", "guj", Some("gu"), List("Gujarati"), List("goudjrati"), List("Gujarati-Sprache"))
  val GwichIn = new NewLanguage("gwi", "gwi", None, List("Gwich'in"), List("gwich'in"), List("Kutchin-Sprache"))
  val Haida = new NewLanguage("hai", "hai", None, List("Haida"), List("haida"), List("Haida-Sprache"))
  val Haitian = new NewLanguage("hat", "hat", Some("ht"), List("Haitian", "Haitian Creole"), List("haïtien", "créole haïtien"), List("Haïtien (Haiti-Kreolisch)"))
  val Hausa = new NewLanguage("hau", "hau", Some("ha"), List("Hausa"), List("haoussa"), List("Haussa-Sprache"))
  val Hawaiian = new NewLanguage("haw", "haw", None, List("Hawaiian"), List("hawaïen"), List("Hawaiisch"))
  val Hebrew = new NewLanguage("heb", "heb", Some("he"), List("Hebrew"), List("hébreu"), List("Hebräisch"))
  val Herero = new NewLanguage("her", "her", Some("hz"), List("Herero"), List("herero"), List("Herero-Sprache"))
  val Hiligaynon = new NewLanguage("hil", "hil", None, List("Hiligaynon"), List("hiligaynon"), List("Hiligaynon-Sprache"))
  val HimachaliLanguages = new NewLanguage("him", "him", None, List("Himachali languages", "Western Pahari languages"), List("langues himachalis", "langues paharis occidentales"), List("Himachali"))
  val Hindi = new NewLanguage("hin", "hin", Some("hi"), List("Hindi"), List("hindi"), List("Hindi"))
  val HiriMotu = new NewLanguage("hmo", "hmo", Some("ho"), List("Hiri Motu"), List("hiri motu"), List("Hiri-Motu"))
  val Hittite = new NewLanguage("hit", "hit", None, List("Hittite"), List("hittite"), List("Hethitisch"))
  val Hmong = new NewLanguage("hmn", "hmn", None, List("Hmong", "Mong"), List("hmong"), List("Miao-Sprachen"))
  val Hungarian = new NewLanguage("hun", "hun", Some("hu"), List("Hungarian"), List("hongrois"), List("Ungarisch"))
  val Hupa = new NewLanguage("hup", "hup", None, List("Hupa"), List("hupa"), List("Hupa-Sprache"))
  val Iban = new NewLanguage("iba", "iba", None, List("Iban"), List("iban"), List("Iban-Sprache"))
  val Icelandic = new NewLanguage("ice", "isl", Some("is"), List("Icelandic"), List("islandais"), List("Isländisch"))
  val Ido = new NewLanguage("ido", "ido", Some("io"), List("Ido"), List("ido"), List("Ido"))
  val Igbo = new NewLanguage("ibo", "ibo", Some("ig"), List("Igbo"), List("igbo"), List("Ibo-Sprache"))
  val IjoLanguages = new NewLanguage("ijo", "ijo", None, List("Ijo languages"), List("ijo, langues"), List("Ijo-Sprache"))
  val Iloko = new NewLanguage("ilo", "ilo", None, List("Iloko"), List("ilocano"), List("Ilokano-Sprache"))
  val InariSami = new NewLanguage("smn", "smn", None, List("Inari Sami"), List("sami d'Inari"), List("Inarisaamisch"))
  val IndicLanguages = new NewLanguage("inc", "inc", None, List("Indic languages"), List("indo-aryennes, langues"), List("Indoarische Sprachen (Andere)"))
  val IndoEuropeanLanguages = new NewLanguage("ine", "ine", None, List("Indo-European languages"), List("indo-européennes, langues"), List("Indogermanische Sprachen (Andere)"))
  val Indonesian = new NewLanguage("ind", "ind", Some("id"), List("Indonesian"), List("indonésien"), List("Bahasa Indonesia"))
  val Ingush = new NewLanguage("inh", "inh", None, List("Ingush"), List("ingouche"), List("Inguschisch"))
  val Interlingua = new NewLanguage("ina", "ina", Some("ia"), List("Interlingua (International Auxiliary Language Association)"), List("interlingua (langue auxiliaire internationale)"), List("Interlingua"))
  val Interlingue = new NewLanguage("ile", "ile", Some("ie"), List("Interlingue", "Occidental"), List("interlingue"), List("Interlingue"))
  val Inuktitut = new NewLanguage("iku", "iku", Some("iu"), List("Inuktitut"), List("inuktitut"), List("Inuktitut"))
  val Inupiaq = new NewLanguage("ipk", "ipk", Some("ik"), List("Inupiaq"), List("inupiaq"), List("Inupik"))
  val IranianLanguages = new NewLanguage("ira", "ira", None, List("Iranian languages"), List("iraniennes, langues"), List("Iranische Sprachen (Andere)"))
  val Irish = new NewLanguage("gle", "gle", Some("ga"), List("Irish"), List("irlandais"), List("Irisch"))
  val IrishMiddle = new NewLanguage("mga", "mga", None, List("Irish, Middle (900-1200)"), List("irlandais moyen (900-1200)"), List("Mittelirisch"))
  val IrishOld = new NewLanguage("sga", "sga", None, List("Irish, Old (to 900)"), List("irlandais ancien (jusqu'à 900)"), List("Altirisch"))
  val IroquoianLanguages = new NewLanguage("iro", "iro", None, List("Iroquoian languages"), List("iroquoises, langues"), List("Irokesische Sprachen"))
  val Italian = new NewLanguage("ita", "ita", Some("it"), List("Italian"), List("italien"), List("Italienisch"))
  val Japanese = new NewLanguage("jpn", "jpn", Some("ja"), List("Japanese"), List("japonais"), List("Japanisch"))
  val Javanese = new NewLanguage("jav", "jav", Some("jv"), List("Javanese"), List("javanais"), List("Javanisch"))
  val JudeoArabic = new NewLanguage("jrb", "jrb", None, List("Judeo-Arabic"), List("judéo-arabe"), List("Jüdisch-Arabisch"))
  val JudeoPersian = new NewLanguage("jpr", "jpr", None, List("Judeo-Persian"), List("judéo-persan"), List("Jüdisch-Persisch"))
  val Kabardian = new NewLanguage("kbd", "kbd", None, List("Kabardian"), List("kabardien"), List("Kabardinisch"))
  val Kabyle = new NewLanguage("kab", "kab", None, List("Kabyle"), List("kabyle"), List("Kabylisch"))
  val Kachin = new NewLanguage("kac", "kac", None, List("Kachin", "Jingpho"), List("kachin", "jingpho"), List("Kachin-Sprache"))
  val Kalaallisut = new NewLanguage("kal", "kal", Some("kl"), List("Kalaallisut", "Greenlandic"), List("groenlandais"), List("Grönländisch"))
  val Kalmyk = new NewLanguage("xal", "xal", None, List("Kalmyk", "Oirat"), List("kalmouk", "oïrat"), List("Kalmückisch"))
  val Kamba = new NewLanguage("kam", "kam", None, List("Kamba"), List("kamba"), List("Kamba-Sprache"))
  val Kannada = new NewLanguage("kan", "kan", Some("kn"), List("Kannada"), List("kannada"), List("Kannada"))
  val Kanuri = new NewLanguage("kau", "kau", Some("kr"), List("Kanuri"), List("kanouri"), List("Kanuri-Sprache"))
  val KaraKalpak = new NewLanguage("kaa", "kaa", None, List("Kara-Kalpak"), List("karakalpak"), List("Karakalpakisch"))
  val KarachayBalkar = new NewLanguage("krc", "krc", None, List("Karachay-Balkar"), List("karatchai balkar"), List("Karatschaiisch-Balkarisch"))
  val Karelian = new NewLanguage("krl", "krl", None, List("Karelian"), List("carélien"), List("Karelisch"))
  val KarenLanguages = new NewLanguage("kar", "kar", None, List("Karen languages"), List("karen, langues"), List("Karenisch"))
  val Kashmiri = new NewLanguage("kas", "kas", Some("ks"), List("Kashmiri"), List("kashmiri"), List("Kaschmiri"))
  val Kashubian = new NewLanguage("csb", "csb", None, List("Kashubian"), List("kachoube"), List("Kaschubisch"))
  val Kawi = new NewLanguage("kaw", "kaw", None, List("Kawi"), List("kawi"), List("Kawi"))
  val Kazakh = new NewLanguage("kaz", "kaz", Some("kk"), List("Kazakh"), List("kazakh"), List("Kasachisch"))
  val Khasi = new NewLanguage("kha", "kha", None, List("Khasi"), List("khasi"), List("Khasi-Sprache"))
  val KhoisanLanguages = new NewLanguage("khi", "khi", None, List("Khoisan languages"), List("khoïsan, langues"), List("Khoisan-Sprachen (Andere)"))
  val Khotanese = new NewLanguage("kho", "kho", None, List("Khotanese", "Sakan"), List("khotanais", "sakan"), List("Sakisch"))
  val Kikuyu = new NewLanguage("kik", "kik", Some("ki"), List("Kikuyu", "Gikuyu"), List("kikuyu"), List("Kikuyu-Sprache"))
  val Kimbundu = new NewLanguage("kmb", "kmb", None, List("Kimbundu"), List("kimbundu"), List("Kimbundu-Sprache"))
  val Kinyarwanda = new NewLanguage("kin", "kin", Some("rw"), List("Kinyarwanda"), List("rwanda"), List("Rwanda-Sprache"))
  val Kirghiz = new NewLanguage("kir", "kir", Some("ky"), List("Kirghiz", "Kyrgyz"), List("kirghiz"), List("Kirgisisch"))
  val Klingon = new NewLanguage("tlh", "tlh", None, List("Klingon", "tlhIngan-Hol"), List("klingon"), List("Klingonisch"))
  val Komi = new NewLanguage("kom", "kom", Some("kv"), List("Komi"), List("kom"), List("Komi-Sprache"))
  val Kongo = new NewLanguage("kon", "kon", Some("kg"), List("Kongo"), List("kongo"), List("Kongo-Sprache"))
  val Konkani = new NewLanguage("kok", "kok", None, List("Konkani"), List("konkani"), List("Konkani"))
  val Korean = new NewLanguage("kor", "kor", Some("ko"), List("Korean"), List("coréen"), List("Koreanisch"))
  val Kosraean = new NewLanguage("kos", "kos", None, List("Kosraean"), List("kosrae"), List("Kosraeanisch"))
  val Kpelle = new NewLanguage("kpe", "kpe", None, List("Kpelle"), List("kpellé"), List("Kpelle-Sprache"))
  val KruLanguages = new NewLanguage("kro", "kro", None, List("Kru languages"), List("krou, langues"), List("Kru-Sprachen (Andere)"))
  val Kuanyama = new NewLanguage("kua", "kua", Some("kj"), List("Kuanyama", "Kwanyama"), List("kuanyama", "kwanyama"), List("Kwanyama-Sprache"))
  val Kumyk = new NewLanguage("kum", "kum", None, List("Kumyk"), List("koumyk"), List("Kumükisch"))
  val Kurdish = new NewLanguage("kur", "kur", Some("ku"), List("Kurdish"), List("kurde"), List("Kurdisch"))
  val Kurukh = new NewLanguage("kru", "kru", None, List("Kurukh"), List("kurukh"), List("Oraon-Sprache"))
  val Kutenai = new NewLanguage("kut", "kut", None, List("Kutenai"), List("kutenai"), List("Kutenai-Sprache"))
  val Ladino = new NewLanguage("lad", "lad", None, List("Ladino"), List("judéo-espagnol"), List("Judenspanisch"))
  val Lahnda = new NewLanguage("lah", "lah", None, List("Lahnda"), List("lahnda"), List("Lahnda"))
  val Lamba = new NewLanguage("lam", "lam", None, List("Lamba"), List("lamba"), List("Lamba-Sprache (Bantusprache)"))
  val LandDayakLanguages = new NewLanguage("day", "day", None, List("Land Dayak languages"), List("dayak, langues"), List("Dajakisch"))
  val Lao = new NewLanguage("lao", "lao", Some("lo"), List("Lao"), List("lao"), List("Laotisch"))
  val Latin = new NewLanguage("lat", "lat", Some("la"), List("Latin"), List("latin"), List("Latein"))
  val Latvian = new NewLanguage("lav", "lav", Some("lv"), List("Latvian"), List("letton"), List("Lettisch"))
  val Lezghian = new NewLanguage("lez", "lez", None, List("Lezghian"), List("lezghien"), List("Lesgisch"))
  val Limburgan = new NewLanguage("lim", "lim", Some("li"), List("Limburgan", "Limburger", "Limburgish"), List("limbourgeois"), List("Limburgisch"))
  val Lingala = new NewLanguage("lin", "lin", Some("ln"), List("Lingala"), List("lingala"), List("Lingala"))
  val Lithuanian = new NewLanguage("lit", "lit", Some("lt"), List("Lithuanian"), List("lituanien"), List("Litauisch"))
  val Lojban = new NewLanguage("jbo", "jbo", None, List("Lojban"), List("lojban"), List("Lojban"))
  val LowGerman = new NewLanguage("nds", "nds", None, List("Low German", "Low Saxon", "German, Low", "Saxon, Low"), List("bas allemand", "bas saxon", "allemand, bas", "saxon, bas"), List("Niederdeutsch"))
  val LowerSorbian = new NewLanguage("dsb", "dsb", None, List("Lower Sorbian"), List("bas-sorabe"), List("Niedersorbisch"))
  val Lozi = new NewLanguage("loz", "loz", None, List("Lozi"), List("lozi"), List("Rotse-Sprache"))
  val LubaKatanga = new NewLanguage("lub", "lub", Some("lu"), List("Luba-Katanga"), List("luba-katanga"), List("Luba-Katanga-Sprache"))
  val LubaLulua = new NewLanguage("lua", "lua", None, List("Luba-Lulua"), List("luba-lulua"), List("Lulua-Sprache"))
  val Luiseno = new NewLanguage("lui", "lui", None, List("Luiseno"), List("luiseno"), List("Luiseño-Sprache"))
  val LuleSami = new NewLanguage("smj", "smj", None, List("Lule Sami"), List("sami de Lule"), List("Lulesaamisch"))
  val Lunda = new NewLanguage("lun", "lun", None, List("Lunda"), List("lunda"), List("Lunda-Sprache"))
  val Luo = new NewLanguage("luo", "luo", None, List("Luo (Kenya and Tanzania)"), List("luo (Kenya et Tanzanie)"), List("Luo-Sprache"))
  val Lushai = new NewLanguage("lus", "lus", None, List("Lushai"), List("lushai"), List("Lushai-Sprache"))
  val Luxembourgish = new NewLanguage("ltz", "ltz", Some("lb"), List("Luxembourgish", "Letzeburgesch"), List("luxembourgeois"), List("Luxemburgisch"))
  val Macedonian = new NewLanguage("mac", "mkd", Some("mk"), List("Macedonian"), List("macédonien"), List("Makedonisch"))
  val Madurese = new NewLanguage("mad", "mad", None, List("Madurese"), List("madourais"), List("Maduresisch"))
  val Magahi = new NewLanguage("mag", "mag", None, List("Magahi"), List("magahi"), List("Khotta"))
  val Maithili = new NewLanguage("mai", "mai", None, List("Maithili"), List("maithili"), List("Maithili"))
  val Makasar = new NewLanguage("mak", "mak", None, List("Makasar"), List("makassar"), List("Makassarisch"))
  val Malagasy = new NewLanguage("mlg", "mlg", Some("mg"), List("Malagasy"), List("malgache"), List("Malagassi-Sprache"))
  val Malay = new NewLanguage("may", "msa", Some("ms"), List("Malay"), List("malais"), List("Malaiisch"))
  val Malayalam = new NewLanguage("mal", "mal", Some("ml"), List("Malayalam"), List("malayalam"), List("Malayalam"))
  val Maltese = new NewLanguage("mlt", "mlt", Some("mt"), List("Maltese"), List("maltais"), List("Maltesisch"))
  val Manchu = new NewLanguage("mnc", "mnc", None, List("Manchu"), List("mandchou"), List("Mandschurisch"))
  val Mandar = new NewLanguage("mdr", "mdr", None, List("Mandar"), List("mandar"), List("Mandaresisch"))
  val Mandingo = new NewLanguage("man", "man", None, List("Mandingo"), List("mandingue"), List("Malinke-Sprache"))
  val Manipuri = new NewLanguage("mni", "mni", None, List("Manipuri"), List("manipuri"), List("Meithei-Sprache"))
  val ManoboLanguages = new NewLanguage("mno", "mno", None, List("Manobo languages"), List("manobo, langues"), List("Manobo-Sprachen"))
  val Manx = new NewLanguage("glv", "glv", Some("gv"), List("Manx"), List("manx", "mannois"), List("Manx"))
  val Maori = new NewLanguage("mao", "mri", Some("mi"), List("Maori"), List("maori"), List("Maori-Sprache"))
  val Mapudungun = new NewLanguage("arn", "arn", None, List("Mapudungun", "Mapuche"), List("mapudungun", "mapuche", "mapuce"), List("Arauka-Sprachen"))
  val Marathi = new NewLanguage("mar", "mar", Some("mr"), List("Marathi"), List("marathe"), List("Marathi"))
  val Mari = new NewLanguage("chm", "chm", None, List("Mari"), List("mari"), List("Tscheremissisch"))
  val Marshallese = new NewLanguage("mah", "mah", Some("mh"), List("Marshallese"), List("marshall"), List("Marschallesisch"))
  val Marwari = new NewLanguage("mwr", "mwr", None, List("Marwari"), List("marvari"), List("Marwari"))
  val Masai = new NewLanguage("mas", "mas", None, List("Masai"), List("massaï"), List("Massai-Sprache"))
  val MayanLanguages = new NewLanguage("myn", "myn", None, List("Mayan languages"), List("maya, langues"), List("Maya-Sprachen"))
  val Mende = new NewLanguage("men", "men", None, List("Mende"), List("mendé"), List("Mende-Sprache"))
  val MiKmaq = new NewLanguage("mic", "mic", None, List("Mi'kmaq", "Micmac"), List("mi'kmaq", "micmac"), List("Micmac-Sprache"))
  val Minangkabau = new NewLanguage("min", "min", None, List("Minangkabau"), List("minangkabau"), List("Minangkabau-Sprache"))
  val Mirandese = new NewLanguage("mwl", "mwl", None, List("Mirandese"), List("mirandais"), List("Mirandesisch"))
  val Mohawk = new NewLanguage("moh", "moh", None, List("Mohawk"), List("mohawk"), List("Mohawk-Sprache"))
  val Moksha = new NewLanguage("mdf", "mdf", None, List("Moksha"), List("moksa"), List("Mokscha-Sprache"))
  val MonKhmerLanguages = new NewLanguage("mkh", "mkh", None, List("Mon-Khmer languages"), List("môn-khmer, langues"), List("Mon-Khmer-Sprachen (Andere)"))
  val Mongo = new NewLanguage("lol", "lol", None, List("Mongo"), List("mongo"), List("Mongo-Sprache"))
  val Mongolian = new NewLanguage("mon", "mon", Some("mn"), List("Mongolian"), List("mongol"), List("Mongolisch"))
  val Mossi = new NewLanguage("mos", "mos", None, List("Mossi"), List("moré"), List("Mossi-Sprache"))
  val MultipleLanguages = new NewLanguage("mul", "mul", None, List("Multiple languages"), List("multilingue"), List("Mehrere Sprachen"))
  val MundaLanguages = new NewLanguage("mun", "mun", None, List("Munda languages"), List("mounda, langues"), List("Mundasprachen (Andere)"))
  val NKo = new NewLanguage("nqo", "nqo", None, List("N'Ko"), List("n'ko"), List("N'Ko"))
  val NahuatlLanguages = new NewLanguage("nah", "nah", None, List("Nahuatl languages"), List("nahuatl, langues"), List("Nahuatl"))
  val Nauru = new NewLanguage("nau", "nau", Some("na"), List("Nauru"), List("nauruan"), List("Nauruanisch"))
  val Navajo = new NewLanguage("nav", "nav", Some("nv"), List("Navajo", "Navaho"), List("navaho"), List("Navajo-Sprache"))
  val NdebeleNorth = new NewLanguage("nde", "nde", Some("nd"), List("Ndebele, North", "North Ndebele"), List("ndébélé du Nord"), List("Ndebele-Sprache (Simbabwe)"))
  val NdebeleSouth = new NewLanguage("nbl", "nbl", Some("nr"), List("Ndebele, South", "South Ndebele"), List("ndébélé du Sud"), List("Ndebele-Sprache (Transvaal)"))
  val Ndonga = new NewLanguage("ndo", "ndo", Some("ng"), List("Ndonga"), List("ndonga"), List("Ndonga"))
  val Neapolitan = new NewLanguage("nap", "nap", None, List("Neapolitan"), List("napolitain"), List("Neapel / Mundart"))
  val NepalBhasa = new NewLanguage("new", "new", None, List("Nepal Bhasa", "Newari"), List("nepal bhasa", "newari"), List("Newari"))
  val Nepali = new NewLanguage("nep", "nep", Some("ne"), List("Nepali"), List("népalais"), List("Nepali"))
  val Nias = new NewLanguage("nia", "nia", None, List("Nias"), List("nias"), List("Nias-Sprache"))
  val NigerKordofanianLanguages = new NewLanguage("nic", "nic", None, List("Niger-Kordofanian languages"), List("nigéro-kordofaniennes, langues"), List("Nigerkordofanische Sprachen (Andere)"))
  val NiloSaharanLanguages = new NewLanguage("ssa", "ssa", None, List("Nilo-Saharan languages"), List("nilo-sahariennes, langues"), List("Nilosaharanische Sprachen (Andere)"))
  val Niuean = new NewLanguage("niu", "niu", None, List("Niuean"), List("niué"), List("Niue-Sprache"))
  val NoLinguisticContent = new NewLanguage("zxx", "zxx", None, List("No linguistic content", "Not applicable"), List("pas de contenu linguistique", "non applicable"), List("Kein linguistischer Inhalt"))
  val Nogai = new NewLanguage("nog", "nog", None, List("Nogai"), List("nogaï", "nogay"), List("Nogaisch"))
  val NorseOld = new NewLanguage("non", "non", None, List("Norse, Old"), List("norrois, vieux"), List("Altnorwegisch"))
  val NorthAmericanIndianLanguages = new NewLanguage("nai", "nai", None, List("North American Indian languages"), List("nord-amérindiennes, langues"), List("Indianersprachen, Nordamerika (Andere)"))
  val NorthernFrisian = new NewLanguage("frr", "frr", None, List("Northern Frisian"), List("frison septentrional"), List("Nordfriesisch"))
  val NorthernSami = new NewLanguage("sme", "sme", Some("se"), List("Northern Sami"), List("sami du Nord"), List("Nordsaamisch"))
  val Norwegian = new NewLanguage("nor", "nor", Some("no"), List("Norwegian"), List("norvégien"), List("Norwegisch"))
  val NorwegianNynorsk = new NewLanguage("nno", "nno", Some("nn"), List("Norwegian Nynorsk", "Nynorsk, Norwegian"), List("norvégien nynorsk", "nynorsk, norvégien"), List("Nynorsk"))
  val NubianLanguages = new NewLanguage("nub", "nub", None, List("Nubian languages"), List("nubiennes, langues"), List("Nubische Sprachen"))
  val Nyamwezi = new NewLanguage("nym", "nym", None, List("Nyamwezi"), List("nyamwezi"), List("Nyamwezi-Sprache"))
  val Nyankole = new NewLanguage("nyn", "nyn", None, List("Nyankole"), List("nyankolé"), List("Nkole-Sprache"))
  val Nyoro = new NewLanguage("nyo", "nyo", None, List("Nyoro"), List("nyoro"), List("Nyoro-Sprache"))
  val Nzima = new NewLanguage("nzi", "nzi", None, List("Nzima"), List("nzema"), List("Nzima-Sprache"))
  val Occitan = new NewLanguage("oci", "oci", Some("oc"), List("Occitan (post 1500)"), List("occitan (après 1500)"), List("Okzitanisch"))
  val OfficialAramaic = new NewLanguage("arc", "arc", None, List("Official Aramaic (700-300 BCE)", "Imperial Aramaic (700-300 BCE)"), List("araméen d'empire (700-300 BCE)"), List("Aramäisch"))
  val Ojibwa = new NewLanguage("oji", "oji", Some("oj"), List("Ojibwa"), List("ojibwa"), List("Ojibwa-Sprache"))
  val Oriya = new NewLanguage("ori", "ori", Some("or"), List("Oriya"), List("oriya"), List("Oriya-Sprache"))
  val Oromo = new NewLanguage("orm", "orm", Some("om"), List("Oromo"), List("galla"), List("Galla-Sprache"))
  val Osage = new NewLanguage("osa", "osa", None, List("Osage"), List("osage"), List("Osage-Sprache"))
  val Ossetian = new NewLanguage("oss", "oss", Some("os"), List("Ossetian", "Ossetic"), List("ossète"), List("Ossetisch"))
  val OtomianLanguages = new NewLanguage("oto", "oto", None, List("Otomian languages"), List("otomi, langues"), List("Otomangue-Sprachen"))
  val Pahlavi = new NewLanguage("pal", "pal", None, List("Pahlavi"), List("pahlavi"), List("Mittelpersisch"))
  val Palauan = new NewLanguage("pau", "pau", None, List("Palauan"), List("palau"), List("Palau-Sprache"))
  val Pali = new NewLanguage("pli", "pli", Some("pi"), List("Pali"), List("pali"), List("Pali"))
  val Pampanga = new NewLanguage("pam", "pam", None, List("Pampanga", "Kapampangan"), List("pampangan"), List("Pampanggan-Sprache"))
  val Pangasinan = new NewLanguage("pag", "pag", None, List("Pangasinan"), List("pangasinan"), List("Pangasinan-Sprache"))
  val Panjabi = new NewLanguage("pan", "pan", Some("pa"), List("Panjabi", "Punjabi"), List("pendjabi"), List("Pandschabi-Sprache"))
  val Papiamento = new NewLanguage("pap", "pap", None, List("Papiamento"), List("papiamento"), List("Papiamento"))
  val PapuanLanguages = new NewLanguage("paa", "paa", None, List("Papuan languages"), List("papoues, langues"), List("Papuasprachen (Andere)"))
  val Pedi = new NewLanguage("nso", "nso", None, List("Pedi", "Sepedi", "Northern Sotho"), List("pedi", "sepedi", "sotho du Nord"), List("Pedi-Sprache"))
  val Persian = new NewLanguage("per", "fas", Some("fa"), List("Persian"), List("persan"), List("Persisch"))
  val PersianOld = new NewLanguage("peo", "peo", None, List("Persian, Old (ca.600-400 B.C.)"), List("perse, vieux (ca. 600-400 av. J.-C.)"), List("Altpersisch"))
  val PhilippineLanguages = new NewLanguage("phi", "phi", None, List("Philippine languages"), List("philippines, langues"), List("Philippinisch-Austronesisch (Andere)"))
  val Phoenician = new NewLanguage("phn", "phn", None, List("Phoenician"), List("phénicien"), List("Phönikisch"))
  val Pohnpeian = new NewLanguage("pon", "pon", None, List("Pohnpeian"), List("pohnpei"), List("Ponapeanisch"))
  val Polish = new NewLanguage("pol", "pol", Some("pl"), List("Polish"), List("polonais"), List("Polnisch"))
  val Portuguese = new NewLanguage("por", "por", Some("pt"), List("Portuguese"), List("portugais"), List("Portugiesisch"))
  val PrakritLanguages = new NewLanguage("pra", "pra", None, List("Prakrit languages"), List("prâkrit, langues"), List("Prakrit"))
  val ProvençalOld = new NewLanguage("pro", "pro", None, List("Provençal, Old (to 1500)", "Occitan, Old (to 1500)"), List("provençal ancien (jusqu'à 1500)", "occitan ancien (jusqu'à 1500)"), List("Altokzitanisch"))
  val Pushto = new NewLanguage("pus", "pus", Some("ps"), List("Pushto", "Pashto"), List("pachto"), List("Paschtu"))
  val Quechua = new NewLanguage("que", "que", Some("qu"), List("Quechua"), List("quechua"), List("Quechua-Sprache"))
  val Rajasthani = new NewLanguage("raj", "raj", None, List("Rajasthani"), List("rajasthani"), List("Rajasthani"))
  val Rapanui = new NewLanguage("rap", "rap", None, List("Rapanui"), List("rapanui"), List("Osterinsel-Sprache"))
  val Rarotongan = new NewLanguage("rar", "rar", None, List("Rarotongan", "Cook Islands Maori"), List("rarotonga", "maori des îles Cook"), List("Rarotonganisch"))
  val ReservedForLocalUse = new NewLanguage("qaa", "qtz", None, List("Reserved for local use"), List("réservée à l'usage local"), List("Reserviert für lokale Verwendung"))
  val RomanceLanguages = new NewLanguage("roa", "roa", None, List("Romance languages"), List("romanes, langues"), List("Romanische Sprachen (Andere)"))
  val Romanian = new NewLanguage("rum", "ron", Some("ro"), List("Romanian", "Moldavian", "Moldovan"), List("roumain", "moldave"), List("Rumänisch"))
  val Romansh = new NewLanguage("roh", "roh", Some("rm"), List("Romansh"), List("romanche"), List("Rätoromanisch"))
  val Romany = new NewLanguage("rom", "rom", None, List("Romany"), List("tsigane"), List("Romani (Sprache)"))
  val Rundi = new NewLanguage("run", "run", Some("rn"), List("Rundi"), List("rundi"), List("Rundi-Sprache"))
  val Russian = new NewLanguage("rus", "rus", Some("ru"), List("Russian"), List("russe"), List("Russisch"))
  val SalishanLanguages = new NewLanguage("sal", "sal", None, List("Salishan languages"), List("salishennes, langues"), List("Salish-Sprache"))
  val SamaritanAramaic = new NewLanguage("sam", "sam", None, List("Samaritan Aramaic"), List("samaritain"), List("Samaritanisch"))
  val SamiLanguages = new NewLanguage("smi", "smi", None, List("Sami languages"), List("sames, langues"), List("Saamisch"))
  val Samoan = new NewLanguage("smo", "smo", Some("sm"), List("Samoan"), List("samoan"), List("Samoanisch"))
  val Sandawe = new NewLanguage("sad", "sad", None, List("Sandawe"), List("sandawe"), List("Sandawe-Sprache"))
  val Sango = new NewLanguage("sag", "sag", Some("sg"), List("Sango"), List("sango"), List("Sango-Sprache"))
  val Sanskrit = new NewLanguage("san", "san", Some("sa"), List("Sanskrit"), List("sanskrit"), List("Sanskrit"))
  val Santali = new NewLanguage("sat", "sat", None, List("Santali"), List("santal"), List("Santali"))
  val Sardinian = new NewLanguage("srd", "srd", Some("sc"), List("Sardinian"), List("sarde"), List("Sardisch"))
  val Sasak = new NewLanguage("sas", "sas", None, List("Sasak"), List("sasak"), List("Sasak"))
  val Scots = new NewLanguage("sco", "sco", None, List("Scots"), List("écossais"), List("Schottisch"))
  val Selkup = new NewLanguage("sel", "sel", None, List("Selkup"), List("selkoupe"), List("Selkupisch"))
  val SemiticLanguages = new NewLanguage("sem", "sem", None, List("Semitic languages"), List("sémitiques, langues"), List("Semitische Sprachen (Andere)"))
  val Serbian = new NewLanguage("srp", "srp", Some("sr"), List("Serbian"), List("serbe"), List("Serbisch"))
  val Serer = new NewLanguage("srr", "srr", None, List("Serer"), List("sérère"), List("Serer-Sprache"))
  val Shan = new NewLanguage("shn", "shn", None, List("Shan"), List("chan"), List("Schan-Sprache"))
  val Shona = new NewLanguage("sna", "sna", Some("sn"), List("Shona"), List("shona"), List("Schona-Sprache"))
  val SichuanYi = new NewLanguage("iii", "iii", Some("ii"), List("Sichuan Yi", "Nuosu"), List("yi de Sichuan"), List("Lalo-Sprache"))
  val Sicilian = new NewLanguage("scn", "scn", None, List("Sicilian"), List("sicilien"), List("Sizilianisch"))
  val Sidamo = new NewLanguage("sid", "sid", None, List("Sidamo"), List("sidamo"), List("Sidamo-Sprache"))
  val SignLanguages = new NewLanguage("sgn", "sgn", None, List("Sign Languages"), List("langues des signes"), List("Zeichensprachen"))
  val Siksika = new NewLanguage("bla", "bla", None, List("Siksika"), List("blackfoot"), List("Blackfoot-Sprache"))
  val Sindhi = new NewLanguage("snd", "snd", Some("sd"), List("Sindhi"), List("sindhi"), List("Sindhi-Sprache"))
  val Sinhala = new NewLanguage("sin", "sin", Some("si"), List("Sinhala", "Sinhalese"), List("singhalais"), List("Singhalesisch"))
  val SinoTibetanLanguages = new NewLanguage("sit", "sit", None, List("Sino-Tibetan languages"), List("sino-tibétaines, langues"), List("Sinotibetische Sprachen (Andere)"))
  val SiouanLanguages = new NewLanguage("sio", "sio", None, List("Siouan languages"), List("sioux, langues"), List("Sioux-Sprachen (Andere)"))
  val SkoltSami = new NewLanguage("sms", "sms", None, List("Skolt Sami"), List("sami skolt"), List("Skoltsaamisch"))
  val Slave = new NewLanguage("den", "den", None, List("Slave (Athapascan)"), List("esclave (athapascan)"), List("Slave-Sprache"))
  val SlavicLanguages = new NewLanguage("sla", "sla", None, List("Slavic languages"), List("slaves, langues"), List("Slawische Sprachen (Andere)"))
  val Slovak = new NewLanguage("slo", "slk", Some("sk"), List("Slovak"), List("slovaque"), List("Slowakisch"))
  val Slovenian = new NewLanguage("slv", "slv", Some("sl"), List("Slovenian"), List("slovène"), List("Slowenisch"))
  val Sogdian = new NewLanguage("sog", "sog", None, List("Sogdian"), List("sogdien"), List("Sogdisch"))
  val Somali = new NewLanguage("som", "som", Some("so"), List("Somali"), List("somali"), List("Somali"))
  val SonghaiLanguages = new NewLanguage("son", "son", None, List("Songhai languages"), List("songhai, langues"), List("Songhai-Sprache"))
  val Soninke = new NewLanguage("snk", "snk", None, List("Soninke"), List("soninké"), List("Soninke-Sprache"))
  val SorbianLanguages = new NewLanguage("wen", "wen", None, List("Sorbian languages"), List("sorabes, langues"), List("Sorbisch (Andere)"))
  val SothoSouthern = new NewLanguage("sot", "sot", Some("st"), List("Sotho, Southern"), List("sotho du Sud"), List("Süd-Sotho-Sprache"))
  val SouthAmericanIndianLanguages = new NewLanguage("sai", "sai", None, List("South American Indian languages"), List("sud-amérindiennes, langues"), List("Indianersprachen, Südamerika (Andere)"))
  val SouthernAltai = new NewLanguage("alt", "alt", None, List("Southern Altai"), List("altai du Sud"), List("Altaisch"))
  val SouthernSami = new NewLanguage("sma", "sma", None, List("Southern Sami"), List("sami du Sud"), List("Südsaamisch"))
  val Spanish = new NewLanguage("spa", "spa", Some("es"), List("Spanish", "Castilian"), List("espagnol", "castillan"), List("Spanisch"))
  val SrananTongo = new NewLanguage("srn", "srn", None, List("Sranan Tongo"), List("sranan tongo"), List("Sranantongo"))
  val StandardMoroccanTamazight = new NewLanguage("zgh", "zgh", None, List("Standard Moroccan Tamazight"), List("amazighe standard marocain"), List(""))
  val Sukuma = new NewLanguage("suk", "suk", None, List("Sukuma"), List("sukuma"), List("Sukuma-Sprache"))
  val Sumerian = new NewLanguage("sux", "sux", None, List("Sumerian"), List("sumérien"), List("Sumerisch"))
  val Sundanese = new NewLanguage("sun", "sun", Some("su"), List("Sundanese"), List("soundanais"), List("Sundanesisch"))
  val Susu = new NewLanguage("sus", "sus", None, List("Susu"), List("soussou"), List("Susu"))
  val Swahili = new NewLanguage("swa", "swa", Some("sw"), List("Swahili"), List("swahili"), List("Swahili"))
  val Swati = new NewLanguage("ssw", "ssw", Some("ss"), List("Swati"), List("swati"), List("Swasi-Sprache"))
  val Swedish = new NewLanguage("swe", "swe", Some("sv"), List("Swedish"), List("suédois"), List("Schwedisch"))
  val SwissGerman = new NewLanguage("gsw", "gsw", None, List("Swiss German", "Alemannic", "Alsatian"), List("suisse alémanique", "alémanique", "alsacien"), List("Schweizerdeutsch"))
  val Syriac = new NewLanguage("syr", "syr", None, List("Syriac"), List("syriaque"), List("Neuostaramäisch"))
  val Tagalog = new NewLanguage("tgl", "tgl", Some("tl"), List("Tagalog"), List("tagalog"), List("Tagalog"))
  val Tahitian = new NewLanguage("tah", "tah", Some("ty"), List("Tahitian"), List("tahitien"), List("Tahitisch"))
  val TaiLanguages = new NewLanguage("tai", "tai", None, List("Tai languages"), List("tai, langues"), List("Thaisprachen (Andere)"))
  val Tajik = new NewLanguage("tgk", "tgk", Some("tg"), List("Tajik"), List("tadjik"), List("Tadschikisch"))
  val Tamashek = new NewLanguage("tmh", "tmh", None, List("Tamashek"), List("tamacheq"), List("Tamašeq"))
  val Tamil = new NewLanguage("tam", "tam", Some("ta"), List("Tamil"), List("tamoul"), List("Tamil"))
  val Tatar = new NewLanguage("tat", "tat", Some("tt"), List("Tatar"), List("tatar"), List("Tatarisch"))
  val Telugu = new NewLanguage("tel", "tel", Some("te"), List("Telugu"), List("télougou"), List("Telugu-Sprache"))
  val Tereno = new NewLanguage("ter", "ter", None, List("Tereno"), List("tereno"), List("Tereno-Sprache"))
  val Tetum = new NewLanguage("tet", "tet", None, List("Tetum"), List("tetum"), List("Tetum-Sprache"))
  val Thai = new NewLanguage("tha", "tha", Some("th"), List("Thai"), List("thaï"), List("Thailändisch"))
  val Tibetan = new NewLanguage("tib", "bod", Some("bo"), List("Tibetan"), List("tibétain"), List("Tibetisch"))
  val Tigre = new NewLanguage("tig", "tig", None, List("Tigre"), List("tigré"), List("Tigre-Sprache"))
  val Tigrinya = new NewLanguage("tir", "tir", Some("ti"), List("Tigrinya"), List("tigrigna"), List("Tigrinja-Sprache"))
  val Timne = new NewLanguage("tem", "tem", None, List("Timne"), List("temne"), List("Temne-Sprache"))
  val Tiv = new NewLanguage("tiv", "tiv", None, List("Tiv"), List("tiv"), List("Tiv-Sprache"))
  val Tlingit = new NewLanguage("tli", "tli", None, List("Tlingit"), List("tlingit"), List("Tlingit-Sprache"))
  val TokPisin = new NewLanguage("tpi", "tpi", None, List("Tok Pisin"), List("tok pisin"), List("Neumelanesisch"))
  val Tokelau = new NewLanguage("tkl", "tkl", None, List("Tokelau"), List("tokelau"), List("Tokelauanisch"))
  val Tonga = new NewLanguage("tog", "tog", None, List("Tonga (Nyasa)"), List("tonga (Nyasa)"), List("Tonga (Bantusprache, Sambia)"))
  val TongaIslands = new NewLanguage("ton", "ton", Some("to"), List("Tonga (Tonga Islands)"), List("tongan (Îles Tonga)"), List("Tongaisch"))
  val Tsimshian = new NewLanguage("tsi", "tsi", None, List("Tsimshian"), List("tsimshian"), List("Tsimshian-Sprache"))
  val Tsonga = new NewLanguage("tso", "tso", Some("ts"), List("Tsonga"), List("tsonga"), List("Tsonga-Sprache"))
  val Tswana = new NewLanguage("tsn", "tsn", Some("tn"), List("Tswana"), List("tswana"), List("Tswana-Sprache"))
  val Tumbuka = new NewLanguage("tum", "tum", None, List("Tumbuka"), List("tumbuka"), List("Tumbuka-Sprache"))
  val TupiLanguages = new NewLanguage("tup", "tup", None, List("Tupi languages"), List("tupi, langues"), List("Tupi-Sprache"))
  val Turkish = new NewLanguage("tur", "tur", Some("tr"), List("Turkish"), List("turc"), List("Türkisch"))
  val TurkishOttoman = new NewLanguage("ota", "ota", None, List("Turkish, Ottoman (1500-1928)"), List("turc ottoman (1500-1928)"), List("Osmanisch"))
  val Turkmen = new NewLanguage("tuk", "tuk", Some("tk"), List("Turkmen"), List("turkmène"), List("Turkmenisch"))
  val Tuvalu = new NewLanguage("tvl", "tvl", None, List("Tuvalu"), List("tuvalu"), List("Elliceanisch"))
  val Tuvinian = new NewLanguage("tyv", "tyv", None, List("Tuvinian"), List("touva"), List("Tuwinisch"))
  val Twi = new NewLanguage("twi", "twi", Some("tw"), List("Twi"), List("twi"), List("Twi-Sprache"))
  val Udmurt = new NewLanguage("udm", "udm", None, List("Udmurt"), List("oudmourte"), List("Udmurtisch"))
  val Ugaritic = new NewLanguage("uga", "uga", None, List("Ugaritic"), List("ougaritique"), List("Ugaritisch"))
  val Uighur = new NewLanguage("uig", "uig", Some("ug"), List("Uighur", "Uyghur"), List("ouïgour"), List("Uigurisch"))
  val Ukrainian = new NewLanguage("ukr", "ukr", Some("uk"), List("Ukrainian"), List("ukrainien"), List("Ukrainisch"))
  val Umbundu = new NewLanguage("umb", "umb", None, List("Umbundu"), List("umbundu"), List("Mbundu-Sprache"))
  val UncodedLanguages = new NewLanguage("mis", "mis", None, List("Uncoded languages"), List("langues non codées"), List("Einzelne andere Sprachen"))
  val Undetermined = new NewLanguage("und", "und", None, List("Undetermined"), List("indéterminée"), List("Nicht zu entscheiden"))
  val UpperSorbian = new NewLanguage("hsb", "hsb", None, List("Upper Sorbian"), List("haut-sorabe"), List("Obersorbisch"))
  val Urdu = new NewLanguage("urd", "urd", Some("ur"), List("Urdu"), List("ourdou"), List("Urdu"))
  val Uzbek = new NewLanguage("uzb", "uzb", Some("uz"), List("Uzbek"), List("ouszbek"), List("Usbekisch"))
  val Vai = new NewLanguage("vai", "vai", None, List("Vai"), List("vaï"), List("Vai-Sprache"))
  val Venda = new NewLanguage("ven", "ven", Some("ve"), List("Venda"), List("venda"), List("Venda-Sprache"))
  val Vietnamese = new NewLanguage("vie", "vie", Some("vi"), List("Vietnamese"), List("vietnamien"), List("Vietnamesisch"))
  val Volapük = new NewLanguage("vol", "vol", Some("vo"), List("Volapük"), List("volapük"), List("Volapük"))
  val Votic = new NewLanguage("vot", "vot", None, List("Votic"), List("vote"), List("Wotisch"))
  val WakashanLanguages = new NewLanguage("wak", "wak", None, List("Wakashan languages"), List("wakashanes, langues"), List("Wakash-Sprachen"))
  val Walloon = new NewLanguage("wln", "wln", Some("wa"), List("Walloon"), List("wallon"), List("Wallonisch"))
  val Waray = new NewLanguage("war", "war", None, List("Waray"), List("waray"), List("Waray"))
  val Washo = new NewLanguage("was", "was", None, List("Washo"), List("washo"), List("Washo-Sprache"))
  val Welsh = new NewLanguage("wel", "cym", Some("cy"), List("Welsh"), List("gallois"), List("Kymrisch"))
  val WesternFrisian = new NewLanguage("fry", "fry", Some("fy"), List("Western Frisian"), List("frison occidental"), List("Friesisch"))
  val Wolaitta = new NewLanguage("wal", "wal", None, List("Wolaitta", "Wolaytta"), List("wolaitta", "wolaytta"), List("Walamo-Sprache"))
  val Wolof = new NewLanguage("wol", "wol", Some("wo"), List("Wolof"), List("wolof"), List("Wolof-Sprache"))
  val Xhosa = new NewLanguage("xho", "xho", Some("xh"), List("Xhosa"), List("xhosa"), List("Xhosa-Sprache"))
  val Yakut = new NewLanguage("sah", "sah", None, List("Yakut"), List("iakoute"), List("Jakutisch"))
  val Yao = new NewLanguage("yao", "yao", None, List("Yao"), List("yao"), List("Yao-Sprache (Bantusprache)"))
  val Yapese = new NewLanguage("yap", "yap", None, List("Yapese"), List("yapois"), List("Yapesisch"))
  val Yiddish = new NewLanguage("yid", "yid", Some("yi"), List("Yiddish"), List("yiddish"), List("Jiddisch"))
  val Yoruba = new NewLanguage("yor", "yor", Some("yo"), List("Yoruba"), List("yoruba"), List("Yoruba-Sprache"))
  val YupikLanguages = new NewLanguage("ypk", "ypk", None, List("Yupik languages"), List("yupik, langues"), List("Ypik-Sprachen"))
  val ZandeLanguages = new NewLanguage("znd", "znd", None, List("Zande languages"), List("zandé, langues"), List("Zande-Sprachen"))
  val Zapotec = new NewLanguage("zap", "zap", None, List("Zapotec"), List("zapotèque"), List("Zapotekisch"))
  val Zaza = new NewLanguage("zza", "zza", None, List("Zaza", "Dimili", "Dimli", "Kirdki", "Kirmanjki", "Zazaki"), List("zaza", "dimili", "dimli", "kirdki", "kirmanjki", "zazaki"), List("Zazaki"))
  val Zenaga = new NewLanguage("zen", "zen", None, List("Zenaga"), List("zenaga"), List("Zenaga"))
  val Zhuang = new NewLanguage("zha", "zha", Some("za"), List("Zhuang", "Chuang"), List("zhuang", "chuang"), List("Zhuang"))
  val Zulu = new NewLanguage("zul", "zul", Some("zu"), List("Zulu"), List("zoulou"), List("Zulu-Sprache"))
  val Zuni = new NewLanguage("zun", "zun", None, List("Zuni"), List("zuni"), List("Zuñi-Sprache"))
}