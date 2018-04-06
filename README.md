# customs-dit-licences

 [ ![Download](https://api.bintray.com/packages/hmrc/releases/customs-dit-licences/images/download.svg) ](https://bintray.com/hmrc/releases/customs-dit-licences/_latestVersion)

This service receives licence usage data from the backend system and passes it on to DIT-LITE. It accepts an XML payload but does not verify it.
If the XML is malformed then a Bad Request error is raised. The response received from the DIT-LITE service, error or otherwise, is returned unmodified to the backend system.
The request/response sequence from the backend to this service to DIT-LITE is synchronous.

Two endpoints are available. Both require an authorisation token.

### Licence Usage

Endpoint - 
```
/send-entry-usage
```


### Late Licence Usage

Endpoint - 
```
/send-late-usage
```


# Switching service endpoints

Dynamic switching of service endpoints has been implemented for the DIT-LITE connector. To configure dynamic
switching of the endpoint there must be a corresponding section in the application config file
(see example below). This should contain the endpoint config details.


## Example
The service `dit-lite-entry-usage` has a `default` configuration and a `stub` configuration. Note
that `default` configuration is declared directly inside the `dit-lite-entry-usage` section.

    Prod {
        ...
        services {
            ...
            dit-lite-entry-usage {
                host = some.host
                port = 80
                bearer-token = "real"
                context = /context
                
                stub {
                  host = localhost
                  port = 80
                  bearer-token = "some_stub_token"
                  context = /context
                }
            }
        }
    }
    
### Switch service configuration to stub for an endpoint

#### REQUEST
    curl -X "POST" http://customs-dit-licences-host/test-only/service/dit-lite-entry-usage/configuration -H 'content-type: application/json' -d '{ "environment": "stub" }'
    

#### RESPONSE

    The service dit-lite-entry-usage is now configured to use the stub environment


### Switch service configuration to default for an endpoint

#### REQUEST

    curl -X "POST" http://customs-dit-licences-host/test-only/service/dit-lite-entry-usage/configuration -H 'content-type: application/json' -d '{ "environment": "default" }'

#### RESPONSE

    The service dit-lite-entry-usage is now configured to use the default environment

### Get the current configuration for a service

#### REQUEST

    curl -X "GET" http://customs-dit-licences-host/test-only/service/dit-lite-entry-usage/configuration

#### RESPONSE

    {
      "service": "dit-lite-entry-usage",
      "environment": "stub",
      "url": "http://currenturl/send-entry-usage"
      "bearerToken": "current token"
    }


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
