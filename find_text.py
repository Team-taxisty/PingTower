import pathlib
text = pathlib.Path('bot/monitoring_bot.py').read_text(encoding='utf-8')
needle = 'Р”РѕР±СЂРѕ'
print('found at', text.find(needle))
