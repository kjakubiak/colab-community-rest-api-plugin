package sailpoint.community.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.tools.GeneralException;

import java.util.Map;

/**
 * The type Role helper.
 */
public class RoleHelper {
    /**
     * The constant log.
     */
    public static final Log log = LogFactory.getLog(RoleHelper.class);

    RoleHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check required attributes.
     *
     * @param attributes the attributes
     * @throws GeneralException the general exception
     */
    public static void checkRequiredAttributes(Map<String, Object> attributes) throws GeneralException {
        if (attributes.get("name") == null) {
            throw new GeneralException("Role name is required");
        }
        if (attributes.get("description") == null) {
            throw new GeneralException("Role description is required");
        }
        if (attributes.get("type") == null) {
            throw new GeneralException("Role type is required");
        }
    }
}
