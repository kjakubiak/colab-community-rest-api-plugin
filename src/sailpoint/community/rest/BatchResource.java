package sailpoint.community.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.integration.ListResult;
import sailpoint.object.Identity;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Batch resource.
 */
@RequiredRight(value = "communityRestBatchResource")
@Path("/Batch")
public class BatchResource extends BasePluginResource {
    /**
     * The constant log.
     */
    public static final Log log = LogFactory.getLog(BatchResource.class);
    public static final String WORKGROUP_WITH_NAME_OR_ID = "Workgroup with name or id ";
    public static final String NOT_FOUND = " not found";
    private static SailPointContext context;

    private static void initConfig() throws GeneralException {
        context = SailPointFactory.getCurrentContext();
    }

    /**
     * Add member list result.
     *
     * @param workgroupNameOrId the workgroup name or id
     * @param attributes        the attributes
     * @return the list result
     * @throws GeneralException the general exception
     */
    @POST
    @Path("/Workgroup/{workgroupNameOrId}/add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ListResult addMember(@PathParam("workgroupNameOrId") String workgroupNameOrId, Map<String, Object> attributes) throws GeneralException {
        log.debug("addMember called");
        initConfig();
        log.debug("Config initiated");
        log.debug("Workgroup name or id: " + workgroupNameOrId);
        Identity workgroup = context.getObject(Identity.class, workgroupNameOrId);
        log.debug("Workgroup: " + workgroup);
        if (workgroup == null) {
            log.debug(WORKGROUP_WITH_NAME_OR_ID + workgroupNameOrId + NOT_FOUND);
            throw new GeneralException(WORKGROUP_WITH_NAME_OR_ID + workgroupNameOrId + NOT_FOUND);
        }
        log.debug("Workgroup found");
        log.debug("Attributes: " + attributes);
        List<String> members = (List<String>) attributes.get("members");
        if (members == null || members.isEmpty()) {
            log.debug("Members list is required");
            throw new GeneralException("Members list is required");
        }
        log.debug("Members list: " + members);
        int counter = 0;
        log.debug("Adding members to workgroup");
        for (String member : members) {
            counter++;
            Identity identity = context.getObject(Identity.class, member);
            log.debug("Identity: " + identity);
            if (identity == null) {
                throw new GeneralException("Identity with name or id " + member + NOT_FOUND);
            }
            log.debug("Adding workgroup to the identity");
            identity.add(workgroup);
            context.saveObject(identity);
            if (counter % 10 == 0) {
                log.debug("Committing transaction");
                context.commitTransaction();
            }
        }
        log.debug("Committing transaction");
        context.commitTransaction();
        log.debug("Returning success");
        List<Identity> wgs = new ArrayList<>();
        wgs.add(workgroup);

        return new ListResult(wgs, wgs.size());
    }


    @Override
    public String getPluginName() {
        return "communityrestapi";
    }
}
