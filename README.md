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
` curl -i -H 'Accept: application/json' http://localhost:8080/balance/marat@timergaliev.com1`  

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
## Deposit

### Request

`POST /deposit/{email}/{balance}`

### Response

  * HttpStatus: 200 OK
  * HttpStatus: 404 Not Found: Client with email doesn't exist
  * HttpStatus: 400 Bad Request: Amount cannot be negative
  
#### Example
` curl -i -H 'Accept: application/json' -X POST http://localhost:8080/deposit/marat@timergaliev.com/100.0`  

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
    
    
    HTTP/1.1 400 Bad Request
    Content-Length: 63
    Content-Type: application/json; charset=UTF-8
    
    {
      "error" : "Illegal argument: balance cannot be negative."
    }

## Withdraw

### Request

`POST /withdraw/{email}/{balance}`

### Response

  * HttpStatus: 200 OK
  * HttpStatus: 404 Not Found: Client with email doesn't exist
  * HttpStatus: 400 Bad Request: Amount cannot be negative
  * HttpStatus: 403 Forbidden: Insufficient funds
  
#### Example
` curl -i -H 'Accept: application/json' -X POST http://localhost:8080/withdraw/marat@timergaliev.com/10.0`  

    HTTP/1.1 200 OK
    Content-Length: 0
    
    
    HTTP/1.1 404 Not Found
    Content-Length: 66
    Content-Type: application/json; charset=UTF-8
    
    {
      "error" : "Client no_marat@timergaliev1.com does not exist."
    }
    
    HTTP/1.1 403 Forbidden
    Content-Length: 36
    Content-Type: application/json; charset=UTF-8
    
    {
      "error" : "Insufficient funds"
    }
    
    

## Transfer

### Request

`POST transfer/{sender}/{receiver}/{balance}`

### Response

  * HttpStatus: 200 OK
  * HttpStatus: 404 Not Found: Client with email doesn't exist
  * HttpStatus: 400 Bad Request: Amount cannot be negative
  * HttpStatus: 403 Forbidden: Insufficient funds
  
#### Example
` curl -i -H 'Accept: application/json' -X POST http://localhost:8080/transfer/marat@timergaliev.com/100.0`  

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
    
    
    HTTP/1.1 400 Bad Request
    Content-Length: 63
    Content-Type: application/json; charset=UTF-8
    
    {
      "error" : "Illegal argument: balance cannot be negative."
    }
