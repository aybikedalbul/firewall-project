package com.example.firewall.service;

import com.example.firewall.model.FirewallRule;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class NetworkUtils {

    public static List<FirewallRule> getNetworkConnections() {
        List<FirewallRule> rules = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (os.contains("win")) {
                processBuilder.command("powershell.exe", "/c", "Get-NetTCPConnection | Select-Object LocalAddress,LocalPort,RemoteAddress,RemotePort,State,OwningProcess | ConvertTo-Csv -NoTypeInformation");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                processBuilder.command("bash", "-c", "ss -tulnp");
            }

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);

                if (os.contains("win")) {
                    if (!line.contains("LocalAddress") && !line.isEmpty()) {
                        String[] parts = line.trim().split(",");
                        if (parts.length >= 6) {
                            String state = parts[4].replace("\"", "");
                            if (!state.equalsIgnoreCase("Bound") && !state.equalsIgnoreCase("Listen")) {
                                String localIp = parts[0].replace("\"", "");
                                int localPort = Integer.parseInt(parts[1].trim().replace("\"", ""));
                                String remoteIp = parts[2].replace("\"", "");
                                int remotePort = Integer.parseInt(parts[3].trim().replace("\"", ""));
                                String processName = getProcessNameByPid(parts[5].replace("\"", ""));
                                long startTime = System.currentTimeMillis(); // Zaman damgası için basit bir örnek
                                rules.add(new FirewallRule(localIp, localPort, remoteIp, remotePort, "TCP", state, processName, startTime, 0L));
                            }
                        }
                    }
                } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                    if (line.startsWith("tcp") || line.startsWith("udp")) {
                        String[] parts = line.split("\\s+");
                        String[] ipPort = parts[4].split(":");
                        if (ipPort.length == 2) {
                            String state = parts[6];
                            if (!state.equalsIgnoreCase("Bound") && !state.equalsIgnoreCase("Listen")) {
                                String localIp = ipPort[0];
                                int localPort = Integer.parseInt(ipPort[1].trim());
                                String remoteIp = parts[5].split(":")[0];
                                int remotePort = Integer.parseInt(parts[5].split(":")[1]);
                                rules.add(new FirewallRule(localIp, localPort, remoteIp, remotePort, parts[0], state, null, System.currentTimeMillis(), 0L));
                            }
                        }
                    }
                }
            }

            int exitVal = process.waitFor();
            if (exitVal != 0) {
                System.err.println("Error: Command execution failed!");
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return rules;
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
                    System.err.println("Error: Command execution failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (os.contains("nix") || os.contains("nux") || os.contains("mac")){
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
                    System.err.println("Error: Command execution failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Bilinmeyen Süreç";
    }
}
