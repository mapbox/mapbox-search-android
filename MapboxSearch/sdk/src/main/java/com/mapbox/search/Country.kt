package com.mapbox.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Countries list to use in [com.mapbox.search.SearchOptions].
 * @property code country code in ISO 3166 alpha 2.
 */
@Parcelize
public class Country(public val code: String) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Country

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
     * @suppress
     */
    @Suppress("LargeClass")
    public companion object {

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AFGHANISTAN: Country = Country("af")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALAND_ISLANDS: Country = Country("ax")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALBANIA: Country = Country("al")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALGERIA: Country = Country("dz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AMERICAN_SAMOA: Country = Country("as")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANDORRA: Country = Country("ad")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANGOLA: Country = Country("ao")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANGUILLA: Country = Country("ai")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANTARCTICA: Country = Country("aq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANTIGUA_AND_BARBUDA: Country = Country("ag")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARGENTINA: Country = Country("ar")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARMENIA: Country = Country("am")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARUBA: Country = Country("aw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AUSTRALIA: Country = Country("au")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AUSTRIA: Country = Country("at")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AZERBAIJAN: Country = Country("az")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BAHAMAS: Country = Country("bs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BAHRAIN: Country = Country("bh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BANGLADESH: Country = Country("bd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BARBADOS: Country = Country("bb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELARUS: Country = Country("by")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELGIUM: Country = Country("be")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELIZE: Country = Country("bz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BENIN: Country = Country("bj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BERMUDA: Country = Country("bm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BHUTAN: Country = Country("bt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOLIVIA: Country = Country("bo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOSNIA_AND_HERZEGOVINA: Country = Country("ba")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOTSWANA: Country = Country("bw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOUVET_ISLAND: Country = Country("bv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRAZIL: Country = Country("br")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRITISH_INDIAN_OCEAN_TERRITORY: Country = Country("io")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRITISH_VIRGIN_ISLANDS: Country = Country("vg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRUNEI: Country = Country("bn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BULGARIA: Country = Country("bg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BURKINA_FASO: Country = Country("bf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BURUNDI: Country = Country("bi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAMBODIA: Country = Country("kh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAMEROON: Country = Country("cm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CANADA: Country = Country("ca")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAPE_VERDE: Country = Country("cv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CARIBBEAN_NETHERLANDS: Country = Country("bq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAYMAN_ISLANDS: Country = Country("ky")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CENTRAL_AFRICAN_REPUBLIC: Country = Country("cf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHAD: Country = Country("td")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHILE: Country = Country("cl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHINA: Country = Country("cn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHRISTMAS_ISLAND: Country = Country("cx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COCOS_ISLANDS: Country = Country("cc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COLOMBIA: Country = Country("co")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COMOROS: Country = Country("km")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CONGO_BRAZZAVILLE: Country = Country("cg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CONGO_KINSHASA: Country = Country("cd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COOK_ISLANDS: Country = Country("ck")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COSTA_RICA: Country = Country("cr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COTE_DIVOIRE: Country = Country("ci")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CROATIA: Country = Country("hr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CUBA: Country = Country("cu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CURACAO: Country = Country("cw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CYPRUS: Country = Country("cy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CZECHIA: Country = Country("cz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DENMARK: Country = Country("dk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DJIBOUTI: Country = Country("dj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DOMINICA: Country = Country("dm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DOMINICAN_REPUBLIC: Country = Country("do")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ECUADOR: Country = Country("ec")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EGYPT: Country = Country("eg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EL_SALVADOR: Country = Country("sv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EQUATORIAL_GUINEA: Country = Country("gq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ERITREA: Country = Country("er")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ESTONIA: Country = Country("ee")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ETHIOPIA: Country = Country("et")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FALKLAND_ISLANDS_: Country = Country("fk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FAROE_ISLANDS: Country = Country("fo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FIJI: Country = Country("fj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FINLAND: Country = Country("fi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRANCE: Country = Country("fr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_GUIANA: Country = Country("gf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_POLYNESIA: Country = Country("pf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_SOUTHERN_TERRITORIES: Country = Country("tf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GABON: Country = Country("ga")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GAMBIA: Country = Country("gm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GEORGIA: Country = Country("ge")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GERMANY: Country = Country("de")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GHANA: Country = Country("gh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GIBRALTAR: Country = Country("gi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GREECE: Country = Country("gr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GREENLAND: Country = Country("gl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GRENADA: Country = Country("gd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUADELOUPE: Country = Country("gp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUAM: Country = Country("gu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUATEMALA: Country = Country("gt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUERNSEY: Country = Country("gg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUINEA: Country = Country("gn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUINEA_BISSAU: Country = Country("gw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUYANA: Country = Country("gy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HAITI: Country = Country("ht")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HEARD_AND_MCDONALD_ISLANDS: Country = Country("hm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HONDURAS: Country = Country("hn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HONG_KONG: Country = Country("hk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HUNGARY: Country = Country("hu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ICELAND: Country = Country("is")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val INDIA: Country = Country("in")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val INDONESIA: Country = Country("id")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRAN: Country = Country("ir")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRAQ: Country = Country("iq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRELAND: Country = Country("ie")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ISLE_OF_MAN: Country = Country("im")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ISRAEL: Country = Country("il")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ITALY: Country = Country("it")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JAMAICA: Country = Country("jm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JAPAN: Country = Country("jp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JERSEY: Country = Country("je")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JORDAN: Country = Country("jo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KAZAKHSTAN: Country = Country("kz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KENYA: Country = Country("ke")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KIRIBATI: Country = Country("ki")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KUWAIT: Country = Country("kw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KYRGYZSTAN: Country = Country("kg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LAOS: Country = Country("la")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LATVIA: Country = Country("lv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LEBANON: Country = Country("lb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LESOTHO: Country = Country("ls")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIBERIA: Country = Country("lr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIBYA: Country = Country("ly")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIECHTENSTEIN: Country = Country("li")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LITHUANIA: Country = Country("lt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LUXEMBOURG: Country = Country("lu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MACAU: Country = Country("mo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MACEDONIA: Country = Country("mk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MADAGASCAR: Country = Country("mg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALAWI: Country = Country("mw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALAYSIA: Country = Country("my")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALDIVES: Country = Country("mv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALI: Country = Country("ml")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALTA: Country = Country("mt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MARSHALL_ISLANDS: Country = Country("mh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MARTINIQUE: Country = Country("mq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAURITANIA: Country = Country("mr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAURITIUS: Country = Country("mu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAYOTTE: Country = Country("yt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MEXICO: Country = Country("mx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MICRONESIA: Country = Country("fm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOLDOVA: Country = Country("md")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONACO: Country = Country("mc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONGOLIA: Country = Country("mn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONTENEGRO: Country = Country("me")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONTSERRAT: Country = Country("ms")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOROCCO: Country = Country("ma")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOZAMBIQUE: Country = Country("mz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MYANMAR: Country = Country("mm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NAMIBIA: Country = Country("na")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NAURU: Country = Country("nr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEPAL: Country = Country("np")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NETHERLANDS: Country = Country("nl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEW_CALEDONIA: Country = Country("nc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEW_ZEALAND: Country = Country("nz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NICARAGUA: Country = Country("ni")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIGER: Country = Country("ne")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIGERIA: Country = Country("ng")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIUE: Country = Country("nu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORFOLK_ISLAND: Country = Country("nf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORTHERN_MARIANA_ISLANDS: Country = Country("mp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORTH_KOREA: Country = Country("kp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORWAY: Country = Country("no")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val OMAN: Country = Country("om")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PAKISTAN: Country = Country("pk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PALAU: Country = Country("pw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PALESTINE: Country = Country("ps")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PANAMA: Country = Country("pa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PAPUA_NEW_GUINEA: Country = Country("pg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PARAGUAY: Country = Country("py")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PERU: Country = Country("pe")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PHILIPPINES: Country = Country("ph")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PITCAIRN_ISLANDS: Country = Country("pn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val POLAND: Country = Country("pl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PORTUGAL: Country = Country("pt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PUERTO_RICO: Country = Country("pr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val QATAR: Country = Country("qa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val REUNION: Country = Country("re")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ROMANIA: Country = Country("ro")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val RUSSIA: Country = Country("ru")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val RWANDA: Country = Country("rw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAMOA: Country = Country("ws")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAN_MARINO: Country = Country("sm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAO_TOME_AND_PRINCIPE: Country = Country("st")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAUDI_ARABIA: Country = Country("sa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SENEGAL: Country = Country("sn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SERBIA: Country = Country("rs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SEYCHELLES: Country = Country("sc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SIERRA_LEONE: Country = Country("sl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SINGAPORE: Country = Country("sg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SINT_MAARTEN: Country = Country("sx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SLOVAKIA: Country = Country("sk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SLOVENIA: Country = Country("si")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOLOMON_ISLANDS: Country = Country("sb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOMALIA: Country = Country("so")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_AFRICA: Country = Country("za")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS: Country = Country("gs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_KOREA: Country = Country("kr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_SUDAN: Country = Country("ss")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SPAIN: Country = Country("es")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SRI_LANKA: Country = Country("lk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_BARTHELEMY: Country = Country("bl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_HELENA: Country = Country("sh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_KITTS_AND_NEVIS: Country = Country("kn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_LUCIA: Country = Country("lc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_MARTIN: Country = Country("mf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_PIERRE_AND_MIQUELON: Country = Country("pm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_VINCENT_AND_GRENADINES: Country = Country("vc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SUDAN: Country = Country("sd")

        /**
         *
         */
        @JvmField
        public val SURINAME: Country = Country("sr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SVALBARD_AND_JAN_MAYEN: Country = Country("sj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWAZILAND: Country = Country("sz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWEDEN: Country = Country("se")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWITZERLAND: Country = Country("ch")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SYRIA: Country = Country("sy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TAIWAN: Country = Country("tw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TAJIKISTAN: Country = Country("tj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TANZANIA: Country = Country("tz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val THAILAND: Country = Country("th")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TIMOR_LESTE: Country = Country("tl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TOGO: Country = Country("tg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TOKELAU: Country = Country("tk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TONGA: Country = Country("to")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TRINIDAD_AND_TOBAGO: Country = Country("tt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TUNISIA: Country = Country("tn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKEY: Country = Country("tr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKMENISTAN: Country = Country("tm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKS_AND_CAICOS_ISLANDS: Country = Country("tc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TUVALU: Country = Country("tv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UGANDA: Country = Country("ug")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UKRAINE: Country = Country("ua")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_ARAB_EMIRATES: Country = Country("ae")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_KINGDOM: Country = Country("gb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_STATES: Country = Country("us")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val URUGUAY: Country = Country("uy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val US_OUTLYING_ISLANDS: Country = Country("um")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val US_VIRGIN_ISLANDS: Country = Country("vi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UZBEKISTAN: Country = Country("uz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VANUATU: Country = Country("vu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VATICAN_CITY: Country = Country("va")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VENEZUELA: Country = Country("ve")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VIETNAM: Country = Country("vn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val WALLIS_AND_FUTUNA: Country = Country("wf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val WESTERN_SAHARA: Country = Country("eh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val YEMEN: Country = Country("ye")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ZAMBIA: Country = Country("zm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ZIMBABWE: Country = Country("zw")
    }
}
