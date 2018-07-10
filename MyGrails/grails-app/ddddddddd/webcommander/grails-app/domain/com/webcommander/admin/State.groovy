package com.webcommander.admin

import com.webcommander.events.AppEventManager

class State {
    Long id
    String name
    String code
    Country country
    Boolean isActive = true
    Boolean isDefault = false

    static constraints = {
        name(size: 1..100)
        code(nullable: false, size: 2..5)
        country(nullable: false, unique: 'code')
    }

    public static void initialize() {
        def insertSql = [
                /*US*/
                ['AK','Alaska','US'],
                ['AL','Alabama','US'],
                ['AR','Arkansas','US'],
                ['AZ','Arizona','US'],
                ['CA','California','US'],
                ['CO','Colorado','US'],
                ['CT','Connecticut','US'],
                ['DC','Washington DC','US'],
                ['DE','Delaware','US'],
                ['FL','Florida','US'],
                ['GA','Georgia','US'],
                ['HI','Hawaii','US'],
                ['IA','Iowa','US'],
                ['ID','Idaho','US'],
                ['IL','Illinois','US'],
                ['IN','Indiana','US'],
                ['KS','Kansas','US'],
                ['KY','Kentucky','US'],
                ['LA','Louisiana','US'],
                ['MA','Massachusetts','US'],
                ['MD','Maryland','US'],
                ['ME','Maine','US'],
                ['MI','Michigan','US'],
                ['MN','Minnesota','US'],
                ['MO','Missouri','US'],
                ['MS','Mississippi','US'],
                ['MT','Montana','US'],
                ['NC','North Carolina','US'],
                ['ND','North Dakota','US'],
                ['NE','Nebraska','US'],
                ['NH','New Hampshire','US'],
                ['NJ','New Jersey','US'],
                ['NM','New Mexico','US'],
                ['NV','Nevada','US'],
                ['NY','New York','US'],
                ['OH','Ohio','US'],
                ['OK','Oklahoma','US'],
                ['OR','Oregon','US'],
                ['PA','Pennsylvania','US'],
                ['PR','Puerto Rico','US'],
                ['RI','Rhode Island','US'],
                ['SC','South Carolina','US'],
                ['SD','South Dakota','US'],
                ['TN','Tennessee','US'],
                ['TX','Texas','US'],
                ['UT','Utah','US'],
                ['VA','Virginia','US'],
                ['VT','Vermont','US'],
                ['WA','Washington','US'],
                ['WI','Wisconsin','US'],
                ['WV','West Virginia','US'],
                ['WY','Wyoming','US'],
                /*Canada*/
                ['AB','Alberta','CA'],
                ['BC','British Columbia','CA'],
                ['MB','Manitoba','CA'],
                ['NB','New Brunswick','CA'],
                ['NL','Newfoundland & Labrador','CA'],
                ['NS','Nova Scotia','CA'],
                ['NT','Northwest Territories','CA'],
                ['NU','Nunavut','CA'],
                ['ON','Ontario','CA'],
                ['PE','Prince Edward Island','CA'],
                ['QC','Québec','CA'],
                ['SK','Saskatchewan','CA'],
                ['YT','Yukon','CA'],
                /*Australia*/
                ['ACT','Australian Capital Territory','AU'],
                ['NSW','New South Wales','AU'],
                ['NT','Northern Territory','AU'],
                ['QLD','Queensland','AU'],
                ['SA','South Australia','AU'],
                ['TAS','Tasmania','AU'],
                ['VIC','Victoria','AU'],
                ['WA','Western Australia','AU'],
                /* New Zealand */
                ["NTL", "Northland", "NZ"],
                ["AUK", "Auckland", "NZ"],
                ["WKO", "Waikato", "NZ"],
                ["BOP", "Bay of Plenty", "NZ"],
                ["GIS", "Gisborne ", "NZ"],
                ["HKB", "Hawke's Bay", "NZ"],
                ["TKI", "Taranaki", "NZ"],
                ["MWT", "Manawatu-Wanganui", "NZ"],
                ["WGN", "Wellington", "NZ"],
                ["TAS", "Tasman ", "NZ"],
                ["NSN", "Nelson", "NZ"],
                ["MBH", "Marlborough", "NZ"],
                ["WTC", "West Coast", "NZ"],
                ["CAN", "Canterbury", "NZ"],
                ["OTA", "Otago", "NZ"],
                ["STL", "Southland", "NZ"],
                /*UK*/
                ["UKC", "North East", "GB"],
                ["UKD", "North West", "GB"],
                ["UKE", "Yorkshire and the Humber", "GB"],
                ["UKF", "East Midlands", "GB"],
                ["UKG", "West Midlands", "GB"],
                ["UKH", "East of England", "GB"],
                ["UKI", "London", "GB"],
                ["UKJ", "South East", "GB"],
                ["UKK", "South West", "GB"],
                ["UKL", "Wales", "GB"],
                ["UKM", "Scotland", "GB"],
                ["UKN", "Northern Ireland", "GB"]

        ]
        def _init = {
            if(State.count() == 0) {
                insertSql.each {
                    new State(code: it[0], name: it[1], country: Country.findByCode(it[2])).save();
                }
                AppEventManager.fire("state-bootstrap-init")
            }
        }
        if(Country.count()) {
            _init()
        } else {
            AppEventManager.one("country-bootstrap-init", "bootstrap-init", _init)
        }
    }

    public boolean equals(Object anotherState) {
        if (anotherState instanceof State) {
            State state = (State) anotherState;
            if (state.id && this.id) {
                return state.id == this.id;
            }
            if (this.country.equals(state.country)) {
                if (state.code == null && this.code == null) {
                    return state.name != null && state.name.equalsIgnoreCase(this.name);
                } else if (state.name == null && this.name == null) {
                    return state.code != null && state.code.equalsIgnoreCase(this.code);
                } else {
                    return (this.name != null && this.name.equalsIgnoreCase(state.name)) && (this.code != null && this.code.equalsIgnoreCase(state.code));
                }
            }
            return false;
        }
    }

    @Override
    int hashCode() {
        return id ? ("ID : " + id).hashCode() : (country.hashCode() + (code ? code.hashCode() : (name ? name.hashCode() : super.hashCode())));
    }
}
