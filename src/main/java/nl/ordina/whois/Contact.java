package nl.ordina.whois;

import lombok.Getter;

@Getter
class Contact {
    private String type;
    private String name;
    private String organization;
    private String street;
    private String city;
    private String zipcode;
    private String state;
    private String country;
    private String phone;
    private String fax;
    private String email;
    private String full_address;
}