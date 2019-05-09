# Jepria Tomcat Manager API

## Login
Method: `POST` or `GET`  
Url: `/login`  
Request params:
*   `username`: string, mandatory
*   `password`: string, mandatory

Request body: none  
Response statuses:
*   `200` success
*   `400` if mandatory request parameter is empty
*   `401` if authorization fails

Response body: none

### Example
Request: `/login?username=admin&password=admin`  
Response: `200`

## Logout
Method: `POST` or `GET`  
Url: `/logout`  
Request params: none  
Request body: none  
Response statuses:
*   `200` success

Response body: none

### Example
Request: `/logout` Response: `200`

## Get HTTP port number
The method is public (not protected by authorization) to provide access to Apache Manager and because the port information is easy to discover.

Method: `GET`  
Url: `/api/port/http`  
Request params: none  
Request body: none  
Response statuses:
*   `200` success
*   `500` if any error occurs (the server is requred to have its HTTP port defined)

Response body (`text/plain; charset=UTF-8`):
`/integer, port number/`

### Example
Request: `/api/port/http`  
Response: `200`
`8080`

## Get AJP port number
The method is public (not protected by authorization) to provide access to Apache Manager and because the port information is easy to discover.

Method: `GET`  
Url: `/api/port/ajp`  
Request params: none  
Request body: none  
Response statuses:
*   `200` success
*   `500` if any error occurs (the server is requred to have its AJP port defined)

Response body (`text/plain; charset=UTF-8`):
`/integer, port number/`

### Example
Request: `/api/port/ajp`  
Response: `200`
`8009`

## Get log file contents
Method: `GET`  
Url: `/api/log`  
Request params:
*   `filename`: string, mandatory, the existing log file name to get contents
*   `inline`: optional. If the parameter is present (or has any value), the response header `Content-Disposition: inline` will be set (thus, the client user agent will itself display the contents). If the parameter is not present, the response header `Content-Disposition: attachment; filename=_filename_` will be set (thus, the client user agent will ask to download the contents).

Request body: none  
Response statuses:
*   `200` success
*   `400` if either the `filename` request parameter is not present, or empty, or contains disallowed symbols `/` or `\` (tries to navigate the file system), or does not denote an existing log file (the existing files are listed by `/api/log/list`)

Response body (`text/plain UTF-8`):
`/file contents/`

### Example
Request: `/api/log?filename=localhost.log&inline`  
Response: `200`

    feb 16, 2019 3:43:40 PM org.apache.catalina.authenticator.SingleSignOn expire
    WARNING: SSO unable to expire session [Host: [*********], Context: [/*******], SessionID: [45185DA5E8693EACD937F160C3D13B37]] because the Session could not be found
    feb 16, 2019 3:43:44 PM org.apache.catalina.authenticator.SingleSignOn expire
    WARNING: SSO unable to expire session [Host: [*********], Context: [/*******], SessionID: [7A1BF0E2A900FDDF0766F37EFBE1DC10]] because the Session could not be found