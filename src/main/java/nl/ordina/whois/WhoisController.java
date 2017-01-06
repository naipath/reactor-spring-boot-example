package nl.ordina.whois;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class WhoisController {

    private WhoisService whoisService;

    @GetMapping("/whois")
    public Mono<List<Contact>> getWhois(@RequestParam String domain) {
        return whoisService.get(domain);
    }
}
