import sqlite3
con = sqlite3.connect(r"bot/monitoring_bot.db")
cur = con.cursor()
print('tables:')
for row in cur.execute("SELECT name FROM sqlite_master WHERE type='table'"):
    print(row)
print('\nregistration_links (username, token, is_used, claimed_at):')
for row in cur.execute("SELECT username, token, is_used, claimed_at FROM registration_links ORDER BY rowid DESC LIMIT 10"):
    print(row)
print('\nusers (username, chat_id, is_registered, linked_at):')
for row in cur.execute("SELECT username, chat_id, is_registered, linked_at FROM users ORDER BY rowid DESC LIMIT 10"):
    print(row)
con.close()
