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
- Set `micronaut.server.client-address-header` (e.g. `X-Forwarded-For`) and have the proxy overwrite it. The Minecraft IP check uses it.
- Set `micronaut.server.host-resolution.protocol-header: X-Forwarded-Proto` so the OAuth `redirect_uri` is `https://`.
- Don't use `micronaut.server.context-path`, the OAuth login/callback routes ignore it. Strip the prefix in the proxy instead and also proxy `/oauth/` to Staff-Auth.
