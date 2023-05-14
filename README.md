# pokemon-go

It's very beginning of Alea's challenge. Please, read this README to get more information

## Prerequisites
- [Maven](https://maven.apache.org/) at least version 3.8.1 installed
- [JDK](https://www.oracle.com/java/technologies/javase-downloads.html) at least version 17 installed
- [Docker](https://docs.docker.com/engine/install/) installed

### Optional
- [GraalVM](https://docs.spring.io/spring-boot/docs/3.0.6/reference/html/native-image.html#native-image) at least version 22.3 for executable with Native Build Tools

## Get started

In this recruitment task, the challenge is to expose an API that provides information about Pokemon based on specific scenarios. However, the data source, PokéAPI, poses several challenges. Retrieving details such as weight, height, and base experience requires multiple HTTP calls, pagination handling, and processing a large amount of data. The goal is to overcome these obstacles and implement an API that showcases the five heaviest, highest, and Pokemon with the most base experience.

### Build & run project (Linux / MacOS)
- Build the project via maven client: `mvn clean install` or via maven wrapper: `./mvnw clean install`
- Build files are in `/target` directory
- Run application via maven client: `mvn spring-boot:run` or via maven wrapper: `./mvnw spring-boot:run`
- Make sure that port `8080` is released before launching application

### Build & run docker image
- Build the docker image via maven client: `mvn spring-boot:build-image -Pnative` or via maven wrapper: `./mvnw spring-boot:build-image -Pnative`
- Make sure docker image is build: `docker images | grep pokemon-go`
- Run docker container: `docker run -d -p [port]:8080 pokemon-go:0.0.1-SNAPSHOT`
- Make sure that port `[port]` is released before launching application

### Build & run GraalVM
- Make sure you have JDK GraalVM distribution installed
- Build the project via maven client: `mvn native:compile -Pnative` or via maven wrapper: `./mvnw native:compile -Pnative`
- Build files are in `/target` directory
- Run application via GraalVM JDK: `target/pokemon-go`
- Make sure that port `8080` is released before launching application

### API Documentation

Retrieves a list of Pokemons based on specified attributes.

**URL**

```
GET localhost:8080/v1/pokemons?attribute={attribute}&limit={limit}'
```

**Parameters**

- `attribute` (optional): Specifies the attribute to find the heaviest, highest, or with the most experience Pokemons. Possible values: `weight`, `height`, `base_experience`.
- `limit` (optional): Specifies the number of how many top Pokemons with the specified attribute are to retrieve.

**Example Request**

```bash
curl --location --request GET 'localhost:8080/v1/pokemons?attribute=weight&limit=5'
```

**Example Response**
```json
[
    {
        "id": 1,
        "name": "pikachu",
        "weight": 6,
        "height": 10,
        "base_experience": 8
    }
]
```

The response is a JSON array containing objects representing Pokemons. Each Pokemon object has the following properties:

- id (integer): The unique identifier of the Pokemon.
- name (string): The name of the Pokemon.
- weight (integer): The weight of the Pokemon.
- height (integer): The height of the Pokemon.
- base_experience (integer): The base experience of the Pokemon.

## Decision log:
- **Spring WebFlux framework** and **WebClient** integration for non-blocking asynchronous application in order to handle a large number of concurrent connections with minimal resource consumption. It's perfect choice for dealing with a high volume of API calls or when processing a large dataset (which is the case in PokéAPI)
- **Reactor Netty** library for non-blocking processing and API calls, taking advantage of its non-blocking nature of I/O operations and support for reactive programming, so it improves performance and the user experience
  - The Pokemon data is independent once we know their ids/urls, so we can query them without respecting order and asynchronously. Hence, we use Netty/WebClient to ensure asynchronous, non-blocking requests
  - The PokeAPI does not provide server-side data filtering, and the number of Pokemon attributes is large. Therefore, Netty is also a good choice for dealing with a high volume
  - The PokeAPI, which provides information about ids/urls for Pokémon characteristics, is pageable. However, it is possible to retrieve all results on a single page by setting an appropriate limit. This approach has its trade-offs. For example, we get all the necessary urls/ids in a single HTTP call. However, for a production solution, I believe it would be better to simultaneously fetch Pokémon characteristics data and ids/urls for the next results via performing pipeline processing. This way, in one cycle, we would receive the next batch of urls and simultaneously retrieve Pokémon characteristics data, avoiding a spike in memory usage
- **Caffeine** for caching PokéAPI responses, so we don't need to retrieve data over and over and application responses time has decreased significantly. The nature of pokemon data is that it doesn't change very often, so it's enough to evict the cache based on TTL (which is 1 hour now), but if there was some better mechanism for evicting the cache (like getting an event when data changes) it would be more reliable
- **Versioned API** (started with `/v1` version) in order to better management of the change (especially since the data source is 3rd party, so it's out of our control)
- **Nulls** are not being checked, because I assume that for PokéAPI all data we need is mandatory and populated
- **Lombok** library for avoiding boilerplate code
- **API documentation** is not provided since it's only one endpoint exposed (but it's described in the README). It would be recommended to use Swagger as an interactive documentation with possible auto-generation
- **One endpoint** to handle all the cases, so the API is simple, generic and easy to extend with new functionalities. Thanks to query params, the API client can request a selected number of results and without the need to change the current version of the API, you can add support for new attributes easily
- **Docker** for containerize application in order to launch in isolated environment and make the application production-ready

## TODO:
- Add some logs in order to monitor application
- Add error handling
- Do not store information in the cache when there is an API call error
- Apply circuit breaker pattern (e.g. Hystrix) since the service relies on the remote call to avoid 'catastrophic cascade'
- Change cache to the distributed one (e.g. Hazelcast) as it suits better to the type of data that is cached
