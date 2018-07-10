package com.webcommander.admin

import com.webcommander.events.AppEventManager

class Country {

    Long id
    String code
    String name
    Boolean isActive = true
    Boolean isDefault = false

    Collection state = []

    static hasMany = [state : State]

    static mapping = {
        state sort: "id", order: "asc"
        state cache: true
    }

    static constraints = {
        name(blank: false, size: 1..100, unique: true)
        code(blank: false, size: 2..5, unique: true)
    }

    public static void initialize() {
        def insertSql = [
                ['Afghanistan', 'AF'],
                ['Albania', 'AL'],
                ['Algeria', 'DZ'],
                ['American Samoa', 'AS'],
                ['Andorra', 'AD'],
                ['Angola', 'AO'],
                ['Anguilla', 'AI'],
                ['Antarctica', 'AQ'],
                ['Antigua & Barbuda', 'AG'],
                ['Argentina', 'AR'],
                ['Armenia', 'AM'],
                ['Australia', 'AU'],
                ['Aruba', 'AW'],
                ['Austria', 'AT'],
                ['Azerbaijan', 'AZ'],
                ['Bahamas, The', 'BS'],
                ['Bahrain', 'BH'],
                ['Bangladesh', 'BD'],
                ['Barbados', 'BB'],
                ['Belarus', 'BY'],
                ['Belgium', 'BE'],
                ['Belize', 'BZ'],
                ['Benin', 'BJ'],
                ['Bermuda', 'BM'],
                ['Bhutan', 'BT'],
                ['Bolivia', 'BO'],
                ['Bosnia and Herzegovina', 'BA'],
                ['Botswana', 'BW'],
                ['Bouvet Island', 'BV'],
                ['Brazil', 'BR'],
                ['British Indian Ocean T.', 'IO'],
                ['Brunei Darussalam', 'BN'],
                ['Bulgaria', 'BG'],
                ['Burkina Faso', 'BF'],
                ['Burundi', 'BI'],
                ['Cambodia', 'KH'],
                ['Cameroon', 'CM'],
                ['Canada', 'CA'],
                ['Cape Verde', 'CV'],
                ['Cayman Islands', 'KY'],
                ['Central African Republic', 'CF'],
                ['Chad', 'TD'],
                ['Chile', 'CL'],
                ['China', 'CN'],
                ['Christmas Island', 'CX'],
                ['Cocos (Keeling) Islands', 'CC'],
                ['Colombia', 'CO'],
                ['Comoros', 'KM'],
                ['Congo', 'CG'],
                ['Congo, Dem. Rep. of the', 'CD'],
                ['Cook Islands', 'CK'],
                ['Costa Rica', 'CR'],
                ['Cote D\'Ivoire', 'CI'],
                ['Croatia', 'HR'],
                ['Cuba', 'CU'],
                ['Cyprus', 'CY'],
                ['Czech Republic', 'CZ'],
                ['Denmark', 'DK'],
                ['Djibouti', 'DJ'],
                ['Dominica', 'DM'],
                ['Dominican Republic', 'DO'],
                ['East Timor (Timor-Leste)', 'TP'],
                ['Ecuador', 'EC'],
                ['Egypt', 'EG'],
                ['El Salvador', 'SV'],
                ['Equatorial Guinea', 'GQ'],
                ['Eritrea', 'ER'],
                ['Estonia', 'EE'],
                ['Ethiopia', 'ET'],
                ['European Union', 'EU'],
                ['Falkland Islands (Malvinas)', 'FK'],
                ['Faroe Islands', 'FO'],
                ['Fiji', 'FJ'],
                ['Finland', 'FI'],
                ['France', 'FR'],
                ['French Guiana', 'GF'],
                ['French Polynesia', 'PF'],
                ['French Southern Terr.', 'TF'],
                ['Gabon', 'GA'],
                ['Gambia, the', 'GM'],
                ['Georgia', 'GE'],
                ['Germany', 'DE'],
                ['Ghana', 'GH'],
                ['Gibraltar', 'GI'],
                ['Greece', 'GR'],
                ['Greenland', 'GL'],
                ['Grenada', 'GD'],
                ['Guadeloupe', 'GP'],
                ['Guam', 'GU'],
                ['Guatemala', 'GT'],
                ['Guernsey and Alderney', 'GG'],
                ['Guinea', 'GN'],
                ['Guinea-Bissau', 'GW'],
                ['Guyana', 'GY'],
                ['Haiti', 'HT'],
                ['Heard & McDonald Is.(AU)', 'HM'],
                ['Holy See (Vatican)', 'VA'],
                ['Honduras', 'HN'],
                ['Hong Kong, (China)', 'HK'],
                ['Hungary', 'HU'],
                ['Iceland', 'IS'],
                ['India', 'IN'],
                ['Indonesia', 'ID'],
                ['Iran, Islamic Republic of', 'IR'],
                ['Iraq', 'IQ'],
                ['Ireland', 'IE'],
                ['Israel', 'IL'],
                ['Italy', 'IT'],
                ['Jamaica', 'JM'],
                ['Japan', 'JP'],
                ['Jersey', 'JE'],
                ['Jordan', 'JO'],
                ['Kazakhstan', 'KZ'],
                ['Kenya', 'KE'],
                ['Kiribati', 'KI'],
                ['Korea Dem. People\'s Rep.', 'KP'],
                ['Korea, (South) Republic of', 'KR'],
                ['Kosovo', 'KV'],
                ['Kuwait', 'KW'],
                ['Kyrgyzstan', 'KG'],
                ['Lao People\'s Democ. Rep.', 'LA'],
                ['Latvia', 'LV'],
                ['Lebanon', 'LB'],
                ['Lesotho', 'LS'],
                ['Liberia', 'LR'],
                ['Libyan Arab Jamahiriya', 'LY'],
                ['Liechtenstein', 'LI'],
                ['Lithuania', 'LT'],
                ['Luxembourg', 'LU'],
                ['Macao, (China)', 'MO'],
                ['Macedonia, TFYR', 'MK'],
                ['Madagascar', 'MG'],
                ['Malawi', 'MW'],
                ['Malaysia', 'MY'],
                ['Maldives', 'MV'],
                ['Mali', 'ML'],
                ['Malta', 'MT'],
                ['Man, Isle of', 'IM'],
                ['Marshall Islands', 'MH'],
                ['Martinique (FR)', 'MQ'],
                ['Mauritania', 'MR'],
                ['Mauritius', 'MU'],
                ['Mayotte (FR)', 'YT'],
                ['Mexico', 'MX'],
                ['Micronesia, Fed. States of', 'FM'],
                ['Moldova, Republic of', 'MD'],
                ['Monaco', 'MC'],
                ['Mongolia', 'MN'],
                ['Montenegro', 'CS'],
                ['Montserrat', 'MS'],
                ['Morocco', 'MA'],
                ['Mozambique', 'MZ'],
                ['Myanmar (ex-Burma)', 'MM'],
                ['Namibia', 'NA'],
                ['Nauru', 'NR'],
                ['Nepal', 'NP'],
                ['Netherlands', 'NL'],
                ['Netherlands Antilles', 'AN'],
                ['New Caledonia', 'NC'],
                ['New Zealand', 'NZ'],
                ['Nicaragua', 'NI'],
                ['Niger', 'NE'],
                ['Nigeria', 'NG'],
                ['Niue', 'NU'],
                ['Norfolk Island', 'NF'],
                ['Northern Mariana Islands', 'MP'],
                ['Norway', 'NO'],
                ['Oman', 'OM'],
                ['Pakistan', 'PK'],
                ['Palau', 'PW'],
                ['Palestinian Territory', 'PS'],
                ['Panama', 'PA'],
                ['Papua New Guinea', 'PG'],
                ['Paraguay', 'PY'],
                ['Peru', 'PE'],
                ['Philippines', 'PH'],
                ['Pitcairn Island', 'PN'],
                ['Poland', 'PL'],
                ['Portugal', 'PT'],
                ['Puerto Rico', 'PR'],
                ['Qatar', 'QA'],
                ['Reunion (FR)', 'RE'],
                ['Romania', 'RO'],
                ['Russia (Russian Fed.)', 'RU'],
                ['Rwanda', 'RW'],
                ['Saint Barthelemy (FR)', 'BL'],
                ['Saint Helena (UK)', 'SH'],
                ['Saint Kitts and Nevis', 'KN'],
                ['Saint Lucia', 'LC'],
                ['Saint Martin (FR)', 'MF'],
                ['S Pierre & Miquelon(FR)', 'PM'],
                ['S Vincent & Grenadines', 'VC'],
                ['Samoa', 'WS'],
                ['San Marino', 'SM'],
                ['Sao Tome and Principe', 'ST'],
                ['Saudi Arabia', 'SA'],
                ['Senegal', 'SN'],
                ['Serbia', 'RS'],
                ['Seychelles', 'SC'],
                ['Sierra Leone', 'SL'],
                ['Singapore', 'SG'],
                ['Slovakia', 'SK'],
                ['Slovenia', 'SI'],
                ['Solomon Islands', 'SB'],
                ['Somalia', 'SO'],
                ['South Africa', 'ZA'],
                ['S.George & S.Sandwich', 'GS'],
                ['South Sudan', 'SS'],
                ['Spain', 'ES'],
                ['Sri Lanka (ex-Ceilan)', 'LK'],
                ['Sudan', 'SD'],
                ['Suriname', 'SR'],
                ['Svalbard & Jan Mayen Is.', 'SJ'],
                ['Swaziland', 'SZ'],
                ['Sweden', 'SE'],
                ['Switzerland', 'CH'],
                ['Syrian Arab Republic', 'SY'],
                ['Taiwan', 'TW'],
                ['Tajikistan', 'TJ'],
                ['Tanzania, United Rep. of', 'TZ'],
                ['Thailand', 'TH'],
                ['Timor-Leste (East Timor)', 'TL'],
                ['Togo', 'TG'],
                ['Tokelau', 'TK'],
                ['Tonga', 'TO'],
                ['Trinidad & Tobago', 'TT'],
                ['Tunisia', 'TN'],
                ['Turkey', 'TR'],
                ['Turkmenistan', 'TM'],
                ['Turks and Caicos Is.', 'TC'],
                ['Tuvalu', 'TV'],
                ['Uganda', 'UG'],
                ['Ukraine', 'UA'],
                ['United Arab Emirates', 'AE'],
                ['United Kingdom', 'GB'],
                ['United States', 'US'],
                ['US Minor Outlying Isl.', 'UM'],
                ['Uruguay', 'UY'],
                ['Uzbekistan', 'UZ'],
                ['Vanuatu', 'VU'],
                ['Venezuela', 'VE'],
                ['Viet Nam', 'VN'],
                ['Virgin Islands, British', 'VG'],
                ['Virgin Islands, U.S.', 'VI'],
                ['Wallis and Futuna', 'WF'],
                ['Western Sahara', 'EH'],
                ['Yemen', 'YE'],
                ['Zambia', 'ZM'],
                ['Zimbabwe', 'ZW']
        ]
        if (Country.count() == 0) {
            insertSql.each {
                new Country(code: it[1], name: it[0]).save();
            }
            Country.where {
                code == "AU"
            }.updateAll([
                isDefault: true
            ])
            AppEventManager.fire("country-bootstrap-init")
        }
    }

    public boolean equals(Object anotherCountry) {
        if (anotherCountry instanceof Country) {
            def country = (Country) anotherCountry
            if (country.id && this.id) {
                return country.id == this.id;
            }
            if (country.code == null && this.code == null) {
                return country.name != null && country.name.equalsIgnoreCase(this.name);
            }
            if (country.name == null && this.name == null) {
                return country.code != null && country.code.equalsIgnoreCase(this.code);
            }
            return (this.name != null && this.name.equalsIgnoreCase(country.name)) && (this.code != null && this.code.equalsIgnoreCase(country.code));
        }
        return false;
    }

    @Override
    int hashCode() {
        if (id) {
            return ("ID : " + id).hashCode();
        }
        if (code) {
            return ("CODE : " + code).hashCode();
        }
        if (name) {
            return ("NAME : " + name).hashCode();
        }
        return super.hashCode();
    }
}
