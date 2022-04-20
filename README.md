# dp-static-files-api-client-java
A Java Client Library for the dp-files-api (https://github.com/ONSdigital/dp-files-api)

## Usage
### Including in your project
This is available as a Maven/Gradle dependency from jitpack.io. See https://jitpack.io/#onsdigital/dp-static-files-api-client-java/

Add this to your dependencies in pom.xml:
```xml
    <dependency>
	    <groupId>com.github.onsdigital</groupId>
	    <artifactId>dp-static-files-api-client-java</artifactId>
	    <version>Tag</version>
	</dependency>
```

if you are not already using jtipack as a maven repo, also add the following to your pom file:
```xml
    <repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
    </repositories>
```

Note that the library is compile with and for Java 8.


### Usage in code
To instatiate the API client, two parameters are needed:
- the URL of the DP-Files API being used
- a (long-lived) authentication token used to authenticate your calls

```java
import com.github.onsdigital.dp.files.api.APIClient;
import com.github.onsdigital.dp.files.api.Client;

        
String filesApiURL = "http://apis.mydomain.com";
String serviceAuthToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9";
Client filesApiClient = new APIClient(filesApiURL, serviceAuthToken);
```
Note that the creation of the client does not validate the parameters, and no attempt to connect to the API endpoint is made on construction.

Then there is one method exposed - publish all files in a collection
```java
String collectionId = "dataset-reports-12345678";
filesApiClient.publishCollection(collectionId);
```

There is no return value (i.e. void) - if there is an error, then one of the following (runtime) Exceptions will be thrown. 
Note that the publishing of files in a collection is atomic - either all files associated with that 
collectionId will be successfully published, or none of the files managed by the files service in 
that collection will be published.
- `FileInvalidStateException`
    - At least one of the files in the collection is in a state that is not eleigible for publishing. Note that this response is used if any of the files in the collection are already published.
- `NoFilesInCollectionException.java`
    - There no files managed by the Files API that are associated with the collection identified by the provided collection id. This might not be an error for your process - it could be that all elements of a collection are managed in other publishing sources, and this exception is informational for you
- `IllegalArguementException`
  - When the provided collectionId is not valid (e.g. empty string) 
- `UnauthorizedException.java`
    - The provided auth token was not acceptable to the authorization service being used by the Files API. It may be that the token has expired, the used does not have the right to publish files, or the token is invalid or corrupted
- `UnexpectedResponseException`
  - The API responded with an unexpected response to the request, and the API client cannot interpreted. It is likely that the files in this collection have not been published, but that should be verified
- `ServerErrorException`
    - The File API had an unexpected issue when attempting to publish the files. It is likely that the files in this collection have not been published, but that should be verified
- `ConnectionException`
    - There was a networking exception when trying to connect to the provided endpoint URL. Please verify the endpoint being used, and the status of the Files API endpoint.

