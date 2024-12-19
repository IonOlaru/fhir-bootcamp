import json
from base64 import urlsafe_b64encode

from cryptography.hazmat.primitives.asymmetric import rsa


def to_base64url(data: bytes) -> str:
    """Encodes bytes to base64url format."""
    return urlsafe_b64encode(data).rstrip(b"=").decode("utf-8")


def generate_rsa_jwk(key_id="example-key-id"):
    """Generates an RSA key and formats it as a JSON Web Key (JWK)."""
    # Generate a private RSA key
    private_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)

    # Extract private and public key components
    numbers = private_key.private_numbers()
    public_numbers = numbers.public_numbers

    # Create the JWK
    jwk = {
        "kty": "RSA",
        "kid": key_id,
        "use": "sig",
        "alg": "RS256",
        "n": to_base64url(public_numbers.n.to_bytes(256, byteorder="big")),
        "e": to_base64url(public_numbers.e.to_bytes(3, byteorder="big")),
        "d": to_base64url(numbers.d.to_bytes(256, byteorder="big")),
        "p": to_base64url(numbers.p.to_bytes(128, byteorder="big")),
        "q": to_base64url(numbers.q.to_bytes(128, byteorder="big")),
        "dp": to_base64url(numbers.dmp1.to_bytes(128, byteorder="big")),
        "dq": to_base64url(numbers.dmq1.to_bytes(128, byteorder="big")),
        "qi": to_base64url(numbers.iqmp.to_bytes(128, byteorder="big")),
    }

    return jwk


def generate_jwks(num_keys=1):
    """Generates a JSON Web Key Set (JWKS) with the specified number of keys."""
    jwks = {"keys": [generate_rsa_jwk(f"key-{i}") for i in range(num_keys)]}
    return jwks


# Generate the JWKS
jwks = generate_jwks(num_keys=1)


# Write the JWKS to a file (keys.json)
with open("keys.json", "w") as file:
    json.dump(jwks, file, indent=4)


print("JWKS has been written to keys.json.")
