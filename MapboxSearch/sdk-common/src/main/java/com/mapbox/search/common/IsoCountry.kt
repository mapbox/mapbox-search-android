package com.mapbox.search.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Iso Country Code.
 * @property code country code in ISO 3166 alpha 2.
 */
@Parcelize
public class IsoCountry(public val code: String) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IsoCountry

        if (code != other.code) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return code.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "Country(code='$code')"
    }

    /**
     * Companion object.
     */
    @Suppress("LargeClass")
    public companion object {

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AFGHANISTAN: IsoCountry = IsoCountry("af")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALAND_ISLANDS: IsoCountry = IsoCountry("ax")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALBANIA: IsoCountry = IsoCountry("al")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALGERIA: IsoCountry = IsoCountry("dz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AMERICAN_SAMOA: IsoCountry = IsoCountry("as")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANDORRA: IsoCountry = IsoCountry("ad")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANGOLA: IsoCountry = IsoCountry("ao")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANGUILLA: IsoCountry = IsoCountry("ai")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANTARCTICA: IsoCountry = IsoCountry("aq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANTIGUA_AND_BARBUDA: IsoCountry = IsoCountry("ag")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARGENTINA: IsoCountry = IsoCountry("ar")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARMENIA: IsoCountry = IsoCountry("am")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARUBA: IsoCountry = IsoCountry("aw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AUSTRALIA: IsoCountry = IsoCountry("au")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AUSTRIA: IsoCountry = IsoCountry("at")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AZERBAIJAN: IsoCountry = IsoCountry("az")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BAHAMAS: IsoCountry = IsoCountry("bs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BAHRAIN: IsoCountry = IsoCountry("bh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BANGLADESH: IsoCountry = IsoCountry("bd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BARBADOS: IsoCountry = IsoCountry("bb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELARUS: IsoCountry = IsoCountry("by")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELGIUM: IsoCountry = IsoCountry("be")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELIZE: IsoCountry = IsoCountry("bz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BENIN: IsoCountry = IsoCountry("bj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BERMUDA: IsoCountry = IsoCountry("bm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BHUTAN: IsoCountry = IsoCountry("bt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOLIVIA: IsoCountry = IsoCountry("bo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOSNIA_AND_HERZEGOVINA: IsoCountry = IsoCountry("ba")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOTSWANA: IsoCountry = IsoCountry("bw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOUVET_ISLAND: IsoCountry = IsoCountry("bv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRAZIL: IsoCountry = IsoCountry("br")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRITISH_INDIAN_OCEAN_TERRITORY: IsoCountry = IsoCountry("io")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRITISH_VIRGIN_ISLANDS: IsoCountry = IsoCountry("vg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRUNEI: IsoCountry = IsoCountry("bn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BULGARIA: IsoCountry = IsoCountry("bg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BURKINA_FASO: IsoCountry = IsoCountry("bf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BURUNDI: IsoCountry = IsoCountry("bi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAMBODIA: IsoCountry = IsoCountry("kh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAMEROON: IsoCountry = IsoCountry("cm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CANADA: IsoCountry = IsoCountry("ca")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAPE_VERDE: IsoCountry = IsoCountry("cv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CARIBBEAN_NETHERLANDS: IsoCountry = IsoCountry("bq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAYMAN_ISLANDS: IsoCountry = IsoCountry("ky")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CENTRAL_AFRICAN_REPUBLIC: IsoCountry = IsoCountry("cf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHAD: IsoCountry = IsoCountry("td")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHILE: IsoCountry = IsoCountry("cl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHINA: IsoCountry = IsoCountry("cn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHRISTMAS_ISLAND: IsoCountry = IsoCountry("cx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COCOS_ISLANDS: IsoCountry = IsoCountry("cc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COLOMBIA: IsoCountry = IsoCountry("co")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COMOROS: IsoCountry = IsoCountry("km")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CONGO_BRAZZAVILLE: IsoCountry = IsoCountry("cg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CONGO_KINSHASA: IsoCountry = IsoCountry("cd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COOK_ISLANDS: IsoCountry = IsoCountry("ck")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COSTA_RICA: IsoCountry = IsoCountry("cr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COTE_DIVOIRE: IsoCountry = IsoCountry("ci")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CROATIA: IsoCountry = IsoCountry("hr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CUBA: IsoCountry = IsoCountry("cu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CURACAO: IsoCountry = IsoCountry("cw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CYPRUS: IsoCountry = IsoCountry("cy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CZECHIA: IsoCountry = IsoCountry("cz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DENMARK: IsoCountry = IsoCountry("dk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DJIBOUTI: IsoCountry = IsoCountry("dj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DOMINICA: IsoCountry = IsoCountry("dm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DOMINICAN_REPUBLIC: IsoCountry = IsoCountry("do")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ECUADOR: IsoCountry = IsoCountry("ec")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EGYPT: IsoCountry = IsoCountry("eg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EL_SALVADOR: IsoCountry = IsoCountry("sv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EQUATORIAL_GUINEA: IsoCountry = IsoCountry("gq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ERITREA: IsoCountry = IsoCountry("er")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ESTONIA: IsoCountry = IsoCountry("ee")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ETHIOPIA: IsoCountry = IsoCountry("et")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FALKLAND_ISLANDS_: IsoCountry = IsoCountry("fk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FAROE_ISLANDS: IsoCountry = IsoCountry("fo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FIJI: IsoCountry = IsoCountry("fj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FINLAND: IsoCountry = IsoCountry("fi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRANCE: IsoCountry = IsoCountry("fr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_GUIANA: IsoCountry = IsoCountry("gf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_POLYNESIA: IsoCountry = IsoCountry("pf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_SOUTHERN_TERRITORIES: IsoCountry = IsoCountry("tf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GABON: IsoCountry = IsoCountry("ga")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GAMBIA: IsoCountry = IsoCountry("gm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GEORGIA: IsoCountry = IsoCountry("ge")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GERMANY: IsoCountry = IsoCountry("de")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GHANA: IsoCountry = IsoCountry("gh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GIBRALTAR: IsoCountry = IsoCountry("gi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GREECE: IsoCountry = IsoCountry("gr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GREENLAND: IsoCountry = IsoCountry("gl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GRENADA: IsoCountry = IsoCountry("gd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUADELOUPE: IsoCountry = IsoCountry("gp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUAM: IsoCountry = IsoCountry("gu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUATEMALA: IsoCountry = IsoCountry("gt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUERNSEY: IsoCountry = IsoCountry("gg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUINEA: IsoCountry = IsoCountry("gn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUINEA_BISSAU: IsoCountry = IsoCountry("gw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUYANA: IsoCountry = IsoCountry("gy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HAITI: IsoCountry = IsoCountry("ht")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HEARD_AND_MCDONALD_ISLANDS: IsoCountry = IsoCountry("hm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HONDURAS: IsoCountry = IsoCountry("hn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HONG_KONG: IsoCountry = IsoCountry("hk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HUNGARY: IsoCountry = IsoCountry("hu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ICELAND: IsoCountry = IsoCountry("is")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val INDIA: IsoCountry = IsoCountry("in")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val INDONESIA: IsoCountry = IsoCountry("id")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRAN: IsoCountry = IsoCountry("ir")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRAQ: IsoCountry = IsoCountry("iq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRELAND: IsoCountry = IsoCountry("ie")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ISLE_OF_MAN: IsoCountry = IsoCountry("im")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ISRAEL: IsoCountry = IsoCountry("il")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ITALY: IsoCountry = IsoCountry("it")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JAMAICA: IsoCountry = IsoCountry("jm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JAPAN: IsoCountry = IsoCountry("jp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JERSEY: IsoCountry = IsoCountry("je")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JORDAN: IsoCountry = IsoCountry("jo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KAZAKHSTAN: IsoCountry = IsoCountry("kz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KENYA: IsoCountry = IsoCountry("ke")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KIRIBATI: IsoCountry = IsoCountry("ki")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KUWAIT: IsoCountry = IsoCountry("kw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KYRGYZSTAN: IsoCountry = IsoCountry("kg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LAOS: IsoCountry = IsoCountry("la")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LATVIA: IsoCountry = IsoCountry("lv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LEBANON: IsoCountry = IsoCountry("lb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LESOTHO: IsoCountry = IsoCountry("ls")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIBERIA: IsoCountry = IsoCountry("lr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIBYA: IsoCountry = IsoCountry("ly")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIECHTENSTEIN: IsoCountry = IsoCountry("li")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LITHUANIA: IsoCountry = IsoCountry("lt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LUXEMBOURG: IsoCountry = IsoCountry("lu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MACAU: IsoCountry = IsoCountry("mo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MACEDONIA: IsoCountry = IsoCountry("mk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MADAGASCAR: IsoCountry = IsoCountry("mg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALAWI: IsoCountry = IsoCountry("mw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALAYSIA: IsoCountry = IsoCountry("my")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALDIVES: IsoCountry = IsoCountry("mv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALI: IsoCountry = IsoCountry("ml")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALTA: IsoCountry = IsoCountry("mt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MARSHALL_ISLANDS: IsoCountry = IsoCountry("mh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MARTINIQUE: IsoCountry = IsoCountry("mq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAURITANIA: IsoCountry = IsoCountry("mr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAURITIUS: IsoCountry = IsoCountry("mu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAYOTTE: IsoCountry = IsoCountry("yt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MEXICO: IsoCountry = IsoCountry("mx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MICRONESIA: IsoCountry = IsoCountry("fm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOLDOVA: IsoCountry = IsoCountry("md")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONACO: IsoCountry = IsoCountry("mc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONGOLIA: IsoCountry = IsoCountry("mn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONTENEGRO: IsoCountry = IsoCountry("me")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONTSERRAT: IsoCountry = IsoCountry("ms")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOROCCO: IsoCountry = IsoCountry("ma")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOZAMBIQUE: IsoCountry = IsoCountry("mz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MYANMAR: IsoCountry = IsoCountry("mm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NAMIBIA: IsoCountry = IsoCountry("na")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NAURU: IsoCountry = IsoCountry("nr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEPAL: IsoCountry = IsoCountry("np")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NETHERLANDS: IsoCountry = IsoCountry("nl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEW_CALEDONIA: IsoCountry = IsoCountry("nc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEW_ZEALAND: IsoCountry = IsoCountry("nz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NICARAGUA: IsoCountry = IsoCountry("ni")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIGER: IsoCountry = IsoCountry("ne")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIGERIA: IsoCountry = IsoCountry("ng")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIUE: IsoCountry = IsoCountry("nu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORFOLK_ISLAND: IsoCountry = IsoCountry("nf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORTHERN_MARIANA_ISLANDS: IsoCountry = IsoCountry("mp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORTH_KOREA: IsoCountry = IsoCountry("kp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORWAY: IsoCountry = IsoCountry("no")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val OMAN: IsoCountry = IsoCountry("om")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PAKISTAN: IsoCountry = IsoCountry("pk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PALAU: IsoCountry = IsoCountry("pw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PALESTINE: IsoCountry = IsoCountry("ps")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PANAMA: IsoCountry = IsoCountry("pa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PAPUA_NEW_GUINEA: IsoCountry = IsoCountry("pg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PARAGUAY: IsoCountry = IsoCountry("py")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PERU: IsoCountry = IsoCountry("pe")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PHILIPPINES: IsoCountry = IsoCountry("ph")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PITCAIRN_ISLANDS: IsoCountry = IsoCountry("pn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val POLAND: IsoCountry = IsoCountry("pl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PORTUGAL: IsoCountry = IsoCountry("pt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PUERTO_RICO: IsoCountry = IsoCountry("pr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val QATAR: IsoCountry = IsoCountry("qa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val REUNION: IsoCountry = IsoCountry("re")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ROMANIA: IsoCountry = IsoCountry("ro")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val RUSSIA: IsoCountry = IsoCountry("ru")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val RWANDA: IsoCountry = IsoCountry("rw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAMOA: IsoCountry = IsoCountry("ws")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAN_MARINO: IsoCountry = IsoCountry("sm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAO_TOME_AND_PRINCIPE: IsoCountry = IsoCountry("st")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAUDI_ARABIA: IsoCountry = IsoCountry("sa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SENEGAL: IsoCountry = IsoCountry("sn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SERBIA: IsoCountry = IsoCountry("rs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SEYCHELLES: IsoCountry = IsoCountry("sc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SIERRA_LEONE: IsoCountry = IsoCountry("sl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SINGAPORE: IsoCountry = IsoCountry("sg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SINT_MAARTEN: IsoCountry = IsoCountry("sx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SLOVAKIA: IsoCountry = IsoCountry("sk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SLOVENIA: IsoCountry = IsoCountry("si")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOLOMON_ISLANDS: IsoCountry = IsoCountry("sb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOMALIA: IsoCountry = IsoCountry("so")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_AFRICA: IsoCountry = IsoCountry("za")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS: IsoCountry = IsoCountry("gs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_KOREA: IsoCountry = IsoCountry("kr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_SUDAN: IsoCountry = IsoCountry("ss")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SPAIN: IsoCountry = IsoCountry("es")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SRI_LANKA: IsoCountry = IsoCountry("lk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_BARTHELEMY: IsoCountry = IsoCountry("bl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_HELENA: IsoCountry = IsoCountry("sh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_KITTS_AND_NEVIS: IsoCountry = IsoCountry("kn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_LUCIA: IsoCountry = IsoCountry("lc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_MARTIN: IsoCountry = IsoCountry("mf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_PIERRE_AND_MIQUELON: IsoCountry = IsoCountry("pm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_VINCENT_AND_GRENADINES: IsoCountry = IsoCountry("vc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SUDAN: IsoCountry = IsoCountry("sd")

        /**
         *
         */
        @JvmField
        public val SURINAME: IsoCountry = IsoCountry("sr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SVALBARD_AND_JAN_MAYEN: IsoCountry = IsoCountry("sj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWAZILAND: IsoCountry = IsoCountry("sz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWEDEN: IsoCountry = IsoCountry("se")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWITZERLAND: IsoCountry = IsoCountry("ch")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SYRIA: IsoCountry = IsoCountry("sy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TAIWAN: IsoCountry = IsoCountry("tw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TAJIKISTAN: IsoCountry = IsoCountry("tj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TANZANIA: IsoCountry = IsoCountry("tz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val THAILAND: IsoCountry = IsoCountry("th")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TIMOR_LESTE: IsoCountry = IsoCountry("tl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TOGO: IsoCountry = IsoCountry("tg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TOKELAU: IsoCountry = IsoCountry("tk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TONGA: IsoCountry = IsoCountry("to")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TRINIDAD_AND_TOBAGO: IsoCountry = IsoCountry("tt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TUNISIA: IsoCountry = IsoCountry("tn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKEY: IsoCountry = IsoCountry("tr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKMENISTAN: IsoCountry = IsoCountry("tm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKS_AND_CAICOS_ISLANDS: IsoCountry = IsoCountry("tc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TUVALU: IsoCountry = IsoCountry("tv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UGANDA: IsoCountry = IsoCountry("ug")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UKRAINE: IsoCountry = IsoCountry("ua")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_ARAB_EMIRATES: IsoCountry = IsoCountry("ae")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_KINGDOM: IsoCountry = IsoCountry("gb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_STATES: IsoCountry = IsoCountry("us")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val URUGUAY: IsoCountry = IsoCountry("uy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val US_OUTLYING_ISLANDS: IsoCountry = IsoCountry("um")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val US_VIRGIN_ISLANDS: IsoCountry = IsoCountry("vi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UZBEKISTAN: IsoCountry = IsoCountry("uz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VANUATU: IsoCountry = IsoCountry("vu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VATICAN_CITY: IsoCountry = IsoCountry("va")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VENEZUELA: IsoCountry = IsoCountry("ve")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VIETNAM: IsoCountry = IsoCountry("vn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val WALLIS_AND_FUTUNA: IsoCountry = IsoCountry("wf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val WESTERN_SAHARA: IsoCountry = IsoCountry("eh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val YEMEN: IsoCountry = IsoCountry("ye")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ZAMBIA: IsoCountry = IsoCountry("zm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ZIMBABWE: IsoCountry = IsoCountry("zw")
    }
}
