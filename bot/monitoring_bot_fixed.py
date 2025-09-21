import telebot
import sqlite3
import hashlib
import secrets
import logging
from flask import Flask, request, jsonify
import threading
import os
from datetime import datetime, timedelta
from dotenv import load_dotenv
from email_notifier import EmailNotifier

# Загрузка переменных окружения
load_dotenv()

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Токен бота из .env файла
BOT_TOKEN = os.getenv('BOT_TOKEN', '7479905970:AAGRDlnda26Aw6Bfbq9HyaPWFmOQt1aAb3o')
BOT_USERNAME = os.getenv('BOT_USERNAME', 'PingTower_tax_bot')
bot = telebot.TeleBot(BOT_TOKEN)

# Flask приложение для API
app = Flask(__name__)

# Инициализация email notifier
email_notifier = EmailNotifier()

# Инициализация базы данных
def init_database():
    conn = sqlite3.connect('monitoring_bot.db')
    cursor = conn.cursor()
    
    # Таблица пользователей
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            chat_id INTEGER PRIMARY KEY,
            username TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            is_registered BOOLEAN DEFAULT FALSE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    
    # Таблица сервисов пользователей
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS user_services (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            chat_id INTEGER,
            service_name TEXT NOT NULL,
            service_url TEXT,
            FOREIGN KEY (chat_id) REFERENCES users (chat_id)
        )
    ''')
    
    # CREATE TABLE for registration links and maintain helper columns on users
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS registration_links (
            token TEXT PRIMARY KEY,
            username TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            expires_at TIMESTAMP,
            chat_id INTEGER,
            claimed_at TIMESTAMP,
            is_used BOOLEAN DEFAULT FALSE
        )
    ''')
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_registration_links_username ON registration_links(username)')

    cursor.execute('PRAGMA table_info(users)')
    user_columns = {column[1] for column in cursor.fetchall()}
    if 'telegram_username' not in user_columns:
        cursor.execute("ALTER TABLE users ADD COLUMN telegram_username TEXT")
    if 'linked_at' not in user_columns:
        cursor.execute("ALTER TABLE users ADD COLUMN linked_at TIMESTAMP")

    conn.commit()
    conn.close()
    logger.info("Database initialized successfully")

# Хеширование пароля
def hash_password(password):
    return hashlib.sha256(password.encode()).hexdigest()

# Проверка регистрации пользователя
def is_user_registered(chat_id):
    conn = sqlite3.connect('monitoring_bot.db')
    cursor = conn.cursor()
    cursor.execute('SELECT is_registered FROM users WHERE chat_id = ?', (chat_id,))
    result = cursor.fetchone()
    conn.close()
    return result and result[0]

# Регистрация пользователя
def register_user(chat_id, username, password):
    conn = sqlite3.connect('monitoring_bot.db')
    cursor = conn.cursor()
    password_hash = hash_password(password)
    
    try:
        cursor.execute('''
            INSERT INTO users (chat_id, username, password_hash, is_registered) 
            VALUES (?, ?, ?, TRUE)
        ''', (chat_id, username, password_hash))
        conn.commit()
        conn.close()
        logger.info(f"User {username} registered successfully with chat_id {chat_id}")
        return True
    except sqlite3.IntegrityError:
        conn.close()
        logger.warning(f"Registration failed for {username} - username already exists")
        return False


# Registration token helpers for deep-link flow
def cleanup_expired_registration_links(cursor):
    """Mark expired registration links as used to avoid reuse."""
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    cursor.execute('''
        UPDATE registration_links
        SET is_used = TRUE
        WHERE expires_at IS NOT NULL
          AND is_used = FALSE
          AND expires_at < ?
    ''', (now_str,))



def store_registration_link(username, token):
    """Persist the provided token so the bot can bind chats later."""
    token = str(token).strip()
    if not token:
        raise ValueError('token is required')

    conn = sqlite3.connect('monitoring_bot.db')
    cursor = conn.cursor()

    cursor.execute('''
        CREATE TABLE IF NOT EXISTS registration_links (
            token TEXT PRIMARY KEY,
            username TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            expires_at TIMESTAMP,
            chat_id INTEGER,
            claimed_at TIMESTAMP,
            is_used BOOLEAN DEFAULT FALSE
        )
    ''')
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_registration_links_username ON registration_links(username)')

    cleanup_expired_registration_links(cursor)

    cursor.execute('DELETE FROM registration_links WHERE username = ? OR token = ?', (username, token))
    cursor.execute('''
        INSERT INTO registration_links (token, username, is_used)
        VALUES (?, ?, FALSE)
    ''', (token, username))

    conn.commit()
    conn.close()
    logger.info(f"Prepared registration token for {username} -> {token}")
    return token

def bind_token_to_chat(token, chat_id, telegram_username=None):
    """Consume a registration token and bind the Telegram chat with the platform account."""
    conn = sqlite3.connect('monitoring_bot.db')
    cursor = conn.cursor()
    cleanup_expired_registration_links(cursor)

    cursor.execute('SELECT username, expires_at, is_used, chat_id FROM registration_links WHERE token = ?', (token,))
    row = cursor.fetchone()
    if not row:
        conn.close()
        return False, {'status': 'token_not_found'}

    username, expires_at_str, is_used, token_chat_id = row
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    if is_used:
        if token_chat_id == chat_id:
            conn.close()
            return True, {'status': 'already_linked', 'username': username}
        conn.close()
        return False, {'status': 'token_already_used'}

    if expires_at_str and expires_at_str < now_str:
        cursor.execute('UPDATE registration_links SET is_used = TRUE WHERE token = ?', (token,))
        conn.commit()
        conn.close()
        return False, {'status': 'token_expired'}

    cursor.execute('SELECT username FROM users WHERE chat_id = ?', (chat_id,))
    existing_chat_user = cursor.fetchone()
    if existing_chat_user and existing_chat_user[0] != username:
        conn.close()
        return False, {'status': 'chat_in_use', 'username': existing_chat_user[0]}

    cursor.execute('SELECT chat_id FROM users WHERE username = ?', (username,))
    user_row = cursor.fetchone()

    if user_row:
        stored_chat_id = user_row[0]
        action = 'already_linked' if stored_chat_id == chat_id else 'updated_link'
        cursor.execute('''
            UPDATE users
            SET chat_id = ?, is_registered = TRUE, telegram_username = ?, linked_at = ?
            WHERE username = ?
        ''', (chat_id, telegram_username, now_str, username))
    else:
        action = 'linked_new'
        password_placeholder = hash_password(secrets.token_hex(16))
        cursor.execute('''
            INSERT INTO users (chat_id, username, password_hash, is_registered, telegram_username, linked_at)
            VALUES (?, ?, ?, TRUE, ?, ?)
        ''', (chat_id, username, password_placeholder, telegram_username, now_str))

    cursor.execute('''
        UPDATE registration_links
        SET chat_id = ?, claimed_at = ?, is_used = TRUE
        WHERE token = ?
    ''', (chat_id, now_str, token))

    conn.commit()
    conn.close()
    return True, {'status': action, 'username': username}





def process_linking_code(chat_id, code, telegram_username=None):
    code = (code or '').strip()
    if not code:
        return False

    success, payload = bind_token_to_chat(code, chat_id, telegram_username)
    if success:
        username = payload.get('username')
        status = payload.get('status')
        if status == 'updated_link':
            message_lines = [
                "Telegram link refreshed.",
                "",
                f"Username: `{username}`",
                "All monitoring alerts will arrive here."
            ]
        elif status == 'already_linked':
            message_lines = [
                "This chat is already linked to your PingTower account.",
                "",
                f"Username: `{username}`"
            ]
        else:
            message_lines = [
                "Your PingTower account is now linked to this chat.",
                "",
                f"Username: `{username}`",
                "We'll deliver alerts here."
            ]
        message = "\n".join(message_lines)
        bot.send_message(chat_id, message, parse_mode='Markdown')
        return True

    status = payload.get('status')
    if status == 'token_not_found':
        message = "We couldn't find an account with that code. Check the ID from the dashboard."
    elif status == 'token_already_used':
        message = "This code was already used from another chat. Request a new link from the dashboard."
    elif status == 'token_expired':
        message = "This link has expired. Generate a new code in the dashboard."
    elif status == 'chat_in_use':
        linked_username = payload.get('username', 'unknown')
        message = "This chat is already connected to `{}`. Disconnect it first in the bot.".format(linked_username)
    else:
        message = "Failed to link the chat. Try generating a fresh code in the dashboard."

    bot.send_message(chat_id, message, parse_mode='Markdown')
    return True


# Хранение временных данных регистрации
user_registration_data = {}

# Обработчик команды /start
@bot.message_handler(commands=['start'])
def start_command(message):
    chat_id = message.chat.id
    logger.info(f"Start command received from chat_id: {chat_id}")
    text = message.text or ''
    args = text.split(maxsplit=1)
    token = args[1].strip() if len(args) > 1 else None

    if token:
        telegram_username = getattr(message.from_user, 'username', None)
        if process_linking_code(chat_id, token, telegram_username):
            return




    if is_user_registered(chat_id):
        bot.send_message(
            chat_id,
            "{registered}",
        )
    else:
        markup = telebot.types.InlineKeyboardMarkup()
        markup.add(telebot.types.InlineKeyboardButton("{button}", callback_data="register"))

        bot.send_message(
            chat_id,
            "{unregistered}",
            reply_markup=markup,
            parse_mode='Markdown',
        )

@bot.callback_query_handler(func=lambda call: call.data == "register")
def registration_callback(call):
    chat_id = call.message.chat.id
    user_registration_data[chat_id] = {"step": "username"}

    bot.edit_message_text(
        chat_id=chat_id,
        message_id=call.message.message_id,
        text="📝 Регистрация в PingTower Bot\n\nВведите желаемый логин (не короче 3 символов):",
        parse_mode='Markdown',
    )

@bot.message_handler(func=lambda message: message.chat.id in user_registration_data)
def handle_registration(message):
    chat_id = message.chat.id
    text = (message.text or '').strip()

    if chat_id not in user_registration_data:
        return

    step = user_registration_data[chat_id]["step"]

    if step == "username":
        if len(text) < 3:
            bot.send_message(chat_id, "Имя пользователя должно содержать минимум 3 символа. Попробуйте ещё раз:")
            return

        user_registration_data[chat_id]["username"] = text
        user_registration_data[chat_id]["step"] = "password"
        bot.send_message(chat_id, "Введите пароль (от 6 символов):")

    elif step == "password":
        if len(text) < 6:
            bot.send_message(chat_id, "Пароль должен содержать минимум 6 символов. Попробуйте ещё раз:")
            return

        username = user_registration_data[chat_id]["username"]

        if register_user(chat_id, username, text):
            bot.send_message(
                chat_id,
                (
                    "✅ Регистрация завершена!\n\n"
                    f"Ваш логин: `{username}`\n"
                    "Теперь вы можете добавлять сервисы и получать уведомления.\n\n"
                    "Команда /help подскажет, что делать дальше."
                ),
                parse_mode='Markdown',
            )
        else:
            bot.send_message(
                chat_id,
                (
                    "⚠️ Не удалось завершить регистрацию.\n\n"
                    "Возможно, такое имя уже занято. Попробуйте другое или выполните вход, если аккаунт уже существует."
                ),
            )

        user_registration_data.pop(chat_id, None)

@bot.message_handler(commands=['status'])
def status_command(message):
    chat_id = message.chat.id

    if not is_user_registered(chat_id):
        bot.send_message(chat_id, "⚠️ Этот чат пока не привязан. Отправьте /start, чтобы завершить настройку.")
        return

    conn = sqlite3.connect('monitoring_bot.db')
    cursor = conn.cursor()
    cursor.execute('SELECT username, created_at FROM users WHERE chat_id = ?', (chat_id,))
    username, created_at = cursor.fetchone()

    cursor.execute('SELECT COUNT(*) FROM user_services WHERE chat_id = ?', (chat_id,))
    services_count = cursor.fetchone()[0]
    conn.close()

    message_text = (
        "📊 Состояние подключения к PingTower\n\n"
        f"Имя пользователя: `{username}`\n"
        f"Количество сервисов: {services_count}\n"
        f"Дата регистрации: {created_at[:10]}\n"
        "Уведомления продолжают приходить в этот чат."
    )

    bot.send_message(chat_id, message_text, parse_mode='Markdown')

@bot.message_handler(commands=['help'])
def help_command(message):
    help_text = """
🤖 **PingTower Monitoring Bot — справка**

**Основные команды:**
/start — привязать Telegram-аккаунт
/status — проверить статус подключения
/help — показать эту подсказку

**Как начать получать уведомления:**
1. Зарегистрируйтесь на сайте PingTower.
2. Откройте ссылку из личного кабинета и нажмите Start в боте.
3. Настройте сервисы и каналы уведомлений.

**Нужна помощь?**
Напишите в командный чат или свяжитесь с поддержкой PingTower.
    """
    bot.send_message(message.chat.id, help_text, parse_mode='Markdown')

@bot.message_handler(func=lambda message: True)
def unknown_command(message):
    if not is_user_registered(message.chat.id):
        bot.send_message(
            message.chat.id,
            "⚠️ Чтобы начать, отправьте команду /start и привяжите Telegram.",
        )
    else:
        bot.send_message(
            message.chat.id,
            "Команда не распознана. Используйте /help, чтобы увидеть доступные команды.",
        )

@bot.message_handler(func=lambda message: True)
def unknown_command(message):
    if not is_user_registered(message.chat.id):
        bot.send_message(message.chat.id, 
            "❓ Для начала работы используйте команду /start")
    else:
        bot.send_message(message.chat.id,
            "❓ Неизвестная команда. Используйте /help для просмотра доступных команд.")

# Запуск Flask в отдельном потоке
def run_flask():
    port = int(os.getenv('FLASK_PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=False)

if __name__ == '__main__':
    try:
        # Инициализация базы данных
        init_database()
        
        # Запуск Flask API в отдельном потоке
        flask_thread = threading.Thread(target=run_flask)
        flask_thread.daemon = True
        flask_thread.start()
        
        logger.info("рџ¤– PingTower Bot started successfully!")
        logger.info(f"рџ"Ў Bot available at: https://t.me/PingTower_tax_bot")
        logger.info(f"рџ"Њ API endpoint: http://localhost:5000/send_notification")
        logger.info(f"вќ¤пёЏ Health check: http://localhost:5000/health")
        
        # Запуск бота
        bot.polling(none_stop=True, interval=0)
        
    except Exception as e:
        logger.error(f"Failed to start bot: {e}")

