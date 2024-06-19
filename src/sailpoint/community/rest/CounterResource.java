package sailpoint.community.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;
import sailpoint.object.WorkItem;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Counter resource.
 */
@RequiredRight(value = "communityRestCounterResource")
@Path("/Count")
public class CounterResource extends BasePluginResource {
    /**
     * The constant log.
     */
    public static final Log log = LogFactory.getLog(CounterResource.class);
    private static SailPointContext context;

    private static void initConfig() throws GeneralException {
        context = SailPointFactory.getCurrentContext();
    }

    /**
     * Count approvals response.
     *
     * @param identityIdOrName the identity id or name
     * @return the response
     * @throws GeneralException the general exception
     */
    @GET
    @Path("/Approvals")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countApprovals(@QueryParam("identity") String identityIdOrName) throws GeneralException {
        log.debug("Counting approvals");
        initConfig();
        log.debug("Config initiated");
        log.debug("Identity: " + identityIdOrName);
        Identity identity = context.getObject(Identity.class, identityIdOrName);
        log.debug("Identity: " + identity);
        QueryOptions qo = new QueryOptions();
        qo.addFilter(Filter.eq("owner.id", identity.getId()));
        log.debug("QueryOptions: " + qo);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Integer> returnMap = new HashMap<>();
        Integer counter = context.countObjects(WorkItem.class, qo);
        log.debug("Counter: " + counter);
        returnMap.put("total", counter);
        log.debug("ReturnMap: " + returnMap);
        return Response.status(200).entity(gson.toJson(returnMap)).build();
    }

    @Override
    public String getPluginName() {
        return "communityrestapi";
    }
}
