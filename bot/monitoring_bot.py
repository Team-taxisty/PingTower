# -*- coding: utf-8 -*-
import os
import sqlite3
import hashlib
import secrets
import logging
from datetime import datetime
import threading

from flask import Flask, request, jsonify
from dotenv import load_dotenv
import telebot

# Внешняя зависимость пользователя
from email_notifier import EmailNotifier

# =========================
# БАЗОВЫЕ НАСТРОЙКИ
# =========================
load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("pingtower_bot")

BOT_TOKEN = os.getenv("BOT_TOKEN", "").strip()
BOT_USERNAME = os.getenv("BOT_USERNAME", "PingTower_tax_bot").strip()

if not BOT_TOKEN:
    raise RuntimeError("BOT_TOKEN не задан. Укажите его в .env")

bot = telebot.TeleBot(BOT_TOKEN, parse_mode="HTML")
app = Flask(__name__)
email_notifier = EmailNotifier()

DB_PATH = os.getenv("DB_PATH", "monitoring_bot.db")


# =========================
# УТИЛИТЫ ДЛЯ БД
# =========================
def init_database():
    with sqlite3.connect(DB_PATH) as conn:
        cursor = conn.cursor()

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS users (
                chat_id INTEGER PRIMARY KEY,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                is_registered BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                telegram_username TEXT,
                linked_at TIMESTAMP
            )
            """
        )

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS user_services (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                chat_id INTEGER,
                service_name TEXT NOT NULL,
                service_url TEXT,
                FOREIGN KEY (chat_id) REFERENCES users (chat_id)
            )
            """
        )

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS registration_links (
                token TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP,
                chat_id INTEGER,
                claimed_at TIMESTAMP,
                is_used BOOLEAN DEFAULT FALSE
            )
            """
        )
        cursor.execute(
            "CREATE INDEX IF NOT EXISTS idx_registration_links_username ON registration_links(username)"
        )

    logger.info("База данных инициализирована")


def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode("utf-8")).hexdigest()


def is_user_registered(chat_id: int) -> bool:
    with sqlite3.connect(DB_PATH) as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT is_registered FROM users WHERE chat_id = ?", (chat_id,))
        row = cursor.fetchone()
    return bool(row and row[0])


def register_user(chat_id: int, username: str, password: str) -> bool:
    password_hash = hash_password(password)
    try:
        with sqlite3.connect(DB_PATH) as conn:
            cursor = conn.cursor()
            cursor.execute(
                """
                INSERT INTO users (chat_id, username, password_hash, is_registered)
                VALUES (?, ?, ?, TRUE)
                """,
                (chat_id, username, password_hash),
            )
            conn.commit()
        logger.info("Пользователь %s зарегистрирован (chat_id=%s)", username, chat_id)
        return True
    except sqlite3.IntegrityError:
        logger.warning("Не удалось зарегистрировать %s: логин уже существует", username)
        return False


def cleanup_expired_registration_links(cursor):
    """Помечаем просроченные ссылки как использованные (чтобы не переиспользовать)."""
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    cursor.execute(
        """
        UPDATE registration_links
        SET is_used = TRUE
        WHERE expires_at IS NOT NULL
          AND is_used = FALSE
          AND expires_at < ?
        """,
        (now_str,),
    )


def store_registration_link(username: str, token: str) -> str:
    token = (token or "").strip()
    if not token:
        raise ValueError("token is required")

    with sqlite3.connect(DB_PATH) as conn:
        cursor = conn.cursor()
        cleanup_expired_registration_links(cursor)
        cursor.execute(
            "DELETE FROM registration_links WHERE username = ? OR token = ?",
            (username, token),
        )
        cursor.execute(
            """
            INSERT INTO registration_links (token, username, is_used)
            VALUES (?, ?, FALSE)
            """,
            (token, username),
        )
        conn.commit()

    logger.info("Подготовлен токен регистрации для %s -> %s", username, token)
    return token


from typing import Optional

def bind_token_to_chat(token: str, chat_id: int, telegram_username: Optional[str] = None):
    """Поглощаем токен и привязываем Telegram-чат к аккаунту платформы."""
    with sqlite3.connect(DB_PATH) as conn:
        cursor = conn.cursor()
        cleanup_expired_registration_links(cursor)

        cursor.execute(
            "SELECT username, expires_at, is_used, chat_id FROM registration_links WHERE token = ?",
            (token,),
        )
        row = cursor.fetchone()
        if not row:
            return False, {"status": "token_not_found"}

        username, expires_at_str, is_used, token_chat_id = row
        now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        # токен уже использован
        if is_used:
            if token_chat_id == chat_id:
                return True, {"status": "already_linked", "username": username}
            return False, {"status": "token_already_used"}

        # токен просрочен
        if expires_at_str and expires_at_str < now_str:
            cursor.execute("UPDATE registration_links SET is_used = TRUE WHERE token = ?", (token,))
            conn.commit()
            return False, {"status": "token_expired"}

        # чат уже привязан к другому юзеру
        cursor.execute("SELECT username FROM users WHERE chat_id = ?", (chat_id,))
        existing_chat_user = cursor.fetchone()
        if existing_chat_user and existing_chat_user[0] != username:
            return False, {"status": "chat_in_use", "username": existing_chat_user[0]}

        # есть ли такой пользователь
        cursor.execute("SELECT chat_id FROM users WHERE username = ?", (username,))
        user_row = cursor.fetchone()

        if user_row:
            stored_chat_id = user_row[0]
            action = "already_linked" if stored_chat_id == chat_id else "updated_link"
            cursor.execute(
                """
                UPDATE users
                SET chat_id = ?, is_registered = TRUE, telegram_username = ?, linked_at = ?
                WHERE username = ?
                """,
                (chat_id, telegram_username, now_str, username),
            )
        else:
            action = "linked_new"
            password_placeholder = hash_password(secrets.token_hex(16))
            cursor.execute(
                """
                INSERT INTO users (chat_id, username, password_hash, is_registered, telegram_username, linked_at)
                VALUES (?, ?, ?, TRUE, ?, ?)
                """,
                (chat_id, username, password_placeholder, telegram_username, now_str),
            )

        cursor.execute(
            """
            UPDATE registration_links
            SET chat_id = ?, claimed_at = ?, is_used = TRUE
            WHERE token = ?
            """,
            (chat_id, now_str, token),
        )
        conn.commit()

    return True, {"status": action, "username": username}


def process_linking_code(chat_id: int, code: str, telegram_username: Optional[str] = None) -> bool:
    code = (code or "").strip()
    if not code:
        return False

    success, payload = bind_token_to_chat(code, chat_id, telegram_username)
    if success:
        username = payload.get("username")
        status = payload.get("status")

        if status == "updated_link":
            message = (
                "<b>Связь с аккаунтом обновлена</b>\n\n"
                f"Пользователь: <code>{username}</code>\n"
                "Все уведомления теперь будут приходить в этот чат."
            )
        elif status == "already_linked":
            message = (
                "Этот чат уже привязан к вашему аккаунту PingTower.\n\n"
                f"Пользователь: <code>{username}</code>"
            )
        else:  # linked_new
            message = (
                "<b>Ваш аккаунт PingTower привязан к этому чату</b>\n\n"
                f"Пользователь: <code>{username}</code>\n"
                "Будем доставлять уведомления сюда."
            )

        bot.send_message(chat_id, message)
        return True

    status = payload.get("status")
    if status == "token_not_found":
        message = "Не удалось найти аккаунт по этому коду. Проверьте код на панели управления."
    elif status == "token_already_used":
        message = "Этот код уже был использован из другого чата. Сгенерируйте новый на панели."
    elif status == "token_expired":
        message = "Ссылка устарела. Сгенерируйте новый код на панели."
    elif status == "chat_in_use":
        linked_username = payload.get("username", "неизвестно")
        message = f"Этот чат уже привязан к <code>{linked_username}</code>. Сначала отвяжите его."
    else:
        message = "Не удалось привязать чат. Попробуйте сгенерировать свежий код на панели."

    bot.send_message(chat_id, message)
    return True


# =========================
# СОСТОЯНИЕ РЕГИСТРАЦИИ В ЧАТЕ
# =========================
user_registration_data: dict[int, dict] = {}


# =========================
# ОБРАБОТЧИКИ ТЕЛЕГРАМ
# =========================
@bot.message_handler(commands=["start"])
def start_command(message: telebot.types.Message):
    chat_id = message.chat.id
    logger.info("Команда /start от chat_id=%s", chat_id)

    text = message.text or ""
    args = text.split(maxsplit=1)
    token = args[1].strip() if len(args) > 1 else None

    # deep-link ?start=<token>
    if token:
        telegram_username = getattr(message.from_user, "username", None)
        if process_linking_code(chat_id, token, telegram_username):
            return

    if is_user_registered(chat_id):
        bot.send_message(
            chat_id,
            "✅ Добро пожаловать в PingTower Bot!\n\n"
            "Вы уже зарегистрированы и будете получать уведомления о состоянии ваших сервисов.\n\n"
            "📋 Доступные команды:\n"
            "/status — проверить статус подключения\n"
            "/help — справка по использованию",
        )
    else:
        markup = telebot.types.InlineKeyboardMarkup()
        markup.add(telebot.types.InlineKeyboardButton("🔐 Пройти регистрацию", callback_data="register"))

        bot.send_message(
            chat_id,
            "👋 Добро пожаловать в <b>PingTower Monitoring Bot</b>!\n\n"
            "🔔 Этот бот предназначен для получения уведомлений о недоступности ваших сервисов.\n\n"
            "Чтобы начать, пройдите регистрацию по логину и паролю вашей системы мониторинга.",
            reply_markup=markup,
        )


@bot.callback_query_handler(func=lambda call: call.data == "register")
def registration_callback(call: telebot.types.CallbackQuery):
    chat_id = call.message.chat.id
    user_registration_data[chat_id] = {"step": "username"}

    bot.edit_message_text(
        chat_id=chat_id,
        message_id=call.message.message_id,
        text="📝 <b>Регистрация в PingTower Bot</b>\n\nВведите ваш логин:",
        parse_mode="HTML",
    )


@bot.message_handler(func=lambda m: m.chat.id in user_registration_data)
def handle_registration(message: telebot.types.Message):
    chat_id = message.chat.id
    text = (message.text or "").strip()

    if chat_id not in user_registration_data:
        return

    step = user_registration_data[chat_id]["step"]

    if step == "username":
        if len(text) < 3:
            bot.send_message(chat_id, "⚠️ Логин должен содержать минимум 3 символа. Попробуйте снова:")
            return

        user_registration_data[chat_id]["username"] = text
        user_registration_data[chat_id]["step"] = "password"
        bot.send_message(chat_id, "🔑 Теперь введите пароль (минимум 6 символов):")

    elif step == "password":
        if len(text) < 6:
            bot.send_message(chat_id, "⚠️ Пароль должен содержать минимум 6 символов. Попробуйте снова:")
            return

        username = user_registration_data[chat_id]["username"]

        if register_user(chat_id, username, text):
            bot.send_message(
                chat_id,
                "✅ <b>Регистрация успешно завершена!</b>\n\n"
                f"👤 Логин: <code>{username}</code>\n"
                "🔔 Теперь вы будете получать уведомления о состоянии ваших сервисов.\n\n"
                "Используйте /help для просмотра доступных команд.",
            )
        else:
            bot.send_message(
                chat_id,
                "❌ <b>Ошибка регистрации</b>\n\n"
                "Возможно, пользователь с таким логином уже существует. "
                "Попробуйте другой логин или обратитесь к администратору.",
            )

        # очищаем состояние
        del user_registration_data[chat_id]


# Сообщение, которое похоже на код привязки (все цифры) и НЕ в процессе регистрации
@bot.message_handler(func=lambda m: (m.text or "").strip().isdigit() and m.chat.id not in user_registration_data)
def handle_link_code_message(message: telebot.types.Message):
    telegram_username = getattr(message.from_user, "username", None)
    process_linking_code(message.chat.id, message.text, telegram_username)


@bot.message_handler(commands=["status"])
def status_command(message: telebot.types.Message):
    chat_id = message.chat.id

    if not is_user_registered(chat_id):
        bot.send_message(chat_id, "❌ Вы не зарегистрированы. Используйте /start для регистрации.")
        return

    with sqlite3.connect(DB_PATH) as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT username, created_at FROM users WHERE chat_id = ?", (chat_id,))
        user_row = cursor.fetchone()

        cursor.execute("SELECT COUNT(*) FROM user_services WHERE chat_id = ?", (chat_id,))
        services_count = cursor.fetchone()[0]

    username, created_at = user_row
    created_date = (created_at or "")[:10]

    bot.send_message(
        chat_id,
        "📊 <b>Статус подключения к PingTower</b>\n\n"
        f"👤 Пользователь: <code>{username}</code>\n"
        f"🧩 Активных сервисов: <b>{services_count}</b>\n"
        f"📅 Дата регистрации: <b>{created_date}</b>\n"
        "✅ Бот активен и готов принимать уведомления.",
    )


@bot.message_handler(commands=["help"])
def help_command(message: telebot.types.Message):
    help_text = (
        "🤝 <b>PingTower Monitoring Bot — справка</b>\n\n"
        "📋 <b>Доступные команды:</b>\n"
        "• /start — начало работы и регистрация\n"
        "• /status — проверить статус подключения\n"
        "• /help — показать эту справку\n\n"
        "🔔 <b>Как работают уведомления:</b>\n"
        "• Вы получаете только свои персональные уведомления\n"
        "• Уведомления приходят при падении сервисов\n"
        "• Уведомления приходят и при восстановлении\n\n"
        "🧠 <b>Принцип работы:</b>\n"
        "1️⃣ Регистрируетесь в боте по логину/паролю\n"
        "2️⃣ Ваша система мониторинга отправляет события в бот\n"
        "3️⃣ Получаете уведомления только по своим сервисам\n\n"
        "🛠️ <b>API для интеграции:</b>\n"
        "<code>POST /send_notification</code>\n"
        "{\n"
        '  "username": "ваш_логин",\n'
        '  "service_name": "Имя сервиса",\n'
        '  "service_url": "https://example.com",\n'
        '  "status": "down",\n'
        '  "message": "Дополнительная информация"\n'
        "}\n\n"
        "🆘 <b>Поддержка:</b>\n"
        "При проблемах обратитесь к администратору системы мониторинга."
    )
    bot.send_message(message.chat.id, help_text)


@bot.message_handler(func=lambda m: True)
def unknown_command(message: telebot.types.Message):
    if not is_user_registered(message.chat.id):
        bot.send_message(message.chat.id, "ℹ️ Для начала работы используйте команду /start")
    else:
        bot.send_message(message.chat.id, "ℹ️ Неизвестная команда. Используйте /help для списка команд.")


# =========================
# FLASK API
# =========================
@app.route("/generate_link", methods=["POST"])
def generate_link():
    try:
        data = request.json or {}
        username = (data.get("username") or "").strip()
        token = data.get("token") or data.get("user_id") or data.get("id")

        if not username:
            return jsonify({"error": "username is required"}), 400
        if token is None:
            return jsonify({"error": "token is required"}), 400

        token = str(token).strip()
        if not token:
            return jsonify({"error": "token is required"}), 400

        try:
            store_registration_link(username, token)
        except ValueError:
            return jsonify({"error": "token is required"}), 400

        link = f"https://t.me/{BOT_USERNAME}?start={token}"

        with sqlite3.connect(DB_PATH) as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT chat_id FROM users WHERE username = ?", (username,))
            row = cursor.fetchone()

        response_body = {
            "link": link,
            "token": token,
            "bot_username": BOT_USERNAME,
            "already_linked": bool(row and row[0]),
        }
        if row and row[0]:
            response_body["chat_id"] = row[0]

        logger.info("Подготовлена Telegram-ссылка для %s (token=%s)", username, token)
        return jsonify(response_body), 200
    except Exception as e:
        logger.exception("Ошибка при генерации ссылки: %s", e)
        return jsonify({"error": "Internal server error"}), 500


@app.route("/send_notification", methods=["POST"])
def send_notification():
    """
    Тело запроса:
    {
        "username": "user1",
        "service_name": "API Backend",
        "service_url": "https://api.example.com",
        "status": "down" | "up",
        "message": "доп. сведения",
        "email": "optional@example.com"
    }
    """
    try:
        data = request.json or {}
        username = (data.get("username") or "").strip()
        service_name = (data.get("service_name") or "").strip()
        service_url = (data.get("service_url") or "").strip()
        status = (data.get("status") or "down").strip().lower()
        message_text = (data.get("message") or "").strip()

        if not username or not service_name:
            return jsonify({"error": "Username и service_name обязательны"}), 400

        with sqlite3.connect(DB_PATH) as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT chat_id FROM users WHERE username = ? AND is_registered = TRUE",
                (username,),
            )
            row = cursor.fetchone()

        if not row:
            logger.warning("Уведомление не отправлено — пользователь %s не найден/не зарегистрирован", username)
            return jsonify({"error": "User not found or not registered"}), 404

        chat_id = row[0]
        status_emoji = "🔴" if status == "down" else "🟢"
        status_text = "❌ Недоступен" if status == "down" else "✅ Восстановлен"
        timestamp = datetime.now().strftime("%d.%m.%Y %H:%M:%S")

        notification_lines = [
            f"{status_emoji} <b>Уведомление PingTower</b>",
            "",
            f"🏷️ <b>Сервис:</b> <code>{service_name}</code>",
        ]
        if service_url:
            notification_lines.append(f"🔗 <b>URL:</b> {service_url}")
        notification_lines.append(f"📊 <b>Статус:</b> {status_text}")
        if message_text:
            notification_lines.append(f"💬 <b>Детали:</b> {message_text}")
        notification_lines.append(f"🕘 <b>Время:</b> {timestamp}")

        bot.send_message(chat_id, "\n".join(notification_lines))

        # Почтовое уведомление (опционально)
        user_email = (data.get("email") or "").strip()
        if user_email:
            try:
                email_success = email_notifier.send_notification(
                    to_email=user_email,
                    username=username,
                    service_name=service_name,
                    service_url=service_url,
                    status=status,
                    message=message_text,
                )
                if email_success:
                    logger.info("Email-уведомление отправлено на %s", user_email)
                else:
                    logger.warning("Не удалось отправить email на %s", user_email)
            except Exception as e:
                logger.warning("Ошибка отправки email (%s): %s", user_email, e)

        logger.info("Уведомление отправлено пользователю %s (chat_id=%s)", username, chat_id)
        return jsonify({"success": True, "message": "Notification sent successfully"}), 200

    except Exception as e:
        logger.exception("Ошибка отправки уведомления: %s", e)
        return jsonify({"error": "Internal server error"}), 500


@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "healthy", "bot": BOT_USERNAME}), 200


# =========================
# ЗАПУСК
# =========================
def run_flask():
    port = int(os.getenv("FLASK_PORT", 5000))
    # debug=False обязательно, т.к. запускаем в отдельном потоке
    app.run(host="0.0.0.0", port=port, debug=False)


if __name__ == "__main__":
    try:
        init_database()

        # Flask API в отдельном потоке
        flask_thread = threading.Thread(target=run_flask, daemon=True)
        flask_thread.start()

        logger.info("🤝 PingTower Bot запущен")
        logger.info("📨 Bot: https://t.me/%s", BOT_USERNAME)
        logger.info("🛰️  API endpoint: http://localhost:%s/send_notification", os.getenv("FLASK_PORT", 5000))
        logger.info("❤️  Health: http://localhost:%s/health", os.getenv("FLASK_PORT", 5000))

        # Запуск бота (long polling)
        bot.polling(none_stop=True, interval=0)

    except Exception as e:
        logger.exception("Не удалось запустить бота: %s", e)
