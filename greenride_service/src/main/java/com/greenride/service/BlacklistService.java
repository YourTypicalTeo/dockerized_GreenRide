
package com.greenride.service;

import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlacklistService {

    // Χρήση Set για γρήγορη αναζήτηση. 
    // ConcurrentHashMap.newKeySet() για thread-safety.
    private final Set<String> blockedIps = ConcurrentHashMap.newKeySet();

    public boolean isBlocked(String ip) {
        return blockedIps.contains(ip);
    }

    public void blockIp(String ip) {
        blockedIps.add(ip);
    }

    public void unblockIp(String ip) {
        blockedIps.remove(ip);
    }
}
