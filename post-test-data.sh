for entry in "./docs/request"/*.json
do
  # include the cognito id token and make the endpoint as a parameter
  curl -H "Content-Type: application/fhir+json" -H "Authorization: <<IDToken>>" -i --data "@$entry" <<API_END_POINT>>Bundle
  echo "$entry"
done