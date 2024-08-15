package com.example.firewall.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirewallRule {
    private String localIp;
    private int localPort;
    private String remoteIp;
    private int remotePort;
    private String protocol;
    private String state;
    private String processName;
    private long startTime;
    private long duration;

}
