import requests
import json
from enum import Enum

protocol = 'https' # change to http for non SSL
host = 'localhost' 
#host = 'easywebapi.com'
baseUrl = protocol + '://' + host
resetUrl = '"/admin/init"' 
tagUrl = '"/api/tags"'
resourceUrl = '"/api/resources"'
registerUrl = '"/auth/register"'
loginUrl = '"/auth/login"'
reserveUrl = '"/api/reservations"'
userUrl = '"/api/users"'
canDeleteUrl = '+ "/candelete"'
headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }
validUser = {'email':'user13@admin.com', 'username':'someusername', 'password':'something', 'should_email':'False'}
#invalidUser = {'email':'admin@admin.com', 'username':'someusername', 'password':'something', 'should_email':'False'}
validUserLogin = {'email':'admin@admin.com', 'username':'admin', 'password':'admin'}
validResource = {'name':'some resource', 'description':'some resource description', 'tags':[]}
validReservation = {"user_id": "1", "resource_id": "1", "begin_time": "2011-08-06T10:54:00.000Z", "end_time": "2011-08-06T11:00:00.000Z", "should_email": "true"}
update = {'resource_id':'2', 'should_email':'false'}
      


getRequest = 'r = requests.get(baseUrl + '
postRequest = 'r = requests.post(baseUrl + '
putRequest = 'r = requests.put(baseUrl + '
deleteRequest = 'r = requests.delete(baseUrl + '
requestEnding = ', headers = headers, verify = False)'  
requestPayloadPre = ', json.dumps('
requestPayload = ''
requestPayloadPost = ')'
requestID = ''
requestIDPre = ' + "/'
requestIDPost = '"'

class Verbs(Enum): 
  Get = 'get'
  Post = 'post'
  Put = 'put'
  Delete = 'delete'

#define options
optionsPrint = {
  1: (Verbs.Get, 'Get all tags'), # don't need to ask for any ID
  2: (Verbs.Post, 'Register a valid user'),
  3: (Verbs.Post, 'Login as valid user'),
  4: (Verbs.Post, 'Create valid reservation'),
  5: (Verbs.Get, 'Get reservation'),
  6: (Verbs.Put, 'Update reservation'),
  7: (Verbs.Delete, 'Delete reservation'),
  8: (Verbs.Post, 'Create resource'),
  9: (Verbs.Get, 'Get resource'),
  10: (Verbs.Put, 'Update resource'),
  11: (Verbs.Delete, 'Delete resource'),
  12: (Verbs.Get,'Get resource canDelete'),
  13: (Verbs.Post, 'Create valid user'),
  14: (Verbs.Get, 'Get user'),
  15: (Verbs.Put, 'Update user'),
  16: (Verbs.Delete, 'Delete user')
}

def refreshDict():
  refreshedDict = {
    1: getRequest + tagUrl + requestEnding, 
    2: postRequest + registerUrl + requestPayload + requestEnding,
    3: postRequest + loginUrl + requestPayload + requestEnding,
    4: postRequest + reserveUrl + requestPayload + requestEnding,
    5: getRequest + reserveUrl + requestID + requestEnding, 
    6: putRequest + reserveUrl + requestID + requestPayload + requestEnding,
    7: deleteRequest + reserveUrl + requestID + requestEnding,
    8: postRequest + resourceUrl + requestPayload + requestEnding,
    9: getRequest + resourceUrl + requestID + requestEnding,
    10: putRequest + resourceUrl + requestID + requestPayload + requestEnding,
    11: deleteRequest + resourceUrl + requestID + requestEnding,
    12: getRequest + resourceUrl + requestID + canDeleteUrl + requestEnding,
    13: postRequest + userUrl + requestPayload + requestEnding,
    14: getRequest + userUrl + requestID + requestEnding,
    15: putRequest + userUrl + requestID + requestPayload + requestEnding,
    16: deleteRequest + userUrl + requestID + requestEnding
  }

  return refreshedDict


def debugger():
  while(True):
    for s in optionsPrint:
      print str(s) + ': ' + optionsPrint[s][1]
    selection = raw_input('Choose one of the above options or "q" to quit: ')
    if selection.isdigit(): 
      processSelection(int(selection))
    else:
      if selection == "q":
        break
      else:
        print 'enter either "q" or one of the above options\n'
    
def processSelection(input):
  r = None
  if input in optionsPrint:
    if optionsPrint[input][0] is Verbs.Put or Verbs.Get or Verbs.Delete:
      requestID = requestIDPre + raw_input('input the desired ID or hit enter if non-applicable: ') + requestIDPost
    if optionsPrint[input][0] is Verbs.Post or Verbs.Put: 
      requestPayload = requestPayloadPre + raw_input('input the json payload or hit enter if non-applicable: ') + requestPayloadPost

    updatedDict = {
      1: getRequest + tagUrl + requestEnding, 
      2: postRequest + registerUrl + requestPayload + requestEnding,
      3: postRequest + loginUrl + requestPayload + requestEnding,
      4: postRequest + reserveUrl + requestPayload + requestEnding,
      5: getRequest + reserveUrl + requestID + requestEnding, 
      6: putRequest + reserveUrl + requestID + requestPayload + requestEnding,
      7: deleteRequest + reserveUrl + requestID + requestEnding,
      8: postRequest + resourceUrl + requestPayload + requestEnding,
      9: getRequest + resourceUrl + requestID + requestEnding,
      10: putRequest + resourceUrl + requestID + requestPayload + requestEnding,
      11: deleteRequest + resourceUrl + requestID + requestEnding,
      12: getRequest + resourceUrl + requestID + canDeleteUrl + requestEnding,
      13: postRequest + userUrl + requestPayload + requestEnding,
      14: getRequest + userUrl + requestID + requestEnding,
      15: putRequest + userUrl + requestID + requestPayload + requestEnding,
      16: deleteRequest + userUrl + requestID + requestEnding
    }
    exec(updatedDict[input])
    
    print 'URL: '+ r.url
    print 'response: ' + str(r.json()) + '\n'
  else:
    print input + ' is not a valid option\n' 


if __name__ == '__main__':
    debugger()










