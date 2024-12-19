## Module-4 SMART backend application on EPIC

- Generate public/private key
```
openssl genrsa -out module-4-private-key.pem 2048
```
- Generate public key from private key
```
openssl rsa -pubout -in module-4-private-key.pem -out module-4-public-key.pem
```
- Generate public key certificate from private key
```
openssl req -new -x509 -key module-4-private-key.pem -out module-4-public-key-cert.pem -subj '/CN=myapp'
```
- Generate `keys.json`
```
python3 ./python/02-generate-keys.py
```
Remove the next fields from the `keys.json` - they are private info
```
- "d"
- "p"
- "q"
- "dp"
- "dq"
- "qi"
```

Deploy the `keys.json` on kubernetes
```
kk apply -f ./k8s/ns.yaml
kk apply -f ./k8s/
```

Check the keys url
```
$ curl -i https://local-fhir-patient-app.luminatehealth.com/jwks
HTTP/2 200 
date: Thu, 19 Dec 2024 04:05:33 GMT
content-type: application/json
content-length: 491
accept-ranges: bytes
etag: "67639aa9-1eb"
last-modified: Thu, 19 Dec 2024 04:01:45 GMT
cf-cache-status: DYNAMIC
server: cloudflare
cf-ray: 8f4483d0bf65e602-IAD

{
  "keys": [
    {
      "kty": "RSA",
      "kid": "key-0",
      "use": "sig",
      "alg": "RS256",
      "n": "uG6vxt2MlnKOgcTu83TptU_DDRbfECf8rvetvK0NpMbdayX7qqqoBlZsT-lIsqjzbcbuzOhoJpIRncSTq5_2KfXcArJCuXj1Xw5ZFxfTazLVCZyZ6JTR2hsftP9mqs0OBXflzV_jQYaGWto5nN8Tb0BFiUvdwFhDj50jQcQ4O14kSkeBE8UfP5LmoNGKOkJ3MJgLgA8zUBQeGkzf38n735dPm4N_X4qdHDCkqi5YBW8TRSi9E5MQbZ77EdExtT_dcmYh-KCs6Os8149jaYNQUzc3GEb-PBMC1qrEzl_vNyGE1OR9LY_V1Isywm3pQT_czTLfm7wsQpS0pLiOFQ4PSw",
      "e": "AQAB"
    }
  ]
}

```

Run the script manually
```
python3 02-get-data-and-email.py
```

Use a cron to schedule the job every day at midnight
```
(crontab -l; echo "0 0 * * * python3 02-get-data-and-email.py") | crontab -
```
## Docs
- https://developers.yubico.com/PIV/Guides/Generating_keys_using_OpenSSL.html
- https://fhir.epic.com/Documentation?docId=oauth2&section=Backend-Oauth2_Creating-JWT
- https://hl7.org/fhir/r4/group.html