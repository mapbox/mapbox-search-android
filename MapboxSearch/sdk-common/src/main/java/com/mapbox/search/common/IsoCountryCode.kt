package com.mapbox.search.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Iso Country Code.
 * @property code country code in ISO 3166 alpha 2.
 */
@Parcelize
public class IsoCountryCode(public val code: String) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IsoCountryCode

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
        public val AFGHANISTAN: IsoCountryCode = IsoCountryCode("af")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALAND_ISLANDS: IsoCountryCode = IsoCountryCode("ax")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALBANIA: IsoCountryCode = IsoCountryCode("al")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ALGERIA: IsoCountryCode = IsoCountryCode("dz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AMERICAN_SAMOA: IsoCountryCode = IsoCountryCode("as")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANDORRA: IsoCountryCode = IsoCountryCode("ad")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANGOLA: IsoCountryCode = IsoCountryCode("ao")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANGUILLA: IsoCountryCode = IsoCountryCode("ai")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANTARCTICA: IsoCountryCode = IsoCountryCode("aq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ANTIGUA_AND_BARBUDA: IsoCountryCode = IsoCountryCode("ag")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARGENTINA: IsoCountryCode = IsoCountryCode("ar")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARMENIA: IsoCountryCode = IsoCountryCode("am")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ARUBA: IsoCountryCode = IsoCountryCode("aw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AUSTRALIA: IsoCountryCode = IsoCountryCode("au")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AUSTRIA: IsoCountryCode = IsoCountryCode("at")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val AZERBAIJAN: IsoCountryCode = IsoCountryCode("az")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BAHAMAS: IsoCountryCode = IsoCountryCode("bs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BAHRAIN: IsoCountryCode = IsoCountryCode("bh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BANGLADESH: IsoCountryCode = IsoCountryCode("bd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BARBADOS: IsoCountryCode = IsoCountryCode("bb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELARUS: IsoCountryCode = IsoCountryCode("by")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELGIUM: IsoCountryCode = IsoCountryCode("be")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BELIZE: IsoCountryCode = IsoCountryCode("bz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BENIN: IsoCountryCode = IsoCountryCode("bj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BERMUDA: IsoCountryCode = IsoCountryCode("bm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BHUTAN: IsoCountryCode = IsoCountryCode("bt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOLIVIA: IsoCountryCode = IsoCountryCode("bo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOSNIA_AND_HERZEGOVINA: IsoCountryCode = IsoCountryCode("ba")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOTSWANA: IsoCountryCode = IsoCountryCode("bw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BOUVET_ISLAND: IsoCountryCode = IsoCountryCode("bv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRAZIL: IsoCountryCode = IsoCountryCode("br")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRITISH_INDIAN_OCEAN_TERRITORY: IsoCountryCode = IsoCountryCode("io")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRITISH_VIRGIN_ISLANDS: IsoCountryCode = IsoCountryCode("vg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BRUNEI: IsoCountryCode = IsoCountryCode("bn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BULGARIA: IsoCountryCode = IsoCountryCode("bg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BURKINA_FASO: IsoCountryCode = IsoCountryCode("bf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val BURUNDI: IsoCountryCode = IsoCountryCode("bi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAMBODIA: IsoCountryCode = IsoCountryCode("kh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAMEROON: IsoCountryCode = IsoCountryCode("cm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CANADA: IsoCountryCode = IsoCountryCode("ca")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAPE_VERDE: IsoCountryCode = IsoCountryCode("cv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CARIBBEAN_NETHERLANDS: IsoCountryCode = IsoCountryCode("bq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CAYMAN_ISLANDS: IsoCountryCode = IsoCountryCode("ky")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CENTRAL_AFRICAN_REPUBLIC: IsoCountryCode = IsoCountryCode("cf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHAD: IsoCountryCode = IsoCountryCode("td")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHILE: IsoCountryCode = IsoCountryCode("cl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHINA: IsoCountryCode = IsoCountryCode("cn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CHRISTMAS_ISLAND: IsoCountryCode = IsoCountryCode("cx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COCOS_ISLANDS: IsoCountryCode = IsoCountryCode("cc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COLOMBIA: IsoCountryCode = IsoCountryCode("co")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COMOROS: IsoCountryCode = IsoCountryCode("km")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CONGO_BRAZZAVILLE: IsoCountryCode = IsoCountryCode("cg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CONGO_KINSHASA: IsoCountryCode = IsoCountryCode("cd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COOK_ISLANDS: IsoCountryCode = IsoCountryCode("ck")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COSTA_RICA: IsoCountryCode = IsoCountryCode("cr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val COTE_DIVOIRE: IsoCountryCode = IsoCountryCode("ci")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CROATIA: IsoCountryCode = IsoCountryCode("hr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CUBA: IsoCountryCode = IsoCountryCode("cu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CURACAO: IsoCountryCode = IsoCountryCode("cw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CYPRUS: IsoCountryCode = IsoCountryCode("cy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val CZECHIA: IsoCountryCode = IsoCountryCode("cz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DENMARK: IsoCountryCode = IsoCountryCode("dk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DJIBOUTI: IsoCountryCode = IsoCountryCode("dj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DOMINICA: IsoCountryCode = IsoCountryCode("dm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val DOMINICAN_REPUBLIC: IsoCountryCode = IsoCountryCode("do")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ECUADOR: IsoCountryCode = IsoCountryCode("ec")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EGYPT: IsoCountryCode = IsoCountryCode("eg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EL_SALVADOR: IsoCountryCode = IsoCountryCode("sv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val EQUATORIAL_GUINEA: IsoCountryCode = IsoCountryCode("gq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ERITREA: IsoCountryCode = IsoCountryCode("er")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ESTONIA: IsoCountryCode = IsoCountryCode("ee")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ETHIOPIA: IsoCountryCode = IsoCountryCode("et")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FALKLAND_ISLANDS_: IsoCountryCode = IsoCountryCode("fk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FAROE_ISLANDS: IsoCountryCode = IsoCountryCode("fo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FIJI: IsoCountryCode = IsoCountryCode("fj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FINLAND: IsoCountryCode = IsoCountryCode("fi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRANCE: IsoCountryCode = IsoCountryCode("fr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_GUIANA: IsoCountryCode = IsoCountryCode("gf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_POLYNESIA: IsoCountryCode = IsoCountryCode("pf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val FRENCH_SOUTHERN_TERRITORIES: IsoCountryCode = IsoCountryCode("tf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GABON: IsoCountryCode = IsoCountryCode("ga")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GAMBIA: IsoCountryCode = IsoCountryCode("gm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GEORGIA: IsoCountryCode = IsoCountryCode("ge")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GERMANY: IsoCountryCode = IsoCountryCode("de")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GHANA: IsoCountryCode = IsoCountryCode("gh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GIBRALTAR: IsoCountryCode = IsoCountryCode("gi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GREECE: IsoCountryCode = IsoCountryCode("gr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GREENLAND: IsoCountryCode = IsoCountryCode("gl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GRENADA: IsoCountryCode = IsoCountryCode("gd")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUADELOUPE: IsoCountryCode = IsoCountryCode("gp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUAM: IsoCountryCode = IsoCountryCode("gu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUATEMALA: IsoCountryCode = IsoCountryCode("gt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUERNSEY: IsoCountryCode = IsoCountryCode("gg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUINEA: IsoCountryCode = IsoCountryCode("gn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUINEA_BISSAU: IsoCountryCode = IsoCountryCode("gw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val GUYANA: IsoCountryCode = IsoCountryCode("gy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HAITI: IsoCountryCode = IsoCountryCode("ht")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HEARD_AND_MCDONALD_ISLANDS: IsoCountryCode = IsoCountryCode("hm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HONDURAS: IsoCountryCode = IsoCountryCode("hn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HONG_KONG: IsoCountryCode = IsoCountryCode("hk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val HUNGARY: IsoCountryCode = IsoCountryCode("hu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ICELAND: IsoCountryCode = IsoCountryCode("is")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val INDIA: IsoCountryCode = IsoCountryCode("in")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val INDONESIA: IsoCountryCode = IsoCountryCode("id")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRAN: IsoCountryCode = IsoCountryCode("ir")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRAQ: IsoCountryCode = IsoCountryCode("iq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val IRELAND: IsoCountryCode = IsoCountryCode("ie")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ISLE_OF_MAN: IsoCountryCode = IsoCountryCode("im")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ISRAEL: IsoCountryCode = IsoCountryCode("il")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ITALY: IsoCountryCode = IsoCountryCode("it")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JAMAICA: IsoCountryCode = IsoCountryCode("jm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JAPAN: IsoCountryCode = IsoCountryCode("jp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JERSEY: IsoCountryCode = IsoCountryCode("je")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val JORDAN: IsoCountryCode = IsoCountryCode("jo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KAZAKHSTAN: IsoCountryCode = IsoCountryCode("kz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KENYA: IsoCountryCode = IsoCountryCode("ke")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KIRIBATI: IsoCountryCode = IsoCountryCode("ki")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KUWAIT: IsoCountryCode = IsoCountryCode("kw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val KYRGYZSTAN: IsoCountryCode = IsoCountryCode("kg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LAOS: IsoCountryCode = IsoCountryCode("la")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LATVIA: IsoCountryCode = IsoCountryCode("lv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LEBANON: IsoCountryCode = IsoCountryCode("lb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LESOTHO: IsoCountryCode = IsoCountryCode("ls")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIBERIA: IsoCountryCode = IsoCountryCode("lr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIBYA: IsoCountryCode = IsoCountryCode("ly")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LIECHTENSTEIN: IsoCountryCode = IsoCountryCode("li")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LITHUANIA: IsoCountryCode = IsoCountryCode("lt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val LUXEMBOURG: IsoCountryCode = IsoCountryCode("lu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MACAU: IsoCountryCode = IsoCountryCode("mo")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MACEDONIA: IsoCountryCode = IsoCountryCode("mk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MADAGASCAR: IsoCountryCode = IsoCountryCode("mg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALAWI: IsoCountryCode = IsoCountryCode("mw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALAYSIA: IsoCountryCode = IsoCountryCode("my")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALDIVES: IsoCountryCode = IsoCountryCode("mv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALI: IsoCountryCode = IsoCountryCode("ml")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MALTA: IsoCountryCode = IsoCountryCode("mt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MARSHALL_ISLANDS: IsoCountryCode = IsoCountryCode("mh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MARTINIQUE: IsoCountryCode = IsoCountryCode("mq")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAURITANIA: IsoCountryCode = IsoCountryCode("mr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAURITIUS: IsoCountryCode = IsoCountryCode("mu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MAYOTTE: IsoCountryCode = IsoCountryCode("yt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MEXICO: IsoCountryCode = IsoCountryCode("mx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MICRONESIA: IsoCountryCode = IsoCountryCode("fm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOLDOVA: IsoCountryCode = IsoCountryCode("md")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONACO: IsoCountryCode = IsoCountryCode("mc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONGOLIA: IsoCountryCode = IsoCountryCode("mn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONTENEGRO: IsoCountryCode = IsoCountryCode("me")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MONTSERRAT: IsoCountryCode = IsoCountryCode("ms")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOROCCO: IsoCountryCode = IsoCountryCode("ma")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MOZAMBIQUE: IsoCountryCode = IsoCountryCode("mz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val MYANMAR: IsoCountryCode = IsoCountryCode("mm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NAMIBIA: IsoCountryCode = IsoCountryCode("na")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NAURU: IsoCountryCode = IsoCountryCode("nr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEPAL: IsoCountryCode = IsoCountryCode("np")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NETHERLANDS: IsoCountryCode = IsoCountryCode("nl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEW_CALEDONIA: IsoCountryCode = IsoCountryCode("nc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NEW_ZEALAND: IsoCountryCode = IsoCountryCode("nz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NICARAGUA: IsoCountryCode = IsoCountryCode("ni")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIGER: IsoCountryCode = IsoCountryCode("ne")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIGERIA: IsoCountryCode = IsoCountryCode("ng")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NIUE: IsoCountryCode = IsoCountryCode("nu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORFOLK_ISLAND: IsoCountryCode = IsoCountryCode("nf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORTHERN_MARIANA_ISLANDS: IsoCountryCode = IsoCountryCode("mp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORTH_KOREA: IsoCountryCode = IsoCountryCode("kp")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val NORWAY: IsoCountryCode = IsoCountryCode("no")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val OMAN: IsoCountryCode = IsoCountryCode("om")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PAKISTAN: IsoCountryCode = IsoCountryCode("pk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PALAU: IsoCountryCode = IsoCountryCode("pw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PALESTINE: IsoCountryCode = IsoCountryCode("ps")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PANAMA: IsoCountryCode = IsoCountryCode("pa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PAPUA_NEW_GUINEA: IsoCountryCode = IsoCountryCode("pg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PARAGUAY: IsoCountryCode = IsoCountryCode("py")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PERU: IsoCountryCode = IsoCountryCode("pe")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PHILIPPINES: IsoCountryCode = IsoCountryCode("ph")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PITCAIRN_ISLANDS: IsoCountryCode = IsoCountryCode("pn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val POLAND: IsoCountryCode = IsoCountryCode("pl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PORTUGAL: IsoCountryCode = IsoCountryCode("pt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val PUERTO_RICO: IsoCountryCode = IsoCountryCode("pr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val QATAR: IsoCountryCode = IsoCountryCode("qa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val REUNION: IsoCountryCode = IsoCountryCode("re")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ROMANIA: IsoCountryCode = IsoCountryCode("ro")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val RUSSIA: IsoCountryCode = IsoCountryCode("ru")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val RWANDA: IsoCountryCode = IsoCountryCode("rw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAMOA: IsoCountryCode = IsoCountryCode("ws")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAN_MARINO: IsoCountryCode = IsoCountryCode("sm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAO_TOME_AND_PRINCIPE: IsoCountryCode = IsoCountryCode("st")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SAUDI_ARABIA: IsoCountryCode = IsoCountryCode("sa")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SENEGAL: IsoCountryCode = IsoCountryCode("sn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SERBIA: IsoCountryCode = IsoCountryCode("rs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SEYCHELLES: IsoCountryCode = IsoCountryCode("sc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SIERRA_LEONE: IsoCountryCode = IsoCountryCode("sl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SINGAPORE: IsoCountryCode = IsoCountryCode("sg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SINT_MAARTEN: IsoCountryCode = IsoCountryCode("sx")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SLOVAKIA: IsoCountryCode = IsoCountryCode("sk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SLOVENIA: IsoCountryCode = IsoCountryCode("si")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOLOMON_ISLANDS: IsoCountryCode = IsoCountryCode("sb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOMALIA: IsoCountryCode = IsoCountryCode("so")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_AFRICA: IsoCountryCode = IsoCountryCode("za")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS: IsoCountryCode = IsoCountryCode("gs")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_KOREA: IsoCountryCode = IsoCountryCode("kr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SOUTH_SUDAN: IsoCountryCode = IsoCountryCode("ss")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SPAIN: IsoCountryCode = IsoCountryCode("es")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SRI_LANKA: IsoCountryCode = IsoCountryCode("lk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_BARTHELEMY: IsoCountryCode = IsoCountryCode("bl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_HELENA: IsoCountryCode = IsoCountryCode("sh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_KITTS_AND_NEVIS: IsoCountryCode = IsoCountryCode("kn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_LUCIA: IsoCountryCode = IsoCountryCode("lc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_MARTIN: IsoCountryCode = IsoCountryCode("mf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_PIERRE_AND_MIQUELON: IsoCountryCode = IsoCountryCode("pm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ST_VINCENT_AND_GRENADINES: IsoCountryCode = IsoCountryCode("vc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SUDAN: IsoCountryCode = IsoCountryCode("sd")

        /**
         *
         */
        @JvmField
        public val SURINAME: IsoCountryCode = IsoCountryCode("sr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SVALBARD_AND_JAN_MAYEN: IsoCountryCode = IsoCountryCode("sj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWAZILAND: IsoCountryCode = IsoCountryCode("sz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWEDEN: IsoCountryCode = IsoCountryCode("se")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SWITZERLAND: IsoCountryCode = IsoCountryCode("ch")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val SYRIA: IsoCountryCode = IsoCountryCode("sy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TAIWAN: IsoCountryCode = IsoCountryCode("tw")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TAJIKISTAN: IsoCountryCode = IsoCountryCode("tj")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TANZANIA: IsoCountryCode = IsoCountryCode("tz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val THAILAND: IsoCountryCode = IsoCountryCode("th")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TIMOR_LESTE: IsoCountryCode = IsoCountryCode("tl")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TOGO: IsoCountryCode = IsoCountryCode("tg")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TOKELAU: IsoCountryCode = IsoCountryCode("tk")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TONGA: IsoCountryCode = IsoCountryCode("to")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TRINIDAD_AND_TOBAGO: IsoCountryCode = IsoCountryCode("tt")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TUNISIA: IsoCountryCode = IsoCountryCode("tn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKEY: IsoCountryCode = IsoCountryCode("tr")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKMENISTAN: IsoCountryCode = IsoCountryCode("tm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TURKS_AND_CAICOS_ISLANDS: IsoCountryCode = IsoCountryCode("tc")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val TUVALU: IsoCountryCode = IsoCountryCode("tv")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UGANDA: IsoCountryCode = IsoCountryCode("ug")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UKRAINE: IsoCountryCode = IsoCountryCode("ua")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_ARAB_EMIRATES: IsoCountryCode = IsoCountryCode("ae")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_KINGDOM: IsoCountryCode = IsoCountryCode("gb")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UNITED_STATES: IsoCountryCode = IsoCountryCode("us")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val URUGUAY: IsoCountryCode = IsoCountryCode("uy")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val US_OUTLYING_ISLANDS: IsoCountryCode = IsoCountryCode("um")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val US_VIRGIN_ISLANDS: IsoCountryCode = IsoCountryCode("vi")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val UZBEKISTAN: IsoCountryCode = IsoCountryCode("uz")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VANUATU: IsoCountryCode = IsoCountryCode("vu")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VATICAN_CITY: IsoCountryCode = IsoCountryCode("va")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VENEZUELA: IsoCountryCode = IsoCountryCode("ve")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val VIETNAM: IsoCountryCode = IsoCountryCode("vn")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val WALLIS_AND_FUTUNA: IsoCountryCode = IsoCountryCode("wf")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val WESTERN_SAHARA: IsoCountryCode = IsoCountryCode("eh")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val YEMEN: IsoCountryCode = IsoCountryCode("ye")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ZAMBIA: IsoCountryCode = IsoCountryCode("zm")

        /**
         * Predefined country constant.
         */
        @JvmField
        public val ZIMBABWE: IsoCountryCode = IsoCountryCode("zw")
    }
}
