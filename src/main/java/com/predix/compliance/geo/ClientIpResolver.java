package com.predix.compliance.geo;

import com.predix.compliance.config.ComplianceProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.List;

@Component
public class ClientIpResolver {

    private final List<String> trustedProxies;

    public ClientIpResolver(ComplianceProperties properties) {
        this.trustedProxies = properties.getTrustedProxies();
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            return remoteAddr;
        }
        if (!isTrustedProxy(remoteAddr)) {
            return remoteAddr;
        }
        String first = forwarded.split(",")[0].trim();
        return first.isBlank() ? remoteAddr : first;
    }

    public String resolveFromHeaders(String remoteAddr, String xForwardedFor) {
        if (xForwardedFor == null || xForwardedFor.isBlank()) {
            return remoteAddr;
        }
        if (!isTrustedProxy(remoteAddr)) {
            return remoteAddr;
        }
        return xForwardedFor.split(",")[0].trim();
    }

    private boolean isTrustedProxy(String remoteAddr) {
        if (remoteAddr == null) {
            return false;
        }
        for (String trusted : trustedProxies) {
            if (trusted.contains("/")) {
                if (matchesCidr(remoteAddr, trusted)) {
                    return true;
                }
            } else if (trusted.equals(remoteAddr) || "localhost".equals(trusted) && isLoopback(remoteAddr)) {
                return true;
            }
        }
        return isLoopback(remoteAddr);
    }

    private boolean isLoopback(String addr) {
        return "127.0.0.1".equals(addr) || "0:0:0:0:0:0:0:1".equals(addr) || "::1".equals(addr);
    }

    private boolean matchesCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress target = InetAddress.getByName(ip);
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            byte[] targetBytes = target.getAddress();
            byte[] networkBytes = network.getAddress();
            if (targetBytes.length != networkBytes.length) {
                return false;
            }
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (targetBytes[i] != networkBytes[i]) {
                    return false;
                }
            }
            if (remainingBits == 0) {
                return true;
            }
            int mask = 0xFF << (8 - remainingBits);
            return (targetBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
        } catch (Exception e) {
            return false;
        }
    }
}
