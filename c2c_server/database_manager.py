# handler class for dealing with all functions of the database
# - creation of the database and tables
# - accessing and inserting into the database

# database stores info in tables:
# - user_profiles:
# - locations:
# - messages:

import sqlite3
import json
import sys


# Strings that contain sqlite execution instructions
insert_user = "INSERT OR REPLACE INTO user_profiles VALUES (?, ?, ?, ?)"
insert_location = "INSERT INTO locations VALUES (?, ?, ?, ?, ?, ?)"
insert_message = "INSERT INTO messages VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
load_users = "SELECT * FROM user_profiles"
load_locations = "SELECT * FROM locations"
load_messages = "SELECT * FROM messages"

class Database(object):

    # open up database
    def __init__(self):
        self.database  = sqlite3.connect("cartalk.db")

    # automatically close database when done
    def __del__(self):
        self.database.close()

    #---SAVE--------------------------------------------------------------------

    # insert list of info into user_profiles
    def save_json_to_users(self, json_package):
        info = self.format_user_info(json_package)
        self.database.execute(insert_user, info)

    # takes a user json_package and converts it into a list to save to db
    def format_user_info(self, json_package):
        info = []
        info.append(json_package["plate_num"])
        info.append(json_package["car_type"])
        info.append(json_package["car_color"])
        info.append(json_package["name"])
        return info

    # insert list of info into locations
    def save_json_to_locations(self, json_package):
        info =  self.format_location_info(json_package)
        self.database.execute(insert_location, info)

    # takes a location json and convert it into a list to save to database
    def format_location_info(self, json_package):
        info = []
        info.append(json_package["plate_num"])
        info.append(json_package["car_type"])
        info.append(json_package["car_color"])
        info.append(json_package["longitude"])
        info.append(json_package["latitude"])
        info.append(json_package["send_time"])
        info.append(json_package["nearby_cars"])
        return info

    # insert into messages
    def save_json_to_messages(self, json_package):
        info = self.format_message_info(json_package)
        self.database.execute(insert_message, info)

    # takes a message json and convert it into a database savable format
    def format_message_info(self, json_package):
        info = []
        info.append(json_package["message"])
        info.append(json_package["sender_plate_num"])
        info.append(json_package["sender_car_type"])
        info.append(json_package["sender_car_color"])
        info.append(json_package["target_plate_num"])
        info.append(json_package["target_car_type"])
        info.append(json_package["target_car_color"])
        info.append(json_package["longitude"])
        info.append(json_package["latitude"])
        info.append(json_package["send_time"])
        info.append(json_package["nearby_cars"])
        return info

    #---LOAD--------------------------------------------------------------------

    # load from user_profiles
    def load_all_user_profiles(self):
        result = self.database.execute(load_users)
        print ("FILL IN CODE HERE")
        sys.exit()

    # load from locations
    def load_all_locations(self):
        result = self.database.execute(load_locations)
        print ("FILL IN CODE HERE")
        sys.exit()

    # load from messages
    def load_all_messages(self):
        result = self.database.execute(load_messages)
        print ("FILL IN CODE HERE")
        sys.exit()

    #---SET UP------------------------------------------------------------------

    # sets up database by calling create table functions
    def set_up_db(self):
        self.create_user_table()
        self.create_locations_table()
        self.create_messages_table()
        print ("Success!")

    # create user table for storing info about people and their cars
    def create_user_table(self):
        self.database.execute('''CREATE TABLE user_profiles
        (plate_num TEXT PRIMARY KEY,
        car_type TEXT,
        car_color TEXT,
        name TEXT);''')
        print ("Created user_profiles table")

    # create locations table to store location updates
    def create_locations_table(self):
        self.database.execute('''CREATE TABLE locations
        (plate_num TEXT,
        car_type TEXT,
        car_color TEXT,
        longitude REAL,
        latitude REAL,
        send_time INT
        nearby_cars TEXT);''')
        print ("Created locations table")

    # create messages table to store sender and target car info
    def create_messages_table(self):
        self.database.execute('''CREATE TABLE messages
        (message TEXT,
        sender_plate_num TEXT,
        sender_car_type TEXT,
        sender_car_color TEXT,
        target_plate_num TEXT,
        target_car_type TEXT,
        target_car_color TEXT,
        longitude REAL,
        latitude REAL,
        send_time INT
        nearby_cars TEXT);''')
        print ("Created messages table")
