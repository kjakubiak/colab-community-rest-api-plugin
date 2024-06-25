package sailpoint.community.helper;

import sailpoint.object.ResourceObject;

import java.util.Map;

/**
 * The type Resource object helper.
 */
public class ResourceObjectHelper {
    ResourceObjectHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * To map map.
     *
     * @param resourceObject the resource object
     * @return the map
     */

    public static Map<String, Object> toMap(ResourceObject resourceObject) {
        return resourceObject.toMap();
    }
}
