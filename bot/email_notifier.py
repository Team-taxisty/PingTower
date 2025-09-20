import smtplib
import json
import os
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from datetime import datetime
from dotenv import load_dotenv

# Загрузка переменных окружения
load_dotenv()

class EmailNotifier:
    def __init__(self):
        self.smtp_host = os.getenv('SMTP_HOST', 'smtp.gmail.com')
        self.smtp_port = int(os.getenv('SMTP_PORT', '587'))
        self.username = os.getenv('EMAIL_USERNAME')
        self.password = os.getenv('EMAIL_PASSWORD')
        self.from_email = os.getenv('FROM_EMAIL')
        
    def send_notification(self, to_email, username, service_name, service_url, status, message):
        """Отправка email уведомления"""
        
        if not self.from_email:
            print("❌ Email не настроен. Проверьте переменные окружения.")
            return False
            
        try:
            # Создание сообщения
            msg = MIMEMultipart()
            msg['From'] = self.from_email
            msg['To'] = to_email
            
            # Тема письма
            status_emoji = "🔴" if status == "down" else "🟢"
            status_text = "❌ Недоступен" if status == "down" else "✅ Восстановлен"
            msg['Subject'] = f"{status_emoji} PingTower Alert: {service_name} - {status_text}"
            
            # Тело письма
            timestamp = datetime.now().strftime("%d.%m.%Y %H:%M:%S")
            
            html_body = f"""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px;">
                    <h2 style="color: #dc3545; margin-bottom: 20px;">
                        {status_emoji} Уведомление PingTower
                    </h2>
                    
                    <div style="background-color: white; padding: 20px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <h3 style="color: #333; margin-top: 0;">Детали сервиса</h3>
                        
                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #666;">👤 Пользователь:</td>
                                <td style="padding: 8px 0;">{username}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #666;">🏷️ Сервис:</td>
                                <td style="padding: 8px 0;">{service_name}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #666;">🔗 URL:</td>
                                <td style="padding: 8px 0;"><a href="{service_url}" style="color: #007bff;">{service_url}</a></td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #666;">📊 Статус:</td>
                                <td style="padding: 8px 0; color: {'#dc3545' if status == 'down' else '#28a745'}; font-weight: bold;">
                                    {status_text}
                                </td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #666;">💬 Сообщение:</td>
                                <td style="padding: 8px 0;">{message}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #666;">⏰ Время:</td>
                                <td style="padding: 8px 0;">{timestamp}</td>
                            </tr>
                        </table>
                    </div>
                    
                    <div style="margin-top: 20px; padding: 15px; background-color: #e9ecef; border-radius: 5px; font-size: 12px; color: #666;">
                        <p style="margin: 0;">Это автоматическое уведомление от системы мониторинга PingTower.</p>
                        <p style="margin: 5px 0 0 0;">Для управления уведомлениями обратитесь к администратору системы.</p>
                    </div>
                </div>
            </body>
            </html>
            """
            
            msg.attach(MIMEText(html_body, 'html', 'utf-8'))
            
            # Отправка письма
            server = smtplib.SMTP(self.smtp_host, self.smtp_port)
            if self.username and self.password:
                server.starttls()
                server.login(self.username, self.password)
            
            text = msg.as_string()
            server.sendmail(self.from_email, to_email, text)
            server.quit()
            
            print(f"✅ Email уведомление отправлено на {to_email}")
            return True
            
        except Exception as e:
            print(f"❌ Ошибка отправки email: {e}")
            return False

# Функция для тестирования
def test_email_notification():
    """Тест отправки email уведомления"""
    notifier = EmailNotifier()
    
    # Тестовые данные
    success = notifier.send_notification(
        to_email="test@example.com",  # Замените на ваш email
        username="testuser",
        service_name="Test Web Server",
        service_url="https://example.com",
        status="down",
        message="Тестовое уведомление - сервер не отвечает"
    )
    
    if success:
        print("🎉 Email уведомление работает!")
    else:
        print("❌ Email уведомление не работает. Проверьте настройки.")

if __name__ == "__main__":
    test_email_notification()
