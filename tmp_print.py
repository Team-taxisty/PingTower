from pathlib import Path
import sys
sys.stdout.reconfigure(encoding='utf-8')
text = Path('bot/monitoring_bot.py').read_text(encoding='utf-8')
start = text.index('help_text = """')
end = text.index('"""', start + 15) + 3
print(text[start:end])
