import sys, datetime, hashlib, hmac
import urllib3
import os
import json
from botocore.credentials import ReadOnlyCredentials
from types import SimpleNamespace
from argparse import RawTextHelpFormatter
from argparse import ArgumentParser
import boto3
import botocore
import requests
from requests_auth_aws_sigv4 import AWSSigV4

client          = boto3.client('lambda')
hl_client       = boto3.client("healthlake")
regionAHL       = os.environ.get('AWS_REGION')
regionMapper    = os.environ.get('MAPPER_REGION')
endpoint        = os.environ.get('AHL_URL')
method          = os.environ.get('API_METHOD')
service         = os.environ.get('SERVICE')
serviceMapper   = os.environ.get('API_OPERATION')
host            = hl_client.meta.endpoint_url.replace('https://','',1)
content_type    = 'application/json'
access_key      = os.environ.get('AWS_ACCESS_KEY_ID')
secret_key      = os.environ.get('AWS_SECRET_ACCESS_KEY')
session_token   = os.environ.get('AWS_SESSION_TOKEN')
session = boto3.session.Session(region_name=regionAHL)


auth = AWSSigV4(service,session=session)
# Replace region and healthlake_url with your solutions method for specifying these values
def lambda_handler(event, context):
    service = os.environ['SERVICE']
    region = os.environ['AWS_REGION']
    datastoreid = os.environ['DATASTOREID']
    healthlake_url = "https://"+service+"."+region+".amazonaws.com/datastore/"+datastoreid+"/r4/"
    print("healthlake_url "+healthlake_url)
    print(event['entry'][4]['resource']['id'])
    
    # Create the DocumentReference with encounter specific parameters
    #jsonDocRef = createDocRef( docText, transcriptionTime, subject, encounter, encounterStartTime, encounterEndTime, serviceProvider, serviceProviderDisplay, practID, practDisplay)
    queryString = "subject="+event['entry'][4]['resource']['id']
    # POST to HealthLake
    hldocrefendpoint = healthlake_url + 'DocumentReference?'+queryString
    print(hldocrefendpoint)
    headers = {
            'Content-Type': content_type,
            'Accept': '*/*',
            'Host': host,
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
              }
    request_result = requests.get(hldocrefendpoint,headers=headers,auth=auth)
    print("STATUS_CODE:{}".format(request_result.status_code))
    print("out"+ format(request_result.text))
    return json.loads(request_result.text)
     

    