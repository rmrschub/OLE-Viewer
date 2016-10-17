/*
 * This file is part of OLE-Viewer. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * You may not use this file except in compliance with the License.
 */
package de.dfki.resc28.ole.viewer.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;

import de.dfki.resc28.ole.viewer.vocabularies.ADMS;


@Path("")
public class Service 
{
	@GET
	@Path("/visualize")
//	@Produces(MediaType.APPLICATION_JSON)
	public Response generateSource( @DefaultValue("http://ole-frontend/repo") @QueryParam("uri") String oleUri ) throws IOException 
	{
		JsonArray elements = new JsonArray();
		
		
//		URL oleURL = new URL(oleUri);
//		InputStream is = oleURL.openStream();
//		Model modelToDisplay = ModelFactory.createDefaultModel(); 
//		RDFDataMgr.read(modelToDisplay, is, oleUri, Lang.TURTLE);
		Model modelToDisplay = RDFDataMgr.loadModel(oleUri, Lang.TURTLE);

		PrefixMapping pm = PrefixMapping.Factory.create();
		pm.setNsPrefixes(modelToDisplay.getNsPrefixMap());
		
		
		
		// generate cytoscape nodes for all resources in modelToDispla	
		Set<RDFNode> nodeSet = modelToDisplay.listObjects().toSet();
		ResIterator subjects = modelToDisplay.listSubjects();
		while (subjects.hasNext())
		{
			nodeSet.add(subjects.next());
		}

		
		Iterator<RDFNode> nodes = nodeSet.iterator();
		while(nodes.hasNext())
		{
			RDFNode rdfNode = nodes.next();
			JsonObject node = new JsonObject();
			
			if (rdfNode.isURIResource())
			{
				String uri = ((Resource)rdfNode).getURI();
				
				node.put("position", "");
				node.put("group", "nodes");
				node.put("removed", false);
				node.put("selected", false);
				node.put("selectable", true);
				node.put("locked",false);
				node.put("grabbed",false);
				node.put("grabbable",true);
				node.put("classes", "");
				
				JsonObject data = new JsonObject();
				data.put("id", rdfNode.hashCode());
				data.put("uri", uri);
				data.put("name", pm.shortForm(uri));
				data.put("nodeType", "uriNode");
				
				node.put("data", data);
			}
			else if (rdfNode.isAnon())
			{
				node.put("position", "");
				node.put("group", "nodes");
				node.put("removed", false);
				node.put("selected", false);
				node.put("selectable", true);
				node.put("locked",false);
				node.put("grabbed",false);
				node.put("grabbable",true);
				node.put("", "blankNode");
				
				JsonObject data = new JsonObject();
				data.put("id", rdfNode.hashCode());
				data.put("nodeType", "blankNode");
				
				node.put("data", data);
			}
			else 
			{
				node.put("position", "");
				node.put("group", "nodes");
				node.put("removed", false);
				node.put("selected", false);
				node.put("selectable", true);
				node.put("locked",false);
				node.put("grabbed",false);
				node.put("grabbable",true);
				node.put("classes", "");
				
				JsonObject data = new JsonObject();
				data.put("id", rdfNode.hashCode());
				String value = String.valueOf(((Literal)rdfNode).getValue());
				data.put("value", value);
				data.put("nodeType", "literalNode");
				
				node.put("data", data);
			}
			
			elements.add(node);
		}
	
		
		// generate cytoscape edges for all triples in modelToDispla
		StmtIterator triples = modelToDisplay.listStatements();
		while (triples.hasNext())
		{
			Statement triple = triples.next();
			
			String uri = triple.getPredicate().getURI();
			
			JsonObject edge = new JsonObject();
			edge.put("group", "edges");
			edge.put("removed", "false");
			edge.put("selected", "false");
			edge.put("selectable", "true");
			edge.put("locked", "false");
			edge.put("grabbed", "false");
			edge.put("grabbable", "true");
			edge.put("classes", "autorotate");
			
			JsonObject data = new JsonObject();
			data.put("source", triple.getSubject().hashCode());
			data.put("target", triple.getObject().hashCode());
			data.put("id", triple.hashCode());
			data.put("uri", uri);
			data.put("name", pm.shortForm(uri));	
			edge.put("data", data);
			
			elements.add(edge);
		}
		
		return Response.ok(elements.toString(), MediaType.APPLICATION_JSON).build();
	}
}