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
groupUrl = '"/api/groups/"'
permissionUrl = ' + "/editablePermissions"'
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
headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcl9pZCI6MSwicGVybWlzc2lvbiI6MX0.r68KlS3szkDOUYvyGTf1HUG1nkF2U8WMM5u3AN0AFfI", "Content-Type": "application/json" }

class Verbs(Enum): 
  Get = 'get'
  Post = 'post'
  Put = 'put'
  Delete = 'delete'

#define options
optionsPrint = {
  1: (Verbs.Get, 'Get all tags'), 
  2: (Verbs.Post, 'Login user'), # can only be for admin
  3: (Verbs.Post, 'Create reservation'),
  4: (Verbs.Get, 'Get reservation by ID'),
  5: (Verbs.Put, 'Update reservation by ID'),
  6: (Verbs.Delete, 'Delete reservation by ID'),
  7: (Verbs.Post, 'Create resource'),
  8: (Verbs.Get, 'Get resource by ID'),
  9: (Verbs.Put, 'Update resource by ID'),
  10: (Verbs.Delete, 'Delete resource by ID'),
  11: (Verbs.Get,'Get resource canDelete by ID'),
  12: (Verbs.Get, 'Get user by ID'),
  13: (Verbs.Post, 'Create Group'),
  14: (Verbs.Get, 'Get Group by ID'),
  15: (Verbs.Get, 'Get all groups'), 
  16: (Verbs.Put, 'Update Group by ID'),
  17: (Verbs.Delete, 'Delete Group by ID'),
  18: (Verbs.Post, 'Update permissions by user ID'),
  19: (Verbs.Get, 'Get permissions by user ID')
}

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
      2: postRequest + loginUrl + requestPayload + requestEnding,
      3: postRequest + reserveUrl + requestPayload + requestEnding,
      4: getRequest + reserveUrl + requestID + requestEnding, 
      5: putRequest + reserveUrl + requestID + requestPayload + requestEnding,
      6: deleteRequest + reserveUrl + requestID + requestEnding,
      7: postRequest + resourceUrl + requestPayload + requestEnding,
      8: getRequest + resourceUrl + requestID + requestEnding,
      9: putRequest + resourceUrl + requestID + requestPayload + requestEnding,
      10: deleteRequest + resourceUrl + requestID + requestEnding,
      11: getRequest + resourceUrl + requestID + canDeleteUrl + requestEnding,
      12: getRequest + userUrl + requestID + requestEnding,
      13: putRequest + groupUrl + requestPayload + requestEnding,
      14: getRequest + groupUrl + requestID + requestEnding,
      15: getRequest + groupUrl + requestEnding,
      16: putRequest + groupUrl + requestID + requestPayload + requestEnding,
      17: deleteRequest + groupUrl + requestID + requestEnding,
      18: postRequest + userUrl + requestID + permissionUrl + requestPayload + requestEnding,
      19: getRequest + userUrl + requestID + permissionUrl + requestEnding
    }
    exec(updatedDict[input])
    
    print 'URL: '+ r.url
    print 'response: ' + str(r.json()) + '\n'
  else:
    print input + ' is not a valid option\n' 


if __name__ == '__main__':
    debugger()










