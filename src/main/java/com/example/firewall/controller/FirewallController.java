package com.example.firewall.controller;

import com.example.firewall.model.FirewallRule;
import com.example.firewall.service.NetworkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Controller
public class FirewallController {
    @Autowired
    private NetworkUtils networkUtils;

    @GetMapping("/home")
    public String getHomePage() {
//        List<FirewallRule> rules = networkUtils.getNetworkConnections();
//        model.addAttribute("rules", rules);
        return "home";
    }

    @GetMapping("/getNetworkData")
    @ResponseBody
    public Map<String,Object> getNetworkData() {
        List<FirewallRule> rules = networkUtils.getNetworkConnections();
        Map<String,Object> response = new HashMap<>();
        response.put("data",rules);
        return response;
    }
//@Async
//@GetMapping("/getNetworkData")
//public CompletableFuture<Map<String, Object>> getNetworkData(@RequestParam int draw,
//                                                             @RequestParam int start,
//                                                             @RequestParam int length) {
//    List<FirewallRule> rules = networkUtils.getNetworkConnections();
//
//    // Sayfalama (Pagination) işlemi
//    List<FirewallRule> paginatedRules = rules.stream()
//            .skip(start)
//            .limit(length)
//            .collect(Collectors.toList());
//
//    Map<String, Object> result = new HashMap<>();
//    result.put("draw", draw);
//    result.put("recordsTotal", rules.size());
//    result.put("recordsFiltered", rules.size()); // Filtre eklenirse bu değişebilir
//    result.put("data", paginatedRules);
//
//    return CompletableFuture.completedFuture(result);
//}

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home"; // Ana sayfa olarak home'a yönlendirme
    }
}
