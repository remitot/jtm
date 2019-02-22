
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
* `400` if mandatory request parameter is empty
* `401` if authorization fails

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

### Restart Apache
Method: `POST` (preferrable) or `GET`

Url: `/api/restart`

Request params: none

Request body: none

Response statuses:
* `200`: success

Response body: none
#### Example
Request: `/api/restart`

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
      "id": "$R31+A32+$T33+H34+P35",
      "active": true,
      "application": "Application1",
      "host": "tomcat-server1.com",
      "ajpPort": 8009,
      "getHttpPortLink": "api/jk/get-http-port?host=tomcat-server1.com&ajp-port=8009"
    },
    {
      "id": "$R28+A29+$T37+H38+P39",
      "active": false,
      "application": "Application2",
      "host": "tomcat-server2",
      "ajpPort": 8109,
      "getHttpPortLink": "api/jk/get-http-port?host=tomcat-server2&ajp-port=8109"
    }
  ]
}
```
### Modify bindings: create and update by ajp port, delete
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
        "active": /boolean, optional, new field value/,
        "application": /string, optional, new field value/,
        "instance": /string, optional, new field value in format "host:ajpPort"/
      }
    }
  }
  ```
* create
  ```
  {
    "modRequestId": /string, mandatory, arbitrary value but unique over the entire list/,
    "modRequestBody": {
      "action": "create",
      "data": {
        "active": /boolean, optional, new field value (default "true")/,
        "application": /string, mandatory, new field value/,
        "instance": /string, optional, new field value in format "host:ajpPort"/
      }
    }
  }
  ```
* delete
  ```
  {
    "modRequestId": /string, mandatory, arbitrary value but unique over the entire list/,
    "modRequestBody": {
      "action": "delete",
      "id": /string, mandatory, reference to the existing binding id/
    }
  }
  ```
Any modification requests with different `action` values will be ignored.

Response statuses:
* `200`: success
* `400` if any `modRequestId` is either `null` or empty or not unique over the whole list
* `400` if an error occurs while parsing the JSON request body

Response body (`application/json; UTF-8`) normally consists of two lists:
* list of statuses corresponding to the list of connection modification requests,  where each element is a _modStatus_ consisting of a `modRequestId` (matching by the `modRequestId` values from the request) and a `modStatusCode`.
* list of connections after all modifications performed, analogous to the response of `/api/jk/list`
```
{
  "modStatusList": [ /array of modStatuses, of length same as list of modRequests/  
    {
      "modRequestId": /string, equals to one of modRequestId values from the request/,
      "modStatusCode": /integer, the values described below/
      "invalidFieldData": /JSON object, described below/
          /present only if the value of "modStatusCode" is 1, /
    },...
  ],
  "_list": [ 
    /contents equal to result of /api/jdbc/list, invoked after the modifications performed/
  ]
}
```
`modStatusCode` values:
* `0`: successful modification
* `1`: error: invalid field data (see description of `"invalidFieldData"` object below)
* `2`: error: `id` field is missing or empty
* `3`: error: no item found by such `id`
* `500`: error: unspecified server exception

If _all_ `modStatusCode` values of the `modStatusList` are `0`, then (and only then) all modifications succeeded, saved, and the `_list` element is present in the response, otherwise (if any error occurred), no modifications are actually performed, and the `_list` element is not present.

If the value of `modStatusCode` is `1` (invalid field data), a special object is present in the response, describing which fields exactly are invalid:
```
{
  /fieldName/: {
    "errorCode": /string, the error code/,
    "errorMessage": /string, optional, description of the error/,
  },...
}
```
#### Example
Request: `/api/jk/mod/ajp`
```
[
  {
    "modRequestId": "$R28+A29+$T37+H38+P39",
    "modRequestBody": {
      "action": "update",
      "id": "$R28+A29+$T37+H38+P39",
      "data": {
        "active": true,
        "application": "NewApplication2",
        "instance": "tomcat-server3.net:8209"
      }
    }
  },
  {
    "modRequestId": "$R31+A32+$T33+H34+P35",
    "modRequestBody": {
      "action": "delete",
      "id": "$R31+A32+$T33+H34+P35"
    }
  },
  {
    "modRequestId": "row-create-1",
    "modRequestBody": {
      "action": "create",
      "data": {
        "application": "Application3",
        "instance": "tomcat-server1.com:8009"
      }
    }
  }
]
```
Response: `200`
```
{
  "_list": [
    {
      "id": "$R32+A33+$T33+H34+P35",
      "active": true,
      "application": "Application3",
      "host": "tomcat-server1.com",
      "ajpPort": 8009,
      "getHttpPortLink": "api/jk/get-http-port?host=tomcat-server1.com&ajp-port=8009"
    },
    {
      "id": "$R28+A29+$T41+H42+P43",
      "active": true,
      "application": "NewApplication2",
      "host": "tomcat-server3.net",
      "ajpPort": 8209,
      "getHttpPortLink": "api/jk/get-http-port?host=tomcat-server3.net&ajp-port=8209"
    }
  ],
  "modStatusList": [
    {
      "modRequestId": "$R28+A29+$T37+H38+P39",
      "modStatusCode": 0
    },
    {
      "modRequestId": "row-create-1",
      "modStatusCode": 0
    },
    {
      "modRequestId": "$R31+A32+$T33+H34+P35",
      "modStatusCode": 0
    }
  ]
}
```
### Modify bindings: create and update by http port, delete
Method: `POST`

Url: `/api/jk/mod/http`

Works exactly the same as `/api/jk/mod/ajp` endpoint, except for the `instance` field having its port as **http** instead of ajp:
```
{
  "modRequestBody": {
    ...
    "instance": /string, optional, new field value in format "host:httpPort"/,
    ...
  }
}
```
The Apache Jk module lets only bind application paths to server instances by _ajp_ ports, not _http_. To do so, the server (requested to bind an application path to an instance by _http_ port) makes a subrequest to the target instance's JTM (if any) over _http_, and gets the _ajp_ port number of that instance as a subresponse. After that, the server is able to bind the application path to the known _ajp_ port. If there is no JTM deployed on the target instance, the server has no way to surely know the _ajp_ port, so the modRequest to `/api/jk/mod/http` fails. However, the modRequests to `/api/jk/mod/ajp` work regardless of JTMs, because they update the Apache's configuration directly.

Because the _delete_ modRequests do not specify a port, there is no difference which endpoint to use to delete the bindings.

### Get http port by ajp port
Makes a subrequest to the target instance's JTM (if any) over _ajp_, and gets the _http_ port number of that instance as a subresponse.

Method: `GET`

Url: `/api/jk/get-http-port`

Request params:
* `host`: string, mandatory, the target instance's host name
* `ajp-port`: string, mandatory, the target instance's _ajp_ port, for which to get the _http_ port

Request body: none
Response statuses:
* `200`: success
* `400` if mandatory request parameter is empty

Request body (`text/plain; UTF-8`):
```
{
  "status": /string, status of the ajp subrequest-subresponse, the values described below/,
  "httpPort": /integer, the http port/
      /present only if the value of "status" is "SUCCESS"/
}
```
The `status` values are:
* `SUCCESS`: successful subrequest
* `UNKNOWN_HOST@@{url}`: unknown host (in the ajp-subrequested `{url}`)
* `CONNECT_EXCEPTION@@{url}`: _probably_ the non-working port (in the ajp-subrequested `{url}`)
* `SOCKET_EXCEPTION@@{url}`: _probably_ the port is working, but not over the _http_ protocol (in the ajp-subrequested `{url}`)
* `CONNECT_TIMEOUT@@{url}`: _probably_ the port is working, but not over the _http_ protocol (in the ajp-subrequested `{url}`)
* `UNSUCCESS_STATUS@@{status_code}@@{url}`: the ajp-subrequest to the `{url}` made a subresponse with the unsuccess `{status_code}` (the values are common HTTP statuses)

#### Example
Request: `/api/jk/get-http-port?host=tomcat-server1.com&ajp-port=8009`

Response: `200`
```
{
  "status": "SUCCESS",
  "httpPort": 8080
}
```
