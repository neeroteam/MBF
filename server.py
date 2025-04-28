from flask import Flask, send_file, abort, send_from_directory, jsonify
import os
import json

app = Flask(__name__)

# Базовый путь к медиафайлам
MEDIA_ROOT = '.'

CATEGORIES = {
    'movies': 'Фильмы',
    'series': 'Сериалы',
    'videos': 'Видео'
}

@app.route('/categories.json')
def categories_json():
    return jsonify(CATEGORIES)

@app.route('/<category>/<path:filename>')
def serve_file(category, filename):
    if category not in CATEGORIES:
        abort(404)
        
    # Ищем файл в соответствующей папке категории
    file_path = os.path.join(MEDIA_ROOT, category, filename)
    if os.path.isfile(file_path):
        # Определяем MIME-тип файла
        if filename.lower().endswith('.png'):
            return send_file(file_path, mimetype='image/png')
        elif filename.lower().endswith('.jpg') or filename.lower().endswith('.jpeg'):
            return send_file(file_path, mimetype='image/jpeg')
        elif filename.lower().endswith('.mkv'):
            return send_file(file_path, mimetype='video/x-matroska')
        elif filename.lower().endswith('.mp4'):
            return send_file(file_path, mimetype='video/mp4')
        else:
            return send_file(file_path)
    abort(404)

@app.route('/<category>/content.json')
def category_content(category):
    if category not in CATEGORIES:
        abort(404)
        
    # Используем content.json из папки категории
    content_path = os.path.join(MEDIA_ROOT, category, 'content.json')
    if os.path.isfile(content_path):
        return send_file(content_path, mimetype='application/json')
    abort(404)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)