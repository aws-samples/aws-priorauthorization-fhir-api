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
def lambda_handler(event, context):
     print("Received request: ")
     service = os.environ['SERVICE']
     region = os.environ['AWS_REGION']
     datastoreid = os.environ['DATASTOREID']
     healthlake_url = "https://"+service+"."+region+".amazonaws.com/datastore/"+datastoreid+"/r4/"
     print("healthlake_url "+healthlake_url)
     headers = {
            'Content-Type': content_type,
            'Accept': '*/*',
            'Host': host,
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
              }
    # POST to HealthLake
     hldocrefendpoint = healthlake_url + 'Bundle'
    
     payload = json.dumps(event)
    #print(context("Token"))
     print( "before AWS request")
     request_result = requests.post(hldocrefendpoint,data=payload,headers=headers,auth=auth)
     print("STATUS_CODE:{}".format(request_result.status_code))
     return json.loads(format(request_result.status_code))
