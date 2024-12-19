import base64
import json
import time
import uuid

import jwt
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import rsa


token_url = "https://fhir.epic.com/interconnect-fhir-oauth/oauth2/token"
client_id = "<PLACE_HOLDER>"

# Function to decode base64url to bytes
def base64url_decode(base64url):
    # Replace base64url characters to base64 standard
    padding = '=' * (4 - len(base64url) % 4)
    base64url = base64url.replace('-', '+').replace('_', '/') + padding
    return base64.b64decode(base64url)


# Load JWKS (keys.json)
with open('keys.json', 'r') as f:
    jwks = json.load(f)

# Extract the key from the JWKS
key_data = jwks['keys'][0]  # Assuming the first key is the one to use

# Decode the RSA key components from base64url
n = base64url_decode(key_data['n'])  # Modulus
e = base64url_decode(key_data['e'])  # Exponent
d = base64url_decode(key_data['d'])
p = base64url_decode(key_data['p'])
q = base64url_decode(key_data['q'])
dp = base64url_decode(key_data['dp'])
dq = base64url_decode(key_data['dq'])
qi = base64url_decode(key_data['qi'])

# Convert the modulus (n) and exponent (e) to integers
n_int = int.from_bytes(n, byteorder='big')
e_int = int.from_bytes(e, byteorder='big')
d_int = int.from_bytes(d, byteorder='big')
p_int = int.from_bytes(p, byteorder='big')
q_int = int.from_bytes(q, byteorder='big')
dp_int = int.from_bytes(dp, byteorder='big')
dq_int = int.from_bytes(dq, byteorder='big')
qi_int = int.from_bytes(qi, byteorder='big')

# Create the RSA public key from the modulus and exponent
public_key = (rsa.RSAPublicNumbers(e_int, n_int).public_key(default_backend()))

# Construct the private key
private_key = rsa.RSAPrivateNumbers(
    p=p_int,
    q=q_int,
    d=d_int,
    dmp1=dp_int,
    dmq1=dq_int,
    iqmp=qi_int,
    public_numbers=rsa.RSAPublicNumbers(e_int, n_int)
).private_key()

time_now = int(time.time())
time_5min_from_now = time_now + 300

# JWT payload
payload = {
    "iss": client_id,
    "sub": client_id,
    "aud": token_url,
    "jti": str(uuid.uuid4()),
    "exp": time_5min_from_now,
    "nbf": time_now,
    "iat": time_now
}

token = jwt.encode(payload, private_key, algorithm='RS384')
print(f"JWT: {token}")
