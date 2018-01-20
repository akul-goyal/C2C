# Sets up cartalk.db to store user info in
# runs db setup code from  Database class

from database_manager import Database

def run_set_up_db():
    db = Database()
    db.set_up_db()

# RUN
if __name__ == "__main__":
    run_set_up_db()
