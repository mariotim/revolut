# BankAccount Transaction REST API
Home assignment for creating RESTful API to handle money operations for a bank client


## Run the app

    Running main() function in class `BankAccountManagerApp` will launch the app with embedded server. 

## Run the tests

   mvn clean test

# REST API

The REST API to the example app is described below.

## Create a client

### Request

`PUT /create/{email}`

### Response

  * HttpStatus: 201 Created: New Client created 
  * HttpStatus: 409 Conflict: Given email already exist
  
#### Example
`curl -i -H 'Accept: application/json' -X PUT http://localhost:8080/create/marat@timergaliev.com`  

    HTTP/1.1 201 Created
    Content-Length: 0`
    
    HTTP/1.1 409 Conflict
    Content-Length: 61
    Content-Type: application/json; charset=UTF-8
    {
        "error" : "Client marat@timergaliev.com already exist."
    }


## Request balance

### Request

`GET /balance/{email}`

### Response

  * HttpStatus: 200 OK
  * HttpStatus: 404 Not Found: Client with email doesn't exist
  
#### Example
` curl -i -H 'Accept: application/json' http://localhost:8080/balance/marat@timergaliev.com`  

    HTTP/1.1 200 OK
    Content-Length: 19
    Content-Type: application/json; charset=UTF-8
    
    {
      "balance" : 0
    }
    
    
    HTTP/1.1 404 Not Found
    Content-Length: 66
    Content-Type: application/json; charset=UTF-8
    
    {
      "error" : "Client no_marat@timergaliev1.com does not exist."
    }
