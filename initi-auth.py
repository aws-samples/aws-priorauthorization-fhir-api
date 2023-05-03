import boto3
import sys

client = boto3.client('cognito-idp')

response = client.initiate_auth(
    AuthFlow='USER_PASSWORD_AUTH',
    AuthParameters={
        'USERNAME': 'manishpl@amazon.com',
        'PASSWORD': 'Master#456'
    },

    ClientId='2qkcfkfg4m09nrbs6u5ps4l95q'
)
sessionid = response['AuthenticationResult']['IdToken']

print(sessionid)