import sqlite3
con = sqlite3.connect(r"bot/monitoring_bot.db")
cur = con.cursor()
print("\nregistration_links (username, token, is_used):")
for r in cur.execute("SELECT username, token, is_used FROM registration_links ORDER BY rowid DESC LIMIT 10"):
    print(r)
print("\nusers (username, chat_id, linked_at):")
for r in cur.execute("SELECT username, chat_id, linked_at FROM users"):
    print(r)
con.close()
