# Keycloak Token Retrieval Sample

This sample shows how to request a client credentials token from the local Keycloak instance running on port `8089`.

## Request

```
POST http://localhost:8089/realms/master/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&
client_id=mystery&
client_secret=Hx7NtmbsYY31X2RjtPd1NFFlxDh1vSsV&
scope=openid email profile
```

## Response

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJJaW41ZVhBRlp5RENKaTBhWkE1WWhZR2FPbjhSSXdkME5XZXkxZkpfczl3In0....",
  "expires_in": 60,
  "refresh_expires_in": 0,
  "token_type": "Bearer",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJJaW41ZVhBRlp5RENKaTBhWkE1WWhZR2FPbjhSSXdkME5XZXkxZkpfczl3In0....",
  "not-before-policy": 0,
  "scope": "openid profile email"
}
```

The `access_token` and `id_token` values above are truncated for readability; the actual values returned by Keycloak are longer JWT strings.
