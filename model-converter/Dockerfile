FROM tensorflow/tensorflow:2.4.1

WORKDIR /app
RUN pip install web.py tf2onnx
COPY . /app

ENTRYPOINT ["python3", "main.py"]
