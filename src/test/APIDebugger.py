import requests
import json
from enum import Enum

protocol = 'https' # change to http for non SSL
#host = 'localhost' 
host = 'easywebapi.com'
baseUrl = protocol + '://' + host
resetUrl = '"/admin/init"' 
tagUrl = '"/api/tags"'
resourceUrl = '"/api/resources"'
forestUrl = '+ "/forest"'
registerUrl = '"/auth/register"'
loginUrl = '"/auth/login"'
reserveUrl = '"/api/reservations"'
userUrl = '"/api/users"'
canDeleteUrl = '+ "/candelete"'
groupUrl = '"/api/groups/"'
permissionUrl = ' + "/editablePermissions"'
incompleteReservationUrl = ' + "/approvableReservations"'
cancelIncompleteReservationUrl = ' + "/canceledWithApproval"'
approveIncompletReservationUrl = ' + "/approveReservation"'
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
headers = {'Accept': 'application/json', "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOjEsInN1cGVyX3AiOnRydWUsInJlc291cmNlX3AiOmZhbHNlLCJyZXNlcnZhdGlvbl9wIjpmYWxzZSwidXNlcl9wIjpmYWxzZX0.ftBgC8KfB5GyQy68I7HdQQZjUdBvkF-il93Z87BwKfg", "Content-Type": "application/json" }
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
  18: (Verbs.Put, 'Update permissions by user ID'),
  19: (Verbs.Get, 'Get permissions by user ID'),
  20: (Verbs.Get, 'Get all incomplete reservations that you can approve'),
  21: (Verbs.Get, 'Get all incomplete reservations that would be canceled with approval of reservation ID (type in)'),  
  22: (Verbs.Post, 'Approve or deny a reservation'),
  23: (Verbs.Get, 'Get resource forest')
}


def debugger():
  print "Assumes you are logged in as admin"
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

def jsonValidate(str):
  if len(str) == 0:
    return False
  if str[0] != "{" or str[len(str) - 1] != "}":
    return False

def processSelection(input):
  r = None
  requestID = ""
  requestPayload = ""
  if input in optionsPrint:
    if optionsPrint[input][0] is Verbs.Put or optionsPrint[input][0] is Verbs.Get or optionsPrint[input][0] is Verbs.Delete:
      temp = raw_input('input the desired ID or hit enter if non-applicable: ')
      if temp == '' and (input is not 1 and input is not 15 and input is not 20 and input is not 23):
        while temp == '':
          temp = raw_input('ID required. Input the desired ID: ')
      else:
        requestID = requestIDPre + temp + requestIDPost
    if optionsPrint[input][0] is Verbs.Post and input is 22:
      temp = raw_input('input the desired ID or hit enter if non-applicable: ')
      if temp == '' and (input is not 1 and input is not 15 and input is not 20):
        while temp == '':
          temp = raw_input('ID required. Input the desired ID: ')
      else:
        requestID = requestIDPre + temp + requestIDPost  
      
    if optionsPrint[input][0] is Verbs.Post or optionsPrint[input][0] is Verbs.Put: 
      temp = raw_input('input the json payload or hit enter if non-applicable: ')
      if temp == '':
        while jsonValidate() is False:
          temp = raw_input('Payload required. input valid json payload: ')
      else:
        requestPayload = requestPayloadPre + temp + requestPayloadPost

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
      13: postRequest + groupUrl + requestPayload + requestEnding,
      14: getRequest + groupUrl + requestID + requestEnding,
      15: getRequest + groupUrl + requestEnding,
      16: putRequest + groupUrl + requestID + requestPayload + requestEnding,
      17: deleteRequest + groupUrl + requestID + requestEnding,
      18: putRequest + userUrl + requestID + permissionUrl + requestPayload + requestEnding,
      19: getRequest + userUrl + requestID + permissionUrl + requestEnding,
      20: getRequest + reserveUrl + incompleteReservationUrl + requestEnding,
      21: getRequest + reserveUrl + cancelIncompleteReservationUrl + requestID + requestEnding,
      22: postRequest + reserveUrl + approveIncompletReservationUrl + requestID + requestPayload + requestEnding,
      23: getRequest + resourceUrl + forestUrl + requestEnding
    }
    print updatedDict[input]
    exec(updatedDict[input])
    
    print 'URL: '+ r.url
    print 'response: ' + str(r.json()) + '\n'
  else:
    print input + ' is not a valid option\n' 


if __name__ == '__main__':
    debugger()










