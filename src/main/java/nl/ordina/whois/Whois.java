package nl.ordina.whois;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
class Whois {
    private String status;
    private String whois_server;
    private String status_desc;
    private boolean limit_hit;
    private boolean registered;
    private List<Contact> contacts = new ArrayList<>();
}
