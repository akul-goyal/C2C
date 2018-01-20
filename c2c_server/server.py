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

from database_manager import Database
db = Database()

import sys

#---REST API Routes-------------------------------------------------------------

# user profile
@app.route('/update_profile', methods = ['POST'])
def update_profile():
    db.save_json_to_users(dict(request.form))


# location
@app.route('/update_location', methods = ['POST'])
def update_location():
    db.save_json_to_locations(dict(request.form))


# messages
@app.route('/send_message', methods = ['POST'])
def send_message():
    info = process_message_info(dict(request.form))
    db.save_json_to_messages(info)


#---Helpful Functions-----------------------------------------------------------

# takes a message dict and adds missing info to it
def process_message_info(message):
    sys.exit()
    message["target_plate_num"] = "CHANGE THIS"
    message["target_car_type"] = "CHANGE THIS"
    message["target_car_color"] = "CHANGE THIS"
    message["message"] = check_message_appropriate(message["message"])
    return message

def check_message_appropriate(message):
    sys.exit()
