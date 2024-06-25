package sailpoint.community.helper;

import sailpoint.object.Identity;

import java.util.Map;

/**
 * The type General helper.
 */
public class GeneralHelper {
    /**
     * Instantiates a new General helper.
     */
    GeneralHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * To map map.
     *
     * @param identity the identity
     * @return the map
     */
    public static Map<String, Object> toMap(Identity identity) {
        return IdentityHelper.toMap(identity);
    }

    /**
     * To map map.
     *
     * @param resourceObject the resource object
     * @return the map
     */
    public static Map<String, Object> toMap(sailpoint.object.ResourceObject resourceObject) {
        return ResourceObjectHelper.toMap(resourceObject);
    }

}
