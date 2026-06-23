import os
# FORCE CPU MODE AND KERAS LEGACY (Before any other import)
os.environ["CUDA_VISIBLE_DEVICES"] = "-1"
os.environ["TF_USE_LEGACY_KERAS"] = "1"
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"

from flask import Flask, request, jsonify
import base64
from io import BytesIO
from PIL import Image
import numpy as np
from datetime import datetime
import tensorflow as tf
import requests

# --- CONFIGURATION ---
SAMPLES_DIR = 'samples'
MODEL_PATH = 'models/best_accuracy.keras'
CLASSES = ['blue', 'brown', 'gray', 'green', 'yellow']
NODE_RED_URL = 'http://172.20.10.4:1880/flask-upload'

app = Flask(__name__)

# --- MODEL LOADING ---
print(" model on CPU...")
try:
    # Loaded with compile=False because the .keras file contains custom loss functions
    # ('combined') which are not needed for inference.
    model = tf.keras.models.load_model(MODEL_PATH, compile=False)
    print("MODEL LOADED SUCCESSFULLY.")
except Exception as e:
    print(f"CRITICAL FAILURE LOADING MODEL: {e}")
    model = None

if not os.path.exists(SAMPLES_DIR):
    os.makedirs(SAMPLES_DIR)

def prepare_image(image_pil):
    """
    Prepares the image for the MobileNetV2 model.
    IMPORTANT: We do not divide by 255.0 because the model already includes
    an internal Rescaling(1./127.5, offset=-1) layer.
    """
    # 1. Resize to the expected model input size (224x224)
    img = image_pil.resize((224, 224))
    
    # 2. Convert to numpy array (values between 0 and 255)
    img_array = tf.keras.preprocessing.image.img_to_array(img)
    
    # 3. Add batch dimension: (224, 224, 3) -> (1, 224, 224, 3)
    img_array = np.expand_dims(img_array, axis=0)
    
    return img_array

@app.route('/api/predict', methods=['POST'])
def predict():
    try:
        if model is None:
            return jsonify({'error': 'Model not loaded on server'}), 500

        data = request.get_json()
        if not data or 'image' not in data:
            return jsonify({'error': 'No image data provided'}), 400

        # Extract binId (default '1')
        bin_id = data.get('binId', '1')

        # 1. Decode Base64
        image_b64 = data['image']
        if "," in image_b64: 
            image_b64 = image_b64.split(",")[1]
        
        image_data = base64.b64decode(image_b64)
        image_pil = Image.open(BytesIO(image_data)).convert('RGB')

        # 2. Save sample for auditing
        filename = f"sample_{datetime.now().strftime('%Y%m%d_%H%M%S')}.jpg"
        file_path = os.path.join(SAMPLES_DIR, filename)
        image_pil.save(file_path)

        # --- SEND TO NODE-RED ---
        try:
            print(f"Sending copy to Node-RED (Bin ID: {bin_id})...")
            with open(file_path, 'rb') as img_file:
                files_payload = {'file': img_file}
                data_payload = {'binId': bin_id}
                requests.post(NODE_RED_URL, files=files_payload, data=data_payload)
                print("Sent to Node-RED successfully.")
        except Exception as e_node:
            print(f"⚠ Error sending to Node-RED: {e_node}")
        # ------------------------

        # 3. Preprocess and Predict
        ready_img = prepare_image(image_pil)
        
        # Perform prediction
        preds = model.predict(ready_img, verbose=0)
        
        # 4. Analyze results
        idx = np.argmax(preds[0])
        confidence = float(preds[0][idx])
        detected_class = CLASSES[idx]

        print("--- New Prediction ---")
        print(f"Image: {filename}")
        print(f"Probabilities: {preds[0]}")
        print(f"Result: {detected_class} ({confidence*100:.2f}%)")

        #os.remove(ready_img)

        return jsonify({
            'status': 'success',
            'class': detected_class,
            'confidence': confidence,
            'timestamp': datetime.now().isoformat()
        })

    except Exception as e:
        print(f"Error during prediction: {e}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':  # pragma: no cover
    print("FLASK SERVER STARTED")
    print(f"Pointing Node-RED to: {NODE_RED_URL}")
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=False)
