
# API
## General
### Login
Method: `POST` (preferrable) or `GET`

Url: `/api/login`

Request params:
* `username`: string, mandatory
* `password`: string, mandatory

Request body: none
Response statuses:
* `200`: success
* `400`: if mandatory request parameter is empty
* `401`: if authorization fails

Response body: none
#### Example
Request: `/api/login?username=admin&password=admin`

Response: `200`

### Logout
Method: `POST` (preferrable) or `GET`

Url: `/api/logout`

Request params: none

Request body: none

Response statuses:
* `200`: success

Response body: none
#### Example
Request: `/api/logout`

Response: `200`

## Jk
Interface to the Apache's `mod_jk` module
### Get list of bindings
Method: `GET`

Url: `/api/jk/list`

Request params: none

Request body: none

Response statuses:
* `200`: success

Response body (`application/json; UTF-8`): 
```
{
  "_list": [
    {
      "id": /string, identificator of the binding/,
      "active": /boolean, whether the binding is commented in internal configuration files/,
      "application": /string, application that is bound to a Jk worker/,
      "host": /string, host of the worker that the application is bound to/,
      "ajpPort": /integer, ajp port number on the host/,
      "getHttpPortLink": /string, link to request the host's http port with, using the JTM on that host/ 
          /the field is normally passed from server to client only/
          /the field value may be constructed on the client-side from other field values (host, ajpPort)/
    },...
  ]
}
```
#### Example
Request: `/api/jk/list`

Response: `200`
```
{
  "_list": [
    {
      "id": "$R21+A22+$T4+H5+P6",
      "active": true,
      "application": "Application1",
      "host": "tomcat-server1.com",
      "ajpPort": 8009,
      "getHttpPortLink": "api/jk/get-http-port?host=tomcat-server1.com&ajp-port=8009"
    },
    {
      "id": "$R24+A25+$T8+H9+P10",
      "active": true,
      "application": "Application2",
      "host": "tomcat-server2",
      "ajpPort": 8010,
      "getHttpPortLink": "api/jk/get-http-port?host=tomcat-server2&ajp-port=8010"
    }
  ]
}
```
### Modify bindings: create and update by `ajp` port, delete
Method: `POST`

Url: `/api/jk/mod/ajp`

Request params: none

Request body (`application/json; UTF-8`): list of connection modification requests (modRequests), where each element is a _modification request_ (described below):
```
[
  {
    /modification request as described below/
  },...
]
```
Modification request is an element consisting of a `modRequestId` and a `modRequestBody` and representing one of the three actions:
* update
  ```
  {
    "modRequestId": /string, mandatory, arbitrary value but unique over the entire list/,
    "modRequestBody": {
      "action": "update",
      "id": /string, mandatory, reference to the existing binding id/,
      "data": {
# stopped here
        "active": /boolean, optional, new field value/,
        "name": /string, optional, new field value/,
        "server": /string, optional, new field value/,
        "db": /string, optional, new field value/,
        "user": /string, optional, new field value/,
        "password": /string, optional, new field value/
      }
    }
  }
  ```
