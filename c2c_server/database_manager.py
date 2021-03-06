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
import math


# Strings that contain sqlite execution instructions
# insert_user = "INSERT OR REPLACE INTO user_profiles VALUES (?, ?, ?, ?)"
insert_location = "INSERT INTO locations VALUES (?, ?, ?, ?, ?, ?, ?)"
insert_message = "INSERT INTO messages VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
# load_users = "SELECT * FROM user_profiles"
load_locations = "SELECT * FROM locations"
load_messages = "SELECT * FROM messages"

class Database(object):

    # open up database
    def __init__(self):
        self.database  = sqlite3.connect("cartalk.db")
        self.location_profile={}
        self.message_profile={}

    # automatically close database when done
    def __del__(self):
        self.database.close()

    #---SAVE--------------------------------------------------------------------

    # insert list of info into user_profiles
    # def save_json_to_users(self, json_package):
    #     info = self.format_user_info(json_package)
    #     self.database.execute(insert_user, info)
    #
    # # takes a user json_package and converts it into a list to save to db
    # def format_user_info(self, json_package):
    #     info = []
    #     info.append(json_package["plate_num"])
    #     info.append(json_package["car_type"])
    #     info.append(json_package["car_color"])
    #     info.append(json_package["name"])
    #     return info
    def get_messages_from_mailbox(self, json_package):
        if json_package["plate_num"] in self.message_profile:
            lis = self.message_profile[json_package["plate_num"]]["message"]
            return lis
        else:
            return None
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

    #---Helper--------------------------------------------------------------------
    # user_profile={}

    # # load from user_profiles
    # def load_all_user_profiles(self):
    #     result = self.database.execute(load_users)
    #
    #     # this is a dictionary of dictionary, go fuck your self if you
    #     # cant undertsand it; you are a dumbass
    #     for objects in result:
    #         user_profile[objects[0]]={}
    #         for elements in objects:
    #             user_profile[objects[0]][elements]=elements
    #
    #     print ("FILL IN CODE HERE")
    #     sys.exit()

    # load from locations


    def load_in_location(self, json_package):
        nameplate=json_package["plate_num"]
        self.location_profile[nameplate]={}
        for elements in json_package:

            self.location_profile[nameplate][elements]=json_package[elements]


    # load from messages
    def load_some_messages(self, json_package):
        nameplate = json_package["target_plate_num"]

        value = self.message_profile.get(nameplate)
        if value == None:
            self.message_profile[nameplate]={}

        value = self.message_profile.get('message')
        if value == None:
            self.message_profile[nameplate]['message']=[json_package['message']]
        else:
            self.message_profile[nameplate]['message']=self.message_profile[nameplate]['message'].append(json_package['message'])



    def load_message_everyone(self, json_package):
        for nameplate in self.location_profile:
            x1=float(json_package["longitude"])
            x2=float(json_package["latitude"])
            y1=float(self.location_profile[nameplate]["longitude"])
            y2=float(self.location_profile[nameplate]["latitude"])
            z1=(floor_time(json_package["time"]))
            z2=floor_time(self.location_profile[nameplate]["send_time"])
            if haversine(x1,x2,y1,y2)< 9000 and  z1==z2:
                if self.message_profile[nameplate["plate_num"]]==None:
                    self.message_profile[nameplate["plate_num"]]=[]
                    self.message_profile[nameplate["plate_num"]]= self.message_profile[nameplate["plate_num"]].append(json_package["message"])
                else:
                    self.message_profile[nameplate["plate_num"]]= self.message_profile[nameplate["plate_num"]].append(json_package["message"])



    def floor_time(self, time):
        timeS=int(str(time[-5:-3]))

        if timeS>=30:
            timeM=int(str(time[-8:-6]))
            if timeM<59:
                timeM=timeM+1

                return time[0:-8] + str(timeM) + time[-6:len(time)]
            else:
                timeH=str(time[-11:-9])
                if ' ' in timeH:
                    timeH=timeH[1:2]
                    flag=True
                timeH=int(timeH)
                timeH=timeH+1
                if flag:

                    return time[0:-10] + str(timeH)+':'+ str(00) + time[-6:len(time)]
                else:

                    return time[0:-11] + str(timeH)+':'+ str(00) + time[-6:len(time)]
        else:
            return time[0:-5] + str(00)+time[-3:len(time)]

    def haversine(self, longitude1, latitude1, longitude2, latitude2):
        lon1 = longitude1
        lat1 = latitude1
        lon2 = longitude1
        lat2 = latitude2

        R = 6371000  # radius of Earth in meters
        phi_1 = math.radians(lat1)
        phi_2 = math.radians(lat2)

        delta_phi = math.radians(lat2 - lat1)
        delta_lambda = math.radians(lon2 - lon1)

        a = math.sin(delta_phi / 2.0) ** 2 + math.cos(phi_1) * math.cos(phi_2) * math.sin(delta_lambda / 2.0) ** 2

        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

        meters = R * c  # output distance in meters
        return meters


    def find_name_plate(self, color, cartype, longitude, latitude, nearby_cars, time, num_plate):

        if color is None:
            return None

        if cartype is None:
            return None

        # calc1=abs(float(longitude)-float(nameplate["longitude"]))<0.001352
        # calc2=abs(float(latitude)-float(nameplate["latitude"]))<.000027
        # calc3=abs(float(latitude)-float(nameplate["latitude"]))<0.001352
        # calc4=abs(float(longitude)-float(nameplate["longitude"]))<.000027

        if nearby_cars is None:
            for nameplate in self.location_profile:
                if nameplate == num_plate:
                    continue
                distance = self.haversine(float(longitude),float(latitude),
                float(self.location_profile[nameplate]["longitude"]),float(self.location_profile[nameplate]["latitude"]))

                time=self.floor_time(time)
                time2=self.floor_time(self.location_profile[nameplate]["send_time"])
                if distance<61 and time==time2:

                        if cartype==self.location_profile[nameplate]['car_type'] and color==self.location_profile[nameplate]['car_color']:

                                    return self.location_profile[nameplate]["plate_num"]
        else:
            for nameplate in nearby_cars:
                if cartype == self.location_profile[nameplate]['car_type']:
                    if color == self.location_profile[nameplate]['car_color']:
                        return self.location_profile[nameplate]["plate_num"]









    #---SET UP------------------------------------------------------------------

    # sets up database by calling create table functions
    def set_up_db(self):
        self.create_user_table()
        self.create_locations_table()
        self.create_messages_table()


    # create user table for storing info about people and their cars
    def create_user_table(self):
        self.database.execute('''CREATE TABLE user_profiles
        (plate_num TEXT PRIMARY KEY,
        car_type TEXT,
        car_color TEXT,
        name TEXT);''')


    # create locations table to store location updates
    def create_locations_table(self):
        self.database.execute('''CREATE TABLE locations
        (plate_num TEXT,
        car_type TEXT,
        car_color TEXT,
        longitude REAL,
        latitude REAL,
        send_time TEXT,
        nearby_cars TEXT);''')


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
        send_time TEXT,
        nearby_cars TEXT);''')
