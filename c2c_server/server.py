# CarTalk flask server

# functions server can handle:
# - user updates their profile
# - user updates location
# - user sends in a message

# When a user sends in a location/message, server checks to see if they
# have any messages queued up for them

# store data in memory, but save to database

# flask dependencies
from flask import Flask
from flask import request
app = Flask(__name__)
from werkzeug.datastructures import ImmutableMultiDict

from database_manager import Database
db = Database()

import sys
import math
import json



#---REST API Routes-------------------------------------------------------------

# user profile
# @app.route('/update_profile', methods = ['POST'])
# def update_profile():
#     db.save_json_to_users(dict(request.form))


# location


@app.route('/update_location', methods = ['GET', 'POST'])
def update_location():

    # convert info into usable format
    info = 0
    server_dict = request.form.to_dict(flat=False)
    for key, values in server_dict.iteritems():
        info = key
    info = json.loads(info)




    db.save_json_to_locations(info)
    db.load_in_location(info)

    return "null"


# messages

@app.route('/send_message', methods = ['POST'])
def send_message():

    # convert info into usable format
    request_info = 0
    server_dict = request.form.to_dict(flat=False)
    for key, values in server_dict.iteritems():
        request_info = key
    request_info = json.loads(request_info)
    #
    flag=False
    flag_error=False
    info = check_everyone_present(request_info, flag, flag_error)

    if flag_error:
        error_message="Car not found, please provide more information"
        return error_message
    if not flag:
        db.save_json_to_messages(info)

        db.load_some_messages(info)
    else:
        db.load_message_everyone(info)

    return "null"


@app.route ('/recieve_message', methods=['POST'])
def recieve_message():
    request_info = 0
    server_dict = request.form.to_dict(flat=False)
    for key, values in server_dict.iteritems():
        request_info = key
    request_info = json.loads(request_info)
    #this might not be right. Dont know how else to parse the request.
    messages = db.get_messages_from_mailbox(request_info)

    if messages is None:
        return "You dont have any messages"
    else:
        
        ret={}
        for x in range(len(messages)):
            ret[x]=messages[x]
        bozox= ''.join('{}, '.format(val) for key, val in ret.items())
        return bozox + '\n'

    #return messages



    #delete message from mailbox


#---Helpful Functions-----------------------------------------------------------


def check_everyone_present(message, flag, flag_error):

    if 'everyone' in message["message"]:
        return process_message_for_everyone(message)
        flag=True
    else:
        return process_message_info(message, flag_error)


# takes a message dict and adds missing info to it
def process_message_for_everyone(message):
    message["message"] = "Someone, 20 miles near you, said " + message["message"]
    return message

def process_message_info(message, flag_error):
    database_cars = ['honda', 'toyota', 'nissan', 'hyundai', 'ford', 'chevrolet']

    database_color = ['red', 'yellow', 'blue', 'orange', 'green', 'violet', 'black'
    'gray', 'white', 'silver', 'brown', 'beige', 'gold']

    for car in database_cars:
        if car in message["message"]:
            message["target_car_type"]=car
            break
    #if time look for red car and if only one then good
    if message["target_car_type"] != None:
        for color in database_color:
            if color in message["message"]:
                message["target_car_color"] = color
                break

    message["target_plate_num"] = db.find_name_plate(message["target_car_color"], message["target_car_type"],
    message["longitude"], message["latitude"], message["nearby_cars"],
    message["send_time"], message["sender_plate_num"])


    if message["target_plate_num"] == None:
        flag_error=True
    if not flag_error:
        message["message"] = check_message_appropriate(message)

    return message

def check_message_appropriate(message):
    message1=message["message"]
    string_final="Someone on the road said " + message1
    return string_final
