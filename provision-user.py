import boto3
import sys

client = boto3.client('cognito-idp')

response = client.admin_create_user(
    UserPoolId=sys.argv[1],
    Username=sys.argv[3],
    UserAttributes=[
        {
            'Name': 'email',
            'Value':sys.argv[3]
        },
        {
            'Name': 'email_verified',
            'Value': 'True'
        }
        
    ],
    ValidationData=[
        {
            'Name': 'email',
            'Value': sys.argv[3]
        }
    ],
    TemporaryPassword=sys.argv[4],
    MessageAction='SUPPRESS'
)

response = client.initiate_auth(
    AuthFlow='USER_PASSWORD_AUTH',
    AuthParameters={
        'USERNAME': sys.argv[3],
        'PASSWORD': sys.argv[4]
    },

    ClientId=sys.argv[2]
)
sessionid = response['Session']

response = client.respond_to_auth_challenge(
    ClientId=sys.argv[2],
    ChallengeName='NEW_PASSWORD_REQUIRED',
    Session=sessionid,
    ChallengeResponses={
        'USERNAME' : sys.argv[3],
        'NEW_PASSWORD': sys.argv[4]
    }
)

response = client.initiate_auth(
    AuthFlow='USER_PASSWORD_AUTH',
    AuthParameters={
        'USERNAME': sys.argv[3],
        'PASSWORD': sys.argv[4]
    },

    ClientId=sys.argv[2]
)

#print(response)
sessionid = response['AuthenticationResult']['IdToken']
print(sessionid)

