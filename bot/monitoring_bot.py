import telebot
import sqlite3
import hashlib
import secrets
import logging
from flask import Flask, request, jsonify
import threading
import os
from datetime import datetime
from dotenv import load_dotenv

# Загрузка переменных окружения
load_dotenv()

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Токен бота из .env файла
BOT_TOKEN = os.getenv('BOT_TOKEN', '7479905970:AAGRDlnda26Aw6Bfbq9HyaPWFmOQt1aAb3o')
bot = telebot.TeleBot(BOT_TOKEN)

# Flask приложение для API
app = Flask(__name__)

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

# Хранение временных данных регистрации
user_registration_data = {}

# Обработчик команды /start
@bot.message_handler(commands=['start'])
def start_command(message):
    chat_id = message.chat.id
    logger.info(f"Start command received from chat_id: {chat_id}")
    
    if is_user_registered(chat_id):
        bot.send_message(chat_id, 
            "✅ Добро пожаловать в PingTower Bot!\n\n"
            "Вы уже зарегистрированы и будете получать уведомления о состоянии ваших сервисов.\n\n"
            "📋 Доступные команды:\n"
            "/status - Проверить статус подключения\n"
            "/help - Справка по использованию")
    else:
        markup = telebot.types.InlineKeyboardMarkup()
        markup.add(telebot.types.InlineKeyboardButton("🔐 Пройти регистрацию", callback_data="register"))
        
        bot.send_message(chat_id, 
            "🤖 Добро пожаловать в **PingTower Monitoring Bot**!\n\n"
            "🔔 Этот бот предназначен для получения уведомлений о падении ваших сервисов.\n\n"
            "Для начала работы необходимо пройти регистрацию по логину и паролю от вашей системы мониторинга.",
            reply_markup=markup, parse_mode='Markdown')

# Обработчик кнопки регистрации
@bot.callback_query_handler(func=lambda call: call.data == "register")
def registration_callback(call):
    chat_id = call.message.chat.id
    user_registration_data[chat_id] = {"step": "username"}
    
    bot.edit_message_text(
        chat_id=chat_id,
        message_id=call.message.message_id,
        text="📝 **Регистрация в PingTower Bot**\n\n"
             "Введите ваш логин от системы мониторинга:",
        parse_mode='Markdown'
    )

# Обработчик текстовых сообщений для регистрации
@bot.message_handler(func=lambda message: message.chat.id in user_registration_data)
def handle_registration(message):
    chat_id = message.chat.id
    text = message.text.strip()
    
    if chat_id not in user_registration_data:
        return
    
    step = user_registration_data[chat_id]["step"]
    
    if step == "username":
        if len(text) < 3:
            bot.send_message(chat_id, "❌ Логин должен содержать минимум 3 символа. Попробуйте снова:")
            return
        
        user_registration_data[chat_id]["username"] = text
        user_registration_data[chat_id]["step"] = "password"
        bot.send_message(chat_id, "🔐 Теперь введите пароль (минимум 6 символов):")
        
    elif step == "password":
        if len(text) < 6:
            bot.send_message(chat_id, "❌ Пароль должен содержать минимум 6 символов. Попробуйте снова:")
            return
        
        username = user_registration_data[chat_id]["username"]
        
        if register_user(chat_id, username, text):
            bot.send_message(chat_id, 
                f"✅ **Регистрация успешно завершена!**\n\n"
                f"👤 Логин: `{username}`\n"
                f"🔔 Теперь вы будете получать уведомления о состоянии ваших сервисов.\n\n"
                f"Используйте /help для просмотра доступных команд.",
                parse_mode='Markdown')
        else:
            bot.send_message(chat_id, 
                "❌ **Ошибка регистрации**\n\n"
                "Возможно, пользователь с таким логином уже существует. "
                "Попробуйте использовать другой логин или обратитесь к администратору.")
        
        del user_registration_data[chat_id]

# Команда статуса
@bot.message_handler(commands=['status'])
def status_command(message):
    chat_id = message.chat.id
    
    if not is_user_registered(chat_id):
        bot.send_message(chat_id, "❌ Вы не зарегистрированы. Используйте /start для регистрации.")
        return
    
    conn = sqlite3.connect('monitoring_bot.db')
    cursor = conn.cursor()
    cursor.execute('SELECT username, created_at FROM users WHERE chat_id = ?', (chat_id,))
    user_data = cursor.fetchone()
    username, created_at = user_data
    
    cursor.execute('SELECT COUNT(*) FROM user_services WHERE chat_id = ?', (chat_id,))
    services_count = cursor.fetchone()[0]
    conn.close()
    
    bot.send_message(chat_id, 
        f"📊 **Статус подключения к PingTower**\n\n"
        f"👤 Пользователь: `{username}`\n"
        f"🔗 Активных сервисов: {services_count}\n"
        f"📅 Дата регистрации: {created_at[:10]}\n"
        f"✅ Бот активен и готов получать уведомления", parse_mode='Markdown')

# API для отправки уведомлений с веб-сайта
@app.route('/send_notification', methods=['POST'])
def send_notification():
    try:
        data = request.json
        username = data.get('username')
        service_name = data.get('service_name')
        service_url = data.get('service_url', '')
        status = data.get('status', 'down')
        message = data.get('message', '')
        
        if not username or not service_name:
            return jsonify({'error': 'Username and service_name are required'}), 400
        
        # Находим пользователя по логину
        conn = sqlite3.connect('monitoring_bot.db')
        cursor = conn.cursor()
        cursor.execute('SELECT chat_id FROM users WHERE username = ? AND is_registered = TRUE', (username,))
        result = cursor.fetchone()
        
        if not result:
            conn.close()
            logger.warning(f"Notification failed - user {username} not found or not registered")
            return jsonify({'error': 'User not found or not registered'}), 404
        
        chat_id = result[0]
        
        # Формируем сообщение
        status_emoji = "🔴" if status == "down" else "🟢"
        timestamp = datetime.now().strftime("%d.%m.%Y %H:%M:%S")
        
        notification_text = f"{status_emoji} **Уведомление PingTower**\n\n"
        notification_text += f"🏷️ **Сервис:** `{service_name}`\n"
        if service_url:
            notification_text += f"🔗 **URL:** {service_url}\n"
        notification_text += f"📊 **Статус:** {'❌ Недоступен' if status == 'down' else '✅ Восстановлен'}\n"
        if message:
            notification_text += f"💬 **Детали:** {message}\n"
        notification_text += f"⏰ **Время:** {timestamp}"
        
        # Отправляем уведомление
        bot.send_message(chat_id, notification_text, parse_mode='Markdown')
        
        conn.close()
        logger.info(f"Notification sent successfully to user {username} (chat_id: {chat_id})")
        return jsonify({'success': True, 'message': 'Notification sent successfully'}), 200
        
    except Exception as e:
        logger.error(f"Error sending notification: {e}")
        return jsonify({'error': 'Internal server error'}), 500

# API для проверки здоровья бота
@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'healthy', 'bot': 'PingTower_tax_bot'}), 200

# Команда помощи
@bot.message_handler(commands=['help'])
def help_command(message):
    help_text = """
🤖 **PingTower Monitoring Bot - Справка**

📋 **Доступные команды:**
• /start - Начать работу и регистрацию
• /status - Проверить статус подключения  
• /help - Показать эту справку

🔔 **Как работают уведомления:**
• Вы получаете только свои персональные уведомления
• Уведомления приходят при падении сервисов
• Уведомления о восстановлении работы

💡 **Принцип работы:**
1️⃣ Регистрируетесь в боте по логину/паролю
2️⃣ Ваша система мониторинга отправляет уведомления в бот
3️⃣ Получаете уведомления только о своих сервисах

🔧 **API для интеграции:**
POST /send_notification
{
"username": "ваш_логин",
"service_name": "Название сервиса",
"service_url": "https://example.com",
"status": "down",
"message": "Дополнительная информация"
}

🆘 **Поддержка:**
При проблемах обратитесь к администратору системы мониторинга.
    """
    bot.send_message(message.chat.id, help_text, parse_mode='Markdown')

# Обработчик неизвестных команд
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
        
        logger.info("🤖 PingTower Bot started successfully!")
        logger.info(f"📡 Bot available at: https://t.me/PingTower_tax_bot")
        logger.info(f"🔌 API endpoint: http://localhost:5000/send_notification")
        logger.info(f"❤️ Health check: http://localhost:5000/health")
        
        # Запуск бота
        bot.polling(none_stop=True, interval=0)
        
    except Exception as e:
        logger.error(f"Failed to start bot: {e}")

