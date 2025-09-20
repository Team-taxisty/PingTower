import os
from flask import Flask, request, jsonify
import requests
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

BOT_TOKEN = os.getenv('BOT_TOKEN', '7479905970:AAGRDlnda26Aw6Bfbq9HyaPWFmOQt1aAb3o')
app = Flask(__name__)

# Список чатов для уведомлений (можно добавлять через API)
notification_chats = [
    # Добавьте ваши chat_id здесь, например:
    # 123456789,  # ваш chat_id
    # -1001234567890,  # группа/канал
]

@app.route('/health')
def health():
    return jsonify({
        "status": "ok", 
        "service": "pingtower-bot-api",
        "bot_token_set": bool(BOT_TOKEN),
        "chats_configured": len(notification_chats),
        "version": "1.1.0"
    })

@app.route('/add_chat', methods=['POST'])
def add_chat():
    """Добавить чат для уведомлений"""
    data = request.get_json()
    chat_id = data.get('chat_id')
    
    if chat_id:
        chat_id = int(chat_id)
        if chat_id not in notification_chats:
            notification_chats.append(chat_id)
            logger.info(f"Added chat {chat_id} to notifications")
            return jsonify({"status": "added", "chat_id": chat_id, "total_chats": len(notification_chats)})
        else:
            return jsonify({"status": "already_exists", "chat_id": chat_id})
    
    return jsonify({"error": "chat_id required"}), 400

@app.route('/chats')
def list_chats():
    """Показать все чаты"""
    return jsonify({
        "chats": notification_chats,
        "count": len(notification_chats)
    })

@app.route('/send_notification', methods=['POST'])
def send_notification():
    try:
        data = request.get_json()
        logger.info(f"Received notification: {data}")
        
        service_name = data.get('service_name', 'Unknown Service')
        status = data.get('status', 'unknown')
        message = data.get('message', '')
        service_url = data.get('service_url', '')
        
        # Emoji для статуса
        emoji = {
            'down': '🚨',
            'up': '✅', 
            'warning': '⚠️',
            'unknown': '❓'
        }.get(status, '📊')
        
        # Формировать сообщение для Telegram
        text = f"{emoji} *{service_name}*\nStatus: {status.upper()}"
        
        if service_url:
            text += f"\nURL: {service_url}"
        if message:
            text += f"\nDetails: {message}"
        
        text += f"\n\n🕐 {data.get('timestamp', 'now')}"
        
        # Отправить во все чаты
        results = []
        telegram_api_url = f"https://api.telegram.org/bot{BOT_TOKEN}/sendMessage"
        
        if not notification_chats:
            logger.warning("No chats configured for notifications")
            return jsonify({
                "status": "no_chats",
                "message": "No chats configured. Use POST /add_chat with chat_id",
                "prepared_message": text
            })
        
        for chat_id in notification_chats:
            try:
                payload = {
                    'chat_id': chat_id,
                    'text': text,
                    'parse_mode': 'Markdown'
                }
                
                logger.info(f"Sending to chat {chat_id}: {text[:50]}...")
                response = requests.post(telegram_api_url, json=payload, timeout=10)
                
                if response.status_code == 200:
                    logger.info(f"Successfully sent to chat {chat_id}")
                    results.append({"chat_id": chat_id, "success": True})
                else:
                    logger.error(f"Failed to send to chat {chat_id}: {response.status_code} - {response.text}")
                    results.append({"chat_id": chat_id, "success": False, "error": response.text})
                    
            except Exception as e:
                logger.error(f"Exception sending to chat {chat_id}: {e}")
                results.append({"chat_id": chat_id, "success": False, "error": str(e)})
        
        success_count = sum(1 for r in results if r.get('success'))
        
        return jsonify({
            "status": "sent", 
            "message": text,
            "results": results,
            "chats_total": len(notification_chats),
            "chats_success": success_count
        })
        
    except Exception as e:
        logger.error(f"Error in send_notification: {e}")
        return jsonify({
            "status": "error",
            "error": str(e)
        }), 500

@app.route('/test_telegram')
def test_telegram():
    """Тестировать подключение к Telegram API"""
    try:
        url = f"https://api.telegram.org/bot{BOT_TOKEN}/getMe"
        response = requests.get(url, timeout=10)
        
        if response.status_code == 200:
            bot_info = response.json()
            return jsonify({
                "status": "ok",
                "telegram_connected": True,
                "bot_info": bot_info['result']
            })
        else:
            return jsonify({
                "status": "error",
                "telegram_connected": False,
                "error": response.text
            })
            
    except Exception as e:
        return jsonify({
            "status": "error", 
            "telegram_connected": False,
            "error": str(e)
        })

@app.route('/test')
def test():
    return jsonify({
        "message": "PingTower Bot API is working!",
        "endpoints": ["/health", "/send_notification", "/add_chat", "/chats", "/test_telegram", "/test"],
        "chats_configured": len(notification_chats)
    })

if __name__ == '__main__':
    logger.info("🤖 Starting PingTower Bot API with real Telegram integration")
    app.run(host='0.0.0.0', port=5000, debug=False)
