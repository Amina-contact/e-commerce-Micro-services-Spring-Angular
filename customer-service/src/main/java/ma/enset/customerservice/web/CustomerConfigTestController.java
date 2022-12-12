package ma.enset.customerservice.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
//annotation de actuator
@RefreshScope
public class CustomerConfigTestController {
    //Pour injecter des variables de la configuration
    @Value("${global.params.p1}")
    private String p1;
    @Value("${global.params.p2}")
    private String p2;
    @Value("${customer.params.x}")
    private String x;
    @Value("${customer.params.y}")
    private String y;

    @GetMapping("/params")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String,String> params(){
        return Map.of("p1",p1,"p2",p2,"x",x,"y",y);
    }
}
