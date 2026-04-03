from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_v1_5
import base64
import requests
import json

# Get public key
resp = requests.get('http://localhost:8080/api/auth/public-key')
pub_key_b64 = resp.json()['data']['publicKey']
pub_key_der = base64.b64decode(pub_key_b64)
pub_key = RSA.import_key(pub_key_der)

# Encrypt
cipher = PKCS1_v1_5.new(pub_key)
enc_username = base64.b64encode(cipher.encrypt(b'testuser')).decode()
enc_password = base64.b64encode(cipher.encrypt(b'Test123456')).decode()

# Register
reg_resp = requests.post('http://localhost:8080/api/auth/register', json={
    'username': enc_username,
    'password': enc_password
})
print('Register:', reg_resp.json())

token = reg_resp.json()['data']['token']

# Login
enc_username2 = base64.b64encode(cipher.encrypt(b'testuser')).decode()
enc_password2 = base64.b64encode(cipher.encrypt(b'Test123456')).decode()
login_resp = requests.post('http://localhost:8080/api/auth/login', json={
    'username': enc_username2,
    'password': enc_password2
})
login_data = login_resp.json()
print('Login:', login_data['code'], '-', login_data['message'])

# Get price items
headers = {'Authorization': 'Bearer ' + token}
price_resp = requests.get('http://localhost:8080/api/price-items?sortOrder=asc', headers=headers)
price_data = price_resp.json()
print('Price items:', price_data['code'], '-', price_data['message'], '- Count:', len(price_data.get('data', [])))
