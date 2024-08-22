package com.example.firewall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.firewall.model.FirewallRule;
import com.example.firewall.repository.FirewallRuleRepository;
import com.example.firewall.service.NetworkUtils;

@Controller
public class FirewallController {
    @Autowired
    private NetworkUtils networkUtils;

    @Autowired
    private FirewallRuleRepository firewallRuleRepository;

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

    // Display the edit form
    @GetMapping("/edit-connection/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        FirewallRule firewallRule = firewallRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid connection ID: " + id));
        model.addAttribute("firewallRule", firewallRule);
        return "edit-connection";
    }

    // Handle form submission to save the edited connection
    @PostMapping("/edit-connection/{id}")
    public String updateConnection(@PathVariable("id") Long id, @ModelAttribute FirewallRule updatedFirewallRule) {
        FirewallRule existingRule = firewallRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid connection ID: " + id));

        // Update the fields
        existingRule.setLocalIp(updatedFirewallRule.getLocalIp());
        existingRule.setLocalPort(updatedFirewallRule.getLocalPort());
        existingRule.setRemoteIp(updatedFirewallRule.getRemoteIp());
        existingRule.setRemotePort(updatedFirewallRule.getRemotePort());
        existingRule.setProtocol(updatedFirewallRule.getProtocol());
        existingRule.setState(updatedFirewallRule.getState());
        existingRule.setProcessName(updatedFirewallRule.getProcessName());

        firewallRuleRepository.save(existingRule);

        return "redirect:/home";
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
