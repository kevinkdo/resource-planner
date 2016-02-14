import requests
import json
import smtplib

protocol = 'https' # change to http for non SSL
#host = '://localhost' 
host = '://easywebapi.com'
baseUrl = protocol + host
resetUrl = '/admin/init' 
tagUrl = '/api/tags'
resourceUrl = '/api/resources'
registerUrl = '/auth/register'
loginUrl = '/auth/login'
reserveUrl = '/api/reservations'
userUrl = '/api/users'
headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
userLogin = {'email':'admin@admin.com', 'username':'admin', 'password':'admin'}

error = False
req = []
#login
req.append(requests.post(baseUrl + loginUrl, data = json.dumps(userLogin), headers = headers, verify = False))

#get requests
req.append(requests.get(baseUrl + tagUrl, headers = headers, verify = False))
req.append(requests.get(baseUrl + userUrl + '/1', headers = headers, verify = False))
req.append(requests.get(baseUrl + reserveUrl + '/1', headers = headers, verify = False))
req.append(requests.get(baseUrl + resourceUrl + '/1', headers = headers, verify = False))

for r in req:
  if r.status_code != 200:
    error = True

if error:
  #http://stackoverflow.com/questions/10147455/how-to-send-an-email-with-gmail-as-provider-using-python
  gmail_user = 'ResourceManagerAlerts@gmail.com'
  gmail_pwd = 'resourcemanager'
  FROM = gmail_user
  recipient = 'mh291@duke.edu; kkd10@duke.edu; davis.treybig@duke.edu; jiawei.zhang@duke.edu '
  TO = recipient if type(recipient) is list else [recipient]
  SUBJECT = 'PROD ERROR'
  TEXT = 'ONE OF THE GET REQUESTS DID NOT RETURN STATUS 200'

  # Prepare actual message
  message = """\From: %s\nTo: %s\nSubject: %s\n\n%s
  """ % (FROM, ", ".join(TO), SUBJECT, TEXT)
  try:
      server = smtplib.SMTP("smtp.gmail.com", 587)
      server.ehlo()
      server.starttls()
      server.login(gmail_user, gmail_pwd)
      server.sendmail(FROM, TO, message)
      server.close()
      print 'successfully sent the mail'
  except:
      print "failed to send mail"


 