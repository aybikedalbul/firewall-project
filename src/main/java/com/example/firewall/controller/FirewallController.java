package com.example.firewall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String getHomePage(Model model) {
        List<FirewallRule> rules = networkUtils.getNetworkConnections();
        model.addAttribute("rules", rules);
        return "home";
    }

    @GetMapping("/config_page")
    public String showConfigPage(@RequestParam("port") String port, Model model) {
        model.addAttribute("port", port); // Port numarasını modele ekleyin
        return "config_page"; // config_page.html dosyasını döndür
    }

    @PostMapping("/apply-config")
    public String applyConfig(@RequestParam("port") String port,
                              @RequestParam("action") String action) {

        String command = "sudo ufw " + action + " " + port;
        executeCommand(command);
        return "redirect:/config_page?port=" + port;
    }

    private void executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/edit-connection/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        FirewallRule firewallRule = firewallRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid connection ID: " + id));
        model.addAttribute("firewallRule", firewallRule);
        return "edit-connection";
    }

    @PostMapping("/edit-connection/{id}")
    public String updateConnection(@PathVariable("id") Long id, @ModelAttribute FirewallRule updatedFirewallRule) {
        FirewallRule existingRule = firewallRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid connection ID: " + id));

        // Update fields as needed
        existingRule.setLocalIp(String.valueOf(updatedFirewallRule.getClass()));
        existingRule.setLocalPort(updatedFirewallRule.getLocalPort());
        existingRule.setRemoteIp(updatedFirewallRule.getRemoteIp());
        existingRule.setRemotePort(updatedFirewallRule.getRemotePort());
        existingRule.setProtocol(updatedFirewallRule.getProtocol());
        existingRule.setState(updatedFirewallRule.getState());
        existingRule.setProcessName(updatedFirewallRule.getProcessName());

        firewallRuleRepository.save(existingRule);

        return "redirect:/home";
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home"; // Ana sayfa olarak home'a yönlendirme
    }
}
