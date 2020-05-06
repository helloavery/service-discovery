# Service Discovery

Load balancer and feign client that leverages Consul.
Eliminates the need to have logic for HTTP clients to make API calls to external services while providing
a load balancing solution

# How it works
Registering services/endpoints:
* Service name, version number, and health check endpoint are defined via annotation which is event driven
* Service is created in Consul with the service version and endpoint stored in metadata

Client discovery:
* Create interface that has the rest endpoints of the external service
* Create bean (RestFeignClientBean object) of the interface that defines the desired service name and version
* When making a call to one of the endpoints, Service Discovery will check Consul for the service name and version
* Consul will check hosts status and only return healthy endpoints

Load Balancing:
* Handled by Consul via http health check
* Only returns services that are healthy

