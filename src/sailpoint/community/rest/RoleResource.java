package sailpoint.community.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.community.helper.RoleHelper;
import sailpoint.integration.ListResult;
import sailpoint.object.Bundle;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Role resource.
 */
@RequiredRight(value = "communityRestRoleResource")
@Path("/Roles")
public class RoleResource extends BasePluginResource {
    /**
     * The constant log.
     */
    public static final Log log = LogFactory.getLog(RoleResource.class);
    public static final String ALREADY_EXISTS = " already exists";
    public static final String ROLE_WITH_NAME = "Role with name ";
    private static SailPointContext context;

    private static void initConfig() throws GeneralException {
        context = SailPointFactory.getCurrentContext();
    }

    /**
     * Create role list result.
     *
     * @param attributes the attributes
     * @return the list result
     * @throws GeneralException the general exception
     * @throws IOException      the io exception
     */
    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ListResult createRole(Map<String, Object> attributes) throws GeneralException {
        log.debug("createRole called");
        initConfig();
        log.debug("Config initiated");
        log.debug("Attributes: " + attributes);
        log.debug("Checking required attributes");
        RoleHelper.checkRequiredAttributes(attributes);
        log.debug("Required attributes checked");
        log.debug("Checking if role with name " + attributes.get("name") + ALREADY_EXISTS);
        if (context.getObject(Bundle.class, (String) attributes.get("name")) != null) {
            log.debug(ROLE_WITH_NAME + attributes.get("name") + ALREADY_EXISTS);
            throw new GeneralException(ROLE_WITH_NAME + attributes.get("name") + ALREADY_EXISTS);
        }
        log.debug(ROLE_WITH_NAME + attributes.get("name") + " does not exist");

        Bundle role = new Bundle();
        log.debug("Setting role attributes");
        role.setName((String) attributes.get("name"));
        role.setType((String) attributes.get("type"));

        log.debug("Role attributes set");
        log.debug("Saving role");
        context.saveObject(role);
        context.commitTransaction();
        log.debug("Role saved");
        List<Bundle> roles = new ArrayList<>();
        roles.add(role);

        return new ListResult(roles, 1);
    }

    @Override
    public String getPluginName() {
        return "communityrestapi";
    }
}
