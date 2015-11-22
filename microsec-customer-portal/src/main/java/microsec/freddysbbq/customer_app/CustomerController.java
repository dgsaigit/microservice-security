package microsec.freddysbbq.customer_app;

import java.security.Principal;
import java.util.Collection;
import java.util.LinkedHashMap;

import lombok.Data;
import microsec.common.Targets;
import microsec.freddysbbq.menu.model.v1.MenuItem;
import microsec.freddysbbq.order.model.v1.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CustomerController {

    @Autowired
    @Qualifier("loadBalancedOauth2RestTemplate")
    private OAuth2RestTemplate oauth2RestTemplate;

    @Autowired
    private Targets targets;

    @RequestMapping("/")
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "index";
    }

    @RequestMapping("/menu")
    public String menu(Model model) throws Exception {
        PagedResources<MenuItem> menu = oauth2RestTemplate
                .exchange(
                        "{menu}/menuItems",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<PagedResources<MenuItem>>() {
                        }, targets.getMenu())
                .getBody();
        model.addAttribute("menu", menu.getContent());
        return "menu";
    }

    @RequestMapping("/myorders")
    public String myOrders(Model model) throws Exception {
        Collection<Order> orders = oauth2RestTemplate
                .exchange(
                        "{order}/myorders",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<Collection<Order>>() {
                        }, targets.getOrder())
                .getBody();
        model.addAttribute("orders", orders);
        return "myorders";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/myorders")
    public String placeOrder(Model model, @ModelAttribute OrderForm orderForm) throws Exception {
        oauth2RestTemplate
                .postForObject("{order}/myorders", orderForm.getOrder(), Void.class, targets.getOrder());
        return "redirect:.";
    }

    @Data
    public static class OrderForm {
        private LinkedHashMap<Long, Integer> order = new LinkedHashMap<Long, Integer>();
    }
}
