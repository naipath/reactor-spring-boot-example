package nl.ordina.whois;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
class Whois {
    private String status;
    private String whois_server;
    private String status_desc;
    private boolean limit_hit;
    private boolean registered;
    private List<Contact> contacts = new ArrayList<>();
}
