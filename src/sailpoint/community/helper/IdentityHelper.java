package sailpoint.community.helper;

import sailpoint.object.Identity;
import sailpoint.object.Link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Identity helper.
 */
public class IdentityHelper {
    /**
     * Instantiates a new Identity helper.
     */
    IdentityHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * To map map.
     *
     * @param identity the identity
     * @return the map
     */
    protected static Map<String, Object> toMap(Identity identity) {
        Map<String, Object> identityMap = new HashMap<>();
        identityMap.put("id", identity.getId());
        identityMap.put("name", identity.getName());
        identityMap.put("displayName", identity.getDisplayName());
        identityMap.put("email", identity.getEmail());
        identityMap.put("lastRefresh", identity.getLastRefresh());
        identityMap.put("lastModified", identity.getModified());
        identityMap.put("created", identity.getCreated());
        identityMap.put("attributes", identity.getAttributes());
        identityMap.put("inactive", identity.isInactive());
        identityMap.put("correlatedOverriden", identity.isCorrelatedOverridden());
        identityMap.put("protected", identity.isProtected());
        identityMap.put("workgroup", identity.isWorkgroup());
        identityMap.put("assignedRoles", identity.getAssignedRoleSummary());

        List<String> workgroups = new ArrayList<>();
        for (Identity workgroup : identity.getWorkgroups()) {
            workgroups.add(workgroup.getName());
        }
        identityMap.put("workgroups", workgroups);

        List<Map<String, Object>> links = new ArrayList<>();
        for (Link link : identity.getLinks()) {
            links.add(toMap(link));
        }
        identityMap.put("links", links);


        return identityMap;
    }

    /**
     * To map map.
     *
     * @param link the link
     * @return the map
     */
    protected static Map<String, Object> toMap(Link link) {
        Map<String, Object> linkMap = new HashMap<>();
        linkMap.put("id", link.getId());
        linkMap.put("nativeIdentity", link.getNativeIdentity());
        linkMap.put("displayName", link.getDisplayName());
        linkMap.put("application", link.getApplicationName());
        linkMap.put("lastRefresh", link.getLastRefresh());
        linkMap.put("lastModified", link.getModified());
        linkMap.put("created", link.getCreated());
        linkMap.put("attributes", link.getAttributes());
        linkMap.put("disabled", link.getIiqDisabled());

        return linkMap;
    }
}
