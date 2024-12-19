import base64
import json
import smtplib
import time
import uuid
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

import jwt
import requests
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import rsa

# Ethereal email credentials
smtp_host = "smtp.ethereal.email"
smtp_port = 587
username = "bret.schaden@ethereal.email"
password = "hJZsaTbZtSQ8cDcB1H"

# Email details
sender_email = "bret.schaden@ethereal.email"
receiver_email = "john.doe@example.com"
subject = "FHIR test results"

# Create the email
message = MIMEMultipart()
message["From"] = sender_email
message["To"] = receiver_email
message["Subject"] = subject

# FHIR
token_url = "https://fhir.epic.com/interconnect-fhir-oauth/oauth2/token"
client_id = "<PLACEHOLDER>"
group_id = "<PLACEHOLDER>"
fhir_base_url = "https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4"
bulk_request_url = f"{fhir_base_url}/Group/{group_id}/$export?_type=patient,observation&_typeFilter=Observation%3Fcategory%3Dlaboratory"

# Function to decode base64url to bytes
def base64url_decode(base64url):
    # Replace base64url characters to base64 standard
    padding = '=' * (4 - len(base64url) % 4)
    base64url = base64url.replace('-', '+').replace('_', '/') + padding
    return base64.b64decode(base64url)


def extract_observation_value_quantity(data):
    value_quantity = data.get("valueQuantity", {})
    value = value_quantity.get("value")
    return value


def extract_observation_low_high(data):
    low_value = None
    high_value = None

    reference_range = data.get("referenceRange", [])
    if reference_range:
        range_entry = reference_range[0] if len(reference_range) > 0 else {}
        low = range_entry.get("low", {})
        low_value = low.get("value")
        high = range_entry.get("high", {})
        high_value = high.get("value")
    return low_value, high_value


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
print(f"jwt_token:\n{token}")
print()

jwt_post_headers = {
    'Content-Type': 'application/x-www-form-urlencoded'
}

data = {
    'grant_type': 'client_credentials',
    'client_assertion_type': 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
    'client_assertion': token
}

response = requests.post(url=token_url, headers=jwt_post_headers, data=data).json()
access_token = response['access_token']
print(f"access_token:\n{access_token}")
print()

# initiate bulk request
bulk_init_headers = {
    'Authorization': f"Bearer {access_token}",
    'Accept': 'application/fhir+json',
    'Prefer': 'respond-async'
}

bulk_init_response = requests.get(url=bulk_request_url, headers=bulk_init_headers)

if bulk_init_response.status_code == 202:
    location_url = bulk_init_response.headers.get('Content-Location')
    if location_url:
        print(f"Polling URL: {location_url}")
        poll_headers = {
            'Authorization': f"Bearer {access_token}"
        }
        while True:
            poll_response = requests.get(location_url, headers=poll_headers)
            if poll_response.status_code == 200:
                print()
                print("Received 200 OK. Here is the JSON response:")

                data = poll_response.json()
                print(json.dumps(data, indent=4))

                data_headers = {
                    'Authorization': f"Bearer {access_token}"
                }

                # processing data
                messages = []
                for item in data.get("output", []):
                    resource_type = item["type"]
                    api_url = item["url"]
                    print(f"Processing `{resource_type}` data from `{api_url}`")
                    data_response = requests.get(api_url, headers=data_headers, stream=True)

                    if data_response.status_code == 200:
                        i = 1
                        for line in data_response.iter_lines():
                            if line:
                                json_object = json.loads(line.decode('utf-8'))

                                # save the JSON object to a file
                                filename = f"{resource_type}_{i}.json"
                                with open(filename, "w") as file:
                                    json.dump(json_object, file, indent=4)
                                print(f"Saved {resource_type} # {i}")
                                i = i + 1

                                if resource_type == 'Observation':
                                    low, high = extract_observation_low_high(json_object)
                                    value = extract_observation_value_quantity(json_object)
                                    text = json_object.get("code", {}).get("text", {})

                                    normal_observation =(value is not None) and (low is not None) and (high is not None) and low <= value <= high
                                    one_message = f"Observation '{text}' is {'normal' if normal_observation else 'abnormal'}"
                                    messages.append(one_message)

                    else:
                        print(f"Failed to fetch {resource_type} data. HTTP Status Code: {response.status_code}")

                # Send the email
                try:
                    if len(messages) == 0:
                        print(f"Nothing to send.")
                    else:
                        email_body = "\n".join(messages)
                        message.attach(MIMEText(email_body, "plain"))

                        with smtplib.SMTP(smtp_host, smtp_port) as server:
                            server.starttls()
                            server.login(username, password)
                            server.sendmail(sender_email, receiver_email, message.as_string())
                            print("Email sent successfully!")

                except Exception as e:
                    print(f"Failed to send email: {e}")


                break
            else:
                print(f"Waiting... Current status: {poll_response.status_code}")
                if poll_response.status_code == 202:
                    print(f"Progress: {poll_response.headers.get('X-Progress')}")
                time.sleep(5)
    else:
        print("Content-Location header is missing in the response.")

else:
    print(f"Initial request failed with status: {bulk_init_response.status_code}")
    print(f"body: {json.dumps(json.loads(bulk_init_response.text), indent=4)}")