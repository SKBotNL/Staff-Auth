# Staff-Auth
An [Ory Hydra](https://github.com/ory/hydra) authentication and authorization provider.

> [!CAUTION]
> This server implements no rate limiting on its own, make sure rate limiting is configured in your reverse proxy, specifically on `/login`.

## Building
JVM: `./gradlew shadowJar`\
GraalVM Native: `./gradlew nativeCompile`

## Configuration
Copy [application.yml.example](application.yml.example), rename it to whatever you like, fill it in and run the server binary with `MICRONAUT_CONFIG_FILES=path/to/your/application.yml`.

## Initial setup
Run the server binary with `--initialSetup.adminUuid=your-uuid-here`.

## Reverse proxy
If Staff-Auth runs behind a reverse proxy:

- Set `micronaut.server.client-address-header` (e.g. `X-Forwarded-For`) and make the proxy overwrite that header; the
  Minecraft IP check reads the client address through Micronaut's `HttpClientAddressResolver`.
- Set `micronaut.server.host-resolution.protocol-header: X-Forwarded-Proto` (and `host-header: Host`) so the OAuth
  `redirect_uri` sent to Hydra uses the public `https://` origin instead of the backend's address.
- Do **not** use `micronaut.server.context-path` to mount the API under a prefix: Micronaut Security registers the
  `/oauth/login/{provider}` and `/oauth/callback/{provider}` routes programmatically and they ignore the context path
  (404). Strip the prefix in the proxy instead (nginx: `location /api/ { proxy_pass http://127.0.0.1:8081/; }`) and
  proxy `/oauth/` to Staff-Auth as well, since the callback URL Micronaut builds has no prefix.
