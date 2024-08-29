package com.example.firewall.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rules")
@Data
@NoArgsConstructor
public class FirewallRule {

    public FirewallRule(long id, String port, String protocol, String action, String ipFrom) {
    }

    public enum Protocol {
        TCP,
        UDP
    }

    public enum Action {
        ALLOW_IN("Allow In"),
        ALLOW_OUT("Allow Out"),
        DENY_IN("Deny In"),
        DENY_OUT("Deny Out");

        private final String displayName;

        Action(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Action fromString(String action) {
            for (Action a : Action.values()) {
                if (a.displayName.equalsIgnoreCase(action)) {
                    return a;
                }
            }
            return null;
        }
    }

    @Id
    private Long id;

    @Column(nullable = false)
    private String port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Protocol protocol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    @Column(nullable = false)
    private String ipFrom;

    // Yeni Alanlar
    @Column
    private String localIp;

    @Column
    private String localPort;

    @Column
    private String remoteIp;

    @Column
    private String remotePort;

    @Column
    private String state;

    @Column
    private String processName;


    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }

    public String getLocalPort() {
        return localPort;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }

    public String getRemotePort() {
        return remotePort;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setIpFrom(String ipFrom) {
        this.ipFrom = ipFrom;
    }

    public String getIpFrom() {
        return ipFrom;
    }
}
