package sailpoint.community.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Rule;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;

@RequiredRight(value = "communityRestObjectResource")
@Path("/Object")
public class ObjectResource extends BasePluginResource {
    public static final Log log = LogFactory.getLog(ObjectResource.class);
    private static SailPointContext context;


    private void initConfig() throws GeneralException {
        context = SailPointFactory.getCurrentContext();
    }


    @GET
    @Path("/getRule/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRule(@PathParam("name") String name) throws GeneralException, DocumentException, IOException {
        initConfig();
        Rule rule;
        String ruleXml;
        try {
            rule = context.getObject(Rule.class, name);
            if (rule == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Rule not found").build();
            }
            ruleXml = rule.toXml(true);

        } catch (GeneralException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        // Pretty print the XML
        Document doc = DocumentHelper.parseText(ruleXml);
        OutputFormat format = OutputFormat.createPrettyPrint();
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(doc);
        ruleXml = writer.toString();
        return Response.ok(ruleXml).build();
    }

    @Override
    public String getPluginName() {
        return "communityrestapi";
    }
}
