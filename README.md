# aws-apigateway

Amazon API Gateway is a fully managed service that makes it easy for developers to create, publish, maintain, monitor, and secure APIs at any scale. Is API Gateway a RESTaaS (REST as a Service) ? Probably yes! because API Gateway with AWS Lambda and AWS DynamoDB permit to build a full REST Service. API Gateway is our HTTP/HTTPS gateway, in Lambda we have our business logic, without managing servers, and on Dynamo we have our data (no-sql data). I will try to describe the features and power of AWS API Gateway through the integration with Arduino devices, AWS Lambda and DynamoDB. Arduino collect some metrics from environment (temperature, pressure, position, etc) and send it to API Gateway by a HTTP call. API Gateway invoke a Lambda function that collect and transform info before to send to DynamoDB. All event will be tracked on Cloudwatch Logs. The metrics collected to DynamoDB will be then available for any Web Application able to interact with API Gateway via REST call.

![alt tag](http://www.xpeppers.com/wp-content/uploads/2015/07/ResourcesAndMethods.png)

## Lambda Handler

I used a single package for all Lambda functions. Infact the package it.paololatella.aws.demo.apigateway  contain Devices class, Metrics class and LambdaToDynamo class. The first class implement a device (Arduino for example) object, the second class implement a metric object (position for example) and finally the LambdaToDynamo class is a main class with the handler specified above

* ApiGatewayGetDevice Lambda function use the it.paololatella.aws.demo.apigateway.LambdaToDynamo::getDevicesHandler Handler. This handler receive the request from GET /devices and list all devices resource

* ApiGatewayPostDevice Lambda function use the it.paololatella.aws.demo.apigateway.LambdaToDynamo::postDevicesHandler Handler. This handler receive the request from POST /devices and create a new device resource

* ApiGatewayGetDevicesAddress Lambda function use the it.paololatella.aws.demo.apigateway.LambdaToDynamo::getDevicesAddressHandler Handler. This handler receive the request from GET /devices/{address} request and return data about a specific device

* ApiGatewayPutDevicesAddress Lambda function use the it.paololatella.aws.demo.apigateway.LambdaToDynamo::putDevicesAddressHandler Handler. This handler receive the request from PUT /devices/{address} request and update data about a specific device

* ApiGatewayDeletDevicesAddress Lambda function use the 
it.paololatella.aws.demo.apigateway.LambdaToDynamo::deleteDevicesAddressHandler Handler. This handler receive the request from DELETE /devices/{address} request and delete data about a specific device

##Input mapping 

In the above screenshot we can see how we map the address of the device, passed by a path parameter on PUT /devices/{address}, in the request body. We map address by this mapping model.

```
#set($inputRoot = $input.path(‘$’))
{
“address” : “$input.params(‘address’)”,
“model” : “$inputRoot.model”,
“type” : “$inputRoot.type”
}
```

With this model we have “address” key on value returned by $input.params(‘address’) therefore on address passed by path parameters. Instead “model” key and “type” key will be mapped by $inputRoot.model and $inputRoot.type therefore on model and type passed on body request. The $input.params(‘address’) retrieve the address of device from url and map it to correspondent key present on body request. Great right ?? Now we can test mapping model directly on AWS API Gateway console.

More info here: http://www.xpeppers.com/2015/07/26/demo-with-api-gateway-lambda-dynamodb-and-arduino/
