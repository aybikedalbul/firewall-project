package com.example.firewall.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.firewall.model.FirewallRule;
import com.example.firewall.repository.FirewallRuleRepository;

@Service
public class NetworkUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
    private final FirewallRuleRepository repository;

    @Autowired
    public NetworkUtils(FirewallRuleRepository repository) {
        this.repository = repository;
    }

    public List<FirewallRule> getNetworkConnections() {
        final String ubuntuPassword = System.getenv("UBUNTU_PASSWORD");
        List<FirewallRule> rules = new ArrayList<>();

        try {
            String enableCommand = String.format("echo %s | sudo -S ufw enable && echo %s | sudo -S ufw status numbered", ubuntuPassword, ubuntuPassword);
            Process process = executeCommand(enableCommand);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            boolean isThereRule = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("To")) {
                    reader.readLine(); // Skip header line
                    while ((line = reader.readLine()) != null && line.length() > 5) {
                        String idPart = line.substring(0, 5).trim();
                        String[] parts = line.substring(5).trim().replaceAll("\\s{2,}", " ").split(" ");

                        if (parts.length == 4 || parts.length == 6) { // (v6) and (out)
                            parseFirewallRule(rules, parts, idPart);
                        } else if (parts.length == 5 || parts.length == 7) { // (out) and (v6) with "ALLOW IN" or "DENY IN"
                            parseFirewallRuleWithProtocol(rules, parts, idPart);
                        }
                    }
                    isThereRule = true;
                    break;
                }
            }

            if (!isThereRule) {
                logger.warn("No firewall rules found.");
            }

            int exitVal = process.waitFor();
            if (exitVal != 0) {
                logger.error("Error: Command execution failed with exit value: " + exitVal);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while getting network connections", e);
        }
        repository.saveAll(rules);
        return rules;
    }

    public boolean isPortAllowed(int port) {
        List<FirewallRule> rules = repository.findAll();
        for (FirewallRule rule : rules) {
            if (rule.getPort().equals(String.valueOf(port)) && rule.getAction() == FirewallRule.Action.DENY_IN) {
                return false;
            }
        }
        return true;
    }

    private void parseFirewallRule(List<FirewallRule> rules, String[] parts, String idPart) {
        int id = Integer.parseInt(idPart.replace("[", "").replace("]", "").trim());
        String[] portAndProtocol = parsePortAndProtocol(parts[0]);
        String port = portAndProtocol[0];
        String protocol = portAndProtocol[1];
        String action = parts[1] + " " + parts[2];
        String ipFrom = parts[3];

        rules.add(new FirewallRule((long) id, port, protocol, action, ipFrom));
    }

    private void parseFirewallRuleWithProtocol(List<FirewallRule> rules, String[] parts, String idPart) {
        int id = Integer.parseInt(idPart.replace("[", "").replace("]", "").trim());
        String[] portAndProtocol = parsePortAndProtocol(parts[0]);
        String port = portAndProtocol[0];
        String protocol = portAndProtocol[1];
        String action = parts[2] + " " + parts[3];
        String ipFrom = parts[4] + " (v6)";

        rules.add(new FirewallRule((long) id, port, protocol, action, ipFrom));
    }

    private String[] parsePortAndProtocol(String portAndProtocol) {
        if (portAndProtocol.contains("/")) {
            return portAndProtocol.replace("/", " ").split(" ");
        } else {
            return new String[]{portAndProtocol, null};
        }
    }

    private Process executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        Process process = processBuilder.start();
        process.waitFor();
        return process;
    }

    public Optional<FirewallRule> getRuleById(Long id) {
        return repository.findById(id);
    }

    private static String getProcessNameByPid(String pid) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(pid)) {
                        return line.split("\\s+")[0];
                    }
                }
                int exitVal = process.waitFor();
                if (exitVal != 0) {
                    logger.error("Error: Command execution failed with exit value: " + exitVal);
                }
            } catch (Exception e) {
                logger.error("Exception occurred while getting process name by PID", e);
            }
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("ps", "-p", pid, "-o", "comm=");
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                if (line != null) {
                    return line.trim();
                }

                int exitVal = process.waitFor();
                if (exitVal != 0) {
                    logger.error("Error: Command execution failed with exit value: " + exitVal);
                }
            } catch (Exception e) {
                logger.error("Exception occurred while getting process name by PID", e);
            }
        }
        return "Unknown Process";
    }
}
