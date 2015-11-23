package introsde.rest.ehealth.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;    
import java.util.ArrayList;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleClient {

    private XPath xpath;
    private String first_person_id;
    private String last_person_id;
    private String xml_created_person_id;
    private String json_created_person_id;
    private ArrayList<String> measure_types;
    private String measure_id;
    private String measure_type;
    private String measure_person_id;
    private String xml_created_measure_id;
    private String json_created_measure_id;
    private static PrintWriter outXml;
    private static PrintWriter outJson;
    
    public static void main(String[] args) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

        SimpleClient c = new SimpleClient();
        ClientConfig clientConfig = new ClientConfig();
        Client client = ClientBuilder.newClient(clientConfig);
        WebTarget service = client.target(getBaseURI());
        XPathFactory factory = XPathFactory.newInstance();
        c.xpath = factory.newXPath();
        
        // Files for logging
        outXml = new PrintWriter("xml.log");
        outJson = new PrintWriter("json.log");
        
        String serverInfo = "Requesting from server running in: " + getBaseURI(); // Print server info
        System.out.println(serverInfo);
        outXml.println(serverInfo);
        outJson.println(serverInfo);
        
        // Queries
        c.sendRequest1(service);
        c.sendRequest2(service);
        c.sendRequest3(service);
        c.sendRequest4(service);
        c.sendRequest5(service);
        c.sendRequest9(service);
        c.sendRequest6(service);
        c.sendRequest7(service);
        c.sendRequest8(service);
        
        outXml.close();
        outJson.close();

    }
    
    /*                          Request 1: Get person                */
    public void sendRequest1(WebTarget service) throws XPathExpressionException {
        int requestNumber = 1;
        String request = "GET";
        String path = "/person";
        
        // XML
        String acceptType = "application/xml";
        Response result = service.path(path).request().accept(acceptType).get();
        int httpCode = result.getStatus();
        String contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String xmlString = result.readEntity(String.class);
        
        // Use Xpath for parsing XML
        Document xml = xmlStringToDocument(xmlString);
        
        XPathExpression getPersons = xpath.compile("/people/person");
        XPathExpression getFirstId = xpath.compile("/people/person[1]/idPerson");
        XPathExpression getLastId = xpath.compile("/people/person[last()]/idPerson");
        NodeList nodes = (NodeList) getPersons.evaluate(xml, XPathConstants.NODESET);
        
        // If nodelist length > 2 - status OK
        String status = getStatus(nodes.getLength()>2);
        // Save first and last person id
        first_person_id = ((Node) getFirstId.evaluate(xml, XPathConstants.NODE)).getTextContent();
        last_person_id = ((Node) getLastId.evaluate(xml, XPathConstants.NODE)).getTextContent();
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyXml(xmlString));
        
        // JSON 
        acceptType = "application/json";
        result = service.path(path).request().accept(acceptType).get();
        httpCode = result.getStatus();
        contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String jsonString = result.readEntity(String.class);
        
        JSONArray jsonArray = new JSONArray(jsonString);    
        status = getStatus(jsonArray.length()>2);

        first_person_id = String.valueOf(jsonArray.getJSONObject(0).getInt("idPerson"));
        last_person_id = String.valueOf(jsonArray.getJSONObject(jsonArray.length()-1).getInt("idPerson"));
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyJson(jsonString));
        
    }
    
    /*                          R2: Get person with id                    */

    public void sendRequest2(WebTarget service) {
        int requestNumber = 2;
        String request = "GET";
        String path = "/person/"+first_person_id;
        
        //XML 
        String acceptType = "application/xml";
        Response result = service.path(path).request().accept(acceptType).get();
        int httpCode = result.getStatus();
        String contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String xmlString = result.readEntity(String.class);
        String status = getStatus(httpCode == 200 || httpCode == 202);
        
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyXml(xmlString));
        
        // JSON 
        acceptType = "application/json";
        result = service.path(path).request().accept(acceptType).get();
        httpCode = result.getStatus();
        contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String jsonString = result.readEntity(String.class);
        status = getStatus(httpCode == 200 || httpCode == 202);
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyJson(jsonString));
        
    }
    
    /*                       R3: Put person with specified id                  */
    public void sendRequest3(WebTarget service) throws ParserConfigurationException, XPathExpressionException {
        int requestNumber = 3;
        String request = "PUT";
        String path = "/person/"+first_person_id;
        
        // XML
        String acceptType = "application/xml";

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("person");
        doc.appendChild(rootElement);
        
        String newFirstname = "Sterlitamak";
        Element firstname = doc.createElement("firstname");
        firstname.appendChild(doc.createTextNode(newFirstname));
        rootElement.appendChild(firstname);
    
        // Send put request with xml doc
        Response result = service.path(path).request().accept(acceptType).put(Entity.xml(doc));
        int httpCode = result.getStatus();
        String contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String xmlString = result.readEntity(String.class);
        
        // Get name from xml doc
        Document xml = xmlStringToDocument(xmlString);
        XPathExpression getFirstname = xpath.compile("/person/firstname");
        String realFirstname = ((Node) getFirstname.evaluate(xml, XPathConstants.NODE)).getTextContent();
        
        String status = getStatus(newFirstname.equals(realFirstname));
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyXml(xmlString));
        
        // JSON
        acceptType = "application/json";
        newFirstname = "Ufa";
        JSONObject obj = new JSONObject();
        obj.put("firstname", newFirstname);
        
        // Send post request with stringified json obj
        result = service.path(path).request().accept(acceptType).put(Entity.json(obj.toString()));
        httpCode = result.getStatus();
        contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String jsonString = result.readEntity(String.class);
        
        // Get name from json obj
        JSONObject jsonObj = new JSONObject(jsonString);    
        realFirstname = String.valueOf(jsonObj.getString("firstname"));
        status = getStatus(newFirstname.equals(realFirstname));
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyJson(jsonString));
    }
    
    /*                      R4: Post new person                   */
    public void sendRequest4(WebTarget service) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        int requestNumber = 4;
        String request = "POST";
        int httpCode = 0;
        String path = "/person";
        String contentType = "";
        String acceptType = "";
        String status = "";
        Response result = null;
        
        // XML
        acceptType = "application/xml";
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        
        Element rootElement = doc.createElement("person");
        doc.appendChild(rootElement);
        
        Element firstname = doc.createElement("firstname");
        firstname.appendChild(doc.createTextNode("John"));
        rootElement.appendChild(firstname);
        
        Element lastname = doc.createElement("lastname");
        lastname.appendChild(doc.createTextNode("Doe"));
        rootElement.appendChild(lastname);
        
        Element birthdate = doc.createElement("birthdate");
        birthdate.appendChild(doc.createTextNode("2000-01-01"));
        rootElement.appendChild(birthdate);
        
        Element healthProfile = doc.createElement("healthProfile");
        rootElement.appendChild(healthProfile);
        
        Element measureType1 = doc.createElement("measureType");
        healthProfile.appendChild(measureType1);
        
        Element measure1 = doc.createElement("measure");
        measure1.appendChild(doc.createTextNode("weight"));
        measureType1.appendChild(measure1);
        
        Element measure1value = doc.createElement("value");
        measure1value.appendChild(doc.createTextNode("55.9"));
        measureType1.appendChild(measure1value);
        
        Element measureType2 = doc.createElement("measureType");
        healthProfile.appendChild(measureType2);
        
        Element measure2 = doc.createElement("measure");
        measure2.appendChild(doc.createTextNode("height"));
        measureType2.appendChild(measure2);
        
        Element measure2value = doc.createElement("value");
        measure2value.appendChild(doc.createTextNode("112.0"));
        measureType2.appendChild(measure2value);
        
        // Send XML POST request
        result = service.path(path).request().accept(acceptType).post(Entity.xml(doc));
        httpCode = result.getStatus();
        contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String xmlString = result.readEntity(String.class);
        
        // Get created person id with Xpath
        Document xml = xmlStringToDocument(xmlString);
        XPathExpression getIdPerson = xpath.compile("/person/idPerson");
        xml_created_person_id = ((Node) getIdPerson.evaluate(xml, XPathConstants.NODE)).getTextContent();
        status = getStatus(xml_created_person_id != null && (httpCode == 200 || httpCode == 202 || httpCode == 201));
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyXml(xmlString));
        
        // JSON
        acceptType = "application/json";
        JSONObject obj = new JSONObject();
        obj.put("firstname", "Salavat");
        obj.put("lastname", "Ulaev");
        obj.put("birthdate", "1745-01-01");
        
        JSONObject measureType = new JSONObject();
        
        JSONObject jsonMeasure1 = new JSONObject();
        jsonMeasure1.put("measure", "weight");
        jsonMeasure1.put("value", "88.9");
        
        JSONObject jsonMeasure2 = new JSONObject();
        jsonMeasure2.put("measure", "height");
        jsonMeasure2.put("value", "192.0");

        JSONArray measures = new JSONArray();
        measures.put(jsonMeasure1);
        measures.put(jsonMeasure2);
        
        measureType.put("measureType", measures);
        
        obj.put("healthProfile", measureType);
        
        // Send JSON POST request
        result = service.path(path).request().accept(acceptType).post(Entity.json(obj.toString()));
        httpCode = result.getStatus();
        contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String jsonString = result.readEntity(String.class);
        
        // Get created person id
        JSONObject jsonObj = new JSONObject(jsonString);    
        json_created_person_id = String.valueOf(jsonObj.getInt("idPerson"));
        status = getStatus(json_created_person_id != null && (httpCode == 200 || httpCode == 202 || httpCode == 201));
        printRequest(requestNumber, "POST", path, acceptType, contentType, status, httpCode, prettyJson(jsonString));
    }
    
    /*           R5: Delete person which was created above      */
    public void sendRequest5(WebTarget service) {
        int requestNumber = 5;
        String request = "DELETE";
        
        // XML 
        String path = "/person/"+xml_created_person_id;
        String acceptType = "application/xml";
        // Send delete
        Response result1 = service.path(path).request().accept(acceptType).delete();
        int deleteHttpCode = result1.getStatus();
        String contentType = result1.getMediaType() != null ? result1.getMediaType().toString() : "null";

        // Send get to see if deleted
        Response result2 = service.path(path).request().accept(acceptType).get();
        int getHttpCode = result2.getStatus();
        // Save get response
        String getResponse = "Request: GET " + path + ": \n" +
                "=> HTTP Status: "  + getHttpCode +
                                    "";
        String status = getStatus(getHttpCode == 404);
        
        printRequest(requestNumber, request, path, acceptType, contentType, status, deleteHttpCode, getResponse);
        
        // JSON request
        path = "/person/"+json_created_person_id;
        acceptType = "application/json";
        result1 = service.path(path).request().accept(acceptType).delete();
        deleteHttpCode = result1.getStatus();
        contentType = result1.getMediaType() != null ? result1.getMediaType().toString() : "null";
        // Send get to see if deleted
        result2 = service.path(path).request().accept(acceptType).get();
        getHttpCode = result2.getStatus();
        // Save get response
        getResponse = "Request: GET " + path + ": \n" +
                "=> HTTP Status: "  + getHttpCode +
                                    "";
        status = getStatus(getHttpCode == 404);
        printRequest(requestNumber, request, path, acceptType, contentType, status, deleteHttpCode, getResponse);
    }
    
    /*                  R6: Get measureTypes of person with specified id              */
    public void sendRequest6(WebTarget service) throws XPathExpressionException {
        int requestNumber = 6;
        String request = "GET";
        String contentType = "";
        int httpCode = 0;
        
        // Create list of personIds
        ArrayList<String> personIds = new ArrayList<String>();
        personIds.add(first_person_id);
        personIds.add(last_person_id);
        
        // XML
        String acceptType = "application/xml";
        Boolean getData = false;
        String response = "";
        String pathList = "";
        
        // Loop though person id's
        for (int i=0; i<personIds.size(); i++) {
            // Loop through measuretypes
            for (int j=0; j<measure_types.size(); j++) {
                String path = "/person/"+personIds.get(i)+"/"+measure_types.get(j);
                pathList += path + ";";
                Response result = service.path(path).request().accept(acceptType).get();
                int tempHttpCode = result.getStatus();
                if (tempHttpCode != 404 && tempHttpCode != 500) {
                    String xmlString = result.readEntity(String.class);
                    Document xml = xmlStringToDocument(xmlString);
                    XPathExpression getMeasures = xpath.compile("/measures/Measure");
                    NodeList nodes = (NodeList) getMeasures.evaluate(xml, XPathConstants.NODESET);
                    
                    // If response contains measures
                    if (nodes.getLength() > 0) {
                        String prettyXmlString = prettyXml(xmlString);
                        response += prettyXmlString;
                        if (!getData) {
                            getData = true;
                            httpCode = tempHttpCode;
                            contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
                            XPathExpression getMeasureId = xpath.compile("/measures/Measure[1]/mid");
                            XPathExpression getMeasureType = xpath.compile("/measures/Measure[1]/measure");
                            measure_id = ((Node) getMeasureId.evaluate(xml, XPathConstants.NODE)).getTextContent();
                            measure_type = ((Node) getMeasureType.evaluate(xml, XPathConstants.NODE)).getTextContent();
                            measure_person_id = personIds.get(i);
                        }
                    }
                }
            }
        }
        String status = getStatus(getData);     
        printRequest(requestNumber, request, pathList, acceptType, contentType, status, httpCode, response);
        
        // JSON
        acceptType = "application/json";
        getData = false;
        response = "";
        pathList = "";
        
        // Loop though person id's
        for (int i=0; i<personIds.size(); i++) {
            // Loop through measuretypes
            for (int j=0; j<measure_types.size(); j++) {
                String path = "/person/"+personIds.get(i)+"/"+measure_types.get(j);
                pathList += path + ";";
                Response result = service.path(path).request().accept(acceptType).get();
                int tempHttpCode = result.getStatus();
                if (tempHttpCode != 404 && tempHttpCode != 500) {
                    String jsonString = result.readEntity(String.class);
                    JSONArray jsonArray = new JSONArray(jsonString);    
                    // If response contains measures
                    if (jsonArray.length() > 0) {
                        // Add response xml to response string
                        String prettyJsonString = prettyJson(jsonString);
                        response += prettyJsonString;
                        // If data of one measure haven't been collected yet, collect it
                        if (!getData) {
                            getData = true;
                            httpCode = tempHttpCode;
                            contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
                            measure_id = String.valueOf(jsonArray.getJSONObject(0).getInt("mid"));
                            measure_type = String.valueOf(jsonArray.getJSONObject(0).getString("measure"));
                            measure_person_id = personIds.get(i);
                        }
                    }
                }
            }
        }
        status = getStatus(getData);        
        printRequest(requestNumber, request, pathList, acceptType, contentType, status, httpCode, response);
    }
    
    /*      R7: Get measure of specified measureType and person with measure id           */
    public void sendRequest7(WebTarget service) {
        int requestNumber = 7;
        String request = "GET";
        String path = "/person/"+measure_person_id+"/"+measure_type+"/"+measure_id;
        
        // XML
        String acceptType = "application/xml";
        Response result = service.path(path).request().accept(acceptType).get();
        int httpCode = result.getStatus();
        String contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String xmlString = result.readEntity(String.class);
        String status = getStatus(httpCode == 200);
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyXml(xmlString));
        
        // JSON 
        acceptType = "application/json";
        result = service.path(path).request().accept(acceptType).get();
        httpCode = result.getStatus();
        contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String jsonString = result.readEntity(String.class);
        status = getStatus(httpCode == 200);
        
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyJson(jsonString));
    }
    
    /*                  R8: Post new measure to measureTypes of specified person          */
    public void sendRequest8(WebTarget service) throws XPathExpressionException, ParserConfigurationException {
        int requestNumber = 8;
        String request = "POST";
        int httpCode = 0;
        String contentType = "";
        String status = "";
        String path = "/person/"+first_person_id+"/"+measure_types.get(0);
        
        // XML 
        String acceptType = "application/xml";
        Response getResult = service.path(path).request().accept(acceptType).get();
        String getXmlString = getResult.readEntity(String.class);
        
        Document getXml = xmlStringToDocument(getXmlString);
        XPathExpression getMeasures = xpath.compile("/measures/Measure");
        NodeList nodes = (NodeList) getMeasures.evaluate(getXml, XPathConstants.NODESET);
        int measuresBefore = nodes.getLength();
        
        // Create XML doc
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Measure");
        doc.appendChild(rootElement);
        
        Element value = doc.createElement("value");
        value.appendChild(doc.createTextNode("99.0"));
        rootElement.appendChild(value);
        
        Element created = doc.createElement("created");
        created.appendChild(doc.createTextNode("2001-12-09"));
        rootElement.appendChild(created);
        
        Response postResult = service.path(path).request().accept(acceptType).post(Entity.xml(doc));
        
        httpCode = postResult.getStatus();
        contentType = postResult.getMediaType() != null ? postResult.getMediaType().toString() : "null";
        String postXmlString = postResult.readEntity(String.class);
        
        Document postXml = xmlStringToDocument(postXmlString);
        XPathExpression getMeasureId = xpath.compile("/Measure/mid");
        // Save created measure id
        xml_created_measure_id = ((Node) getMeasureId.evaluate(postXml, XPathConstants.NODE)).getTextContent();
        
        // Send get request again
        getResult = service.path(path).request().accept(acceptType).get();
        getXmlString = getResult.readEntity(String.class);
        getXml = xmlStringToDocument(getXmlString);
        nodes = (NodeList) getMeasures.evaluate(getXml, XPathConstants.NODESET);
        
        // Save amount of measures
        int measuresAfter = nodes.getLength();
        status = getStatus(measuresAfter-measuresBefore==1);
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyXml(postXmlString));
        
        //JSON
        acceptType = "application/json";
        
        // Send get request and save amount of measures
        getResult = service.path(path).request().accept(acceptType).get();
        String getJsonString = getResult.readEntity(String.class);
        JSONArray jsonArray = new JSONArray(getJsonString);  
        measuresBefore = jsonArray.length();
        
        // Create JSON object
        JSONObject obj = new JSONObject();
        obj.put("created", "2011-12-09");
        obj.put("value", "72.0");
        
        // Send post request
        postResult = service.path(path).request().accept(acceptType).post(Entity.json(obj.toString()));
        
        httpCode = postResult.getStatus();
        contentType = postResult.getMediaType() != null ? postResult.getMediaType().toString() : "null";
        String postJsonString = postResult.readEntity(String.class);
        
        JSONObject jsonObj = new JSONObject(postJsonString);
        json_created_measure_id = String.valueOf(jsonObj.getInt("mid"));
        
        getResult = service.path(path).request().accept(acceptType).get();
        getJsonString = getResult.readEntity(String.class);
        jsonArray = new JSONArray(getJsonString);  
        measuresAfter = jsonArray.length();
        status = getStatus(measuresAfter-measuresBefore==1);
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyJson(postJsonString));
    }
    
    /*                  R9: Get measureTypes              */
    public void sendRequest9(WebTarget service) throws XPathExpressionException {
        int requestNumber = 9;
        String request = "GET";
        String path = "/measureTypes";
        
        // XML
        String acceptType = "application/xml";
        Response result = service.path(path).request().accept(acceptType).get();
        int httpCode = result.getStatus();
        String contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String xmlString = result.readEntity(String.class);
        Document xml = xmlStringToDocument(xmlString);
        XPathExpression getMeasureTypes = xpath.compile("/measureTypes/measureType/measureName");
        NodeList nodes = (NodeList) getMeasureTypes.evaluate(xml, XPathConstants.NODESET);
        String status = getStatus(nodes.getLength()>2);
        
        // Add measureTypes to list
        measure_types = new ArrayList<String>();
        for (int i=0; i<nodes.getLength(); i++) {
            measure_types.add(nodes.item(i).getTextContent());
        }       
        
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyXml(xmlString));
        
        // JSON
        acceptType = "application/json";
        result = service.path(path).request().accept(acceptType).get();
        httpCode = result.getStatus();
        contentType = result.getMediaType() != null ? result.getMediaType().toString() : "null";
        String jsonString = result.readEntity(String.class);
        JSONArray jsonArray = new JSONArray(jsonString);  
        status = getStatus(jsonArray.length()>2);
        measure_types = new ArrayList<String>();
        for (int i=0; i<jsonArray.length(); i++) {
            measure_types.add(jsonArray.getJSONObject(i).getString("measureName"));
        }       
        
        printRequest(requestNumber, request, path, acceptType, contentType, status, httpCode, prettyJson(jsonString));
    }
    
    // Status
    public String getStatus(boolean isTrue) {
        if (isTrue) {
            return "OK";
        } else {
            return "ERROR";
        }
    }
    
    /*          Prettifies json string for printing          */
    public static String prettyJson(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        Object json = null;
        try {
            json = mapper.readValue(jsonString, Object.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String indentedJsonString = "";
        try {
            indentedJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return indentedJsonString;
    }
    
    /*          Prettifies xml string for printing       */
    public static String prettyXml(String xmlString) {
        try {
            Source xmlInput = new StreamSource(new StringReader(xmlString));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer(); 
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /*          Convert xml string to document            */
    public Document xmlStringToDocument(String xmlString) {
        DocumentBuilder db = null;
        Document doc = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xmlString));
        try {
            doc = db.parse(is);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    public void printRequest(int number, String httpMethod, String url, String acceptType, 
            String contentType, String status, int httpCode, String body) {
        System.out.println("");
        System.out.println("Request #" + number + ": " + httpMethod + " " + url + " Accept: " + acceptType.toUpperCase() + " Content-type: " + contentType.toUpperCase());
        System.out.println("=> Result: " + status);
        System.out.println("=> HTTP Status: " + httpCode);
        System.out.println(body);
        if (acceptType.contains("xml")) {
            outXml.println("");
            outXml.println("Request #" + number + ": " + httpMethod + " " + url + " Accept: " + acceptType.toUpperCase() + " Content-type: " + contentType.toUpperCase());
            outXml.println("=> Result: " + status);
            outXml.println("=> HTTP Status: " + httpCode);
            outXml.println(body);
        } else if (acceptType.contains("json")) {
            outJson.println("");
            outJson.println("Request #" + number + ": " + httpMethod + " " + url + " Accept: " + acceptType.toUpperCase() + " Content-type: " + contentType.toUpperCase());
            outJson.println("=> Result: " + status);
            outJson.println("=> HTTP Status: " + httpCode);
            outJson.println(body);
        }
    }
    
    private static URI getBaseURI() {
        return UriBuilder.fromUri(
                "https://immense-caverns-1125.herokuapp.com/sdelab").build();
    }