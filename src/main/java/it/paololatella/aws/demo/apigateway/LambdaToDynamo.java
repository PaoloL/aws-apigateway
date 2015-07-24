package it.paololatella.aws.demo.apigateway;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.*;


public class LambdaToDynamo  {

	//Only for Test
	public static void main(String[] args)
	{
		//Devices device = new Devices ("00:0a:95:9d:68:17","arduino","ethernet");
		Devices device = new Devices ("00:0a:95:9d:68:17");
		Gson gson = new Gson();
		//Serialize
		String serialized = gson.toJson(device);
		System.out.println("Serialize: " + serialized);
		//Deserialize
        Devices new_device = gson.fromJson(serialized, Devices.class);
        System.out.print("De Serialize: " + new_device.toString('-'));
	}
	
	//insert a device to DynamoDB
	public static PutItemOutcome putDevice(Devices device)
	{
		//Create a connection to DynamoDB Services
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(Region.getRegion(Regions.EU_WEST_1)); 
		
		//Create a DynamoDB Object from connection
		DynamoDB dynamoDB = new DynamoDB(client);
		
		//get table reference
		Table table = dynamoDB.getTable("ApiGatewayDemo-Devices");
		
		//Build the item
		Item item = new Item()
		    .withPrimaryKey("Address", device.getAddress())
		    .withString("Type", device.getType())
		    .withString("Model", device.getModel());

		// Write the item to the table 
		PutItemOutcome outcome = table.putItem(item);
		return outcome;
	}
	
	//Scan all items on DynamoDB (not efficient)
	public static ScanResult getAllDevices()
	{
		//Create a connection to DynamoDB Services
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(Region.getRegion(Regions.EU_WEST_1)); 
		
		//Create a scan request
		ScanRequest scanRequest = new ScanRequest()
			    .withTableName("ApiGatewayDemo-Devices")
			    .withProjectionExpression("Address");
		ScanResult result = client.scan(scanRequest);
		return result;
		
	}
	
	//Get attribute of item of device
	public static Item getDevice(Devices device)
	{
		//Create a connection to DynamoDB Services
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(Region.getRegion(Regions.EU_WEST_1)); 
		
		//Create a DynamoDB Object from connection
		DynamoDB dynamoDB = new DynamoDB(client);
		
		//get Item with hash key
		Table table = dynamoDB.getTable("ApiGatewayDemo-Devices");
		Item item = table.getItem("Address", device.getAddress());
		return item;
	}
	
	//Delete attribute of item fo device
	public static DeleteItemOutcome deleteDevice(Devices device)
	{
		//Create a connection to DynamoDB Services
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(Region.getRegion(Regions.EU_WEST_1)); 
		
		//Create a DynamoDB Object from connection
		DynamoDB dynamoDB = new DynamoDB(client);
		
		//get Item with hash key
		Table table = dynamoDB.getTable("ApiGatewayDemo-Devices");
		DeleteItemOutcome outcome = table.deleteItem("Address", device.getAddress());
		return outcome;
		
	}
	
	/*
	 * Implement Handler for Lamda Function
	 * Any method of class is mapped to Lambda Handler
	 */
	
	//POST /devices
    public Map postDevicesHandler(Object body, Context context) {
    	//Make an instance for CloudWatch logs
    	LambdaLogger logger = context.getLogger();
    	
    	//Serialize Body
    	Gson gson = new Gson();
    	String json = gson.toJson(body,LinkedHashMap.class);

        logger.log("JAVA_INFO: Received " + json + "\n");
        
        //Deserialize in to Devices Object
    	Devices device = gson.fromJson(json, Devices.class);
    	PutItemOutcome result = putDevice(device);
    	
    	logger.log("device: " + device.toString(':') + " inserted on dynamodb");
    	
    	//Create output message
    	Map<String, String> message = new HashMap<String, String>();
    	message.put("message", "ok");
    	return message;
    }
    
    //GET /devices
    public ScanResult getDevicesHandler(Object body, Context context) {
    	LambdaLogger logger = context.getLogger();
    	logger.log("JAVA_INFO: Received " + body.toString() + "\n");
    	return getAllDevices();
    	
    }
    
    //GET /devices/{address}
    public Map getDevicesAddressHandler(Object body, Context context) {
    	LambdaLogger logger = context.getLogger();
    	
    	//The {address} parameter is in the body of API Gateway Request by Input Mapping
    	//Serialize Body
    	Gson gson = new Gson();
    	String json = gson.toJson(body,LinkedHashMap.class);

        logger.log("JAVA_INFO: Received " + json + "\n");
        
        //Deserialize in to Devices Object
    	Devices device_in = gson.fromJson(json, Devices.class);
    	logger.log("JAVA_INFO: Retrieve info for " + device_in.getAddress());
    	
    	String device_out = getDevice(device_in).toJSONPretty();
    	
    	//Create output message
    	Map<String,String> map_return = new HashMap<String,String>();
    	ObjectMapper mapper = new ObjectMapper();
    	
    	//convert JSON string to Map
    	try 
    	{
			map_return = mapper.readValue(device_out, new TypeReference<HashMap<String,String>>(){});
			logger.log("JAVA_INFO: found " + device_out);
		} 
		catch (IOException e) 
    	{
			logger.log("JAVA EXCEPTION:" + e.getMessage());
		}
    	return map_return;
    }
    
    //PUT /devices/{address}
    public Map putDevicesAddressHandler(Object body, Context context) {
    	//Make an instance for CloudWatch logs
    	LambdaLogger logger = context.getLogger();
    	
    	//Serialize Body
    	
    	Gson gson = new Gson();
    	String json = gson.toJson(body,LinkedHashMap.class);

        logger.log("JAVA_INFO: Received " + json + "\n");
        
        //Deserialize in to Devices Object
    	Devices device = gson.fromJson(json, Devices.class);
    	PutItemOutcome result = putDevice(device);
    	
    	logger.log("device: " + device.toString(':') + " inserted on dynamodb");
    	
    	//Create output message
    	Map<String, String> message = new HashMap<String, String>();
    	message.put("message", "ok");
    	return message;
    }
    
    public Map deleteDevicesAddressHandler(Object body, Context context) {
    	//Make an instance for CloudWatch logs
    	LambdaLogger logger = context.getLogger();
    	
    	//Serialize Body
    	
    	Gson gson = new Gson();
    	String json = gson.toJson(body,LinkedHashMap.class);

        logger.log("JAVA_INFO: Received " + json + "\n");
        
        //Deserialize in to Devices Object
    	Devices device = gson.fromJson(json, Devices.class);
    	DeleteItemOutcome result = deleteDevice(device);
    	
    	logger.log("device: " + device.toString(':') + " deleted from dynamodb");
    	
    	//Create output message
    	Map<String, String> message = new HashMap<String, String>();
    	message.put("message", "ok");
		return message;
    }
}
