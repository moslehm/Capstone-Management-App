import socket
import datetime
import sys
import requests
import json
import firebase_admin
import datetime
from firebase_admin import messaging
from firebase_admin import credentials
from oauth2client.service_account import ServiceAccountCredentials

SCOPES = ['https://www.googleapis.com/auth/firebase.messaging']
with open('server_token.secret', 'r') as file:
    server_token = file.read().rstrip()

def get_token():
    server_token = ServiceAccountCredentials.from_json_keyfile_name('firebase_service_key.json', SCOPES).get_access_token().access_token

def construct_payload(json_data):
    mTitle = "Default title"
    mBody = "Default body"
    topic = "Default"
    topicA = ""
    topicB = ""
    topicC = ""
    device_token = ""

    if "title" in json_data:
        mTitle = json_data["title"]
    
    if "body" in json_data:
        mBody = json_data["body"]

    if "topicA" in json_data:
        topicA = json_data["topicA"]

    if "topicB" in json_data:
        topicB = json_data["topicB"]

    if "topicC" in json_data:
        topicC = json_data["topicC"]

    if "device_token" in json_data:
        device_token = json_data["device_token"]

    conditionString = ""
    
    if topicA:
        conditionString = f"'{topicA}' in topics"

        if topicB:
            conditionString += f" && '{topicB}' in topics"
        
        if topicC:
            conditionString += f" && '{topicC}' in topics"

    print(conditionString)

    if conditionString != "":
        payload = messaging.Message(
            data={ "sender":device_token },
            notification=messaging.Notification(
                mTitle,
                mBody
            ),
            android=messaging.AndroidConfig(
                ttl=datetime.timedelta(seconds=3600),
                priority='normal',
            ),
            condition=conditionString,
        )
    elif device_token:
        payload = messaging.Message(
            notification=messaging.Notification(
                mTitle,
                mBody
            ),
            android=messaging.AndroidConfig(
                ttl=datetime.timedelta(seconds=3600),
                priority='normal',
            ),
            token=device_token,
        )
    else:
        payload = messaging.Message(
            notification=messaging.Notification(
                mTitle,
                mBody
            ),
            android=messaging.AndroidConfig(
                ttl=datetime.timedelta(seconds=3600),
                priority='normal',
            ),
            topic='test',
        )

    return payload

    

def main():
    firebase_cred = credentials.Certificate("firebase_service_key.json")
    app = firebase_admin.initialize_app(firebase_cred)
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    server_address = ('10.138.0.2', 10000)
    print(sys.stderr, 'starting up on %s port %s' % server_address)
    sock.bind(server_address)


    sock.listen(10)

    while True:
        print ('waiting for a connection')
        connection, client_address = sock.accept()

        try:
            print('connection from', client_address)

            while True:
                raw_data = connection.recv(4096)

                if (isinstance(raw_data, bytes)):
                    data = raw_data.decode('utf-8')
                else:
                    data = raw_data

                if (data != '' and isinstance(data, str)):
                    print(data)
                    json_data = json.loads(data)
                    print(f'{json_data}')
                    if json_data:
                        message = construct_payload(json_data)
                        response = messaging.send(message)
                        print(f"Response: {response}")
                
                try:
                    s.send("send some data")
                except:
                    break

        except ConnectionResetError:
            pass

        finally:
            connection.close()

if __name__ == '__main__':
    main()

