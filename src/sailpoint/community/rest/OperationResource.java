package sailpoint.community.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.Aggregator;
import sailpoint.api.Identitizer;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.api.Terminator;
import sailpoint.community.helper.GeneralHelper;
import sailpoint.connector.Connector;
import sailpoint.connector.ConnectorFactory;
import sailpoint.object.*;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

/**
 * The type Operation resource.
 */
@RequiredRight(value = "communityRestOperationResource")
@Path("/Operation")
public class OperationResource extends BasePluginResource {
    /**
     * The constant log.
     */
    public static final Log log = LogFactory.getLog(OperationResource.class);
    /**
     * The constant ERROR_REFRESHING_IDENTITY.
     */
    public static final String ERROR_REFRESHING_IDENTITY = "Error refreshing identity: ";
    public static final String FALSE = "false";
    public static final String TRUE = "true";
    private static SailPointContext context;

    private static void initConfig() throws GeneralException {
        context = SailPointFactory.getCurrentContext();
    }

    /**
     * Refresh identity response.
     *
     * @param identityIdOrName the identity id or name
     * @param attributes       the attributes
     * @return the response
     * @throws Exception the exception
     */
    @POST
    @Path("/Refresh/{identityIdOrName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)

    public Response refreshIdentity(@PathParam("identityIdOrName") String identityIdOrName, Map<String, Object> attributes) throws Exception {
        log.debug("Starting refreshIdentity");
        initConfig();
        Identity identity = context.getObject(Identity.class, identityIdOrName);

        log.debug("Identity: " + identity);
        if (identity == null) {
            log.debug("Identity not found: " + identityIdOrName);
            return Response.status(400).entity("Identity not found: " + identityIdOrName).build();
        }

        Attributes<String, Object> args = new Attributes<>();
        args.put("identity", identity);
        args.put("filter", "name==" + identity.getName());
        log.debug("Attributes: " + attributes);
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            log.debug("Adding attribute: " + entry + " = " + attributes.get(entry.getKey()));
            args.put(entry.getKey(), entry.getValue());
        }

        log.debug("Args: " + args);
        try {
            Identitizer idtz = new Identitizer(context, args);
            log.debug("Identitizer: " + idtz);
            log.debug("Last refresh before execution: " + identity.getLastRefresh().toString());
            idtz.refresh(identity);
            context.saveObject(identity);
            context.commitTransaction();
            log.debug(identity.getName() + " identity refreshed.");
            log.debug("Last refresh after execution: " + identity.getLastRefresh().toString());
        } catch (Exception e) {
            log.error(ERROR_REFRESHING_IDENTITY + identity.getName(), e);
            return Response.status(400).entity(ERROR_REFRESHING_IDENTITY + identity.getName()).build();
        }

        return Response.status(200).entity(GeneralHelper.toMap(identity)).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Refresh identities with filter response.
     *
     * @param filterString the filter string
     * @param attributes   the attributes
     * @return the response
     * @throws Exception the exception
     */
    @POST
    @Path("/Refresh/Filter")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response refreshIdentitiesWithFilter(@QueryParam("filter") String filterString, Map<String, Object> attributes) throws Exception {
        log.debug("Starting refreshIdentitiesWithFilter");
        initConfig();
        log.debug("Filter: " + filterString);
        Filter filter = Filter.compile(filterString);
        QueryOptions qo = new QueryOptions();
        qo.addFilter(filter);
        log.debug("QueryOptions: " + qo);
        List<Identity> identities = context.getObjects(Identity.class, qo);
        log.debug("Identities: " + identities);
        List<Map<String, Object>> listOfRefreshedIds = new ArrayList<>();
        for (Identity identity : identities) {
            log.debug("Refreshing identity: " + identity.getName());
            try {
                Response response = refreshIdentity(identity.getName(), attributes);
                listOfRefreshedIds.add(GeneralHelper.toMap(identity));
                log.debug("Refresh response: " + response);
            } catch (Exception e) {
                log.error(ERROR_REFRESHING_IDENTITY + identity.getName(), e);
                return Response.status(400).entity(ERROR_REFRESHING_IDENTITY + identity.getName()).build();
            }
        }
        log.debug("Finished refreshIdentitiesWithFilter");
        return Response.status(200).entity(listOfRefreshedIds).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Aggregate single account response.
     *
     * @param attributes the attributes
     * @return the response
     * @throws GeneralException the general exception
     */
    @POST
    @Path("/Aggregate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aggregateSingleAccount(Map<String, Object> attributes) throws GeneralException {
        initConfig();
        log.debug("Attributes: " + attributes);
        String errorMessage = "";
        String applicationName = Util.otos(attributes.get("applicationName"));
        String accountName = Util.otos(attributes.get("nativeIdentity"));
        log.debug("applicationName: " + applicationName);
        log.debug("accountName: " + accountName);
        if (Util.isNullOrEmpty(applicationName)) {
            errorMessage = "Missing required argument 'applicationName'";
            log.error(errorMessage);
            return Response.status(400).entity(errorMessage).build();
        }
        if (Util.isNullOrEmpty(accountName)) {
            errorMessage = "Missing required argument 'nativeIdentity'";
            log.error(errorMessage);
            return Response.status(400).entity(errorMessage).build();
        }
        Application appObject = context.getObjectByName(Application.class, applicationName);
        log.debug("Application object: " + appObject);
        if (null == appObject) {
            errorMessage = "Could not find application: " + applicationName;
            log.error(errorMessage);
            return Response.status(400).entity(errorMessage).build();
        }
        String appConnName = appObject.getConnector();
        log.debug("Application " + applicationName + " uses connector " + appConnName);
        if (appObject.getFeaturesString().contains("NO_RANDOM_ACCESS")) {
            errorMessage = "Application " + applicationName + " does not support getObject method - NO_RANDOM_ACCESS token present in FeaturesString.";
            log.error(errorMessage);
            return Response.status(400).entity(errorMessage).build();
        }
        Connector appConnector = ConnectorFactory.getConnector(appObject, null);
        log.debug("Connector instantiated, calling getObject() to read account details...");
        if (null == appConnector) {
            errorMessage = "Could not get connector for application: " + applicationName;
            log.error(errorMessage);
            return Response.status(400).entity(errorMessage).build();
        }
        log.debug("Connector instantiated, calling getObject() to read account details...");
        log.debug("Getting ResourceObject for account: " + accountName);
        ResourceObject rObj = null;

        try {
            rObj = appConnector.getObject("account", accountName, null);
            log.debug("Got raw resourceObject: " + rObj.toXml());
        } catch (Exception e) {
            errorMessage = "Exception while getting ResourceObject for account: " + accountName;
            log.error(errorMessage, e);
            return Response.status(400).entity(errorMessage).build();
        }
        if(rObj == null) {
            Filter f1 = Filter.eq("nativeIdentity", accountName);
            QueryOptions qo = new QueryOptions();
            qo.addFilter(f1);
            Identity id = context.getUniqueObject(Identity.class, f1);
            if (id == null) {
                errorMessage = "Could not find identity for account: " + accountName;
                log.error(errorMessage);
                return Response.status(400).entity(errorMessage).build();
            }
            List<Link> links = id.getLinks();
            for(Link link : links) {
                if(link.getApplication().getName().equals(applicationName) && link.getNativeIdentity().equals(accountName)) {
                    Terminator arnold = new Terminator(context);
                    arnold.deleteObject(link);
                    break;
                }
            }
        }
        Rule customizationRule = appObject.getCustomizationRule();
        log.debug("Customization rule: " + customizationRule);
        if (null != customizationRule) {
            log.debug("Customization rule found for application " + applicationName);
            try {
                HashMap<String, Object> ruleArgs = new HashMap<>();
                ruleArgs.put("context", context);
                ruleArgs.put("log", log);
                ruleArgs.put("object", rObj);
                ruleArgs.put("application", appObject);
                ruleArgs.put("connector", appConnector);
                ruleArgs.put("state", new HashMap<>());
                log.debug("Running Customization rule for " + applicationName);
                log.debug("ResourceObject before customization: " + rObj.toXml());
                ResourceObject newRObj = (ResourceObject) context.runRule(customizationRule, ruleArgs, null);
                log.debug("ResourceObject after customization: " + rObj.toXml());
                if (null != newRObj) {
                    rObj = newRObj;
                    log.debug("Got post-customization resourceObject: " + rObj.toXml());
                }
            } catch (Exception ex) {
                log.error("Error while running Customization rule for " + applicationName);
                return Response.status(400).entity(errorMessage).build();
            }
        }
        log.debug("Executing aggregation for account: " + accountName);
        Attributes<String, Object> argMap = new Attributes<>();
        argMap.put("promoteAttributes", TRUE);
        argMap.put("correlateEntitlements", TRUE);
        argMap.put("checkDeleted", FALSE);
        argMap.put("checkHistory", TRUE);
        argMap.put("checkPolicies", TRUE);
        argMap.put("correlateOnly", TRUE);
        argMap.put("correlateScope", TRUE);
        argMap.put("deltaAggregation", FALSE);
        argMap.put("enableManagedAttributeRenameDetection", FALSE);
        argMap.put("enablePartitioning", FALSE);
        argMap.put("noAutoCreateApplications", TRUE);
        argMap.put("noAutoCreateScopes", TRUE);
        argMap.put("noNeedsRefresh", FALSE);
        argMap.put("promoteManagedAttributes", FALSE);

        if (attributes.containsKey("settings")) {
            Map<String, Object> settings = (Map<String, Object>) attributes.get("settings");
            argMap.putAll(settings);
        }
        log.debug("Aggregator arguments: " + argMap);

        Aggregator agg = new Aggregator(context, argMap);
        log.debug("Aggregator instantiated, calling aggregate()...");
        TaskResult taskResult = agg.aggregate(appObject, rObj);

        log.debug("aggregation complete.");

        if (null == taskResult) {
            errorMessage = "Null taskResult returned from aggregate() call.";
            log.error(errorMessage);
            return Response.status(400).entity(errorMessage).build();
        }
        log.debug("TaskResult details: \n" + taskResult.toXml());
        return Response.status(200).entity(GeneralHelper.toMap(rObj)).build();
    }

    @Override
    public String getPluginName() {
        return "communityrestapi";
    }
}
